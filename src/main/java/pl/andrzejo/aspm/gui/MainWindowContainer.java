package pl.andrzejo.aspm.gui;

import javax.swing.*;
import java.awt.*;

public class MainWindowContainer {
    private static Component wnd = null;

    public static void setMainWindowComponent(Component window) {
        wnd = window;
    }

    public static Component getComponent() {
        return wnd;
    }
}
