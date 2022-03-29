package com.anaplan.client.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.anaplan.client.CellWriter.DataRow;
import com.anaplan.client.exceptions.AnaplanAPIException;
import com.opencsv.CSVParser;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Properties;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JDBCWriterParamTest {
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

  @BeforeEach
  public void setUp() throws SQLException, IOException {
    jdbcConfig = loadConfig();
    jdbcCellWriter = new JDBCCellWriter(jdbcConfig);
  }

  @AfterEach
  public void tearDown() {
    jdbcCellWriter.close();
  }

  @ParameterizedTest
  @MethodSource("provider")
  void testSqlInsertQuerySingleRow(String path, int expected, int[] mapcols) throws AnaplanAPIException, IOException, SQLException {
    InputStream inputStream = new FileInputStream(path);
    DataRow dr = new DataRow();
    dr.setMapcols(mapcols);
    dr.setInputStream(inputStream);
    dr.setExportId("exportId");
    dr.setMaxRetryCount(5);
    dr.setRetryTimeout(10);
    dr.setChunks(1);
    dr.setChunkId("0");
    dr.setColumnCount(3);
    dr.setSeparator(",");
    assertEquals(expected,
        jdbcCellWriter.writeDataRow(dr));
  }

  private static Stream<Arguments> provider(){
    return Stream.of(
        Arguments.of("src/test/resources/files/chunk_invalid_value.txt",2, new int[]{0, 0, 0, 0})
    );
  }


}
