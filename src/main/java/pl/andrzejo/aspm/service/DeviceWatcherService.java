package pl.andrzejo.aspm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.app.ApplicationStartedEvent;
import pl.andrzejo.aspm.eventbus.events.device.DeviceDescriptionEvent;
import pl.andrzejo.aspm.eventbus.events.device.DeviceListChangedEvent;
import pl.andrzejo.aspm.eventbus.impl.Subscribe;
import pl.andrzejo.aspm.factory.ObjectFactory;
import pl.andrzejo.aspm.serial.PortDescriptionFetcher;
import pl.andrzejo.aspm.serial.SerialPorts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static pl.andrzejo.aspm.factory.ObjectFactory.instance;

public class DeviceWatcherService {
    private static final Logger logger = LoggerFactory.getLogger(DeviceWatcherService.class);
    private final ScheduledExecutorService watcherExecutor;
    private final ExecutorService descProviderExecutor;
    private final ApplicationEventBus eventBus;
    private final List<String> lastDevices = new ArrayList<>();
    private final Map<String, String> lastDesc = new HashMap<>();
    private final PortDescriptionFetcher descriptionFetcher;
    private Future<?> descFetcherFuture;

    private DeviceWatcherService() {
        watcherExecutor = Executors.newSingleThreadScheduledExecutor();
        descProviderExecutor = Executors.newSingleThreadExecutor();
        eventBus = instance(ApplicationEventBus.class);
        eventBus.register(this);
        descriptionFetcher = new PortDescriptionFetcher();
    }

    public void start() {
        watcherExecutor.scheduleWithFixedDelay(this::checkDevices, 0, 1, TimeUnit.SECONDS);
    }

    public void checkDevices() {
        try {
            List<String> newDevices = SerialPorts.getList();
            if (!lastDevices.equals(newDevices)) {
                logger.info("TTY devices list changed: {} -> {} ", lastDevices, newDevices);
                setNewDevices(newDevices);
                fetchDeviceDescriptions(newDevices);
                triggerEvent();
            }
        } catch (Exception e) {
            logger.error("checkDevices error", e);
        }
    }

    private void fetchDeviceDescriptions(List<String> newDevices) {
        if (descFetcherFuture != null) {
            descFetcherFuture.cancel(true);
        }
        descFetcherFuture = descProviderExecutor.submit(() -> {
            Map<String, String> desc = descriptionFetcher.fetch(newDevices);
            if (!lastDesc.equals(desc)) {
                logger.info("TTY device desc changed: {} -> {} ", lastDesc, desc);
                lastDesc.clear();
                lastDesc.putAll(desc);
                eventBus.post(new DeviceDescriptionEvent(lastDesc));
            }
        });
    }

    private void setNewDevices(List<String> newDevices) {
        lastDevices.clear();
        lastDevices.addAll(newDevices);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(ApplicationStartedEvent event) {
        triggerEvent();
    }

    private void triggerEvent() {
        eventBus.post(new DeviceListChangedEvent(lastDevices));
    }

}
