package network.hera.gamekit.velocity.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import network.hera.gamekit.core.id.ServerId;
import org.jetbrains.annotations.NotNull;

public final class VelocityRuntimeConfigLoader {

    private static final String DEFAULT_CONFIG = """
        redis:
          host: "127.0.0.1"
          port: 6379
          database: 0
          username: ""
          password: ""
          ssl: false
          verify-peer: true
          key-prefix: "hera"
          environment: "dev"
          server-id: "velocity-01"

        routing:
          strict-gamekit-server-validation: false
          protected-server-ids: []
          transfer-timeout-ms: 5000
          inflight-ttl-seconds: 15

        fallback:
          default-lobby-server-id: ""
          disconnect-if-no-fallback: true
        """;

    private VelocityRuntimeConfigLoader() {
    }

    public static @NotNull VelocityRuntimeConfig load(@NotNull Path dataDirectory) throws IOException {
        Files.createDirectories(dataDirectory);
        final Path configPath = dataDirectory.resolve("config.yml");
        if (Files.notExists(configPath)) {
            Files.writeString(configPath, DEFAULT_CONFIG);
        }
        return parse(YamlDocument.create(configPath.toFile()));
    }

    public static @NotNull VelocityRuntimeConfig parse(@NotNull YamlDocument document) {
        final VelocityRuntimeConfig.RedisSettings redis = new VelocityRuntimeConfig.RedisSettings(
            string(document, "redis.host", "127.0.0.1"),
            integer(document, "redis.port", 6379),
            integer(document, "redis.database", 0),
            string(document, "redis.username", ""),
            string(document, "redis.password", ""),
            bool(document, "redis.ssl", false),
            bool(document, "redis.verify-peer", true),
            string(document, "redis.key-prefix", "hera"),
            string(document, "redis.environment", "dev"),
            string(document, "redis.server-id", "velocity-01")
        );

        final List<ServerId> protectedServers = document.getStringList("routing.protected-server-ids", List.of()).stream()
            .filter(value -> !value.isBlank())
            .map(ServerId::of)
            .toList();

        final VelocityRuntimeConfig.RoutingSettings routing = new VelocityRuntimeConfig.RoutingSettings(
            bool(document, "routing.strict-gamekit-server-validation", false),
            protectedServers,
            Duration.ofMillis(lng(document, "routing.transfer-timeout-ms", 5000L)),
            Duration.ofSeconds(lng(document, "routing.inflight-ttl-seconds", 15L))
        );

        final String fallbackServer = string(document, "fallback.default-lobby-server-id", "");
        final VelocityRuntimeConfig.FallbackSettings fallback = new VelocityRuntimeConfig.FallbackSettings(
            fallbackServer.isBlank() ? null : ServerId.of(fallbackServer),
            bool(document, "fallback.disconnect-if-no-fallback", true)
        );

        return new VelocityRuntimeConfig(redis, routing, fallback);
    }

    private static String string(YamlDocument document, String route, String def) { return document.getString(route, def); }

    private static boolean bool(YamlDocument document, String route, boolean def) { return Boolean.TRUE.equals(document.getBoolean(route, def)); }

    private static int integer(YamlDocument document, String route, int def) { return document.getInt(route, def); }

    private static long lng(YamlDocument document, String route, long def) { return document.getLong(route, def); }
}
