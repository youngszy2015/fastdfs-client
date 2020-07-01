package org.y.fdfsclient.exception;

public class FastdfsClientException extends Exception{

    public FastdfsClientException() {
        super();
    }

    public FastdfsClientException(String message) {
        super(message);
    }

    public FastdfsClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public FastdfsClientException(Throwable cause) {
        super(cause);
    }
}
