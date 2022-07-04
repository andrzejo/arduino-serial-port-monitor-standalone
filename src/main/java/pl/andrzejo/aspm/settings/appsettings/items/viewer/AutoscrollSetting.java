package pl.andrzejo.aspm.settings.appsettings.items.viewer;

import pl.andrzejo.aspm.settings.types.BoolSetting;

public class AutoscrollSetting extends GroupViewerSetting<Boolean> {
    protected AutoscrollSetting() {
        super(BoolSetting.class, "autoScroll", false);
    }
}
