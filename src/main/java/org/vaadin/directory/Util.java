package org.vaadin.directory;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
}
