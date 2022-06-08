package pl.andrzejo.aspm.settings;

public abstract class Setting<T> {
    private final SettingsRepository repository;
    private final String key;

    public Setting(String key) {
        this.key = key;
        this.repository = SettingsRepository.instance();
    }

    public T get(T defValue) {
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
