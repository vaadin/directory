package org.vaadin.directory.endpoint.addon;

import com.vaadin.directory.backend.service.ComponentService;
import com.vaadin.directory.backend.service.UserInfoService;
import com.vaadin.directory.entity.directory.Component;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.fusion.Endpoint;
import com.vaadin.fusion.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
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

    AddonEndpoint(@Autowired ComponentService service,
                  @Autowired UserInfoService userNameService,
                  @Autowired Store store) {
        this.service = service;
        this.userNameService = userNameService;
        this.store = store;
    }

    @Transactional(readOnly = true)
    public Addon getAddon(String urlIdentifier) {
        Optional<Component> maybeComponent = service.getComponentByUrl(urlIdentifier);
        return maybeComponent.isPresent() ? createAddon(maybeComponent.get()) : null;
    }

    private Addon createAddon(Component c) {
        Addon a = new Addon(c);
        a.setAuthor(userNameService.getNameforId(c.getOwner().getId()));
        return a;
    }

    public Double getAverageRating(String addon) {
        return store.getAverageRating(addon);
    }

    public int getUserRating(String urlIdentifier, String user) {
        return store.getUserRating(urlIdentifier, user);
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
