package com.anaplan.client.dto;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 6/21/17
 * Time: 3:32 PM
 */
public class ServerFileData extends NamedObjectData {

    private int chunkCount;
    private String origin;
    private String format;
    private String language;
    private String country;
    private String encoding;
    private String separator;
    private String delimiter;
    private Integer headerRow;
    private Integer firstDataRow;

    public int getChunkCount() {
        return chunkCount;
    }

    public String getOrigin() {
        return origin;
    }

    public String getFormat() {
        return format;
    }

    public String getLanguage() {
        return language;
    }

    public String getCountry() {
        return country;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getSeparator() {
        return separator;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public Integer getHeaderRow() {
        return headerRow;
    }

    public Integer getFirstDataRow() {
        return firstDataRow;
    }

    public void setChunkCount(int chunkCount) {
        this.chunkCount = chunkCount;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public void setHeaderRow(Integer headerRow) {
        this.headerRow = headerRow;
    }

    public void setFirstDataRow(Integer firstDataRow) {
        this.firstDataRow = firstDataRow;
    }
}
