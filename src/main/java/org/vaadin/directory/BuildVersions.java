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

    @Value("${app.build-time}")
    private String appBuildTime;

    @Value("${app.vaadin.version}")
    private String appVaadinVersion;

    @Value("${app.java.version}")
    private String appJavaVersion;

    private String startTime = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX").withZone(ZoneId.of("UTC")).format(Instant.now());

    private final String jvmVersion = String.valueOf(Runtime.version().feature());

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

    /** Build JDK major version only, e.g. "25" from "25.0.1". */
    public String getJavaVersionMajor() {
        String v = appJavaVersion == null ? "" : appJavaVersion;
        int dot = v.indexOf('.');
        return dot > 0 ? v.substring(0, dot) : v;
    }

    /** Runtime JVM major version, e.g. "21". */
    public String getJvmVersion() {
        return jvmVersion;
    }

    public String getStartTime() { return startTime; }

}