package network.hera.gamekit.lobbypaper.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import java.io.IOException;
import java.util.Objects;
import network.hera.gamekit.lobby.config.LobbyRuntimeConfig;
import network.hera.gamekit.lobby.loadout.LobbyLoadout;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public final class BoostedYamlLobbyConfigStore implements LobbyConfigStore {

    private final YamlDocument document;

    public BoostedYamlLobbyConfigStore(@NotNull YamlDocument document) {
        this.document = Objects.requireNonNull(document, "document");
    }

    @Override
    public void setSpawn(@NotNull Location location) {
        Objects.requireNonNull(location, "location");
        if (location.getWorld() == null) {
            throw new IllegalArgumentException("Cannot set lobby spawn without a world.");
        }
        this.document.set("spawn.world", location.getWorld().getName());
        this.document.set("spawn.x", location.getX());
        this.document.set("spawn.y", location.getY());
        this.document.set("spawn.z", location.getZ());
        this.document.set("spawn.yaw", location.getYaw());
        this.document.set("spawn.pitch", location.getPitch());
    }

    @Override
    public void save() {
        try {
            this.document.save();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not save lobby config.", exception);
        }
    }

    @Override
    public @NotNull LobbyRuntimeConfig reloadConfig() {
        reload();
        return LobbyRuntimeConfigParser.parseConfig(this.document);
    }

    @Override
    public @NotNull LobbyLoadout reloadLoadout() {
        reload();
        return LobbyRuntimeConfigParser.parseLoadout(this.document);
    }

    private void reload() {
        try {
            this.document.reload();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not reload lobby config.", exception);
        }
    }
}
