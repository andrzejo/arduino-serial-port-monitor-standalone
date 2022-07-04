package pl.andrzejo.aspm.settings;

import pl.andrzejo.aspm.settings.repository.SettingsRepository;

public abstract class Setting<T> {
    protected final SettingsRepository repository;
    protected final String key;
    protected final T defValue;

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
