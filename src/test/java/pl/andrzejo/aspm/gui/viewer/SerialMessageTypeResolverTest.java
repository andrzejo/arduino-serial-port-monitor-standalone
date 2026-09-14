/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2026.
 */

package pl.andrzejo.aspm.gui.viewer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import pl.andrzejo.aspm.gui.viewer.model.MessageType;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SerialMessageTypeResolverTest {

    @ParameterizedTest
    @CsvSource(
            value = {
                    "SERIAL_INFO|[1249102][I][BluetoothA2DPSink.cpp:775] handle_audio_state(): [BT_AV] A2DP audio state: Suspended",
                    "SERIAL_DEBUG|[1249102][D][BluetoothA2DPSink.cpp:775] debug message",
                    "SERIAL_WARN|[1249102][W][BluetoothA2DPSink.cpp:775] warning message",
                    "SERIAL_ERROR|[1249102][E][BluetoothA2DPSink.cpp:775] error message",
                    "SERIAL_INFO|normal serial output",
                    "SERIAL_INFO|[1268497][I][BluetoothA2DPSink.cpp:775] handle_audio_state(): [BT_AV] A2DP audio state: Suspended"
            },
            delimiter = '|'
    )
    void shouldMatchEspDebug(MessageType expectedType, String text) {
        //given
        SerialMessageTypeResolver resolver = new SerialMessageTypeResolver();

        //when
        MessageType type = resolver.resolve(text);

        //then
        assertEquals(expectedType, type);
    }

    /*
    19:24:35.415 [1265601][I][BluetoothA2DPSink.cpp:1025] av_notify_evt_handler(): [BT_AV] Play position changed: 0-ms
19:24:36.155 [1266341][I][BluetoothA2DPSink.cpp:1074] av_hdl_avrc_evt(): [BT_AV] AVRC metadata rsp: attribute id 0x1, Queen - Another One Bites The Dust (SDJM Remix)
19:24:36.166 [BT] Title: Queen - Another One Bites The Dust (SDJM Remix)
19:24:36.172 [1266355][I][BluetoothA2DPSink.cpp:1074] av_hdl_avrc_evt(): [BT_AV] AVRC metadata rsp: attribute id 0x2, EDM Bot
19:24:36.183 [BT] Artist: EDM Bot
19:24:36.183 [1266370][I][BluetoothA2DPSink.cpp:1074] av_hdl_avrc_evt(): [BT_AV] AVRC metadata rsp: attribute id 0x40, -1
19:24:36.193 [BT] Time: -1
19:24:38.310 [1268497][I][BluetoothA2DPSink.cpp:775] handle_audio_state(): [BT_AV] A2DP audio state: Suspended
19:24:38.319 [BT] Audio suspended
19:24:38.319 [1268506][I][BluetoothA2DPSink.cpp:804] set_i2s_active(): [BT_AV] set_i2s_active 0
19:24:38.327 [1268515][W][BluetoothA2DPOutput.cpp:186] set_output_active(): [BT_AV] i2s_stop
19:24:41.527 [1271712][I][BluetoothA2DPSink.cpp:503] app_gap_callback(): [BT_AV] ESP_BT_GAP_MODE_CHG_EVT
     */
}
