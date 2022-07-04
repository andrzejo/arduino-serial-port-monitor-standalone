package pl.andrzejo.aspm.settings.guihandlers;

import com.google.common.eventbus.Subscribe;
import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.SettingsResetToDefaultEvent;
import pl.andrzejo.aspm.settings.appsettings.TtyDeviceSetting;
import pl.andrzejo.aspm.settings.types.DeviceConfig;

import javax.swing.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static pl.andrzejo.aspm.gui.util.ComponentListenerHandler.handleAction;

public abstract class ListSettingHandler<T> {
    protected final LinkedHashMap<T, String> items = new LinkedHashMap<>();
    protected final List<T> values = new ArrayList<>();
    protected final TtyDeviceSetting setting;
    protected final DeviceConfig config;
    protected final Consumer<T> setter;
    protected final Supplier<T> getter;
    protected final T defValue;
    private final boolean handleRestToDefault;
    protected JComboBox<String> combo;

    public ListSettingHandler(TtyDeviceSetting setting,
                              DeviceConfig config,
                              Consumer<T> setter,
                              Supplier<T> getter,
                              T defValue,
                              boolean handleRestToDefault) {
        this.setting = setting;
        this.config = config;
        this.setter = setter;
        this.getter = getter;
        this.defValue = defValue;
        this.handleRestToDefault = handleRestToDefault;
        fillItems(items);
        values.addAll(items.keySet());
        ApplicationEventBus.instance().register(this);
    }

    public void setupComponent(JComboBox<String> component) {
        combo = component;
        setComboValue();
        component.addActionListener(handleAction((e) -> {
            int idx = component.getSelectedIndex();
            T val = (idx > -1) ? values.get(idx) : defValue;
            T last = getter.get();
            if (last != null && !last.equals(val)) {
                setter.accept(val);
                setting.set(config);
            }
        }));
    }

    private void setComboValue() {
        if (combo != null) {
            combo.removeAllItems();
            items.values().forEach(combo::addItem);
            combo.setSelectedIndex(itemIndexOrDefault());
        }
    }

    protected abstract void fillItems(LinkedHashMap<T, String> items);

    protected void setNewValues(LinkedHashMap<T, String> newItems) {
        items.clear();
        items.putAll(newItems);
        values.clear();
        values.addAll(items.keySet());
        setComboValue();
    }

    private int itemIndexOrDefault() {
        int index = values.indexOf(getter.get());
        if (index > -1) {
            return index;
        }
        return values.indexOf(defValue);
    }

    @Subscribe
    public void handleEvent(SettingsResetToDefaultEvent event) {
        if (handleRestToDefault && combo != null) {
            combo.setSelectedIndex(values.indexOf(defValue));
        }
    }
}
