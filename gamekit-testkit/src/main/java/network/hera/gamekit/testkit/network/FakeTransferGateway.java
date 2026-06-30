package network.hera.gamekit.testkit.network;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import network.hera.gamekit.core.id.PlayerId;
import network.hera.gamekit.core.id.ServerId;
import network.hera.gamekit.core.result.Decision;
import network.hera.gamekit.network.transfer.TransferGateway;
import network.hera.gamekit.network.transfer.TransferRejectReason;
import org.jetbrains.annotations.NotNull;

public final class FakeTransferGateway implements TransferGateway {

    private final List<Transfer> transfers = new ArrayList<>();
    private Decision<TransferRejectReason> nextDecision = Decision.accept();

    @Override
    public @NotNull CompletableFuture<Decision<TransferRejectReason>> transfer(@NotNull PlayerId playerId, @NotNull ServerId targetServerId) {
        final Decision<TransferRejectReason> decision = this.nextDecision;
        if (decision.accepted()) {
            this.transfers.add(new Transfer(playerId, targetServerId));
        }
        this.nextDecision = Decision.accept();
        return CompletableFuture.completedFuture(decision);
    }

    public void rejectNext(@NotNull TransferRejectReason reason) {
        this.nextDecision = Decision.reject(reason);
    }

    public @NotNull List<Transfer> transfers() {
        return List.copyOf(this.transfers);
    }

    public record Transfer(@NotNull PlayerId playerId, @NotNull ServerId targetServerId) {
    }
}
