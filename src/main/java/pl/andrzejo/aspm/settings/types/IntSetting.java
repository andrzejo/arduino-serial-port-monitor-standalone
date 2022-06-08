package pl.andrzejo.aspm.settings.types;

import pl.andrzejo.aspm.settings.Setting;

public class IntSetting extends Setting<Integer> {

    public IntSetting(String key) {
        super(key);
    }

    public IntSetting(String key, Integer defValue) {
        super(key, defValue);
    }

    @Override
    protected String serialize(Integer value) {
        return String.valueOf(value);
    }

    @Override
    protected Integer deserialize(String value) {
        return Integer.parseInt(value);
    }

}
