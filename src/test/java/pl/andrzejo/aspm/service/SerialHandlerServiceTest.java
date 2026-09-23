/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2026.
 */

package pl.andrzejo.aspm.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import pl.andrzejo.aspm.eventbus.ApplicationEventBus;
import pl.andrzejo.aspm.eventbus.events.api.commands.ApiCloseDeviceEvent;
import pl.andrzejo.aspm.eventbus.events.api.commands.ApiOpenDeviceEvent;
import pl.andrzejo.aspm.eventbus.events.command.CommandExecutedEvent;
import pl.andrzejo.aspm.eventbus.events.command.ExecuteCommandEvent;
import pl.andrzejo.aspm.eventbus.events.device.DeviceCloseEvent;
import pl.andrzejo.aspm.eventbus.events.device.DeviceErrorEvent;
import pl.andrzejo.aspm.eventbus.events.device.DeviceOpenEvent;
import pl.andrzejo.aspm.factory.BeanFactory;
import pl.andrzejo.aspm.gui.cmd.CommandItem;
import pl.andrzejo.aspm.serial.Serial2;
import pl.andrzejo.aspm.serial.SerialException;
import pl.andrzejo.aspm.settings.appsettings.items.device.LastDeviceSetting;
import pl.andrzejo.aspm.settings.appsettings.items.device.TtyDeviceSetting;
import pl.andrzejo.aspm.settings.types.DeviceConfig;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

class SerialHandlerServiceTest {
    private final ApplicationEventBus bus = mock(ApplicationEventBus.class);
    private final Serial2 serial = mock(Serial2.class);
    private SerialHandlerService service;

    @BeforeEach
    void setUp() {
        BeanFactory.reset();
        TtyDeviceSetting setting = mock(TtyDeviceSetting.class);
        when(setting.get()).thenReturn(DeviceConfig.defaultConfig().setDevice("test"));
        BeanFactory.overrideInstance(TtyDeviceSetting.class, setting);
        BeanFactory.overrideInstance(LastDeviceSetting.class, mock(LastDeviceSetting.class));
        BeanFactory.overrideInstance(ApplicationEventBus.class, bus);
        BeanFactory.overrideInstance(Serial2.class, serial);
        service = BeanFactory.instance(SerialHandlerService.class);
    }

    @AfterEach
    void tearDown() {
        BeanFactory.reset();
    }

    @Test
    void shouldPublishCommandExecutedOnlyAfterSuccessfulWrite() throws Exception {
        //given
        service.handleEvent(new ApiOpenDeviceEvent("test"));
        ExecuteCommandEvent command = new ExecuteCommandEvent(new CommandItem("hello", ""), "\n");

        //when
        service.handleEvent(command);

        //then
        InOrder order = inOrder(serial, bus);
        order.verify(serial).write("hello\n");
        ArgumentCaptor<CommandExecutedEvent> executed = ArgumentCaptor.forClass(CommandExecutedEvent.class);
        order.verify(bus).post(executed.capture());
        assertThat(executed.getValue().getCommand()).isSameAs(command.getCommand());
        assertThat(executed.getValue().getLineEnding()).isEqualTo("\n");
        verify(bus, never()).post(isA(DeviceErrorEvent.class));
    }

    @Test
    void shouldReportWriteFailureWithoutPublishingCommandExecuted() throws Exception {
        //given
        service.handleEvent(new ApiOpenDeviceEvent("test"));
        reset(bus);
        doThrow(new SerialException("write failed")).when(serial).write("hello\n");

        //when
        service.handleEvent(new ExecuteCommandEvent(new CommandItem("hello", ""), "\n"));

        //then
        verify(bus, never()).post(isA(CommandExecutedEvent.class));
        ArgumentCaptor<DeviceErrorEvent> error = ArgumentCaptor.forClass(DeviceErrorEvent.class);
        verify(bus).post(error.capture());
        assertThat(error.getValue().getMessage()).isEqualTo("write failed");
    }

    @Test
    void shouldKeepTheDeviceForAnotherCloseAttemptAfterFailure() throws Exception {
        //given
        service.handleEvent(new ApiOpenDeviceEvent("test"));
        doThrow(new IOException("close failed")).doNothing().when(serial).dispose();

        //when
        service.handleEvent(new ApiCloseDeviceEvent());

        //then
        verify(bus).post(isA(DeviceErrorEvent.class));
        verify(bus, never()).post(isA(DeviceCloseEvent.class));

        //when
        service.handleEvent(new ApiCloseDeviceEvent());
        service.handleEvent(new ApiCloseDeviceEvent());

        //then
        verify(serial, times(2)).dispose();
        verify(bus).post(isA(DeviceCloseEvent.class));
        assertThat(service.getStatus().isOpen()).isFalse();
    }

    @Test
    void shouldCloseThePortWhenDiscardingInitialBuffersFails() throws Exception {
        //given
        doThrow(new SerialException("discard failed")).when(serial).discardBuffers();

        //when
        service.handleEvent(new ApiOpenDeviceEvent("test"));

        //then
        verify(serial).dispose();
        verify(bus).post(isA(DeviceErrorEvent.class));
        verify(bus, never()).post(isA(DeviceOpenEvent.class));
        assertThat(service.getStatus().isOpen()).isFalse();
    }
}
