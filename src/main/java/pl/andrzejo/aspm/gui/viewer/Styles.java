package pl.andrzejo.aspm.gui.viewer;

import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Styles {
    private final Map<MessageType, Style> styles = new HashMap<>();
    private final StyledDocument doc;

    public Styles(StyledDocument doc) {
        this.doc = doc;
        styles.put(MessageType.TIME, timeStyle());
        styles.put(MessageType.INTERNAL_MESSAGE, intMessageStyle());
        styles.put(MessageType.INTERNAL_INFO, intInfoStyle());
        styles.put(MessageType.INTERNAL_ERROR, intErrorStyle());
        styles.put(MessageType.SERIAL_MESSAGE, serialMsgStyle());

        styles.put(MessageType.SERIAL_DEBUG, serialDebugStyle());
        styles.put(MessageType.SERIAL_INFO, serialInfoStyle());
        styles.put(MessageType.SERIAL_WARN, serialWarnStyle());
        styles.put(MessageType.SERIAL_ERROR, serialErrorStyle());
    }

    public Style get(MessageType type) {
        return styles.get(type);
    }

    private Style serialMsgStyle() {
        return createStyle(MessageType.SERIAL_MESSAGE, "#000000");
    }

    private Style serialDebugStyle() {
        return createStyle(MessageType.SERIAL_DEBUG, "#000000");
    }

    private Style serialInfoStyle() {
        return createStyle(MessageType.SERIAL_INFO, "#000000");
    }

    private Style serialWarnStyle() {
        return createStyle(MessageType.SERIAL_WARN, "#000000");
    }

    private Style serialErrorStyle() {
        return createStyle(MessageType.SERIAL_ERROR, "#000000");
    }

    private Style intErrorStyle() {
        Style style = createStyle(MessageType.INTERNAL_MESSAGE, "#ffffff");
        StyleConstants.setBackground(style, getColor("#ff3300"));
        return style;
    }

    private Style intMessageStyle() {
        Style style = createStyle(MessageType.INTERNAL_MESSAGE, "#076707");
        StyleConstants.setBackground(style, getColor("#a5ffa5"));
        return style;
    }

    private Style intInfoStyle() {
        Style style = createStyle(MessageType.INTERNAL_INFO, "#ffffff");
        StyleConstants.setBackground(style, getColor("#3366ff"));
        return style;
    }

    private Style timeStyle() {
        Style style = createStyle(MessageType.TIME, "#a8a8a8");
        StyleConstants.setItalic(style, true);
        return style;
    }

    private Style createStyle(MessageType type, String color) {
        Color c = getColor(color);
        Style style = doc.addStyle(type.name(), null);
        StyleConstants.setForeground(style, c);
        return style;
    }

    private Color getColor(String color) {
        int colorAsInt = Integer.parseInt(color.substring(1), 16);
        return new Color(colorAsInt);
    }

    public enum MessageType {
        TIME,
        INTERNAL_MESSAGE,
        INTERNAL_INFO,
        INTERNAL_ERROR,
        SERIAL_MESSAGE,
        SERIAL_DEBUG,
        SERIAL_INFO,
        SERIAL_WARN,
        SERIAL_ERROR;
    }
}
