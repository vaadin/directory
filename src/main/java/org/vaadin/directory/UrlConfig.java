package org.vaadin.directory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Configuration for all application visible URLs.
 *
 */
@PropertySource(value = {"classpath:application.properties"})
@Configuration
public class UrlConfig {

    @Value("${app.url}")
    private String appUrl;

    @Value("${app.componentUrl}")
    private String componentUrl;

    @Value("${app.componentEditBaseUrl}")
    private String componentEditBaseUrl;

    @Value("${app.imageBaseUrl}")
    private String imageBaseUrl;

    @Value("${app.defaultIconUrl}")
    private String defaultIconUrl;

    @Value("${app.addonProjectDownloadBaseUrl}")
    private String addonProjectDownloadBaseUrl;

    @Value("${app.addonZipDownloadBaseUrl}")
    private String addonZipDownloadBaseUrl;

    public String getAppUrl() {
        return appUrl;
    }

    public String getComponentUrl() {
        return componentUrl;
    }

    public String getComponentEditBaseUrl() {
        return componentEditBaseUrl;
    }

    public String getImageBaseUrl() {
        return imageBaseUrl;
    }

    public String getDefaultIconUrl() {
        return defaultIconUrl;
    }

    public String getAddonProjectDownloadBaseUrl() {
        return addonProjectDownloadBaseUrl;
    }

    public String getAddonZipDownloadBaseUrl() {
        return addonZipDownloadBaseUrl;
    }
}