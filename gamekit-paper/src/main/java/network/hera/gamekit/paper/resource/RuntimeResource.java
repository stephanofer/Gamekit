package network.hera.gamekit.paper.resource;

public interface RuntimeResource extends AutoCloseable {

    @Override
    void close();

    static RuntimeResource noop() {
        return () -> {
        };
    }
}
