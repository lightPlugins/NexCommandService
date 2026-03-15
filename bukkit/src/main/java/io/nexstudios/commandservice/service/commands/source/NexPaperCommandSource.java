package io.nexstudios.commandservice.service.commands.source;

import io.nexstudios.serviceregistry.di.Service;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.Nullable;

public interface NexPaperCommandSource extends Service {

  CommandSender sender();

  @Nullable Entity executor();

  Location location();

  CommandSourceStack paper();
}