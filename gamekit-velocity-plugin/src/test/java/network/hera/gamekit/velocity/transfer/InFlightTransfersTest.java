package network.hera.gamekit.velocity.transfer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import network.hera.gamekit.core.id.PlayerId;
import network.hera.gamekit.core.id.ServerId;
import network.hera.gamekit.network.admission.AdmissionRequestId;
import network.hera.gamekit.network.transfer.TransferRequest;
import org.junit.jupiter.api.Test;

class InFlightTransfersTest {

    private static final Instant NOW = Instant.parse("2026-07-06T20:00:00Z");
    private static final PlayerId PLAYER = PlayerId.of(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final ServerId TARGET = ServerId.of("bedwars-arena-01");
    private static final AdmissionRequestId ADMISSION = AdmissionRequestId.parse("00000000-0000-0000-0000-000000000002");

    @Test
    void forgetRemovesInFlightTransferBeforeTtl() {
        InFlightTransfers transfers = new InFlightTransfers(Duration.ofSeconds(15));

        transfers.remember(request(), NOW);
        assertTrue(transfers.matches(PLAYER, TARGET, NOW.plusSeconds(1)));

        transfers.forget(ADMISSION);

        assertFalse(transfers.matches(PLAYER, TARGET, NOW.plusSeconds(2)));
    }

    @Test
    void expiredInFlightTransferDoesNotMatch() {
        InFlightTransfers transfers = new InFlightTransfers(Duration.ofSeconds(15));

        transfers.remember(request(), NOW);

        assertFalse(transfers.matches(PLAYER, TARGET, NOW.plusSeconds(16)));
    }

    private static TransferRequest request() {
        return new TransferRequest(PLAYER, TARGET, ADMISSION, NOW);
    }
}
