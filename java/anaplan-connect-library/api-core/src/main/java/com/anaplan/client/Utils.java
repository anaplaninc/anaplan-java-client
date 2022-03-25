package com.anaplan.client;

import com.anaplan.client.exceptions.AnaplanAPIException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * Created by Spondon Saha Date: 5/5/18 Time: 3:48 PM
 */
public class Utils {

  private static final CSVFormat.Builder PROPERTY_FORMAT_BUILDER = CSVFormat.newFormat('=').builder().setQuote('"');

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

  /**
   * Extract and return the list of column values
   * @param lines
   * @param startIndex
   * @return
   */
  public static List<String> getColumnValues(String[] lines, int startIndex) {
    //Regex - ,(?=(?:[^"]*"[^"]*")*[^"]*$) - matches the character , that's not inside the double quotes
    return IntStream.range(startIndex, lines.length).filter(index -> lines[index].startsWith(","))
            .mapToObj(index -> {
                      String regex;
                      regex = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
                      return Arrays.stream(lines[index].split(regex)).filter((s1) -> s1 != null && !"".equals(s1)).collect(Collectors.joining(","));
                    }
            ).collect(Collectors.toList());
  }

  /**
   * If source not exist and is not a file an Exception is throw
   * @param source source to check
   * @throws FileNotFoundException
   */
  public static void isFileAndReadable(final Path source) throws FileNotFoundException {
    if (source == null || !source.toFile().exists()) {
      throw new FileNotFoundException("Path \"" + source
          + "\" does not exist");
    }
    if (!source.toFile().isFile()) {
      throw new FileNotFoundException("Path \"" + source
          + "\" exists but is not a file");
    } else if (!source.toFile().canRead()) {
      throw new AnaplanAPIException(
          "File \""
              + source
              + "\" cannot be read - check ownership and/or permissions");
    }
  }

  /**
   *
   * @param value to be search in list
   * @param list Collection
   * @return key if exist in list
   */
  public static String findInList(final String value, final Collection<String> list ){
    for (final String listValue : list) {
      if (listValue.equalsIgnoreCase(value)) {
        return listValue;
      }
    }
    return null;
  }

  /**
   *
   * @param collection
   * @return
   */
  public static boolean collectionIsEmpty(final Collection collection) {
    return collection == null || collection.size() == 0;
  }

  /**
   *
   * @param map
   * @return
   */
  public static boolean mapIsEmpty(final Map map) {
    return map == null || map.size() == 0;
  }

  /**
   *
   * @param value
   * @return
   * @throws IOException
   */
  public static String getParsedValue(final String value) throws IOException {
    if (value == null) {
      return null;
    }
    final CSVFormat format = PROPERTY_FORMAT_BUILDER.build();
    List<CSVRecord> keyParsed = CSVParser.parse(value, format)
        .getRecords();
    if (keyParsed.size() > 0) {
      return keyParsed.get(0).get(0);
    }
    return value;
  }

  public static Map<String, String> getPropertyFile(final InputStream inputStream)
      throws IOException {

    final Map<String, String> result = new HashMap<>();
    final LineNumberReader lnr = new LineNumberReader(
        new InputStreamReader(inputStream));
    String line;

    final CSVFormat format = PROPERTY_FORMAT_BUILDER.build();
    while (null != (line = lnr.readLine())) {
      final List<CSVRecord> records = CSVParser.parse(line, format).getRecords();
        if (records.size() > 0) {
          result.put(records.get(0).get(0), records.get(0).get(1));
        }
    }
    return result;
  }
}
