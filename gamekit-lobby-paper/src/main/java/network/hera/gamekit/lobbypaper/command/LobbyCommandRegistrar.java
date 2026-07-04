package network.hera.gamekit.lobbypaper.command;

import network.hera.gamekit.lobbypaper.config.LobbyConfigStore;
import network.hera.gamekit.lobbypaper.runtime.PaperLobbyRuntime;
import network.hera.gamekit.paper.resource.RuntimeResource;
import org.bukkit.entity.Player;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.paper.util.sender.Source;
import org.jetbrains.annotations.NotNull;

public final class LobbyCommandRegistrar {

    private LobbyCommandRegistrar() {
    }

    public static @NotNull RuntimeResource register(
            @NotNull PaperCommandManager<Source> commands,
            @NotNull PaperLobbyRuntime runtime,
            @NotNull LobbyConfigStore configStore
    ) {
        if (!runtime.config().commandPolicy().enabled()) {
            return RuntimeResource.noop();
        }
        String root = runtime.config().commandPolicy().root();
        String permissionPrefix = runtime.config().commandPolicy().permissionPrefix();
        commands.command(commands.commandBuilder(root)
            .literal("lobby")
            .literal("setspawn")
            .senderType(PlayerSource.class)
            .permission(permissionPrefix + ".setspawn")
            .handler(context -> {
                Player player = context.sender().source();
                configStore.setSpawn(player.getLocation());
                configStore.save();
                runtime.replaceConfig(configStore.reloadConfig(), configStore.reloadLoadout());
                player.sendMessage("Lobby spawn updated.");
            })
        );
        commands.command(commands.commandBuilder(root)
            .literal("lobby")
            .literal("spawn")
            .senderType(PlayerSource.class)
            .permission(permissionPrefix + ".spawn")
            .handler(context -> runtime.teleportSpawn(context.sender().source()))
        );
        commands.command(commands.commandBuilder(root)
            .literal("lobby")
            .literal("reload")
            .permission(permissionPrefix + ".reload")
            .handler(context -> runtime.replaceConfig(configStore.reloadConfig(), configStore.reloadLoadout()))
        );
        commands.command(commands.commandBuilder(root)
            .literal("lobby")
            .literal("staff")
            .senderType(PlayerSource.class)
            .permission(permissionPrefix + ".staff")
            .handler(context -> runtime.toggleStaffMode(context.sender().source()))
        );
        commands.command(commands.commandBuilder(root)
            .literal("lobby")
            .literal("items")
            .literal("refresh")
            .senderType(PlayerSource.class)
            .permission(permissionPrefix + ".items.refresh")
            .handler(context -> runtime.refreshLoadout(context.sender().source()))
        );
        return () -> commands.deleteRootCommand(root);
    }
}
