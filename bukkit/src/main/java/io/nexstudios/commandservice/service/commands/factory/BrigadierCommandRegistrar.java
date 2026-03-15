package io.nexstudios.commandservice.service.commands.factory;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.nexstudios.commandservice.service.commands.factory.args.ArgTypeSupport;
import io.nexstudios.commandservice.service.commands.factory.model.CmdNode;
import io.nexstudios.commandservice.service.commands.factory.model.NodeKind;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.List;
import java.util.Objects;

import static io.papermc.paper.command.brigadier.Commands.literal;

public final class BrigadierCommandRegistrar {

  private BrigadierCommandRegistrar() {}

  public static void register(ServiceAccessor services, Commands commands, CommandModel model) {
    Objects.requireNonNull(services, "services");
    Objects.requireNonNull(commands, "commands");
    Objects.requireNonNull(model, "model");

    CmdNode root = model.root();

    LiteralArgumentBuilder<CommandSourceStack> rootBuilder = literal(root.name);

    resolve(services, root, rootBuilder, root.permission, root.playerOnly);

    commands.register(
        rootBuilder.build(),
        model.rootAnn().description(),
        List.of(model.rootAnn().aliases())
    );
  }

  private static ArgumentBuilder<CommandSourceStack, ?> buildBuilder(ServiceAccessor services, CmdNode node) {

    return (node.kind == NodeKind.LITERAL)
        ? literal(node.name)
        : ArgTypeSupport.buildArgumentBuilder(services, node);
  }

  private static void resolve(
      ServiceAccessor services,
      CmdNode node,
      ArgumentBuilder<CommandSourceStack, ?> builder,
      String inheritedPermission,
      boolean inheritedPlayerOnly
  ) {
    String execPerm = (node.exec == null) ? "" : node.exec.permission;
    boolean execPlayerOnly = (node.exec != null) && node.exec.playerOnly;

    CommandRequires.applyMerged(builder, inheritedPermission, inheritedPlayerOnly, execPerm, execPlayerOnly);

    if (node.exec != null) {
      builder.executes(ctx -> CommandInvoker.invoke(node.exec.handler, node.exec.method, ctx));
    }

    for (CmdNode child : node.children.values()) {
      ArgumentBuilder<CommandSourceStack, ?> childBuilder = buildBuilder(services, child);
      resolve(services, child, childBuilder, inheritedPermission, inheritedPlayerOnly);
      builder.then(childBuilder);
    }
  }
}