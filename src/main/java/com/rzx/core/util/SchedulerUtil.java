package com.rzx.core.util;

import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

/**
 * Convenience wrappers around Paper's scheduler API.
 */
public final class SchedulerUtil {

    private SchedulerUtil() {}

    /** Runs a task on the main thread (global region). */
    public static void runSync(Plugin plugin, Runnable task) {
        plugin.getServer().getGlobalRegionScheduler().run(plugin, scheduledTask -> task.run());
    }

    /** Runs a task asynchronously. */
    public static void runAsync(Plugin plugin, Runnable task) {
        plugin.getServer().getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
    }

    /** Runs a task on the main thread after a delay in ticks (1 tick = 50ms). */
    public static void runLater(Plugin plugin, Runnable task, long delayTicks) {
        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> task.run(), delayTicks);
    }

    /** Runs a repeating task on the main thread. */
    public static void runRepeating(Plugin plugin, Runnable task, long delayTicks, long periodTicks) {
        plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(
                plugin, scheduledTask -> task.run(), delayTicks, periodTicks);
    }

    /** Runs a delayed async task. */
    public static void runAsyncLater(Plugin plugin, Runnable task, long delayMs) {
        plugin.getServer().getAsyncScheduler().runDelayed(
                plugin, scheduledTask -> task.run(), delayMs, TimeUnit.MILLISECONDS);
    }

    /** Runs a repeating async task. */
    public static void runAsyncRepeating(Plugin plugin, Runnable task, long delayMs, long periodMs) {
        plugin.getServer().getAsyncScheduler().runAtFixedRate(
                plugin, scheduledTask -> task.run(), delayMs, periodMs, TimeUnit.MILLISECONDS);
    }
}

