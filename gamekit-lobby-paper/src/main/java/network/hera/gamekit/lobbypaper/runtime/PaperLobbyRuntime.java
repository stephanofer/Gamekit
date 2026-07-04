package network.hera.gamekit.lobbypaper.runtime;

import java.util.Objects;
import network.hera.gamekit.lobby.config.LobbyRuntimeConfig;
import network.hera.gamekit.lobby.loadout.LobbyLoadout;
import network.hera.gamekit.lobbypaper.action.LobbyActionRegistry;
import network.hera.gamekit.lobbypaper.item.PaperLobbyItemKeys;
import network.hera.gamekit.lobbypaper.item.PaperLobbyItemRenderer;
import network.hera.gamekit.lobbypaper.language.LobbyLanguageResolver;
import network.hera.gamekit.paper.listener.PaperListenerRegistration;
import network.hera.gamekit.paper.player.PaperPlayerOperations;
import network.hera.gamekit.paper.resource.RuntimeResource;
import network.hera.gamekit.paper.resource.RuntimeResources;
import network.hera.gamekit.paper.scheduler.PaperScheduler;
import network.hera.gamekit.paper.world.PaperWorldOperations;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class PaperLobbyRuntime {

    private final JavaPlugin plugin;
    private final LobbyActionRegistry actions;
    private final LobbyLanguageResolver languageResolver;
    private final PaperPlayerOperations players = new PaperPlayerOperations();
    private final PaperWorldOperations worlds = new PaperWorldOperations();
    private final PaperLobbyItemRenderer itemRenderer;
    private final LobbyStaffModeService staffMode;
    private volatile LobbyRuntimeConfig config;
    private volatile LobbyLoadout loadout;

    private PaperLobbyRuntime(
            @NotNull JavaPlugin plugin,
            @NotNull LobbyRuntimeConfig config,
            @NotNull LobbyLoadout loadout,
            @NotNull LobbyActionRegistry actions,
            @NotNull LobbyLanguageResolver languageResolver
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.config = Objects.requireNonNull(config, "config");
        this.loadout = Objects.requireNonNull(loadout, "loadout");
        this.actions = Objects.requireNonNull(actions, "actions");
        this.languageResolver = Objects.requireNonNull(languageResolver, "languageResolver");
        this.itemRenderer = new PaperLobbyItemRenderer(PaperLobbyItemKeys.create(plugin));
        this.staffMode = new LobbyStaffModeService(this.players);
    }

    public static @NotNull PaperLobbyRuntime create(
            @NotNull JavaPlugin plugin,
            @NotNull LobbyRuntimeConfig config,
            @NotNull LobbyLoadout loadout,
            @NotNull LobbyActionRegistry actions,
            @NotNull LobbyLanguageResolver languageResolver
    ) {
        return new PaperLobbyRuntime(plugin, config, loadout, actions, languageResolver);
    }

    public @NotNull RuntimeResource register() {
        if (!this.config.enabled()) {
            return RuntimeResource.noop();
        }
        RuntimeResources resources = RuntimeResources.create();
        applyWorldPolicy();
        if (this.config.worldPolicy().fixedTimeEnabled()) {
            resources.add(new PaperScheduler(this.plugin).runTimer(this::applyWorldPolicy, 200L, 200L));
        }
        resources.add(this::disableStaffModeForOnlinePlayers);
        resources.add(PaperListenerRegistration.register(this.plugin, new PaperLobbyListener(this)));
        for (Player player : Bukkit.getOnlinePlayers()) {
            applyJoin(player);
        }
        return resources;
    }

    public void replaceConfig(@NotNull LobbyRuntimeConfig config, @NotNull LobbyLoadout loadout) {
        this.config = Objects.requireNonNull(config, "config");
        this.loadout = Objects.requireNonNull(loadout, "loadout");
        applyWorldPolicy();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isStaffMode(player)) {
                continue;
            }
            refreshLoadout(player);
        }
    }

    public void applyJoin(@NotNull Player player) {
        Objects.requireNonNull(player, "player");
        if (isStaffMode(player)) {
            return;
        }
        var join = this.config.joinPolicy();
        if (join.clearInventory()) {
            this.players.clearInventory(player);
        }
        var reset = join.resetPlayer();
        if (reset.enabled()) {
            this.players.resetPlayer(player, reset.health(), reset.food(), reset.saturation(), reset.fire(), reset.freeze(), reset.fallDistance(), reset.remainingAir(), reset.potionEffects(), reset.exp());
        }
        if (join.teleportSpawn()) {
            this.players.teleport(player, requireSpawnLocation());
        }
        if (join.setGameMode().enabled()) {
            this.players.setGameMode(player, gameMode(join.setGameMode().value()));
        }
        if (join.applyLoadout()) {
            refreshLoadout(player);
        }
        if (join.heldSlot().enabled()) {
            player.getInventory().setHeldItemSlot(join.heldSlot().slot());
        }
    }

    public void refreshLoadout(@NotNull Player player) {
        Objects.requireNonNull(player, "player");
        if (isStaffMode(player)) {
            return;
        }
        String language = this.languageResolver.resolveLanguage(player);
        for (var item : this.loadout.items()) {
            player.getInventory().setItem(item.slot(), this.itemRenderer.render(item, language, this.config.defaultLanguage()));
        }
        player.updateInventory();
    }

    public void teleportSpawn(@NotNull Player player) {
        this.players.teleport(player, requireSpawnLocation());
    }

    public boolean enableStaffMode(@NotNull Player player) {
        if (!this.config.staffModePolicy().enabled()) {
            return false;
        }
        if (!player.hasPermission(this.config.staffModePolicy().permission())) {
            return false;
        }
        this.staffMode.enable(player, gameMode(this.config.staffModePolicy().gameMode()), this.config.staffModePolicy().clearInventoryOnEnable());
        return true;
    }

    public void disableStaffMode(@NotNull Player player) {
        boolean disabled = this.staffMode.disable(player);
        if (disabled && this.config.staffModePolicy().restoreLobbyOnDisable()) {
            applyJoin(player);
        }
    }

    public boolean toggleStaffMode(@NotNull Player player) {
        if (isStaffMode(player)) {
            disableStaffMode(player);
            return false;
        }
        return enableStaffMode(player);
    }

    public boolean isStaffMode(@NotNull Player player) {
        return this.staffMode.isEnabled(player);
    }

    void cleanup(@NotNull Player player) {
        this.staffMode.clear(player);
    }

    boolean isLobbyWorld(@NotNull World world) {
        return world.getName().equals(this.config.spawn().world());
    }

    boolean isInLobbyWorld(@NotNull Player player) {
        return isLobbyWorld(player.getWorld());
    }

    private void disableStaffModeForOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isStaffMode(player)) {
                disableStaffMode(player);
            }
        }
    }

    void applyWorldPolicy() {
        World world = requireSpawnLocation().getWorld();
        if (world == null) {
            throw new IllegalStateException("Lobby spawn world is not loaded.");
        }
        if (this.config.worldPolicy().fixedTimeEnabled()) {
            this.worlds.applyFixedTime(world, this.config.worldPolicy().fixedTimeValue());
        }
        if (this.config.worldPolicy().clearWeatherEnabled()) {
            this.worlds.applyClearWeather(world);
        }
    }

    Location requireSpawnLocation() {
        World world = Bukkit.getWorld(this.config.spawn().world());
        if (world == null) {
            throw new IllegalStateException("Lobby spawn world is not loaded: " + this.config.spawn().world());
        }
        return new Location(world, this.config.spawn().x(), this.config.spawn().y(), this.config.spawn().z(), this.config.spawn().yaw(), this.config.spawn().pitch());
    }

    static GameMode gameMode(String value) {
        return GameMode.valueOf(value.trim().toUpperCase(java.util.Locale.ROOT));
    }

    public JavaPlugin plugin() { return this.plugin; }
    public LobbyRuntimeConfig config() { return this.config; }
    LobbyLoadout loadout() { return this.loadout; }
    LobbyActionRegistry actions() { return this.actions; }
    PaperLobbyItemRenderer itemRenderer() { return this.itemRenderer; }
}
