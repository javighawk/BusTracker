package com.bustracker.trip.calendar;

import com.google.common.collect.Sets;

import java.time.LocalDate;
import java.util.Set;

/**
 * Created by Javier on 2017-04-30.
 */
class TripCalendarException {

    private Set<LocalDate> addedServiceDates = Sets.newHashSet();
    private Set<LocalDate> removedServiceDates = Sets.newHashSet();

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
}
