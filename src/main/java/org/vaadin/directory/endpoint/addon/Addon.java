package org.vaadin.directory.endpoint.addon;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import com.vaadin.hilla.Nonnull;
import jakarta.validation.constraints.NotBlank;

import com.vaadin.directory.entity.directory.Component;
import com.vaadin.directory.entity.directory.ComponentVersion;
import com.vaadin.directory.entity.directory.HighlightScreenShot;
import com.vaadin.directory.entity.directory.HighlightVideo;
import org.vaadin.directory.UrlConfig;
import org.vaadin.directory.Util;
import org.vaadin.directory.store.Store;

public class Addon {

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

    private @Nonnull String authorImage;

    private @Nonnull Double rating;

    @Nonnull
    private Long ratingCount;

    @NotBlank
    @Nonnull
    private String discussionId;

    @NotBlank
    @Nonnull
    private String addonProjectDownloadBaseUrl;

    public Addon() {}

    public Addon(Component component, UrlConfig urlConfig, Store store) {
        this.urlIdentifier = component.getUrlIdentifier();
        this.addonProjectDownloadBaseUrl = urlConfig.getAddonProjectDownloadBaseUrl();
        this.name = component.getDisplayName();
        this.icon = component.getIcon() != null ?
                (component.getIcon().getLocalFileName().startsWith("http") ?
                        component.getIcon().getLocalFileName() :
                        urlConfig.getImageBaseUrl() + component.getIcon().getLocalFileName()):
                urlConfig.getDefaultIconUrl();
        this.summary = component.getSummary();
        this.description = component.getDescription();
        this.downloadCount = component.getDownloadCountAsLong();
        this.lastUpdated = Util.dateToLocalDate(component.getLatestPublicationDate());
        this.author = "User " + component.getOwner().getId().toString();
        this.authorImage = urlConfig.getProfileImageBaseUrl()+"0";
        this.rating = weightedAvg(component.getAverageRating() == null ? 0.0 : component.getAverageRating(),
                component.getRatingCount() == null ? 0 : component.getRatingCount(),
                store.getAverageRating(this.urlIdentifier),
                store.getRatingCount(this.urlIdentifier)
                );
        this.ratingCount = component.getRatingCount() == null ? 0 : component.getRatingCount() + store.getRatingCount(this.urlIdentifier);
        this.tags = Util.tagsToStrings(component.getTagGroups());
        Comparator<AddonVersion> compareAddonDates = (o1, o2) -> {
            if (o1 != null && o2 != null) {
                return Comparator
                        .comparing(AddonVersion::getDate)
                        .thenComparing(AddonVersion::getName)
                        .compare(o1,o2);
            }
            return 0;
        };
        Predicate<? super ComponentVersion> availableFilter = (Predicate<ComponentVersion>) componentVersion -> componentVersion != null &&
                componentVersion.getAvailable() != null &&
                componentVersion.getAvailable();
        this.versions = component.getVersions().stream()
                .filter(availableFilter)
                .map(cv -> new AddonVersion(cv, urlConfig))
                .sorted(compareAddonDates)
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
                .map(highlight -> new MediaHighlight(highlight, urlConfig))
                .collect(Collectors.toList());

        this.codeSamples = component.getCodeHighlights().stream()
                .map(highlight -> new CodeSample(highlight))
                .collect(Collectors.toList());

        this.discussionId = Long.toString(component.getId());

    }

    public static double weightedAvg(double avg1, long count1, double avg2, long count2) {
        if (count1 == 0 && count2 == 0) return 0;
        return (avg1 * count1 + avg2 * count2) / (count1+count2);
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

    public String getAuthorImage() {
        return authorImage;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setAuthorImage(String imageUrl) {
        this.authorImage = imageUrl;
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

    public String getAddonProjectDownloadBaseUrl() { return addonProjectDownloadBaseUrl; }

}
