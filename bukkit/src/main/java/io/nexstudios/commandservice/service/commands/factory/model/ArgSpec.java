package io.nexstudios.commandservice.service.commands.factory.model;

import io.nexstudios.commandservice.service.commands.factory.suggest.SuggestionProvider;

public final class ArgSpec {
  public final Class<?> type;
  public final boolean greedy;
  public final boolean optional;
  public final Class<? extends SuggestionProvider> suggestClass;

  public ArgSpec(Class<?> type, boolean greedy, boolean optional, Class<? extends SuggestionProvider> suggestClass) {
    this.type = type;
    this.greedy = greedy;
    this.optional = optional;
    this.suggestClass = suggestClass;
  }
}