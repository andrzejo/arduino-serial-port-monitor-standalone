package pl.andrzejo.aspm.settings.appsettings.items.viewer;

import pl.andrzejo.aspm.settings.types.IntSetting;

public class FontSizeSetting extends GroupViewerSetting<Integer> {
    protected FontSizeSetting() {
        super(IntSetting.class, "fontSize", 12);
    }
}
