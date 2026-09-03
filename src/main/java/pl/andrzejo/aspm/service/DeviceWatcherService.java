/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2026.
 */

package pl.andrzejo.aspm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.app.ApplicationStartedEvent;
import pl.andrzejo.aspm.eventbus.events.device.DeviceDescriptionEvent;
import pl.andrzejo.aspm.eventbus.events.device.DeviceListChangedEvent;
import pl.andrzejo.aspm.eventbus.impl.Subscribe;
import pl.andrzejo.aspm.serial.SerialPorts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static pl.andrzejo.aspm.factory.BeanFactory.instance;

public class DeviceWatcherService {
    private static final Logger logger = LoggerFactory.getLogger(DeviceWatcherService.class);
    private final ScheduledExecutorService watcherExecutor;
    private final ApplicationEventBus eventBus;
    private final List<String> lastDevices = new ArrayList<>();
    private final Map<String, String> lastDesc = new HashMap<>();
    private final SerialPorts serialPorts;

    private DeviceWatcherService() {
        watcherExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "DeviceWatcher-Thread");
            t.setDaemon(true);
            return t;
        });
        serialPorts = instance(SerialPorts.class);
        eventBus = instance(ApplicationEventBus.class);
        eventBus.register(this);
    }

    public void start() {
        watcherExecutor.scheduleWithFixedDelay(this::checkDevices, 0, 1, TimeUnit.SECONDS);
    }

    public void checkDevices() {
        try {
            List<SerialPorts.Port> ports = serialPorts.getList();
            List<String> newDevices = ports.stream()
                    .map(SerialPorts.Port::getName)
                    .collect(Collectors.toList());

            if (!lastDevices.equals(newDevices)) {
                logger.info("TTY devices list changed: {} -> {} ", lastDevices, newDevices);
                lastDevices.clear();
                lastDevices.addAll(newDevices);

                Map<String, String> desc = new HashMap<>();
                for (SerialPorts.Port p : ports) {
                    desc.put(p.getName(), p.getDesc());
                }

                lastDesc.clear();
                lastDesc.putAll(desc);

                eventBus.post(new DeviceDescriptionEvent(lastDesc));
                eventBus.post(new DeviceListChangedEvent(lastDevices));
            }
        } catch (Exception e) {
            logger.error("checkDevices error", e);
        }
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void handleEvent(ApplicationStartedEvent event) {
        checkDevices();
    }
}
