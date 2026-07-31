package org.vaadin.directory.security;

import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Server-side accessor for the authenticated user's directory screen name.
 *
 * <p>The screen name is resolved from the session security context (populated by
 * {@link HaasAuthFilter} from the HaaS SSO cookie, or by the local-dev {@link AuthFakerController}),
 * never from client-supplied input. Endpoints must derive the current user from here rather than
 * trusting a request parameter.
 *
 * <p>Exposed as a bean so it can be referenced both from Java code and from Spring {@code @Cacheable}
 * key SpEL (e.g. {@code key = "... + @currentUser.screenNameOrAnon()"}), which also avoids the
 * self-invocation pitfall of computing a cache key inside the cached bean itself.
 */
@Component("currentUser")
public class CurrentUser {

    /** Placeholder principal name used for anonymous requests (also serves as a stable cache key). */
    public static final String ANONYMOUS = "(not logged in)";

    private final AuthenticationContext authenticationContext;

    CurrentUser(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    /**
     * The authenticated user's screen name, or empty when the request is anonymous. Never throws:
     * outside a Vaadin/Hilla request (e.g. plain MVC controllers, the MCP service) it degrades to
     * empty rather than propagating an error.
     */
    public Optional<String> screenName() {
        try {
            return authenticationContext.getAuthenticatedUser(UserDetails.class)
                    .map(UserDetails::getUsername)
                    .filter(name -> name != null && !name.isBlank());
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /** The current screen name, or {@link #ANONYMOUS} when anonymous. Suitable for cache keys. */
    public String screenNameOrAnon() {
        return screenName().orElse(ANONYMOUS);
    }
}
