package org.y.fdfsclient.command;

import org.y.fdfsclient.protocol.ProtoCommon;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

//获取group信息
@Slf4j
public class ListGroupCommand extends AbstractCommand {


    public ListGroupCommand() {
        super(ProtoCommon.TRACKER_PROTO_CMD_RESP, -1);
    }

    @Override
    protected byte[] doEncode() {
        return packHeader(ProtoCommon.TRACKER_PROTO_CMD_SERVER_LIST_GROUP, 0, (byte) 0);
    }

    @Override
    protected byte doDecode(ByteBuf in, long decodeBodyLength) {
        byte[] body = new byte[(int) decodeBodyLength];
        in.readBytes(body);
        ProtoCommon.printBytes("list group body", body);
        log.info("0-17,groupName:[{}]", new String(body, 0, 17));
        log.info("18-26,totalMB:[{}]", new String(body, 18, 26));
        log.info("26-34,freeMB:[{}]", new String(body, 18, 34));
        return 0;
    }


    @Override
    public String toString() {
        return "ListGroupCommand:" + this.hashCode();
    }
}
