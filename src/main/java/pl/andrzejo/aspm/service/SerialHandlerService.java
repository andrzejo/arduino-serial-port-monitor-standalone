package pl.andrzejo.aspm.service;

import com.google.common.eventbus.Subscribe;
import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.SerialMessageReceived;
import pl.andrzejo.aspm.serial.Serial;
import pl.andrzejo.aspm.serial.SerialException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class SerialHandlerService {
    private final ScheduledExecutorService executor;
    private final ApplicationEventBus eventBus;
    private Serial serial;

    public SerialHandlerService() {
        executor = Executors.newSingleThreadScheduledExecutor();
        eventBus = ApplicationEventBus.instance();
        openSerial();
    }

    private void openSerial() {
        if (serial != null) return;

        try {
            serial = new Serial("/dev/ttyUSB0", 9600) {
                @Override
                protected void message(char buff[], int n) {
                    String msg = new String(buff);
                    eventBus.post(new SerialMessageReceived(msg));
                }
            };
        } catch (SerialException e) {
            throw new RuntimeException(e);
        }
    }

    @Subscribe
    public void handleEvent(SerialMessageReceived event) {

    }
}
