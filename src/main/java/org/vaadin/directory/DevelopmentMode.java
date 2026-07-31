package org.vaadin.directory;

import com.vaadin.flow.server.VaadinServletContext;
import com.vaadin.flow.server.startup.ApplicationConfiguration;
import jakarta.servlet.ServletContext;
import org.springframework.stereotype.Component;

/**
 * Generic "are we running in development mode?" signal for the app.
 *
 * <p>Backed by Vaadin's production-mode flag, which is the project's authoritative dev/prod
 * distinction: the production Docker image is built with {@code -Pproduction} (→ {@code build-frontend}
 * → {@code flow-build-info.json productionMode:true}), while a local {@code mvn spring-boot:run} runs
 * in development mode. Because {@code vaadin.productionMode} is not a Spring {@code Environment}
 * property here, this is read at runtime from {@link ApplicationConfiguration} rather than via a
 * {@code @ConditionalOnProperty}.
 */
@Component
public class DevelopmentMode {

    private final ServletContext servletContext;
    private volatile Boolean cached;

    DevelopmentMode(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * {@code true} when the app runs in Vaadin development mode. Evaluated lazily (the first call
     * happens while serving a request, by when Vaadin is fully initialized) and cached. If the Vaadin
     * configuration is not available yet, returns {@code false} — a production-safe default.
     */
    public boolean isDevelopmentMode() {
        Boolean c = cached;
        if (c != null) {
            return c;
        }
        ApplicationConfiguration config =
                ApplicationConfiguration.get(new VaadinServletContext(servletContext));
        if (config == null) {
            return false; // Vaadin not initialized yet — assume production (faker stays off).
        }
        boolean dev = !config.isProductionMode();
        cached = dev;
        return dev;
    }
}
