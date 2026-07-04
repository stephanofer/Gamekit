package network.hera.gamekit.paper.player;

import java.util.Objects;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

public final class PaperPlayerOperations {

    public void clearInventory(@NotNull Player player) {
        Objects.requireNonNull(player, "player").getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().setExtraContents(null);
        player.setItemOnCursor(null);
        player.updateInventory();
    }

    public void teleport(@NotNull Player player, @NotNull Location location) {
        Objects.requireNonNull(player, "player").teleport(Objects.requireNonNull(location, "location"), PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    public void setGameMode(@NotNull Player player, @NotNull GameMode gameMode) {
        Objects.requireNonNull(player, "player").setGameMode(Objects.requireNonNull(gameMode, "gameMode"));
    }

    public void resetPlayer(@NotNull Player player, boolean health, boolean food, boolean saturation, boolean fire, boolean freeze,
                            boolean fallDistance, boolean remainingAir, boolean potionEffects, boolean exp) {
        Objects.requireNonNull(player, "player");
        if (health) {
            AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
            player.setHealth(maxHealth == null ? 20.0D : Math.max(1.0D, maxHealth.getValue()));
        }
        if (food) {
            player.setFoodLevel(20);
        }
        if (saturation) {
            player.setSaturation(20.0F);
            player.setExhaustion(0.0F);
        }
        if (fire) {
            player.setFireTicks(0);
            player.setVisualFire(false);
        }
        if (freeze) {
            player.setFreezeTicks(0);
        }
        if (fallDistance) {
            player.setFallDistance(0.0F);
        }
        if (remainingAir) {
            player.setRemainingAir(player.getMaximumAir());
        }
        if (potionEffects) {
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
        }
        if (exp) {
            player.setLevel(0);
            player.setExp(0.0F);
            player.setTotalExperience(0);
        }
    }
}
