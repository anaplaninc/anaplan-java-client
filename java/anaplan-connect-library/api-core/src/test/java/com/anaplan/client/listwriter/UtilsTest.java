package com.anaplan.client.listwriter;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.anaplan.client.Utils;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class UtilsTest {

  @Test
  void fileAbsoluteTest () throws FileNotFoundException {
    assertTrue((Utils.getAbsolutePath(Paths.get("a.txt")).isAbsolute()));
  }

  @Test
  void fileAbsoluteNullTest () {
    assertThrows(FileNotFoundException.class , () -> Utils.getAbsolutePath(null));
  }
}
