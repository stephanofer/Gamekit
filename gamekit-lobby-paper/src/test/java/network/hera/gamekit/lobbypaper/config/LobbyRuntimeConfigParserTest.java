package network.hera.gamekit.lobbypaper.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.dejvokep.boostedyaml.YamlDocument;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import network.hera.gamekit.lobby.loadout.LobbyLoadout;
import org.junit.jupiter.api.Test;

final class LobbyRuntimeConfigParserTest {

    @Test
    void parsesSingleLobbyYaml() throws IOException {
        YamlDocument document = document(validYaml());

        var config = LobbyRuntimeConfigParser.parseConfig(document);
        LobbyLoadout loadout = LobbyRuntimeConfigParser.parseLoadout(document);

        assertEquals("en", config.defaultLanguage());
        assertEquals("bedwars_lobby", config.spawn().world());
        assertEquals(1, loadout.items().size());
        assertEquals("play_casual_2v2", loadout.findBySlot(0).orElseThrow().id());
    }

    @Test
    void rejectsDuplicateItemSlots() throws IOException {
        YamlDocument document = document(validYaml() + "\n  cosmetics:\n    slot: 0\n    material: CHEST\n    text:\n      en:\n        name: Cosmetics\n        lore: []\n");

        assertThrows(IllegalArgumentException.class, () -> LobbyRuntimeConfigParser.parseLoadout(document));
    }

    private static YamlDocument document(String yaml) throws IOException {
        return YamlDocument.create(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
    }

    private static String validYaml() {
        return """
            config-version: 1
            runtime:
              enabled: true
              default-language: en
            spawn:
              world: bedwars_lobby
              x: 0.5
              y: 80.0
              z: 0.5
              yaw: 180
              pitch: 0
            items:
              play_casual_2v2:
                slot: 0
                material: COMPASS
                text:
                  en:
                    name: Play Casual 2v2
                    lore:
                      - Right click to join.
                actions:
                  right:
                    type: join_casual_queue
                    queue: bedwars:casual_2v2
            """;
    }
}
