package network.hera.gamekit.network.registry;

public enum ServerState {
    ONLINE,
    DRAINING,
    FULL,
    OFFLINE,
    UNKNOWN;

    public boolean acceptsNewWork() {
        return this == ONLINE;
    }

    public boolean acceptsExistingMatchRouting() {
        return this == ONLINE || this == DRAINING;
    }
}
