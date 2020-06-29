package org.y.fdfsclient;

import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.util.concurrent.Future;
import org.y.fdfsclient.command.ListStorageCommand;
import org.y.fdfsclient.protocol.GroupInfo;
import org.y.fdfsclient.protocol.ProtoCommon;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.y.fdfsclient.command.Command;
import org.y.fdfsclient.command.ListGroupCommand;
import org.y.fdfsclient.protocol.StorageInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class FdfsClient {

    private String trackerAddr;

    private Bootstrap trackerBootStrap = new Bootstrap();

    private FixedChannelPool trackerChannelPool;

    Map<String, FixedChannelPool> storagePool = new ConcurrentHashMap<>();

    private NioEventLoopGroup trackerGroup = new NioEventLoopGroup(1);

    public FdfsClient(String trackerAddr) {
        this.trackerAddr = trackerAddr;
    }


    public void init() throws Exception {
        if (trackerAddr == null || trackerAddr.length() == 0) {
            throw new Exception("tracker addr not be null");
        }
        String[] split = trackerAddr.split(":");
        String ip = split[0];
        int port = Integer.parseInt(split[1]);
        initTrackerChannelPool(ip, port);
        //get group info
        List<GroupInfo> listGroup = getListGroup();
        for (GroupInfo groupInfo : listGroup) {
            String groupName = groupInfo.getGroupName();
            List<StorageInfo> storageInfos = getStorageForEachGroup(groupName);
            for (StorageInfo storageInfo : storageInfos) {
                System.out.println("ip: " + storageInfo.getIpAddr() + " port: " + storageInfo.getStoragePort());
            }


        }


    }

    private List<StorageInfo> getStorageForEachGroup(String groupName) throws Exception {
        List<StorageInfo> addr = new ArrayList<>(0);
        ListStorageCommand listStorageCommand = new ListStorageCommand(groupName);
        byte[] encode = listStorageCommand.encode();
        if (encode.length == 0) {
            throw new Exception("[ListStorageCommand] encoder err");
        }
        Channel trackerChannel = null;
        try {
            Future<Channel> sync = trackerChannelPool.acquire().sync();
            if (!sync.isSuccess()) {
                throw new Exception("[ListStorageCommand] get channel err:", sync.cause());
            }
            trackerChannel = sync.get();
            Attribute<Object> command_attr = trackerChannel.attr(AttributeKey.valueOf("COMMAND_ATTR"));
            command_attr.set(listStorageCommand);
            ProtoCommon.printBytes("list storage encoder ", encode);
//            byte[] fake = {0, 0, 0, 0, 0, 0, 0, 16, 92, 0, 103, 114, 111, 117, 112, 49, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            ByteBuf out = trackerChannel.alloc().buffer(encode.length).writeBytes(encode);
            trackerChannel.writeAndFlush(out);
            Object o = listStorageCommand.get();
            System.out.println(o);

        } finally {
            trackerChannelPool.release(trackerChannel);
        }
        return addr;

    }


    private List<GroupInfo> getListGroup() {
        ListGroupCommand listGroupCommand = new ListGroupCommand();
        byte[] encode = listGroupCommand.encode();
        Channel trackerChannel = null;
        try {
            Future<Channel> sync = trackerChannelPool.acquire().sync();
            if (!sync.isSuccess()) {
                return null;
            }
            trackerChannel = sync.get();
            Attribute<Object> command_attr = trackerChannel.attr(AttributeKey.valueOf("COMMAND_ATTR"));
            command_attr.set(listGroupCommand);
            ByteBuf out = trackerChannel.alloc().buffer().writeBytes(encode);
            trackerChannel.writeAndFlush(out);
            List<GroupInfo> groupInfos = (List<GroupInfo>) listGroupCommand.get();
            return groupInfos;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != trackerChannel) {
                trackerChannelPool.release(trackerChannel);
            }
        }
        return null;
    }


    private void initTrackerChannelPool(String ip, int port) {
        trackerBootStrap
                .group(trackerGroup)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .channel(NioSocketChannel.class)
                .remoteAddress(ip, port);


        trackerChannelPool = new FixedChannelPool(trackerBootStrap, new ChannelPoolHandler() {
            @Override
            public void channelReleased(Channel ch) throws Exception {
                log.info("Released channel:{}", ch.id().asShortText());
            }

            @Override
            public void channelAcquired(Channel ch) throws Exception {
                log.info("Acquired channel:{}", ch.id().asShortText());
            }

            @Override
            public void channelCreated(Channel ch) throws Exception {
                log.info("Created channel:{}", ch.id().asShortText());
                ch.pipeline().addLast(new TrackerHandler());
            }
        }, Runtime.getRuntime().availableProcessors() * 2);
    }


    private Channel getTrackerChannel() throws Exception {
        ChannelFuture connect = trackerBootStrap.connect();
        ChannelFuture sync = connect.sync();
        if (sync.isSuccess()) {
            return sync.channel();
        } else {
            throw new Exception("get tracker channel err:", sync.cause());
        }
    }


    static class TrackerHandler extends ChannelInboundHandlerAdapter {

        AttributeKey<Command> COMMAND_ATTR = AttributeKey.valueOf("COMMAND_ATTR");

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            Command command = ctx.channel().attr(COMMAND_ATTR).get();
            log.info("read:{} ", command);
            command.decode(ctx.channel(), (ByteBuf) msg);
        }
    }

}
