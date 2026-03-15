package io.nexstudios.commandservice.service.commands.factory;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.nexstudios.commandservice.service.commands.factory.util.CommandUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

final class CommandRequires {

  private CommandRequires() {}

  static <B extends ArgumentBuilder<CommandSourceStack, ?>> B applyMerged(
      B node,
      String basePermission,
      boolean basePlayerOnly,
      String extraPermission,
      boolean extraPlayerOnly
  ) {
    String basePerm = CommandUtils.normalizePerm(basePermission);
    String extraPerm = CommandUtils.normalizePerm(extraPermission);
    boolean playerOnly = basePlayerOnly || extraPlayerOnly;

    if (basePerm.isEmpty() && extraPerm.isEmpty() && !playerOnly) return node;

    node.requires(src -> {
      CommandSender sender = src.getSender();
      if (playerOnly && !(sender instanceof Player)) return false;

      if (!basePerm.isEmpty() && !sender.hasPermission(basePerm)) return false;
      return extraPerm.isEmpty() || sender.hasPermission(extraPerm);
    });

    return node;
  }
}