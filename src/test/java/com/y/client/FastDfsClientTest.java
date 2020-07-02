package com.y.client;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.y.fdfsclient.FastDfsClient;
import org.y.fdfsclient.exception.FastdfsClientException;
import org.y.fdfsclient.protocol.UploadFileResponse;

import java.io.File;
import java.io.IOException;

/**
 * @author szy47143
 * @date 2020/7/2 10:19
 */
public class FastDfsClientTest {
    FastDfsClient fastDfsClient;

    @Before
    public void init() throws FastdfsClientException {
        fastDfsClient = new FastDfsClient();
        fastDfsClient.create();
    }

    @Test
    public void uploadFile() throws Exception {
        String path = "D:\\newwork\\opensource\\lettuce\\fastdfs-client-java\\src\\main\\resources\\1.txt";
        File file = new File(path);
        byte[] bytes = FileUtils.readFileToByteArray(file);
        UploadFileResponse response = fastDfsClient.uploadFile(null, bytes, "txt");
        System.out.println("upload response: " + response);
    }

    @After
    public void close() {
        fastDfsClient.close();
    }
}
