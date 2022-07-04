package pl.andrzejo.aspm.gui.setting;

import pl.andrzejo.aspm.settings.appsettings.LineEndingSetting;
import pl.andrzejo.aspm.settings.guihandlers.ListSettingHandler;

import java.util.LinkedHashMap;

public class LineEndingSettingHandler extends ListSettingHandler<String, String> {

    public LineEndingSettingHandler(LineEndingSetting setting) {
        super(setting::set, setting::get, LineEndingSetting.DEF_VALUE, false);
    }

    @Override
    protected void fillItems(LinkedHashMap<String, String> items) {
        items.put("\n", "\\n");
        items.put("\r", "\\r");
        items.put("\n\r", "\\n\\r");
        items.put("", "none");
    }
}
