package pl.andrzejo.aspm.gui;

import javax.swing.*;
import java.awt.*;

public class MainWindowContainer {
    private static JFrame wnd = null;

    public static void setMainWindowComponent(JFrame window) {
        wnd = window;
    }

    public static JFrame getComponent() {
        return wnd;
    }
}
