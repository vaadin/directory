package org.vaadin.directory.store;

import com.google.cloud.firestore.annotation.DocumentId;
import org.springframework.cloud.gcp.data.firestore.Document;

import java.util.List;

@Document
public class UserInstallInfo {

    @DocumentId
    private String userId;
    private List<InstallInfo> installs;

    public UserInstallInfo() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<InstallInfo> getInstalls() {
        return installs;
    }

    public void setInstalls(List<InstallInfo> installs) {
        this.installs = installs;
    }
}
