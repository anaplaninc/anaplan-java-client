package com.anaplan.client.jdbc;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.anaplan.client.exceptions.AnaplanAPIException;
import com.opencsv.CSVParser;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Properties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JDBCCellReaderTest {

  private static final String testJdbcQueryProperties = "/test-jdbc-query-imports.properties";
  private JDBCCellReader jdbcCellReader;
  private JDBCConfig jdbcConfig;

  public JDBCConfig loadConfig() throws IOException {
    Properties jdbcProperties = new Properties();
    try {
      jdbcProperties.load(JDBCCellReader.class.getResourceAsStream(testJdbcQueryProperties));
    } catch (IOException e) {
      throw new RuntimeException("Cannot find test properties!", e);
    }
    JDBCConfig jdbcConfig = new JDBCConfig();
    jdbcConfig.setJdbcConnectionUrl(jdbcProperties.getProperty("jdbc.connect.url"));
    jdbcConfig.setJdbcFetchSize(Integer.parseInt(jdbcProperties.getProperty("jdbc.fetch.size")));
    jdbcConfig
        .setStoredProcedure(Boolean.parseBoolean(jdbcProperties.getProperty("jdbc.isStoredProcedure")));
    jdbcConfig.setJdbcQuery(jdbcProperties.getProperty("jdbc.query"));
    jdbcConfig.setJdbcParams(new CSVParser().parseLine(jdbcProperties.getProperty("jdbc.params")));

    jdbcConfig.setJdbcUsername(jdbcProperties.getProperty("jdbc.username"));
    jdbcConfig.setJdbcPassword(jdbcProperties.getProperty("jdbc.password").toCharArray());
    return jdbcConfig;
  }

  private Method getPrivateMethod(String methodName, Class... argClasses)
      throws NoSuchMethodException {
    Method method = JDBCCellReader.class.getDeclaredMethod(methodName, argClasses);
    method.setAccessible(true);
    return method;
  }

  @BeforeEach
  public void setUp() throws SQLException, IOException {
    jdbcConfig = loadConfig();
    jdbcCellReader = new JDBCCellReader(jdbcConfig);
    jdbcCellReader.connectAndExecute();
  }

  @AfterEach
  public void tearDown() {
    jdbcCellReader.close();
  }

  @Test
  void testDefaultSqlSelectQuery() throws AnaplanAPIException {
    assertThat(jdbcCellReader.getHeaderRow(), arrayContaining("COLA", "COLB", "COLC"));
    assertThat(jdbcCellReader.readDataRow(), arrayContaining("C$A&,Z(*yV@lue", "123", "W@K!A"));
    assertEquals(0, jdbcCellReader.readDataRow().length);
  }

  @Test
  void testStoredProcedureCallQuery() throws Exception {
    jdbcConfig.setStoredProcedure(true);
    jdbcConfig.setJdbcQuery("call testreverse(?)");
    jdbcConfig.setJdbcParams(new String[]{"test"});

    jdbcCellReader.close();
    jdbcCellReader = new JDBCCellReader(jdbcConfig)
        .connectAndExecute();
    assertThat(jdbcCellReader.readDataRow(), arrayContaining("tset"));
  }

  @Test
  void testGoodSanitizeQuery() throws Exception {
    String query = jdbcConfig.getJdbcQuery();
    Method sanitizeQueryMethod = getPrivateMethod("sanitizeQuery", String.class);
    assertEquals(query, sanitizeQueryMethod.invoke(jdbcCellReader, query));
  }

  @Test
  void testBadSanitizeQuery() throws Throwable {
    String veryLongQuery = new String(new char[1000])
        .replace("\0", jdbcConfig.getJdbcQuery() + ";");
    assertTrue(veryLongQuery.length() >= 65535);
    Method sanitizeQueryMethod = getPrivateMethod("sanitizeQuery", String.class);
    assertThrows(InvocationTargetException.class, () -> sanitizeQueryMethod.invoke(jdbcCellReader, veryLongQuery));
  }

}
