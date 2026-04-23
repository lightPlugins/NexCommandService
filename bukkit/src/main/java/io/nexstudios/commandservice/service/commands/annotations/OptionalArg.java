package io.nexstudios.commandservice.service.commands.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;

/**
 * Marks a method parameter as an optional command argument.
 * <p>
 * Use {@code [argname]} syntax in the {@link Command} path to declare the arg as optional.
 * If the player omits the argument, {@code defaultValue} is used instead.
 * <p>
 * Example:
 * <pre>
 * {@code @Command("add <name> <amount> [silent]")}
 * public void add(NexPaperCommandSource src,
 *                 {@code @Arg("name")} String name,
 *                 {@code @Arg("amount")} int amount,
 *                 {@code @OptionalArg("silent")} boolean silent) { ... }
 * </pre>
 * For {@code boolean} the implicit default is {@code false}, for numeric types {@code 0},
 * for {@code String} an empty string. Supply {@link #defaultValue()} to override.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(PARAMETER)
public @interface OptionalArg {
  /** The argument name – must match the {@code [argname]} token in the command path. */
  String value();

  /**
   * String representation of the default value used when the argument is omitted.
   * If left empty, the type's natural zero/false/empty default is used.
   */
  String defaultValue() default "";
}

