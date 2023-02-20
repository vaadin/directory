package org.vaadin.directory.endpoint.addon;

import com.vaadin.directory.backend.service.ComponentService;
import com.vaadin.directory.backend.service.UserInfoService;
import com.vaadin.directory.entity.directory.Component;
import com.vaadin.directory.entity.directory.ComponentDirectoryUser;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.Endpoint;
import dev.hilla.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.vaadin.directory.UrlConfig;
import org.vaadin.directory.Util;
import org.vaadin.directory.store.Store;

import java.util.List;
import java.util.Optional;

@Endpoint
@AnonymousAllowed
public class AddonEndpoint {

    private static final String USER_NOT_LOGGED_IN = "(not logged in)";
    private final UserInfoService userNameService;
    private final ComponentService service;
    private final Store store;
    private UrlConfig urlConfig;

    AddonEndpoint(@Autowired ComponentService service,
                  @Autowired UserInfoService userNameService,
                  @Autowired Store store,
                  @Autowired UrlConfig urlConfig) {
        this.service = service;
        this.userNameService = userNameService;
        this.store = store;
        this.urlConfig = urlConfig;
    }

    @Transactional(readOnly = true)
    public Addon getAddon(String urlIdentifier, String currentUser) {
        Optional<Component> maybeComponent = service.getComponentByUrl(urlIdentifier);
        long id = userNameService.findByScreenName(currentUser).stream().findFirst().orElse(-1L);
        return maybeComponent.isPresent() ? createAddon(maybeComponent.get(), id == maybeComponent.get().getOwner().getId()) : null;
    }

    private Addon createAddon(Component c, boolean addEditLink) {
        Addon a = new Addon(c, urlConfig);
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
        user != null && !user.isEmpty() && user.contains("not logged")) {
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

    public @Nonnull List<@Nonnull String> getAddonInstalls(String addon, String user) {
        return store.getAddonInstalls(addon, user);
    }

}
