package network.hera.gamekit.core.error;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Map;
import org.junit.jupiter.api.Test;

class GameKitExceptionTest {

    @Test
    void preservesCodeAttributesAndCause() {
        RuntimeException cause = new RuntimeException("root cause");
        GameKitException exception = new GameKitException(
                CoreErrorCode.INVALID_STATE,
                "technical message",
                Map.of("state", "LOBBY"),
                cause
        );

        assertEquals(CoreErrorCode.INVALID_STATE, exception.errorCode());
        assertEquals("technical message", exception.getMessage());
        assertEquals("LOBBY", exception.attributes().get("state"));
        assertSame(cause, exception.getCause());
    }
}
