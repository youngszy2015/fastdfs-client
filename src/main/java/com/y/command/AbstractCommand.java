package com.y.command;


import com.y.protocol.ProtoCommon;
import io.netty.buffer.ByteBuf;

import java.util.Arrays;


public abstract class AbstractCommand<R> implements Command<R> {

    static int headerLength = ProtoCommon.FDFS_PROTO_PKG_LEN_SIZE + 2;

    private byte cmd;

    private byte expectCmd;

    private long pkgLen;

    private byte errNo;

    private int bodyLength;

    private R res;

    public AbstractCommand(byte cmd,byte expectCmd,long pkgLen,byte errNo) {
        this.cmd = cmd;
        this.expectCmd = expectCmd;
        this.pkgLen = pkgLen;
        this.errNo = errNo;
    }

    @Override
    public int getBodyLength() {
        return bodyLength;
    }

    @Override
    public byte[] header() {
        byte[] header;
        byte[] hex_len;
        header = new byte[ProtoCommon.FDFS_PROTO_PKG_LEN_SIZE + 2];
        Arrays.fill(header, (byte) 0);
        hex_len = long2buff(pkgLen);
        System.arraycopy(hex_len, 0, header, 0, hex_len.length);
        header[ProtoCommon.PROTO_HEADER_CMD_INDEX] = cmd;
        header[ProtoCommon.PROTO_HEADER_STATUS_INDEX] = errNo;
        return header;
    }

    @Override
    public R decode(ByteBuf in,int bodyLen) {
        return doDecode(in,bodyLen);
    }



    @Override
    public boolean canDecode(ByteBuf in) {
        byte[] header = new byte[headerLength];
        in.readBytes(header);
        if (header.length != headerLength) {
            //discard bytes
            return false;
        }
        if (ProtoCommon.PROTO_HEADER_CMD_INDEX != expectCmd) {
            //discard bytes
            return false;
        }
        if (header[ProtoCommon.PROTO_HEADER_STATUS_INDEX] != 0) {
            //discard bytes
            return false;
        }

        long pkgLen = buff2long(header, 0);

        if (pkgLen < 0) {
            //discard bytes
            return false;
        }

        if (in.readableBytes() != pkgLen) {
            return false;
        }

        int i = in.readableBytes();
        if (i != pkgLen) {
            return false;
        }
        this.setBodyLength((int) pkgLen);
        return true;
    }

    protected abstract R doDecode(ByteBuf in,int bodyLen);


    public void setBodyLength(int bodyLength) {
        this.bodyLength = bodyLength;
    }

//    private void decode(ByteBuf in, byte expectCmd, long expectBodyLen) {
//
//        byte[] bytes = new byte[(int) pkgLen];
//        in.readBytes(bytes);
//
//    }

    public static byte[] long2buff(long n) {
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

    public static long buff2long(byte[] bs, int offset) {
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
