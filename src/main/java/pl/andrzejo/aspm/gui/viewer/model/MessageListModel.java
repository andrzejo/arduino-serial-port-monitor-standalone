/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2026.
 */

package pl.andrzejo.aspm.gui.viewer.model;

import lombok.RequiredArgsConstructor;

import javax.swing.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class MessageListModel extends AbstractListModel<Message> {
    private final Message[] buffer;
    private final int capacity;
    private int head = 0;
    private int size = 0;
    private Message incompleteMessage = null;

    public MessageListModel(int capacity) {
        this.capacity = capacity;
        this.buffer = new Message[capacity];
    }

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
        if (newMessages.isEmpty() && newIncomplete == incompleteMessage) {
            return;
        }

        int oldTotalSize = getSize();
        boolean hadIncomplete = (incompleteMessage != null);
        this.incompleteMessage = newIncomplete;

        if (newMessages.isEmpty()) {
            if (oldTotalSize > 0) {
                fireContentsChanged(this, oldTotalSize - 1, oldTotalSize - 1);
            } else if (incompleteMessage != null) {
                fireIntervalAdded(this, 0, 0);
            }
            return;
        }

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

        if (size < capacity && !hadIncomplete) {
            fireIntervalAdded(this, oldTotalSize, getSize() - 1);
        } else {
            fireContentsChanged(this, 0, getSize() - 1);
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
            fireContentsChanged(this, 0, oldSize - 1);
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
