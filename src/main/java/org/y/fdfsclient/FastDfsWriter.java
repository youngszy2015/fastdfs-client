package org.y.fdfsclient;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.y.fdfsclient.command.Command;
import org.y.fdfsclient.exception.FastDfsClientWriteException;
import org.y.fdfsclient.exception.FastdfsClientException;
import org.y.fdfsclient.util.StringUtils;

import java.util.concurrent.locks.ReentrantLock;

public class FastDfsWriter {

    private static final Logger logger = LoggerFactory.getLogger(FastDfsWriter.class);

    private ReentrantLock lock = new ReentrantLock();


    private ConnectionManager connectionManager;

    public FastDfsWriter(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public Object write(Command command, boolean tracker, String groupName) throws FastdfsClientException {
        Channel channel = null;
        boolean acquired = false;
        try {
            lock.lock();
            byte[] encode = command.encode();
            if (tracker) {
                //write tracker command
                channel = connectionManager.getTrackerChannel();
            } else {
                if(StringUtils.isEmpty(groupName)){
                    groupName = connectionManager.getGroupName();
                }
                channel = connectionManager.getStorageChannel(groupName);
            }
            acquired = true;
            ByteBuf out = channel.alloc().buffer(encode.length).writeBytes(encode);
            if (channel.isActive() && channel.isWritable()) {
                writeAndFlush(out, channel, command);
                return command.get();
            } else {
                completeWriteError(command, channel);
            }
        } finally {
            lock.unlock();
            if (acquired) {
                connectionManager.releaseChannel(channel, tracker, groupName);
            }
        }
        throw new FastdfsClientException("write command err");
    }

    private void completeWriteError(Command command, Channel channel) {
        command.completeOnError(new FastDfsClientWriteException("channel is not writeable"));
        channel.close().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                String id = future.channel().id().asShortText();
                if (future.isSuccess()) {
                    logger.warn("channel closed success for not writeable: {}", id);
                } else {
                    logger.warn("channel closed failed for not writeable: {},err:", id, future.cause());
                }
            }
        });
    }

    private void writeAndFlush(ByteBuf out, Channel channel, Command command) {
        channel.attr(connectionManager.getCommandAttr()).set(command);
        channel.writeAndFlush(out).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                logger.debug("tracker command write success");
            } else {
                logger.error("tracker command write failed:", future.cause());
            }
        });
    }

}
