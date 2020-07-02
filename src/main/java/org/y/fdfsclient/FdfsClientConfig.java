package org.y.fdfsclient;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.y.fdfsclient.util.Assert;
import org.y.fdfsclient.util.StringUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author szy47143
 * @date 2020/7/2 9:25
 */
public class FdfsClientConfig implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(FdfsClientConfig.class);

    private static final String CONFIG_FILE_NAME = "fdfs_client.conf";

    //tracker 地址 ip/domain:port
    private String trackerAddr;

    private int connectionTimeout = 3000;

    private int soTimeout = 30000;

    //tracker channel pool max channels
    private int trackerPoolMaxCount = 0;

    //storage channel pool max channels
    private int storagePoolMaxCount = 0;

    //default 300 seconds
    private int readWriteIdleTime = 300;


    public void parse() {
        try (InputStream resourceAsStream = FdfsClientConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME);
             InputStreamReader inputStreamReader = new InputStreamReader(resourceAsStream);
             BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line;
            Map<String, String> configMap = new HashMap<>();
            while ((line = reader.readLine()) != null) {
                String[] arr = line.split("=");
                if (arr.length == 2) {
                    configMap.put(arr[0].trim(), arr[1].trim());
                    log.info("fastdfs config,key:{},value:{}", arr[0], arr[1]);
                }
            }
            if (!configMap.isEmpty()) {
                processConfigField(configMap);
            } else {
                String trackerAddr = System.getProperty("trackerAddr");
                Assert.notEmpty(trackerAddr, "trackerAddr need config");
                this.trackerAddr = trackerAddr;
            }
        } catch (IOException e) {
            log.warn("read fastdfs client config file err:", e);
        }
    }

    private void processConfigField(Map<String, String> configMap) {
        for (String key : configMap.keySet()) {
            processKeyValue(key, configMap.get(key));
        }
    }

    private void processKeyValue(String key, String value) {
        switch (key) {
            case "trackerAddr":
                //system property
                String trackerAddr = System.getProperty("trackerAddr");
                if (!StringUtils.isEmpty(trackerAddr)) {
                    this.trackerAddr = trackerAddr;
                    break;
                }
                this.trackerAddr = value;
                break;
            case "connectionTimeout":
                this.connectionTimeout = Integer.parseInt(value);
                break;
            case "trackerPoolMaxCount":
                this.trackerPoolMaxCount = Integer.parseInt(value);
                break;
            case "storagePoolMaxCount":
                this.storagePoolMaxCount = Integer.parseInt(value);
                break;
            case "soTimeout":
                this.soTimeout = Integer.parseInt(value);
                break;
            case "readWriteIdleTime":
                this.readWriteIdleTime = Integer.parseInt(value);
                break;
            default:
                break;
        }
    }

    public int getReadWriteIdleTime() {
        return readWriteIdleTime;
    }

    public void setReadWriteIdleTime(int readWriteIdleTime) {
        this.readWriteIdleTime = readWriteIdleTime;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    public String getTrackerAddr() {
        return trackerAddr;
    }

    public void setTrackerAddr(String trackerAddr) {
        this.trackerAddr = trackerAddr;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getTrackerPoolMaxCount() {
        return trackerPoolMaxCount;
    }

    public void setTrackerPoolMaxCount(int trackerPoolMaxCount) {
        this.trackerPoolMaxCount = trackerPoolMaxCount;
    }

    public int getStoragePoolMaxCount() {
        return storagePoolMaxCount;
    }

    public void setStoragePoolMaxCount(int storagePoolMaxCount) {
        this.storagePoolMaxCount = storagePoolMaxCount;
    }

    @Override
    public void close() throws Exception {

    }


    @Override
    public String toString() {
        return "FdfsClientConfig{" +
                "trackerAddr='" + trackerAddr + '\'' +
                ", connectionTimeout=" + connectionTimeout +
                ", trackerPoolMaxCount=" + trackerPoolMaxCount +
                ", storagePoolMaxCount=" + storagePoolMaxCount +
                '}';
    }
}
