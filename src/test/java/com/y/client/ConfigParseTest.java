package com.y.client;

import org.junit.Test;
import org.y.fdfsclient.FdfsClientConfig;

import java.io.InputStream;

/**
 * @author szy47143
 * @date 2020/7/2 9:57
 */
public class ConfigParseTest {

    @Test
    public void testConfigParse() {
        FdfsClientConfig fdfsClientConfig = new FdfsClientConfig();
        fdfsClientConfig.parse();
        System.out.println(fdfsClientConfig);
    }
}
