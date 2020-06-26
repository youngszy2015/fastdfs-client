package com.y.handler;

import com.y.command.Command;
import com.y.protocol.ResponseFuture;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

public class CommandHandler extends ChannelDuplexHandler {

    public static AttributeKey<ResponseFuture> RESPONSE_FUTURE_ATTR = AttributeKey.valueOf("ft_response");

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Attribute<ResponseFuture> responseFutureAttribute = ctx.channel().attr(RESPONSE_FUTURE_ATTR);
        ResponseFuture responseFuture = responseFutureAttribute.get();
        Command command = responseFuture.getCommand();
        if (canDecoder((ByteBuf) msg, command)) {
            int bodyLength = command.getBodyLength();
            Object decodeRes = command.decode((ByteBuf) msg, bodyLength);
            responseFuture.setResult(decodeRes);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        Attribute<ResponseFuture> responseFutureAttribute = ctx.channel().attr(RESPONSE_FUTURE_ATTR);
        ResponseFuture responseFuture = responseFutureAttribute.get();
        if (null != responseFuture) {
            responseFuture.setCause(cause);
        }
    }

    private boolean canDecoder(ByteBuf msg, Command command) {
        return command.canDecode(msg);
    }
}
