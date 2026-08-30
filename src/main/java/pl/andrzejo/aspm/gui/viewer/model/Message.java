/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2026.
 */

package pl.andrzejo.aspm.gui.viewer.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.andrzejo.aspm.gui.viewer.util.TimestampHelper;

import java.time.Instant;
import java.util.Date;

@Getter
@RequiredArgsConstructor
public class Message {
    private final long timestamp;
    private final String text;
    private final MessageType type;
    private transient String formattedTime;

    public static Message info(String msg) {
        return new Message(System.currentTimeMillis(), msg, MessageType.INTERNAL_INFO);
    }

    public static Message error(String msg) {
        return new Message(System.currentTimeMillis(), msg, MessageType.INTERNAL_ERROR);
    }

    public static Message message(String msg, Date date) {
        return new Message(date.getTime(), msg, MessageType.SERIAL_INFO);
    }

    public String getFormattedTimestamp() {
        if (formattedTime == null) {
            formattedTime = TimestampHelper.getTimestamp(Instant.ofEpochMilli(timestamp));
        }
        return formattedTime;
    }
}
