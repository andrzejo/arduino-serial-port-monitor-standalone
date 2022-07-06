package pl.andrzejo.aspm.settings.appsettings;

import pl.andrzejo.aspm.factory.BeanFactory;

public class AppSettingsFactory {

    public static <T extends AppSetting<?>> T create(Class<T> appSettingType) {
        return BeanFactory.instance(appSettingType);
    }

}
