package pl.andrzejo.aspm.settings.guihandlers;

import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.setting.SettingsResetToDefaultEvent;
import pl.andrzejo.aspm.eventbus.impl.Subscribe;
import pl.andrzejo.aspm.settings.appsettings.AppSetting;

import javax.swing.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static pl.andrzejo.aspm.gui.util.ComponentListenerHandler.handleAction;

public abstract class ListSettingHandler<T, I> {
    protected final LinkedHashMap<I, String> items = new LinkedHashMap<>();
    protected final List<I> values = new ArrayList<>();
    protected final AppSetting<T> setting;
    protected final T configObject;
    protected final Consumer<I> setter;
    protected final Supplier<I> getter;
    protected final I defValue;
    private final boolean handleRestToDefault;
    protected JComboBox<String> combo;

    public ListSettingHandler(AppSetting<T> setting,
                              T configObject,
                              Consumer<I> setter,
                              Supplier<I> getter,
                              I defValue,
                              boolean handleRestToDefault) {
        this.setting = setting;
        this.configObject = configObject;
        this.setter = setter;
        this.getter = getter;
        this.defValue = defValue;
        this.handleRestToDefault = handleRestToDefault;
        fillItems(items);
        values.addAll(items.keySet());
        ApplicationEventBus.instance().register(this);
    }

    public ListSettingHandler(Consumer<I> setter,
                              Supplier<I> getter,
                              I defValue,
                              boolean handleRestToDefault) {
        this(null, null, setter, getter, defValue, handleRestToDefault);
    }

    public void setupComponent(JComboBox<String> component) {
        combo = component;
        setComboValue();
        component.addActionListener(handleAction((e) -> {
            int idx = component.getSelectedIndex();
            I val = (idx > -1) ? values.get(idx) : defValue;
            I last = getter.get();
            if (last != null && !last.equals(val)) {
                setter.accept(val);
                if (configObject != null) {
                    setting.set(configObject);
                }
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

    protected abstract void fillItems(LinkedHashMap<I, String> items);

    protected void setNewValues(LinkedHashMap<I, String> newItems) {
        items.clear();
        items.putAll(newItems);
        values.clear();
        values.addAll(items.keySet());
        setComboValue();
    }

    protected void selectValue(I value) {
        if (combo != null && value != null) {
            combo.setSelectedIndex(itemIndexOrDefault(value));
        }
    }

    private int itemIndexOrDefault() {
        return itemIndexOrDefault(getter.get());
    }

    private int itemIndexOrDefault(I value) {
        int index = values.indexOf(value);
        if (index > -1) {
            return index;
        }
        return values.indexOf(defValue);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(SettingsResetToDefaultEvent event) {
        if (handleRestToDefault && combo != null) {
            combo.setSelectedIndex(values.indexOf(defValue));
        }
    }
}
