package org.vaadin.directory.store;

import com.google.api.services.sheets.v4.model.ValueRange;
import com.vaadin.directory.backend.util.GoogleSheet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FeaturedAddons extends GoogleSheet {

    private static final long EXPIRE_TIME_MS = 1000*60*10; // 10 minutes

    @Value("${gapi.user.spreadsheet.id}")
    private String sheetId;
    private List<String> featuredCache;
    private long storedAt;

    @Override
    protected String getSheetId() {
        return sheetId;
    }

    private boolean cacheTimeExpired() {
        return System.currentTimeMillis() > this.storedAt + EXPIRE_TIME_MS;
    }


    public List<String> listFeatured() {
        if (this.featuredCache != null && !cacheTimeExpired()) {
            return this.featuredCache;
        }
        
        try {
            ValueRange r = getRange("featured");
            List<List<Object>> values = r.getValues();
            List<String> list = values.get(0).stream().map(Object::toString).collect(Collectors.toList());
            this.featuredCache = list;
            this.storedAt = System.currentTimeMillis();
            return list;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

}
