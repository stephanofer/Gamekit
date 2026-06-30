package network.hera.gamekit.network.routing;

import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import network.hera.gamekit.core.id.ArenaId;
import network.hera.gamekit.core.id.ServerId;
import network.hera.gamekit.network.admission.AdmissionRequest;
import network.hera.gamekit.network.admission.AdmissionRequestId;
import network.hera.gamekit.network.admission.AdmissionStore;
import network.hera.gamekit.network.location.MatchLocation;
import network.hera.gamekit.network.location.MatchLocationRegistry;
import network.hera.gamekit.network.registry.RegisteredServer;
import network.hera.gamekit.network.registry.ServerRegistry;
import org.jetbrains.annotations.NotNull;

public final class DefaultRoutingService implements RoutingService {

    private final ServerRegistry serverRegistry;
    private final AdmissionStore admissionStore;
    private final MatchLocationRegistry matchLocationRegistry;

    public DefaultRoutingService(
            @NotNull ServerRegistry serverRegistry,
            @NotNull AdmissionStore admissionStore,
            @NotNull MatchLocationRegistry matchLocationRegistry
    ) {
        this.serverRegistry = Objects.requireNonNull(serverRegistry, "serverRegistry");
        this.admissionStore = Objects.requireNonNull(admissionStore, "admissionStore");
        this.matchLocationRegistry = Objects.requireNonNull(matchLocationRegistry, "matchLocationRegistry");
    }

    @Override
    public @NotNull CompletableFuture<RoutingDecision> route(@NotNull RoutingRequest request) {
        Objects.requireNonNull(request, "request");
        if (request.matchId() != null) {
            return routeExistingMatch(request);
        }
        if (request.preferredServerId() != null) {
            return routePreferredServer(request, request.preferredServerId());
        }
        return this.serverRegistry.findAvailable(request.gameId(), request.targetRole())
            .thenCompose(servers -> servers.stream()
                .min(Comparator.comparingInt(RegisteredServer::onlinePlayers))
                .map(server -> createAdmission(request, server.serverId()))
                .orElseGet(() -> CompletableFuture.completedFuture(RoutingDecision.reject(RoutingRejectReason.NO_SERVER_AVAILABLE))));
    }

    private @NotNull CompletableFuture<RoutingDecision> routeExistingMatch(@NotNull RoutingRequest request) {
        return this.matchLocationRegistry.find(request.matchId()).thenCompose(location -> {
            if (location.isEmpty()) {
                return CompletableFuture.completedFuture(RoutingDecision.reject(RoutingRejectReason.MATCH_LOCATION_NOT_FOUND));
            }
            final MatchLocation resolvedLocation = location.get();
            if (!resolvedLocation.gameId().equals(request.gameId())
                    || request.variantIdOptional().filter(variantId -> !resolvedLocation.variantId().equals(variantId)).isPresent()) {
                return CompletableFuture.completedFuture(RoutingDecision.reject(RoutingRejectReason.MATCH_LOCATION_MISMATCH));
            }
            if (!resolvedLocation.state().routable()) {
                return CompletableFuture.completedFuture(RoutingDecision.reject(RoutingRejectReason.MATCH_LOCATION_CLOSED));
            }
            return this.serverRegistry.find(resolvedLocation.serverId()).thenCompose(server -> {
                if (server.isEmpty() || !server.get().acceptsExistingMatchRoutingAt(request.requestedAt())) {
                    return CompletableFuture.completedFuture(RoutingDecision.reject(RoutingRejectReason.TARGET_SERVER_NOT_AVAILABLE));
                }
                return createAdmission(request, resolvedLocation.serverId(), resolvedLocation.arenaId());
            });
        });
    }

    private @NotNull CompletableFuture<RoutingDecision> routePreferredServer(@NotNull RoutingRequest request, @NotNull ServerId serverId) {
        return this.serverRegistry.find(serverId).thenCompose(server -> {
            if (server.isEmpty()
                    || !server.get().gameId().equals(request.gameId())
                    || server.get().role() != request.targetRole()
                    || !server.get().acceptsNewWorkAt(request.requestedAt())) {
                return CompletableFuture.completedFuture(RoutingDecision.reject(RoutingRejectReason.TARGET_SERVER_NOT_AVAILABLE));
            }
            return createAdmission(request, serverId);
        });
    }

    private @NotNull CompletableFuture<RoutingDecision> createAdmission(@NotNull RoutingRequest request, @NotNull ServerId serverId) {
        return createAdmission(request, serverId, null);
    }

    private @NotNull CompletableFuture<RoutingDecision> createAdmission(
            @NotNull RoutingRequest request,
            @NotNull ServerId serverId,
            ArenaId arenaId
    ) {
        final AdmissionRequest admission = AdmissionRequest.builder()
            .id(AdmissionRequestId.random())
            .playerId(request.playerId())
            .type(request.admissionType())
            .targetServerId(serverId)
            .gameId(request.gameId())
            .variantId(request.variantIdOptional().orElse(null))
            .arenaId(arenaId)
            .matchId(request.matchIdOptional().orElse(null))
            .createdAt(request.requestedAt())
            .expiresAt(request.requestedAt().plus(request.admissionTtl()))
            .build();
        return this.admissionStore.save(admission)
            .thenApply(ignored -> RoutingDecision.accept(serverId, admission))
            .exceptionally(ignored -> RoutingDecision.reject(RoutingRejectReason.ADMISSION_CREATION_FAILED));
    }
}
