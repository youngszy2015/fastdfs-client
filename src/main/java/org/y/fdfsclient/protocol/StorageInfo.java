package org.y.fdfsclient.protocol;

import lombok.Data;

import java.util.Date;

@Data
public class StorageInfo {
    private byte status;
    private String id;
    private String ipAddr;
    private String srcIpAddr;
    private String domainName; //http domain name
    private String version;
    private long totalMB; //total disk storage in MB
    private long freeMB;  //free disk storage in MB
    private int uploadPriority;  //upload priority
    private Date joinTime; //storage join timestamp (create timestamp)
    private Date upTime;   //storage service started timestamp
    private int storePathCount;  //store base path count of each storage server
    private int subdirCountPerPath;
    private int storagePort;
    private int storageHttpPort; //storage http server port
    private int currentWritePath; //current write path index
    private int connectionAllocCount;
    private int connectionCurrentCount;
    private int connectionMaxCount;
    private long totalUploadCount;
    private long successUploadCount;
    private long totalAppendCount;
    private long successAppendCount;
    private long totalModifyCount;
    private long successModifyCount;
    private long totalTruncateCount;
    private long successTruncateCount;
    private long totalSetMetaCount;
    private long successSetMetaCount;
    private long totalDeleteCount;
    private long successDeleteCount;
    private long totalDownloadCount;
    private long successDownloadCount;
    private long totalGetMetaCount;
    private long successGetMetaCount;
    private long totalCreateLinkCount;
    private long successCreateLinkCount;
    private long totalDeleteLinkCount;
    private long successDeleteLinkCount;
    private long totalUploadBytes;
    private long successUploadBytes;
    private long totalAppendBytes;
    private long successAppendBytes;
    private long totalModifyBytes;
    private long successModifyBytes;
    private long totalDownloadloadBytes;
    private long successDownloadloadBytes;
    private long totalSyncInBytes;
    private long successSyncInBytes;
    private long totalSyncOutBytes;
    private long successSyncOutBytes;
    private long totalFileOpenCount;
    private long successFileOpenCount;
    private long totalFileReadCount;
    private long successFileReadCount;
    private long totalFileWriteCount;
    private long successFileWriteCount;
    private Date lastSourceUpdate;
    private Date lastSyncUpdate;
    private Date lastSyncedTimestamp;
    private Date lastHeartBeatTime;
    private boolean ifTrunkServer;
}
