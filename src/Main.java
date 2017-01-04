import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class Main {
	
	/* Initialize user variables */
	public static String gtfsPath = "gtfs/";
	public static String gtfsURL = "http://apps2.saskatoon.ca/app/data/TripUpdate/TripUpdates.pb";
	public static String[] bStop_names = {"22nd Street / Avenue M"};
	public static int numOfBusesToShow = 3;
	
	/* Global variables */
	public static BusStopTrackThread[] bStops;
	public static Comparator<WaitTime> waitTimeComp = new Comparator<WaitTime>(){
		@Override
		public int compare(WaitTime o1, WaitTime o2) {
			return o1.getSchedTime().plusSeconds(o1.getDelay()).compareTo(o2.getSchedTime().plusSeconds(o2.getDelay()));
		}		
	};
	
	/* Shared variables */
	public static GTFSData gtfsdata = new GTFSData();
	public static URL url;
	
	
	/*
	 * Initialize bus tracker 
	 * @param gtfsPath Path to the static GTFS files
	 */
	public static void initialize(String gtfsPath){
		// Load GTFS data
		gtfsdata.parseFromPath(gtfsPath);
		
		// Initialize set with all the bus stops
		Set<BusStopTrackThread> bStops_set = new HashSet<BusStopTrackThread>();
		
		// Initialize bus stop objects and add them to the set
		for (String name : bStop_names)
			bStops_set.add(new BusStopTrackThread(name, gtfsURL));
		
		// Convert to array
		bStops = (BusStopTrackThread[]) bStops_set.toArray();
		
		// Initialize URL
		try {
			url = new URL(gtfsURL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
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
	}
}