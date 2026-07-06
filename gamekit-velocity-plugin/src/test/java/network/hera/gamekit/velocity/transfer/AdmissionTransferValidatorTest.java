package network.hera.gamekit.velocity.transfer;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import network.hera.gamekit.core.id.GameId;
import network.hera.gamekit.core.id.PlayerId;
import network.hera.gamekit.core.id.ServerId;
import network.hera.gamekit.core.result.Decision;
import network.hera.gamekit.network.admission.AdmissionRequest;
import network.hera.gamekit.network.admission.AdmissionRequestId;
import network.hera.gamekit.network.admission.AdmissionType;
import network.hera.gamekit.network.transfer.TransferRejectReason;
import network.hera.gamekit.network.transfer.TransferRequest;
import network.hera.gamekit.testkit.network.FakeAdmissionStore;
import network.hera.gamekit.testkit.time.FakeGameKitClock;
import org.junit.jupiter.api.Test;

class AdmissionTransferValidatorTest {

    private static final Instant NOW = Instant.parse("2026-07-06T20:00:00Z");
    private static final PlayerId PLAYER = PlayerId.of(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final ServerId TARGET = ServerId.of("bedwars-arena-01");
    private static final AdmissionRequestId ADMISSION = AdmissionRequestId.parse("00000000-0000-0000-0000-000000000002");

    @Test
    void acceptsMatchingAdmission() {
        FakeAdmissionStore store = storeWithAdmission(admission(PLAYER, TARGET, NOW.plusSeconds(30)));
        AdmissionTransferValidator validator = new AdmissionTransferValidator(store);

        Decision<TransferRejectReason> decision = validator.validate(request(PLAYER, TARGET), NOW).join();

        assertTrue(decision.accepted());
    }

    @Test
    void rejectsMissingAdmission() {
        FakeAdmissionStore store = new FakeAdmissionStore(FakeGameKitClock.fixedAt(NOW));
        AdmissionTransferValidator validator = new AdmissionTransferValidator(store);

        Decision<TransferRejectReason> decision = validator.validate(request(PLAYER, TARGET), NOW).join();

        assertEquals(TransferRejectReason.ADMISSION_NOT_FOUND, decision.requireRejectReason());
    }

    @Test
    void rejectsMismatchedTarget() {
        FakeAdmissionStore store = storeWithAdmission(admission(PLAYER, ServerId.of("bedwars-arena-02"), NOW.plusSeconds(30)));
        AdmissionTransferValidator validator = new AdmissionTransferValidator(store);

        Decision<TransferRejectReason> decision = validator.validate(request(PLAYER, TARGET), NOW).join();

        assertEquals(TransferRejectReason.ADMISSION_TARGET_MISMATCH, decision.requireRejectReason());
    }

    private static FakeAdmissionStore storeWithAdmission(AdmissionRequest admission) {
        FakeAdmissionStore store = new FakeAdmissionStore(FakeGameKitClock.fixedAt(NOW));
        store.save(admission).join();
        return store;
    }

    private static AdmissionRequest admission(PlayerId playerId, ServerId targetServerId, Instant expiresAt) {
        return AdmissionRequest.builder()
            .id(ADMISSION)
            .playerId(playerId)
            .type(AdmissionType.JOIN_WAITING_ROOM)
            .targetServerId(targetServerId)
            .gameId(GameId.of("bedwars"))
            .createdAt(NOW)
            .expiresAt(expiresAt)
            .build();
    }

    private static TransferRequest request(PlayerId playerId, ServerId targetServerId) {
        return new TransferRequest(playerId, targetServerId, ADMISSION, NOW.plus(Duration.ofMillis(1)));
    }
}
