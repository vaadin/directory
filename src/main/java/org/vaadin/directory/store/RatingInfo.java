package org.vaadin.directory.store;

import org.springframework.cloud.gcp.data.firestore.Document;

import java.util.Date;

@Document(collectionName = "ratings")
public class RatingInfo {

    private String user;

    private Integer rating;

    private Date timestamp;

    public RatingInfo() {
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
