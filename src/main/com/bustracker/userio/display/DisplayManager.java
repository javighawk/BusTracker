package com.bustracker.userio.display;

import com.bustracker.bus.BusStopManager;
import com.bustracker.gtfs.GTFSManager;
import com.pi4j.io.i2c.I2CFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DisplayManager {

    private final BusStopManager busStopManager;
    private final BusDisplay display;

    public DisplayManager(
            BusStopManager busStopManager,
            Duration taskPeriod,
            ScheduledExecutorService executorService )
            throws IOException, I2CFactory.UnsupportedBusNumberException {
        this.busStopManager = busStopManager;
        this.display = new BusDisplay();
        executorService.scheduleAtFixedRate(
                this::task,
                0,
                taskPeriod.getSeconds(),
                TimeUnit.SECONDS );
    }

    private void task() {

    }

    void showBusLineAndWaitingTime( int busLine, long waitingTime );

    void showRealTimeIndicator();

    void showUpcomingBusIndex();

}
