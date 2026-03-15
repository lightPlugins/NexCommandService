package io.nexstudios.commandservice.service.commands.annotations;

import io.nexstudios.commandservice.service.commands.factory.suggest.SuggestionProvider;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;

@Retention(RetentionPolicy.RUNTIME)
@Target(PARAMETER)
public @interface Suggest {
  Class<? extends SuggestionProvider> value();
}