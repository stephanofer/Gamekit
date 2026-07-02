package network.hera.gamekit.arena;

public enum ArenaState {
    AVAILABLE,
    RESERVED,
    WAITING_ROOM,
    PLAYING,
    RESETTING,
    DISABLED;

    public boolean reservable() {
        return this == AVAILABLE;
    }
}
