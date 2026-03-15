package io.nexstudios.commandservice.service.commands.factory.args;

import io.nexstudios.commandservice.service.commands.annotations.Arg;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public final class ArgParsing {

  private ArgParsing() {}

  public record ArgToken(String name, boolean greedy) {}

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

  public static Parameter findArgParameter(Method method, String argName) {
    for (Parameter p : method.getParameters()) {
      Arg a = p.getAnnotation(Arg.class);
      if (a == null) continue;
      if (!a.value().equals(argName)) continue;
      return p;
    }
    throw new IllegalStateException("Missing @Arg(\"" + argName + "\") parameter in method: " + method);
  }
}