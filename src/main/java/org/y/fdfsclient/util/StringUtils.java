package org.y.fdfsclient.util;

/**
 * @author szy47143
 * @date 2020/6/30 17:04
 */
public class StringUtils {


    public static boolean isEmpty(String str) {
        if (null == str || str.length() == 0) {
            return true;
        } else {
            return false;
        }
    }


}
