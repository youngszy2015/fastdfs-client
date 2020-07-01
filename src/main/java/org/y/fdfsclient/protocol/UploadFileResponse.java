package org.y.fdfsclient.protocol;

/**
 * @author szy47143
 * @date 2020/6/30 18:49
 */
public class UploadFileResponse {

    private String path;

    private String groupName;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public String toString() {
        return "UploadFileResponse{" +
                "path='" + path + '\'' +
                ", groupName='" + groupName + '\'' +
                '}';
    }
}
