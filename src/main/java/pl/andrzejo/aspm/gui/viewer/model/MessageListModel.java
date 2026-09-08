/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2026.
 */

package pl.andrzejo.aspm.gui.viewer.model;

import javax.swing.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class MessageListModel extends AbstractListModel<Message> {
    private static final int MAX_LINES = 20_000;
    private final Message[] buffer = new Message[MAX_LINES];
    private final int capacity = MAX_LINES;
    private int head = 0;
    private int size = 0;
    private Message incompleteMessage = null;

    @Override
    public synchronized int getSize() {
        return size + (incompleteMessage != null ? 1 : 0);
    }

    @Override
    public synchronized Message getElementAt(int index) {
        if (index < 0 || index >= getSize()) {
            return null;
        }
        if (incompleteMessage != null && index == size) {
            return incompleteMessage;
        }
        int actualIndex = (head + index) % capacity;
        return buffer[actualIndex];
    }

    public synchronized void appendBatch(List<Message> newMessages, Message newIncomplete) {
        if (newMessages.isEmpty() && Objects.equals(newIncomplete, incompleteMessage)) {
            return;
        }

        int oldLogicalSize = getSize();
        boolean hadIncomplete = (incompleteMessage != null);
        this.incompleteMessage = newIncomplete;
        boolean hasIncompleteNow = (incompleteMessage != null);

        if (newMessages.isEmpty()) {
            if (!hadIncomplete && hasIncompleteNow) {
                fireIntervalAdded(this, oldLogicalSize, oldLogicalSize);
            } else if (hadIncomplete && !hasIncompleteNow) {
                fireIntervalRemoved(this, oldLogicalSize - 1, oldLogicalSize - 1);
            } else if (hadIncomplete) {
                fireContentsChanged(this, oldLogicalSize - 1, oldLogicalSize - 1);
            }
            return;
        }

        int oldSize = size;
        for (Message msg : newMessages) {
            if (size < capacity) {
                int insertIndex = (head + size) % capacity;
                buffer[insertIndex] = msg;
                size++;
            } else {
                buffer[head] = msg;
                head = (head + 1) % capacity;
            }
        }

        if (oldSize + newMessages.size() <= capacity && !hadIncomplete && !hasIncompleteNow) {
            fireIntervalAdded(this, oldLogicalSize, getSize() - 1);
        } else {
            fireContentsChanged(this, 0, Math.max(oldLogicalSize, getSize()) - 1);
        }
    }

    public void add(Message message) {
        appendBatch(Collections.singletonList(message), null);
    }

    public synchronized void clear() {
        int oldSize = getSize();
        head = 0;
        size = 0;
        incompleteMessage = null;
        if (oldSize > 0) {
            fireIntervalRemoved(this, 0, oldSize - 1);
        }
    }

    public synchronized void forEach(Consumer<? super Message> action) {
        Objects.requireNonNull(action, "Action cannot be null");
        for (int i = 0; i < size; i++) {
            int actualIndex = (head + i) % capacity;
            Message msg = buffer[actualIndex];
            if (msg != null) {
                action.accept(msg);
            }
        }
        if (incompleteMessage != null) {
            action.accept(incompleteMessage);
        }
    }
}
