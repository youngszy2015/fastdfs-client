package org.y.fdfsclient;

import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.FixedChannelPool;
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

import java.util.List;

@Slf4j
public class FdfsClient {

    private String trackerAddr;

    private Bootstrap trackerBootStrap = new Bootstrap();

    private FixedChannelPool trackerChannelPool;

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
        trackerBootStrap
                .group(trackerGroup)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .channel(NioSocketChannel.class)
                .remoteAddress(ip, port)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new TrackerHandler());
                    }
                });
        //get group info
        List<GroupInfo> listGroup = getListGroup();
        for (GroupInfo groupInfo : listGroup) {


        }


    }


    public List<GroupInfo> getListGroup() {
        ListGroupCommand listGroupCommand = new ListGroupCommand();
        byte[] encode = listGroupCommand.encode();
        try {
            Channel trackerChannel = getTrackerChannel();
            Attribute<Object> command_attr = trackerChannel.attr(AttributeKey.valueOf("COMMAND_ATTR"));
            command_attr.set(listGroupCommand);
            ByteBuf out = trackerChannel.alloc().buffer().writeBytes(encode);
            trackerChannel.writeAndFlush(out);
            List<GroupInfo> groupInfos = (List<GroupInfo>) listGroupCommand.get();
            return groupInfos;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
