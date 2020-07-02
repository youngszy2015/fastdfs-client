package org.y.fdfsclient.exception;

/**
 * @author szy47143
 * @date 2020/7/2 9:13
 */
public class FastDfsClientWriteException extends FastdfsClientException{

    public FastDfsClientWriteException() {
    }

    public FastDfsClientWriteException(String message) {
        super(message);
    }

    public FastDfsClientWriteException(String message, Throwable cause) {
        super(message, cause);
    }

    public FastDfsClientWriteException(Throwable cause) {
        super(cause);
    }

}
