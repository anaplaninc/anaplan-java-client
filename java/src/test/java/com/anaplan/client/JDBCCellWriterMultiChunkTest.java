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
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class JDBCCellWriterMultiChunkTest extends BaseTest {

    private static final String testJdbcQueryProperties = "/test-jdbc-query-large-exports.properties";
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
    public void testSqlInsertQueryMultipleRows() throws AnaplanAPIException,IOException,SQLException {
        int[] mapcols = {};
        InputStream inputStream1 = new FileInputStream("src/test/resources/files/xee.txt");
        InputStream inputStream2 = new FileInputStream("src/test/resources/files/xef.txt");
        InputStream inputStream3 = new FileInputStream("src/test/resources/files/xeg.txt");
        InputStream inputStream4 = new FileInputStream("src/test/resources/files/xeh.txt");
        InputStream inputStream5 = new FileInputStream("src/test/resources/files/xei.txt");
        InputStream inputStream6 = new FileInputStream("src/test/resources/files/xej.txt");
        assertEquals(1049,jdbcCellWriter.writeDataRow("exportId",5,10,inputStream1,6,"0",mapcols,12,","));
        assertEquals(1059,jdbcCellWriter.writeDataRow("exportId",5,10,inputStream2,6,"1",mapcols,12,","));
        assertEquals(2159,jdbcCellWriter.writeDataRow("exportId",5,10,inputStream3,6,"2",mapcols,12,","));
        assertEquals(3159,jdbcCellWriter.writeDataRow("exportId",5,10,inputStream4,6,"3",mapcols,12,","));
        assertEquals(4158,jdbcCellWriter.writeDataRow("exportId",5,10,inputStream5,6,"4",mapcols,12,","));
        assertEquals(6158,jdbcCellWriter.writeDataRow("exportId",5,10,inputStream6,6,"5",mapcols,12,","));

    }


}
