package network.hera.gamekit.velocity.transfer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import network.hera.gamekit.network.transfer.TransferRejectReason;
import org.junit.jupiter.api.Test;

class ConnectionStatusMapperTest {

    @Test
    void acceptsSuccessfulVelocityStatuses() {
        assertTrue(ConnectionStatusMapper.accepted(ConnectionRequestBuilder.Status.SUCCESS));
        assertTrue(ConnectionStatusMapper.accepted(ConnectionRequestBuilder.Status.ALREADY_CONNECTED));
    }

    @Test
    void mapsRejectedVelocityStatuses() {
        assertFalse(ConnectionStatusMapper.accepted(ConnectionRequestBuilder.Status.CONNECTION_IN_PROGRESS));
        assertEquals(TransferRejectReason.CONNECTION_IN_PROGRESS, ConnectionStatusMapper.rejectReason(ConnectionRequestBuilder.Status.CONNECTION_IN_PROGRESS));
        assertEquals(TransferRejectReason.TRANSFER_CANCELLED, ConnectionStatusMapper.rejectReason(ConnectionRequestBuilder.Status.CONNECTION_CANCELLED));
        assertEquals(TransferRejectReason.TARGET_SERVER_UNAVAILABLE, ConnectionStatusMapper.rejectReason(ConnectionRequestBuilder.Status.SERVER_DISCONNECTED));
    }
}
