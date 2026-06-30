package network.hera.gamekit.core.definition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import network.hera.gamekit.core.error.InvalidGameKitDefinitionException;
import network.hera.gamekit.core.id.GameId;
import network.hera.gamekit.core.id.VariantId;
import org.junit.jupiter.api.Test;

class GameKitDefinitionTest {

    @Test
    void teamSpecCalculatesMaxPlayers() {
        TeamSpec teamSpec = TeamSpec.of(2, 2);

        assertEquals(4, teamSpec.maxPlayers());
    }

    @Test
    void teamSpecRejectsInvalidCapacity() {
        assertThrows(InvalidGameKitDefinitionException.class, () -> TeamSpec.of(0, 2));
        assertThrows(InvalidGameKitDefinitionException.class, () -> TeamSpec.of(2, 0));
    }

    @Test
    void matchVariantExposesQueueId() {
        MatchVariant variant = MatchVariant.of(
                GameId.of("bedwars"),
                VariantId.of("casual_2v2"),
                MatchKind.CASUAL,
                TeamSpec.of(2, 2)
        );

        assertEquals("bedwars:casual_2v2", variant.queueId().toString());
    }
}
