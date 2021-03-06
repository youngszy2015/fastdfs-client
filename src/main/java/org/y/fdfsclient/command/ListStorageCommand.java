package org.y.fdfsclient.command;

import io.netty.buffer.ByteBuf;
import org.y.fdfsclient.protocol.ProtoCommon;
import org.y.fdfsclient.protocol.StorageInfo;
import org.y.fdfsclient.util.Constant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListStorageCommand extends AbstractCommand {
    private String groupName;

    public ListStorageCommand(String groupName) {
        super(ProtoCommon.TRACKER_PROTO_CMD_RESP, -1);
        this.groupName = groupName;
    }

    @Override
    protected byte[] doEncode() {
        byte[] bs = groupName.getBytes(Constant.DEFAULT_CHARSET);
        byte[] bGroupName = new byte[ProtoCommon.FDFS_GROUP_NAME_MAX_LEN];
        int len;
        if (bs.length <= ProtoCommon.FDFS_GROUP_NAME_MAX_LEN) {
            len = bs.length;
        } else {
            len = ProtoCommon.FDFS_GROUP_NAME_MAX_LEN;
        }
        Arrays.fill(bGroupName, (byte) 0);
        System.arraycopy(bs, 0, bGroupName, 0, len);
        byte[] header = packHeader(ProtoCommon.TRACKER_PROTO_CMD_SERVER_LIST_STORAGE, ProtoCommon.FDFS_GROUP_NAME_MAX_LEN, (byte) 0);
        byte[] wholePkg = new byte[header.length + bGroupName.length];
        System.arraycopy(header, 0, wholePkg, 0, header.length);
        System.arraycopy(bGroupName, 0, wholePkg, header.length, bGroupName.length);
        return wholePkg;
    }

    @Override
    protected Object doDecode(ByteBuf in, long decodeBodyLength) {
        byte[] body = new byte[(int) decodeBodyLength];
        in.readBytes(body);
        int i = body.length / 612;
        int offset = 0;
        List<StorageInfo> storageInfos = new ArrayList<>();
        for (int j = 0; j < i; j++) {
            StorageInfo storageInfo = new StorageInfo();
            byte status = body[offset];
            storageInfo.setStatus(status);
            String ip = new String(body, offset + 17, 16);
            storageInfo.setIpAddr(ip);
            int port = (int) buff2long(body, offset + 247);
            storageInfo.setStoragePort(port);
            offset += 612;
            storageInfos.add(storageInfo);
        }
        return storageInfos;
    }
}
