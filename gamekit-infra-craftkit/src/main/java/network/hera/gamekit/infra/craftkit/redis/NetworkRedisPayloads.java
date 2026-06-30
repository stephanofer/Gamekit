package network.hera.gamekit.infra.craftkit.redis;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import network.hera.gamekit.core.id.ArenaId;
import network.hera.gamekit.core.id.GameId;
import network.hera.gamekit.core.id.MatchId;
import network.hera.gamekit.core.id.PlayerId;
import network.hera.gamekit.core.id.ServerId;
import network.hera.gamekit.core.id.VariantId;
import network.hera.gamekit.network.admission.AdmissionRequest;
import network.hera.gamekit.network.admission.AdmissionRequestId;
import network.hera.gamekit.network.admission.AdmissionType;
import network.hera.gamekit.network.admission.WaitingRoomId;
import network.hera.gamekit.network.location.MatchLocation;
import network.hera.gamekit.network.location.MatchLocationState;
import network.hera.gamekit.network.registry.RegisteredServer;
import network.hera.gamekit.network.registry.ServerRole;
import network.hera.gamekit.network.registry.ServerState;

final class NetworkRedisPayloads {

    private static final String EMPTY = "-";

    private NetworkRedisPayloads() {
    }

    static String admission(final AdmissionRequest request) {
        return join(Map.ofEntries(
            Map.entry("id", request.id().toString()),
            Map.entry("player", request.playerId().toString()),
            Map.entry("type", request.type().name()),
            Map.entry("target", request.targetServerId().toString()),
            Map.entry("game", request.gameId().toString()),
            Map.entry("variant", optional(request.variantId())),
            Map.entry("arena", optional(request.arenaId())),
            Map.entry("room", optional(request.waitingRoomId())),
            Map.entry("match", optional(request.matchId())),
            Map.entry("created", Long.toString(request.createdAt().toEpochMilli())),
            Map.entry("expires", Long.toString(request.expiresAt().toEpochMilli()))
        ));
    }

    static AdmissionRequest admission(final String payload) {
        final Map<String, String> values = split(payload);
        return AdmissionRequest.builder()
            .id(AdmissionRequestId.parse(values.get("id")))
            .playerId(PlayerId.of(java.util.UUID.fromString(values.get("player"))))
            .type(AdmissionType.valueOf(values.get("type")))
            .targetServerId(ServerId.of(values.get("target")))
            .gameId(GameId.of(values.get("game")))
            .variantId(optional(values.get("variant"), VariantId::of))
            .arenaId(optional(values.get("arena"), ArenaId::of))
            .waitingRoomId(optional(values.get("room"), WaitingRoomId::of))
            .matchId(optional(values.get("match"), value -> MatchId.of(java.util.UUID.fromString(value))))
            .createdAt(Instant.ofEpochMilli(Long.parseLong(values.get("created"))))
            .expiresAt(Instant.ofEpochMilli(Long.parseLong(values.get("expires"))))
            .build();
    }

    static String server(final RegisteredServer server) {
        return join(Map.ofEntries(
            Map.entry("id", server.serverId().toString()),
            Map.entry("game", server.gameId().toString()),
            Map.entry("role", server.role().name()),
            Map.entry("state", server.state().name()),
            Map.entry("capacity", Integer.toString(server.capacity())),
            Map.entry("online", Integer.toString(server.onlinePlayers())),
            Map.entry("heartbeat", Long.toString(server.lastHeartbeatAt().toEpochMilli())),
            Map.entry("ttl", Long.toString(server.heartbeatTtl().toMillis()))
        ));
    }

    static RegisteredServer server(final String payload) {
        final Map<String, String> values = split(payload);
        return new RegisteredServer(
            ServerId.of(values.get("id")),
            GameId.of(values.get("game")),
            ServerRole.valueOf(values.get("role")),
            ServerState.valueOf(values.get("state")),
            Integer.parseInt(values.get("capacity")),
            Integer.parseInt(values.get("online")),
            Instant.ofEpochMilli(Long.parseLong(values.get("heartbeat"))),
            Duration.ofMillis(Long.parseLong(values.get("ttl")))
        );
    }

    static String matchLocation(final MatchLocation location) {
        return join(Map.ofEntries(
            Map.entry("match", location.matchId().toString()),
            Map.entry("game", location.gameId().toString()),
            Map.entry("variant", location.variantId().toString()),
            Map.entry("server", location.serverId().toString()),
            Map.entry("arena", location.arenaId().toString()),
            Map.entry("state", location.state().name())
        ));
    }

    static MatchLocation matchLocation(final String payload) {
        final Map<String, String> values = split(payload);
        return new MatchLocation(
            MatchId.of(java.util.UUID.fromString(values.get("match"))),
            GameId.of(values.get("game")),
            VariantId.of(values.get("variant")),
            ServerId.of(values.get("server")),
            ArenaId.of(values.get("arena")),
            MatchLocationState.valueOf(values.get("state"))
        );
    }

    private static String join(final Map<String, String> values) {
        final StringJoiner joiner = new StringJoiner(";");
        values.forEach((key, value) -> joiner.add(key + '=' + encode(value)));
        return joiner.toString();
    }

    private static Map<String, String> split(final String payload) {
        final Map<String, String> values = new LinkedHashMap<>();
        for (final String part : Objects.requireNonNull(payload, "payload").split(";")) {
            final int separator = part.indexOf('=');
            if (separator < 1) {
                throw new IllegalArgumentException("Invalid Redis payload part: " + part);
            }
            values.put(part.substring(0, separator), decode(part.substring(separator + 1)));
        }
        return values;
    }

    private static String optional(final Object value) {
        return value == null ? EMPTY : value.toString();
    }

    private static <T> T optional(final String value, final Parser<T> parser) {
        if (value == null || EMPTY.equals(value)) {
            return null;
        }
        return parser.parse(value);
    }

    private static String encode(final String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(Objects.requireNonNull(value, "value").getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(final String value) {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }

    @FunctionalInterface
    private interface Parser<T> {
        T parse(String value);
    }
}
