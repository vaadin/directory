package org.vaadin.directory.store;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.WriteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.WeakHashMap;

@Component
public class Store {

    private StoreSettings settings;
    private Firestore firestoreInstance;
    private WeakHashMap<String, AddonRatingInfo> cache = new WeakHashMap<>();

    public Store(@Autowired StoreSettings settings) {
        this.settings = settings;
    }


    public Firestore getFirestore() throws IOException {
        if (this.firestoreInstance != null) {
            return this.firestoreInstance;
        }

        InputStream input = new ByteArrayInputStream(settings.getValue("storekey").getBytes(StandardCharsets.UTF_8));
        GoogleCredentials credentials = GoogleCredentials.fromStream(input);

        FirestoreOptions options = FirestoreOptions.newBuilder().setCredentials(credentials).build();
        this.firestoreInstance = options.getService();
        return firestoreInstance;
    }

    private void writeAddonRating(AddonRatingInfo a) {
        try {
            // write through cache
            synchronized (this.cache) {
                this.cache.put(a.getAddon(), a);
            }
            WriteResult writeResult = this.getFirestore().document("addonRatings/" + a.getAddon()).set(a).get();
        } catch (Exception e) {
            e.printStackTrace(); //TODO: logging
        }
    }

    private AddonRatingInfo readAddonRating(String urlIdentifier, boolean createIfMissing) {
        AddonRatingInfo a = null;

        // Try cache
        synchronized (this.cache) {
            if (this.cache.containsKey(urlIdentifier)) {
                return this.cache.get(urlIdentifier);
            }
        }

        // Read remote and cache
        try {
            ApiFuture<DocumentSnapshot> documentSnapshotApiFuture = this.getFirestore().document("addonRatings/" + urlIdentifier).get();
            a = documentSnapshotApiFuture.get().toObject(AddonRatingInfo.class);
            if (a != null) {
                synchronized (this.cache) {
                    cache.put(urlIdentifier, a);
                }
            }
            return a;
        } catch (Exception e) {
            e.printStackTrace(); //TODO: logging
        }

        if (createIfMissing) {
            a = new AddonRatingInfo();
            a.setAddon(urlIdentifier);
        }
        return a;
    }

    public int getUserRating(String urlIdentifier, String user) {
        AddonRatingInfo a = readAddonRating(urlIdentifier, false);
        RatingInfo r = a.getRatings().stream().filter(e -> user.equals(e.getUser())).findFirst().orElse(null);
        return r != null ? r.getRating() : 0;
    }

    public void setUserRating(String urlIdentifier, int rating, String user) {
        AddonRatingInfo a = readAddonRating(urlIdentifier, true);

        ArrayList<RatingInfo> rl = new ArrayList<>();
        if (a.getRatings() != null) {
            rl.addAll(a.getRatings());
        }

        // Find previous rating
        RatingInfo r = rl.stream().filter(e -> user.equals(e.getUser())).findFirst().orElse(null);

        if (r == null) {
            r = new RatingInfo();
            r.setUser(user);
            rl.add(r);
        }

        // User rating and date
        r.setRating(rating);
        r.setTimestamp(Date.from(Instant.now()));

        // Rating count
        a.setRatings(rl);
        a.setRatingCount(a.getRatingCount() + 1);

        //  Incremental mean value ('previous mean' * '(count -1)') + 'new value') / 'count'
        if (a.getRatingCount() > 1)
            a.setAvg((a.getAvg() * (double) (a.getRatingCount() - 1) + rating) / (double) a.getRatingCount());
        else a.setAvg((double) rating);

        writeAddonRating(a);
    }

    public Double getAverageRating(String urlIdentifier) {
        AddonRatingInfo a = readAddonRating(urlIdentifier, false);
        return a != null ? a.getAvg() : 0;
    }
}
