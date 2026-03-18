package io.nexstudios.commandservice.service.commands.factory;

import io.nexstudios.commandservice.service.commands.annotations.*;
import io.nexstudios.commandservice.service.commands.factory.args.ArgParsing;
import io.nexstudios.commandservice.service.commands.factory.model.ArgSpec;
import io.nexstudios.commandservice.service.commands.factory.model.CmdNode;
import io.nexstudios.commandservice.service.commands.factory.model.Exec;
import io.nexstudios.commandservice.service.commands.factory.suggest.OnlinePlayersSuggestion;
import io.nexstudios.commandservice.service.commands.factory.suggest.SuggestionProvider;
import io.nexstudios.commandservice.service.commands.factory.util.CommandUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

public final class CommandModelBuilder {

  private CommandModelBuilder() {}

  public static CommandModel build(Object handler) {
    CommandRoot rootAnn = handler.getClass().getAnnotation(CommandRoot.class);
    if (rootAnn == null) {
      throw new IllegalStateException("Missing @CommandRoot on " + handler.getClass().getName());
    }

    CmdNode root = CmdNode.root(rootAnn.name());
    root.permission = CommandUtils.normalizePerm(rootAnn.permission());
    root.playerOnly = rootAnn.playerOnly();

    buildInto(handler, root, rootAnn);

    return new CommandModel(rootAnn, root);
  }

  public static void buildInto(Object handler, CmdNode root, CommandRoot rootAnn) {
    if (rootAnn == null) {
      throw new IllegalStateException("Missing @CommandRoot on " + handler.getClass().getName());
    }

    List<Method> commandMethods = CommandMethodScanner.findCommandMethods(handler.getClass());

    for (Method m : commandMethods) {
      Command ann = m.getAnnotation(Command.class);
      String path = ann.value().trim();

      if (path.isEmpty()) {
        if (root.exec != null) {
          throw new IllegalStateException("Duplicate root execution (@Command(\"\")) in: " + m);
        }
        root.exec = new Exec(handler, m, CommandUtils.normalizePerm(ann.permission()), ann.playerOnly());
        continue;
      }

      String[] parts = path.split("\\s+");
      CmdNode current = root;

      for (int i = 0; i < parts.length; i++) {
        String token = parts[i];

        if (ArgParsing.isArgToken(token)) {
          ArgParsing.ArgToken arg = ArgParsing.parseArgToken(token);
          ArgParsing.assertGreedyLast(arg, i, parts.length, m);

          current = current.childArg(arg.name(), resolveArgSpec(m, arg.name(), arg.greedy()));
          continue;
        }

        current = current.childLiteral(token);
      }

      if (current.exec != null) {
        throw new IllegalStateException("Duplicate command path '" + path + "' (already mapped) in: " + m);
      }
      current.exec = new Exec(handler, m, CommandUtils.normalizePerm(ann.permission()), ann.playerOnly());
    }
  }

  private static ArgSpec resolveArgSpec(Method method, String argName, boolean greedyFromPath) {
    Parameter param = ArgParsing.findArgParameter(method, argName);
    Class<?> type = param.getType();

    boolean greedy = greedyFromPath || param.isAnnotationPresent(Greedy.class);

    Suggest suggest = param.getAnnotation(Suggest.class);
    Class<? extends SuggestionProvider> suggestClass = (suggest == null) ? null : suggest.value();

    if (suggestClass == null && param.isAnnotationPresent(SuggestPlayers.class)) {
      suggestClass = OnlinePlayersSuggestion.class;
    }

    return new ArgSpec(type, greedy, suggestClass);
  }
}