package network.hera.gamekit.core.id;

import java.util.regex.Pattern;
import network.hera.gamekit.core.error.InvalidGameKitIdException;

final class IdFormats {

    private static final int MAX_LENGTH = 64;
    private static final Pattern DOMAIN_ID = Pattern.compile("[a-z0-9]+(?:_[a-z0-9]+)*");
    private static final Pattern SERVER_ID = Pattern.compile("[a-z0-9]+(?:-[a-z0-9]+)*");

    private IdFormats() {
    }

    static String requireDomainId(String type, String value) {
        return requireMatching(type, value, DOMAIN_ID, "lowercase_snake_case");
    }

    static String requireServerId(String value) {
        return requireMatching("ServerId", value, SERVER_ID, "kebab-case");
    }

    private static String requireMatching(
        String type,
        String value,
        Pattern pattern,
        String format
    ) {
        if (value == null) {
            throw InvalidGameKitIdException.invalid(type, null, format);
        }

        if (value.isEmpty() || value.length() > MAX_LENGTH || !pattern.matcher(value).matches()) {
            throw InvalidGameKitIdException.invalid(type, value, format);
        }

        return value;
    }
}
