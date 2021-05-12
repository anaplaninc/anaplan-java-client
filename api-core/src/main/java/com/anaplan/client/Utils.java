package com.anaplan.client;

import com.anaplan.client.exceptions.AnaplanAPIException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Spondon Saha Date: 5/5/18 Time: 3:48 PM
 */
public class Utils {

  /**
   * Provide a suitable error message from an exception.
   *
   * @param thrown the exception
   * @return a message describing the exception
   * @since 1.3
   */
  public static String formatThrowable(Throwable thrown) {
    StringBuilder message = new StringBuilder(
        thrown instanceof AnaplanAPIException ? "AnaplanAPI" : thrown
            .getClass().getSimpleName());
    if (message.length() > 9 && message.toString().endsWith("Exception")) {
      message.delete(message.length() - 9, message.length());
    }
    for (int i = 1; i < message.length() - 1; ++i) {
      char pc = message.charAt(i - 1);
      char ch = message.charAt(i);
      char nc = message.charAt(i + 1);
      if (Character.isUpperCase(ch)) {
        if (!Character.isUpperCase(nc)) {
          message.setCharAt(i, Character.toLowerCase(ch));
        }
        if (!Character.isUpperCase(pc) || !Character.isUpperCase(nc)) {
          message.insert(i++, ' ');
        }
      }
    }
    if (null != thrown.getMessage()) {
      message.append(": ").append(thrown.getMessage());
    }
    if (null != thrown.getCause()) {
      message.append(" (").append(formatThrowable(thrown.getCause()))
          .append(')');
    }
    return message.toString();
  }

  /**
   * Format values as tab-separated text
   *
   * @param values a list of values
   * @return tab-separated text
   * @since 1.3
   */
  public static String formatTSV(Object... values) {
    StringBuilder tsv = new StringBuilder();
    for (Object value : values) {
      if (tsv.length() > 0) {
        tsv.append('\t');
      }
      if (value != null) {
        tsv.append(value.toString());
      }
    }
    return tsv.toString();
  }

  /**
   * Split CSV line to non empty tokens
   * @param line Source line
   * @return A list of parsed values
   */
  public static List<String> splitValues(String line) {
    String regex = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
    return Arrays.stream(line.split(regex))
        .filter(s1 -> s1 != null && !"".equals(s1))
        .collect(Collectors.toList());
  }

}
