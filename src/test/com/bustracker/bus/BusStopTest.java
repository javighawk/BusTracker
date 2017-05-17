package com.bustracker.bus;

import com.bustracker.trip.TripStop;
import com.bustracker.trip.calendar.TripCalendar;
import com.google.common.collect.Sets;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Javier on 16-May-17.
 */
public class BusStopTest {
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
            1, 10, 1,
            now.plusMinutes( 1 ), tripCalendar, Duration.ZERO );
    private final TripStop tripStop2 = new TripStop(
            2, 11, 1,
            now.plusMinutes( 2 ), tripCalendar, Duration.ZERO );
    private final TripStop tripStop3 = new TripStop(
            3, 12, 1,
            now.plusMinutes( 3 ), tripCalendar, Duration.ZERO );
    private final TripStop tripStop4 = new TripStop(
            4, 13, 1,
            now.plusMinutes( 4 ), tripCalendar, Duration.ZERO );
    private final TripStop tripStop5 = new TripStop(
            5, 14, 1,
            now.plusMinutes( 5 ), tripCalendar, Duration.ZERO );
    private final TripStop tripStop6 = new TripStop(
            6, 15, 1,
            now.plusMinutes( 6 ), tripCalendar, Duration.ZERO );
    private final BusStop busStop = new BusStop(
            1, Sets.newHashSet(
                    tripStop1,
                    tripStop2,
                    tripStop3,
                    tripStop4,
                    tripStop5,
                    tripStop6 ) );

    @Test
    public void testGetUpcomingTrip() {
        assertTripStop( tripStop1, busStop.getUpcomingBus( 0 ) );
        assertTripStop( tripStop2, busStop.getUpcomingBus( 1 ) );
        assertTripStop( tripStop3, busStop.getUpcomingBus( 2 ) );
        assertTripStop( tripStop4, busStop.getUpcomingBus( 3 ) );
        assertTripStop( tripStop5, busStop.getUpcomingBus( 4 ) );
        assertTripStop( tripStop6, busStop.getUpcomingBus( 5 ) );
    }

    private void assertTripStop( TripStop expected, Optional<TripStop> actual ) {
        assertTrue( actual.isPresent() );
        assertEquals( expected, actual.get() );
    }
}
