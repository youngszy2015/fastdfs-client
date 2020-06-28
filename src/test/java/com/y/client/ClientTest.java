package com.y.client;

import com.y.Client;
import org.junit.Test;
import org.y.fdfsclient.FdfsClient;

import java.util.concurrent.TimeUnit;

public class ClientTest {

    @Test
    public void testInit() throws Exception {
        String trackerAddr = System.getProperty("trackerAddr");
        FdfsClient client = new FdfsClient(trackerAddr);
        client.init();
        client.getListGroup();
        Thread.sleep(Integer.MAX_VALUE);
    }

}
