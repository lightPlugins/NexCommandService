package io.nexstudios.commandservice.service.commands.factory.model;

import java.util.LinkedHashMap;
import java.util.Map;

public final class CmdNode {
  public final NodeKind kind;
  public final String name;

  public ArgSpec argSpec; // only for ARG
  public final Map<String, CmdNode> children = new LinkedHashMap<>();

  public String permission = "";
  public boolean playerOnly = false;

  public Exec exec;

  private CmdNode(NodeKind kind, String name) {
    this.kind = kind;
    this.name = name;
  }

  public static CmdNode root(String name) {
    return new CmdNode(NodeKind.LITERAL, name);
  }

  public CmdNode childLiteral(String literal) {
    String key = "L:" + literal;
    return children.computeIfAbsent(key, k -> new CmdNode(NodeKind.LITERAL, literal));
  }

  public CmdNode childArg(String argName, ArgSpec spec) {
    String key = "A:" + argName;
    CmdNode n = children.computeIfAbsent(key, k -> new CmdNode(NodeKind.ARG, argName));
    if (n.argSpec == null) {
      n.argSpec = spec;
    }
    return n;
  }
}