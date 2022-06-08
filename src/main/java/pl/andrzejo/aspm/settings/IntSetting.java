package pl.andrzejo.aspm.settings;

public class IntSetting extends Setting<Integer> {

    public IntSetting(String key) {
        super(key);
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
