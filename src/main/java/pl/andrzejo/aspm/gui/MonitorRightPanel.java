package pl.andrzejo.aspm.gui;

import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.gui.ClearMonitorOutputEvent;
import pl.andrzejo.aspm.eventbus.events.gui.FontChangedEvent;
import pl.andrzejo.aspm.settings.appsettings.AppSettingsFactory;
import pl.andrzejo.aspm.settings.appsettings.items.viewer.FontNameSetting;
import pl.andrzejo.aspm.settings.appsettings.items.viewer.SaveLogToFile;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static pl.andrzejo.aspm.gui.util.ComponentListenerHandler.handleAction;

public class MonitorRightPanel extends ContentPanel {

    private final FontRenderContext fontRenderContext = new FontRenderContext(null, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT, RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT);


    public MonitorRightPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(0, 10, 0, 10));

        JButton clear = new JButton("Clear output");
        add(clear, BorderLayout.NORTH);
        clear.addActionListener(handleAction((e) -> ApplicationEventBus.instance().post(new ClearMonitorOutputEvent())));

        JPanel panel = new JPanel();
        JComboBox<String> fontsCombo = new JComboBox<>();
        setPreferredWidthSize(fontsCombo);
        fillFonts(fontsCombo, true);
        panel.add(createLabeled("Font", fontsCombo));
        add(panel, BorderLayout.CENTER);

        setupFontCombo(fontsCombo);

        JCheckBox saveToFile = new JCheckBox("Save log to file");
        handleCheckboxSetting(saveToFile, AppSettingsFactory.create(SaveLogToFile.class));
        add(saveToFile, BorderLayout.SOUTH);
    }

    private void setupFontCombo(JComboBox<String> fontsCombo) {
        FontNameSetting setting = AppSettingsFactory.create(FontNameSetting.class);
        ApplicationEventBus eventBus = ApplicationEventBus.instance();
        fontsCombo.setSelectedItem(setting.get());
        fontsCombo.addActionListener(handleAction((e) -> {
            String name = (String) fontsCombo.getSelectedItem();
            setting.set(name);
            eventBus.post(new FontChangedEvent(name));
        }));
    }

    private void fillFonts(JComboBox<String> fontsCombo, boolean mono) {
        List<Font> fonts = Arrays
                .stream(GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts())
                .filter(f -> mono && isMonospaced(f))
                .collect(Collectors.toList());
        fontsCombo.removeAllItems();
        fonts.forEach(f -> fontsCombo.addItem(f.getName()));
    }

    private boolean isMonospaced(Font font) {
        Rectangle2D iBounds = font.getStringBounds("l", fontRenderContext);
        Rectangle2D mBounds = font.getStringBounds("m", fontRenderContext);
        return iBounds.getWidth() == mBounds.getWidth();
    }
}
