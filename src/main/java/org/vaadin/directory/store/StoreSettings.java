package org.vaadin.directory.store;

import com.google.api.services.sheets.v4.model.ValueRange;
import com.vaadin.directory.backend.util.GoogleSheet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Component
class StoreSettings extends GoogleSheet {

    @Value("${gapi.user.spreadsheet.id}")
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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }
}
