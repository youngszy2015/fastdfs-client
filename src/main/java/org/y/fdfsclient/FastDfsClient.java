package org.y.fdfsclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.y.fdfsclient.command.ListGroupCommand;
import org.y.fdfsclient.exception.FastdfsClientException;
import org.y.fdfsclient.protocol.GroupInfo;
import org.y.fdfsclient.util.Assert;

import java.util.List;

public class FastDfsClient {

    private static final Logger logger = LoggerFactory.getLogger(FastDfsClient.class);


    private String trackerAddr;

    public FastDfsClient(String trackerAddr) {
        Assert.notEmpty(trackerAddr, "tracker attr must not be empty");
        this.trackerAddr = trackerAddr;
    }


    public void init() throws FastdfsClientException {
        synchronized (FastDfsClient.class) {
            ConnectionManager connectionManager = new ConnectionManager(this.trackerAddr);
            FastDfsWriter fastDfsWriter = new FastDfsWriter(connectionManager);
            getGroupInfoList(fastDfsWriter);
        }

    }

    private List<GroupInfo> getGroupInfoList(FastDfsWriter fastDfsWriter) throws FastdfsClientException {
        ListGroupCommand listGroupCommand = new ListGroupCommand();
        List<GroupInfo> groupInfos = (List<GroupInfo>) fastDfsWriter.write(listGroupCommand, true, null);
        for (GroupInfo groupInfo : groupInfos) {
            logger.info("group info:{}", groupInfo);
        }
        return groupInfos;
    }


}
