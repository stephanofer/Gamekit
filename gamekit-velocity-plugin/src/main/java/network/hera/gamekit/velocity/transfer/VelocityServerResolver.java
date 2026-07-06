package network.hera.gamekit.velocity.transfer;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import java.util.Objects;
import java.util.Optional;
import network.hera.gamekit.core.id.ServerId;
import org.jetbrains.annotations.NotNull;

public final class VelocityServerResolver {

    private final ProxyServer proxy;

    public VelocityServerResolver(@NotNull ProxyServer proxy) {
        this.proxy = Objects.requireNonNull(proxy, "proxy");
    }

    public @NotNull Optional<RegisteredServer> find(@NotNull ServerId serverId) {
        return this.proxy.getServer(Objects.requireNonNull(serverId, "serverId").toString());
    }
}
