package org.vaadin.directory.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.vaadin.directory.DevelopmentMode;

/**
 * Local-development login "faker" (see {@code auth-howto.md} §6).
 *
 * <p>Active automatically in Vaadin development mode (see {@link DevelopmentMode}); in production it
 * responds {@code 404}, so it can never authenticate a real deployment. Setting
 * {@code directory.security.enable-auth-faker=true} (env {@code APP_AUTHENTICATION_ENABLE_AUTH_FAKER})
 * is an optional force-on override for a non-dev environment.
 *
 * <p>{@code POST /authfaker?screenname=<name>} authenticates the session as that user by placing a
 * {@link UserDetails} principal (username = screen name) into the session security context — exactly
 * what {@link HaasAuthFilter} would do for a real SSO cookie, so endpoints reading the
 * {@code AuthenticationContext} behave identically to production. {@code POST /authfaker} with no
 * screenname clears the session.
 */
@RestController
public class AuthFakerController {

    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();
    private final DevelopmentMode developmentMode;
    private final SecurityProperties properties;

    AuthFakerController(DevelopmentMode developmentMode, SecurityProperties properties) {
        this.developmentMode = developmentMode;
        this.properties = properties;
    }

    @PostMapping("/authfaker")
    public ResponseEntity<String> fakeLogin(
            @RequestParam(required = false) String screenname,
            HttpServletRequest request, HttpServletResponse response) {

        // Only in development (or when explicitly forced on); inert in production.
        if (!developmentMode.isDevelopmentMode() && !properties.isEnableAuthFaker()) {
            return ResponseEntity.notFound().build();
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        if (screenname != null && !screenname.isBlank()) {
            UserDetails principal = new User(screenname, "",
                    AuthorityUtils.createAuthorityList("ROLE_USER"));
            var token = new TestingAuthenticationToken(principal, null, principal.getAuthorities());
            token.setAuthenticated(true);
            context.setAuthentication(token);
        }
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        return ResponseEntity.ok(screenname == null || screenname.isBlank()
                ? "logged out" : "logged in as " + screenname);
    }
}
