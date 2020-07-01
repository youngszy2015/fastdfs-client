package org.y.fdfsclient.util;

public class Assert {

    public static void notNull(Object obj, String message) {
        if (null == obj) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty(CharSequence charSequence, String message) {
        if (null == charSequence || charSequence.length() == 0) {
            throw new IllegalArgumentException(message);
        }
    }
}
