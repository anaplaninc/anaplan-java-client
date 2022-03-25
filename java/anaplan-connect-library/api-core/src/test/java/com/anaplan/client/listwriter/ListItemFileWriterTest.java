package com.anaplan.client.listwriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ListItemFileWriterTest {

  @Test
  public void escapeCsv(){
    assertEquals("\"abc\"\"def,ghi\"",ListItemFileWriter.escapeCsv("abc\"def,ghi"));
  }

}