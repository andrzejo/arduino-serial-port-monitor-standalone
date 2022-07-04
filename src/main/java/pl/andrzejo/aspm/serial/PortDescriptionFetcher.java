package pl.andrzejo.aspm.serial;

import jssc.SerialNativeInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.andrzejo.aspm.serial.description.DescriptionProvider;
import pl.andrzejo.aspm.serial.description.PortDescriptionProviderWin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PortDescriptionFetcher {
    private static final Logger logger = LoggerFactory.getLogger(PortDescriptionFetcher.class);
    private final DescriptionProvider descriptionProvider;

    public PortDescriptionFetcher() {
        switch (SerialNativeInterface.getOsType()) {
            case SerialNativeInterface.OS_LINUX: {
                descriptionProvider = null;
                break;
            }

            case SerialNativeInterface.OS_WINDOWS: {
                descriptionProvider = new PortDescriptionProviderWin();
                break;
            }

            default:
                descriptionProvider = null;
        }
    }

    public Map<String, String> fetch(List<String> p) {
        try {
            if (descriptionProvider != null) {
                Map<String, String> desc = descriptionProvider.get(p);
                if (desc != null) {
                    return desc;
                }
            }
        } catch (Exception e) {
            logger.warn("Device description fetch failed.", e);
        }
        return new HashMap<>();
    }
}
