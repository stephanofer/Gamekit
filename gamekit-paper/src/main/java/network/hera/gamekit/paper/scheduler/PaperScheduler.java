package network.hera.gamekit.paper.scheduler;

import java.util.Objects;
import network.hera.gamekit.paper.resource.RuntimeResource;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public final class PaperScheduler {

    private final Plugin plugin;

    public PaperScheduler(@NotNull Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    public @NotNull RuntimeResource runNextTick(@NotNull Runnable task) {
        return handle(this.plugin.getServer().getScheduler().runTask(this.plugin, Objects.requireNonNull(task, "task")));
    }

    public @NotNull RuntimeResource runLater(@NotNull Runnable task, long delayTicks) {
        return handle(this.plugin.getServer().getScheduler().runTaskLater(this.plugin, Objects.requireNonNull(task, "task"), delayTicks));
    }

    public @NotNull RuntimeResource runTimer(@NotNull Runnable task, long delayTicks, long periodTicks) {
        return handle(this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, Objects.requireNonNull(task, "task"), delayTicks, periodTicks));
    }

    public @NotNull RuntimeResource runAsync(@NotNull Runnable task) {
        return handle(this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, Objects.requireNonNull(task, "task")));
    }

    public @NotNull RuntimeResource runLaterAsync(@NotNull Runnable task, long delayTicks) {
        return handle(this.plugin.getServer().getScheduler().runTaskLaterAsynchronously(this.plugin, Objects.requireNonNull(task, "task"), delayTicks));
    }

    private static @NotNull RuntimeResource handle(@NotNull BukkitTask task) {
        return task::cancel;
    }
}
