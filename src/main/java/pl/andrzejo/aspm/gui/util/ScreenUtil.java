package pl.andrzejo.aspm.gui.util;

import java.awt.*;

public class ScreenUtil {

    public static Rectangle getMaximumScreenBounds() {
        int minx = 0, miny = 0, maxx = 0, maxy = 0;
        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (GraphicsDevice device : environment.getScreenDevices()) {
            Rectangle bounds = device.getDefaultConfiguration().getBounds();
            minx = Math.min(minx, bounds.x);
            miny = Math.min(miny, bounds.y);
            maxx = Math.max(maxx, bounds.x + bounds.width);
            maxy = Math.max(maxy, bounds.y + bounds.height);
        }
        return new Rectangle(minx, miny, maxx - minx, maxy - miny);
    }

}
