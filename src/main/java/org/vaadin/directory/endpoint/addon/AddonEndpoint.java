package org.vaadin.directory.endpoint.addon;

import com.vaadin.directory.backend.service.AuditLogService;
import com.vaadin.directory.backend.service.ComponentService;
import com.vaadin.directory.backend.service.UserInfoService;
import com.vaadin.directory.backend.util.GoogleAnalytics;
import com.vaadin.directory.entity.directory.Component;
import com.vaadin.directory.entity.directory.ComponentDirectoryUser;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;
import com.vaadin.hilla.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.vaadin.directory.UrlConfig;
import org.vaadin.directory.Util;
import org.vaadin.directory.security.CurrentUser;
import org.vaadin.directory.discussion.AddonInfoService;
import org.vaadin.directory.store.Store;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Endpoint
@AnonymousAllowed
public class AddonEndpoint implements AddonInfoService {

    private static final String USER_NOT_LOGGED_IN = "(not logged in)";
    private final UserInfoService userNameService;
    private final ComponentService service;
    private final AuditLogService logService;
    private final Store store;
    private final GoogleAnalytics analyticsService;
    private final CurrentUser currentUser;
    private UrlConfig urlConfig;

    AddonEndpoint(@Autowired ComponentService service,
                  @Autowired UserInfoService userNameService,
                  @Autowired Store store,
                  @Autowired AuditLogService logService,
                  @Autowired GoogleAnalytics analyticsService,
                  @Autowired UrlConfig urlConfig,
                  @Autowired CurrentUser currentUser) {
        this.service = service;
        this.userNameService = userNameService;
        this.logService = logService;
        this.store = store;
        this.urlConfig = urlConfig;
        this.analyticsService = analyticsService;
        this.currentUser = currentUser;
    }

    /**
     * The current user's screen name resolved server-side from the security context (populated by
     * {@code HaasAuthFilter}), or empty when the request is anonymous. Endpoints must derive the
     * user from here rather than trusting a client-supplied parameter.
     */
    private Optional<String> currentUser() {
        return currentUser.screenName();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "cache5m", key = "'addon' + #urlIdentifier + '_' + @currentUser.screenNameOrAnon()")
    public Addon getAddon(String urlIdentifier) {
        // Ownership (which gates the "Edit" link) is decided from the server-side session identity,
        // never from a client-supplied name. Anonymous requests own nothing.
        Optional<Component> maybeComponent = service.getComponentByUrl(urlIdentifier);
        if (maybeComponent.isEmpty()) {
            return null;
        }
        Component component = maybeComponent.get();
        boolean isOwner = currentUser().flatMap(name ->
                        userNameService.findByScreenName(name).stream().findFirst())
                .map(id -> id.longValue() == component.getOwner().getId())
                .orElse(false);
        return createAddon(component, isOwner);
    }

    private Addon createAddon(Component c, boolean addEditLink) {
        Addon a = new Addon(c, this.urlConfig, this.store);
        if (addEditLink) {
            a.getLinks().add(new Link("Edit",
                    urlConfig.getComponentEditBaseUrl() + c.getUrlIdentifier(),
                    null));
        }
        String name = Util.getNameOrGitHubId(c.getOwner(), this.userNameService);
        a.setAuthor(name);
        String image = this.userNameService.getImageforId(c.getOwner().getId());
        a.setAuthorImage(urlConfig.getProfileImageBaseUrl()+image);
        return a;
    }

    public Double getAverageRating(String addon) {
        return store.getAverageRating(addon);
    }

    public int getUserRating(String urlIdentifier) {
        Optional<String> user = currentUser();
        if (urlIdentifier != null && !urlIdentifier.isEmpty() && user.isPresent()) {
            return store.getUserRating(urlIdentifier, user.get());
        }
        return -1;
    }

    public void setUserRating(String addon, int rating) {
        currentUser().ifPresent(user -> store.setUserRating(addon, rating, user));
    }

    public void logAddonInstall(String addon, String version, String type) {
        store.logInstall(addon, version, type, currentUser().orElse(USER_NOT_LOGGED_IN));
    }

    @Transactional(readOnly = true)
    public @Nonnull List<@Nonnull String> getAddonInstalls(String addon) {
        Optional<String> user = currentUser();
        if (addon == null || user.isEmpty()) {
            return List.of();
        }
        return store.getAddonInstalls(addon, user.get());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "cache1h", key = "'installs' + #addon")
    public @Nonnull Integer getAddonInstallCount(String addon) {
        return Math.round((float)getAddonInstallCountExact(addon) /100)*100;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "cache24h", key = "'installsExact' + #addon")
    public @Nonnull Integer getAddonInstallCountExact(String addon) {
            return store.getAddonInstallTotal(addon);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "cache24h", key = "'maven' +#addonUrl")
    public @Nonnull Long getAddonMavenDownloadCount(String addonUrl) {
        Component c = service.getComponentByUrl(addonUrl).orElse(null);
        if (c != null) return logService.getUIMavenInstallCount(c);
        return 0L;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "cache24h", key = "'visits' + #days + '_' + #addonUrl")
    public @Nonnull List<Long> getVisits(int days, String addonUrl) {
        Component c = service.getComponentByUrl(addonUrl).orElse(null);
        if (c != null) return analyticsService.getDailyStats(days,"component/"+addonUrl);
        return List.of();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "cache24h", key = "'countries' + #days + '_' + #addonUrl")
    public @Nonnull Map<String, Long> getCountries(int days, String addonUrl) {
        Component c = service.getComponentByUrl(addonUrl).orElse(null);
        if (c != null) return analyticsService.getTopCountries(days,"component/"+addonUrl);
        return Map.of();
    }

    @Override
    @Transactional(readOnly = true)
    public Addon getAddonInfo(String addonIdentifier) {
        return getAddon(addonIdentifier);
    }


    @Override
    public String getComponentUrl() {
        return urlConfig.getComponentUrl();
    }
}
