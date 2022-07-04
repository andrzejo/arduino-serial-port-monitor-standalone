package pl.andrzejo.aspm.serial;

import jssc.SerialPortList;

import java.util.Arrays;
import java.util.List;

public class SerialPorts {

    public static List<String> getList() {
        return Arrays.asList(SerialPortList.getPortNames());
    }

}