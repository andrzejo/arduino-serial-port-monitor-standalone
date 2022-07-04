package pl.andrzejo.aspm.service;

import com.google.common.eventbus.Subscribe;
import jssc.SerialPortList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.ApplicationStartedEvent;
import pl.andrzejo.aspm.eventbus.events.DeviceListChangedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DeviceWatcherService {
    private static Logger logger = LoggerFactory.getLogger(DeviceWatcherService.class);
    private final ScheduledExecutorService executor;
    private final ApplicationEventBus eventBus;
    private final List<String> lastDevices = new ArrayList<>();

    public DeviceWatcherService() {
        executor = Executors.newSingleThreadScheduledExecutor();
        eventBus = ApplicationEventBus.instance();
        eventBus.register(this);
    }

    public void start() {
        executor.scheduleAtFixedRate(this::checkDevices, 0, 3, TimeUnit.SECONDS);
    }

    public void checkDevices() {
        List<String> newDevices = Arrays.asList(SerialPortList.getPortNames());
        if (!lastDevices.equals(newDevices)) {
            setNewDevices(newDevices);
            logger.info("TTY devices list changed: {}", newDevices);
            triggerEvent();
        }
    }

    private void setNewDevices(List<String> newDevices) {
        lastDevices.clear();
        lastDevices.addAll(newDevices);
    }

    @Subscribe
    public void handleEvent(ApplicationStartedEvent event) {
        triggerEvent();
    }

    private void triggerEvent() {
        eventBus.post(new DeviceListChangedEvent(lastDevices));
    }

}
