package io.nexstudios.commandservice.service.commands.factory.args;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.nexstudios.commandservice.service.commands.factory.model.ArgSpec;
import io.nexstudios.commandservice.service.commands.factory.model.CmdNode;
import io.nexstudios.commandservice.service.commands.factory.suggest.SuggestionProvider;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Locale;
import java.util.Objects;

import static io.papermc.paper.command.brigadier.Commands.argument;

public final class ArgTypeSupport {

  private ArgTypeSupport() {}

  public static ArgumentBuilder<CommandSourceStack, ?> buildArgumentBuilder(ServiceAccessor services, CmdNode node) {
    Objects.requireNonNull(services, "services");

    ArgSpec spec = Objects.requireNonNull(node.argSpec, "argSpec");
    SuggestionProvider provider = (spec.suggestClass == null) ? null : services.create(spec.suggestClass);

    if (spec.type == String.class) {
      RequiredArgumentBuilder<CommandSourceStack, String> a = argument(
          node.name,
          spec.greedy ? StringArgumentType.greedyString() : StringArgumentType.word()
      );
      if (provider != null) a = a.suggests(provider::suggest);
      return a;
    }

    if (spec.type == int.class || spec.type == Integer.class) {
      RequiredArgumentBuilder<CommandSourceStack, Integer> a = argument(node.name, IntegerArgumentType.integer());
      if (provider != null) a = a.suggests(provider::suggest);
      return a;
    }

    if (spec.type == boolean.class || spec.type == Boolean.class) {
      RequiredArgumentBuilder<CommandSourceStack, Boolean> a = argument(node.name, BoolArgumentType.bool());
      if (provider != null) a = a.suggests(provider::suggest);
      return a;
    }

    if (spec.type == double.class || spec.type == Double.class) {
      RequiredArgumentBuilder<CommandSourceStack, Double> a = argument(node.name, DoubleArgumentType.doubleArg());
      if (provider != null) a = a.suggests(provider::suggest);
      return a;
    }

    if (spec.type == Player.class) {
      RequiredArgumentBuilder<CommandSourceStack, String> a = argument(node.name, StringArgumentType.word());
      if (provider != null) a = a.suggests(provider::suggest);
      return a;
    }

    if (spec.type.isEnum()) {
      @SuppressWarnings("unchecked")
      Class<? extends Enum<?>> enumType = (Class<? extends Enum<?>>) spec.type;

      RequiredArgumentBuilder<CommandSourceStack, String> a = argument(node.name, StringArgumentType.word());

      if (provider != null) {
        a = a.suggests(provider::suggest);
      } else {
        a = a.suggests((ctx, builder) -> {
          String remaining = builder.getRemainingLowerCase();
          for (Enum<?> c : enumType.getEnumConstants()) {
            String s = c.name().toLowerCase(Locale.ROOT);
            if (remaining.isEmpty() || s.startsWith(remaining)) {
              builder.suggest(s);
            }
          }
          return builder.buildFuture();
        });
      }

      return a;
    }

    if (spec.type == Duration.class) {
      RequiredArgumentBuilder<CommandSourceStack, String> a = argument(
          node.name,
          spec.greedy ? StringArgumentType.greedyString() : StringArgumentType.word()
      );
      if (provider != null) a = a.suggests(provider::suggest);
      return a;
    }

    throw new IllegalStateException("Unsupported arg type: " + spec.type.getName());
  }
}