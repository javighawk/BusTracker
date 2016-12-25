import java.util.Map;
import java.util.Set;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.lang.Exception;
import java.time.LocalTime;
import java.time.ZoneId;
import static java.time.temporal.ChronoUnit.MINUTES;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;

public class Main {
	
	/* Initialize user variables */
	public static String gtfsPath = "gtfs/";
	public static String gtfsURL = "http://apps2.saskatoon.ca/app/data/TripUpdate/TripUpdates.pb";
	public static String bStop_22nd_name = "22nd Street / Avenue M";
	
	/* Declare global variables */
	public static BusStopTrackThread bStop_22nd;
	public static GTFSData gtfsdata = new GTFSData();
	
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
		
		// Initialize bus stop objects
		bStop_22nd = new BusStopTrackThread(bStop_22nd_name, gtfsURL);
		
		// Trace
		System.out.println("End init");
	}
	
	
	/*
	 * Main function
	 * @param args Arguments
	 */
	public static void main(String[] args){
		// Initialize system
		initialize();
		
		// Start thread
		bStop_22nd.start();
	}
}