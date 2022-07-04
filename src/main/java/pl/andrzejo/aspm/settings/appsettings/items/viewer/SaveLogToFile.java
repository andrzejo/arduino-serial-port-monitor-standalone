package pl.andrzejo.aspm.settings.appsettings.items.viewer;

import pl.andrzejo.aspm.settings.types.BoolSetting;

public class SaveLogToFile extends GroupViewerSetting<Boolean> {

    protected SaveLogToFile() {
        super(BoolSetting.class, "saveLogToFile", false);
    }
}
