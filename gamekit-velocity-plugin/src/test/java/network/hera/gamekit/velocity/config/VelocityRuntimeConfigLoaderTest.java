package network.hera.gamekit.velocity.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.dejvokep.boostedyaml.YamlDocument;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import network.hera.gamekit.core.id.ServerId;
import org.junit.jupiter.api.Test;

class VelocityRuntimeConfigLoaderTest {

    @Test
    void parsesVelocityRuntimeConfig() throws IOException {
        YamlDocument document = YamlDocument.create(new ByteArrayInputStream("""
            redis:
              host: "redis.internal"
              port: 6380
              database: 2
              username: "gamekit"
              password: "secret"
              ssl: true
              verify-peer: false
              key-prefix: "hera"
              environment: "prod"
              server-id: "velocity-01"
            routing:
              strict-gamekit-server-validation: true
              protected-server-ids:
                - "bedwars-arena-01"
              transfer-timeout-ms: 2500
              inflight-ttl-seconds: 20
            fallback:
              default-lobby-server-id: "global-lobby-01"
              disconnect-if-no-fallback: false
            """.getBytes(StandardCharsets.UTF_8)));

        VelocityRuntimeConfig config = VelocityRuntimeConfigLoader.parse(document);

        assertEquals("redis.internal", config.redis().host());
        assertEquals(6380, config.redis().port());
        assertEquals(2, config.redis().database());
        assertEquals("gamekit", config.redis().username());
        assertEquals("secret", config.redis().password());
        assertTrue(config.redis().ssl());
        assertFalse(config.redis().verifyPeer());
        assertEquals("hera", config.redis().keyPrefix());
        assertEquals("prod", config.redis().environment());
        assertEquals("velocity-01", config.redis().serverId());
        assertTrue(config.routing().strictGameKitServerValidation());
        assertEquals(List.of(ServerId.of("bedwars-arena-01")), config.routing().protectedServerIds());
        assertEquals(Duration.ofMillis(2500), config.routing().transferTimeout());
        assertEquals(Duration.ofSeconds(20), config.routing().inflightTtl());
        assertEquals(ServerId.of("global-lobby-01"), config.fallback().defaultLobbyServerId());
        assertFalse(config.fallback().disconnectIfNoFallback());
    }
}
