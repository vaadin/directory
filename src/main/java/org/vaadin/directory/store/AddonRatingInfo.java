package org.vaadin.directory.store;

import com.google.cloud.firestore.annotation.DocumentId;
import org.springframework.cloud.gcp.data.firestore.Document;

import java.util.List;

@Document(collectionName = "addonRatings")
public class AddonRatingInfo {

    @DocumentId
    private String addon;

    private int ratingCount;

    private Double avg;

    private Double avg180;

    private List<RatingInfo> ratings;

    public AddonRatingInfo() {
    }

    public String getAddon() {
        return addon;
    }

    public void setAddon(String addon) {
        this.addon = addon;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public Double getAvg() {
        return avg;
    }

    public void setAvg(Double avg) {
        this.avg = avg;
    }

    public List<RatingInfo> getRatings() {
        return ratings;
    }

    public void setRatings(List<RatingInfo> ratings) {
        this.ratings = ratings;
    }

    public Double getAvg180() {
        return avg180;
    }

    public void setAvg180(Double avg180) {
        this.avg180 = avg180;
    }
}
