package com.anaplan.client;

import com.anaplan.client.dto.ListMetadataProperty;
import com.anaplan.client.exceptions.AnaplanAPIException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Spondon Saha Date: 5/5/18 Time: 3:48 PM
 */
public class Utils {

  private Utils(){}
  private static final CSVFormat.Builder PROPERTY_FORMAT_BUILDER = CSVFormat.newFormat('=').builder().setQuote('"');
  private static final CSVFormat.Builder LINE_FORMAT_BUILDER = CSVFormat.newFormat(',').builder().setQuote('"');
  private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

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
    final StringBuilder messageResult = new StringBuilder(message.toString());
    int j = 0;
    for (int i = 1; i < message.length() - 1; ++i) {
      char pc = message.charAt(i - 1);
      char ch = message.charAt(i);
      char nc = message.charAt(i + 1);
      if (Character.isUpperCase(ch)) {
        if (!Character.isUpperCase(nc)) {
          messageResult.setCharAt(i + j, Character.toLowerCase(ch));
        }
        if (!Character.isUpperCase(pc) || !Character.isUpperCase(nc)) {
          messageResult.insert(i + j, ' ');
          j++;
        }
      }
    }
    if (null != thrown.getMessage()) {
      messageResult.append(": ").append(thrown.getMessage());
    }
    if (null != thrown.getCause()) {
      messageResult.append(" (").append(formatThrowable(thrown.getCause()))
          .append(')');
    }
    return messageResult.toString();
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
        tsv.append(value);
      }
    }
    return tsv.toString();
  }

  /**
   * Split CSV line to non empty tokens
   * @param line Source line
   * @return A list of parsed values
   */
  public static List<String> splitValues(String line) throws IOException {
    final CSVFormat format = LINE_FORMAT_BUILDER.build();
    CSVParser keyParsed = CSVParser.parse(line, format);
    List<String> list = new ArrayList<>();
    for (CSVRecord s1 : keyParsed.getRecords()) {
      for (String str : s1.toList()) {
        if (str != null) {
          if (str.contains(format.getDelimiterString()) || str.contains(format.getQuoteCharacter().toString())) {
            list.add("\""+ str+"\"");
          } else {
            list.add(str);
          }
        }
      }
    }
    return list;
  }

  /**
   * Extract and return the list of column values
   * @param lines the lines
   * @param startIndex index limit
   * @return column values
   */
  public static List<String> getColumnValues(String[] lines, int startIndex) {
    //Regex - ,(?=(?:[^"]*"[^"]*")*[^"]*$) - matches the character , that's not inside the double quotes
    return IntStream.range(startIndex, lines.length).filter(index -> lines[index].startsWith(","))
        .mapToObj(index -> {
              String regex;
              regex = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
              return Arrays.stream(lines[index].split(regex)).filter(s1 -> s1 != null && !"".equals(s1)).collect(Collectors.joining(","));
            }
        ).collect(Collectors.toList());
  }

  /**
   * If source not exist and is not a file an Exception is throw
   * @param source source to check
   * @throws FileNotFoundException if file is not found
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
   * Check if a collection is empty
   * @param collection collection to be check
   * @return true if is empty
   */
  public static boolean collectionIsEmpty(final Collection<String[]> collection) {
    return collection.isEmpty();
  }

  /**
   * Check if a map is empty
   * @param map the map to be check
   * @return true is is empty
   */
  public static boolean mapIsEmpty(final Map<String, String> map) {
    return map.isEmpty();
  }

  /**
   * CSV parse
   * @param value value to be parsed
   * @return csv parsed value
   * @throws IOException parser error
   */
  public static String getParsedValue(final String value) throws IOException {
    if (value == null) {
      return null;
    }
    final CSVFormat format = PROPERTY_FORMAT_BUILDER.build();
    List<CSVRecord> keyParsed = CSVParser.parse(value, format)
        .getRecords();
    if (!keyParsed.isEmpty()) {
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
      if (!records.isEmpty()) {
        result.put(records.get(0).get(0), records.get(0).get(1));
      }
    }
    return result;
  }

  /**
   * Safe close a random file
   * @param sourceFile {@link RandomAccessFile}
   * @param source the source name
   */
  public static void closeDataInput(final RandomAccessFile sourceFile, final String source) {
    if (sourceFile != null) {
      try {
        sourceFile.close();
      } catch (IOException ioException) {
        LOG.warn("Warning: failed to close file {}: {}", source, ioException.getMessage());
      }
    }
  }

  /**
   * Safe close result set and statement
   * @param resultSet {@link ResultSet}
   * @param statement {@link Statement}
   */
  public static void closeJDBC(final ResultSet resultSet, final Statement statement) {
    if (resultSet != null) {
      try {
        resultSet.close();
      } catch (SQLException sqle) {
        LOG.error("Failed to close result set: {0}", sqle);
      }
    }
    if (statement != null) {
      try {
        statement.close();
      } catch (SQLException sqle) {
        LOG.error("Failed to close prepared statement: {0}", sqle);
      }
    }
  }

  /**
   * Safe close statement
   * @param preparedStatement {@link PreparedStatement}
   * @throws SQLException jdbc error
   */
  public static void closeStatement(final PreparedStatement preparedStatement) throws SQLException {
    if (preparedStatement != null && !preparedStatement.isClosed()) {
      preparedStatement.close();
    }
  }

  /**
   * Check line separator
   * @param separator the column separator
   */
  public static void checkSeparator(final String separator) {
    if (separator == null || "".equals(separator) || separator.length() > 1) {
      throw new AnaplanAPIException("Separator \"" + separator + "\" is not valid");
    }
  }

  /**
   * Checks provided file status
   * @param target
   * @param path
   * @param file
   * @param deleteExisting
   * @throws IOException
   */
  public static void checkTarget(final File target, String path, String file, boolean deleteExisting) throws IOException {
    if (target.exists()) {
      if (!target.isFile()) {
        throw new FileNotFoundException(path + target
            + "\" exists but is not a file");
      } else if (!deleteExisting) {
        throw new IllegalStateException(file + target
            + "\" already exists");
      } else if (!target.canWrite()) {
        throw new FileNotFoundException(
            file
                + target
                + "\" cannot be written to - check ownership and/or permissions");
      }
      Files.delete(target.toPath());
    }
  }

  /**
   * Get absolute path if the path is relative
   * @param target the path to check
   * @return absolute path
   */
  public static Path getAbsolutePath(Path target) throws FileNotFoundException {
    if (target == null) {
      throw new FileNotFoundException("Path does not exist");
    }
    if (!target.isAbsolute()) {
      target = Paths.get(target.toFile().getAbsolutePath());
    }
    return target;
  }

  public static byte[] createHash(String clientId) {
    byte[] encodedHash = new byte[0];
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-512/256");
      encodedHash = digest.digest(clientId.getBytes(StandardCharsets.UTF_8));
    } catch (NoSuchAlgorithmException e) {
      LOG.error("The SHA algorithm was not found. {}", e.getMessage());
    }
    return encodedHash;
  }

  public static String bytesToHex(byte[] hash) {
    StringBuilder hexString = new StringBuilder(2 * hash.length);
    for (byte b : hash) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }

  /**
   * Loads the Keystore and provides the resulted encrypted Key
   * @param fileInputStream
   * @param keyStoreName
   * @param password
   * @return {@link Key}
   * @throws KeyStoreException
   * @throws IOException
   * @throws NoSuchAlgorithmException
   * @throws CertificateException
   * @throws UnrecoverableKeyException
   */
  public static Key loadKeystore(FileInputStream fileInputStream, String keyStoreName, char[] password)
      throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {

    KeyStore jks = KeyStore.getInstance("JKS");
    jks.load(fileInputStream, password);
    Key secretKeyAlias = jks.getKey(keyStoreName, password);
    return secretKeyAlias;
  }

  /**
   * Saves the provided encoded key into the keystore
   * @param encodedKey
   * @param keyStoreName
   * @param password
   * @return {@link KeyStore}
   * @throws KeyStoreException
   * @throws IOException
   * @throws NoSuchAlgorithmException
   * @throws CertificateException
   */
  public static KeyStore saveEntryInKeyStore(byte[] encodedKey, String keyStoreName, char[] password)
      throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
    KeyStore ks = KeyStore.getInstance("pkcs12");
    ks.load(null, password);

    SecretKey mySecretKey = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
    KeyStore.SecretKeyEntry skEntry = new KeyStore.SecretKeyEntry(mySecretKey);
    KeyStore.ProtectionParameter protectionParameter = new KeyStore.PasswordProtection(password);
    ks.setEntry(keyStoreName, skEntry, protectionParameter);
    return ks;
  }
  /**
   * For jdbc source items we prepare the boolean property list from model
   * to compare with the source properties and check for 1 and 0 values to convert it to boolean value
   * @param properties {@link ListMetadataProperty} list
   */
  public static List<String> getBooleanParams(final List<ListMetadataProperty> properties) {
    if (properties == null) {
      return new ArrayList<>(0);
    }
    final List<String> params = new ArrayList<>();
    for (final ListMetadataProperty listMetadataProperty : properties) {
      if (listMetadataProperty.getFormatMetadata().get(Constants.DATA_TYPE)
          .equalsIgnoreCase(Constants.BOOLEAN)) {
        params.add(listMetadataProperty.getName());
      }
    }
    return params;
  }

  public static String getPropertiesFromClassPathPomProperties(final String property, final String defaultValue){
    Properties prop = new Properties();

    try (InputStreamReader stream = new InputStreamReader(Objects.requireNonNull(Utils.class.getClassLoader()
        .getResourceAsStream("properties-from-pom.properties")))) {
      prop.load(stream);
      return Objects.requireNonNull(prop.getProperty(property));
    } catch (RuntimeException | IOException e) {
      LOG.warn("Could not read property: {}. Returning Default Value: {}",  property, defaultValue);
      return defaultValue;
    }
  }
}
