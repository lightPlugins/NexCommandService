package io.nexstudios.commandservice.service.commands;

import io.nexstudios.commandservice.service.commands.annotations.CommandRoot;
import io.nexstudios.commandservice.service.commands.factory.BrigadierCommandRegistrar;
import io.nexstudios.commandservice.service.commands.factory.CommandModel;
import io.nexstudios.commandservice.service.commands.factory.CommandModelBuilder;
import io.nexstudios.commandservice.service.commands.factory.model.CmdNode;
import io.nexstudios.commandservice.service.commands.factory.util.CommandUtils;
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

  private final ConcurrentHashMap<String, RootAggregate> roots = new ConcurrentHashMap<>();

  private record RootAggregate(CommandRoot rootAnn, CmdNode root) {}

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
    CommandRoot rootAnn = handler.getClass().getAnnotation(CommandRoot.class);
    if (rootAnn == null) {
      throw new IllegalStateException("Missing @CommandRoot on " + handler.getClass().getName());
    }

    RootAggregate agg = roots.compute(rootAnn.name(), (name, existing) -> {
      if (existing == null) {
        CmdNode root = CmdNode.root(rootAnn.name());
        root.permission = CommandUtils.normalizePerm(rootAnn.permission());
        root.playerOnly = rootAnn.playerOnly();

        CommandModelBuilder.buildInto(handler, root, rootAnn);
        return new RootAggregate(rootAnn, root);
      }

      assertCompatibleRoot(existing.rootAnn(), rootAnn);
      CommandModelBuilder.buildInto(handler, existing.root(), rootAnn);
      return existing;
    });

    BrigadierCommandRegistrar.register(services, commands, new CommandModel(agg.rootAnn(), agg.root()));
  }

  private static void assertCompatibleRoot(CommandRoot a, CommandRoot b) {
    if (!Objects.equals(a.name(), b.name())) {
      throw new IllegalStateException("Internal error: root names differ while merging: " + a.name() + " vs " + b.name());
    }
    if (!Objects.equals(a.description(), b.description())) {
      throw new IllegalStateException("Conflicting @CommandRoot.description for '" + a.name() + "'");
    }
    if (!Objects.equals(CommandUtils.normalizePerm(a.permission()), CommandUtils.normalizePerm(b.permission()))) {
      throw new IllegalStateException("Conflicting @CommandRoot.permission for '" + a.name() + "'");
    }
    if (a.playerOnly() != b.playerOnly()) {
      throw new IllegalStateException("Conflicting @CommandRoot.playerOnly for '" + a.name() + "'");
    }
    if (!java.util.Arrays.equals(a.aliases(), b.aliases())) {
      throw new IllegalStateException("Conflicting @CommandRoot.aliases for '" + a.name() + "'");
    }
  }
}