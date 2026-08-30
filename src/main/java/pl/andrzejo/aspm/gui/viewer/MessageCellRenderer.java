/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2026.
 */

package pl.andrzejo.aspm.gui.viewer;

import pl.andrzejo.aspm.factory.BeanFactory;
import pl.andrzejo.aspm.gui.viewer.model.Message;

import javax.swing.*;
import java.awt.*;

import static pl.andrzejo.aspm.gui.viewer.model.MessageType.TIME;

public class MessageCellRenderer extends JPanel implements ListCellRenderer<Message> {
    private Message currentMessage;
    private boolean isSelected;
    private Color listBg, listSelectionBg, listSelectionFg;
    private boolean renderTimestamp;
    private final Styles styles = BeanFactory.instance(Styles.class);

    public MessageCellRenderer() {
        //setLayout(new BorderLayout(8, 0));
        setOpaque(true);
/*
        timestampLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        timestampLabel.setForeground(Color.GRAY);

        add(timestampLabel, BorderLayout.WEST);
        add(textLabel, BorderLayout.CENTER);
*/
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
        /*
        timestampLabel.setText(entry.getFormattedTimestamp());
        textLabel.setText(entry.getText());

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            timestampLabel.setForeground(list.getSelectionForeground());
            textLabel.setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            timestampLabel.setForeground(Color.GRAY);
            applyStyle(entry.getStyle(), list);
        }
*/
        return this;
    }

    /*
        private void applyStyle(MessageType type, JList<?> list) {
            switch (type) {
                case INTERNAL_ERROR:
                    textLabel.setForeground(new Color(139, 0, 0));
                    break;

                case INTERNAL_INFO:
                    textLabel.setForeground(new Color(0, 100, 0));
                    break;

                case INTERNAL_MESSAGE:
                    textLabel.setForeground(new Color(0, 80, 180));
                    break;

                default:
                    textLabel.setForeground(list.getForeground());
            }
        }
    */
    public void renderTimestamp(boolean renderTimestamp) {
        this.renderTimestamp = renderTimestamp;
//        timestampLabel.setVisible(renderTimestamp);
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

        String timestampStr = renderTimestamp ? currentMessage.getFormattedTimestamp() + " " : "";

        if (isSelected) {
            g.setColor(listSelectionFg);
            String fullLine = timestampStr + currentMessage.getText();
            g.drawString(fullLine, currentX, textY);
            return;
        }

        currentX += drawString(g, currentX, textY, styles.get(TIME), timestampStr);
        drawString(g, currentX, textY, styles.get(currentMessage.getType()), currentMessage.getText());
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
}
