package com.bustracker.userio;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.subjects.PublishSubject;

public class GPIOManager {

	public enum GPIOEvent {
		SHOW_NEXT_TRIP,
		SHOW_NEXT_BUS_STOP
	}

	private final Pin nextTripPin = RaspiPin.GPIO_04;
	private final Pin nextBusStopPin = RaspiPin.GPIO_05;

	private GpioPinDigitalInput nextTripGpioPin;
	private GpioPinDigitalInput nextBusStopGpioPin;

	private final PublishSubject buttonPressedSubject = PublishSubject.create();
	private static final Logger LOG =
			LoggerFactory.getLogger( GPIOManager.class );

	public static GPIOManager createAndInit() {
	    GPIOManager result = new GPIOManager();
	    result.addListeners();
	    return result;
    }

	private GPIOManager() {
		final GpioController gpio = GpioFactory.getInstance();
		nextTripGpioPin = gpio.provisionDigitalInputPin( nextTripPin );
		nextBusStopGpioPin = gpio.provisionDigitalInputPin( nextBusStopPin );
	}

	private void addListeners() {
		nextTripGpioPin.addListener(
				(GpioPinListenerDigital) event ->
						fireButtonPressedEvent( GPIOEvent.SHOW_NEXT_TRIP ) );
		nextBusStopGpioPin.addListener(
				(GpioPinListenerDigital) event ->
						fireButtonPressedEvent( GPIOEvent.SHOW_NEXT_BUS_STOP ) );
	}

	private void fireButtonPressedEvent( GPIOEvent event ) {
		LOG.info( "New GPIO event received" );
		buttonPressedSubject.onNext( event );
	}

	public Observable<GPIOEvent> getEvents() {
		return buttonPressedSubject.asObservable();
	}


}
