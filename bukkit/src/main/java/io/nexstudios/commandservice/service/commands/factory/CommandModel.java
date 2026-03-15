package io.nexstudios.commandservice.service.commands.factory;

import io.nexstudios.commandservice.service.commands.annotations.CommandRoot;
import io.nexstudios.commandservice.service.commands.factory.model.CmdNode;

import java.util.Objects;

public record CommandModel(CommandRoot rootAnn, CmdNode root) {
  public CommandModel {
    Objects.requireNonNull(rootAnn, "rootAnn");
    Objects.requireNonNull(root, "root");
  }
}