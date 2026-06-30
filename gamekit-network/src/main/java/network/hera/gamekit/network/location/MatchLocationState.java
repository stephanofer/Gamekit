package network.hera.gamekit.network.location;

public enum MatchLocationState {
    STARTING,
    RUNNING,
    ENDING,
    CLOSED;

    public boolean routable() {
        return this == STARTING || this == RUNNING || this == ENDING;
    }
}
