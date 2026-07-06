package network.hera.gamekit.network.transfer;

import java.time.Instant;
import java.util.Objects;
import network.hera.gamekit.core.id.PlayerId;
import network.hera.gamekit.core.id.ServerId;
import network.hera.gamekit.network.admission.AdmissionRequestId;
import org.jetbrains.annotations.NotNull;

public record TransferRequest(
        @NotNull PlayerId playerId,
        @NotNull ServerId targetServerId,
        @NotNull AdmissionRequestId admissionRequestId,
        @NotNull Instant requestedAt
) {

    public TransferRequest {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(targetServerId, "targetServerId");
        Objects.requireNonNull(admissionRequestId, "admissionRequestId");
        Objects.requireNonNull(requestedAt, "requestedAt");
    }
}
