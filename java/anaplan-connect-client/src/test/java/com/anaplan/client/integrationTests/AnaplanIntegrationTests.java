// Copyright 2012 Anaplan Limited
/**
 * This file runs integration-tests for Anaplan Connect. It uses a set of YAML based configuration files loaded in
 * src/test/resources/runner folder to execute and validate output of various shell scripts.
 */
package com.anaplan.client.integrationTests;

import com.anaplan.client.integrationTests.helpers.dto.AnaplanTest;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(value = Parameterized.class)
public class AnaplanIntegrationTests extends BaseIntegrationTest {

  private static final Logger LOG = LoggerFactory.getLogger(AnaplanIntegrationTests.class);

  private final AnaplanTest anaplanTest;

  public AnaplanIntegrationTests(AnaplanTest anaplanTest) {
    super(anaplanTest);
    this.anaplanTest = anaplanTest;
  }

  @Parameters(name = "{index} - {0}")
  public static Collection<AnaplanTest> data() {
    final List<String> YAML_FILES = Arrays
        .asList(
            "listviewsmodules.yaml"
        );
    return getTestsFromYAMLFiles(YAML_FILES);
  }

  @Test
  public void runTest() {
    try {
      runIntegrationTest();
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      Assert.fail(anaplanTest.getTestName() + " failed.");
    }
  }

}
