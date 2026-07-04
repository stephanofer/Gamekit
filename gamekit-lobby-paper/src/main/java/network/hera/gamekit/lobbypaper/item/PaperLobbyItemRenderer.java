package network.hera.gamekit.lobbypaper.item;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import network.hera.gamekit.lobby.loadout.LobbyItemDefinition;
import network.hera.gamekit.lobby.loadout.LocalizedLobbyItemText;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public final class PaperLobbyItemRenderer {

    private final PaperLobbyItemKeys keys;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public PaperLobbyItemRenderer(@NotNull PaperLobbyItemKeys keys) {
        this.keys = Objects.requireNonNull(keys, "keys");
    }

    public @NotNull ItemStack render(@NotNull LobbyItemDefinition definition, @NotNull String language, @NotNull String defaultLanguage) {
        Objects.requireNonNull(definition, "definition");
        Material material = Material.matchMaterial(definition.material());
        if (material == null || material.isAir()) {
            throw new IllegalArgumentException("Invalid lobby item material for " + definition.id() + ": " + definition.material());
        }
        LocalizedLobbyItemText text = definition.textFor(language, defaultLanguage);
        ItemStack item = new ItemStack(material);
        item.editMeta(meta -> {
            meta.displayName(parse(text.name()));
            meta.lore(text.lore().stream().map(this::parse).toList());
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
            meta.getPersistentDataContainer().set(this.keys.itemId(), PersistentDataType.STRING, definition.id());
        });
        return item;
    }

    public boolean isLobbyItem(ItemStack item) {
        return lobbyItemId(item) != null;
    }

    public String lobbyItemId(ItemStack item) {
        if (item == null || item.isEmpty() || !item.hasItemMeta()) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer().get(this.keys.itemId(), PersistentDataType.STRING);
    }

    private @NotNull Component parse(@NotNull String input) {
        return this.miniMessage.deserialize(input);
    }
}
