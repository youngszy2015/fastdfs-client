package org.y.fdfsclient;

import com.sun.corba.se.impl.activation.CommandHandler;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.util.NetUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.commons.io.FileUtils;
import org.y.fdfsclient.command.ListStorageCommand;
import org.y.fdfsclient.command.UploadFileCommand;
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
import org.y.fdfsclient.protocol.UploadFileResponse;

import javax.management.relation.RoleUnresolved;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class FdfsClient {

    private String trackerAddr;

    private Bootstrap trackerBootStrap = new Bootstrap();

    private FixedChannelPool trackerChannelPool;
    //todo
    Map<String, FixedChannelPool> storagePool = new ConcurrentHashMap<>();
    Map<String/**groupName*/, List<StorageInfo>/**ip addr*/> storageIpTable = new ConcurrentHashMap<>();

    private NioEventLoopGroup trackerGroup = new NioEventLoopGroup(1);

    public FdfsClient(String trackerAddr) {
        this.trackerAddr = trackerAddr;
    }


    public void init() throws Exception {
        synchronized (FdfsClient.class) {
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
                    log.info("[storage] ip: " + storageInfo.getIpAddr() + " port: " + storageInfo.getStoragePort() + ((storageInfo.getStatus() == (byte) 7) ? " ACTIVE" : " UNKNOWN"));
                }

                List<StorageInfo> storageInfoList = storageInfos.stream().filter(n -> n.getStatus() == (byte) 7).collect(Collectors.toList());
                if (storageInfoList.size() > 0) {
                    storageIpTable.put(groupName, storageInfoList);
                }


            }
            registerShutdownHook();
        }
    }


    public void uploadFile(byte[] fileBytes, String fileName) throws Exception {
        String groupName = null;
        UploadFileCommand fileCommand = new UploadFileCommand(groupName, fileBytes, "txt", 1);
        String addr = pickStorageAddr();
        if (null == addr) {
            throw new Exception("pick channel err");
        }
        Channel channel = null;
        try {
            channel = getStorageChannel(addr);
            byte[] encode = fileCommand.encode();
            ByteBuf buffer = channel.alloc().buffer(encode.length).writeBytes(encode);
            Attribute<Object> command_attr = channel.attr(AttributeKey.valueOf("COMMAND_ATTR"));
            command_attr.set(fileCommand);
            if (channel.isActive() && channel.isWritable()) {
                buffer.writeBytes(fileBytes);
                ChannelFuture channelFuture = channel.writeAndFlush(buffer);
                channelFuture.addListener(future -> {
                    if (future.isSuccess()) {
                        System.out.println("write future success");
                    } else {
                        future.cause().printStackTrace();
                    }
                });
                UploadFileResponse o = (UploadFileResponse) fileCommand.get();
                System.out.println(o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != channel) {
                channel.close();
            }
        }


    }

    private Channel getStorageChannel(String addr) throws Exception {
        Bootstrap b = new Bootstrap();
        String[] split = addr.split(":");
        NioEventLoopGroup g = new NioEventLoopGroup(1);
        b.group(g)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .channel(NioSocketChannel.class)
                .remoteAddress(split[0], Integer.parseInt(split[1]));

        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new TrackerHandler());
            }
        });

        ChannelFuture sync = b.connect(split[0], Integer.parseInt(split[1])).sync();
        if (sync.isSuccess()) {
            return sync.channel();
        }
        return null;
    }

    private String pickStorageAddr() {
        for (String s : storageIpTable.keySet()) {
            List<StorageInfo> storageInfoList = storageIpTable.get(s);
            if (storageInfoList.size() > 0) {
                StorageInfo storageInfo = storageInfoList.get(0);
                return storageInfo.getIpAddr() + ":" + storageInfo.getStoragePort();
            }

        }
        return null;
    }


    private List<StorageInfo> getStorageForEachGroup(String groupName) throws Exception {
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
            ByteBuf out = trackerChannel.alloc().buffer(encode.length).writeBytes(encode);
            trackerChannel.writeAndFlush(out);
            Object o = listStorageCommand.get();
            return (List<StorageInfo>) o;
        } finally {
            trackerChannelPool.release(trackerChannel);
        }
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


        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
        }


    }


    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                if (null != trackerChannelPool) {
                    trackerChannelPool.close();
                }
            }
        }));
    }

}
