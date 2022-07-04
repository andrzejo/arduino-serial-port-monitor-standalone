package pl.andrzejo.aspm.settings.appsettings;

public class AppSettingGetter {

    public static <R, T extends AppSetting<R>> R get(Class<T> settingClass) {
        T object = AppSettingsFactory.create(settingClass);
        return object.get();
    }

}
