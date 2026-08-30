/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2026.
 */

package pl.andrzejo.aspm.gui.viewer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.andrzejo.aspm.gui.viewer.model.MessageType;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static pl.andrzejo.aspm.gui.viewer.model.MessageType.*;

public class Styles {
    private final Map<MessageType, Style> styles = new HashMap<>();

    public Styles() {
        Arrays.asList(
                create(TIME, "#808080"),

                create(INTERNAL_INFO, "#3794FF"),
                create(INTERNAL_ERROR, "#F44747"),

                create(SERIAL_DEBUG, "#6A9955"),
                create(SERIAL_INFO, "#000000"),
                create(SERIAL_WARN, "#CCA633"),
                create(SERIAL_ERROR, "#F44747")
        ).forEach(s -> {
            styles.put(s.getType(), s);
        });
    }

    public Style get(MessageType type) {
        return styles.get(type);
    }

    private Style create(MessageType messageType, String color) {
        return new Style(messageType, getColor(color), null);
    }

    private Color getColor(String color) {
        int colorAsInt = Integer.parseInt(color.substring(1), 16);
        return new Color(colorAsInt);
    }

    @Getter
    @RequiredArgsConstructor
    public static class Style {
        private final MessageType type;
        private final Color color;
        private final Color bgColor;
    }
}
