package org.openbravo.scheduling;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides utility methods that help to deal with dates when scheduling a process.
 */
class SchedulerTimeUtils {

  private static final Logger log = LogManager.getLogger();
  private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter
      .ofPattern("dd-MM-yyyy HH:mm:ss");

  private SchedulerTimeUtils() {
  }

  /**
   * Utility method to parse a start date string and a start time string into a {@link Date}.
   * 
   * @param date
   *          A date as a String. Expected format: 'dd-MM-yyyy'
   * 
   * @param time
   *          A time as a String. Expected format: 'HH:mm:ss'
   * 
   * @return a {@link Date} with the provided date and time.
   * 
   * @throws ParseException
   *           if the provided date and time can not be parsed to create the {@link Date} instance.
   */
  static Date timestamp(String date, String time) throws ParseException {
    LocalDateTime localDateTime = parse(date, time);
    try {
      return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    } catch (Exception ex) {
      log.error("Could not parse date {} {}", date, time, ex);
      throw new ParseException("Could not parse date " + date + " " + time, -1);
    }
  }

  /**
   * Utility method to parse a start date string and a start time string into a
   * {@link LocalDateTime}.
   * 
   * @param date
   *          A date as a String. Expected format: 'dd-MM-yyyy'
   * 
   * @param time
   *          A time as a String. Expected format: 'HH:mm:ss'
   * 
   * @return a {@link LocalDateTime} with the provided date and time.
   * 
   * @throws ParseException
   *           if the provided date and time can not be parsed to create the {@link LocalDateTime}
   *           instance.
   */
  static LocalDateTime parse(String date, String time) throws ParseException {
    try {
      return LocalDateTime.parse(date + " " + time, DEFAULT_FORMATTER);
    } catch (DateTimeParseException ex) {
      log.error("Could not parse date {} {}", date, time, ex);
      throw new ParseException("Could not parse date " + date + " " + time, -1);
    }
  }

  /**
   * Formats the current date using a specific format.
   * 
   * @param format
   *          the date time format to be applied.
   * @return a String with the current date time formatted with the provided format.
   * 
   */
  static String currentDate(String format) {
    return format(LocalDateTime.now(), format);
  }

  /**
   * Formats the provided date using a specific format.
   * 
   * @return a String with the provided date formatted with the provided format.
   * 
   * @param format
   *          the date time format to be applied.
   */
  static String format(Date date, String format) {
    LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    return format(localDateTime, format);
  }

  private static String format(LocalDateTime localDateTime, String format) {
    return localDateTime.format(DateTimeFormatter.ofPattern(format));
  }
}
