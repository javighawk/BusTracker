package com.bustracker.userio.display;

import com.bustracker.bus.BusStop;
import com.bustracker.trip.TripStop;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

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

    public DisplayManager(
            int numberOfTripsToShow,
            Duration updateDisplayTaskPeriod,
            ScheduledExecutorService executorService )
            throws IOException, UnsupportedBusNumberException {
        this.numberOfTripsToShow = numberOfTripsToShow;
        this.busDisplay = new BusDisplay();
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
        if( currentBusStopDisplayed.isPresent() ) {
            drawTripStopOnDisplay( currentBusStopDisplayed.get() );
        } else {
            busDisplay.clear();
        }
        busDisplay.drawBusIndexIndicator( currentlyShownTripIndex );
    }

    private void drawTripStopOnDisplay( BusStop busStopToDisplay ) {
        Optional<TripStop> tripStopOpt = busStopToDisplay.getUpcomingBus(
                currentlyShownTripIndex );
        if( tripStopOpt.isPresent() ) {
            TripStop tripStop = tripStopOpt.get( );
            busDisplay.drawOnDisplay(
                    tripStop.getBusLine(),
                    getDurationUntil(
                            tripStop.getRealArrivalDateTime() ),
                    tripStop.isRealTime()
                    );
        } else {
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
        currentlyShownTripIndex =
                ( currentlyShownTripIndex + 1 ) % numberOfTripsToShow;
        updateDisplay();
    }

    public Optional<BusStop> getCurrentBusStopDisplay() {
        return currentBusStopDisplayed;
    }
}
