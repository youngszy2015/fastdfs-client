package com.y.protocol;

public class RecvPackageInfo {
    public byte errno;
    public byte[] body;

    public RecvPackageInfo(byte errno, byte[] body) {
        this.errno = errno;
        this.body = body;
    }
}