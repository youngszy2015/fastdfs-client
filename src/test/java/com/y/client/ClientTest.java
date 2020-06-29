package com.y.client;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.y.fdfsclient.FdfsClient;
import org.y.fdfsclient.protocol.GroupInfo;

import java.util.List;

@Slf4j
public class ClientTest {

    @Test
    public void testInit() throws Exception {
        String trackerAddr = System.getProperty("trackerAddr");
        FdfsClient client = new FdfsClient(trackerAddr);
        client.init();
    }

}
