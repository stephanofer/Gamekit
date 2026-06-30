package network.hera.gamekit.testkit.fixture;

import java.time.Instant;
import java.util.UUID;
import network.hera.gamekit.core.definition.MatchKind;
import network.hera.gamekit.core.definition.MatchVariant;
import network.hera.gamekit.core.definition.TeamSpec;
import network.hera.gamekit.core.id.GameId;
import network.hera.gamekit.core.id.PlayerId;
import network.hera.gamekit.core.id.QueueId;
import network.hera.gamekit.core.id.ServerId;
import network.hera.gamekit.core.id.VariantId;
import org.jetbrains.annotations.NotNull;

public final class GameKitFixtures {

    public static final Instant BASE_TIME = Instant.parse("2026-01-01T00:00:00Z");

    private GameKitFixtures() {
    }

    public static @NotNull GameId bedwars() {
        return GameId.of("bedwars");
    }

    public static @NotNull VariantId casual2v2() {
        return VariantId.of("casual_2v2");
    }

    public static @NotNull VariantId ranked2v2() {
        return VariantId.of("ranked_2v2");
    }

    public static @NotNull QueueId bedwarsCasual2v2Queue() {
        return QueueId.of(bedwars(), casual2v2());
    }

    public static @NotNull PlayerId playerOne() {
        return PlayerId.of(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    }

    public static @NotNull PlayerId playerTwo() {
        return PlayerId.of(UUID.fromString("00000000-0000-0000-0000-000000000002"));
    }

    public static @NotNull ServerId bedwarsLobby01() {
        return ServerId.of("bedwars-lobby-01");
    }

    public static @NotNull ServerId bedwarsArena01() {
        return ServerId.of("bedwars-arena-01");
    }

    public static @NotNull MatchVariant casual2v2Variant() {
        return MatchVariant.of(bedwars(), casual2v2(), MatchKind.CASUAL, TeamSpec.of(2, 2));
    }
}
