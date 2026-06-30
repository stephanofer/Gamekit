package network.hera.gamekit.core.definition;

import network.hera.gamekit.core.error.InvalidGameKitDefinitionException;
import org.jetbrains.annotations.NotNull;

public record TeamSpec(int teamCount, int playersPerTeam) {

    public TeamSpec {
        if (teamCount <= 0) {
            throw new InvalidGameKitDefinitionException("TeamSpec", "teamCount must be greater than zero");
        }
        if (playersPerTeam <= 0) {
            throw new InvalidGameKitDefinitionException("TeamSpec", "playersPerTeam must be greater than zero");
        }
    }

    public static @NotNull TeamSpec of(int teamCount, int playersPerTeam) {
        return new TeamSpec(teamCount, playersPerTeam);
    }

    public int maxPlayers() {
        return this.teamCount * this.playersPerTeam;
    }
}
