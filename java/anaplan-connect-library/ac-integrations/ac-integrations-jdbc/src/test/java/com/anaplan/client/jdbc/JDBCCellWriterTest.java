package com.anaplan.client.jdbc;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.anaplan.client.exceptions.AnaplanAPIException;
import com.opencsv.CSVParser;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Properties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JDBCCellWriterTest {

  private static final String testJdbcQueryProperties = "/test-jdbc-query-exports.properties";
  private JDBCCellWriter jdbcCellWriter;
  private JDBCConfig jdbcConfig;


  private JDBCConfig loadConfig() throws IOException {
    Properties jdbcProperties = new Properties();
    jdbcProperties.forEach((p, v) -> System.out.println(p +  ": " +  v));
    try {
      jdbcProperties.load(JDBCCellWriter.class.getResourceAsStream(testJdbcQueryProperties));
    } catch (IOException e) {
      throw new RuntimeException("Cannot find test properties!", e);
    }
    JDBCConfig jdbcConfig = new JDBCConfig();
    jdbcConfig.setJdbcConnectionUrl(jdbcProperties.getProperty("jdbc.connect.url"));
    jdbcConfig.setJdbcQuery(jdbcProperties.getProperty("jdbc.query"));
    jdbcConfig.setJdbcParams(new CSVParser().parseLine(jdbcProperties.getProperty("jdbc.params")));

    jdbcConfig.setJdbcUsername(jdbcProperties.getProperty("jdbc.username"));
    jdbcConfig.setJdbcPassword(jdbcProperties.getProperty("jdbc.password").toCharArray());
    return jdbcConfig;
  }

  private Method getPrivateMethod(String methodName, Class... argClasses)
      throws NoSuchMethodException {
    Method method = JDBCCellWriter.class.getDeclaredMethod(methodName, argClasses);
    method.setAccessible(true);
    return method;
  }

  @BeforeEach
  public void setUp() throws SQLException, IOException {
    jdbcConfig = loadConfig();
    jdbcCellWriter = new JDBCCellWriter(jdbcConfig);
  }

  @AfterEach
  public void tearDown() {
    jdbcCellWriter.close();
  }

  @Test
  public void testSqlInsertQuerySingleRow() throws AnaplanAPIException, IOException, SQLException {
    int[] mapcols = {0, 0, 0};
    InputStream inputStream = new FileInputStream("src/test/resources/files/chunk_1row.txt");
    assertEquals(1,
        jdbcCellWriter.writeDataRow("exportId", 5, 10, inputStream, 1, "0", mapcols, 3, ","));
  }

  @Test
  public void testSqlInsertQueryFourRows() throws AnaplanAPIException, IOException, SQLException {
    int[] mapcols = {0, 0, 0};
    InputStream inputStream = new FileInputStream("src/test/resources/files/chunk_2rows.txt");
    assertEquals(4,
        jdbcCellWriter.writeDataRow("exportId", 5, 10, inputStream, 1, "0", mapcols, 3, ","));
  }

  @Test
  public void testCorruptedRecordsException()
      throws AnaplanAPIException, IOException, SQLException {
    int[] mapcols = {0, 0, 0, 11111};
    InputStream inputStream = new FileInputStream("src/test/resources/files/chunk_3rows.txt");
    assertEquals(4,
        jdbcCellWriter.writeDataRow("exportId", 5, 10, inputStream, 1, "0", mapcols, 3, ","));
  }

  @Test
  public void testGoodSanitizeQuery() throws Exception {
    String query = jdbcConfig.getJdbcQuery();
    Method sanitizeQueryMethod = getPrivateMethod("sanitizeQuery", String.class);
    assertEquals(query, sanitizeQueryMethod.invoke(jdbcCellWriter, query));
  }

  @Test
  public void testBadSanitizeQuery() throws Throwable {
    String veryLongQuery = new String(new char[2000])
        .replace("\0", jdbcConfig.getJdbcQuery() + ";");
    assertTrue(veryLongQuery.length() >= 65535);
    Method sanitizeQueryMethod = getPrivateMethod("sanitizeQuery", String.class);
    assertThrows(InvocationTargetException.class, () -> sanitizeQueryMethod.invoke(jdbcCellWriter, veryLongQuery));
  }

  @Test
  public void testInvalidValueInExportData()
      throws FileNotFoundException, SQLException {
    int[] mapcols = {0, 0, 0};
    InputStream inputStream = new FileInputStream("src/test/resources/files/chunk_invalid_value.txt");
    assertEquals(2,
      jdbcCellWriter.writeDataRow("exportId", 5, 10, inputStream, 1, "0", mapcols, 3, ","));
  }
}
