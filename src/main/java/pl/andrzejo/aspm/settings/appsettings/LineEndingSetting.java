package pl.andrzejo.aspm.settings.appsettings;

import pl.andrzejo.aspm.settings.types.StringSetting;

public class LineEndingSetting extends GroupMonitorSetting<String> {

    public static final String DEF_VALUE = "\n";

    public LineEndingSetting() {
        super(StringSetting.class, "lineEnding", DEF_VALUE);
    }

}
