package org.y.fdfsclient;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.y.fdfsclient.command.Command;
import org.y.fdfsclient.exception.FastdfsClientException;

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
                channel = connectionManager.getStorageChannel(groupName);
            }
            acquired = true;
            if (channel.isActive() && channel.isWritable()) {
                ByteBuf byteBuf = channel.alloc().buffer(encode.length).writeBytes(encode);
                channel.attr(connectionManager.getCommandAttr()).set(command);
                channel.writeAndFlush(byteBuf).addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        logger.debug("tracker command write success");
                    } else {
                        logger.error("tracker command write failed:", future.cause());
                    }
                });
                return command.get();
            }
        } finally {
            lock.unlock();
            if (acquired) {
                connectionManager.releaseChannel(channel, tracker, groupName);
            }
        }
        throw new FastdfsClientException("write command err");
    }

}
