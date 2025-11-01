package org.vaadin.directory.store;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

@Component
public class Store {

    private final StoreSettings settings;
    private Firestore dbInstance;
    private final WeakHashMap<String, AddonRatingInfo> ratingCache = new WeakHashMap<>();
    private final WeakHashMap<String, UserInstallInfo> installCache = new WeakHashMap<>();

    private final WeakHashMap<String, Integer> statsCache = new WeakHashMap<>();

    public Store(@Autowired StoreSettings settings) {
        this.settings = settings;
    }

    public Firestore getDb() throws IOException {
        if (this.dbInstance != null) {
            return this.dbInstance;
        }

        InputStream input = new ByteArrayInputStream(settings.getValue("storekey").getBytes(StandardCharsets.UTF_8));
        GoogleCredentials credentials = GoogleCredentials.fromStream(input);

        FirestoreOptions options = FirestoreOptions.newBuilder().setCredentials(credentials).build();
        this.dbInstance = options.getService();
        return dbInstance;
    }

    @PreDestroy
    public void preDestroy() {
        if (this.dbInstance != null) {
            try {
                this.dbInstance.close();
            } catch (Exception e) {
                e.printStackTrace(); // TODO: logging
            }
        }
    }

    private void writeAddonRating(AddonRatingInfo addonRatingInfo) {
        try {
            // write through cache
            synchronized (this.ratingCache) {
                this.ratingCache.put(addonRatingInfo.getAddon(), addonRatingInfo);
            }
            WriteResult writeResult = this.getDb()
                    .collection("addonRatings")
                    .document(addonRatingInfo.getAddon()).set(addonRatingInfo)
                    .get();
        } catch (Exception e) {
            e.printStackTrace(); //TODO: logging
        }
    }

    private void writeUserInstall(UserInstallInfo installInfo) {
        try {
            // write through cache
            synchronized (this.installCache) {
                this.installCache.put(installInfo.getUserId(), installInfo);
            }
            WriteResult writeResult = this.getDb()
                    .collection("userInstalls")
                    .document(installInfo.getUserId()).set(installInfo)
                    .get();
        } catch (Exception e) {
            e.printStackTrace(); //TODO: logging
        }
    }

    private UserInstallInfo readUserInstalls(String userId) {

        // Try cache
        synchronized (this.ratingCache) {
            if (this.installCache.containsKey(userId)) {
                return this.installCache.get(userId);
            }
        }

        // Read remote and cache
        UserInstallInfo data = null;
        try {
            ApiFuture<DocumentSnapshot> documentSnapshotApiFuture = this.getDb()
                    .collection("userInstalls")
                    .document(userId)
                    .get();
            data = documentSnapshotApiFuture.get().toObject(UserInstallInfo.class);
            if (data == null) {
                // Create if missing
                data = new UserInstallInfo();
                data.setUserId(userId);
            }
            // Cache
            synchronized (this.installCache) {
                this.installCache.put(userId, data);
            }

        } catch (Exception e) {
            e.printStackTrace(); //TODO: logging
        }

        return data;
    }

    private AddonRatingInfo readAddonRating(String urlIdentifier) {

        // Try cache
        synchronized (this.ratingCache) {
            if (this.ratingCache.containsKey(urlIdentifier)) {
                return this.ratingCache.get(urlIdentifier);
            }
        }

        // Read remote and cache
        AddonRatingInfo data = null;
        try {
            ApiFuture<DocumentSnapshot> documentSnapshotApiFuture = this.getDb()
                    .collection("addonRatings")
                    .document(urlIdentifier)
                    .get();
            data = documentSnapshotApiFuture.get().toObject(AddonRatingInfo.class);
            // Create if missing
            if (data == null) {
                data = new AddonRatingInfo();
                data.setAddon(urlIdentifier);
                data.setRatingCount(0);
                data.setAvg((double) -1);
                data.setAvg180((double) -1);
            }

            // Cache
            synchronized (this.ratingCache) {
                ratingCache.put(urlIdentifier, data);
            }
        } catch (Exception e) {
            e.printStackTrace(); //TODO: logging
        }

        return data;
    }

    public int getUserRating(String urlIdentifier, String user) {
        AddonRatingInfo a = readAddonRating(urlIdentifier);
        if (a.getRatings() != null) {
            RatingInfo r = a.getRatings().stream().filter(e -> user.equals(e.getUser())).findFirst().orElse(null);
            return r != null ? r.getRating() : 0;
        }
        return -1;
    }

    public void setUserRating(String urlIdentifier, int rating, String user) {
        AddonRatingInfo a = readAddonRating(urlIdentifier);
        LocalDateTime now = LocalDateTime.now();

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
        r.setTimestamp(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()));

        // Rating count
        a.setRatings(rl);
        a.setRatingCount(rl.size());

        //  Store averages for all-time / 180 days
        Double avg = rl.stream().collect(Collectors.averagingInt(RatingInfo::getRating));
        Double avg180 = rl.stream().filter(ratingInfo ->
                    ChronoUnit.DAYS.between(now, ratingInfo.getTimestamp().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()) < 180)
                .collect(Collectors.averagingInt(RatingInfo::getRating));
        a.setAvg(avg);
        a.setAvg180(avg180);

        writeAddonRating(a);
    }

    public Double getAverageRating(String urlIdentifier) {
        AddonRatingInfo a = readAddonRating(urlIdentifier);
        return a.getAvg();
    }

    public int getRatingCount(String urlIdentifier) {
        AddonRatingInfo a = readAddonRating(urlIdentifier);
        return a.getRatingCount();
    }

    public void logInstall(String addon, String version, String type, String userId) {

        final String t = type.toUpperCase();
        if ("MAVEN".equals(t) || "BOWER".equals(t) || "NPMYARN".equals(t) || "ZIP".equals(t) || "CREATE".equals(t)) {

            // Log
            log(addon,version,"COMPONENT_UI_"+type.toUpperCase()+"_INSTALL",userId);

            // Update the total cache count
            incrementCachedTotalInstallCount(addon);

            // User installs
            UserInstallInfo data = readUserInstalls(userId);
            ArrayList<InstallInfo> list = new ArrayList<>();
            if (data.getInstalls() != null) {
                list.addAll(data.getInstalls());
            }

            // Update install time or add new install entry
            Optional<InstallInfo> previousInstall = list.stream()
                    .filter(i -> addon.equals(i.getAddon()) && type.equals(i.getType())
                            && version.equals(i.getVersion()))
                    .findFirst();
            if (previousInstall.isPresent()) {
                previousInstall.get().setTimestamp(Date.from(Instant.now()));
            } else {
                InstallInfo install = new InstallInfo(Date.from(Instant.now()),addon,version,type);
                list.add(install);
                data.setInstalls(list);
            }

            // Write
            try {
                writeUserInstall(data);
            } catch (Exception e) {
                e.printStackTrace(); //TODO: logging
            }
        }
    }

    public void log(String addon, String version, String event, String userId) {
        LogEntry l = new LogEntry(Date.from(Instant.now()), addon, version, "",
                event, userId, false);
        try {
            this.getDb().collection("log").add(l);
        } catch (Exception e) {
            e.printStackTrace(); //TODO: logging
        }
    }

    public List<String> getAddonInstalls(String addon, String user) {
        UserInstallInfo data = readUserInstalls(user);
        if (data.getInstalls() != null) {
            return data.getInstalls().stream().filter(i -> addon.equals(i.getAddon()))
                    .map(i -> i.getVersion()+"/"+i.getType()+"/"+i.getTimestamp())
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    public void incrementCachedTotalInstallCount(String addon) {
        // This is for limiting the total count queries
        Integer currentCount = getAddonInstallTotal(addon);
        if (statsCache.containsKey(addon)) {
            statsCache.put(addon, ++currentCount);
        }
    }

    public Integer getAddonInstallTotal(String addon) {
        try {
            // Check statsCache first
            if (statsCache.containsKey(addon)) {
                return statsCache.get(addon);
            }

            // Count all documents in log with matching field "addon"
            Query query = this.getDb().collection("log").whereEqualTo("addon", addon);
            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            int size = querySnapshot.get().size();

            // Cache results
            statsCache.put(addon,size);
            return size;

        } catch (Exception e) {
            e.printStackTrace(); //TODO: logging
        }
        return 0;
    }
}
