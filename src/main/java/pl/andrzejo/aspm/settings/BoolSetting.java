package pl.andrzejo.aspm.settings;

public class BoolSetting extends Setting<Boolean> {

    public BoolSetting(String key) {
        super(key, false);
    }

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
