package org.vaadin.directory.endpoint.search;

import com.vaadin.directory.entity.directory.ComponentDirectoryUser;
import com.vaadin.hilla.Nonnull;
import jakarta.validation.constraints.NotBlank;

public class UserInfo {

    public static final UserInfo NOT_FOUND =  new UserInfo("User Not Found", "notfound", "n/a");
    @NotBlank
    @Nonnull
    private String screenName;

    @NotBlank
    @Nonnull
    private String image;

    @NotBlank
    @Nonnull
    private String fullName;

    public UserInfo(String fullName, String screenName, String image) {
        this.fullName = fullName;
        this.screenName = screenName;
        this.image = image;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getImage() {
        return image;
    }

    public String getFullName() {
        return fullName;
    }
}
