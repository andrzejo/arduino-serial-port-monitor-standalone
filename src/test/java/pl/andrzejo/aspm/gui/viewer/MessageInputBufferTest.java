package pl.andrzejo.aspm.gui.viewer;

import org.junit.jupiter.api.Test;
import pl.andrzejo.aspm.gui.viewer.model.Message;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class MessageInputBufferTest {
    private final MessageInputBuffer buffer = new MessageInputBuffer(new SerialMessageTypeResolver());
    private final Instant first = Instant.ofEpochMilli(1000);
    private final Instant later = Instant.ofEpochMilli(9000);

    @Test
    void shouldPreserveOrderAndFirstFragmentTimestampWhenAnApplicationMessageArrives() {
        //given
        buffer.addSerialLog("first\npar", first);
        MessageInputBuffer.Batch initial = buffer.drain();
        assertThat(initial.completed).extracting(Message::getText).containsExactly("first");
        assertThat(initial.incomplete.getText()).isEqualTo("par");

        //when
        buffer.addSerialLog("tial", later);
        Message closed = Message.info("Close serial");
        buffer.addMessage(closed);
        buffer.addSerialLog("next\n", later);
        MessageInputBuffer.Batch batch = buffer.drain();

        //then
        assertThat(batch.completed).extracting(Message::getText).containsExactly("partial", "Close serial", "next");
        assertThat(batch.completed.get(0).getTimestamp()).isEqualTo(first.toEpochMilli());
        assertThat(batch.completed.get(1)).isSameAs(closed);
        assertThat(batch.completed.get(2).getTimestamp()).isEqualTo(later.toEpochMilli());
        assertThat(batch.incomplete).isNull();
    }

    @Test
    void shouldHandleCrLfRegardlessOfChunkBoundariesAndStripOnlyOneCr() {
        //given
        String text = "A\r\r\nB\r\nC\n";
        for (int split = 0; split <= text.length(); split++) {
            MessageInputBuffer input = new MessageInputBuffer(new SerialMessageTypeResolver());

            //when
            input.addSerialLog(text.substring(0, split), first);
            List<Message> messages = new ArrayList<>(input.drain().completed);
            input.addSerialLog(text.substring(split), later);
            messages.addAll(input.drain().completed);

            //then
            assertThat(messages).extracting(Message::getText).containsExactly("A\r", "B", "C");
        }
    }

    @Test
    void shouldClearPendingFragmentsAndQueuedDataWithoutDiscardingNewMessages() {
        //given
        buffer.addSerialLog("partial", first);
        buffer.drain();
        buffer.addSerialLog("discarded\n", first);

        //when
        buffer.clear();
        buffer.addMessage(Message.info("Output cleared"));
        buffer.addSerialLog("new\n", later);

        MessageInputBuffer.Batch batch = buffer.drain();

        //then
        assertThat(batch.clearModel).isTrue();
        assertThat(batch.completed).extracting(Message::getText).containsExactly("Output cleared", "new");
        assertThat(batch.completed.get(1).getTimestamp()).isEqualTo(later.toEpochMilli());
        assertThat(batch.incomplete).isNull();
    }

    @Test
    void shouldLimitWorkPerBatchEvenForOneLargeChunk() {
        //given
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            text.append(i).append('\n');
        }
        buffer.addSerialLog(text.toString(), first);

        //when
        MessageInputBuffer.Batch firstBatch = buffer.drain();

        //then
        assertThat(firstBatch.completed).hasSize(MessageInputBuffer.MAX_BATCH_MESSAGES);
        assertThat(buffer.hasPending()).isTrue();

        //when
        List<Message> messages = new ArrayList<>(firstBatch.completed);
        while (buffer.hasPending()) {
            messages.addAll(buffer.drain().completed);
        }

        //then
        assertThat(messages).hasSize(1000);
        for (int i = 0; i < messages.size(); i++) {
            assertThat(messages.get(i).getText()).isEqualTo(String.valueOf(i));
        }
    }

    @Test
    void shouldSplitUnterminatedLongLinesWithoutLosingCharacters() {
        //given
        String text = String.join("", Collections.nCopies(MessageInputBuffer.MAX_LINE_CHARS * 3 + 7, "x"));
        buffer.addSerialLog(text, first);

        //when
        MessageInputBuffer.Batch batch = buffer.drain();

        //then
        assertThat(buffer.hasPending()).isTrue();
        assertThat(batch.incomplete.getText()).hasSize(MessageInputBuffer.MAX_BATCH_CHARS);

        //when
        buffer.close();
        List<Message> messages = new ArrayList<>(batch.completed);
        while (buffer.hasPending()) {
            messages.addAll(buffer.drain().completed);
        }

        //then
        assertThat(messages).allSatisfy(message -> {
            assertThat(message.getText().length()).isLessThanOrEqualTo(MessageInputBuffer.MAX_LINE_CHARS);
            assertThat(message.getTimestamp()).isEqualTo(first.toEpochMilli());
        });
        assertThat(messages.stream().map(Message::getText).collect(Collectors.joining())).isEqualTo(text);
    }

    @Test
    void shouldReportOverflowInOrderAndNotJoinTextAcrossMissingData() {
        //given
        buffer.addSerialLog("before", first);
        String tooLarge = String.join("", Collections.nCopies(MessageInputBuffer.MAX_QUEUED_CHARS + 1, "x"));
        buffer.addSerialLog(tooLarge, first);
        buffer.addSerialLog("after\n", later);

        //when
        MessageInputBuffer.Batch batch = buffer.drain();

        //then
        assertThat(batch.completed).hasSize(3);
        assertThat(batch.completed.get(0).getText()).isEqualTo("before");
        assertThat(batch.completed.get(1).getText()).contains("dropped 1 inputs", "1048577 characters");
        assertThat(batch.completed.get(2).getText()).isEqualTo("after");
    }

    @Test
    void shouldNotSplitASurrogatePairWhenLimitingLineLength() {
        //given
        String prefix = String.join("", Collections.nCopies(MessageInputBuffer.MAX_LINE_CHARS - 1, "x"));
        buffer.addSerialLog(prefix + "\uD83D\uDE00\n", first);

        //when
        List<Message> messages = new ArrayList<>();
        while (buffer.hasPending()) {
            messages.addAll(buffer.drain().completed);
        }

        //then
        assertThat(messages).extracting(Message::getText).containsExactly(prefix, "\uD83D\uDE00");
    }

    @Test
    void shouldLimitTheNumberOfQueuedInputsAndCoalesceOverflowReports() {
        //given
        for (int i = 0; i < MessageInputBuffer.MAX_QUEUED_INPUTS + 10; i++) {
            buffer.addSerialLog("x", first);
        }

        //when
        List<Message> messages = new ArrayList<>();
        while (buffer.hasPending()) {
            messages.addAll(buffer.drain().completed);
        }

        //then
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).getText()).hasSize(MessageInputBuffer.MAX_QUEUED_INPUTS);
        assertThat(messages.get(1).getText()).contains("dropped 10 inputs", "10 characters");
    }

    @Test
    void shouldFinishAnAlreadyDisplayedPartialLineOnCloseAndRejectFurtherInput() {
        //given
        buffer.addSerialLog("tail", first);
        assertThat(buffer.drain().incomplete.getText()).isEqualTo("tail");

        //when
        buffer.close();
        buffer.addSerialLog("ignored", later);
        buffer.addMessage(Message.info("ignored"));
        buffer.clear();

        MessageInputBuffer.Batch finalBatch = buffer.drain();

        //then
        assertThat(finalBatch.completed).extracting(Message::getText).containsExactly("tail");
        assertThat(finalBatch.completed.get(0).getTimestamp()).isEqualTo(first.toEpochMilli());
        assertThat(finalBatch.incomplete).isNull();
        assertThat(buffer.hasPending()).isFalse();
    }
}
