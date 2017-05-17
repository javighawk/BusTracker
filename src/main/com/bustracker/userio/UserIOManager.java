package com.bustracker.userio;

import com.bustracker.bus.BusStopManager;
import com.bustracker.userio.display.DisplayManager;
import com.pi4j.io.i2c.I2CFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by Javier on 2017-05-07.
 */
public class UserIOManager {

    private final DisplayManager displayManager;
    private final GPIOManager gpioManager;
    private final BusStopManager busStopManager;
    private static final Logger LOG =
            LoggerFactory.getLogger( UserIOManager.class );

    public static UserIOManager createAndInit(
            BusStopManager busStopManager,
            int numberOfTripsToShow,
            Duration updateDisplayTaskPeriod,
            ScheduledExecutorService executorService ) {
        UserIOManager result = new UserIOManager(
                busStopManager,
                numberOfTripsToShow,
                updateDisplayTaskPeriod,
                executorService );
        result.init();
        return result;
    }

    private UserIOManager(
            BusStopManager busStopManager,
            int numberOfTripsToShow,
            Duration updateDisplayTaskPeriod,
            ScheduledExecutorService executorService ) {
        try {
            this.displayManager = DisplayManager.createAndInit(
                    numberOfTripsToShow,
                    updateDisplayTaskPeriod,
                    executorService );
        } catch( IOException | I2CFactory.UnsupportedBusNumberException e ) {
            LOG.error( "Error initializing display" );
            throw new IllegalStateException( e );
        }
        this.busStopManager = busStopManager;
        this.gpioManager = GPIOManager.createAndInit();
    }

    private void init() {
        gpioManager.getEvents().subscribe( this::onGpioEvent );
        busStopManager.getAddedBusStopEvents().subscribe(
                v -> onAddedBusStopEvent() );
        busStopManager.getAddedBusStopEvents().subscribe(
                v -> onRemovedBusStopEvent() );
    }

    private void onRemovedBusStopEvent() {
        onDisplayNextBusStopEvent();
    }

    private void onAddedBusStopEvent() {
        if( !displayManager.getCurrentBusStopDisplay().isPresent() ) {
            onDisplayNextBusStopEvent();
        }
    }

    private void onGpioEvent( GPIOManager.GPIOEvent event ) {
        LOG.info( "New GPIO event received" );
        switch( event ) {
            case SHOW_NEXT_BUS_STOP:
                onDisplayNextBusStopEvent();
                break;
            case SHOW_NEXT_TRIP:
                onDisplayNextTripEvent();
                break;
        }
    }

    private void onDisplayNextBusStopEvent() {
        LOG.info( "Change bus stop to display" );
        displayManager.updateDisplayWith(
                busStopManager.getNextBusStop(
                        displayManager.getCurrentBusStopDisplay() ) );
    }

    private void onDisplayNextTripEvent() {
        LOG.info( "Change trip to display" );
        displayManager.displayNextTrip();
    }
}
