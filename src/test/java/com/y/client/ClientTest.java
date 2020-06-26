package com.y.client;

import com.y.Client;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class ClientTest {

    @Test
    public void testInit() throws Exception {
        Client client = new Client("");
        client.init();
    }

}
