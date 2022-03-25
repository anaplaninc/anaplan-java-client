package com.anaplan.client.integrationTests.helpers.Utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * selects environment specific xml files for testing.
 */
public class PropertyList {

  private final static Logger LOGGER = LoggerFactory.getLogger(PropertyList.class);


  static Properties props;
  static String testEnvironment;

  /* Static block to load all values from the properties.xml file */
  static {

    // check for TestEnvironment System variable, or set as blank string
    testEnvironment = System.getProperty("TestEnvironment");
    testEnvironment = (testEnvironment != null ? testEnvironment : "");

    try {
      props = new Properties();
      LOGGER.info("Environment chosen: " + testEnvironment);
      switch (testEnvironment) {
        case "stg":
          props
              .loadFromXML(new FileInputStream("src/test/resources/properties/stg/properties.xml"));
          break;
        default:
          props
              .loadFromXML(new FileInputStream("src/test/resources/properties/stg/properties.xml"));
          break;
      }


    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Get any property that was stored in properties.xml
   *
   * @param prop key of the property needed
   * @return value set on a property
   */
  public static String getProperty(String prop) {

    String systemPropertyValue = System.getProperty(prop);

    return (systemPropertyValue != null ? systemPropertyValue : props.getProperty(prop));

  }

}
