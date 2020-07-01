package org.y.fdfsclient.command;

import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.y.fdfsclient.protocol.ProtoCommon;
import org.y.fdfsclient.protocol.UploadFileResponse;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author szy47143
 * @date 2020/6/30 9:25
 */
@Slf4j
public class UploadFileCommand extends AbstractCommand {

    private String groupName;

    private byte[] fileBytes;

    private String extName;

    private int expireDays;


    public UploadFileCommand(String groupName, byte[] fileBytes, String extName, int expireDays) {
        super(ProtoCommon.STORAGE_PROTO_CMD_RESP, -1);
        this.groupName = groupName;
        this.fileBytes = fileBytes;
        this.extName = extName;
        this.expireDays = expireDays;
    }

    @Override
    protected byte[] doEncode() {
        int fileSize = fileBytes.length;
        byte[] extNameBs = new byte[ProtoCommon.FDFS_FILE_EXT_NAME_MAX_LEN];
        //process file ext name
        if (extName != null) {
            byte[] bytes = extName.getBytes(StandardCharsets.UTF_8);
            System.arraycopy(bytes, 0, extNameBs, 0, bytes.length);
        }
        byte[] sizeBytes = new byte[1 + 1 * ProtoCommon.FDFS_PROTO_PKG_LEN_SIZE];
        int bodyLen = sizeBytes.length + ProtoCommon.FDFS_FILE_EXT_NAME_MAX_LEN + fileSize;
        sizeBytes[0] = (byte) 0;
        int offset = 1;
        byte[] hexLenBytes = long2buff(fileSize);
        System.arraycopy(hexLenBytes, 0, sizeBytes, offset, hexLenBytes.length);

        byte[] header = packHeader(ProtoCommon.STORAGE_PROTO_CMD_UPLOAD_FILE, bodyLen, (byte) 0);
        byte[] wholePkg = new byte[(int) (header.length + bodyLen - fileSize)];
        System.arraycopy(header, 0, wholePkg, 0, header.length);
        System.arraycopy(sizeBytes, 0, wholePkg, header.length, sizeBytes.length);
        offset = header.length + sizeBytes.length;
        System.arraycopy(extNameBs, 0, wholePkg, offset, extNameBs.length);
        offset += extNameBs.length;
        ProtoCommon.printBytes("[upload file cmd]", wholePkg);
        return wholePkg;
    }

    @Override
    protected Object doDecode(ByteBuf in, long decodeBodyLength) {
        int bodyLen = (int) decodeBodyLength;
        byte[] body = new byte[bodyLen];
        in.readBytes(body);
        String newGroupName = new String(body, 0, ProtoCommon.FDFS_GROUP_NAME_MAX_LEN).trim();
        String remoteFileName = new String(body, ProtoCommon.FDFS_GROUP_NAME_MAX_LEN, bodyLen - ProtoCommon.FDFS_GROUP_NAME_MAX_LEN);
        log.info("newGroupName：" + newGroupName);
        log.info("remoteFileName：" + remoteFileName);
        UploadFileResponse uploadFileResponse = new UploadFileResponse();
        uploadFileResponse.setGroupName(newGroupName);
        uploadFileResponse.setPath(remoteFileName);
        return uploadFileResponse;
    }
}
