import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.lang.Exception;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedHeader;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.Position;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeEvent;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;
import com.google.transit.realtime.GtfsRealtime.VehicleDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition.VehicleStopStatus;

public class BusTracker {
	
	/* Initialize user variables */
	public static String gtfsPath = "gtfs/";
	public static String gtfsURL = "http://apps2.saskatoon.ca/app/data/TripUpdate/TripUpdates.pb";
	public static String busStopName = "22nd Street / Avenue M";
	
	/* Declare global variables */
	public static GTFSData gtfsdata = new GTFSData();
	public static Set<String> stop_ids = new HashSet<String>();
	public static URL url;
	
	/*
	 * Initialize bus tracker 
	 */
	public static void initialize(){
		// Trace
		System.out.println("start");
		
		// Load GTFS data
		gtfsdata.parseFromPath(gtfsPath);
		
		// Trace
		System.out.println("Parsed");
		
		// Get the stop IDs of the desired bus stops
		Map<String, String> busStopMap = new HashMap<String, String>();
		busStopMap.put("stop_name", busStopName);
		Set<Map<String, String>> stops = gtfsdata.getMapFromData(gtfsdata.stops, busStopMap);
		
		// Store stops IDs in a global set
		for (Map<String, String> m : stops)
			stop_ids.add(m.get("stop_id"));
		
		// Trace
		System.out.println("End init");
	}
	
	
	/*
	 * Main function
	 */
	public static void main(String[] args){
		// Initialize system
		initialize();

		// Start Trip update retrieval
		try {
			// Trace
			System.out.println("Initialize");
			
			// Initialize URL
			url = new URL(gtfsURL);
			
			// Get FeedMessage
			FeedMessage feed = FeedMessage.parseFrom(url.openStream());

			// Iterate through all the FeedEntities
			for (FeedEntity entity : feed.getEntityList()) {
				// Check if it has a Trip update
				if (!entity.hasTripUpdate()) {
					continue;
				}
							  
				// Get trip update
				TripUpdate tu = entity.getTripUpdate();
				
				// Get route number
				String routeNumber = gtfsdata.getRouteNumber(tu.getTrip().getRouteId());
				
				// Get trip direction
				String tripDirection = gtfsdata.getTripDirection(tu.getTrip().getTripId());
				
				if (!tripDirection.equals("City Centre"))
					continue;
				
				// Iterate through all the Stop updates
				for (StopTimeUpdate stu : tu.getStopTimeUpdateList()){
					
					// Work only with the stops of interest
					if (stop_ids.contains(stu.getStopId())) {						  
						System.out.print(routeNumber + " " + tripDirection + ": ");
						System.out.println(stu.getArrival().getDelay()/60 + " min");						
						System.out.println(gtfsdata.getScheduledArrivalTime(tu.getTrip().getTripId(), stu.getStopId()));
					}
				}
				  
				 }			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("End");
	}
}