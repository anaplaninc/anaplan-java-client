package com.anaplan.client.jdbc;


public class JDBCConfig {

  private String jdbcConnectionUrl;
  private String jdbcUsername;
  private char[] jdbcPassword;
  private Integer jdbcFetchSize;
  private boolean isStoredProcedure;
  private String jdbcQuery;
  private Object[] jdbcParams;

  public String getJdbcConnectionUrl() {
    return jdbcConnectionUrl;
  }

  public String getJdbcUsername() {
    return jdbcUsername;
  }

  public char[] getJdbcPassword() {
    return jdbcPassword;
  }

  public Integer getJdbcFetchSize() {
    return jdbcFetchSize;
  }

  public boolean isStoredProcedure() {
    return isStoredProcedure;
  }

  public String getJdbcQuery() {
    return jdbcQuery;
  }

  public Object[] getJdbcParams() {
    return jdbcParams;
  }

  public void setJdbcConnectionUrl(String jdbcConnectionUrl) {
    this.jdbcConnectionUrl = jdbcConnectionUrl;
  }

  public void setJdbcUsername(String jdbcUsername) {
    this.jdbcUsername = jdbcUsername;
  }

  public void setJdbcPassword(final char[] jdbcPassword) {
    this.jdbcPassword = jdbcPassword == null ? new char[0]:jdbcPassword.clone();
  }

  public void setJdbcFetchSize(Integer jdbcFetchSize) {
    this.jdbcFetchSize = jdbcFetchSize;
  }

  public void setStoredProcedure(boolean storedProcedure) {
    isStoredProcedure = storedProcedure;
  }

  public void setJdbcQuery(String jdbcQuery) {
    this.jdbcQuery = jdbcQuery;
  }

  public void setJdbcParams(Object[] jdbcParams) {
    this.jdbcParams = jdbcParams;
  }
}
