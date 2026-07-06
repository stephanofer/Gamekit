package network.hera.gamekit.infra.craftkit.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import network.hera.gamekit.network.transfer.TransferRejectReason;
import org.junit.jupiter.api.Test;

class CraftKitRedisTransferGatewayTest {

    @Test
    void acceptsWhenRedisReportsSubscribers() {
        assertTrue(CraftKitRedisTransferGateway.decisionFromSubscribers(1).accepted());
    }

    @Test
    void rejectsWhenSignalWasNotDeliveredToAnySubscriber() {
        assertEquals(
            TransferRejectReason.TRANSFER_SIGNAL_NOT_DELIVERED,
            CraftKitRedisTransferGateway.decisionFromSubscribers(0).requireRejectReason()
        );
    }
}
