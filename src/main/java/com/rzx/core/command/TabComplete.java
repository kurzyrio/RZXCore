package com.rzx.core.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a tab-completion provider for a {@link Command}.
 * <p>
 * Annotated methods must accept {@code (org.bukkit.command.CommandSender, String[])} and
 * return {@code java.util.List<String>}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TabComplete {

    /** The command name this tab-completer is associated with. */
    String name();
}

