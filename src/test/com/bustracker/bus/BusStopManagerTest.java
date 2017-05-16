package com.bustracker.bus;

import com.bustracker.gtfs.GTFSManager;
import com.bustracker.gtfs.GTFSStaticData;
import com.bustracker.trip.TripStop;
import com.bustracker.trip.calendar.TripCalendar;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Javier on 2017-05-15.
 */
public class BusStopManagerTest {

    private final GTFSStaticData gtfsStaticData = mock( GTFSStaticData.class );
    private final GTFSManager gtfsManager = mock( GTFSManager.class );
    private final BusStopManager busStopManager =
            new BusStopManager( gtfsManager );
    private final Set<String> busStopIdsString =
            Sets.newHashSet( "1", "2", "3" );
    private final LocalTime now = LocalTime.now();
    private final Set<DayOfWeek> dayOfWeeks =
            Sets.newHashSet(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY,
                    DayOfWeek.SATURDAY,
                    DayOfWeek.SUNDAY );
    private final TripCalendar tripCalendar =
            TripCalendar.builder()
                    .withCalendarId( "1" )
                    .withEndDate( "99991231" )
                    .withStartDate( "19700101" )
                    .withOperatingWeekdays( dayOfWeeks )
                    .build();
    private final TripStop tripStop1 = new TripStop(
            1, 10, 1, now, tripCalendar, Duration.ZERO );
    private final TripStop tripStop2 = new TripStop(
            2, 11, 1,
            now.plusMinutes( 10 ), tripCalendar, Duration.ZERO );
    private final TripStop tripStop3 = new TripStop(
            3, 12, 2, now, tripCalendar, Duration.ZERO );
    private final TripStop tripStop4 = new TripStop(
            4, 13, 2,
            now.plusMinutes( 10 ), tripCalendar, Duration.ZERO );
    private final TripStop tripStop5 = new TripStop(
            5, 14, 3, now, tripCalendar, Duration.ZERO );
    private final TripStop tripStop6 = new TripStop(
            6, 15, 3,
            now.plusMinutes( 10 ), tripCalendar, Duration.ZERO );

    @Before
    public void setUp() {
        when( gtfsManager.getStaticData() ).thenReturn( gtfsStaticData );
        when( gtfsStaticData.getBusStopIDs( anyString() ) )
                .thenReturn( busStopIdsString );
        when( gtfsStaticData.getAllScheduledTripStopsFromBusStopId( "1" ) )
                .thenReturn( Sets.newHashSet( tripStop1, tripStop2 ) );
        when( gtfsStaticData.getAllScheduledTripStopsFromBusStopId( "2" ) )
                .thenReturn( Sets.newHashSet( tripStop3, tripStop4 ) );
        when( gtfsStaticData.getAllScheduledTripStopsFromBusStopId( "3" ) )
                .thenReturn( Sets.newHashSet( tripStop5, tripStop6 ) );
    }

    @Test
    public void getNextBusStop( ) throws Exception {
        busStopManager.addBusStopToTrack( "myBusStopId" );
        Optional<BusStop> busStop =
                busStopManager.getNextBusStop( Optional.empty( ) );
        assertTrue( busStop.isPresent() );
        assertEquals( 1, busStop.get().getBusStopId() );

        busStop =
                busStopManager.getNextBusStop( Optional.of( 1 ) );
        assertTrue( busStop.isPresent() );
        assertEquals( 2, busStop.get().getBusStopId() );

        busStop =
                busStopManager.getNextBusStop( Optional.of( 2 ) );
        assertTrue( busStop.isPresent() );
        assertEquals( 3, busStop.get().getBusStopId() );
    }

}