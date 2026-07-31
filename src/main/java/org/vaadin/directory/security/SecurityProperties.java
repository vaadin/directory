package org.vaadin.directory.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Security / SSO configuration for resolving the current user server-side.
 *
 * <p>The vaadin.com / HaaS portal establishes a session via a shared cookie; the backend
 * validates it by calling a remote user-info endpoint (see {@link HaasAuthFilter}). All values
 * are externalized here so they can be overridden per environment.
 */
@ConfigurationProperties("directory.security")
public class SecurityProperties {

    /**
     * Name of the SSO marker cookie the portal sets on the vaadin.com domain to signal an active
     * session (e.g. {@code SSO_SESSION_RUNNING}). Presence gates the user-info lookup; the full
     * cookie header is then forwarded so the user-info service can validate the real session.
     */
    private String cookieName = "";

    /** URL of the user-info endpoint that resolves the SSO cookie to a user (screenname + id). */
    private String userinfoUrl = "";

    /** Optional basic-auth username for the user-info call. */
    private String userinfoUsername = "";

    /** Optional basic-auth password for the user-info call. */
    private String userinfoPassword = "";

    /**
     * Local-dev only: enables the {@link AuthFakerController} so developers can log in without
     * real SSO. Must stay {@code false} in production.
     */
    private boolean enableAuthFaker = false;

    public String getCookieName() {
        return cookieName;
    }

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    public String getUserinfoUrl() {
        return userinfoUrl;
    }

    public void setUserinfoUrl(String userinfoUrl) {
        this.userinfoUrl = userinfoUrl;
    }

    public String getUserinfoUsername() {
        return userinfoUsername;
    }

    public void setUserinfoUsername(String userinfoUsername) {
        this.userinfoUsername = userinfoUsername;
    }

    public String getUserinfoPassword() {
        return userinfoPassword;
    }

    public void setUserinfoPassword(String userinfoPassword) {
        this.userinfoPassword = userinfoPassword;
    }

    public boolean isEnableAuthFaker() {
        return enableAuthFaker;
    }

    public void setEnableAuthFaker(boolean enableAuthFaker) {
        this.enableAuthFaker = enableAuthFaker;
    }
}
