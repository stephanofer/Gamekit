package network.hera.gamekit.lobbypaper.action;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import network.hera.gamekit.core.id.GameId;
import network.hera.gamekit.core.id.PlayerId;
import network.hera.gamekit.core.id.QueueId;
import network.hera.gamekit.core.id.VariantId;
import network.hera.gamekit.queue.casual.CasualQueueDecision;
import network.hera.gamekit.queue.casual.CasualQueueRequest;
import network.hera.gamekit.queue.casual.CasualQueueResolver;
import network.hera.gamekit.queue.definition.QueueDefinition;
import org.jetbrains.annotations.NotNull;

public final class JoinCasualQueueAction implements LobbyItemAction {

    private final CasualQueueResolver resolver;
    private final QueueDefinitionProvider queues;
    private final BiConsumer<Context, CasualQueueDecision> decisionHandler;

    public JoinCasualQueueAction(
            @NotNull CasualQueueResolver resolver,
            @NotNull QueueDefinitionProvider queues,
            @NotNull BiConsumer<Context, CasualQueueDecision> decisionHandler
    ) {
        this.resolver = Objects.requireNonNull(resolver, "resolver");
        this.queues = Objects.requireNonNull(queues, "queues");
        this.decisionHandler = Objects.requireNonNull(decisionHandler, "decisionHandler");
    }

    @Override
    public @NotNull CompletableFuture<Void> execute(@NotNull Context context) {
        Objects.requireNonNull(context, "context");
        final QueueId queueId = parseQueueId(context.definition().argument("queue"));
        final QueueDefinition queue = this.queues.findQueue(queueId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown queue configured for lobby item: " + queueId));
        return this.resolver.resolve(new CasualQueueRequest(PlayerId.of(context.player().getUniqueId()), queue))
                .thenAccept(decision -> this.decisionHandler.accept(context, decision));
    }

    private static @NotNull QueueId parseQueueId(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("join_casual_queue action requires a queue argument.");
        }
        String[] parts = raw.trim().split(":", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Queue id must use game:variant format: " + raw);
        }
        return QueueId.of(GameId.of(parts[0]), VariantId.of(parts[1]));
    }
}
