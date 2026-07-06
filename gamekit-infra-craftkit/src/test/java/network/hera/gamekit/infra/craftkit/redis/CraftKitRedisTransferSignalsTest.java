package network.hera.gamekit.infra.craftkit.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.UUID;
import network.hera.gamekit.core.id.PlayerId;
import network.hera.gamekit.core.id.ServerId;
import network.hera.gamekit.network.admission.AdmissionRequestId;
import network.hera.gamekit.network.transfer.TransferRequest;
import org.junit.jupiter.api.Test;

class CraftKitRedisTransferSignalsTest {

    @Test
    void roundTripsTransferRequestPayload() {
        TransferRequest request = new TransferRequest(
            PlayerId.of(UUID.fromString("00000000-0000-0000-0000-000000000001")),
            ServerId.of("bedwars-arena-01"),
            AdmissionRequestId.parse("00000000-0000-0000-0000-000000000002"),
            Instant.parse("2026-07-06T20:00:00Z")
        );

        TransferRequest decoded = CraftKitRedisTransferSignals.decode(CraftKitRedisTransferSignals.encode(request));

        assertEquals(request, decoded);
    }
}
