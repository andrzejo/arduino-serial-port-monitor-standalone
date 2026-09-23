/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2026.
 */

package pl.andrzejo.aspm.serial;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;

final class JSerialCommPort implements Serial2.Port {
    private final SerialPort port;

    JSerialCommPort(String name) {
        port = SerialPort.getCommPort(name);
    }

    @Override
    public void configure(int baudRate, char parity, int dataBits, float stopBits) {
        port.setBaudRate(baudRate);
        port.setNumDataBits(dataBits);
        port.setNumStopBits(mapStopBits(stopBits));
        port.setParity(mapParity(parity));
        port.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
    }

    @Override
    public boolean open() {
        return port.openPort();
    }

    @Override
    public void setControlLines(boolean rts, boolean dtr) {
        if (dtr) port.setDTR();
        else port.clearDTR();
        if (rts) port.setRTS();
        else port.clearRTS();
    }

    @Override
    public boolean addDataListener(SerialPortDataListener listener) {
        return port.addDataListener(listener);
    }

    @Override
    public void removeDataListener() {
        port.removeDataListener();
    }

    @Override
    public boolean flushIOBuffers() {
        return port.flushIOBuffers();
    }

    @Override
    public int write(byte[] bytes, int length, int offset) {
        return port.writeBytes(bytes, length, offset);
    }

    @Override
    public boolean isOpen() {
        return port.isOpen();
    }

    @Override
    public boolean close() {
        return port.closePort();
    }

    private static int mapParity(char parity) {
        switch (Character.toUpperCase(parity)) {
            case 'E':
                return SerialPort.EVEN_PARITY;
            case 'O':
                return SerialPort.ODD_PARITY;
            case 'M':
                return SerialPort.MARK_PARITY;
            case 'S':
                return SerialPort.SPACE_PARITY;
            default:
                return SerialPort.NO_PARITY;
        }
    }

    private static int mapStopBits(float stopBits) {
        if (Float.compare(stopBits, 1.5f) == 0) return SerialPort.ONE_POINT_FIVE_STOP_BITS;
        if (Float.compare(stopBits, 2.0f) == 0) return SerialPort.TWO_STOP_BITS;
        return SerialPort.ONE_STOP_BIT;
    }
}
