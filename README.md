# fastdfs-client
fastdfs-client with netty

这是一个 [fastdfs](https://github.com/happyfish100/fastdfs-client-java) 项目的java实现的客户端。
协议参考已经实现的客户端  [fastdfs-client-java](https://github.com/happyfish100/fastdfs-client-java)。

Usage：

    ```
        FastDfsClient fastDfsClient = new FastDfsClient();
        fastDfsClient.create();
        String filePath = "";
        File file = new File(path);
        byte[] bytes = FileUtils.readFileToByteArray(file);
        UploadFileResponse response = fastDfsClient.uploadFile(null, bytes, "txt");
        fastDfsClient.close();
    ```