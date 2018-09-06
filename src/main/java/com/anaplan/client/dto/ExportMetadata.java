package com.anaplan.client.dto;

import com.anaplan.client.DataType;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 6/22/17
 * Time: 2:26 AM
 */
public class ExportMetadata {
    private int columnCount;
    private int rowCount;
    private String exportFormat;
    private String separator;
    private String encoding;
    private String delimiter;
    private DataType[] dataTypes;
    private String[] headerNames;
    private String[] listNames;

    public int getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public String getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat(String exportFormat) {
        this.exportFormat = exportFormat;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public DataType[] getDataTypes() {
        return dataTypes;
    }

    public void setDataTypes(DataType[] dataTypes) {
        this.dataTypes = dataTypes;
    }

    public String[] getHeaderNames() {
        return headerNames;
    }

    public void setHeaderNames(String[] headerNames) {
        this.headerNames = headerNames;
    }

    public String[] getListNames() {
        return listNames;
    }

    public void setListNames(String[] listNames) {
        this.listNames = listNames;
    }
}
