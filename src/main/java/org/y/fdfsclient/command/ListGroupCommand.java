package org.y.fdfsclient.command;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.y.fdfsclient.protocol.GroupInfo;
import org.y.fdfsclient.protocol.ProtoCommon;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

//获取group信息
public class ListGroupCommand extends AbstractCommand {
    private static final Logger log = LoggerFactory.getLogger(ListGroupCommand.class);
    static int filedTotalSize = 105;

    public ListGroupCommand() {
        super(ProtoCommon.TRACKER_PROTO_CMD_RESP, -1);
    }

    @Override
    protected byte[] doEncode() {
        return packHeader(ProtoCommon.TRACKER_PROTO_CMD_SERVER_LIST_GROUP, 0, (byte) 0);
    }

    @Override
    protected Object doDecode(ByteBuf in, long decodeBodyLength) {
        byte[] body = new byte[(int) decodeBodyLength];
        in.readBytes(body);
        int i = body.length / 105;
        int offset = 0;
        ProtoCommon.printBytes("list group body", body);
        List<GroupInfo> groupInfos = new ArrayList<>();
        for (int j = 0; j < i; j++) {
            GroupInfo groupInfo = new GroupInfo();
            groupInfo.setGroupName(new String(body, offset, offset + 17));
            groupInfo.setTotalMB(buff2long(body, offset + 17));
            groupInfo.setFreeMB(buff2long(body, offset + 25));
            groupInfo.setTrunkFreeMB(buff2long(body, offset + 33));
            groupInfo.setStorageCount((int) buff2long(body, offset + 41));
            groupInfo.setStoragePort((int) buff2long(body, offset + 49));
            groupInfo.setStorageHttpPort((int) buff2long(body, offset + 57));
            groupInfo.setActiveCount((int) buff2long(body, offset + 65));
            groupInfo.setCurrentWriteServer((int) buff2long(body, offset + 73));
            groupInfo.setStorePathCount((int) buff2long(body, offset + 81));
            groupInfo.setSubdirCountPerPath((int) buff2long(body, offset + 89));
            groupInfo.setCurrentTrunkFileId((int) buff2long(body, offset + 97));
            offset += 105;
            groupInfos.add(groupInfo);
        }
        return groupInfos;
    }


    @Override
    public String toString() {
        return "ListGroupCommand:" + this.hashCode();
    }


}
