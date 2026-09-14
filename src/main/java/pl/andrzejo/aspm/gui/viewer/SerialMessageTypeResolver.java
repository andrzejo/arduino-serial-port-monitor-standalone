/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.gui.viewer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import pl.andrzejo.aspm.gui.viewer.model.MessageType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pl.andrzejo.aspm.gui.viewer.model.MessageType.*;

public class SerialMessageTypeResolver {
    private enum TokenMatch {
        PREFIX,
        CONTAINS
    }

    private final Map<MessageType, List<Token>> msgStylesPrefixes = new HashMap<>();

    public SerialMessageTypeResolver() {
        msgStylesPrefixes.put(SERIAL_DEBUG, Arrays.asList(prefix("D"), prefix("DEBUG"), contains("[D]"), contains("[V]")));
        msgStylesPrefixes.put(SERIAL_INFO, Arrays.asList(prefix("I"), prefix("INFO"), contains("[I]")));
        msgStylesPrefixes.put(SERIAL_ERROR, Arrays.asList(prefix("E"), prefix("ERROR"), prefix("ERR"), contains("[E]")));
        msgStylesPrefixes.put(SERIAL_WARN, Arrays.asList(prefix("W"), prefix("WARN"), contains("[W]")));
    }


    private static Token prefix(String token) {
        return new Token(token, TokenMatch.PREFIX);
    }

    private static Token contains(String token) {
        return new Token(token, TokenMatch.CONTAINS);
    }

    public MessageType resolve(String text) {
        for (Map.Entry<MessageType, List<Token>> entry : msgStylesPrefixes.entrySet()) {
            if (entry.getValue().stream().anyMatch(p -> p.matches(text))) {
                return entry.getKey();
            }
        }
        return SERIAL_INFO;
    }

    @Getter
    @RequiredArgsConstructor
    private static class Token {
        private final String token;
        private final TokenMatch match;

        public boolean matches(String text) {
            if (match == TokenMatch.CONTAINS) {
                return StringUtils.contains(text, token);
            }
            return StringUtils.startsWithIgnoreCase(text, token + ":");
        }
    }
}
