package io.nexstudios.commandservice.service.commands.factory.suggest;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class OnlinePlayersSuggestion implements SuggestionProvider {

  @Override
  public CompletableFuture<Suggestions> suggest(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
    String remaining = builder.getRemainingLowerCase();

    for (Player p : Bukkit.getOnlinePlayers()) {
      String name = p.getName();
      if (remaining.isEmpty() || name.toLowerCase(Locale.ROOT).startsWith(remaining)) {
        builder.suggest(name);
      }
    }

    return builder.buildFuture();
  }
}