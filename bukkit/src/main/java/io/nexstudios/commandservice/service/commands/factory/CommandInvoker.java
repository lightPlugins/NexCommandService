package io.nexstudios.commandservice.service.commands.factory;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.nexstudios.commandservice.service.commands.annotations.Arg;
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
      if (a == null) {
        throw new IllegalStateException("Missing @Arg on parameter " + p.getName() + " in " + method);
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
}