package org.vaadin.directory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Load properties from application.properties.
 *
 * This uses properties in Spring via Java configuration.
 */
@PropertySource(value = {"classpath:application.properties"})
@Configuration
public class BuildVersions {

    @Value("${app.version}")
    private String appVersion;

    @Value("${app.buildtime}")
    private String appBuildTime;

    @Value("${app.vaadin.version}")
    private String appVaadinVersion;

    @Value("${app.java.version}")
    private String appJavaVersion;

    private String startTime = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX").withZone(ZoneId.of("UTC")).format(Instant.now());

    public String getVersion() {
        return appVersion;
    }

    public String getBuildTime() {
        return appBuildTime;
    }

    public String getVaadinVersion() {
        return appVaadinVersion;
    }

    public String getJavaVersion() {
        return appJavaVersion;
    }

    public String getStartTime() { return startTime; }

}