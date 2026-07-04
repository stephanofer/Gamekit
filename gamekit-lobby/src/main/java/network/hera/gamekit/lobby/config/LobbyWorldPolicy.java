package network.hera.gamekit.lobby.config;

public record LobbyWorldPolicy(
        boolean fixedTimeEnabled,
        long fixedTimeValue,
        boolean clearWeatherEnabled
) {

    public LobbyWorldPolicy {
        if (fixedTimeValue < 0L || fixedTimeValue > 24000L) {
            throw new IllegalArgumentException("world.fixed-time.value must be between 0 and 24000.");
        }
    }
}
