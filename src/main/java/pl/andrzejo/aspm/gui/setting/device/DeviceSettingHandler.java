/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.gui.setting.device;

import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.device.DeviceDescriptionEvent;
import pl.andrzejo.aspm.eventbus.events.device.DeviceListChangedEvent;
import pl.andrzejo.aspm.eventbus.events.device.SelectDeviceEvent;
import pl.andrzejo.aspm.eventbus.impl.Subscribe;
import pl.andrzejo.aspm.settings.appsettings.items.device.TtyDeviceSetting;
import pl.andrzejo.aspm.settings.guihandlers.ListSettingHandler;
import pl.andrzejo.aspm.settings.types.DeviceConfig;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.swing.SwingUtilities.invokeLater;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static pl.andrzejo.aspm.factory.BeanFactory.instance;

public class DeviceSettingHandler extends ListSettingHandler<DeviceConfig, String> {

    public DeviceSettingHandler(TtyDeviceSetting setting, DeviceConfig config, DeviceConfig defValue) {
        super(setting, config, config::setDevice, config::getDevice, defValue.getDevice(), false);
        instance(ApplicationEventBus.class).register(this);
    }

    @Override
    protected void fillItems(LinkedHashMap<String, String> items) {
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(DeviceListChangedEvent event) {
        invokeLater(() -> setNewValues(toMap(event.getDevices())));
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(DeviceDescriptionEvent event) {
        invokeLater(() -> setNewValues(toMap(event.getDesc())));
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(SelectDeviceEvent event) {
        invokeLater(() -> selectValue(event.getDevice()));
    }

    private LinkedHashMap<String, String> toMap(List<String> devices) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        devices.forEach(s -> {
            String oldLabel = items.get(s);
            map.put(s, oldLabel == null ? s : oldLabel);
        });
        return sort(map);
    }

    private LinkedHashMap<String, String> sort(LinkedHashMap<String, String> map) {
        LinkedHashMap<String, String> sorted = new LinkedHashMap<>();
        map.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> sorted.put(e.getKey(), e.getValue()));
        return sorted;
    }

    private LinkedHashMap<String, String> toMap(Map<String, String> m) {
        Map<String, String> map = m.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, this::desc));
        return sort(new LinkedHashMap<>(map));
    }

    private String desc(Map.Entry<String, String> v) {
        if (isNotBlank(v.getValue())) {
            return v.getKey() + " - " + v.getValue();
        }
        return v.getKey();
    }
}
