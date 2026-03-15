package io.nexstudios.commandservice.service.commands.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

@Retention(RetentionPolicy.RUNTIME)
@Target(TYPE)
public @interface CommandRoot {
  String name();
  String description() default "";
  String[] aliases() default {};
  String permission() default "";
  boolean playerOnly() default false;
}