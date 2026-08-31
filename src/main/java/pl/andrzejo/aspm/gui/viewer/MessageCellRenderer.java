/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2026.
 */

package pl.andrzejo.aspm.gui.viewer;

import lombok.Getter;
import pl.andrzejo.aspm.factory.BeanFactory;
import pl.andrzejo.aspm.gui.viewer.model.Message;

import javax.swing.*;
import java.awt.*;

import static pl.andrzejo.aspm.gui.viewer.model.MessageType.TIME;

public class MessageCellRenderer extends JComponent implements ListCellRenderer<Message> {
    private final Styles styles = BeanFactory.instance(Styles.class);
    private final DisplayData display = new DisplayData();
    private Message currentMessage;
    private boolean isSelected;
    private Color listBg, listSelectionBg, listSelectionFg;
    private boolean renderTimestamp;
    private Boolean escapeChars;

    public MessageCellRenderer() {
        setOpaque(true);
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Message> list, Message entry, int index,
                                                  boolean isSelected, boolean cellHasFocus
    ) {
        this.currentMessage = entry;
        this.isSelected = isSelected;
        this.listBg = list.getBackground();
        this.listSelectionBg = list.getSelectionBackground();
        this.listSelectionFg = list.getSelectionForeground();
        if (entry != null) {
            display.update(entry, renderTimestamp, escapeChars);
        }
        return this;
    }

    public void renderTimestamp(boolean renderTimestamp) {
        this.renderTimestamp = renderTimestamp;
    }

    public void escapeChars(Boolean escapeChars) {
        this.escapeChars = escapeChars;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (currentMessage == null) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        int width = getWidth();
        int height = getHeight();

        g.setColor(isSelected ? listSelectionBg : listBg);
        g.fillRect(0, 0, width, height);

        FontMetrics fm = g.getFontMetrics();
        int textY = (height - fm.getHeight()) / 2 + fm.getAscent();
        int currentX = 5;

        if (isSelected) {
            g.setColor(listSelectionFg);
            if (display.isWithTimestamp()) {
                g.drawString(display.getTimestamp(), currentX, textY);
                currentX += fm.stringWidth(display.getTimestamp());
            }
            g.drawString(display.getText(), currentX, textY);
            return;
        }

        if (display.isWithTimestamp()) {
            currentX += drawString(g, currentX, textY, styles.get(TIME), display.getTimestamp());
        }
        drawString(g, currentX, textY, styles.get(currentMessage.getType()), display.getText());
    }

    @Override
    public Dimension getPreferredSize() {
        if (currentMessage == null) {
            return new Dimension(100, 16);
        }
        FontMetrics fm = getFontMetrics(getFont());
        int width = 5 + display.calculateWidth(fm) + 20;
        return new Dimension(width, fm.getHeight());
    }

    private int drawString(Graphics g, int currentX, int textY, Styles.Style style, String str) {
        Color bg = style.getBgColor();
        int width = g.getFontMetrics().stringWidth(str);

        if (bg != null) {
            g.setColor(bg);
            g.fillRect(currentX, 0, width, getHeight());
        }

        g.setColor(style.getColor());
        g.drawString(str, currentX, textY);
        return width;
    }

    @Override
    public void validate() {
    }

    @Override
    public void invalidate() {
    }

    @Override
    public void repaint() {
    }

    @Override
    public void revalidate() {
    }

    @Getter
    private static class DisplayData {
        private String timestamp = "";
        private String text = "";
        private boolean withTimestamp = false;

        public void update(Message msg, boolean renderTimestamp, boolean escape) {
            if (renderTimestamp) {
                this.timestamp = msg.getFormattedTimestamp() + " ";
                this.withTimestamp = true;
            } else {
                this.timestamp = "";
                this.withTimestamp = false;
            }

            if (msg.isInternal()) {
                this.text = msg.getText();
            } else {
                this.text = escape ? msg.getEscapedText() : msg.getText();
            }
        }

        public int calculateWidth(FontMetrics fm) {
            int w = fm.stringWidth(text);
            if (withTimestamp) {
                w += fm.stringWidth(timestamp);
            }
            return w;
        }
    }
}
