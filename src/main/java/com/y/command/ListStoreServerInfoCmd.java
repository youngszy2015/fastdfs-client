package com.y.command;

import com.y.protocol.ListStoreServerInfoResponse;
import com.y.protocol.ProtoCommon;
import io.netty.buffer.ByteBuf;

public class ListStoreServerInfoCmd<ListStoreServerInfoResponse> extends AbstractCommand<ListStoreServerInfoResponse> {


    public ListStoreServerInfoCmd() {
        super(ProtoCommon.TRACKER_PROTO_CMD_SERVER_LIST_STORAGE, ProtoCommon.TRACKER_PROTO_CMD_RESP,
                ProtoCommon.FDFS_GROUP_NAME_MAX_LEN, (byte) 0);
    }

    @Override
    protected ListStoreServerInfoResponse doDecode(ByteBuf in, int bodyLength) {
        byte[] bytes = new byte[bodyLength];
        for (byte aByte : bytes) {
            System.out.print(aByte + " ");
        }
        return null;
    }
}
