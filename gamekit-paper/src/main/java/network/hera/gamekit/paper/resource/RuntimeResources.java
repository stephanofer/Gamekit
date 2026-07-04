package network.hera.gamekit.paper.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class RuntimeResources implements RuntimeResource {

    private final List<RuntimeResource> resources = new ArrayList<>();
    private boolean closed;

    private RuntimeResources() {
    }

    public static @NotNull RuntimeResources create() {
        return new RuntimeResources();
    }

    public synchronized <T extends RuntimeResource> @NotNull T add(@NotNull T resource) {
        Objects.requireNonNull(resource, "resource");
        if (this.closed) {
            resource.close();
            throw new IllegalStateException("RuntimeResources is already closed.");
        }
        this.resources.add(resource);
        return resource;
    }

    @Override
    public synchronized void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        RuntimeException failure = null;
        for (int index = this.resources.size() - 1; index >= 0; index--) {
            try {
                this.resources.get(index).close();
            } catch (RuntimeException exception) {
                if (failure == null) {
                    failure = exception;
                } else {
                    failure.addSuppressed(exception);
                }
            }
        }
        this.resources.clear();
        if (failure != null) {
            throw failure;
        }
    }
}
