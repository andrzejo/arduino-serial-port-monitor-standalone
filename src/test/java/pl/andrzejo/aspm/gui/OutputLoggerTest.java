package pl.andrzejo.aspm.gui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pl.andrzejo.aspm.gui.viewer.model.Message;
import pl.andrzejo.aspm.settings.appsettings.items.viewer.SaveLogToFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OutputLoggerTest {
    @TempDir
    Path directory;

    @Test
    void shouldWaitForQueuedBatchesToBeWrittenBeforeShutdownReturns() throws Exception {
        //given
        SaveLogToFile enabled = mock(SaveLogToFile.class);
        when(enabled.get()).thenReturn(true);
        Path file = directory.resolve("output.txt");
        OutputLogger logger = new OutputLogger(file.toFile(), enabled);

        //when
        try {
            for (int batch = 0; batch < 20; batch++) {
                List<Message> messages = new ArrayList<>();
                for (int i = 0; i < 100; i++) {
                    messages.add(Message.info("line " + (batch * 100 + i)));
                }
                logger.log(messages);
            }
        } finally {
            logger.shutdown();
        }

        //then
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        assertThat(lines).hasSize(2000);
        for (int i = 0; i < lines.size(); i++) {
            assertThat(lines.get(i)).endsWith("line " + i);
        }
    }
}
