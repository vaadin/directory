package org.vaadin.directory.discussion;

import org.vaadin.directory.endpoint.addon.Addon;

/** Interface for the getting addon details for forum thread creation.
 *
 */
public interface AddonInfoService {
    /**
     * Get the addon human-readable name.
     *
     * @param addonIdentifier The URL identifier of the addon
     * @return The identifier of the addon
     */
    Addon getAddonInfo(String addonIdentifier);

    String getComponentUrl();
}
