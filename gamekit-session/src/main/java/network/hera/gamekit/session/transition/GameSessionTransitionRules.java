package network.hera.gamekit.session.transition;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import network.hera.gamekit.session.state.GameSessionState;
import org.jetbrains.annotations.NotNull;

public final class GameSessionTransitionRules {

    private static final Map<GameSessionState, Set<GameSessionState>> ALLOWED_TRANSITIONS = allowedTransitions();

    private GameSessionTransitionRules() {
    }

    public static boolean canTransition(@NotNull GameSessionState from, @NotNull GameSessionState to) {
        return ALLOWED_TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }

    private static Map<GameSessionState, Set<GameSessionState>> allowedTransitions() {
        Map<GameSessionState, Set<GameSessionState>> transitions = new EnumMap<>(GameSessionState.class);
        transitions.put(GameSessionState.LOBBY, EnumSet.of(GameSessionState.QUEUE, GameSessionState.WAITING_ROOM));
        transitions.put(GameSessionState.QUEUE, EnumSet.of(GameSessionState.LOBBY, GameSessionState.WAITING_ROOM));
        transitions.put(GameSessionState.WAITING_ROOM, EnumSet.of(GameSessionState.PLAYING, GameSessionState.LOBBY));
        transitions.put(GameSessionState.PLAYING, EnumSet.of(GameSessionState.SPECTATING, GameSessionState.ENDING));
        transitions.put(GameSessionState.SPECTATING, EnumSet.of(GameSessionState.LOBBY, GameSessionState.ENDING));
        transitions.put(GameSessionState.ENDING, EnumSet.of(GameSessionState.LOBBY));
        return Map.copyOf(transitions);
    }
}
