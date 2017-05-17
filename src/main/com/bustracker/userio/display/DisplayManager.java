package com.bustracker.userio.display;

import com.bustracker.bus.BusStop;
import com.bustracker.trip.TripStop;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DisplayManager {

    private final BusDisplay busDisplay;
    private Optional<BusStop> currentBusStopDisplayed = Optional.empty();
    private final int numberOfTripsToShow;
    private int currentlyShownTripIndex = 0;
    private static final Logger LOG =
            LoggerFactory.getLogger( DisplayManager.class );

    public static DisplayManager createAndInit(
            int numberOfTripsToShow,
            Duration updateDisplayTaskPeriod,
            ScheduledExecutorService executorService )
            throws IOException, UnsupportedBusNumberException {
        DisplayManager result = new DisplayManager( numberOfTripsToShow );
        result.start( updateDisplayTaskPeriod, executorService );
        return result;
    }

    private DisplayManager(
            int numberOfTripsToShow )
            throws IOException, UnsupportedBusNumberException {
        this.numberOfTripsToShow = numberOfTripsToShow;
        this.busDisplay = new BusDisplay();
    }

    private void start(
            Duration updateDisplayTaskPeriod,
            ScheduledExecutorService executorService ) {
        executorService.scheduleAtFixedRate(
                this::task,
                0,
                updateDisplayTaskPeriod.getSeconds(),
                TimeUnit.SECONDS );
    }

    private void task() {
        updateDisplay();
    }

    private synchronized void updateDisplay() {
        LOG.info( "Update display" );
        if( currentBusStopDisplayed.isPresent() ) {
            LOG.info( "Bus stop is present for displaying" );
            drawTripStopOnDisplay( currentBusStopDisplayed.get() );
        } else {
            LOG.warn( "No bus stop to display" );
            busDisplay.clear();
        }
        busDisplay.drawBusIndexIndicator( currentlyShownTripIndex );
    }

    private void drawTripStopOnDisplay( BusStop busStopToDisplay ) {
        Optional<TripStop> tripStopOpt = busStopToDisplay.getUpcomingBus(
                currentlyShownTripIndex );
        if( tripStopOpt.isPresent() ) {
            TripStop tripStop = tripStopOpt.get( );
            LOG.info( "Displaying trip: {}", tripStop );
            busDisplay.drawOnDisplay(
                    tripStop.getBusLine(),
                    getDurationUntil(
                            tripStop.getRealArrivalDateTime() ),
                    tripStop.isRealTime()
                    );
        } else {
            LOG.info( "No trip to display" );
            busDisplay.clear();
        }
    }

    private Duration getDurationUntil( LocalDateTime realArrivalDateTime ) {
        return Duration.ofMinutes(
                LocalDateTime.now().until(
                        realArrivalDateTime, ChronoUnit.MINUTES ) );
    }

    public void updateDisplayWith( Optional<BusStop> busStopDisplayOpt ) {
        this.currentBusStopDisplayed = busStopDisplayOpt;
        updateDisplay();
    }

    public void displayNextTrip() {
        LOG.info( "Display next trip" );
        currentlyShownTripIndex =
                ( currentlyShownTripIndex + 1 ) % numberOfTripsToShow;
        updateDisplay();
    }

    public Optional<BusStop> getCurrentBusStopDisplay() {
        LOG.info( "Display next bus stop" );
        return currentBusStopDisplayed;
    }
}
