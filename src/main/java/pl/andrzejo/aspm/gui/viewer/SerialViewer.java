package pl.andrzejo.aspm.gui.viewer;

import javax.swing.*;

public interface SerialViewer {
    void setAutoScroll(boolean auto);

    void setAddTimestamps(boolean auto);

    JComponent getComponent();

    void clear();

    void appendText(Text text);
}
