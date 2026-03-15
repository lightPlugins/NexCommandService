package io.nexstudios.commandservice.service.commands;

import io.nexstudios.commandservice.service.commands.factory.BrigadierCommandRegistrar;
import io.nexstudios.commandservice.service.commands.factory.CommandModelBuilder;
import io.nexstudios.serviceregistry.di.Service;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

public final class DefaultCommandService implements CommandService {

  private final ServiceAccessor services;

  private volatile Commands commands;
  private final AtomicLong bindEpoch = new AtomicLong(0L);

  private final List<Object> handlers = new CopyOnWriteArrayList<>();
  private final ConcurrentHashMap<Object, Long> lastRegisteredEpoch = new ConcurrentHashMap<>();

  public DefaultCommandService(ServiceAccessor services) {
    this.services = Objects.requireNonNull(services, "services");
  }

  @Override
  public void bindToLifecycle(Plugin plugin) {
    Objects.requireNonNull(plugin, "plugin");

    plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
      bind(event.registrar());
    });
  }

  @Override
  public void bind(Commands commands) {
    this.commands = Objects.requireNonNull(commands, "commands");
    long epoch = bindEpoch.incrementAndGet();

    for (Object handler : handlers) {
      registerHandlerIfNeeded(handler, epoch);
    }
  }

  @Override
  public <T extends Service> T register(Class<T> handlerType) {
    Objects.requireNonNull(handlerType, "handlerType");

    T handler = services.create(handlerType);
    handlers.add(handler);

    Commands local = this.commands;
    if (local != null) {
      registerHandlerIfNeeded(handler, bindEpoch.get());
    }

    return handler;
  }

  private void registerHandlerIfNeeded(Object handler, long epoch) {
    Long prev = lastRegisteredEpoch.put(handler, epoch);
    if (prev != null && prev == epoch) {
      return;
    }
    registerHandler(handler);
  }

  private void registerHandler(Object handler) {
    var model = CommandModelBuilder.build(handler);
    BrigadierCommandRegistrar.register(services, commands, model);
  }
}