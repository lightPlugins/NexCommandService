package io.nexstudios.commandservice.service.commands.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

@Retention(RetentionPolicy.RUNTIME)
@Target(METHOD)
public @interface Command {
  /**
   * Literal path relative to root, examples:
   * ""            -> root execution
   * "reload"      -> /root reload
   * "debug on"    -> /root debug on
   */
  String value();
  String permission() default "";
  boolean playerOnly() default false;
}