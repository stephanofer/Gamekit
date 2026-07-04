package network.hera.gamekit.lobbypaper.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import network.hera.gamekit.lobby.config.LobbyCommandPolicy;
import network.hera.gamekit.lobby.config.LobbyJoinPolicy;
import network.hera.gamekit.lobby.config.LobbyProtectionPolicy;
import network.hera.gamekit.lobby.config.LobbyRuntimeConfig;
import network.hera.gamekit.lobby.config.LobbySpawn;
import network.hera.gamekit.lobby.config.LobbyStaffModePolicy;
import network.hera.gamekit.lobby.config.LobbyWorldPolicy;
import network.hera.gamekit.lobby.loadout.LobbyActionDefinition;
import network.hera.gamekit.lobby.loadout.LobbyClickType;
import network.hera.gamekit.lobby.loadout.LobbyItemDefinition;
import network.hera.gamekit.lobby.loadout.LobbyLoadout;
import network.hera.gamekit.lobby.loadout.LocalizedLobbyItemText;
import org.jetbrains.annotations.NotNull;

public final class LobbyRuntimeConfigParser {

    private LobbyRuntimeConfigParser() {
    }

    public static @NotNull LobbyRuntimeConfig parseConfig(@NotNull YamlDocument document) {
        return new LobbyRuntimeConfig(
            bool(document, "runtime.enabled", true),
            str(document, "runtime.default-language", "en"),
            new LobbySpawn(
                required(document, "spawn.world"),
                dbl(document, "spawn.x", 0.5D),
                dbl(document, "spawn.y", 80.0D),
                dbl(document, "spawn.z", 0.5D),
                flt(document, "spawn.yaw", 0.0F),
                flt(document, "spawn.pitch", 0.0F)
            ),
            parseJoin(document),
            parseProtections(document),
            new LobbyWorldPolicy(
                bool(document, "world.fixed-time.enabled", true),
                lng(document, "world.fixed-time.value", 6000L),
                bool(document, "world.clear-weather.enabled", true)
            ),
            new LobbyStaffModePolicy(
                bool(document, "staff-mode.enabled", true),
                str(document, "staff-mode.permission", "gamekit.lobby.staff"),
                str(document, "staff-mode.gamemode", "creative"),
                bool(document, "staff-mode.clear-inventory-on-enable", true),
                bool(document, "staff-mode.restore-lobby-on-disable", true)
            ),
            new LobbyCommandPolicy(
                bool(document, "commands.enabled", true),
                str(document, "commands.root", "gamekit"),
                str(document, "commands.permission-prefix", "gamekit.lobby")
            )
        );
    }

    public static @NotNull LobbyLoadout parseLoadout(@NotNull YamlDocument document) {
        Section items = document.getSection("items", null);
        if (items == null) {
            return new LobbyLoadout(List.of());
        }
        List<LobbyItemDefinition> definitions = items.getKeys().stream()
            .map(String::valueOf)
            .map(id -> parseItem(items, id))
            .toList();
        return new LobbyLoadout(definitions);
    }

    private static @NotNull LobbyItemDefinition parseItem(@NotNull Section items, @NotNull String id) {
        Section item = items.getSection(id);
        Map<String, LocalizedLobbyItemText> text = new LinkedHashMap<>();
        Section textSection = item.getSection("text", null);
        if (textSection != null) {
            for (Object key : textSection.getKeys()) {
                String language = String.valueOf(key);
                Section languageSection = textSection.getSection(language);
                text.put(language, new LocalizedLobbyItemText(
                    languageSection.getString("name", id),
                    languageSection.getStringList("lore")
                ));
            }
        }
        Map<LobbyClickType, LobbyActionDefinition> actions = new EnumMap<>(LobbyClickType.class);
        Section actionsSection = item.getSection("actions", null);
        if (actionsSection != null) {
            parseAction(actionsSection, "left", LobbyClickType.LEFT, actions);
            parseAction(actionsSection, "right", LobbyClickType.RIGHT, actions);
        }
        return new LobbyItemDefinition(
            id,
            item.getInt("slot", -1),
            item.getString("material", ""),
            text,
            actions
        );
    }

    private static void parseAction(Section section, String key, LobbyClickType clickType, Map<LobbyClickType, LobbyActionDefinition> actions) {
        Section action = section.getSection(key, null);
        if (action == null) {
            return;
        }
        Map<String, String> arguments = new LinkedHashMap<>();
        for (Object route : action.getKeys()) {
            String argument = String.valueOf(route);
            if (!"type".equals(argument)) {
                arguments.put(argument, String.valueOf(action.get(argument)));
            }
        }
        actions.put(clickType, new LobbyActionDefinition(action.getString("type", ""), arguments));
    }

    private static @NotNull LobbyJoinPolicy parseJoin(@NotNull YamlDocument document) {
        return new LobbyJoinPolicy(
            bool(document, "join.clear-inventory.enabled", true),
            new LobbyJoinPolicy.ResetPlayer(
                bool(document, "join.reset-player.enabled", true),
                bool(document, "join.reset-player.health", true),
                bool(document, "join.reset-player.food", true),
                bool(document, "join.reset-player.saturation", true),
                bool(document, "join.reset-player.fire", true),
                bool(document, "join.reset-player.freeze", true),
                bool(document, "join.reset-player.fall-distance", true),
                bool(document, "join.reset-player.remaining-air", true),
                bool(document, "join.reset-player.potion-effects", true),
                bool(document, "join.reset-player.exp", false)
            ),
            bool(document, "join.teleport-spawn.enabled", true),
            new LobbyJoinPolicy.SetGameMode(bool(document, "join.set-gamemode.enabled", true), str(document, "join.set-gamemode.value", "adventure")),
            bool(document, "join.apply-loadout.enabled", true),
            new LobbyJoinPolicy.HeldSlot(bool(document, "join.held-slot.enabled", true), integer(document, "join.held-slot.slot", 0))
        );
    }

    private static @NotNull LobbyProtectionPolicy parseProtections(@NotNull YamlDocument document) {
        return new LobbyProtectionPolicy(
            bool(document, "protections.inventory", true),
            bool(document, "protections.drop", true),
            bool(document, "protections.pickup", true),
            bool(document, "protections.offhand", true),
            bool(document, "protections.craft", true),
            bool(document, "protections.item-damage", true),
            bool(document, "protections.block-place", true),
            bool(document, "protections.block-break", true),
            bool(document, "protections.block-interact", true),
            bool(document, "protections.pvp", true),
            bool(document, "protections.fall-damage", true),
            bool(document, "protections.fire-damage", true),
            bool(document, "protections.drowning", true),
            bool(document, "protections.hunger", true),
            bool(document, "protections.void-teleport", true),
            bool(document, "protections.mob-spawn", true),
            bool(document, "protections.mob-drops", true),
            bool(document, "protections.death-messages", true),
            bool(document, "protections.weather-change", true),
            bool(document, "protections.fire-spread", true),
            bool(document, "protections.leaf-decay", true),
            bool(document, "protections.item-frames", true)
        );
    }

    private static @NotNull String required(@NotNull YamlDocument document, @NotNull String route) {
        String value = document.getString(route, null);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(route + " is required.");
        }
        return value;
    }

    private static String str(YamlDocument document, String route, String def) { return document.getString(route, def); }
    private static boolean bool(YamlDocument document, String route, boolean def) { return Boolean.TRUE.equals(document.getBoolean(route, def)); }
    private static int integer(YamlDocument document, String route, int def) { return document.getInt(route, def); }
    private static long lng(YamlDocument document, String route, long def) { return document.getLong(route, def); }
    private static double dbl(YamlDocument document, String route, double def) { return document.getDouble(route, def); }
    private static float flt(YamlDocument document, String route, float def) { return document.getFloat(route, def); }
}
