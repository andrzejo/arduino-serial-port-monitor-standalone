package pl.andrzejo.aspm.gui.viewer;

public class Text {
    public enum Type {
        SERIAL_MESSAGE,
        INTERNAL_MESSAGE,
        INTERNAL_ERROR
    }

    private String text;
    private Type type;

    public String getText() {
        return text;
    }

    public Type getType() {
        return type;
    }

    public Text(String text, Type type) {
        this.text = text;
        this.type = type;
    }

    public static Text message(String text) {
        return new Text(text, Type.SERIAL_MESSAGE);
    }

    public static Text appMessage(String text) {
        return new Text(text, Type.INTERNAL_MESSAGE);
    }

    public static Text error(String text) {
        return new Text(text, Type.INTERNAL_ERROR);
    }
}
