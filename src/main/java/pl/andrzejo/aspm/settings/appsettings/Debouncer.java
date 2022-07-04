package pl.andrzejo.aspm.settings.appsettings;

import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Debouncer<T> {
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final int millis;
    private Consumer<AppSetting<T>> handler;
    private long nextExecTimestamp;
    private AppSetting<T> current;

    public Debouncer(Consumer<AppSetting<T>> handler, int millis) {
        this.handler = handler;
        this.millis = millis;
        executor.scheduleWithFixedDelay(this::run, 0, 10, TimeUnit.MILLISECONDS);
    }

    private void run() {
        if (nextExecTimestamp != 0 && nextExecTimestamp < System.currentTimeMillis()) {
            nextExecTimestamp = 0;
            handler.accept(current);
            LoggerFactory.getLogger("ddd").info("Debouncd {}", current);
        }
    }

    public void debounce(AppSetting<T> data) {
        current = data;
        nextExecTimestamp = System.currentTimeMillis() + millis;
    }
}
