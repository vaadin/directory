package org.vaadin.directory;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.directory.entity.directory.ComponentFrameworkVersion;
import com.vaadin.directory.entity.directory.TagGroup;

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
}

