package network.hera.gamekit.lobbypaper.runtime;

import network.hera.gamekit.lobby.loadout.LobbyClickType;
import network.hera.gamekit.lobby.loadout.LobbyItemDefinition;
import network.hera.gamekit.lobbypaper.action.LobbyItemAction;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.PlayerInventory;

final class PaperLobbyListener implements Listener {

    private final PaperLobbyRuntime runtime;

    PaperLobbyListener(PaperLobbyRuntime runtime) {
        this.runtime = runtime;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        this.runtime.applyJoin(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.runtime.cleanup(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (this.runtime.isStaffMode(player)) {
            return;
        }
        if (!this.runtime.isInLobbyWorld(player)) {
            return;
        }
        if (event.getHand() != null && event.getHand() != EquipmentSlot.HAND) {
            event.setCancelled(true);
            return;
        }
        String itemId = this.runtime.itemRenderer().lobbyItemId(event.getItem());
        if (itemId != null) {
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            LobbyClickType clickType = clickType(event.getAction());
            if (clickType != null) {
                executeAction(player, itemId, clickType);
            }
            return;
        }
        if (event.hasBlock() && this.runtime.config().protectionPolicy().blockInteract()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player) || this.runtime.isStaffMode(player) || !this.runtime.isInLobbyWorld(player) || !this.runtime.config().protectionPolicy().inventory()) {
            return;
        }
        boolean playerInventory = event.getClickedInventory() instanceof PlayerInventory;
        boolean hotbarSwap = event.getAction() == InventoryAction.HOTBAR_SWAP || event.getClick() == ClickType.NUMBER_KEY || event.getClick() == ClickType.SWAP_OFFHAND;
        boolean shiftMove = event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY;
        boolean protectedItem = this.runtime.itemRenderer().isLobbyItem(event.getCurrentItem()) || this.runtime.itemRenderer().isLobbyItem(event.getCursor());
        if (playerInventory || hotbarSwap || shiftMove || protectedItem) {
            event.setCancelled(true);
            this.runtime.refreshLoadout(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player && !this.runtime.isStaffMode(player) && this.runtime.isInLobbyWorld(player) && this.runtime.config().protectionPolicy().inventory()) {
            int topSize = event.getView().getTopInventory().getSize();
            if (event.getRawSlots().stream().anyMatch(slot -> slot >= topSize)) {
                event.setCancelled(true);
                this.runtime.refreshLoadout(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(PlayerDropItemEvent event) {
        if (!this.runtime.isStaffMode(event.getPlayer()) && this.runtime.isInLobbyWorld(event.getPlayer()) && this.runtime.config().protectionPolicy().drop()) {
            event.setCancelled(true);
            this.runtime.refreshLoadout(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickup(PlayerAttemptPickupItemEvent event) {
        if (!this.runtime.isStaffMode(event.getPlayer()) && this.runtime.isInLobbyWorld(event.getPlayer()) && this.runtime.config().protectionPolicy().pickup()) {
            event.setCancelled(true);
            event.setFlyAtPlayer(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player && !this.runtime.isStaffMode(player) && this.runtime.isInLobbyWorld(player) && this.runtime.config().protectionPolicy().pickup()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        if (!this.runtime.isStaffMode(event.getPlayer()) && this.runtime.isInLobbyWorld(event.getPlayer()) && this.runtime.config().protectionPolicy().offhand()) {
            event.setCancelled(true);
            this.runtime.refreshLoadout(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemDamage(PlayerItemDamageEvent event) {
        if (!this.runtime.isStaffMode(event.getPlayer()) && this.runtime.isInLobbyWorld(event.getPlayer()) && this.runtime.config().protectionPolicy().itemDamage() && this.runtime.itemRenderer().isLobbyItem(event.getItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCraft(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player player && !this.runtime.isStaffMode(player) && this.runtime.isInLobbyWorld(player) && this.runtime.config().protectionPolicy().craft()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (event.getView().getPlayer() instanceof Player player && !this.runtime.isStaffMode(player) && this.runtime.isInLobbyWorld(player) && this.runtime.config().protectionPolicy().craft()) {
            event.getInventory().setResult(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!this.runtime.isStaffMode(event.getPlayer()) && this.runtime.isInLobbyWorld(event.getPlayer()) && this.runtime.config().protectionPolicy().blockPlace()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!this.runtime.isStaffMode(event.getPlayer()) && this.runtime.isInLobbyWorld(event.getPlayer()) && this.runtime.config().protectionPolicy().blockBreak()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || this.runtime.isStaffMode(player) || !this.runtime.isInLobbyWorld(player)) {
            return;
        }
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause == EntityDamageEvent.DamageCause.VOID && this.runtime.config().protectionPolicy().voidTeleport()) {
            event.setCancelled(true);
            Location spawn = this.runtime.requireSpawnLocation();
            player.teleport(spawn);
            player.setFallDistance(0.0F);
            player.setFireTicks(0);
            return;
        }
        boolean cancel = switch (cause) {
            case FALL -> this.runtime.config().protectionPolicy().fallDamage();
            case FIRE, FIRE_TICK, LAVA, CAMPFIRE, HOT_FLOOR -> this.runtime.config().protectionPolicy().fireDamage();
            case DROWNING -> this.runtime.config().protectionPolicy().drowning();
            case STARVATION -> this.runtime.config().protectionPolicy().hunger();
            default -> false;
        };
        if (cancel) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPvp(EntityDamageByEntityEvent event) {
        if (this.runtime.config().protectionPolicy().pvp() && event.getEntity() instanceof Player victim && event.getDamager() instanceof Player damager && this.runtime.isInLobbyWorld(victim) && !this.runtime.isStaffMode(damager)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMobSpawn(CreatureSpawnEvent event) {
        if (isLobbyWorld(event.getLocation().getWorld()) && this.runtime.config().protectionPolicy().mobSpawn()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player) && isLobbyWorld(event.getEntity().getWorld()) && this.runtime.config().protectionPolicy().mobDrops()) {
            event.getDrops().clear();
            event.setDroppedExp(0);
            event.setShouldPlayDeathSound(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (this.runtime.isInLobbyWorld(event.getEntity()) && this.runtime.config().protectionPolicy().deathMessages()) {
            event.setShowDeathMessages(false);
            event.deathMessage(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWeather(WeatherChangeEvent event) {
        if (this.runtime.isLobbyWorld(event.getWorld()) && this.runtime.config().protectionPolicy().weatherChange() && event.toWeatherState()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTimeSkip(TimeSkipEvent event) {
        if (this.runtime.isLobbyWorld(event.getWorld()) && this.runtime.config().worldPolicy().fixedTimeEnabled()) {
            event.setCancelled(true);
            event.getWorld().setTime(this.runtime.config().worldPolicy().fixedTimeValue());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onThunder(ThunderChangeEvent event) {
        if (this.runtime.isLobbyWorld(event.getWorld()) && this.runtime.config().protectionPolicy().weatherChange() && event.toThunderState()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockSpread(BlockSpreadEvent event) {
        if (isLobbyWorld(event.getBlock().getWorld()) && this.runtime.config().protectionPolicy().fireSpread()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBurn(BlockBurnEvent event) {
        if (isLobbyWorld(event.getBlock().getWorld()) && this.runtime.config().protectionPolicy().fireSpread()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeavesDecay(LeavesDecayEvent event) {
        if (isLobbyWorld(event.getBlock().getWorld()) && this.runtime.config().protectionPolicy().leafDecay()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (isLobbyWorld(event.getEntity().getWorld()) && this.runtime.config().protectionPolicy().itemFrames() && event.getEntity() instanceof Hanging && event.getRemover() instanceof Player player && !this.runtime.isStaffMode(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (!this.runtime.isStaffMode(event.getPlayer()) && this.runtime.isInLobbyWorld(event.getPlayer()) && this.runtime.config().protectionPolicy().itemFrames() && event.getRightClicked() instanceof Hanging) {
            event.setCancelled(true);
        }
    }

    private boolean isLobbyWorld(World world) {
        return world != null && this.runtime.isLobbyWorld(world);
    }

    private void executeAction(Player player, String itemId, LobbyClickType clickType) {
        LobbyItemDefinition item = this.runtime.loadout().findById(itemId).orElse(null);
        if (item == null) {
            this.runtime.refreshLoadout(player);
            return;
        }
        var definition = item.actions().get(clickType);
        if (definition == null) {
            return;
        }
        var action = this.runtime.actions().find(definition.type())
            .orElseThrow(() -> new IllegalStateException("No lobby action registered for type: " + definition.type()));
        action.execute(new LobbyItemAction.Context(player, item, clickType, definition));
    }

    private static LobbyClickType clickType(Action action) {
        if (action.isLeftClick()) {
            return LobbyClickType.LEFT;
        }
        if (action.isRightClick()) {
            return LobbyClickType.RIGHT;
        }
        return null;
    }
}
