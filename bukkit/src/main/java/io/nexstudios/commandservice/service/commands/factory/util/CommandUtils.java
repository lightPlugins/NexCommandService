package io.nexstudios.commandservice.service.commands.factory.util;

public final class CommandUtils {

  private CommandUtils() {}

  public static String normalizePerm(String permission) {
    return permission == null ? "" : permission.trim();
  }
}