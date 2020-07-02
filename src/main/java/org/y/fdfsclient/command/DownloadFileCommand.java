package org.y.fdfsclient.command;

import io.netty.buffer.ByteBuf;
import org.y.fdfsclient.protocol.ProtoCommon;

import java.nio.charset.StandardCharsets;

/**
 * @author szy47143
 * @date 2020/7/1 9:30
 */
public class DownloadFileCommand extends AbstractCommand {

    private String groupName;

    private String path;

    public DownloadFileCommand(String groupName, String path) {
        super(ProtoCommon.STORAGE_PROTO_CMD_RESP, -1);
        this.groupName = groupName;
        this.path = path;
    }

    @Override
    protected byte[] doEncode() {
        byte[] bsOffset = long2buff(0);
        byte[] bsDownBytes = long2buff(0);
        byte[] groupBytes = new byte[ProtoCommon.FDFS_GROUP_NAME_MAX_LEN];
        byte[] bs = groupName.getBytes(StandardCharsets.UTF_8);
        byte[] filenameBytes = path.getBytes(StandardCharsets.UTF_8);
        int groupLen;
        if (bs.length <= groupBytes.length) {
            groupLen = bs.length;
        } else {
            groupLen = groupBytes.length;
        }
        System.arraycopy(bs, 0, groupBytes, 0, groupLen);
        byte[] header = packHeader(ProtoCommon.STORAGE_PROTO_CMD_DOWNLOAD_FILE,
                bsOffset.length + bsDownBytes.length + groupBytes.length + filenameBytes.length, (byte) 0);
        byte[] wholePkg = new byte[header.length + bsOffset.length + bsDownBytes.length + groupBytes.length + filenameBytes.length];
        System.arraycopy(header, 0, wholePkg, 0, header.length);
        System.arraycopy(bsOffset, 0, wholePkg, header.length, bsOffset.length);
        System.arraycopy(bsDownBytes, 0, wholePkg, header.length + bsOffset.length, bsDownBytes.length);
        System.arraycopy(groupBytes, 0, wholePkg, header.length + bsOffset.length + bsDownBytes.length, groupBytes.length);
        System.arraycopy(filenameBytes, 0, wholePkg, header.length + bsOffset.length + bsDownBytes.length + groupBytes.length, filenameBytes.length);
        return wholePkg;
    }

    @Override
    protected Object doDecode(ByteBuf in, long decodeBodyLength) {
        byte[] bytes = new byte[(int) decodeBodyLength];
        in.readBytes(bytes);
        return bytes;
    }
}
