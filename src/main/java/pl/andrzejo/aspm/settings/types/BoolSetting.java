package pl.andrzejo.aspm.settings.types;

import pl.andrzejo.aspm.settings.Setting;

public class BoolSetting extends Setting<Boolean> {

    public BoolSetting(String key, Boolean defValue) {
        super(key, defValue);
    }

    @Override
    protected String serialize(Boolean value) {
        return String.valueOf(value);
    }

    @Override
    protected Boolean deserialize(String value) {
        return Boolean.parseBoolean(value);
    }
}
