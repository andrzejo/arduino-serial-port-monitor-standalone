package pl.andrzejo.aspm.serial.description;

import java.util.List;
import java.util.Map;

public interface DescriptionProvider {
    Map<String, String> get(List<String> ports);
}
