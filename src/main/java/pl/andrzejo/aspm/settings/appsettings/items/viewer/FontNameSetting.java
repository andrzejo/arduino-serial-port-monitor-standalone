package pl.andrzejo.aspm.settings.appsettings.items.viewer;

import pl.andrzejo.aspm.settings.types.StringSetting;

public class FontNameSetting extends GroupViewerSetting<String> {
    protected FontNameSetting() {
        super(StringSetting.class, "fontName", "Monospaced");
    }
}
