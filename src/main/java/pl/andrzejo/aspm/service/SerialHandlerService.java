/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.service;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.api.commands.ApiCloseDeviceEvent;
import pl.andrzejo.aspm.eventbus.events.api.commands.ApiOpenDeviceEvent;
import pl.andrzejo.aspm.eventbus.events.app.ApplicationClosingEvent;
import pl.andrzejo.aspm.eventbus.events.app.ApplicationStartedEvent;
import pl.andrzejo.aspm.eventbus.events.command.CommandExecutedEvent;
import pl.andrzejo.aspm.eventbus.events.command.ExecuteCommandEvent;
import pl.andrzejo.aspm.eventbus.events.device.*;
import pl.andrzejo.aspm.eventbus.events.serial.SerialMessageReceivedEvent;
import pl.andrzejo.aspm.eventbus.impl.Subscribe;
import pl.andrzejo.aspm.factory.BeanFactory;
import pl.andrzejo.aspm.serial.Serial;
import pl.andrzejo.aspm.serial.SerialException;
import pl.andrzejo.aspm.serial.SerialPorts;
import pl.andrzejo.aspm.settings.appsettings.AppSettingGetter;
import pl.andrzejo.aspm.settings.appsettings.AppSettingsFactory;
import pl.andrzejo.aspm.settings.appsettings.items.device.LastDeviceSetting;
import pl.andrzejo.aspm.settings.appsettings.items.device.TtyDeviceSetting;
import pl.andrzejo.aspm.settings.appsettings.items.monitor.AutoOpenSetting;
import pl.andrzejo.aspm.settings.types.DeviceConfig;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static pl.andrzejo.aspm.factory.BeanFactory.instance;

public class SerialHandlerService {
    private static final Logger logger = LoggerFactory.getLogger(SerialHandlerService.class);
    private final ApplicationEventBus eventBus;
    private final LastDeviceSetting lastDeviceSetting;
    private Serial serial;
    private DeviceConfig config;
    private DeviceConfig openDeviceConfig;
    private boolean autoOpen;
    private boolean applicationStarted = false;

    private SerialHandlerService() {
        lastDeviceSetting = AppSettingsFactory.create(LastDeviceSetting.class);
        config = AppSettingGetter.get(TtyDeviceSetting.class);
        eventBus = instance(ApplicationEventBus.class);
        eventBus.register(this);
    }

    private void openSerial() {
        if (!isValidDevice()) {
            return;
        }
        openSerial(config);
    }

    private void openSerial(DeviceConfig config) {
        try {
            logger.info("Open serial: {}", config);
            serial = BeanFactory.newInstance(Serial.class, createSerial(config));
            openDeviceConfig = config;
            eventBus.post(new DeviceOpenEvent(config));
        } catch (Exception e) {
            if (e.getCause() instanceof SerialException) {
                eventBus.post(new DeviceErrorEvent(e.getMessage()));
            } else {
                eventBus.post(new DeviceCloseEvent(config));
            }
        }
    }

    private Supplier<Serial> createSerial(DeviceConfig config) {
        return () -> {
            try {
                return new Serial(config.getDevice(), config.getBaud(), config.getParity(), config.getDataBits(), config.getStopBits(), config.isRTS(), config.isDTR()) {
                    @Override
                    protected void message(char[] buff, int n) {
                        String msg = new String(buff);
                        eventBus.post(new SerialMessageReceivedEvent(msg));
                    }
                };
            } catch (SerialException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(AutoOpenSetting autoOpen) {
        this.autoOpen = autoOpen.get();
        if (this.autoOpen && config != null && !isOpen()) {
            if (isLastDevice()) {
                openSerial();
            }
        }
    }

    private boolean isLastDevice() {
        if (config == null) {
            return false;
        }
        return StringUtils.equals(config.getDevice(), lastDeviceSetting.get());
    }

    private boolean isOpen() {
        return serial != null;
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(TtyDeviceSetting config) {
        if (!applicationStarted) {
            return;
        }
        DeviceConfig deviceConfig = config.get();
        if (!deviceConfig.equals(this.config)) {
            this.config = deviceConfig;
            logger.info("New TtyDeviceSetting: {}", deviceConfig);
            if (autoOpen && isLastDevice()) {
                reopenDevice();
            }
        }
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(ExecuteCommandEvent event) {
        if (serial != null) {
            String command = event.getCommand() + event.getLineEnding();
            serial.write(command);
            eventBus.post(new CommandExecutedEvent(event.getCommand()));
        }
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(ApiCloseDeviceEvent event) {
        if (isOpen()) {
            closeSerial();
        }
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(ApiOpenDeviceEvent event) {
        if (!isOpen()) {
            String device = event.getDevice();
            if (isNotBlank(device) && config != null) {
                DeviceConfig clone = config.clone();
                clone.setDevice(device);
                openSerial(clone);
            } else {
                openSerial();
            }
        }
    }

    private boolean isValidDevice(String device) {
        List<String> devices = instance(SerialPorts.class).getList();
        return devices.contains(device);
    }

    private boolean isValidDevice() {
        if (config != null) {
            return isValidDevice(config.getDevice());
        }
        return false;
    }

    private void reopenDevice() {
        closeSerial();
        openSerial();
    }

    private void closeSerial() {
        if (isOpen()) {
            try {
                logger.info("Close serial");
                serial.dispose();
            } catch (IOException e) {
                logger.warn("Failed to close device");
            } finally {
                serial = null;
                eventBus.post(new DeviceCloseEvent(openDeviceConfig));
            }
        }
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(ToggleDeviceStatusEvent event) {
        if (isOpen()) {
            closeSerial();
        } else {
            lastDeviceSetting.set(config.getDevice());
            openSerial();
        }
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(DeviceListChangedEvent event) {
        if (config != null) {
            String device = config.getDevice();
            if (!event.getDevices().contains(device)) {
                closeSerial();
            }
        }
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(ApplicationClosingEvent event) {
        if (isOpen()) {
            closeSerial();
        }
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(ApplicationStartedEvent event) {
        logger.info("ApplicationStarted: {}", event);
        applicationStarted = true;
        eventBus.post(new SelectDeviceEvent(lastDeviceSetting.get()));
    }

    public void start() {

    }

    public Status getStatus() {
        return new Status(openDeviceConfig, isOpen());
    }

    public static class Status {
        private final DeviceConfig config;
        private final boolean isOpen;

        public Status(DeviceConfig config, boolean isOpen) {
            this.config = config;
            this.isOpen = isOpen;
        }

        public DeviceConfig getConfig() {
            return config;
        }

        public boolean isOpen() {
            return isOpen;
        }

        public String toHumanReadableString() {
            StringBuilder builder = new StringBuilder();
            addLine(builder, "DeviceOpen", isOpen);
            if (isOpen) {
                addLine(builder, "Device", config.getDevice());
                addLine(builder, "Baud", config.getBaud());
                addLine(builder, "Parity", config.getParity());
                addLine(builder, "DataBits", config.getDataBits());
                addLine(builder, "StopBits", config.getStopBits());
                addLine(builder, "DTR", config.isDTR());
                addLine(builder, "RTS", config.isRTS());
            }

            return builder.toString();
        }

        private void addLine(StringBuilder builder, String label, Object value) {
            builder
                    .append(label)
                    .append(": ")
                    .append(value)
                    .append("\n");
        }
    }
}
