package org.vaadin.directory.endpoint.addon;

import com.vaadin.directory.backend.maven.PomXmlUtil;
import com.vaadin.directory.entity.directory.ComponentVersion;
import com.vaadin.directory.entity.directory.License;
import org.vaadin.directory.UrlConfig;
import org.vaadin.directory.Util;

import dev.hilla.Nonnull;
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
    private List<@Nonnull String> compatibility;

    @Nonnull
    private List<@Nonnull String> browserCompatibility;

    @Nonnull
    private Map<String, String> installs;

    @NotBlank
    @Nonnull
    private String releaseNotes;

    public AddonVersion() { }

    public AddonVersion(ComponentVersion cv, UrlConfig urlConfig) {
        this.name = cv.getName();
        this.date = Util.dateToLocalDate(cv.getPublicationDate());
        this.maturity = cv.getMaturity().name();
        if (cv.getLicenses() != null) {
            cv.getLicenses().stream().findFirst().ifPresent( l -> {
                this.license = l.getName();
            });
        }
        if (this.license == null) {
            this.license = License.OTHER_LICENSE;
        }
        this.compatibility = cv.getFrameworkVersions().stream().sorted()
                .map(Util::getVersionName)
                .collect(Collectors.toList());
        this.browserCompatibility = cv.getBrowserIndependent() ? List.of("Browser Independent") :
                    cv.getSupportedBrowsers().stream().map(b -> b.getName())
                            .collect(Collectors.toList());
        this.installs = new LinkedHashMap<>();
        if (cv.isMavenDeployed()) {
            String snippet = PomXmlUtil.getDependencyPomSnippet(cv);
            if (cv.getMavenGroupId().startsWith("org.vaadin.")) {
                snippet += "\n\n<!-- Vaadin Maven repository -->\n";
                snippet += PomXmlUtil.getRepositoryPomSnippet();
            }

            this.installs.put("Maven", snippet);
        }
        if (cv.isWebComponent()) {
            this.installs.put("Bower", PomXmlUtil.getWebJarDependencyPomSnippet(cv));
        }
        if (cv.isDownloadable()) {
            License l = cv.getLicenses() != null && cv.getLicenses().size() > 0? cv.getLicenses().stream().findFirst().orElse(null): null;
            this.installs.put("Zip", urlConfig.getAddonZipDownloadBaseUrl() + cv.getContentForLicense(l).getLocalFileName());
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

    public List<@Nonnull String> getCompatibility() {
        return compatibility;
    }

    public List<@Nonnull String> getBrowserCompatibility() {
        return browserCompatibility;
    }

    public Map<String,String> getInstalls() {
        return installs;
    }

    public String getReleaseNotes() {
        return releaseNotes;
    }

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

    public void setBrowserCompatibility(List<String> browserCompatibility) { this.browserCompatibility = browserCompatibility; }

    public void setInstalls(Map<String, String> installs) {
        this.installs = installs;
    }

    public void setReleaseNotes(String releaseNotes) {
        this.releaseNotes = releaseNotes;
    }

}
