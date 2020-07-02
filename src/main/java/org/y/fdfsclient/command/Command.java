package org.y.fdfsclient.command;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.y.fdfsclient.exception.FastdfsClientException;

import java.util.concurrent.TimeUnit;

public interface Command {

    void decode(Channel channel, ByteBuf in);

    byte[] encode();

    Object get() throws FastdfsClientException;

    Object get(long timeout, TimeUnit unit) throws FastdfsClientException;

    void completeOnError(Throwable exception);
}
