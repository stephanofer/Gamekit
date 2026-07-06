package network.hera.gamekit.velocity.transfer;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import network.hera.gamekit.core.result.Decision;
import network.hera.gamekit.network.admission.AdmissionRequest;
import network.hera.gamekit.network.admission.AdmissionStore;
import network.hera.gamekit.network.transfer.TransferRejectReason;
import network.hera.gamekit.network.transfer.TransferRequest;
import org.jetbrains.annotations.NotNull;

public final class AdmissionTransferValidator {

    private final AdmissionStore admissionStore;

    public AdmissionTransferValidator(@NotNull AdmissionStore admissionStore) {
        this.admissionStore = Objects.requireNonNull(admissionStore, "admissionStore");
    }

    public @NotNull CompletableFuture<Decision<TransferRejectReason>> validate(@NotNull TransferRequest request, @NotNull Instant now) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(now, "now");
        return this.admissionStore.find(request.admissionRequestId()).thenApply(admission -> {
            if (admission.isEmpty()) {
                return Decision.reject(TransferRejectReason.ADMISSION_NOT_FOUND);
            }
            return validate(request, admission.get(), now);
        }).exceptionally(ignored -> Decision.reject(TransferRejectReason.REDIS_UNAVAILABLE));
    }

    private @NotNull Decision<TransferRejectReason> validate(
            @NotNull TransferRequest request,
            @NotNull AdmissionRequest admission,
            @NotNull Instant now
    ) {
        if (admission.expiredAt(now)) {
            return Decision.reject(TransferRejectReason.ADMISSION_EXPIRED);
        }
        if (!admission.playerId().equals(request.playerId())) {
            return Decision.reject(TransferRejectReason.ADMISSION_PLAYER_MISMATCH);
        }
        if (!admission.targetServerId().equals(request.targetServerId())) {
            return Decision.reject(TransferRejectReason.ADMISSION_TARGET_MISMATCH);
        }
        return Decision.accept();
    }
}
