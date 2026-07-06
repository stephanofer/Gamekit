package network.hera.gamekit.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import java.nio.file.Path;
import network.hera.gamekit.velocity.runtime.VelocityRuntime;
import org.slf4j.Logger;

@Plugin(
    id = "gamekit-velocity",
    name = "GameKit Velocity",
    version = "0.1.0",
    description = "Central Velocity transport plane for GameKit cross-server routing."
)
public final class GameKitVelocityPlugin {

    private final ProxyServer proxy;
    private final Logger logger;
    private final Path dataDirectory;
    private VelocityRuntime runtime;

    @Inject
    public GameKitVelocityPlugin(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        this.runtime = VelocityRuntime.start(this, this.proxy, this.logger, this.dataDirectory);
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (this.runtime != null) {
            this.runtime.close();
            this.runtime = null;
        }
    }
}
