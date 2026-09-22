/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2026.
 */

package pl.andrzejo.aspm.gui.viewer;

import pl.andrzejo.aspm.gui.viewer.model.Message;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import static pl.andrzejo.aspm.gui.viewer.model.MessageType.INTERNAL_ERROR;

final class MessageInputBuffer {
    static final int MAX_BATCH_CHARS = 16_384;
    static final int MAX_BATCH_MESSAGES = 256;
    static final int MAX_LINE_CHARS = 16_384;
    static final int MAX_QUEUED_CHARS = 1_048_576;
    static final int MAX_QUEUED_INPUTS = 4096;

    private final ArrayDeque<Input> inputs = new ArrayDeque<>();
    private final StringBuilder line = new StringBuilder(4096);
    private final SerialMessageTypeResolver resolver;
    private Instant lineTimestamp;
    private int queuedChars;
    private boolean closed;

    MessageInputBuffer(SerialMessageTypeResolver resolver) {
        this.resolver = resolver;
    }

    void addSerialLog(String text, Instant timestamp) {
        if (text != null && !text.isEmpty()) {
            offer(new Input(Kind.SERIAL, timestamp != null ? timestamp : Instant.now(), text, null));
        }
    }

    void addMessage(Message message) {
        if (message != null) {
            offer(new Input(Kind.MESSAGE, null, message.getText(), message));
        }
    }

    private synchronized void offer(Input input) {
        if (closed) {
            return;
        }
        int length = input.text == null ? 0 : input.text.length();
        if (inputs.size() >= MAX_QUEUED_INPUTS || length > MAX_QUEUED_CHARS - queuedChars) {
            Input overflow = inputs.peekLast();
            if (overflow == null || overflow.kind != Kind.OVERFLOW) {
                overflow = new Input(Kind.OVERFLOW, Instant.now(), null, null);
                inputs.addLast(overflow);
            }
            overflow.droppedInputs++;
            overflow.droppedChars += length;
            return;
        }
        inputs.addLast(input);
        queuedChars += length;
    }

    synchronized void clear() {
        if (!closed) {
            inputs.clear();
            queuedChars = 0;
            inputs.addLast(new Input(Kind.CLEAR, null, null, null));
        }
    }

    synchronized void close() {
        closed = true;
    }

    synchronized boolean isClosed() {
        return closed;
    }

    synchronized boolean hasPending() {
        return !inputs.isEmpty() || (closed && line.length() > 0);
    }

    synchronized Batch drain() {
        List<Message> completed = new ArrayList<>();
        boolean clearModel = false;
        int chars = 0;
        int entries = 0;
        while (!inputs.isEmpty() && chars < MAX_BATCH_CHARS &&
                entries < MAX_BATCH_MESSAGES &&
                completed.size() < MAX_BATCH_MESSAGES) {
            Input input = inputs.peekFirst();
            switch (input.kind) {
                case SERIAL:
                    chars += drainSerial(input, completed, MAX_BATCH_CHARS - chars);
                    break;
                case MESSAGE:
                    appendDirectMessage(input, completed);
                    break;
                case CLEAR:
                    clearCurrentBatch(completed);
                    clearModel = true;
                    break;
                case OVERFLOW:
                    appendOverflowMessage(input, completed);
                    break;
            }
            if (input.kind == Kind.SERIAL && input.offset < input.text.length()) {
                break;
            }
            inputs.removeFirst();
            entries++;
        }
        if (closed && inputs.isEmpty()) {
            completeLine(completed, false);
        }
        return new Batch(completed, currentLine(), clearModel);
    }

    private int drainSerial(Input input, List<Message> completed, int charBudget) {
        int consumed = 0;
        while (input.offset < input.text.length() && consumed < charBudget
                && completed.size() < MAX_BATCH_MESSAGES) {
            char c = input.text.charAt(input.offset++);
            consumed++;
            queuedChars--;
            appendSerialChar(c, input.timestamp, completed);
        }
        return consumed;
    }

    private void appendSerialChar(char c, Instant timestamp, List<Message> completed) {
        if (c == '\n') {
            completeLine(completed, true);
            return;
        }
        if (line.length() == MAX_LINE_CHARS) {
            splitLongLine(c, completed);
        }
        if (lineTimestamp == null) {
            lineTimestamp = timestamp;
        }
        line.append(c);
    }

    private void splitLongLine(char nextChar, List<Message> completed) {
        char last = line.charAt(line.length() - 1);
        if (Character.isHighSurrogate(last) && Character.isLowSurrogate(nextChar)) {
            Instant timestamp = lineTimestamp;
            line.setLength(line.length() - 1);
            completeLine(completed, false);
            lineTimestamp = timestamp;
            line.append(last);
        } else {
            completeLine(completed, false);
        }
    }

    private void appendDirectMessage(Input input, List<Message> completed) {
        completeLine(completed, false);
        completed.add(input.message);
        queuedChars -= input.text == null ? 0 : input.text.length();
    }

    private void clearCurrentBatch(List<Message> completed) {
        line.setLength(0);
        lineTimestamp = null;
        completed.clear();
    }

    private void appendOverflowMessage(Input input, List<Message> completed) {
        completeLine(completed, false);
        completed.add(new Message(input.timestamp.toEpochMilli(),
                "Input buffer full: dropped " + input.droppedInputs + " inputs ("
                        + input.droppedChars + " characters)", INTERNAL_ERROR));
    }

    private void completeLine(List<Message> completed, boolean stripCarriageReturn) {
        if (stripCarriageReturn && line.length() > 0 && line.charAt(line.length() - 1) == '\r') {
            line.setLength(line.length() - 1);
        }
        Message message = currentLine();
        if (message != null) {
            completed.add(message);
        }
        line.setLength(0);
        lineTimestamp = null;
    }

    private Message currentLine() {
        if (line.length() == 0) {
            return null;
        }
        String text = line.toString();
        return new Message(lineTimestamp.toEpochMilli(), text, resolver.resolve(text));
    }

    static final class Batch {
        final List<Message> completed;
        final Message incomplete;
        final boolean clearModel;

        Batch(List<Message> completed, Message incomplete, boolean clearModel) {
            this.completed = completed;
            this.incomplete = incomplete;
            this.clearModel = clearModel;
        }
    }

    private enum Kind {SERIAL, MESSAGE, CLEAR, OVERFLOW}

    private static final class Input {
        final Kind kind;
        final Instant timestamp;
        final String text;
        final Message message;
        int offset;
        long droppedInputs;
        long droppedChars;

        Input(Kind kind, Instant timestamp, String text, Message message) {
            this.kind = kind;
            this.timestamp = timestamp;
            this.text = text;
            this.message = message;
        }
    }
}
