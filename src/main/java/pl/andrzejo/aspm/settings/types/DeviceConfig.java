/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.settings.types;

public class DeviceConfig implements Cloneable {
    private String device;
    private int baud;
    private char parity;
    private int dataBits;
    private float stopBits;
    private boolean RTS;
    private boolean DTR;

    public static DeviceConfig defaultConfig() {
        return new DeviceConfig()
                .setDevice("")
                .setBaud(9600)
                .setParity('N')
                .setDataBits(8)
                .setStopBits(1);
    }

    public String getDevice() {
        return device;
    }

    public DeviceConfig setDevice(String device) {
        this.device = device;
        return this;
    }

    public int getBaud() {
        return baud;
    }

    public DeviceConfig setBaud(int baud) {
        this.baud = baud;
        return this;
    }

    public char getParity() {
        return parity;
    }

    public DeviceConfig setParity(char parity) {
        this.parity = parity;
        return this;
    }

    public int getDataBits() {
        return dataBits;
    }

    public DeviceConfig setDataBits(int dataBits) {
        this.dataBits = dataBits;
        return this;
    }

    public float getStopBits() {
        return stopBits;
    }

    public DeviceConfig setStopBits(float stopBits) {
        this.stopBits = stopBits;
        return this;
    }

    public boolean isRTS() {
        return RTS;
    }

    public DeviceConfig setRTS(boolean RTS) {
        this.RTS = RTS;
        return this;
    }

    public boolean isDTR() {
        return DTR;
    }

    public DeviceConfig setDTR(boolean DTR) {
        this.DTR = DTR;
        return this;
    }

    @Override
    public String toString() {
        return "DeviceConfig{" +
                "device='" + device + '\'' +
                ", baud=" + baud +
                ", parity=" + parity +
                ", dataBits=" + dataBits +
                ", stopBits=" + stopBits +
                ", RTS=" + RTS +
                ", DTR=" + DTR +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceConfig that = (DeviceConfig) o;

        if (baud != that.baud) return false;
        if (parity != that.parity) return false;
        if (dataBits != that.dataBits) return false;
        if (Float.compare(that.stopBits, stopBits) != 0) return false;
        if (RTS != that.RTS) return false;
        if (DTR != that.DTR) return false;
        return device != null ? device.equals(that.device) : that.device == null;
    }

    @Override
    public int hashCode() {
        int result = device != null ? device.hashCode() : 0;
        result = 31 * result + baud;
        result = 31 * result + (int) parity;
        result = 31 * result + dataBits;
        result = 31 * result + (stopBits != +0.0f ? Float.floatToIntBits(stopBits) : 0);
        result = 31 * result + (RTS ? 1 : 0);
        result = 31 * result + (DTR ? 1 : 0);
        return result;
    }

    @Override
    public DeviceConfig clone() {
        try {
            DeviceConfig clone = (DeviceConfig) super.clone();
            clone.setDevice(device);
            clone.setBaud(baud);
            clone.setDataBits(dataBits);
            clone.setStopBits(stopBits);
            clone.setParity(parity);
            clone.setDTR(DTR);
            clone.setRTS(RTS);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
