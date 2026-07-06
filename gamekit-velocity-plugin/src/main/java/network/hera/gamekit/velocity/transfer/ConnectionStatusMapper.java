package network.hera.gamekit.velocity.transfer;

import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import network.hera.gamekit.network.transfer.TransferRejectReason;
import org.jetbrains.annotations.NotNull;

public final class ConnectionStatusMapper {

    private ConnectionStatusMapper() {
    }

    public static boolean accepted(@NotNull ConnectionRequestBuilder.Status status) {
        return status == ConnectionRequestBuilder.Status.SUCCESS
            || status == ConnectionRequestBuilder.Status.ALREADY_CONNECTED;
    }

    public static @NotNull TransferRejectReason rejectReason(@NotNull ConnectionRequestBuilder.Status status) {
        return switch (status) {
            case CONNECTION_IN_PROGRESS -> TransferRejectReason.CONNECTION_IN_PROGRESS;
            case CONNECTION_CANCELLED -> TransferRejectReason.TRANSFER_CANCELLED;
            case SERVER_DISCONNECTED -> TransferRejectReason.TARGET_SERVER_UNAVAILABLE;
            case SUCCESS, ALREADY_CONNECTED -> throw new IllegalArgumentException("Successful status has no reject reason: " + status);
        };
    }
}
