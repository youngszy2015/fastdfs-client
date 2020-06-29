package org.y.fdfsclient.protocol;

import lombok.Data;

@Data
public class GroupInfo {
    private String groupName;  //name of this group
    private long totalMB;      //total disk storage in MB
    private long freeMB;       //free disk space in MB
    private long trunkFreeMB;  //trunk free space in MB
    private int storageCount;  //storage server count
    private int storagePort;   //storage server port
    private int storageHttpPort; //storage server HTTP port
    private int activeCount;     //active storage server count
    private int currentWriteServer; //current storage server index to upload file
    private int storePathCount;     //store base path count of each storage server
    private int subdirCountPerPath; //sub dir count per store path
    private int currentTrunkFileId; //current trunk file id

    @Override
    public String toString() {
        return "GroupInfo{" + '\n' +
                "groupName='" + groupName + '\'' + '\n' +
                ", totalMB=" + totalMB + '\n' +
                ", freeMB=" + freeMB + '\n' +
                ", trunkFreeMB=" + trunkFreeMB + '\n' +
                ", storageCount=" + storageCount + '\n' +
                ", storagePort=" + storagePort + '\n' +
                ", storageHttpPort=" + storageHttpPort + '\n' +
                ", activeCount=" + activeCount + '\n' +
                ", currentWriteServer=" + currentWriteServer + '\n' +
                ", storePathCount=" + storePathCount + '\n' +
                ", subdirCountPerPath=" + subdirCountPerPath + '\n' +
                ", currentTrunkFileId=" + currentTrunkFileId + '\n' +
                '}';
    }
}