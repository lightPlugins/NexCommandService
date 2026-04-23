package io.nexstudios.commandservice.service.commands.factory.args;

import io.nexstudios.commandservice.service.commands.annotations.Arg;
import io.nexstudios.commandservice.service.commands.annotations.OptionalArg;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public final class ArgParsing {

  private ArgParsing() {}

  public record ArgToken(String name, boolean greedy) {}

  // ── required arg  <argname> / <argname...> ──────────────────────────────
  public static boolean isArgToken(String token) {
    return token.length() >= 3 && token.startsWith("<") && token.endsWith(">");
  }

  public static ArgToken parseArgToken(String token) {
    String inner = token.substring(1, token.length() - 1).trim();
    boolean greedy = inner.endsWith("...");

    String name = greedy ? inner.substring(0, inner.length() - 3) : inner;
    name = name.trim();

    if (name.isEmpty()) throw new IllegalArgumentException("Invalid arg token: " + token);
    return new ArgToken(name, greedy);
  }

  public static void assertGreedyLast(ArgToken arg, int index, int total, Method m) {
    if (arg.greedy() && index != total - 1) {
      throw new IllegalStateException(
          "Greedy argument <" + arg.name() + "...> must be the last token in @Command path: " + m
      );
    }
  }

  // ── optional arg  [argname] ─────────────────────────────────────────────
  public static boolean isOptionalArgToken(String token) {
    return token.length() >= 3 && token.startsWith("[") && token.endsWith("]");
  }

  public static String parseOptionalArgToken(String token) {
    String name = token.substring(1, token.length() - 1).trim();
    if (name.isEmpty()) throw new IllegalArgumentException("Invalid optional arg token: " + token);
    return name;
  }

  public static void assertOptionalLast(String argName, int index, int total, Method m) {
    if (index != total - 1) {
      throw new IllegalStateException(
          "Optional argument [" + argName + "] must be the last token in @Command path: " + m
      );
    }
  }

  // ── parameter lookup ────────────────────────────────────────────────────
  public static Parameter findArgParameter(Method method, String argName) {
    for (Parameter p : method.getParameters()) {
      Arg a = p.getAnnotation(Arg.class);
      if (a != null && a.value().equals(argName)) return p;
    }
    throw new IllegalStateException("Missing @Arg(\"" + argName + "\") parameter in method: " + method);
  }

  public static Parameter findOptionalArgParameter(Method method, String argName) {
    for (Parameter p : method.getParameters()) {
      OptionalArg oa = p.getAnnotation(OptionalArg.class);
      if (oa != null && oa.value().equals(argName)) return p;
      // Also accept @Arg – the [argname] path syntax alone is enough to mark it optional
      Arg a = p.getAnnotation(Arg.class);
      if (a != null && a.value().equals(argName)) return p;
    }
    throw new IllegalStateException(
        "Missing @Arg(\"" + argName + "\") or @OptionalArg(\"" + argName + "\") parameter in method: " + method);
  }
}