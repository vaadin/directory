package org.vaadin.directory.store;

import org.springframework.cloud.gcp.data.firestore.Document;

import java.util.Date;

@Document
public class LogEntry {


    private Date timestamp;
    private String addon;
    private String version;
    private String details;
    private String type;
    private String userId;
    private boolean alreadyNotified;

    public LogEntry(Date timestamp, String addon, String version, String details,
                    String type, String userId, boolean alreadyNotified) {
        this.timestamp = timestamp;
        this.addon = addon;
        this.version = version;
        this.details = details;
        this.type = type;
        this.userId = userId;
        this.alreadyNotified = alreadyNotified;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getAddon() {
        return addon;
    }

    public void setAddon(String addon) {
        this.addon = addon;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isAlreadyNotified() {
        return alreadyNotified;
    }

    public void setAlreadyNotified(boolean alreadyNotified) {
        this.alreadyNotified = alreadyNotified;
    }

}
