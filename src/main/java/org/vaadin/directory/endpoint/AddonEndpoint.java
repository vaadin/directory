package org.vaadin.directory.endpoint;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.fusion.Endpoint;
import com.vaadin.fusion.Nonnull;
import org.vaadin.directory.search.Addon;


@Endpoint
@AnonymousAllowed
public class AddonEndpoint {

    private Addon dummyAddon = new Addon("MyAddon", "Text-only description");

    public @Nonnull Addon getAddon(String urlIdentifier) {
        return dummyAddon;
    }

    /*
     * TODO: We need the full details for the view public @Nonnull @Nonnull Addon
     * getAddonDetails(String urlIdentifier) { return addon; }
     */

}
