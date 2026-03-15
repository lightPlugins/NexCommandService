package io.nexstudios.commandservice.service.commands.source;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class DefaultNexPaperCommandSource implements NexPaperCommandSource {

  private final CommandSourceStack source;

  public DefaultNexPaperCommandSource(CommandSourceStack source) {
    this.source = Objects.requireNonNull(source, "source");
  }

  @Override
  public CommandSender sender() {
    return source.getSender();
  }

  @Override
  public @Nullable Entity executor() {
    return source.getExecutor();
  }

  @Override
  public Location location() {
    return source.getLocation();
  }

  @Override
  public CommandSourceStack paper() {
    return source;
  }
}