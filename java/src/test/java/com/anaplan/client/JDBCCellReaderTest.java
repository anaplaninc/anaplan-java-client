package com.anaplan.client;


import com.anaplan.client.ex.AnaplanAPIException;
import com.anaplan.client.jdbc.JDBCCellReader;
import com.anaplan.client.jdbc.JDBCConfig;
import com.opencsv.CSVParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Properties;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class JDBCCellReaderTest extends BaseTest {

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
        jdbcConfig.setStoredProcedure(Boolean.valueOf(jdbcProperties.getProperty("jdbc.isStoredProcedure")));
        jdbcConfig.setJdbcQuery(jdbcProperties.getProperty("jdbc.query"));
        jdbcConfig.setJdbcParams(new CSVParser().parseLine(jdbcProperties.getProperty("jdbc.params")));

        return jdbcConfig;
    }

    private Method getPrivateMethod(String methodName, Class... argClasses) throws NoSuchMethodException {
        Method method = JDBCCellReader.class.getDeclaredMethod(methodName, argClasses);
        method.setAccessible(true);
        return method;
    }

    @Before
    public void setUp() throws SQLException, IOException {
        jdbcConfig = loadConfig();
        jdbcCellReader = new JDBCCellReader(jdbcConfig);
        jdbcCellReader.connectAndExecute();
    }

    @After
    public void tearDown() {
        jdbcCellReader.close();
    }

    @Test
    public void testDefaultSqlSelectQuery() throws AnaplanAPIException {
        assertEquals(jdbcCellReader.getHeaderRow(), new String[]{"COLA", "COLB", "COLC"});
        assertEquals(jdbcCellReader.readDataRow(), new String[]{"C$A&,Z(*yV@lue", "123", "W@K!A"});
        assertEquals(jdbcCellReader.readDataRow(), null);
    }

    @Test
    public void testStoredProcedureCallQuery() throws Exception {
        jdbcConfig.setStoredProcedure(true);
        jdbcConfig.setJdbcQuery("call testreverse(?)");
        jdbcConfig.setJdbcParams(new String[]{"test"});

        jdbcCellReader.close();
        jdbcCellReader = new JDBCCellReader(jdbcConfig)
                .connectAndExecute();
        assertEquals(jdbcCellReader.readDataRow(), new String[]{"tset"});
    }

    @Test
    public void testGoodSanitizeQuery() throws Exception {
        String query = jdbcConfig.getJdbcQuery();
        Method sanitizeQueryMethod = getPrivateMethod("sanitizeQuery", String.class);
        assertEquals(query, sanitizeQueryMethod.invoke(jdbcCellReader, query));
    }

    @Test(expected = RuntimeException.class)
    public void testBadSanitizeQuery() throws Throwable {
        String veryLongQuery = new String(new char[1000]).replace("\0", jdbcConfig.getJdbcQuery() + ";");
        assertTrue(veryLongQuery.length() >= 65535);
        Method sanitizeQueryMethod = getPrivateMethod("sanitizeQuery", String.class);
        try {
            sanitizeQueryMethod.invoke(jdbcCellReader, veryLongQuery);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

}
