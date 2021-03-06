/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.gui.viewer;

import java.util.Date;

public class Text {
    private final String text;
    private final Type type;
    private final Date date;
    public Text(String text, Type type, Date date) {
        this.text = text;
        this.type = type;
        this.date = date;
    }
    public Text(String text, Type type) {
        this(text, type, new Date());
    }

    public static Text message(String text, Date date) {
        return new Text(text, Type.SERIAL_MESSAGE, date);
    }

    public static Text appMessage(String text) {
        return new Text(text, Type.INTERNAL_MESSAGE);
    }

    public static Text error(String text) {
        return new Text(text, Type.INTERNAL_ERROR);
    }

    public static Text info(String text) {
        return new Text(text, Type.INTERNAL_INFO);
    }

    public Date getDate() {
        return date;
    }

    public String getText() {
        return text;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        SERIAL_MESSAGE, INTERNAL_MESSAGE, INTERNAL_ERROR, INTERNAL_INFO
    }
}
