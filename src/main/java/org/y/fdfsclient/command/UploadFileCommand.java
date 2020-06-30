package org.y.fdfsclient.command;

import io.netty.buffer.ByteBuf;
import org.y.fdfsclient.protocol.ProtoCommon;

/**
 * @author szy47143
 * @date 2020/6/30 9:25
 */
public class UploadFileCommand extends AbstractCommand {


    public UploadFileCommand(int expectCmd, int expectBodyLength) {
        super(ProtoCommon.STORAGE_PROTO_CMD_UPLOAD_FILE, expectBodyLength);
    }

    @Override
    protected byte[] doEncode() {
        return new byte[0];
    }

    @Override
    protected Object doDecode(ByteBuf in, long decodeBodyLength) {
        return null;
    }
}
