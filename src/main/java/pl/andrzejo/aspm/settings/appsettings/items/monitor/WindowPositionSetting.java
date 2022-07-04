package pl.andrzejo.aspm.settings.appsettings.items.monitor;

import pl.andrzejo.aspm.settings.types.RectSetting;

import java.awt.*;

public class WindowPositionSetting extends GroupMonitorSetting<Rectangle> {

    public static final Rectangle DEF_VALUE = new Rectangle(10, 10, 400, 400);

    protected WindowPositionSetting() {
        super(RectSetting.class, "windowSize", DEF_VALUE);
    }

}