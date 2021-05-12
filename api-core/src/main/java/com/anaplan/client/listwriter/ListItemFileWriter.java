package com.anaplan.client.listwriter;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for exporting ListItems to files
 */
public class ListItemFileWriter {

  private static final Logger LOG = LoggerFactory.getLogger(ListItemFileWriter.class);

  /**
   * Converts to JSON and writes the result to a file identified by a path
   *
   * @param name         The name of the list to export
   * @param path         The path of the export file
   * @param elements     Elements to be exported as JSON
   * @param objectMapper ObjectMapper to be used for the JSON conversion
   */
  public static <T> void listToFile(String name, Path path, List<T> elements,
      ObjectMapper objectMapper) {
    try {
      ensureFileExists(path);
      objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), elements);
      LOG.info("{} exported successfully to the file - {}", name, path.getFileName());
    } catch (IOException e) {
      LOG.error("Issue while exporting {} to the file - {}", name, path.getFileName());
      throw new IllegalStateException(
          "Not able to export - " + name + " to the file - " + path.getFileName(), e);
    }
  }

  /**
   * Writes string lines to a file identified by a path
   *
   * @param listName The name of the list to export
   * @param path     The path of the export file
   * @param lines    Stream of strings to write
   */
  public static void listItemToFile(String listName, Path path, String lines) {
    listItemToFile(listName, path, Arrays.asList(lines));
  }

  /**
   * Writes string lines to a file identified by a path
   *
   * @param listName The name of the list to export
   * @param path     The path of the export file
   * @param iterable Iterable of strings to write
   */
  private static void listItemToFile(String listName, Path path, Iterable<String> iterable) {
    try {
      ensureFileExists(path);
      Files.write(path, iterable);
      LOG.info("List - {} exported successfully to the file - {}", listName, path.getFileName());
    } catch (IOException e) {
      LOG.error("Issue while exporting list - {} to the file - {}", listName, path.getFileName());
      throw new IllegalStateException(
          "Not able to export the list - " + listName + " to the file - " + path.getFileName(), e);
    }
  }

  /**
   * Writes string lines to a file identified by a path
   *
   * @param name  Name of the export
   * @param path  The path of the export file
   * @param lines Stream of strings to write
   */
  public static void linesToFile(String name, Path path, Stream<String> lines) {
    try {
      ensureFileExists(path);
      Files.write(path, (Iterable<String>) () -> lines.iterator());
      LOG.info("{} exported successfully to the file - {}", name, path.getFileName());
    } catch (IOException e) {
      LOG.error("Issue while exporting {} to the file - {}", name, path.getFileName());
      throw new IllegalStateException(
          "Not able to export - " + name + " to the file - " + path.getFileName(), e);
    }
  }

  /**
   * Writes string to a file identified by a path
   *
   * @param name  Name of the export
   * @param path  The path of the export file
   * @param lines String to write
   */
  public static void linesToFile(String name, Path path, String lines) {
    try {
      ensureFileExists(path);
      Files.write(path, lines.getBytes("utf-8"));
      LOG.info("View - {} exported successfully to the file - {}", name, path.getFileName());
    } catch (IOException e) {
      LOG.error("Issue while exporting View - {} to the file - {}", name, path.getFileName());
      throw new IllegalStateException(
          "Not able to export View - " + name + " to the file - " + path.getFileName(), e);
    }
  }

  /**
   * Utiltiy method to join strings using a comma separator
   *
   * @param lineItems Source strings
   * @return Result as a string
   */
  public static String concat(String[] lineItems) {
    return String.join(",", lineItems);
  }

  /**
   * Returns the string value of an object or an empty string for null values
   *
   * @param value Value to be exported
   * @return Result string
   */
  public static <T extends Object> String valueOrEmpty(T value) {
    return Optional.ofNullable(value)
        .map(String::valueOf)
        .map(ListItemFileWriter::escapeCsv)
        .orElse("");
  }

  /**
   * Escape so the CSV can be imported
   */
  public static String escapeCsv(String src) {
    src = src.replaceAll("\"", "\"\"");
    if (src.contains(",")) {
      src = "\"" + src + "\"";
    }
    return src;
  }

  /**
   * Utility for reading the keys of an object property (property is a map)
   *
   * @param mapSupplier Function to read the map property from the object
   * @return Keys as stream
   */
  static <U, T> List<String> getMapPropertyKeys(U u, Function<U, Map<String, T>> mapSupplier) {
    return Optional.ofNullable(u)
        .map(mapSupplier)
        .map(Map::keySet)
        .<List<String>>map(ArrayList::new)
        .orElse(Collections.emptyList());
  }

  /**
   * Utility for reading the values of an object property (property is a map)
   *
   * @param u             The object
   * @param mapSupplier   Function to read the map property from the object
   * @param valueSupplier Function to read the property value as string
   * @param searchKeys    Filter by these search keys
   * @return Values as stream
   */
  static <U, T> Stream<String> getMapPropertyValues(U u, Function<U, Map<String, T>> mapSupplier,
      Function<T, String> valueSupplier, List<String> searchKeys) {
    Map<String, T> propertyAsMap = mapSupplier.apply(u);
    if (propertyAsMap == null || propertyAsMap.isEmpty() || searchKeys == null || searchKeys
        .isEmpty()) {
      return Stream.empty();
    }
    return searchKeys.stream()
        .map(key -> Optional.ofNullable(propertyAsMap.get(key))
            .map(v -> valueSupplier.apply(v))
            .map(ListItemFileWriter::valueOrEmpty)
            .orElse(""));
  }

  /**
   * Retruns a stream of <count> elements of empty strings ''
   * @param count The number of returned elements
   * @return Stream of empty values
   */
  public static Stream<String> streamOfEmptyValues(int count) {
    String[] strings = new String[count];
    for (int i = 0; i < count; i++) {
      strings[i] = "";
    }
    return Stream.of(strings);
  }

  /**
   * Creates a file if it doesn't exist
   * @param path File path
   *
   * @throws IOException In case an error occurs
   */
  public static void ensureFileExists(Path path) throws IOException {
    File file = path.toFile();
    if (!file.exists()) {
      if (file.getParentFile() != null) {
        file.getParentFile().mkdirs();
      }
      file.createNewFile();
    }
  }

}
