package com.bustracker.gtfs;

import java.time.LocalTime;
import java.util.Map;
import java.util.Set;

public interface GTFSStaticData {
	
	Set<String> getBusStopIds( String busStopName );
	
	String getBusStopID( String busStopName, String tripId );
	
	Set<Map<String, String>> getBusStopTimes( String busStopName );
	
	String getTripDirection( String tripId );
	
	String getBusNumberFromTrip( String tripId );
	
	String getBusNumberFromRoute( String routeId );
	
	String getScheduledArrivalTimeFromStopID( String tripId, String stopId );
	
	LocalTime getScheduledArrivalFromTripAndStopName( String tripId, String stopName );
	
	Map<String, Boolean> getDatesFromServiceId( String serviceId );
	
	Map<String, String> getCaledarFromTripId( String tripID );
	
	String getAgencyTimeZone();

}
