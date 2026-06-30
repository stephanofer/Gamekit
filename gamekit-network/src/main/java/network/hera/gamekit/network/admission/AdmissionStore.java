package network.hera.gamekit.network.admission;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

public interface AdmissionStore {

    @NotNull CompletableFuture<Void> save(@NotNull AdmissionRequest request);

    @NotNull CompletableFuture<Optional<AdmissionRequest>> find(@NotNull AdmissionRequestId requestId);

    @NotNull CompletableFuture<Optional<AdmissionRequest>> consume(@NotNull AdmissionRequestId requestId);
}
