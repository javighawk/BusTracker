package com.bustracker.userio.display;

import com.bustracker.bus.BusStopManager;
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

    private final BusStopManager busStopManager;
    private final BusDisplay busDisplay;
    private final int numberOfTripsToShow;
    private int currentlyShownTripIndex = 0;

    public DisplayManager(
            BusStopManager busStopManager,
            Duration taskPeriod,
            int numberOfTripsToShow,
            ScheduledExecutorService executorService )
            throws IOException, UnsupportedBusNumberException {
        this.busStopManager = busStopManager;
        this.busDisplay = new BusDisplay();
        this.numberOfTripsToShow = numberOfTripsToShow;
        executorService.scheduleAtFixedRate(
                this::task,
                0,
                taskPeriod.getSeconds(),
                TimeUnit.SECONDS );
    }

    private void task() {
        updateDisplay();
    }

    private synchronized void updateDisplay() {
        Optional<Optional<TripStop>> tripStopOpt =
                busStopManager.getBusStop( "busStopId" )
                        .map( bs -> bs.getUpcomingBus(
                                currentlyShownTripIndex ) );
        if( tripStopOpt.isPresent() ) {
            if( tripStopOpt.get().isPresent() ) {
                drawTripStopOnDisplay( tripStopOpt.get().get() );
            } else {
                busDisplay.clear();
            }
        } else {
            busDisplay.drawError();
        }
        busDisplay.drawBusIndexIndicator( currentlyShownTripIndex );
    }

    private void drawTripStopOnDisplay( TripStop tripStop ) {
        long waitTime = LocalDateTime.now().until(
                tripStop.getRealArrivalDateTime(), ChronoUnit.MINUTES );
        busDisplay.drawBusNumber( Integer.parseInt( tripStop.getBusLine() ) );
        busDisplay.drawWaitingTime( Duration.ofMinutes( waitTime ) );
        if( tripStop.isRealTime() ) {
            busDisplay.drawRealTimeIndicator();
        } else {
            busDisplay.clearRealTimeIndicator();
        }
    }

    public void showNextUpcomingBusIndex() {
        currentlyShownTripIndex +=
                ( currentlyShownTripIndex + 1 ) % numberOfTripsToShow;
        updateDisplay();
    }

    public void showNextBusStop() {

    }
}
