package com.dongkuksystems.dbox.utils;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateTimeUtils {
  
  public static String addYears(long year) {
    if (year == 0) {
      return LocalDate.parse("9999-12-31").toString();
    }
    return LocalDate.now().plusYears(year).toString();
  }
  
  /*
   * ex
  */
  public static String now(String format) {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern(format));
  }
  /*
   * ex
  */
  public static Timestamp timestampOf(LocalDateTime time) {
    return time == null ? null : Timestamp.valueOf(time);
  }

  /*
   * ex
  */
  public static LocalDateTime dateTimeOf(Timestamp timestamp) {
      return timestamp == null ? null : timestamp.toLocalDateTime();
  }

  /*
   * ex
  */
  public static LocalDateTime addDays(LocalDateTime time, int days) {
    return time == null ? null : time.plusDays(days);
  }

  /*
   * ex
  */
  public static LocalDateTime minusDays(LocalDateTime time, int days) {
    return time == null ? null : time.minusDays(days);
  }

  /*
   * ex
  */
  public static boolean isBefore(LocalDateTime time) {  
    return time == null ? null : time.toLocalDate().isBefore(LocalDate.now());
  }
  
  /*
   * ex
  */
  public static boolean isAfter(LocalDateTime time) { 
    return time == null ? null : time.toLocalDate().isAfter(LocalDate.now());
  }

  /*
   * ex
  */
  public static Long beetweenDays(LocalDateTime time) {
    return time == null ? null : ChronoUnit.DAYS.between(time, LocalDateTime.now()); 
  }
}
