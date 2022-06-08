package pl.andrzejo.aspm.settings;

public abstract class Setting<T> {
    private final SettingsRepository repository;
    private final String key;
    private T defValue;

    public Setting(String key) {
        this(key, null);
    }

    public Setting(String key, T defValue) {
        this.key = key;
        this.defValue = defValue;
        this.repository = SettingsRepository.instance();
    }

    public T get() {
        String val = repository.getString(key, null);
        if (val == null) {
            return defValue;
        }

        try {
            return deserialize(val);
        } catch (Exception e) {
            return defValue;
        }
    }

    public void set(T value) {
        String val = serialize(value);
        repository.saveString(key, val);
    }

    protected abstract String serialize(T value);

    protected abstract T deserialize(String value);
}
