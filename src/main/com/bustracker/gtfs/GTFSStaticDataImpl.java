package com.bustracker.gtfs;

import com.bustracker.trip.TripStop;
import com.bustracker.trip.calendar.TripCalendar;
import com.bustracker.trip.calendar.TripCalendarBuilder;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GTFSStaticDataImpl implements GTFSStaticData {

    private final String pathToGtfsFiles;

	/* Sets of maps storing the information from the static GTFS feed */
    private Set<Map<String, String>> agency = new HashSet<>();
	private Set<Map<String, String>> routes = new HashSet<>();
	private Set<Map<String, String>> stop_times = new HashSet<>();
	private Set<Map<String, String>> stops = new HashSet<>();
	private Set<Map<String, String>> trips = new HashSet<>();
	private Set<Map<String, String>> calendar = new HashSet<>();
	private Set<Map<String, String>> calendar_dates = new HashSet<>();
	private final Logger LOG = LoggerFactory.getLogger( GTFSStaticDataImpl.class );
	
	public GTFSStaticDataImpl( String path ) {
	    pathToGtfsFiles = path;
	    parseFromPath();
    }

	private void parseFromPath(){
		// Parse agency, routes, stop times, stops and trips
		parse( Paths.get(
		        pathToGtfsFiles, "agency.txt").toString(),
                this.agency);
		parse( Paths.get(
		        pathToGtfsFiles, "routes.txt").toString(),
                this.routes);
		parse( Paths.get(
		        pathToGtfsFiles, "stop_times.txt").toString(),
                this.stop_times);
		parse( Paths.get(
		        pathToGtfsFiles, "stops.txt").toString(),
                this.stops);
		parse( Paths.get(
		        pathToGtfsFiles, "trips.txt").toString(),
                this.trips);
		parse( Paths.get(
		        pathToGtfsFiles, "calendar.txt").toString(),
                this.calendar);
		parse( Paths.get(
		        pathToGtfsFiles, "calendar_dates.txt").toString(),
                this.calendar_dates);
	}

	/*
	 * Parse data from GTFS static feed
	 * @param filename Path to the text file
	 * @param map Set of maps storing the info
	 */
    private void parse( String filepath, Set<Map<String, String>> map ){
    	LOG.info( "Parsing file {}", filepath );

        // Declare header of the file as string array, where each element is
		// each of the keys of the maps
		String[] keys;
		
		// Read file
		try (Stream<String> stream = Files.lines(Paths.get(filepath))) {
			// Extract text file as array
			Object[] txtArray = stream.toArray();
			
			// Extract header and save it as keys array
			keys = ((String) txtArray[0]).split(",");
			
			// Iterate through the rest of the lines
			for (Object s : Arrays.copyOfRange(txtArray, 1, txtArray.length)) {
				// Split fields
				String[] fields = ((String) s).split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
				
				// Initialize map for the current line
				Map<String, String> m = new HashMap<>( );
				
				// Iterate through all the keys
				for (int i=0 ; i<fields.length ; i++) {
					m.put(keys[i], fields[i]);
				}
				
				// Save map
				map.add(m);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	/*
	 * Find on a given set an entry that contains the (key,value) pairs specified in the
	 * parameter map
	 * @param set Set where the function will look in
	 * @param map Map containing the (key,value) pairs to look for
	 * @return Set with the maps found that has the (key,value) pairs to find
	 */
    private Set<Map<String, String>> getMapFromData( Set<Map<String, String>> set, Map<String, String> map ){
		// Initialize Set
		Set<Map<String, String>> returnSet = new HashSet<>( );
		
		// Convert given map into set
		Set<Map.Entry<String, String>> refSet = map.entrySet();
		
		// Iterate through all the entries in the given set
		for (Map<String, String> entry : set) {
			// Convert to Set
			Set<Map.Entry<String, String>> checkSet = entry.entrySet();
			
			// Check if this set contains the reference set
			if (checkSet.containsAll(refSet))
				returnSet.add(entry);
		}
		return returnSet;
	}
	
	
	/*
	 * Find on a given set and entry that contains the (key,value) pair specified in the
	 * parameter map
	 * @param set Set where the function will look in
	 * @param key Key to look for
	 * @param value Value to look for
	 * @return Set with the maps found that has the (key,value) pair to find
	 */
    private Set<Map<String, String>> getMapFromData( Set<Map<String, String>> set, String key, String value ){
		// Create a HashMap to execute query
		Map<String, String> m1 = new HashMap<>( );
		m1.put(key, value);
		
		// Execute query and return
		return getMapFromData(set, m1);
	}
	
	
	/*
	 * Get bus stop IDs for a given bus stop name. Usually there would be 2,
	 * one for each direction
	 * @param busStopName Name of the bus stop
	 * @return Set of the bus stop IDs matching the given name
	 */
	@Override
	public Set<String> getBusStopIDs(String busStopName){
		// Initialize Set to return
		Set<String> ret = new HashSet<>( );
		
		// Run query
		Set<Map<String, String>> m = getMapFromData(this.stops, "stop_name", busStopName);
		
		// Extract the stop IDs
		for (Map<String, String> s : m)
			ret.add(s.get("stop_id"));
		
		return ret;
	}

	@Override
	public Set<TripStop> getAllScheduledTripStopsFromBusStopId( String busStopId ) {
		Set<TripStop> tripStops = Sets.newHashSet();
        Set<Map<String, String>> stopTimes =
                getMapFromData( this.stop_times, "stop_id", busStopId );
		for( Map<String, String> map : stopTimes ) {
            addTripStopToSet(
                    map.get( "trip_id" ),
                    map.get( "stop_id" ),
                    map.get( "arrival_time" ),
					tripStops );
		}
		return tripStops;
	}

    private void addTripStopToSet(
            String tripId,
            String stopId,
            String arrivalTime,
			Set<TripStop> set ) {
        Optional<TripCalendar> tripCalendar =
                getTripCalendarFromTripID( tripId );
        getBusNumberFromTrip( tripId ).ifPresent(
                bus -> tripCalendar.ifPresent( cal -> set.add(
                        TripStop.builder( )
                                .withTripId( tripId )
                                .withBusLine( bus )
                                .withBusStopId( stopId )
                                .withScheduledArrival( arrivalTime )
                                .withTripCalendar( cal )
                                .withDelay( Duration.ZERO )
                                .build( ) ) ) );
    }

	/*
	 * Retrieve the bus line number given the trip ID
	 * @param trip_id Trip id as a String
	 * @return Optional containing the bus line number as a String, or
	 *         an empty Optional if the given route ID maps to more
	 *         than 1 route or trip or none.
	 */
	private Optional<String> getBusNumberFromTrip(String tripID) {
		// Run query
		Set<Map<String, String>> m2 = getMapFromData(this.trips, "trip_id", tripID);
		
		// Check if we have retrieved more than one Trip
		if( m2.size() != 1 ) {
		    return Optional.empty();
        }

		// Get the line number given the route ID extracted
		return getBusNumberFromRoute(
		                m2.iterator().next().get( "route_id" ) );

	}
	
	/*
	 * Get route number given a route ID
	 * @param routeID Route ID in string
	 * @return Optional containing route number as a string as specified in
	 *         the GTFS static data, or an empty Optional if the given route
	 *         ID maps to more than 1 route or none.
	 */
	private Optional<String> getBusNumberFromRoute( String routeID ) {
		// Run query
		Set<Map<String, String>> m2 = getMapFromData(
		        this.routes, "route_id", routeID );

		// Check if we have retrieved more than one Trip
		if( m2.size() != 1 ) {
			return Optional.empty();
		}
		  
		// Return route number as String
		return Optional.of( m2.iterator().next().get( "route_short_name" ) );
	}

	private Optional<TripCalendar> getTripCalendarFromTripID(
	        String tripId ) {
	    Set<Map<String, String>> entry = getMapFromData(
	            this.trips, "trip_id", tripId );

        if( entry.size() != 1 ) {
            return Optional.empty();
        }

		return getTripCalendarFromServiceID(
				entry.iterator( ).next( ).get( "service_id" ) );
    }

	private Optional<TripCalendar> getTripCalendarFromServiceID(
	        String serviceID ) {
		// Retrieve the calendar entry for the given Service ID
		Set<Map<String, String>> entry = getMapFromData(
		        this.calendar, "service_id", serviceID);
		
		// Check output
		if( entry.size() != 1 ) {
		    return Optional.empty();
        }

		// Retrieve only component of the Set we have obtained
		Map<String, String> cal = entry.iterator().next();

        return getTripCalendarFromCalendarEntry( serviceID, cal );
	}

    private Optional<TripCalendar> getTripCalendarFromCalendarEntry(
            String serviceID, Map<String, String> cal ) {
        Set<DayOfWeek> weekdays = Sets.newHashSet( DayOfWeek.values() )
                .stream()
                .filter( d -> cal.get( d.name().toLowerCase() ).equals( "1" ) )
                .collect( Collectors.toSet() );

        TripCalendarBuilder builder = TripCalendar.builder( )
                .withCalendarId( cal.get( "service_id" ) )
                .withStartDate( cal.get( "start_date" ) )
                .withEndDate( cal.get( "end_date" ) )
                .withOperatingWeekdays( weekdays );

        getMapFromData( this.calendar_dates, "service_id", serviceID )
                .forEach( map -> {
                    String date = map.get( "date" );
                    if( map.get( "exception_type" ).equals( "1" ) ) {
                        builder.withServiceAddedExceptionDate( date );
                    } else {
                        builder.withServiceRemovedExceptionDate( date );
                    }
                } );
        return Optional.of( builder.build() );
    }

	/*
	 * Get timezone of the bus agency
	 * @return Timezone as String
	 */
	@Override
	public String getTimeZone(){
		// Return timezone as String
		return this.agency.iterator().next().get("agency_timezone");
	}
}