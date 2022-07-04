package pl.andrzejo.aspm.settings.appsettings.items.viewer;

import pl.andrzejo.aspm.settings.types.BoolSetting;

public class AddTimestampSetting extends GroupViewerSetting<Boolean> {
    protected AddTimestampSetting() {
        super(BoolSetting.class, "addTimestamp", false);
    }
}
