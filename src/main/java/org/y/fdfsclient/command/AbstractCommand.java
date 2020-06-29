package org.y.fdfsclient.command;

import org.y.fdfsclient.protocol.ProtoCommon;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import static org.y.fdfsclient.protocol.ProtoCommon.*;

@Slf4j
public abstract class AbstractCommand extends CountDownLatch implements Command {

    private int expectCmd;

    private int expectBodyLength;

    //是否已经解析或头部
    private boolean hasDecodeHeader = false;

    private long decodeBodyLength = 0L;

    ByteBuf cacheBuf;


    public AbstractCommand(int expectCmd, int expectBodyLength) {
        super(1);
        this.expectCmd = expectCmd;
        this.expectBodyLength = expectBodyLength;
    }

    @Override
    public void decode(Channel channel, ByteBuf in) {
        if (!hasDecodeHeader) {
            if (in.readableBytes() < 10) {
                log.warn("wait enough header bytes,channel:[{}]", channel.id().asShortText());
                return;
            }
            byte[] header = new byte[FDFS_PROTO_PKG_LEN_SIZE + 2];
            in.readBytes(header);
            if (header[PROTO_HEADER_CMD_INDEX] != expectCmd) {
                log.warn("receive cmd is not expectCmd,close channel ,channel:[{}]", channel.id().asShortText());
                channel.close();
                return;
            }
            ProtoCommon.printBytes("header", header);
            long bodyLength = buff2long(header, 0);
            if (bodyLength < 0) {
                log.warn("receive empty body length,close channel ,channel:[{}]", channel.id().asShortText());
                channel.close();
                return;
            }
            if (expectBodyLength >= 0 && bodyLength != expectBodyLength) {
                log.warn("receive body length is not expectBodyLength,bodyLen:[{}],expectBodyLen:[{}],close channel ,channel:[{}]",
                        bodyLength,
                        expectBodyLength,
                        channel.id().asShortText());
                channel.close();
                return;
            }
            hasDecodeHeader = true;
            decodeBodyLength = bodyLength;
            cacheBuf = channel.alloc().buffer();
            cacheBuf.writeBytes(in);
            System.out.println("cacheBuf write index: " + cacheBuf.writerIndex());
            if (cacheBuf.readableBytes() < bodyLength) {
                return;
            }
        } else {
            cacheBuf.writeBytes(in);
            System.out.println("cacheBuf write index: " + cacheBuf.writerIndex());
            if (cacheBuf.readableBytes() < decodeBodyLength) {
                return;
            }
        }
        Throwable ex = null;
        try {
            Object result = doDecode(cacheBuf, decodeBodyLength);
            complete(result, null);
            cacheBuf.release();
        } catch (Exception e) {
            complete(result, e);
            cacheBuf.release();
        }
    }


    @Override
    public byte[] encode() {
        return doEncode();
    }

    private Object result;
    private Throwable cause;

    private void complete(Object result, Throwable cause) {
        if (cause == null) {
            this.result = result;
        } else {
            this.cause = cause;
        }
        countDown();
    }

    public Object get() throws Exception {
        try {
            await();
            if (cause != null) {
                throw new Exception(cause.getMessage());
            }
            return result;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }


    protected abstract byte[] doEncode();

    protected abstract Object doDecode(ByteBuf in, long decodeBodyLength);


    public byte[] packHeader(byte cmd, long pkg_len, byte errno) {
        byte[] header;
        byte[] hex_len;

        header = new byte[FDFS_PROTO_PKG_LEN_SIZE + 2];
        Arrays.fill(header, (byte) 0);

        hex_len = long2buff(pkg_len);
        System.arraycopy(hex_len, 0, header, 0, hex_len.length);
        header[PROTO_HEADER_CMD_INDEX] = cmd;
        header[PROTO_HEADER_STATUS_INDEX] = errno;
        return header;
    }


    public byte[] long2buff(long n) {
        byte[] bs;

        bs = new byte[8];
        bs[0] = (byte) ((n >> 56) & 0xFF);
        bs[1] = (byte) ((n >> 48) & 0xFF);
        bs[2] = (byte) ((n >> 40) & 0xFF);
        bs[3] = (byte) ((n >> 32) & 0xFF);
        bs[4] = (byte) ((n >> 24) & 0xFF);
        bs[5] = (byte) ((n >> 16) & 0xFF);
        bs[6] = (byte) ((n >> 8) & 0xFF);
        bs[7] = (byte) (n & 0xFF);

        return bs;
    }


    public long buff2long(byte[] bs, int offset) {
        return (((long) (bs[offset] >= 0 ? bs[offset] : 256 + bs[offset])) << 56) |
                (((long) (bs[offset + 1] >= 0 ? bs[offset + 1] : 256 + bs[offset + 1])) << 48) |
                (((long) (bs[offset + 2] >= 0 ? bs[offset + 2] : 256 + bs[offset + 2])) << 40) |
                (((long) (bs[offset + 3] >= 0 ? bs[offset + 3] : 256 + bs[offset + 3])) << 32) |
                (((long) (bs[offset + 4] >= 0 ? bs[offset + 4] : 256 + bs[offset + 4])) << 24) |
                (((long) (bs[offset + 5] >= 0 ? bs[offset + 5] : 256 + bs[offset + 5])) << 16) |
                (((long) (bs[offset + 6] >= 0 ? bs[offset + 6] : 256 + bs[offset + 6])) << 8) |
                ((long) (bs[offset + 7] >= 0 ? bs[offset + 7] : 256 + bs[offset + 7]));
    }
}
