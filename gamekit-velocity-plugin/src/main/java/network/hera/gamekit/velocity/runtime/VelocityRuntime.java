package network.hera.gamekit.velocity.runtime;

import com.hera.craftkit.redis.RedisClient;
import com.hera.craftkit.redis.RedisConfig;
import com.hera.craftkit.redis.RedisClients;
import com.velocitypowered.api.proxy.ProxyServer;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Set;
import network.hera.gamekit.infra.craftkit.redis.CraftKitRedisAdmissionStore;
import network.hera.gamekit.velocity.config.VelocityRuntimeConfig;
import network.hera.gamekit.velocity.config.VelocityRuntimeConfigLoader;
import network.hera.gamekit.velocity.fallback.VelocityFallbackHandler;
import network.hera.gamekit.velocity.listener.VelocityConnectionListener;
import network.hera.gamekit.velocity.transfer.AdmissionTransferValidator;
import network.hera.gamekit.velocity.transfer.InFlightTransfers;
import network.hera.gamekit.velocity.transfer.VelocityServerResolver;
import network.hera.gamekit.velocity.transfer.VelocityTransferExecutor;
import network.hera.gamekit.velocity.transfer.VelocityTransferSubscriber;
import org.slf4j.Logger;

public final class VelocityRuntime implements AutoCloseable {

    private final VelocityRuntimeResources resources;
    private final Logger logger;

    private VelocityRuntime(VelocityRuntimeResources resources, Logger logger) {
        this.resources = resources;
        this.logger = logger;
    }

    public static VelocityRuntime start(Object plugin, ProxyServer proxy, Logger logger, Path dataDirectory) {
        final VelocityRuntimeResources resources = new VelocityRuntimeResources();
        try {
            final VelocityRuntimeConfig config = VelocityRuntimeConfigLoader.load(dataDirectory);
            final RedisClient redis = resources.add(RedisClients.lettuce(redisConfig(config.redis())));
            final Clock clock = Clock.systemUTC();
            final CraftKitRedisAdmissionStore admissionStore = new CraftKitRedisAdmissionStore(redis, clock::instant);
            final VelocityServerResolver serverResolver = new VelocityServerResolver(proxy);
            final InFlightTransfers inFlightTransfers = new InFlightTransfers(config.routing().inflightTtl());
            final AdmissionTransferValidator validator = new AdmissionTransferValidator(admissionStore);
            final VelocityTransferExecutor transferExecutor = new VelocityTransferExecutor(
                proxy,
                serverResolver,
                validator,
                inFlightTransfers,
                clock,
                config.routing().transferTimeout().toMillis()
            );

            final VelocityConnectionListener connectionListener = new VelocityConnectionListener(
                inFlightTransfers,
                clock,
                config.routing().strictGameKitServerValidation(),
                Set.copyOf(config.routing().protectedServerIds())
            );
            proxy.getEventManager().register(plugin, connectionListener);
            resources.add(() -> proxy.getEventManager().unregisterListener(plugin, connectionListener));

            final VelocityFallbackHandler fallbackHandler = new VelocityFallbackHandler(serverResolver, inFlightTransfers, clock, config.fallback());
            proxy.getEventManager().register(plugin, fallbackHandler);
            resources.add(() -> proxy.getEventManager().unregisterListener(plugin, fallbackHandler));
            resources.add(new VelocityTransferSubscriber(redis, transferExecutor, logger));

            logger.info("GameKit Velocity runtime started. Redis transfer channel is active.");
            return new VelocityRuntime(resources, logger);
        } catch (Exception exception) {
            try {
                resources.close();
            } catch (RuntimeException closeFailure) {
                exception.addSuppressed(closeFailure);
            }
            throw new IllegalStateException("Failed to start GameKit Velocity runtime.", exception);
        }
    }

    private static RedisConfig redisConfig(VelocityRuntimeConfig.RedisSettings settings) {
        return RedisConfig.builder()
            .host(settings.host())
            .port(settings.port())
            .database(settings.database())
            .username(settings.username())
            .password(settings.password())
            .ssl(settings.ssl())
            .verifyPeer(settings.verifyPeer())
            .keyPrefix(settings.keyPrefix())
            .environment(settings.environment())
            .serverId(settings.serverId())
            .build();
    }

    @Override
    public void close() {
        try {
            this.resources.close();
            this.logger.info("GameKit Velocity runtime stopped.");
        } catch (RuntimeException exception) {
            this.logger.warn("GameKit Velocity runtime stopped with resource close failures.", exception);
        }
    }
}
