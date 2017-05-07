package com.bustracker;

import com.bustracker.bus.BusStopManager;
import com.bustracker.gtfs.GTFSManager;
import com.bustracker.gtfs.GTFSStaticData;
import com.bustracker.userio.display.DisplayManager;
import com.pi4j.io.i2c.I2CFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main {
	
	/* Initialize user variables */
	private static String gtfsPath = "gtfs/";
	private static String gtfsURL = "http://apps2.saskatoon.ca/app/data/TripUpdate/TripUpdates.pb";
	private static ScheduledExecutorService executorService;

	private static GTFSStaticData gtfsStaticData;
	private static GTFSManager gtfsManager;
	private static BusStopManager busStopManager;
	private static DisplayManager displayManager;
    private static final Logger LOG = LoggerFactory.getLogger( Main.class );

	public static void main( String[] args ){
		executorService = getNewThreadPool( );
		gtfsStaticData = new GTFSStaticData( gtfsPath );
		gtfsManager = new GTFSManager(
				gtfsURL,
				Duration.ofSeconds( 10 ),
				gtfsStaticData,
				executorService );
		busStopManager = new BusStopManager( gtfsManager );
        try {
            displayManager = new DisplayManager(
                    busStopManager,
                    Duration.ofSeconds( 10 ),
                    3, executorService );
        } catch( IOException | I2CFactory.UnsupportedBusNumberException e ) {
            LOG.error( "Error initializing display", e );
        }
        setUpTest( );
	}

	private static void setUpTest( ) {
		String busStopName = "Bryans / 108th Street";
		Set<String> busStopIds = gtfsStaticData.getBusStopIDs( busStopName );
		busStopManager.addBusStopToTrack(
				busStopName );
		busStopIds.forEach(
				bsid -> executorService.scheduleAtFixedRate(
						() -> busStopManager.getBusStop( bsid ).ifPresent(
								bstop -> {
										System.out.println(
												busStopName );
										System.out.println(
												bstop.getBusStopId( ) );
										System.out.println(
												bstop.getUpcomingBus( 0 ).orElse( null ) );
										System.out.println(
												bstop.getUpcomingBus( 1 ).orElse( null ) );
										System.out.println(
												bstop.getUpcomingBus( 2 ).orElse( null ) );
								} ),
						0,
						10, TimeUnit.SECONDS ) );
	}

	private static ScheduledThreadPoolExecutor getNewThreadPool( ) {
		return new ScheduledThreadPoolExecutor( 5 );
	}
}