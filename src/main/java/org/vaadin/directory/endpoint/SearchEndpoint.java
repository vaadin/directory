package org.vaadin.directory.endpoint;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.fusion.Endpoint;
import com.vaadin.fusion.Nonnull;
import org.vaadin.directory.search.Addon;

import java.util.ArrayList;
import java.util.List;

@Endpoint
@AnonymousAllowed
public class SearchEndpoint {

    static final List<Addon> dummyAddonList = new ArrayList<>();

    public SearchEndpoint() {
        dummyAddonList.add(new Addon("Addon 1", "Text-only description"));
        dummyAddonList.add(new Addon("Second Addon", "Text-only description."));
    }

    public @Nonnull List<@Nonnull Addon> getAllAddons() {
        return dummyAddonList;
    }

    public @Nonnull List<@Nonnull Addon> search(String searchString) {
        return dummyAddonList;
    }

}