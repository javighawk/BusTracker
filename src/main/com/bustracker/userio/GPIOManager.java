package com.bustracker.userio;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import rx.Observable;
import rx.subjects.PublishSubject;

public class GPIOManager {

	public enum GPIOEvent {
		SHOW_NEXT_UPCOMING_BUS,
		SHOW_NEXT_BUS_STOP,
		SHOW_OPPOSITE_DIRECTION
	}

	private final Pin nextBusPin = RaspiPin.GPIO_04;
	private final Pin nextStopPin = RaspiPin.GPIO_05;
	private final Pin directionPin = RaspiPin.GPIO_01;
	private final Pin toCityCentrePin = RaspiPin.GPIO_00;
	private final Pin fromCityCentrePin = RaspiPin.GPIO_02;
	
	private GpioPinDigitalInput nextBusGpioPin;
	private GpioPinDigitalInput nextStopGpioPin;
	private GpioPinDigitalInput directionGpioPin;
	private GpioPinDigitalOutput toCityCentreGpioPin;
	private GpioPinDigitalOutput fromCityCentreGpioPin;

	private final PublishSubject buttonPressedSubject = PublishSubject.create();
	
	public GPIOManager() {
		final GpioController gpio = GpioFactory.getInstance();
		nextBusGpioPin = gpio.provisionDigitalInputPin(nextBusPin);
		nextStopGpioPin = gpio.provisionDigitalInputPin(nextStopPin);
		directionGpioPin = gpio.provisionDigitalInputPin(directionPin);
		toCityCentreGpioPin = gpio.provisionDigitalOutputPin(
				toCityCentrePin, PinState.HIGH);
		fromCityCentreGpioPin = gpio.provisionDigitalOutputPin(
				fromCityCentrePin, PinState.HIGH);

		toCityCentreGpioPin.setShutdownOptions(true, PinState.LOW);
		fromCityCentreGpioPin.setShutdownOptions(true, PinState.LOW);
		
		// TODO: Update display with current direction
		// TODO: Add listeners to buttons
	}

	private void fireButtonPressedEvent( GPIOEvent event ) {

	}

	public Observable<GPIOEvent> getEvents() {
		return buttonPressedSubject.asObservable();
	}


}
