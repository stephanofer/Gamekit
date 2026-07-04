package network.hera.gamekit.paper.world;

import java.util.Objects;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public final class PaperWorldOperations {

    public void applyFixedTime(@NotNull World world, long time) {
        Objects.requireNonNull(world, "world").setTime(time);
    }

    public void applyClearWeather(@NotNull World world) {
        Objects.requireNonNull(world, "world").setStorm(false);
        world.setThundering(false);
        world.setClearWeatherDuration(20 * 60 * 10);
    }

}
