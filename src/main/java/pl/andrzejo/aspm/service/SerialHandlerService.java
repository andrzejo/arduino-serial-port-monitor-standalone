package pl.andrzejo.aspm.service;

import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.SerialMessageReceivedEvent;
import pl.andrzejo.aspm.serial.Serial;
import pl.andrzejo.aspm.serial.SerialException;
import pl.andrzejo.aspm.settings.appsettings.TtyDeviceSetting;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class SerialHandlerService {
    private static Logger logger = LoggerFactory.getLogger(SerialHandlerService.class);

    private final ScheduledExecutorService executor;
    private final ApplicationEventBus eventBus;
    private Serial serial;

    public SerialHandlerService() {
        executor = Executors.newSingleThreadScheduledExecutor();
        eventBus = ApplicationEventBus.instance();
        eventBus.register(this);
    }

    private void openSerial() {
        if (serial != null || true) return;

        try {
            serial = new Serial("/dev/ttyUSB0", 9600) {
                @Override
                protected void message(char buff[], int n) {
                    String msg = new String(buff);
                    eventBus.post(new SerialMessageReceivedEvent(msg));
                }
            };
        } catch (SerialException e) {
            throw new RuntimeException(e);
        }
    }

    @Subscribe
    public void handleEvent(TtyDeviceSetting config) {
        logger.info("Config: {}", config.get());
    }

    public void start() {

    }
}
