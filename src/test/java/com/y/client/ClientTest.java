package com.y.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.y.fdfsclient.FdfsClient;

import java.io.File;

@Slf4j
public class ClientTest {

    FdfsClient client;

    @Before
    public void before() throws Exception {
        String trackerAddr = System.getProperty("trackerAddr");
        client = new FdfsClient(trackerAddr);
        client.init();
    }

    @Test
    public void testInit() throws Exception {

    }


    @Test
    public void testUpload() throws Exception {
        String path = "D:\\newwork\\opensource\\lettuce\\fastdfs-client-java\\src\\main\\resources\\1.txt";
        File file = new File(path);
        byte[] bytes = FileUtils.readFileToByteArray(file);
        client.uploadFile(bytes, null);
    }


    @Test
    public void testDownload() throws Exception {
        String groupName = "group1";
        String path = "M00/1E/FE/CmSfjF776M6AI0ELAAAAEuQpMgk723.txt";
        byte[] bytes = client.downloadFile(groupName, path);
        File file = new File("D:\\zlz\\fastdfs-client\\src\\test\\java\\com\\y\\client\\" + System.currentTimeMillis() + ".txt");
        if (!file.exists()) file.createNewFile();
        FileUtils.writeByteArrayToFile(file, bytes);
    }

}
