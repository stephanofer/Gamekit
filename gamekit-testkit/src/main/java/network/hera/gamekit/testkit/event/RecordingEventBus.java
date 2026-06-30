package network.hera.gamekit.testkit.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import network.hera.gamekit.core.event.GameKitEvent;
import network.hera.gamekit.core.event.GameKitEventBus;
import org.jetbrains.annotations.NotNull;

public final class RecordingEventBus implements GameKitEventBus {

    private final List<GameKitEvent> events = new ArrayList<>();

    @Override
    public void publish(@NotNull GameKitEvent event) {
        this.events.add(Objects.requireNonNull(event, "event"));
    }

    public @NotNull List<GameKitEvent> events() {
        return Collections.unmodifiableList(this.events);
    }

    public <T extends GameKitEvent> @NotNull List<T> eventsOfType(@NotNull Class<T> eventType) {
        return this.events.stream()
                .filter(eventType::isInstance)
                .map(eventType::cast)
                .toList();
    }

    public void clear() {
        this.events.clear();
    }
}
