package com.anaplan.client;

import static com.anaplan.client.Strings.lenientFormat;

public class Preconditions {

  private Preconditions(){}

  /**
   * Ensures that an object reference passed as a parameter to the calling method is not null.
   *
   * @param reference an object reference
   * @return the non-null reference that was validated
   * @throws NullPointerException if {@code reference} is null
   */
  public static <T> T checkNotNull(T reference) {
    if (reference == null) {
      throw new NullPointerException();
    }
    return reference;
  }

  /**
   * Ensures the truth of an expression involving one or more parameters to the calling method.
   */
  public static void checkArgument(boolean b, String errorMessageTemplate, int p1) {
    if (!b) {
      throw new IllegalArgumentException(lenientFormat(errorMessageTemplate, p1));
    }
  }

  /**
   * Ensures that an object reference passed as a parameter to the calling method is not null.
   *
   * @param reference    an object reference
   * @param errorMessage the exception message to use if the check fails; will be converted to a
   *                     string using {@link String#valueOf(Object)}
   * @return the non-null reference that was validated
   * @throws NullPointerException if {@code reference} is null
   */
  public static <T> T checkNotNull(T reference, Object errorMessage) {
    if (reference == null) {
      throw new NullPointerException(String.valueOf(errorMessage));
    }
    return reference;
  }


}

