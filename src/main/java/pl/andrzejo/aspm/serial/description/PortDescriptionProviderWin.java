package pl.andrzejo.aspm.serial.description;

import com.sun.istack.internal.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PortDescriptionProviderWin extends DescriptionProviderCommand {
    private static final String Command = "Get-WMIObject -Query \\\"SELECT Caption FROM Win32_PnPEntity WHERE Caption LIKE '%(COM%' \\\" | findstr 'Caption ' ";

    public PortDescriptionProviderWin() {
        super(Command);
    }

    @Override
    @NotNull
    protected Map<String, String> getDescriptions(List<String> ports, List<String> descriptions) {
        return ports.stream().collect(Collectors.toMap(Function.identity(), p -> getDesc(p, descriptions)));
    }

    private String getDesc(String port, List<String> descriptions) {
        for (String s : descriptions) {
            String label = String.format("(%s)", port);
            if (s.contains(label)) {
                String[] split = s.split("Caption\\s+:");
                if (split.length > 1) {
                    return split[1].replace(label, "").trim();
                }
            }
        }
        return "";
    }
}
