package pl.andrzejo.aspm.settings.guihandlers;

import com.google.common.eventbus.Subscribe;
import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.SettingsResetToDefaultEvent;
import pl.andrzejo.aspm.settings.appsettings.TtyDeviceSetting;
import pl.andrzejo.aspm.settings.types.DeviceConfig;

import javax.swing.*;
import java.util.function.Consumer;

import static pl.andrzejo.aspm.gui.util.ComponentListenerHandler.handleAction;

public class CheckBoxSettingsHandler {
    protected final TtyDeviceSetting setting;
    protected final DeviceConfig config;
    protected final Consumer<Boolean> setter;
    protected final Boolean initialValue;
    protected final Boolean defValue;
    private final boolean handleRestToDefault;
    protected JCheckBox checkBox;

    public CheckBoxSettingsHandler(TtyDeviceSetting setting, DeviceConfig config, Consumer<Boolean> setter, Boolean initialValue, Boolean defValue, boolean handleRestToDefault) {
        this.setting = setting;
        this.config = config;
        this.setter = setter;
        this.initialValue = initialValue;
        this.defValue = defValue;
        this.handleRestToDefault = handleRestToDefault;
        if (handleRestToDefault) {
            ApplicationEventBus.instance().register(this);
        }
    }

    public void setupComponent(JCheckBox box) {
        checkBox = box;
        box.setSelected(initialValue);
        box.addActionListener(handleAction((e) -> onChange()));
    }
    private void onChange() {
        boolean val = checkBox.isSelected();
        setter.accept(val);
        setting.set(config);
    }

    @Subscribe
    public void handleEvent(SettingsResetToDefaultEvent event) {
        if (handleRestToDefault && checkBox != null) {
            checkBox.setSelected(defValue);
            onChange();
        }
    }

}
