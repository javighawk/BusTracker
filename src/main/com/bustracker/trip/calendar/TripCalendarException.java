package com.bustracker.trip.calendar;

import com.google.common.collect.Sets;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Javier on 2017-04-30.
 */
class TripCalendarException {

    private final Set<LocalDate> addedServiceDates;
    private final Set<LocalDate> removedServiceDates;

    public TripCalendarException() {
        addedServiceDates = Sets.newHashSet();
        removedServiceDates = Sets.newHashSet();
    }

    private TripCalendarException(
            Set<LocalDate> addedServiceDates,
            Set<LocalDate> removedServiceDates ) {
        this.addedServiceDates = addedServiceDates;
        this.removedServiceDates = removedServiceDates;
    }

    public void addServiceAddedDate( LocalDate date ) {
        addedServiceDates.add( date );
    }

    public void addServiceRemovedDate( LocalDate date ) {
        removedServiceDates.add( date );
    }

    public boolean isServiceAdded( LocalDate date ) {
        return addedServiceDates.contains( date );
    }

    public boolean isServiceRemoved( LocalDate date ) {
        return removedServiceDates.contains( date );
    }

    public boolean isServiceAddedOnOrAfter( LocalDate localDate ) {
        for( LocalDate date : addedServiceDates ) {
            if( localDate.isBefore( date ) || localDate.equals( date ) ) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format(
                "CalendarException=[addedServiceOn=%s, removedServiceOn=%s]",
                addedServiceDates,
                removedServiceDates );
    }

    TripCalendarException getCopyWithDayShift() {
        Set<LocalDate> addedServiceDatesShifted = addedServiceDates
                .stream( )
                .map( d -> d.plusDays( 1 ) )
                .collect( Collectors.toSet( ) );
        Set<LocalDate> removedServiceDatesShifted = removedServiceDates
                .stream( )
                .map( d -> d.plusDays( 1 ) )
                .collect( Collectors.toSet( ) );
        return new TripCalendarException(
                addedServiceDatesShifted, removedServiceDatesShifted );
    }
}
