package network.hera.gamekit.network.routing;

public enum RoutingRejectReason {
    NO_SERVER_AVAILABLE,
    TARGET_SERVER_NOT_AVAILABLE,
    MATCH_LOCATION_NOT_FOUND,
    MATCH_LOCATION_MISMATCH,
    MATCH_LOCATION_CLOSED,
    ADMISSION_CREATION_FAILED
}
