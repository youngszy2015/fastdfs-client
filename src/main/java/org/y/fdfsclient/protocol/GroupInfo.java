package org.y.fdfsclient.protocol;


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

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public long getTotalMB() {
        return totalMB;
    }

    public void setTotalMB(long totalMB) {
        this.totalMB = totalMB;
    }

    public long getFreeMB() {
        return freeMB;
    }

    public void setFreeMB(long freeMB) {
        this.freeMB = freeMB;
    }

    public long getTrunkFreeMB() {
        return trunkFreeMB;
    }

    public void setTrunkFreeMB(long trunkFreeMB) {
        this.trunkFreeMB = trunkFreeMB;
    }

    public int getStorageCount() {
        return storageCount;
    }

    public void setStorageCount(int storageCount) {
        this.storageCount = storageCount;
    }

    public int getStoragePort() {
        return storagePort;
    }

    public void setStoragePort(int storagePort) {
        this.storagePort = storagePort;
    }

    public int getStorageHttpPort() {
        return storageHttpPort;
    }

    public void setStorageHttpPort(int storageHttpPort) {
        this.storageHttpPort = storageHttpPort;
    }

    public int getActiveCount() {
        return activeCount;
    }

    public void setActiveCount(int activeCount) {
        this.activeCount = activeCount;
    }

    public int getCurrentWriteServer() {
        return currentWriteServer;
    }

    public void setCurrentWriteServer(int currentWriteServer) {
        this.currentWriteServer = currentWriteServer;
    }

    public int getStorePathCount() {
        return storePathCount;
    }

    public void setStorePathCount(int storePathCount) {
        this.storePathCount = storePathCount;
    }

    public int getSubdirCountPerPath() {
        return subdirCountPerPath;
    }

    public void setSubdirCountPerPath(int subdirCountPerPath) {
        this.subdirCountPerPath = subdirCountPerPath;
    }

    public int getCurrentTrunkFileId() {
        return currentTrunkFileId;
    }

    public void setCurrentTrunkFileId(int currentTrunkFileId) {
        this.currentTrunkFileId = currentTrunkFileId;
    }

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