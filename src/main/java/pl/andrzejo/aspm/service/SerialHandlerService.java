package pl.andrzejo.aspm.service;

import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.ApplicationClosingEvent;
import pl.andrzejo.aspm.eventbus.events.ApplicationStartedEvent;
import pl.andrzejo.aspm.eventbus.events.SerialMessageReceivedEvent;
import pl.andrzejo.aspm.eventbus.events.device.DeviceCloseEvent;
import pl.andrzejo.aspm.eventbus.events.device.DeviceErrorEvent;
import pl.andrzejo.aspm.eventbus.events.device.DeviceOpenEvent;
import pl.andrzejo.aspm.eventbus.events.device.ToggleDeviceStatusEvent;
import pl.andrzejo.aspm.serial.Serial;
import pl.andrzejo.aspm.serial.SerialException;
import pl.andrzejo.aspm.settings.appsettings.items.monitor.AutoOpenSetting;
import pl.andrzejo.aspm.settings.appsettings.items.device.TtyDeviceSetting;
import pl.andrzejo.aspm.settings.types.DeviceConfig;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class SerialHandlerService {
    private static final Logger logger = LoggerFactory.getLogger(SerialHandlerService.class);
    private final ScheduledExecutorService executor;
    private final ApplicationEventBus eventBus;
    private Serial serial;
    private DeviceConfig config;
    private boolean autoOpen;

    public SerialHandlerService() {
        executor = Executors.newSingleThreadScheduledExecutor();
        eventBus = ApplicationEventBus.instance();
        eventBus.register(this);
    }

    private void openSerial() {
        try {
            logger.info("Open serial: {}", config);
            serial = new Serial(config.getDevice(), config.getBaud(), config.getParity(), config.getDataBits(),
                    config.getStopBits(), config.isRTS(), config.isDTR()) {
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
    public void handleEvent(AutoOpenSetting autoOpen) {
        this.autoOpen = autoOpen.get();
        if (this.autoOpen && config != null && !isOpen()) {
            openSerial();
        }
    }

    private boolean isOpen() {
        return serial != null;
    }

    @Subscribe
    public void handleEvent(TtyDeviceSetting config) {
        DeviceConfig deviceConfig = config.get();
        if (!deviceConfig.equals(this.config)) {
            this.config = deviceConfig;
            logger.info("New TtyDeviceSetting: {}", deviceConfig);
            if (autoOpen) {
                reopenDevice();
            }
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
                logger.warn("Failed to close device: {0}", e);
            } finally {
                serial = null;
                eventBus.post(new DeviceCloseEvent(config));
            }
        }
    }

    @Subscribe
    public void handleEvent(ToggleDeviceStatusEvent event) {
        if (isOpen()) {
            closeSerial();
        } else {
            openSerial();
        }
    }

    @Subscribe
    public void handleEvent(ApplicationClosingEvent event) {
        if (isOpen()) {
            closeSerial();
        }
    }

    @Subscribe
    public void handleEvent(ApplicationStartedEvent event) {
        logger.info("Config: {}", event);
    }

    public void start() {

    }
}
