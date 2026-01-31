package org.vaadin.directory.mcp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration for Spring AI MCP endpoints.
 *
 * Spring AI 1.1.2 does not include built-in CORS support for MCP endpoints,
 * so we must implement custom WebMvcConfigurer for browser clients and MCP Inspector.
 * This configuration applies only to /mcp endpoints.
 */
@Configuration
public class McpCorsConfiguration implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/mcp/**")
                // Use localhost patterns instead of wildcard for security
                .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")

                // Add all HTTP methods for full REST support
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")

                // Allow all headers
                .allowedHeaders("*")

                // Enable credentials for authenticated requests
                .allowCredentials(true)

                // Expose MCP-specific headers
                .exposedHeaders("Mcp-Session-Id", "Content-Type")

                .maxAge(3600);
    }
}
