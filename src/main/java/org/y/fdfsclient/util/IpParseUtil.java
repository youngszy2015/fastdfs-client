package org.y.fdfsclient.util;

public class IpParseUtil {


    public static IpPortInfo getIpPort(String addr) {
        String[] arr = addr.split(":");
        return new IpPortInfo(arr[0], Integer.parseInt(arr[1]));

    }


}
