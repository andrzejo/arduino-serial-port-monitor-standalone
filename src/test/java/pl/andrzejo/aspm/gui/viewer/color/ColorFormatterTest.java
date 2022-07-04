package pl.andrzejo.aspm.gui.viewer.color;

import org.junit.jupiter.api.Test;
import pl.andrzejo.aspm.gui.viewer.Text;

import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class ColorFormatterTest {

    @Test
    void shouldFormatDate() {
        //given
        ColorFormatter formatter = new ColorFormatter();

        //when
        Date date = Date.from(Instant.parse("2022-01-01T12:00:00Z"));
        String format = formatter.format(Text.message("Message 1", date), true);

        //then
        assertThat(format).isEqualTo("<span class='serial-msg'><span class='time'>13:00:00.000</span>: Message 1</span>");
    }

    @Test
    void shouldFormatDateContinue() {
        //given
        ColorFormatter formatter = new ColorFormatter();

        //when
        Date date = Date.from(Instant.parse("2022-01-01T12:00:00Z"));
        formatter.format(Text.message("Message 1", date), true);
        String format = formatter.format(Text.message("Message 2", date), true);

        //then
        assertThat(format).isEqualTo("<span class='serial-msg serial-msg-continue'>Message 2</span>");
    }
}