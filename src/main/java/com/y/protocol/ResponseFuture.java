package com.y.protocol;


import com.y.command.Command;
import io.netty.channel.Channel;
import io.netty.channel.pool.FixedChannelPool;

import java.util.concurrent.CountDownLatch;

public class ResponseFuture extends CountDownLatch {

    private FixedChannelPool pool;

    private Channel processChannel;

    private Command command;

    private Object result;

    private Throwable cause;

    private boolean requestOk;

    public ResponseFuture(FixedChannelPool pool, Channel processChannel, Command command) {
        super(1);
        this.pool = pool;
        this.processChannel = processChannel;
        this.command = command;
    }

    public Object get() throws Throwable {
        try {
            await();
            if (null == cause) {
                return result;
            } else {
                throw cause;
            }
        } catch (InterruptedException e) {
            throw cause;
        } finally {
            pool.release(processChannel);
        }
    }

    public void setResult(Object result) {
        this.result = result;
        this.countDown();
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
        this.countDown();
    }

    public boolean isRequestOk() {
        return requestOk;
    }

    public void setRequestOk(boolean requestOk) {
        this.requestOk = requestOk;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }
}
