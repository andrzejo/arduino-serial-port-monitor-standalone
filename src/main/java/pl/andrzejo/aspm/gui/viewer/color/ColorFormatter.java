package pl.andrzejo.aspm.gui.viewer.color;

import org.apache.commons.lang.StringEscapeUtils;
import pl.andrzejo.aspm.gui.viewer.Text;
import pl.andrzejo.aspm.gui.viewer.util.TimestampHelper;

import java.util.Date;

import static org.apache.commons.lang.StringUtils.trimToEmpty;

public class ColorFormatter {
    private boolean isLastTagClosed = true;

    public String time(Date date) {
        return style("time", TimestampHelper.getTimestamp(date));
    }

    public String style(String cssClass, String text) {
        return style(cssClass, text, true);
    }

    private String style(String cssClass, String text, boolean closeTag) {
        return String.format("<span class='%s'>%s%s", cssClass, StringEscapeUtils.escapeHtml(text), closeTag ? "</span>" : "");
    }

    public String format(Text text, boolean appendTimestamp) {
        switch (text.getType()) {
            case SERIAL_MESSAGE:
                return formatSerialMessage(text.getText(), text.getDate(), appendTimestamp);
            case INTERNAL_MESSAGE:
                return formatInternalMessage(text.getText(), text.getDate(), appendTimestamp);
            case INTERNAL_ERROR:
                return formatErrorMessage(text.getText(), text.getDate(), appendTimestamp);
            default:
                throw new ColorFormatterException("Unsupported text type: " + text.getType().name());
        }
    }

    private String formatErrorMessage(String text, Date date, boolean appendTimestamp) {
        isLastTagClosed = true;
        return null;
    }

    private String formatInternalMessage(String text, Date date, boolean appendTimestamp) {
        isLastTagClosed = true;
        String time = appendTimestamp ? time(date) + " - " : "";
        return String.format("<div class='%s'>%s%s</div>", "int-msg", time, escape(text));
    }

    private String escape(String text) {
        return StringEscapeUtils.escapeHtml(text);
    }

    private String formatSerialMessage(String text, Date date, boolean appendTimestamp) {
        String t = trimToEmpty(text);
        char lastChar = t.charAt(t.length() - 1);
        boolean isMessageCompleted = lastChar == '\n' || lastChar == '\r';
        String time = appendTimestamp ? time(date) + ": " : "";

        if (isLastTagClosed) {
            if (isMessageCompleted) {
                return String.format("<span class='%s'>%s%s</span><br>", "serial-msg", time, nlToBr(escape(text)));
            } else {
                isLastTagClosed = false;
                return String.format("<span class='%s'>%s%s</span>", "serial-msg", time, nlToBr(escape(text)));
            }
        } else {
            return String.format("<span class='%s %s'>%s</span>", "serial-msg", "serial-msg-continue", nlToBr(escape(text)));
        }
    }

    private String nlToBr(String escape) {
        return escape.replaceAll("[\n\r]", "<br>");
    }
}
