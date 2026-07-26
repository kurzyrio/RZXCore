package com.rzx.core.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a subcommand handler under a parent {@link Command}.
 * <p>
 * Annotated methods must accept {@code (org.bukkit.command.CommandSender, String[])} as parameters,
 * where the {@code String[]} contains only the arguments <em>after</em> the subcommand name.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubCommand {

    /** The name of the subcommand (first argument after the parent command). */
    String name();

    /** The parent command name this subcommand belongs to. */
    String parent();

    /** Permission node required. Empty = inherits parent's permission. */
    String permission() default "";

    /** If true, only players can run this subcommand. */
    boolean playerOnly() default false;

    /** A short description shown in help output. */
    String description() default "";
}

