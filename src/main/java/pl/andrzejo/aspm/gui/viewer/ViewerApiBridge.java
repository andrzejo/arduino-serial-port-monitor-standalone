/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2026.
 */

package pl.andrzejo.aspm.gui.viewer;

import pl.andrzejo.aspm.gui.viewer.model.MessageListModel;

import static pl.andrzejo.aspm.factory.BeanFactory.instance;

public class ViewerApiBridge {
    private final MessageListModel messagesListModel = instance(MessageListModel.class);

    public String getOutput(boolean withMessages) {
        StringBuilder sb = new StringBuilder();
        messagesListModel.forEach(msg -> {
            if (!withMessages && msg.isInternal()) {
                return;
            }
            sb.append(msg.getFormattedTimestamp());
            sb.append(": ");
            sb.append(msg.getText());
            sb.append("\n");
        });
        return sb.toString();
    }
}
