package com.bustracker.userio;

import com.pi4j.io.gpio.*;

public class ButtonsManager {
	
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
	
	public ButtonsManager() {
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
}
