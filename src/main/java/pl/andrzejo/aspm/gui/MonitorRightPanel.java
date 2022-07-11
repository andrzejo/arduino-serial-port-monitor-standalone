package pl.andrzejo.aspm.gui;

import org.apache.commons.lang.StringUtils;
import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.gui.ClearMonitorOutputEvent;
import pl.andrzejo.aspm.eventbus.events.gui.FontChangedEvent;
import pl.andrzejo.aspm.settings.appsettings.AppSettingsFactory;
import pl.andrzejo.aspm.settings.appsettings.items.viewer.FontNameSetting;
import pl.andrzejo.aspm.settings.appsettings.items.viewer.FontSizeSetting;
import pl.andrzejo.aspm.settings.appsettings.items.viewer.SaveLogToFile;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static pl.andrzejo.aspm.factory.BeanFactory.instance;
import static pl.andrzejo.aspm.gui.util.ComponentListenerHandler.handleAction;

public class MonitorRightPanel extends ContentPanel {

    private final FontRenderContext fontRenderContext = new FontRenderContext(null, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT, RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT);
    private final ApplicationEventBus eventBus;
    private Integer fontSize;
    private String fontName;


    public MonitorRightPanel() {
        eventBus = instance(ApplicationEventBus.class);

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(0, 10, 0, 10));

        JButton clear = new JButton("Clear output");
        add(clear, BorderLayout.NORTH);
        clear.addActionListener(handleAction((e) -> instance(ApplicationEventBus.class).post(new ClearMonitorOutputEvent())));

        JPanel panel = new JPanel();
        JComboBox<String> fontsCombo = new JComboBox<>();
        setPreferredWidthSize(fontsCombo);
        fillFonts(fontsCombo);

        panel.add(createLabeled("Font", fontsCombo));
        SpinnerListModel fontSize = new SpinnerListModel(Arrays.asList(8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 26, 28, 36, 48));
        JSpinner fontSizeSpinner = new JSpinner(fontSize);
        setupFontSizeSpinner(fontSizeSpinner);
        panel.add(createLabeled("Size", fontSizeSpinner));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel panel2 = new JPanel();
        panel2.add(panel);
        add(panel2, BorderLayout.CENTER);

        setupFontCombo(fontsCombo);

        JCheckBox saveToFile = new JCheckBox("Save log to file");
        handleCheckboxSetting(saveToFile, AppSettingsFactory.create(SaveLogToFile.class));
        add(saveToFile, BorderLayout.SOUTH);
    }

    private void setupFontSizeSpinner(JSpinner fontSizeSpinner) {
        FontSizeSetting setting = AppSettingsFactory.create(FontSizeSetting.class);
        fontSize = setting.get();
        fontSizeSpinner.setValue(fontSize);
        fontSizeSpinner.addChangeListener(e -> {
            fontSize = (Integer) fontSizeSpinner.getValue();
            setting.set(fontSize);
            eventBus.post(new FontChangedEvent(fontName, fontSize));
        });
    }

    private void setupFontCombo(JComboBox<String> fontsCombo) {
        FontNameSetting setting = AppSettingsFactory.create(FontNameSetting.class);
        fontsCombo.setSelectedItem(setting.get());
        fontName = setting.get();
        fontsCombo.addActionListener(handleAction((e) -> {
            fontName = (String) fontsCombo.getSelectedItem();
            if (StringUtils.startsWith(fontName, "---")) {
                setting.set(fontName);
                eventBus.post(new FontChangedEvent(fontName, fontSize));
            }
        }));
    }

    private void fillFonts(JComboBox<String> fontsCombo) {
        fontsCombo.removeAllItems();
        appendFonts("--- nonospaced ---", true, fontsCombo);
        appendFonts("--- other ---", false, fontsCombo);
    }

    private void appendFonts(String label, boolean mono, JComboBox<String> fontsCombo) {
        List<Font> fonts = getFonts(mono);
        fontsCombo.addItem(label);
        fonts.forEach(f -> fontsCombo.addItem(f.getName()));
    }

    private List<Font> getFonts(boolean mono) {
        return Arrays
                .stream(GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts())
                .filter(font -> {
                    if (mono && isMonospaced(font)) {
                        return true;
                    }
                    return !mono && !isMonospaced(font);
                })
                .collect(Collectors.toList());
    }

    private boolean isMonospaced(Font font) {
        Rectangle2D iBounds = font.getStringBounds("l", fontRenderContext);
        Rectangle2D mBounds = font.getStringBounds("m", fontRenderContext);
        return iBounds.getWidth() == mBounds.getWidth();
    }
}
