package com.bustracker.trip;

import com.bustracker.trip.calendar.TripCalendar;
import com.google.common.collect.Sets;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Created by Javier on 2017-05-06.
 */
public class TripStopBuilderTest {

    private final Set<DayOfWeek> operatingDaysToday = Sets.newHashSet(
            LocalDate.now().getDayOfWeek() );
    private final TripCalendar tripCalendarToday = TripCalendar.builder( )
            .withStartDate( "19700101" )
            .withEndDate( "99991231" )
            .withCalendarId( "" )
            .withOperatingWeekdays( operatingDaysToday )
            .build( );
    private final TripStop tripStopToday = TripStop.builder( )
            .withBusLine( "" )
            .withBusStopId( "" )
            .withTripId( "" )
            .withDelay( Duration.ZERO )
            .withScheduledArrival( "24:30:00" )
            .withTripCalendar( tripCalendarToday )
            .build( );

    private final Set<DayOfWeek> operatingDaysNextWeek = Sets.newHashSet(
            LocalDate.now().getDayOfWeek() );
    private final TripCalendar tripCalendarNextWeek = TripCalendar.builder( )
            .withStartDate( "19700101" )
            .withEndDate( "99991231" )
            .withCalendarId( "" )
            .withOperatingWeekdays( operatingDaysNextWeek )
            .build( );
    private final TripStop tripStopNextWeek = TripStop.builder( )
            .withBusLine( "" )
            .withBusStopId( "" )
            .withTripId( "" )
            .withDelay( Duration.ZERO )
            .withScheduledArrival( "00:00:00" )
            .withTripCalendar( tripCalendarNextWeek )
            .build( );

    @Test
    public void testArrivalHourOver23() {
        assertEquals(
                LocalDate.now().plusDays( 1 ),
                tripStopToday.getRealArrivalDateTime().toLocalDate() );
    }

    @Test
    public void testArrivalHourUnder23() {
        assertEquals(
                LocalDate.now().plusDays( 7 ),
                tripStopNextWeek.getRealArrivalDateTime().toLocalDate() );
    }
}
