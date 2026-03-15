package io.nexstudios.commandservice.service.commands.factory.model;

import java.lang.reflect.Method;

public final class Exec {
  public final Object handler;
  public final Method method;
  public final String permission;
  public final boolean playerOnly;

  public Exec(Object handler, Method method, String permission, boolean playerOnly) {
    this.handler = handler;
    this.method = method;
    this.permission = permission;
    this.playerOnly = playerOnly;
  }
}