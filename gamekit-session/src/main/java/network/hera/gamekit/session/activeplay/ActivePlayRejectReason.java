package network.hera.gamekit.session.activeplay;

public enum ActivePlayRejectReason {
    ALREADY_IN_QUEUE,
    ALREADY_IN_WAITING_ROOM,
    ALREADY_IN_MATCH,
    ALREADY_SPECTATING,
    RECONNECTABLE_MATCH_EXISTS
}
