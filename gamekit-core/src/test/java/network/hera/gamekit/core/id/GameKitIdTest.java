package network.hera.gamekit.core.id;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import network.hera.gamekit.core.error.InvalidGameKitIdException;
import org.junit.jupiter.api.Test;

class GameKitIdTest {

    @Test
    void acceptsValidDomainIds() {
        assertEquals("bedwars", GameId.of("bedwars").value());
        assertEquals("ranked_2v2", VariantId.of("ranked_2v2").value());
        assertEquals("lighthouse_01", ArenaId.of("lighthouse_01").value());
    }

    @Test
    void rejectsInvalidDomainIds() {
        assertThrows(InvalidGameKitIdException.class, () -> GameId.of("BedWars"));
        assertThrows(InvalidGameKitIdException.class, () -> VariantId.of("ranked-2v2"));
        assertThrows(InvalidGameKitIdException.class, () -> ArenaId.of("lighthouse 01"));
        assertThrows(InvalidGameKitIdException.class, () -> GameId.of("bedwars "));
        assertThrows(InvalidGameKitIdException.class, () -> GameId.of(null));
    }

    @Test
    void queueIdComposesGameAndVariant() {
        QueueId queueId = QueueId.of(GameId.of("bedwars"), VariantId.of("ranked_2v2"));

        assertEquals("bedwars:ranked_2v2", queueId.toString());
    }

    @Test
    void acceptsServerIdAsKebabCase() {
        assertEquals("bedwars-arena-01", ServerId.of("bedwars-arena-01").value());
    }

    @Test
    void rejectsInvalidServerId() {
        assertThrows(InvalidGameKitIdException.class, () -> ServerId.of("bedwars_arena_01"));
    }

    @Test
    void playerIdWrapsUuidOnly() {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        assertEquals(uuid, PlayerId.of(uuid).value());
    }
}
