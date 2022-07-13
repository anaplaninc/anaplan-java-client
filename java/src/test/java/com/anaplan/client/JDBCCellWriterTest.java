package com.anaplan.client;


import com.anaplan.client.ex.AnaplanAPIException;
import com.anaplan.client.jdbc.JDBCCellWriter;
import com.anaplan.client.jdbc.JDBCConfig;
import com.opencsv.CSVParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Properties;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class JDBCCellWriterTest extends BaseTest {

    private static final String testJdbcQueryProperties = "/test-jdbc-query-exports.properties";
    private JDBCCellWriter jdbcCellWriter;
    private JDBCConfig jdbcConfig;


    private JDBCConfig loadConfig() throws IOException {
        Properties jdbcProperties = new Properties();
        try {
            jdbcProperties.load(JDBCCellWriter.class.getResourceAsStream(testJdbcQueryProperties));
        } catch (IOException e) {
            throw new RuntimeException("Cannot find test properties!", e);
        }
        JDBCConfig jdbcConfig = new JDBCConfig();
        jdbcConfig.setJdbcConnectionUrl(jdbcProperties.getProperty("jdbc.connect.url"));
        jdbcConfig.setJdbcQuery(jdbcProperties.getProperty("jdbc.query"));
        jdbcConfig.setJdbcParams(new CSVParser().parseLine(jdbcProperties.getProperty("jdbc.params")));

        return jdbcConfig;
    }

    private Method getPrivateMethod(String methodName, Class... argClasses) throws NoSuchMethodException {
        Method method = JDBCCellWriter.class.getDeclaredMethod(methodName, argClasses);
        method.setAccessible(true);
        return method;
    }

    @Before
    public void setUp() throws SQLException, IOException {
        jdbcConfig = loadConfig();
        jdbcCellWriter = new JDBCCellWriter(jdbcConfig);
    }

    @After
    public void tearDown() {
        jdbcCellWriter.close();
    }

    @Test
    public void testSqlInsertQuerySingleRow() throws AnaplanAPIException, IOException, SQLException {
        int[] mapcols = {0, 0, 0};
        InputStream inputStream = new FileInputStream("src/test/resources/files/chunk_1row.txt");
        assertEquals(1, jdbcCellWriter.writeDataRow("exportId", 5, 10, inputStream, 1, "0", mapcols, 3, ","));
    }

    @Test
    public void testSqlInsertQueryFourRows() throws AnaplanAPIException, IOException, SQLException {
        int[] mapcols = {0, 0, 0};
        InputStream inputStream = new FileInputStream("src/test/resources/files/chunk_2rows.txt");
        assertEquals(4, jdbcCellWriter.writeDataRow("exportId", 5, 10, inputStream, 1, "0", mapcols, 3, ","));
    }

    @Test
    public void testCorruptedRecordsException() throws AnaplanAPIException, IOException, SQLException {
        int[] mapcols = {0, 0, 0, 11111};
        InputStream inputStream = new FileInputStream("src/test/resources/files/chunk_3rows.txt");
        assertEquals(4, jdbcCellWriter.writeDataRow("exportId", 5, 10, inputStream, 1, "0", mapcols, 3, ","));
    }

    @Test
    public void testGoodSanitizeQuery() throws Exception {
        String query = jdbcConfig.getJdbcQuery();
        Method sanitizeQueryMethod = getPrivateMethod("sanitizeQuery", String.class);
        assertEquals(query, sanitizeQueryMethod.invoke(jdbcCellWriter, query));
    }

    @Test(expected = RuntimeException.class)
    public void testBadSanitizeQuery() throws Throwable {
        String veryLongQuery = new String(new char[2000]).replace("\0", jdbcConfig.getJdbcQuery() + ";");
        assertTrue(veryLongQuery.length() >= 65535);
        Method sanitizeQueryMethod = getPrivateMethod("sanitizeQuery", String.class);
        try {
            sanitizeQueryMethod.invoke(jdbcCellWriter, veryLongQuery);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

}

