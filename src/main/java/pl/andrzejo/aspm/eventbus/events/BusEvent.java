package pl.andrzejo.aspm.eventbus.events;

import org.apache.commons.lang.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;

public class BusEvent {
    @Override
    public String toString() {
        String fields = Arrays.stream(getClass().getDeclaredFields())
                .map(f -> String.format("%s=%s", f.getName(), getField(f)))
                .collect(Collectors.joining(", "));
        return String.format("%s{%s}", getClass().getName(), fields);
    }

    private Object getField(Field f) {
        try {
            return FieldUtils.readField(f, this, true);
        } catch (IllegalAccessException e) {
            return "/* read failed */";
        }
    }
}
