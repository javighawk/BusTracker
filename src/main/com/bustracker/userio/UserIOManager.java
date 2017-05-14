package com.bustracker.userio;

import com.bustracker.bus.BusStop;
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

    public UserIOManager(
            BusStopManager busStopManager,
            int numberOfTripsToShow,
            Duration updateDisplayTaskPeriod,
            ScheduledExecutorService executorService ) {
        try {
            this.displayManager = new DisplayManager(
                    numberOfTripsToShow,
                    updateDisplayTaskPeriod,
                    executorService );
        } catch( IOException | I2CFactory.UnsupportedBusNumberException e ) {
            LOG.error( "Error initializing display" );
            throw new IllegalStateException( e );
        }
        this.busStopManager = busStopManager;
        this.gpioManager = new GPIOManager();
    }

    private void onDisplayNextBusStopEvent() {
        displayManager.updateDisplayWith(
                busStopManager.getNextBusStop(
                        displayManager.getCurrentBusStopDisplay( ).map(
                                BusStop::getBusStopId ) ) );
    }

    private void onDisplayNextTripEvent() {
        displayManager.displayNextTrip();
    }
}
