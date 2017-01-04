import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main {
	
	/* Initialize user variables */
	public static String gtfsPath = "gtfs/";
	public static String gtfsURL = "http://apps2.saskatoon.ca/app/data/TripUpdate/TripUpdates.pb";
	public static String[] bStop_names = {"22nd Street / Avenue M"};
	
	/* Global variables */
	public static List<BusStopTrackThread> bStops;
	public static Comparator<WaitTime> waitTimeComp = new Comparator<WaitTime>(){
		@Override
		public int compare(WaitTime o1, WaitTime o2) {
			return o1.getSchedTime().plusSeconds(o1.getDelay()).compareTo(o2.getSchedTime().plusSeconds(o2.getDelay()));
		}		
	};
	
	/* Shared variables */
	public static GTFSData gtfsdata = new GTFSData();
	public static Display disp;
	public static URL url;
	
	
	/*
	 * Initialize bus tracker 
	 * @param gtfsPath Path to the static GTFS files
	 */
	public static void initialize(String gtfsPath){
		// Load GTFS data
		gtfsdata.parseFromPath(gtfsPath);
		
		// Initialize ArrayList with all the bus stops
		bStops = new ArrayList<BusStopTrackThread>();
		
		// Initialize bus stop objects and add them to the set
		for (String name : bStop_names)
			bStops.add(new BusStopTrackThread(name));
		
		// Initialize URL
		try {
			url = new URL(gtfsURL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		// Initialize display
		disp = new Display();
	}
	
	
	/*
	 * Main function
	 * @param args Arguments. Path to the GTFS static data
	 */
	public static void main(String[] args){
		// Initialize system
		initialize(args[0]);
		
		// Start threads
		for(BusStopTrackThread bstt : bStops)
			bstt.start();
		
		// Start display thread
		disp.start();
	}
}