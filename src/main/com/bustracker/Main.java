package com.bustracker;

import com.bustracker.bus.BusStopManager;
import com.bustracker.gtfs.GTFSManager;
import com.bustracker.gtfs.GTFSManagerImpl;
import com.bustracker.gtfs.GTFSStaticData;
import com.bustracker.gtfs.GTFSStaticDataImpl;
import com.bustracker.userio.UserIOManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class Main {
    private static String gtfsPath = "gtfs/";
    private static String gtfsURL =
            "http://apps2.saskatoon.ca/app/data/TripUpdate/TripUpdates.pb";
    private static Duration gtfsManagerTaskPeriod = Duration.ofSeconds( 10 );
    private static Duration userIoTaskPeriod = Duration.ofSeconds( 10 );
    private static int numberOfTripsToShow = 3;

    private static ScheduledExecutorService executorService;
    private static GTFSStaticData gtfsStaticData;
    private static GTFSManager gtfsManager;
    private static BusStopManager busStopManager;
    private static UserIOManager userIoManager;
    private static final Logger LOG = LoggerFactory.getLogger( Main.class );

    public static void main( String[] args ){
        LOG.info( "Start main task" );
        executorService = getNewThreadPool();
        gtfsStaticData = new GTFSStaticDataImpl( gtfsPath );
        gtfsManager = GTFSManagerImpl.createAndStart(
                gtfsURL,
                gtfsManagerTaskPeriod,
                gtfsStaticData,
                executorService );
        busStopManager = new BusStopManager( gtfsManager );

        LOG.info( "Start initializing User IO" );
        userIoManager = UserIOManager.createAndInit(
                busStopManager,
                numberOfTripsToShow,
                userIoTaskPeriod,
                executorService );

        LOG.info( "Finished init" );
        addBusStops();
    }

    private static void addBusStops( ) {
        busStopManager.addBusStopToTrack( "Bryans / 108th Street");
    }

    private static ScheduledThreadPoolExecutor getNewThreadPool( ) {
        return new ScheduledThreadPoolExecutor( 5 );
    }
}
