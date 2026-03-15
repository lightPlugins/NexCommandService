package io.nexstudios.commandservice.service.commands.factory;

import io.nexstudios.commandservice.service.commands.annotations.Command;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

final class CommandMethodScanner {

  private CommandMethodScanner() {}

  static List<Method> findCommandMethods(Class<?> type) {
    List<Method> out = new ArrayList<>();
    for (Method m : type.getDeclaredMethods()) {
      if (m.getAnnotation(Command.class) == null) continue;
      m.setAccessible(true);
      out.add(m);
    }
    return out;
  }
}