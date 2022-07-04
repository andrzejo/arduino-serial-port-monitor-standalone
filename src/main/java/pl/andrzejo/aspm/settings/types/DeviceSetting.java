package pl.andrzejo.aspm.settings.types;

import pl.andrzejo.aspm.settings.Setting;

import static org.apache.commons.lang.StringUtils.isBlank;
import static pl.andrzejo.aspm.settings.Conversion.*;

public class DeviceSetting extends Setting<DeviceConfig> {

    public DeviceSetting(String key, DeviceConfig defValue) {
        super(key, defValue);
    }

    @Override
    protected String serialize(DeviceConfig value) {
        return String.format("%s|%d|%s|%s|%s|%s|%s", value.getDevice(), value.getBaud(), value.getParity(),
                value.getDataBits(), value.getStopBits(), value.isRTS(), value.isDTR());
    }

    @Override
    protected DeviceConfig deserialize(String value) {
        String[] parts = value.split("\\|");
        if (parts.length == 7) {
            return new DeviceConfig()
                    .setDevice(parts[0])
                    .setBaud(toInt(parts[1], defValue.getBaud()))
                    .setParity(getParity(parts[2]))
                    .setDataBits(toInt(parts[3], defValue.getDataBits()))
                    .setStopBits(toFloat(parts[4], defValue.getStopBits()))
                    .setRTS(toBool(parts[5], defValue.isRTS()))
                    .setDTR(toBool(parts[6], defValue.isDTR()));
        }
        return null;
    }

    private char getParity(String value) {
        if (isBlank(value)) {
            return defValue.getParity();
        }
        return value.charAt(0);
    }
}
