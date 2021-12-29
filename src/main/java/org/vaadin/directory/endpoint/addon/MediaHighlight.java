package org.vaadin.directory.endpoint.addon;

import com.vaadin.directory.entity.directory.Highlight;
import com.vaadin.directory.entity.directory.HighlightScreenShot;

public class MediaHighlight {

    private String url;
    private String name;
    private String description;
    private int displayOrder;

    public MediaHighlight(Highlight highlight) {
        if (highlight instanceof HighlightScreenShot) {
            HighlightScreenShot screenshot = (HighlightScreenShot) highlight;
            this.url = Addon.IMAGE_LOCATION_URL + screenshot.getLocalFileName();
            this.name = screenshot.getName();
            this.displayOrder = screenshot.getDisplayOrder();
            this.description = screenshot.getDescription();
        }
    }

    public String getUrl() { return url; }

    public String getName() { return name; }

    public String getDescription() { return description; }

    public int getDisplayOrder() { return displayOrder; }

}
