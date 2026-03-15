package io.nexstudios.commandservice.service.commands;

import io.nexstudios.serviceregistry.di.Service;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The CommandService interface defines the contract for services that manage the registration
 * and lifecycle binding of command handlers. It extends the {@code Service} interface and provides
 * methods for binding commands, binding to plugin lifecycles, and registering handler instances.
 * Implementations of this interface allow for dynamic command handling registration during runtime.
 */
public interface CommandService extends Service {

  /**
   * Binds the provided {@link Commands} instance to this service, enabling the registration
   * of command handlers. This method ensures that any pre-registered handlers are associated
   * with the given {@link Commands} instance.
   *
   * @param commands the {@link Commands} instance to bind to this service
   * @throws NullPointerException if the {@code commands} parameter is {@code null}
   */
  void bind(Commands commands);

  /**
   * Binds the provided {@link Plugin} instance to the lifecycle of this service. This enables
   * the service to register necessary lifecycle event handlers, such as those required for
   * handling command registration during the plugin's lifecycle.
   *
   * @param plugin the {@link Plugin} instance to bind to the lifecycle of this service
   * @throws NullPointerException if the {@code plugin} parameter is {@code null}
   */
  void bindToLifecycle(Plugin plugin);

  /**
   * Registers a handler of the specified type with this service. The handler is created
   * and managed by the {@code ServiceAccessor} and will be available for further use
   * in the service. If commands were already bound to the service, the handler is
   * immediately registered to handle commands.
   *
   * @param handlerType the {@code Class} representing the type of handler to be registered
   * @param <T> the type of the service to be registered, extending {@code Service}
   * @return the instance of the handler that was registered
   * @throws NullPointerException if the {@code handlerType} parameter is {@code null}
   */
  <T extends Service> T register(Class<T> handlerType);

  /**
   * Registers all handler types provided in the specified {@code handlerTypes} iterable with this service.
   * Each handler type is individually registered using the {@code register} method.
   *
   * @param handlerTypes an {@code Iterable} containing {@code Class} objects that represent the handler types
   *                     to be registered. Each handler type must extend {@code Service}.
   * @return a {@code List} of {@code Service} instances representing the handlers that were successfully registered.
   * @throws NullPointerException if {@code handlerTypes} is {@code null}, or if any element within {@code handlerTypes} is {@code null}.
   */
  default List<Service> registerAll(Iterable<? extends Class<? extends Service>> handlerTypes) {
    Objects.requireNonNull(handlerTypes, "handlerTypes");

    List<Service> out = new ArrayList<>();
    for (Class<? extends Service> t : handlerTypes) {
      out.add(register(Objects.requireNonNull(t, "handlerTypes contains null")));
    }
    return out;
  }
}