package io.nexstudios.commandservice.service.commands.util;

import java.time.Duration;
import java.util.Locale;

public final class DurationParsing {

  private DurationParsing() {}

  public static Duration parse(String input) {
    if (input == null) throw new IllegalArgumentException("input");
    String s = input.trim();
    if (s.isEmpty()) throw new IllegalArgumentException("Invalid duration: empty");

    // ISO-8601 support, e.g. PT15M
    if (s.startsWith("P") || s.startsWith("p")) {
      return Duration.parse(s.toUpperCase(Locale.ROOT));
    }

    // Simple suffix format: 10s, 5m, 2h, 1d
    long totalSeconds = 0L;

    int i = 0;
    while (i < s.length()) {
      while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
      if (i >= s.length()) break;

      int start = i;
      while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
      if (start == i) throw new IllegalArgumentException("Invalid duration token in: " + input);

      long value = Long.parseLong(s.substring(start, i));

      if (i >= s.length()) throw new IllegalArgumentException("Missing unit in: " + input);
      char unit = Character.toLowerCase(s.charAt(i));
      i++;

      long seconds = switch (unit) {
        case 's' -> value;
        case 'm' -> value * 60L;
        case 'h' -> value * 3600L;
        case 'd' -> value * 86400L;
        default -> throw new IllegalArgumentException("Unknown unit '" + unit + "' in: " + input);
      };

      totalSeconds = Math.addExact(totalSeconds, seconds);
    }

    return Duration.ofSeconds(totalSeconds);
  }
}