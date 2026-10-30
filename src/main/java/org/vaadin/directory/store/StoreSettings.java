package org.vaadin.directory.store;

import com.google.api.services.sheets.v4.model.ValueRange;
import com.vaadin.directory.backend.util.GoogleSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Component
class StoreSettings extends GoogleSheet {

    private static final Logger LOG = LoggerFactory.getLogger(StoreSettings.class);

    @Value("${gapi.application.settings}")
    private String settingsId;

    public StoreSettings() {
    }

    @Override
    protected String getSheetId() {
        return this.settingsId;
    }

    public String getValue(String key) {
        try {
            ValueRange r = getRange(key);
            List<List<Object>> values = r.getValues();
            return values.get(0).get(0).toString();
        } catch (IOException | GeneralSecurityException e) {
            LOG.error("Failed to read setting '{}' from settings sheet '{}'. Check "
                    + "gapi.credentials.file.path (env GAPI_CREDENTIALS_FILE_PATH) and sheet access.",
                    key, settingsId, e);
        } catch (RuntimeException e) {
            LOG.error("Unexpected error reading setting '{}' from settings sheet '{}'",
                    key, settingsId, e);
        }
        return null;
    }
}
