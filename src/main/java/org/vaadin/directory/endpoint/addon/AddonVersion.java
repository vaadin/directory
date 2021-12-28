package org.vaadin.directory.endpoint.addon;

import com.vaadin.directory.backend.maven.PomXmlUtil;
import com.vaadin.directory.entity.directory.ComponentVersion;
import com.vaadin.fusion.Nonnull;
import org.vaadin.directory.Util;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AddonVersion {

    @NotBlank
    @Nonnull
    private String name;

    @Nonnull
    private LocalDate date;

    @NotBlank
    @Nonnull
    private String maturity;

    @NotBlank
    @Nonnull
    private String license;

    @Nonnull
    private List<String> compatibility;

    @Nonnull
    private List<String> browserCompatibility;

    public void setName(String name) {
        this.name = name;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setMaturity(String maturity) {
        this.maturity = maturity;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public void setCompatibility(List<String> compatibility) {
        this.compatibility = compatibility;
    }

    public void setBrowserCompatibility(List<String> browserCompatibility) {
        this.browserCompatibility = browserCompatibility;
    }

    public void setInstalls(Map<String, String> installs) {
        this.installs = installs;
    }

    public void setReleaseNotes(String releaseNotes) {
        this.releaseNotes = releaseNotes;
    }

    @Nonnull
    private Map<String, String> installs;


    @NotBlank
    @Nonnull
    private String releaseNotes;

    public AddonVersion() { }

    public AddonVersion(ComponentVersion cv) {
        this.name = cv.getName();
        this.date = Util.dateToLocalDate(cv.getPublicationDate());
        this.maturity = cv.getMaturity().name();
        // TODO: This needs to be more sophisticated for some addons
        this.license = cv.getLicenses().iterator().next().getName();
        this.compatibility = cv.getFrameworkVersions().stream()
                .map(v -> v.getFramework().getName()+" "+v.getVersion())
                .collect(Collectors.toList());
        this.browserCompatibility = cv.getBrowserIndependent() ? List.of("Browser Independent") :
                    cv.getSupportedBrowsers().stream().map(b -> b.getName())
                            .collect(Collectors.toList());
        this.installs = new LinkedHashMap<>();
        if (cv.isMavenDeployed()) {
            this.installs.put("Maven", PomXmlUtil.getDependencyPomSnippet(cv));
        }
        if (cv.isWebComponent()) {
            this.installs.put("Bower", PomXmlUtil.getWebJarDependencyPomSnippet(cv));
        }
        if (cv.isDownloadable()) {
            this.installs.put("Zip", cv.getContentForLicense(cv.getLicenses().iterator().next()).getLocalFileName());
        }

        this.releaseNotes = cv.getReleaseNotes();
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() { return date; }

    public String getMaturity() {
        return maturity;
    }

    public String getLicense() {
        return license;
    }

    public List<String> getCompatibility() {
        return compatibility;
    }

    public List<String> getBrowserCompatibility() {
        return browserCompatibility;
    }

    public Map<String,String> getInstalls() {
        return installs;
    }

    public String getReleaseNotes() {
        return releaseNotes;
    }

}
