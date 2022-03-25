package com.anaplan.client;

import static com.anaplan.client.Preconditions.checkArgument;
import static com.anaplan.client.Preconditions.checkNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Strings {

  private static final Logger LOG = LoggerFactory.getLogger(Strings.class);

  /**
   * Returns a string consisting of a specific number of concatenated copies of an input string. For
   * example, {@code repeat("hey", 3)} returns the string {@code "heyheyhey"}.
   *
   * @param string any non-null string
   * @param count  the number of times to repeat it; a nonnegative integer
   * @return a string containing {@code string} repeated {@code count} times (the empty string if
   * {@code count} is zero)
   * @throws IllegalArgumentException if {@code count} is negative
   */
  public static String repeat(String string, int count) {
    checkNotNull(string); // eager for GWT.

    if (count <= 1) {
      checkArgument(count >= 0, "invalid count: %s", count);
      return (count == 0) ? "" : string;
    }

    // IF YOU MODIFY THE CODE HERE, you must update StringsRepeatBenchmark
    final int len = string.length();
    final long longSize = (long) len * (long) count;
    final int size = (int) longSize;
    if (size != longSize) {
      throw new ArrayIndexOutOfBoundsException("Required array size too large: " + longSize);
    }

    final char[] array = new char[size];
    string.getChars(0, len, array, 0);
    int n;
    for (n = len; n < size - n; n <<= 1) {
      System.arraycopy(array, 0, array, n, n);
    }
    System.arraycopy(array, 0, array, n, size - n);
    return new String(array);
  }

  /**
   * Returns the given {@code template} string with each occurrence of {@code "%s"} replaced with
   * the corresponding argument value from {@code args}; or, if the placeholder and argument counts
   * do not match, returns a best-effort form of that string. Will not throw an exception under
   * normal conditions.
   *
   * <p><b>Note:</b> For most string-formatting needs, use {@link String#format String.format},
   * {@link java.io.PrintWriter#format PrintWriter.format}, and related methods. These support the
   * full range of <a href="https://docs.oracle.com/javase/9/docs/api/java/util/Formatter.html#syntax">format
   * specifiers</a>, and alert you to usage errors by throwing {@link
   * java.util.IllegalFormatException}.
   *
   * <p>In certain cases, such as outputting debugging information or constructing a message to be
   * used for another unchecked exception, an exception during string formatting would serve little
   * purpose except to supplant the real information you were trying to provide. These are the cases
   * this method is made for; it instead generates a best-effort string with all supplied argument
   * values present. This method is also useful in environments such as GWT where {@code
   * String.format} is not available. As an example, method implementations of the {@link
   * Preconditions} class use this formatter, for both of the reasons just discussed.
   *
   * <p><b>Warning:</b> Only the exact two-character placeholder sequence {@code "%s"} is
   * recognized.
   *
   * @param template a string containing zero or more {@code "%s"} placeholder sequences. {@code
   *                 null} is treated as the four-character string {@code "null"}.
   * @param args     the arguments to be substituted into the message template. The first argument
   *                 specified is substituted for the first occurrence of {@code "%s"} in the
   *                 template, and so forth. A {@code null} argument is converted to the
   *                 four-character string {@code "null"}; non-null values are converted to strings
   *                 using {@link Object#toString()}.
   */
  public static String lenientFormat(
      String template, Object... args) {
    template = String.valueOf(template); // null -> "null"

    if (args == null) {
      args = new Object[]{"(Object[])null"};
    } else {
      for (int i = 0; i < args.length; i++) {
        args[i] = lenientToString(args[i]);
      }
    }

    // start substituting the arguments into the '%s' placeholders
    StringBuilder builder = new StringBuilder(template.length() + 16 * args.length);
    int templateStart = 0;
    int i = 0;
    while (i < args.length) {
      int placeholderStart = template.indexOf("%s", templateStart);
      if (placeholderStart == -1) {
        break;
      }
      builder.append(template, templateStart, placeholderStart);
      builder.append(args[i++]);
      templateStart = placeholderStart + 2;
    }
    builder.append(template, templateStart, template.length());

    // if we run out of placeholders, append the extra args in square braces
    if (i < args.length) {
      builder.append(" [");
      builder.append(args[i++]);
      while (i < args.length) {
        builder.append(", ");
        builder.append(args[i++]);
      }
      builder.append(']');
    }

    return builder.toString();
  }

  private static String lenientToString(Object o) {
    try {
      return String.valueOf(o);
    } catch (Exception e) {
      // Default toString() behavior - see Object.toString()
      String objectToString =
          o.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(o));
      // Logger is created inline with fixed name to avoid forcing Proguard to create another class.
      LOG.warn("Exception during lenientFormat for " + objectToString, e);
      return "<" + objectToString + " threw " + e.getClass().getName() + ">";
    }
  }

}
