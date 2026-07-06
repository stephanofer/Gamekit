package network.hera.gamekit.velocity.runtime;

import java.util.ArrayList;
import java.util.List;

final class VelocityRuntimeResources implements AutoCloseable {

    private final List<AutoCloseable> resources = new ArrayList<>();
    private boolean closed;

    <T extends AutoCloseable> T add(T resource) {
        if (this.closed) {
            throw new IllegalStateException("Velocity runtime resources are already closed.");
        }
        this.resources.add(resource);
        return resource;
    }

    @Override
    public void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        RuntimeException failure = null;
        for (int index = this.resources.size() - 1; index >= 0; index--) {
            try {
                this.resources.get(index).close();
            } catch (Exception exception) {
                if (failure == null) {
                    failure = new RuntimeException("Failed to close Velocity runtime resources.", exception);
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
