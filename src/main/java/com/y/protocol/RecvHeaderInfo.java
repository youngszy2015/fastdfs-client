package com.y.protocol;

public class RecvHeaderInfo {
    public byte errno;
    public long body_len;

    public RecvHeaderInfo(byte errno, long body_len) {
      this.errno = errno;
      this.body_len = body_len;
    }
  }