import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class GTFSData {

	/* Sets of maps storing the information from the static GTFS feed */
	public Set<Map<String, String>> agency = new HashSet<Map<String, String>>();
	public Set<Map<String, String>> routes = new HashSet<Map<String, String>>();
	public Set<Map<String, String>> stop_times = new HashSet<Map<String, String>>();
	public Set<Map<String, String>> stops = new HashSet<Map<String, String>>();
	public Set<Map<String, String>> trips = new HashSet<Map<String, String>>();
	
	
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
		  
		// Return route number as String
		return m2.iterator().next().get("trip_headsign");
	}
	
	
	/*
	 * Get route number given a route ID
	 * @param routeID Route ID in string
	 * @return Route number as a string as specified in the GTFS static data
	 * @throws Exception If the given route ID maps to more than 1 route
	 */
	public String getRouteNumber(String routeID) throws Exception{
		// Run query
		Set<Map<String, String>> m2 = getMapFromData(this.routes, "route_id", routeID);
		
		// Check if we have retrieved more than one Trip
		if (m2.size() > 1)
			throw new Exception("More than one route matches the given route ID");
		  
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
	public String getScheduledArrivalTime(String tripID, String stopID) throws Exception{
		// Create a HashMap to execute query
		Map<String, String> m1 = new HashMap<String, String>();
		m1.put("trip_id", tripID);
		m1.put("stop_id", stopID);
		
		// Execute query
		Set<Map<String, String>> m2 = getMapFromData(this.stop_times, (Map<String, String>) m1);
		
		// Check if we have retrieved more than one Trip
		if (m2.size() > 1)
			throw new Exception("More than one sched time matches the given trip and stop ID");
		
		// Return scheduled time as String
		return m2.iterator().next().get("arrival_time");
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