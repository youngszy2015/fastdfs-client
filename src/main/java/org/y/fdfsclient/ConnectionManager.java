package org.y.fdfsclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.y.fdfsclient.command.Command;
import org.y.fdfsclient.exception.FastdfsClientException;
import org.y.fdfsclient.util.IpPortInfo;
import org.y.fdfsclient.util.Assert;
import org.y.fdfsclient.util.IpParseUtil;

import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionManager {

    private static Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

    private FixedChannelPool trackerChannelPool;

    private FdfsClientConfig config;
    //    private FixedChannelPool storageChannelPool;
    private Random random = new Random();

    private Map<String/**groupName*/, Map<SocketAddress /**tracker addr*/, FixedChannelPool>> storageChannelPool = new ConcurrentHashMap<>();

    private AtomicBoolean trackerInit = new AtomicBoolean(false);
    private AtomicBoolean storageInit = new AtomicBoolean(false);

    private static AttributeKey<Command> COMMAND_ATTR = AttributeKey.valueOf("COMMAND_ATTR");

    public ConnectionManager(FdfsClientConfig config) {
        String trackerAddr = config.getTrackerAddr();
        this.config = config;
        Assert.notEmpty(trackerAddr, "tracker addr not be null");
        if (!trackerInit.get()) {
            //init tracker channel pool
            IpPortInfo ipPort = IpParseUtil.getIpPort(trackerAddr);
            initTrackerPool(ipPort);
        }
    }

    public void initStorageChannelPool(String groupName, SocketAddress socketAddress) {
        if (!storageInit.get()) {
            //init storage channel pool
            FixedChannelPool fixedChannelPool = initStorageChannelPool(socketAddress);
            Map<SocketAddress, FixedChannelPool> spool = new ConcurrentHashMap<>();
            spool.put(socketAddress, fixedChannelPool);
            storageChannelPool.put(groupName, spool);
            logger.info("init storage channel pool,groupName:{},addr:{}", groupName, socketAddress);
        }
    }

    public Channel getTrackerChannel() throws FastdfsClientException {
        try {
            Channel channel = trackerChannelPool.acquire().get();
            return channel;
        } catch (Exception e) {
            logger.error("fetch tracker channel err:", e);
            throw new FastdfsClientException(e);
        }
    }

    public AttributeKey<Command> getCommandAttr() {
        return COMMAND_ATTR;
    }


    public Channel getStorageChannel(String groupName) throws FastdfsClientException {
        Map<SocketAddress, FixedChannelPool> fixedChannelPoolMap = storageChannelPool.get(groupName);
        ArrayList<SocketAddress> arr = new ArrayList<>(fixedChannelPoolMap.keySet());
        SocketAddress socketAddress = arr.get(random.nextInt(arr.size()));
        FixedChannelPool fixedChannelPool = fixedChannelPoolMap.get(socketAddress);
        try {
            return fixedChannelPool.acquire().get();
        } catch (Exception e) {
            logger.error("fetch storage channel err:", e);
            throw new FastdfsClientException(e);
        }
    }

    public void releaseChannel(Channel channel, boolean tracker, String groupName) {
        if (null != channel) {
            if (tracker) {
                trackerChannelPool.release(channel);
            } else {
                Map<SocketAddress, FixedChannelPool> fixedChannelPoolMap = storageChannelPool.get(groupName);
                SocketAddress socketAddress = channel.remoteAddress();
                FixedChannelPool fixedChannelPool = fixedChannelPoolMap.get(socketAddress);
                fixedChannelPool.release(channel);
            }
        }

    }


    public void close() {
        trackerChannelPool.close();
        Set<String> groupNames = storageChannelPool.keySet();
        for (String groupName : groupNames) {
            Map<SocketAddress, FixedChannelPool> spoolMap = storageChannelPool.get(groupName);
            Set<SocketAddress> spools = spoolMap.keySet();
            for (SocketAddress spool : spools) {
                FixedChannelPool fixedChannelPool = spoolMap.get(spool);
                fixedChannelPool.close();
            }
        }
    }


    private FixedChannelPool initStorageChannelPool(SocketAddress socketAddress) {
        Bootstrap storageBootstrap = new Bootstrap();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(config.getStoragePoolMaxCount() <= 0 ? Runtime.getRuntime().availableProcessors() * 2 :
                config.getStoragePoolMaxCount(), new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("FastdfsClient-Storage-NettyWorker_%d", this.threadIndex.incrementAndGet()));
            }
        });
        storageBootstrap.group(workerGroup)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectionTimeout())
                .option(ChannelOption.SO_TIMEOUT, config.getSoTimeout())
                .channel(NioSocketChannel.class)
                .remoteAddress(socketAddress);
        FixedChannelPool channelPool = new FixedChannelPool(storageBootstrap, new ChannelPoolHandler() {
            @Override
            public void channelReleased(io.netty.channel.Channel ch) throws Exception {
                logger.info("storage channel pool released channel,id:{}", ch.id().asShortText());
            }

            @Override
            public void channelAcquired(io.netty.channel.Channel ch) throws Exception {
                logger.info("storage channel pool acquired channel,id:{}", ch.id().asShortText());
            }

            @Override
            public void channelCreated(io.netty.channel.Channel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("FdfsClientStorageHandler", new FdfsHandler());
                pipeline.addLast(new IdleStateHandler(0, 0, config.getReadWriteIdleTime()));
                logger.info("storage channel pool create channel,id:{}", ch.id().asShortText());
            }
        }, Runtime.getRuntime().availableProcessors());

        return channelPool;
    }


    private void initTrackerPool(IpPortInfo ipPort) {
        Bootstrap trackerBootstrap = new Bootstrap();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(config.getTrackerPoolMaxCount() <= 0 ? Runtime.getRuntime().availableProcessors() * 2 :
                config.getTrackerPoolMaxCount(), new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("FastdfsClient-tracker-NettyWorker_%d", this.threadIndex.incrementAndGet()));
            }
        });
        trackerBootstrap.group(workerGroup)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectionTimeout())
                .option(ChannelOption.SO_TIMEOUT, config.getSoTimeout())
                .channel(NioSocketChannel.class)
                .remoteAddress(ipPort.getIp(), ipPort.getPort());
        trackerChannelPool = new FixedChannelPool(trackerBootstrap, new ChannelPoolHandler() {
            @Override
            public void channelReleased(io.netty.channel.Channel ch) throws Exception {
                logger.info("tracker channel pool released channel,id:{}", ch.id().asShortText());
            }

            @Override
            public void channelAcquired(io.netty.channel.Channel ch) throws Exception {
                logger.info("tracker channel pool acquired channel,id:{}", ch.id().asShortText());
            }

            @Override
            public void channelCreated(io.netty.channel.Channel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("FdfsTrackerStorageHandler", new FdfsHandler());
                pipeline.addLast(new IdleStateHandler(0, 0, config.getReadWriteIdleTime()));
                logger.info("tarcker channel pool create channel,id:{}", ch.id().asShortText());
            }
        }, Runtime.getRuntime().availableProcessors());
    }

    public String getGroupName() throws FastdfsClientException {
        Optional<String> any = storageChannelPool.keySet().stream().findAny();
        if (any.isPresent()) {
            return any.get();
        }
        throw new FastdfsClientException("choose groupName err");
    }


    private class FdfsHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent) evt;
                IdleState state = event.state();
                if (IdleState.ALL_IDLE == state) {
                    //todo close or write active test command
                    logger.info("channel tragger idle state,id: {}", ctx.channel().id().asShortText());
                }
            }
        }


        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            Command command = ctx.channel().attr(COMMAND_ATTR).get();
            command.decode(ctx.channel(), (ByteBuf) msg);
        }


        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            logger.info("channel active,id:{}", ctx.channel().id().asShortText());
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
            logger.info("channel inActive,id:{}", ctx.channel().id().asShortText());
        }


        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            logger.info("channel exception catched,id:{}", ctx.channel().id().asShortText(), cause);
        }
    }

}
