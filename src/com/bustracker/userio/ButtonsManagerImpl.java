package com.bustracker.userio;

import com.bustracker.Main;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class ButtonsManagerImpl {
	
	private final static Pin nextBusPin = RaspiPin.GPIO_04;
	private final static Pin nextStopPin = RaspiPin.GPIO_05;
	private final static Pin directionPin = RaspiPin.GPIO_01;
	private final static Pin toCityCentrePin = RaspiPin.GPIO_00;
	private final static Pin fromCityCentrePin = RaspiPin.GPIO_02;
	
	private static GpioPinDigitalInput nextBusGpioPin;
	private static GpioPinDigitalInput nextStopGpioPin;
	private static GpioPinDigitalInput directionGpioPin;
	private static GpioPinDigitalOutput toCityCentreGpioPin;
	private static GpioPinDigitalOutput fromCityCentreGpioPin;
	
	private class GpioCustomListener implements GpioPinListenerDigital {
		@Override
		public void handleGpioPinDigitalStateChangeEvent(
				GpioPinDigitalStateChangeEvent arg0) {
		}
	}
	
	public ButtonsManagerImpl() {
		final GpioController gpio = GpioFactory.getInstance();
		nextBusGpioPin = gpio.provisionDigitalInputPin(nextBusPin);
		nextStopGpioPin = gpio.provisionDigitalInputPin(nextStopPin);
		directionGpioPin = gpio.provisionDigitalInputPin(directionPin);
		toCityCentreGpioPin = gpio.provisionDigitalOutputPin(toCityCentrePin, PinState.HIGH);
		fromCityCentreGpioPin = gpio.provisionDigitalOutputPin(fromCityCentrePin, PinState.HIGH);

		toCityCentreGpioPin.setShutdownOptions(true, PinState.LOW);
		fromCityCentreGpioPin.setShutdownOptions(true, PinState.LOW);
		
		updateDirectionFromSwitch( directionGpioPin.isHigh() );
		
		nextBusGpioPin.addListener(new GpioCustomListener() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent(
					GpioPinDigitalStateChangeEvent arg0) {
				if( arg0.getState().isHigh() ) {
					Main.disp.displayNextUpcomingBus();
				}
			}
		});
			
		nextStopGpioPin.addListener(new GpioCustomListener() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent(
					GpioPinDigitalStateChangeEvent arg0) {
				if( arg0.getState().isHigh() ) {
					Main.disp.displayNextBusStop();
				}
			}
		});
		
		directionGpioPin.addListener(new GpioCustomListener() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent(
					GpioPinDigitalStateChangeEvent arg0) {
				updateDirectionFromSwitch( arg0.getState().isLow() );
			}
		});
	}
	
	private void updateDirectionFromSwitch(boolean goesToCityCentre) {
		if (goesToCityCentre) {
			toCityCentreGpioPin.high();
			fromCityCentreGpioPin.low();
		} else {
			toCityCentreGpioPin.low();
			fromCityCentreGpioPin.high();
		}
		Main.disp.displayDirectionToCityCentre( goesToCityCentre );
	}
}
