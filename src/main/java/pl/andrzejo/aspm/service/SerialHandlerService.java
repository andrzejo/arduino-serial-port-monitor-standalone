package pl.andrzejo.aspm.service;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.*;
import pl.andrzejo.aspm.eventbus.events.device.DeviceCloseEvent;
import pl.andrzejo.aspm.eventbus.events.device.DeviceErrorEvent;
import pl.andrzejo.aspm.eventbus.events.device.DeviceOpenEvent;
import pl.andrzejo.aspm.eventbus.events.device.ToggleDeviceStatusEvent;
import pl.andrzejo.aspm.eventbus.impl.Subscribe;
import pl.andrzejo.aspm.serial.Serial;
import pl.andrzejo.aspm.serial.SerialException;
import pl.andrzejo.aspm.settings.appsettings.AppSettingsFactory;
import pl.andrzejo.aspm.settings.appsettings.items.device.LastDeviceSetting;
import pl.andrzejo.aspm.settings.appsettings.items.device.TtyDeviceSetting;
import pl.andrzejo.aspm.settings.appsettings.items.monitor.AutoOpenSetting;
import pl.andrzejo.aspm.settings.types.DeviceConfig;

import java.io.IOException;

public class SerialHandlerService {
    private static final Logger logger = LoggerFactory.getLogger(SerialHandlerService.class);
    private final ApplicationEventBus eventBus;
    private final LastDeviceSetting lastDeviceSetting;
    private Serial serial;
    private DeviceConfig config;
    private boolean autoOpen;

    public SerialHandlerService() {
        eventBus = ApplicationEventBus.instance();
        eventBus.register(this);
        lastDeviceSetting = AppSettingsFactory.create(LastDeviceSetting.class);
    }

    private void openSerial() {
        try {
            logger.info("Open serial: {}", config);
            serial = new Serial(config.getDevice(), config.getBaud(), config.getParity(), config.getDataBits(), config.getStopBits(), config.isRTS(), config.isDTR()) {
                @Override
                protected void message(char[] buff, int n) {
                    String msg = new String(buff);
                    eventBus.post(new SerialMessageReceivedEvent(msg));
                }
            };
            eventBus.post(new DeviceOpenEvent(config));
        } catch (SerialException e) {
            eventBus.post(new DeviceErrorEvent(e.getMessage()));
        } catch (Exception e) {
            eventBus.post(new DeviceCloseEvent(config));
        }
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
        }
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
                eventBus.post(new DeviceCloseEvent(config));
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
        logger.info("Config: {}", event);
    }

    public void start() {

    }
}
