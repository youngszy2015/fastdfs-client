package com.y;

import com.y.command.Command;
import com.y.command.ListStoreServerInfoCmd;
import com.y.handler.CommandHandler;
import com.y.protocol.ResponseFuture;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.Attribute;
import io.netty.util.concurrent.Future;

import java.net.InetSocketAddress;

public class Client {
    private String trackerAddr;
    private Bootstrap bootstrap = new Bootstrap();
    FixedChannelPool trackerChannelPool;
    private Bootstrap storeBootstrap = new Bootstrap();
//    FixedChannelPool storePool = new FixedChannelPool(storeBootstrap, new PoolHandler(), 10);

    public Client(String trackerAddr) {
        this.trackerAddr = trackerAddr;
    }

    public void init() throws Exception {
        synchronized (Client.class) {
            NioEventLoopGroup workGroup = new NioEventLoopGroup();
            bootstrap.group(workGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            bootstrap.remoteAddress(new InetSocketAddress(trackerAddr.split(":")[0], Integer.parseInt(trackerAddr.split(":")[1])));
            trackerChannelPool = new FixedChannelPool(bootstrap, new PoolHandler(), 10);
            ListStoreServerInfoCmd<ListStoreServerInfoCmd> cmd = new ListStoreServerInfoCmd<>();
            ResponseFuture responseFuture = listServer(cmd);
            try {
                Object o = responseFuture.get();
                System.out.println(o);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            //todo init store pool
        }

    }


    public ResponseFuture listServer(Command cmd) throws Exception {
        ResponseFuture responseFuture;
        //pick Channel
        try {
            Future<Channel> channelFuture = trackerChannelPool.acquire().sync();
            if (channelFuture.isSuccess()) {
                Channel channel = channelFuture.get();

                responseFuture = new ResponseFuture(trackerChannelPool, channel, cmd);
                if (channel.isActive() && channel.isWritable()) {
                    ByteBuf buffer = channel.alloc().buffer();
                    buffer.writeBytes(new byte[]{0, 0, 0, 0, 0, 0, 0, 16, 92, 0, 103, 114, 111, 117, 112, 49, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
                    Attribute<ResponseFuture> responseFutureAttribute = channel.attr(CommandHandler.RESPONSE_FUTURE_ATTR);
                    responseFutureAttribute.set(responseFuture);
                    ChannelFuture writeFuture = channel.writeAndFlush(buffer).sync();
                    if (writeFuture.isSuccess()) {
                        responseFuture.setRequestOk(true);
                    } else {
                        writeFuture.get();
                        responseFuture.setCause(new RuntimeException("write err"));
                    }
                }
                return responseFuture;
            } else {
                throw new Exception("pick channel err");
            }
        } catch (Exception e) {
            throw new Exception("pick channel err");
        }
    }


    class PoolHandler implements ChannelPoolHandler {

        @Override
        public void channelReleased(Channel ch) throws Exception {

        }

        @Override
        public void channelAcquired(Channel ch) throws Exception {

        }

        @Override
        public void channelCreated(Channel ch) throws Exception {
            ch.pipeline().addLast(new CommandHandler());
        }
    }

}
