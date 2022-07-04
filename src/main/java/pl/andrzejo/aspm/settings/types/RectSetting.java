package pl.andrzejo.aspm.settings.types;

import pl.andrzejo.aspm.gui.util.ScreenUtil;
import pl.andrzejo.aspm.settings.Setting;

import java.awt.*;

import static java.lang.Integer.parseInt;

public class RectSetting extends Setting<Rectangle> {

    private RectSetting(String key) {
        super(key);
    }

    public RectSetting(String key, Rectangle defValue) {
        super(key, defValue);
    }

    @Override
    protected String serialize(Rectangle value) {
        return String.format("%d|%d|%s|%s", value.x, value.y, value.width, value.height);
    }

    @Override
    protected Rectangle deserialize(String value) {
        String[] parts = value.split("\\|");
        if (parts.length == 4) {
            int x = parseInt(parts[0]);
            int y = parseInt(parts[1]);
            int w = parseInt(parts[2]);
            int h = parseInt(parts[3]);
            return validated(new Rectangle(x, y, w, h));
        }
        return null;
    }

    private Rectangle validated(Rectangle rectangle) {
        Rectangle desktop = ScreenUtil.getMaximumScreenBounds();
        if (desktop.contains(rectangle)) {
            return rectangle;
        }
        return defValue;
    }

}
