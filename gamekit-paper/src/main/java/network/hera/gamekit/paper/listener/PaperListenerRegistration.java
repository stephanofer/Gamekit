package network.hera.gamekit.paper.listener;

import java.util.Objects;
import network.hera.gamekit.paper.resource.RuntimeResource;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class PaperListenerRegistration implements RuntimeResource {

    private final Listener listener;
    private boolean closed;

    private PaperListenerRegistration(@NotNull Listener listener) {
        this.listener = Objects.requireNonNull(listener, "listener");
    }

    public static @NotNull PaperListenerRegistration register(@NotNull Plugin plugin, @NotNull Listener listener) {
        Objects.requireNonNull(plugin, "plugin").getServer().getPluginManager().registerEvents(listener, plugin);
        return new PaperListenerRegistration(listener);
    }

    @Override
    public void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        HandlerList.unregisterAll(this.listener);
    }
}
