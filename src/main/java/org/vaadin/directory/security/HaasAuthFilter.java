package org.vaadin.directory.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.vaadin.directory.backend.repository.liferay.LiferayUserRepository;
import com.vaadin.directory.entity.liferay.LiferayUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.client.RestClient;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

/**
 * Syncs the local Spring Security session with the vaadin.com / HaaS SSO session on each request.
 *
 * <p>The portal sets a marker cookie (e.g. {@code SSO_SESSION_RUNNING}) when a session exists.
 * On the first request of a session that carries the marker, this filter resolves the user by
 * calling a remote user-info endpoint (the {@code RemoteUserInfoService} pattern from
 * {@code auth-howto.md} §5), maps it to a directory screen name, and stores the resulting
 * {@link org.springframework.security.core.userdetails.UserDetails} principal (username = screen
 * name) in the HTTP session — so subsequent requests
 * reuse it without another remote call. When the marker disappears (SSO logout) the local
 * authentication is cleared. Any failure leaves the request anonymous; an outage never breaks it.
 */
public class HaasAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(HaasAuthFilter.class);

    private final SecurityProperties properties;
    private final LiferayUserRepository liferayUserRepository;
    private final RestClient restClient;
    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public HaasAuthFilter(SecurityProperties properties, LiferayUserRepository liferayUserRepository) {
        this.properties = properties;
        this.liferayUserRepository = liferayUserRepository;
        this.restClient = RestClient.create();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        // Not configured yet (placeholders) — leave auth untouched.
        if (properties.getCookieName().isBlank() || properties.getUserinfoUrl().isBlank()) {
            chain.doFilter(request, response);
            return;
        }

        boolean markerPresent = hasCookie(request, properties.getCookieName());
        Authentication existing = SecurityContextHolder.getContext().getAuthentication();

        if (existing != null) {
            // Log out locally when the SSO marker is gone. Never touch the local-dev faker's
            // session (it authenticates with a TestingAuthenticationToken, not our token).
            if (!markerPresent && existing instanceof UsernamePasswordAuthenticationToken) {
                SecurityContextHolder.clearContext();
                securityContextRepository.saveContext(
                        SecurityContextHolder.createEmptyContext(), request, response);
            }
            chain.doFilter(request, response);
            return;
        }

        // No local user yet: if the portal says a session exists, resolve it once and remember it.
        if (markerPresent) {
            String cookieHeader = request.getHeader(HttpHeaders.COOKIE);
            resolveUser(cookieHeader).ifPresent(principal -> {
                var authentication = new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities());
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
                securityContextRepository.saveContext(context, request, response);
            });
        }

        chain.doFilter(request, response);
    }

    private static boolean hasCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return false;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calls the user-info endpoint and maps the response ({@code UserSummary}) to a principal.
     * Forwards the full incoming {@code Cookie} header so whichever session cookie the (trusted,
     * first-party) user-info service validates is present. {@code UserSummary.id} is the Liferay
     * user id, which we resolve to the directory screen name the store keys ratings/installs by.
     * Swallows every error and returns empty so an outage never breaks the request.
     */
    private Optional<UserDetails> resolveUser(String cookieHeader) {
        try {
            JsonNode body = restClient.get()
                    .uri(properties.getUserinfoUrl())
                    .header(HttpHeaders.COOKIE, cookieHeader)
                    .headers(this::applyBasicAuth)
                    .retrieve()
                    .body(JsonNode.class);

            if (body == null || !body.hasNonNull("id")) {
                return Optional.empty();
            }
            long liferayUserId = body.get("id").asLong();

            LiferayUser liferayUser = liferayUserRepository.findByUserId(liferayUserId);
            if (liferayUser == null || liferayUser.getScreenName() == null
                    || liferayUser.getScreenName().isBlank()) {
                return Optional.empty();
            }

            // Principal username == screen name (what the store keys ratings/installs by).
            return Optional.of(new User(liferayUser.getScreenName(), "",
                    AuthorityUtils.createAuthorityList("ROLE_USER")));
        } catch (Exception e) {
            log.debug("HaaS user-info lookup failed; continuing anonymously", e);
            return Optional.empty();
        }
    }

    private void applyBasicAuth(HttpHeaders headers) {
        if (!properties.getUserinfoUsername().isBlank()) {
            String creds = properties.getUserinfoUsername() + ":" + properties.getUserinfoPassword();
            headers.set(HttpHeaders.AUTHORIZATION,
                    "Basic " + Base64.getEncoder().encodeToString(creds.getBytes(StandardCharsets.UTF_8)));
        }
    }
}
