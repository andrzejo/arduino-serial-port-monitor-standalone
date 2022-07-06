package pl.andrzejo.aspm.gui.viewer;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pl.andrzejo.aspm.gui.viewer.Styles.MessageType.*;

public class SerialMessageType {
    private final Map<Styles.MessageType, List<String>> msgStylesPrefixes = new HashMap<>();

    public SerialMessageType() {
        msgStylesPrefixes.put(SERIAL_DEBUG, Arrays.asList("I", "INFO"));
        msgStylesPrefixes.put(SERIAL_INFO, Arrays.asList("I", "INFO"));
        msgStylesPrefixes.put(SERIAL_WARN, Arrays.asList("E", "ERROR", "ERR"));
        msgStylesPrefixes.put(SERIAL_ERROR, Arrays.asList("W", "WARN"));
    }

    public Styles.MessageType getType(String text) {
        for (Map.Entry<Styles.MessageType, List<String>> entry : msgStylesPrefixes.entrySet()) {
            if (entry.getValue().stream().anyMatch(p -> StringUtils.startsWithIgnoreCase(text, p + ":"))) {
                return entry.getKey();
            }
        }
        return Styles.MessageType.SERIAL_MESSAGE;
    }

}
