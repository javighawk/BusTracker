import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.sun.org.apache.bcel.internal.generic.RET;

public class GTFSData {

	/* Sets of maps storing the information from the static GTFS feed */
	public Set<Map<String, String>> agency = new HashSet<Map<String, String>>();
	public Set<Map<String, String>> routes = new HashSet<Map<String, String>>();
	public Set<Map<String, String>> stop_times = new HashSet<Map<String, String>>();
	public Set<Map<String, String>> stops = new HashSet<Map<String, String>>();
	public Set<Map<String, String>> trips = new HashSet<Map<String, String>>();
	public Set<Map<String, String>> calendar = new HashSet<Map<String, String>>();
	public Set<Map<String, String>> calendar_dates = new HashSet<Map<String, String>>();
	
	
	/*
	 * Parse all GTFS data from txt files
	 * @param path Path to the txt files containing the GTFS static data
	 */
	public void parseFromPath(String path){
		// Parse agency, routes, stop times, stops and trips
		parse(Paths.get(path, "agency.txt").toString(), this.agency);
		parse(Paths.get(path, "routes.txt").toString(), this.routes);
		parse(Paths.get(path, "stop_times.txt").toString(), this.stop_times);
		parse(Paths.get(path, "stops.txt").toString(), this.stops);
		parse(Paths.get(path, "trips.txt").toString(), this.trips);
		parse(Paths.get(path, "calendar.txt").toString(), this.calendar);
		parse(Paths.get(path, "calendar_dates.txt").toString(), this.calendar_dates);
	}
	
	
	/*
	 * Parse data from GTFS static feed
	 * @param filename Path to the text file
	 * @param map Set of maps storing the info
	 */
	public void parse(String filepath, Set<Map<String, String>> map){
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
				Map<String, String> m = new HashMap<String, String>();
				
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
	public Set<Map<String, String>> getMapFromData(Set<Map<String, String>> set, Map<String, String> map){
		// Initialize Set
		Set<Map<String, String>> returnSet = new HashSet<Map<String, String>>();
		
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
	public Set<Map<String, String>> getMapFromData(Set<Map<String, String>> set, String key, String value){
		// Create a HashMap to execute query
		Map<String, String> m1 = new HashMap<String, String>();
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
	public Set<String> getBusStopIDs(String busStopName){
		// Initialize Set to return
		Set<String> ret = new HashSet<String>();
		
		// Run query
		Set<Map<String, String>> m = getMapFromData(this.stops, "stop_name", busStopName);
		
		// Extract the stop IDs
		for (Map<String, String> s : m)
			ret.add(s.get("stop_id"));
		
		return ret;
	}
	
	
	/*
	 * Get bus stop ID for a given trip and stop name
	 * @param busStopName Name of the bus stop
	 * @param tripID Trip id as a String
	 * @return Stop ID as a String
	 * @throws Exception if a trip ID and bus name match more than one bus stop ID
	 */
	public String getBusStopID(String busStopName, String tripID) throws Exception{
		// Get bus stop IDs for the given bus stop name
		Set<String> busStopIDs = getBusStopIDs(busStopName);
		
		// Iterate through all the bus stop IDs
		for (String s : busStopIDs) {
			// Create map to use on query
			Map<String, String> key = new HashMap<String, String>();
			key.put("trip_id", tripID);
			key.put("stop_id", s);
			
			// Get trip entries with the given trip ID and this bus stop ID
			Set<Map<String, String>> trips = getMapFromData(this.stop_times, key);
			
			// Check length
			if (trips.size() > 1)
				throw new Exception("More than one stop id match the given trip ID and bus stop name");
			else if (trips.size() == 1){
				return trips.iterator().next().get("stop_id");
			}
		}
		
		return null;
	}
	
	
	/*
	 * Retrieve stop times entries of all trips going through a given bus stop
	 * @param busStopName Name of the bus stop as String
	 * @return Set of entries from stop_times that go through the given bus stop
	 */
	public Set<Map<String, String>> getBusStopTimes(String busStopName){
		// Initialize returning set
		Set<Map<String, String>> ret = new HashSet<Map<String, String>>();
		
		// Get stop IDs matching the given bus stop name
		Set<String> stop_id = getBusStopIDs(busStopName);
		
		// Iterate through the stop_ids
		for (String sid : stop_id)
			// Retrieve the stop times for the given stop id and add it to returning set
			ret.addAll(getMapFromData(this.stop_times, "stop_id", sid));
			
		return ret;
	}
	
	
	/*
	 * Get trip direction given a trip ID
	 * @param tripID Trip ID in string
	 * @return Trip direction as a string as specified in the GTFS static data
	 * @throws Exception If the given trip ID maps to more than 1 trip
	 */
	public String getTripDirection(String tripID) throws Exception{
		// Run query
		Set<Map<String, String>> m2 = getMapFromData(this.trips, "trip_id", tripID);
		
		// Check if we have retrieved more than one Trip
		if (m2.size() > 1)
			throw new Exception("More than one trip matches the given trip ID");
		else if (m2.size() == 0){
			return null;
		}
		  
		// Return route number as String
		return m2.iterator().next().get("trip_headsign");
	}
	
	
	/*
	 * Retrieve the bus line number given the trip ID
	 * @param trip_id Trip id as a String
	 * @return Bus line number as a String
	 * @throws Exception If the given route ID maps to more than 1 route or trip
	 */
	public String getBusNumberFromTrip(String tripID) throws Exception{
		// Run query
		Set<Map<String, String>> m2 = getMapFromData(this.trips, "trip_id", tripID);
		
		// Check if we have retrieved more than one Trip
		if (m2.size() > 1)
			throw new Exception("More than one route matches the given route ID");
		else if (m2.size() == 0){
			return null;
		}
		
		// Get the line number given the route ID extracted
		return getBusNumberFromRoute(m2.iterator().next().get("route_id"));
	}
	
	
	/*
	 * Get route number given a route ID
	 * @param routeID Route ID in string
	 * @return Route number as a string as specified in the GTFS static data
	 * @throws Exception If the given route ID maps to more than 1 route
	 */
	public String getBusNumberFromRoute(String routeID) throws Exception{
		// Run query
		Set<Map<String, String>> m2 = getMapFromData(this.routes, "route_id", routeID);
		
		// Check if we have retrieved more than one Trip
		if (m2.size() > 1)
			throw new Exception("More than one route matches the given route ID");
		else if (m2.size() == 0){
			return null;
		}
		  
		// Return route number as String
		return m2.iterator().next().get("route_short_name");
	}
	
	
	/*
	 * Get scheduled arrival time of bus given the bus stop ID and the trip ID
	 * @param tripID Trip ID as String
	 * @param stopID Stop ID as String
	 * @return Scheduled arrival time as String
	 * @throws Exception If the given stop ID and trip ID maps to more than 1 scheduled time
	 */
	public String getScheduledArrivalTimeFromStopID(String tripID, String stopID) throws Exception{
		// Create a HashMap to execute query
		Map<String, String> m1 = new HashMap<String, String>();
		m1.put("trip_id", tripID);
		m1.put("stop_id", stopID);
		
		// Execute query
		Set<Map<String, String>> m2 = getMapFromData(this.stop_times, (Map<String, String>) m1);
		
		// Check if we have retrieved more than one Trip
		if (m2.size() > 1)
			throw new Exception("More than one sched time matches the given trip and stop ID");
		else if (m2.size() == 0){
			return null;
		}
		
		// Return scheduled time as String
		return m2.iterator().next().get("arrival_time");
	}
	
	
	/*
	 * Get scheduled arrival time of bus given trip ID and a bus name
	 * @param tripID Trip ID as String
	 * @param stopName Stop name as String
	 * @return Scheduled arrival time as a LocalTime object
	 * @throws Exception If the given stop ID and trip ID maps to more than 1 scheduled time
	 */
	public LocalTime getScheduledArrivalTimeFromStopName(String tripID, String stopName) throws Exception{
		// Get stop IDs for the given stop name
		Set<String> stopIDs = getBusStopIDs(stopName);
		
		// Iterate through all the stop IDs
		for (String stopID : stopIDs) {
			// Get scheduled time
			String sched = getScheduledArrivalTimeFromStopID(tripID, stopID);
			
			if (!(sched == null)){
				try {
					// Parse scheduled arrival (this is in the bus agency's time zone)
					return LocalTime.parse(sched);					
					
				} catch (DateTimeParseException e) {
					// Extract the hour value and subtract "24"
					int hour = Integer.parseInt(sched.substring(0,2)) - 24;
					
					// Parse scheduled arrival (this is in the bus agency's time zone)
					return LocalTime.parse(String.format("%02d", hour) + sched.substring(2));
				}
			}
		}
		
		return null;
	}
	
	
	/*
	 * Get the working days for a given service ID
	 * @param serciceID Service ID as a String
	 * @return Map containing true on the days where the service is working
	 * @throws Exception If the given serviceID maps to multiple or none entries on the calendar table
	 */
	public Map<String, Boolean> getDatesFromServiceID(String serviceID) throws Exception{
		// Retrieve the calendar entry for the given Service ID
		Set<Map<String, String>> entry = getMapFromData(this.calendar, "service_id", serviceID);
		
		// Check output
		if (entry.size() > 1)
			throw new Exception("This service ID maps to multiple calendar entries");
		else if (entry.size() == 0)
			throw new NullPointerException("No calendar entry for the given service ID");
		
		// Retrieve only component of the Set we have obtained
		Map<String, String> cal = entry.iterator().next();
		
		// Initialize returning Map
		Map<String, Boolean> ret = new HashMap<String, Boolean>();
		
		// Add values to ret
		ret.put("monday", cal.get("monday").equals("1"));
		ret.put("tuesday", cal.get("tuesday").equals("1"));
		ret.put("wednesday", cal.get("wednesday").equals("1"));
		ret.put("thursday", cal.get("thursday").equals("1"));
		ret.put("friday", cal.get("friday").equals("1"));
		ret.put("saturday", cal.get("saturday").equals("1"));
		ret.put("sunday", cal.get("sunday").equals("1"));
		
		return ret;
	}
	
	
	/*
	 * Get the calendar data for a given trip ID
	 * @param tripID Trip ID as a String
	 * @return Map containing true on the days where the service is working
	 * @throws Exception If the given trip ID maps to multiple or none entries on the calendar table
	 */
	public Map<String, String> getCaledarFromTripID(String tripID) throws Exception{
		// Retrieve the trip entry for the given Service ID
		Set<Map<String, String>> entry = getMapFromData(this.trips, "trip_id", tripID);
		
		// Check output
		if (entry.size() > 1)
			throw new Exception("This trip ID maps to multiple trip entries");
		else if (entry.size() == 0)
			throw new NullPointerException("No trips entry for the given trip ID");
		
		// Retrieve only component of the Set we have obtained
		Map<String, String> trip = entry.iterator().next();
		
		// Retrieve the calendar entry for the given Service ID
		Set<Map<String, String>> cal = getMapFromData(this.calendar, "service_id", entry.iterator().next().get("service_id"));
		
		// Check output
		if (cal.size() > 1)
			throw new Exception("This service ID maps to multiple calendar entries");
		else if (cal.size() == 0)
			throw new NullPointerException("No calendar entry for the given service ID");
		
		// Return only component of the Set we have obtained
		return cal.iterator().next();
	}
	
	
	/*
	 * Get timezone of the bus agency
	 * @return Timezone as String
	 */
	public String getTimeZone(){
		// Return timezone as String
		return this.agency.iterator().next().get("agency_timezone");
	}
}