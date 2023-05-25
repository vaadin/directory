package org.vaadin.directory;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import com.vaadin.directory.backend.service.UserInfoService;
import com.vaadin.directory.entity.directory.ComponentDirectoryUser;
import com.vaadin.directory.entity.directory.ComponentFrameworkVersion;
import com.vaadin.directory.entity.directory.TagGroup;
import io.swagger.codegen.utils.SemVer;

public class Util {

  public static LocalDate dateToLocalDate(Date date) {
    if (date == null)
      return LocalDate.of(2000, 1, 1);
    return date.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDate();
  }

  public static List<String> tagsToStrings(Set<TagGroup> tags) {
    if (tags == null) {
      return List.of();
    }
    return tags.stream()
        .map(t -> t.getName())
        .collect(Collectors.toList());
  }

  public static String getVersionName(ComponentFrameworkVersion version) {
    String fw = version.getFramework().getName();
    String fwv = version.getVersion();
    if (fw.endsWith(" platform")) {
      fw = fw.substring(0,fw.length()-9);
    } else if (fw.matches(".+(\\s[0-9]+)")) {
      fw = fw.replaceFirst("\\s[0-9]+", "");
    }
    return fw + " " + fwv;
  }

  public static List<String> matchingVersionStrings(String versionName) {
    List<String> matchingVersionStrings = new ArrayList<>();
    matchingVersionStrings.add(versionName);
    // Expand version like "6.7" to match "6.0+,6.1+,6.2+,6.3+,6.4+,6.5+,6.6+,6.7+" and "6.7"
    if(versionName.contains(".")) {
      final int major = Integer.parseInt(versionName.substring(0, versionName.indexOf('.')));
      final int minor = Integer.parseInt(versionName.substring(versionName.indexOf('.') + 1));
      for (int i = 0; i <= minor; i++) {
        matchingVersionStrings.add(major + "." + i + "+");
      }
      // Expand version like "11" to match "10+, 11, 11+"
    } else {
      final int minor = Integer.parseInt(versionName);
      for (int i = 10; i <= minor; i++) {
        matchingVersionStrings.add(i + "+");
      }
    }

    return matchingVersionStrings;
  }

  public static int compareSemver(String v0, String v1) {
    return compareSemver(v0,v1, false);
  }

  public static int compareSemver(String v0, String v1, boolean reverse) {
    try {
      SemVer sv0 = new SemVer(v0);
      SemVer sv1 = new SemVer(v1);
      return reverse ? sv1.compareTo(sv0) : sv0.compareTo(sv1);
    } catch (NumberFormatException ignored) {
    }
    return v0.compareTo(v1);
  }

  public static String getNameOrGitHubId(ComponentDirectoryUser owner, UserInfoService userNameService) {
    String name = userNameService.getNameforId(owner.getId());
    String ghLogin = owner.getGitHubLogin();
    // Fallback to use GitHub name if no username found
    name = name.indexOf(""+ owner.getId()) >= 0 && ghLogin != null ? ghLogin : name;
    return name;
  }

}

