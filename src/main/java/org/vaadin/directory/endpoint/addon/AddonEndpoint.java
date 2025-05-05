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
    private UrlConfig urlConfig;

    AddonEndpoint(@Autowired ComponentService service,
                  @Autowired UserInfoService userNameService,
                  @Autowired Store store,
                  @Autowired AuditLogService logService,
                  @Autowired GoogleAnalytics analyticsService,
                  @Autowired UrlConfig urlConfig) {
        this.service = service;
        this.userNameService = userNameService;
        this.logService = logService;
        this.store = store;
        this.urlConfig = urlConfig;
        this.analyticsService = analyticsService;
    }

    @Transactional(readOnly = true)
    public Addon getAddon(String urlIdentifier, String currentUser) {
        if ("(not logged in)".equals((""+currentUser).trim())) currentUser = "Vaadin.156"; //TODO
        Optional<Component> maybeComponent = service.getComponentByUrl(urlIdentifier);
        long id = userNameService.findByScreenName(currentUser).stream().findFirst().orElse(-1L);
        return maybeComponent.isPresent() ? createAddon(maybeComponent.get(), id == maybeComponent.get().getOwner().getId()) : null;
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

    public int getUserRating(String urlIdentifier, String user) {
        if (urlIdentifier != null && !urlIdentifier.isEmpty() &&
        user != null && !user.isEmpty() && !user.contains("not logged")) {
            return store.getUserRating(urlIdentifier, user);
        }
        return -1;
    }

    public void setUserRating(String addon, int rating, String user) {
        if (!USER_NOT_LOGGED_IN.equals(user)) {
            store.setUserRating(addon, rating, user);
        }
    }

    public void logAddonInstall(String addon, String version, String type, String user) {
        store.logInstall(addon, version, type, user);
    }

    @Transactional(readOnly = true)
    public @Nonnull List<@Nonnull String> getAddonInstalls(String addon, String user) {
        if (addon == null || user == null || user.contains("not logged")) {
            return List.of();
        }
        return store.getAddonInstalls(addon, user);
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
    public Addon getAddon(String addonIdentifier) {
        return getAddon(addonIdentifier, null);
    }


    @Override
    public String getComponentUrl() {
        return urlConfig.getComponentUrl();
    }
}
