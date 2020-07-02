package org.y.fdfsclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.y.fdfsclient.command.DownloadFileCommand;
import org.y.fdfsclient.command.ListGroupCommand;
import org.y.fdfsclient.command.ListStorageCommand;
import org.y.fdfsclient.command.UploadFileCommand;
import org.y.fdfsclient.exception.FastdfsClientException;
import org.y.fdfsclient.protocol.GroupInfo;
import org.y.fdfsclient.protocol.ProtoCommon;
import org.y.fdfsclient.protocol.StorageInfo;
import org.y.fdfsclient.protocol.UploadFileResponse;

import java.net.InetSocketAddress;
import java.util.List;

public class FastDfsClient {

    private static final Logger logger = LoggerFactory.getLogger(FastDfsClient.class);

    private FastDfsWriter fastDfsWriter;
    private ConnectionManager connectionManager;

    public void create() throws FastdfsClientException {
        synchronized (FastDfsClient.class) {
            FdfsClientConfig config = new FdfsClientConfig();
            config.parse();
            connectionManager = new ConnectionManager(config);
            fastDfsWriter = new FastDfsWriter(connectionManager);
            List<GroupInfo> groupInfoList = getGroupInfoList(fastDfsWriter);
            logger.info("get group info list:{}", groupInfoList);
            for (GroupInfo groupInfo : groupInfoList) {
                String groupName = groupInfo.getGroupName();
                List<StorageInfo> storageInfo = getStorageInfo(fastDfsWriter, groupName);
                for (StorageInfo info : storageInfo) {
                    if (info.getStatus() == ProtoCommon.FDFS_STORAGE_STATUS_ACTIVE) {
                        //choose active storage
                        connectionManager.initStorageChannelPool(groupName, new InetSocketAddress(info.getIpAddr(), info.getStoragePort()));
                    }
                }
            }
            logger.info("Fastdfs client init success");
        }
    }

    /**
     * upload file
     *
     * @param groupName   groupName
     * @param fileBytes   file bytes
     * @param extFileName file extension name :txt，xls ..
     * @return
     * @throws FastdfsClientException
     */
    public UploadFileResponse uploadFile(String groupName, byte[] fileBytes, String extFileName) throws FastdfsClientException {
        UploadFileCommand uploadFileCommand = new UploadFileCommand(groupName, fileBytes, extFileName, 0);
        UploadFileResponse response = (UploadFileResponse) fastDfsWriter.write(uploadFileCommand, false, groupName);
        return response;
    }

    public byte[] downloadFile(String groupName, String path) throws FastdfsClientException {
        DownloadFileCommand downloadFileCommand = new DownloadFileCommand(groupName, path);
        byte[] fileBytes = (byte[]) fastDfsWriter.write(downloadFileCommand, false, groupName);
        return fileBytes;
    }


    public void close() {
        synchronized (FastDfsClient.class) {
            connectionManager.close();
        }
    }


    private List<StorageInfo> getStorageInfo(FastDfsWriter fastDfsWriter, String groupName) throws FastdfsClientException {
        ListStorageCommand listStorageCommand = new ListStorageCommand(groupName);
        List<StorageInfo> storageInfoList = (List<StorageInfo>) fastDfsWriter.write(listStorageCommand, true, groupName);
        return storageInfoList;
    }

    private List<GroupInfo> getGroupInfoList(FastDfsWriter fastDfsWriter) throws FastdfsClientException {
        ListGroupCommand listGroupCommand = new ListGroupCommand();
        return (List<GroupInfo>) fastDfsWriter.write(listGroupCommand, true, null);
    }


}
