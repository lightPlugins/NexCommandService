package io.nexstudios.commandservice.service.commands.factory.suggest;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.nexstudios.serviceregistry.di.Service;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.concurrent.CompletableFuture;

public interface SuggestionProvider extends Service {
  CompletableFuture<Suggestions> suggest(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder);
}