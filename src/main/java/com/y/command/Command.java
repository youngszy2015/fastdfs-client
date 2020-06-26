package com.y.command;

import io.netty.buffer.ByteBuf;


public interface Command<R> {



    byte[] header();


    R decode(ByteBuf in,int bodyLen);

    boolean canDecode(ByteBuf in);

    int getBodyLength();

}
