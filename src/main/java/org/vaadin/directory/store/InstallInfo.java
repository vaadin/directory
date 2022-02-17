package org.vaadin.directory.store;

import java.util.Date;

public class InstallInfo {

    private Date timestamp;
    private String addon;
    private String version;
    private String type;

    public InstallInfo() {
    }

    public InstallInfo(Date timestamp, String addon, String version, String type) {
        this.timestamp = timestamp;
        this.addon = addon;
        this.version = version;
        this.type = type;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
