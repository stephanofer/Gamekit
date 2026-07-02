package network.hera.gamekit.infra.craftkit.redis;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import network.hera.gamekit.arena.ArenaSlot;
import network.hera.gamekit.arena.ArenaState;
import network.hera.gamekit.arena.allocation.ArenaReservation;
import network.hera.gamekit.core.id.ArenaId;
import network.hera.gamekit.core.id.GameId;
import network.hera.gamekit.core.id.ServerId;
import network.hera.gamekit.core.id.VariantId;

final class ArenaRedisPayloads {

    private ArenaRedisPayloads() {
    }

    static String slot(final ArenaSlot slot) {
        return join(Map.ofEntries(
            Map.entry("arena", slot.arenaId().toString()),
            Map.entry("template", slot.templateId()),
            Map.entry("game", slot.gameId().toString()),
            Map.entry("server", slot.serverId().toString()),
            Map.entry("world", slot.worldName()),
            Map.entry("state", slot.state().name()),
            Map.entry("tags", String.join(",", slot.tags())),
            Map.entry("max", Integer.toString(slot.maxPlayers())),
            Map.entry("updated", Long.toString(slot.updatedAt().toEpochMilli())),
            Map.entry("ttl", Long.toString(slot.ttl().toMillis()))
        ));
    }

    static ArenaSlot slot(final String payload) {
        final Map<String, String> values = split(payload);
        return new ArenaSlot(
            ArenaId.of(values.get("arena")),
            values.get("template"),
            GameId.of(values.get("game")),
            ServerId.of(values.get("server")),
            values.get("world"),
            ArenaState.valueOf(values.get("state")),
            tags(values.get("tags")),
            Integer.parseInt(values.get("max")),
            Instant.ofEpochMilli(Long.parseLong(values.get("updated"))),
            Duration.ofMillis(Long.parseLong(values.get("ttl")))
        );
    }

    static String reservation(final ArenaReservation reservation) {
        return join(Map.ofEntries(
            Map.entry("arena", reservation.arenaId().toString()),
            Map.entry("server", reservation.serverId().toString()),
            Map.entry("game", reservation.gameId().toString()),
            Map.entry("variant", reservation.variantId().toString()),
            Map.entry("reserved", Long.toString(reservation.reservedAt().toEpochMilli())),
            Map.entry("expires", Long.toString(reservation.expiresAt().toEpochMilli()))
        ));
    }

    static ArenaReservation reservation(final String payload) {
        final Map<String, String> values = split(payload);
        return new ArenaReservation(
            ArenaId.of(values.get("arena")),
            ServerId.of(values.get("server")),
            GameId.of(values.get("game")),
            VariantId.of(values.get("variant")),
            Instant.ofEpochMilli(Long.parseLong(values.get("reserved"))),
            Instant.ofEpochMilli(Long.parseLong(values.get("expires")))
        );
    }

    private static Set<String> tags(final String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(value.split(","))
            .filter(tag -> !tag.isBlank())
            .collect(Collectors.toUnmodifiableSet());
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

    private static String encode(final String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(Objects.requireNonNull(value, "value").getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(final String value) {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
