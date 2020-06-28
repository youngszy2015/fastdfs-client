package org.y.fdfsclient.command;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public interface Command {

    void decode(Channel channel, ByteBuf in);

    byte[] encode();
}
