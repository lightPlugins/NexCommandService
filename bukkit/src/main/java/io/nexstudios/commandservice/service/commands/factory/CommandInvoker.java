package io.nexstudios.commandservice.service.commands.factory;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.nexstudios.commandservice.service.commands.annotations.Arg;
import io.nexstudios.commandservice.service.commands.annotations.OptionalArg;
import io.nexstudios.commandservice.service.commands.source.DefaultNexPaperCommandSource;
import io.nexstudios.commandservice.service.commands.source.NexPaperCommandSource;
import io.nexstudios.commandservice.service.commands.util.DurationParsing;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.Duration;
import java.util.Locale;

final class CommandInvoker {

  private CommandInvoker() {}

  static int invoke(Object handler, Method method, CommandContext<CommandSourceStack> ctx) {
    try {
      Object[] args = buildInvokeArgs(method, ctx);
      Object r = method.invoke(handler, args);
      return (r instanceof Integer i) ? i : 1;
    } catch (Exception e) {
      throw new IllegalStateException("Command execution failed: " + method, e);
    }
  }

  private static Object[] buildInvokeArgs(Method method, CommandContext<CommandSourceStack> ctx) {
    Parameter[] params = method.getParameters();
    Object[] out = new Object[params.length];

    for (int i = 0; i < params.length; i++) {
      Parameter p = params[i];
      Class<?> t = p.getType();

      if (t.equals(CommandContext.class)) {
        out[i] = ctx;
        continue;
      }

      if (t.equals(CommandSourceStack.class)) {
        out[i] = ctx.getSource();
        continue;
      }

      if (t.equals(NexPaperCommandSource.class)) {
        out[i] = new DefaultNexPaperCommandSource(ctx.getSource());
        continue;
      }

      Arg a = p.getAnnotation(Arg.class);
      OptionalArg oa = p.getAnnotation(OptionalArg.class);

      if (a == null && oa == null) {
        throw new IllegalStateException("Missing @Arg or @OptionalArg on parameter " + p.getName() + " in " + method);
      }

      if (oa != null) {
        out[i] = resolveOptionalArg(ctx, oa, t);
        continue;
      }

      String name = a.value();

      if (t.equals(String.class)) {
        out[i] = StringArgumentType.getString(ctx, name);
        continue;
      }
      if (t.equals(int.class) || t.equals(Integer.class)) {
        out[i] = IntegerArgumentType.getInteger(ctx, name);
        continue;
      }
      if (t.equals(double.class) || t.equals(Double.class)) {
        out[i] = DoubleArgumentType.getDouble(ctx, name);
        continue;
      }
      if (t.equals(boolean.class) || t.equals(Boolean.class)) {
        out[i] = BoolArgumentType.getBool(ctx, name);
        continue;
      }

      if (t.equals(Player.class)) {
        String playerName = StringArgumentType.getString(ctx, name);
        Player player = Bukkit.getPlayerExact(playerName);
        if (player == null) {
          throw new IllegalStateException("Player not found: " + playerName);
        }
        out[i] = player;
        continue;
      }

      if (t.isEnum()) {
        String raw = StringArgumentType.getString(ctx, name);
        out[i] = parseEnumOrThrow(t, name, raw);
        continue;
      }

      if (t.equals(Duration.class)) {
        String raw = StringArgumentType.getString(ctx, name);
        out[i] = DurationParsing.parse(raw);
        continue;
      }

      throw new IllegalStateException("Unsupported @Arg parameter type in " + method + ": " + t.getName());
    }

    return out;
  }

  private static Enum<?> parseEnumOrThrow(Class<?> enumType, String argName, String raw) {
    String normalized = raw.trim();
    if (normalized.isEmpty()) {
      throw new IllegalStateException("Invalid empty value for " + argName + ": " + raw);
    }

    String upper = normalized.toUpperCase(Locale.ROOT);

    try {
      return enumValueOfUnchecked(enumType, upper);
    } catch (IllegalArgumentException ex) {
      throw new IllegalStateException("Invalid value for " + argName + ": " + raw);
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static Enum<?> enumValueOfUnchecked(Class<?> enumType, String name) {
    Class<? extends Enum> c = enumType.asSubclass(Enum.class);
    return Enum.valueOf(c, name);
  }

  // ── optional arg resolution ─────────────────────────────────────────────

  private static Object resolveOptionalArg(CommandContext<CommandSourceStack> ctx, OptionalArg oa, Class<?> t) {
    String argName = oa.value();
    String explicitDefault = oa.defaultValue();

    try {
      // Try to read the value – succeeds when the optional arg was provided
      return readArgFromCtx(ctx, argName, t);
    } catch (IllegalArgumentException ignored) {
      // Arg was not in the context (command executed without optional arg)
      return parseDefaultValue(t, argName, explicitDefault);
    }
  }

  private static Object readArgFromCtx(CommandContext<CommandSourceStack> ctx, String name, Class<?> t) {
    if (t == String.class)                               return StringArgumentType.getString(ctx, name);
    if (t == int.class || t == Integer.class)            return IntegerArgumentType.getInteger(ctx, name);
    if (t == double.class || t == Double.class)          return DoubleArgumentType.getDouble(ctx, name);
    if (t == boolean.class || t == Boolean.class)        return BoolArgumentType.getBool(ctx, name);
    if (t == Player.class) {
      String playerName = StringArgumentType.getString(ctx, name);
      Player player = Bukkit.getPlayerExact(playerName);
      if (player == null) throw new IllegalStateException("Player not found: " + playerName);
      return player;
    }
    if (t.isEnum()) {
      String raw = StringArgumentType.getString(ctx, name);
      return parseEnumOrThrow(t, name, raw);
    }
    if (t == Duration.class) {
      String raw = StringArgumentType.getString(ctx, name);
      return DurationParsing.parse(raw);
    }
    throw new IllegalStateException("Unsupported @OptionalArg parameter type: " + t.getName());
  }

  private static Object parseDefaultValue(Class<?> t, String argName, String explicitDefault) {
    boolean hasExplicit = explicitDefault != null && !explicitDefault.isEmpty();

    if (t == boolean.class || t == Boolean.class)  return hasExplicit ? Boolean.parseBoolean(explicitDefault) : false;
    if (t == int.class     || t == Integer.class)   return hasExplicit ? Integer.parseInt(explicitDefault) : 0;
    if (t == double.class  || t == Double.class)    return hasExplicit ? Double.parseDouble(explicitDefault) : 0.0d;
    if (t == String.class)                          return hasExplicit ? explicitDefault : null;
    if (t == Duration.class)                        return hasExplicit ? DurationParsing.parse(explicitDefault) : null;
    if (t.isEnum()) {
      if (!hasExplicit) return null;
      return parseEnumOrThrow(t, argName, explicitDefault);
    }
    // For complex types (Player etc.) just return null when omitted
    return null;
  }
}