package io.nexstudios.commandservice;

import io.nexstudios.commandservice.service.commands.CommandService;
import io.nexstudios.commandservice.service.commands.DefaultCommandService;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import io.nexstudios.serviceregistry.di.ServiceModule;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

public class CommandServiceModule implements ServiceModule {

  private final Plugin plugin;

  public CommandServiceModule(Plugin plugin) {
    this.plugin = Objects.requireNonNull(plugin, "plugin");
  }

  @Override
  public void install(ServiceAccessor serviceAccessor) {
    Objects.requireNonNull(serviceAccessor, "serviceAccessor");

    CommandService commandService = new DefaultCommandService(serviceAccessor);
    commandService.bindToLifecycle(plugin);

    serviceAccessor.register(CommandService.class, commandService);
  }
}