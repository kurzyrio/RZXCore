package com.rzx.core.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a top-level command handler.
 * <p>
 * Annotated methods must accept {@code (org.bukkit.command.CommandSender, String[])} as parameters.
 * The framework will register the command with Bukkit's command map at runtime,
 * so there is no need to declare it in {@code plugin.yml}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {

    /** The primary name of the command (without leading slash). */
    String name();

    /** Permission node required to execute. Empty = no permission required. */
    String permission() default "";

    /** Alternative names for this command. */
    String[] aliases() default {};

    /** A short description shown in help output. */
    String description() default "";

    /** Usage string shown when the command is used incorrectly. */
    String usage() default "";

    /** If true, only players (not console) can run this command. */
    boolean playerOnly() default false;
}

