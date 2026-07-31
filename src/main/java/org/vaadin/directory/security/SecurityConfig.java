package org.vaadin.directory.security;

import com.vaadin.directory.backend.repository.liferay.LiferayUserRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security setup for the Directory app (see {@code auth-howto.md} §2).
 *
 * <p>This is a Hilla app whose login happens externally on the client (HaaS), so there is no
 * server-side login view. Every URL is therefore public at the HTTP layer; access control happens
 * per Hilla endpoint via {@code @AnonymousAllowed}/{@code @PermitAll} annotations (enforced by
 * Hilla's {@code EndpointController}, independently of this chain). The {@link HaasAuthFilter} runs
 * on every request to sync the security context from the HaaS SSO cookie.
 *
 * <p>We deliberately do NOT use {@code VaadinWebSecurity}: it is built around a server login view
 * and installs {@code anyRequest().authenticated()} + a form-login redirect to {@code /login},
 * which for this client-routed app sends unauthenticated navigations to a non-existent
 * {@code /login} route.
 */
@EnableWebSecurity
@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

    private final SecurityProperties properties;
    private final LiferayUserRepository liferayUserRepository;

    public SecurityConfig(SecurityProperties properties, LiferayUserRepository liferayUserRepository) {
        this.properties = properties;
        this.liferayUserRepository = liferayUserRepository;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        // No server-side login: never redirect to a /login view or challenge with basic auth.
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.logout(AbstractHttpConfigurer::disable);

        // Allow same-origin framing (do not send X-Frame-Options: DENY).
        http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));

        // Hilla endpoints, the MCP server, and the local-dev faker are POSTed without a CSRF token.
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/connect/**", "/mcp", "/mcp/**", "/authfaker"));

        // Sync the security context from the HaaS SSO cookie.
        http.addFilterBefore(new HaasAuthFilter(properties, liferayUserRepository),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
