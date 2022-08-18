package org.vaadin.directory.endpoint.addon;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import dev.hilla.Nonnull;
import javax.validation.constraints.NotBlank;

import com.vaadin.directory.entity.directory.Component;
import com.vaadin.directory.entity.directory.ComponentVersion;
import com.vaadin.directory.entity.directory.HighlightScreenShot;
import com.vaadin.directory.entity.directory.HighlightVideo;
import org.vaadin.directory.Util;

public class Addon {

    public static final String IMAGE_LOCATION_URL = "https://static.vaadin.com/directory/";
    public static final String DEFAULT_ICON_URL = "https://vaadin.com/images/directory/addon-icon-default.png";
    public static final String EDIT_URL_BASE = "https://vaadin.com/directory/component/edit/";

    @NotBlank
    @Nonnull
    private String urlIdentifier;

    @NotBlank
    @Nonnull
    private String name;

    @NotBlank
    @Nonnull
    private String icon;

    @NotBlank
    @Nonnull
    private String summary;

    @Nonnull
    private String description;

    @Nonnull
    private  List<MediaHighlight> mediaHighlights;

    @Nonnull
    private List<CodeSample> codeSamples;

    @Nonnull
    private Long downloadCount;

    @Nonnull
    private  List<@Nonnull String> tags;

    @Nonnull
    private  List<@Nonnull AddonVersion> versions;

    @Nonnull
    private  List<@Nonnull Link> links;

    @Nonnull
    private LocalDate lastUpdated;

    private @Nonnull String author;

    private @Nonnull Double rating;

    @Nonnull
    private Long ratingCount;

    @NotBlank
    @Nonnull
    private String discussionId;


    public Addon() {}

    public Addon(Component component) {
        this.urlIdentifier = component.getUrlIdentifier();
        this.name = component.getDisplayName();
        this.icon = component.getIcon() != null ?
                (component.getIcon().getLocalFileName().startsWith("http") ?
                        component.getIcon().getLocalFileName() :
                        Addon.IMAGE_LOCATION_URL + component.getIcon().getLocalFileName()):
                Addon.DEFAULT_ICON_URL;
        this.summary = component.getSummary();
        this.description = component.getDescription();
        this.downloadCount = component.getDownloadCountAsLong();
        this.lastUpdated = Util.dateToLocalDate(component.getLatestPublicationDate());
        this.author = "User " + component.getOwner().getId().toString();
        this.rating = component.getAverageRating() == null ? 0.0 : component.getAverageRating();
        this.ratingCount = component.getRatingCount();
        this.tags = Util.tagsToStrings(component.getTagGroups());
        this.versions = component.getVersions().stream()
                .filter(ComponentVersion::getAvailable)
                .map(cv -> new AddonVersion(cv))
                .sorted((v1,v2) -> v1.getDate().compareTo(v2.getDate()))
                .collect(Collectors.toList());
        //TODO: Generate icons
        this.links = component.getLinks().stream()
                .filter(l -> l.getUrl() != null && !l.getUrl().isBlank())
                .map(l -> new Link(l.getTitle(), l.getUrl(), null))
                .collect(Collectors.toList());

        this.mediaHighlights = component.getHighlights().stream()
                .filter(highlight ->
                        highlight instanceof HighlightScreenShot
                                || highlight instanceof HighlightVideo)
                .map(highlight -> new MediaHighlight(highlight))
                .collect(Collectors.toList());

        this.codeSamples = component.getCodeHighlights().stream()
                .map(highlight -> new CodeSample(highlight))
                .collect(Collectors.toList());

        this.discussionId = Long.toString(component.getId());

    }

    public String getUrlIdentifier() { return urlIdentifier; }

    public void setUrlIdentifier(String urlIdentifier) { this.urlIdentifier = urlIdentifier; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() { return icon; }

    public void setIcon(String icon) { this.icon = icon;}

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) { this.description = description; }

    public List<MediaHighlight> getMediaHighlights() { return mediaHighlights; }

    public void setMediaHighlights(List<MediaHighlight> mediaHighlights) { this.mediaHighlights = mediaHighlights; }

    public Long getDownloadCount() { return downloadCount; }

    public void setDownloadCount(Long downloadCount) { this.downloadCount = downloadCount; }

    public List<String> getTags() { return tags; }

    public void setTags(List<String> tags) { this.tags = tags;}

    public List<AddonVersion> getVersions() { return versions; }

    public void setVersions(List<AddonVersion> versions) { this.versions = versions;}

    public List<Link> getLinks() { return links; }

    public void setLinks(List<Link> links) { this.links = links; }

    public LocalDate getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDate lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Long getRatingCount() { return ratingCount; }

    public void setRatingCount(Long ratingCount) { this.ratingCount = ratingCount; }

    public List<CodeSample> getCodeSamples() { return codeSamples; }

    public void setCodeSamples(List<CodeSample> codeSamples) { this.codeSamples = codeSamples; }

    public String getDiscussionId() { return discussionId; }

    public void setDiscussionId(String discussionId) { this.discussionId = discussionId;}

}
