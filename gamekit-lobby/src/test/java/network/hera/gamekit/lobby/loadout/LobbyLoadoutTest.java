package network.hera.gamekit.lobby.loadout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

final class LobbyLoadoutTest {

    @Test
    void rejectsDuplicateSlots() {
        LobbyItemDefinition first = item("play", 0);
        LobbyItemDefinition second = item("cosmetics", 0);

        assertThrows(IllegalArgumentException.class, () -> new LobbyLoadout(List.of(first, second)));
    }

    @Test
    void resolvesFallbackLanguage() {
        LobbyItemDefinition item = item("play", 0);

        assertEquals("Play", item.textFor("fr", "en").name());
    }

    @Test
    void normalizesActionType() {
        LobbyActionDefinition action = new LobbyActionDefinition(" Join_Casual_Queue ", Map.of());

        assertEquals("join_casual_queue", action.type());
    }

    private static LobbyItemDefinition item(String id, int slot) {
        return new LobbyItemDefinition(
            id,
            slot,
            "COMPASS",
            Map.of("en", new LocalizedLobbyItemText("Play", List.of("Right click"))),
            Map.of()
        );
    }
}
