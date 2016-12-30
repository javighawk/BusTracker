import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;

public class Main {
	
	/* Initialize user variables */
	public static String gtfsPath = "gtfs/";
	public static String gtfsURL = "http://apps2.saskatoon.ca/app/data/TripUpdate/TripUpdates.pb";
	public static String bStop_22nd_name = "22nd Street / Avenue M";
	
	/* Global variables */
	public static BusStopTrackThread bStop_22nd;
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
	 */
	public static void initialize(){
		System.out.println("Init");
		
		// Load GTFS data
		gtfsdata.parseFromPath(gtfsPath);
		
		// Initialize bus stop objects
		bStop_22nd = new BusStopTrackThread(bStop_22nd_name, gtfsURL);
		
		// Initialize URL
		try {
			url = new URL(gtfsURL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
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