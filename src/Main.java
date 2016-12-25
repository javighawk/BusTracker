import java.net.MalformedURLException;
import java.net.URL;

public class Main {
	
	/* Initialize user variables */
	public static String gtfsPath = "gtfs/";
	public static String gtfsURL = "http://apps2.saskatoon.ca/app/data/TripUpdate/TripUpdates.pb";
	public static String bStop_22nd_name = "22nd Street / Avenue M";
	
	/* Global variables */
	public static BusStopTrackThread bStop_22nd;
	
	/* Shared variables */
	public static GTFSData gtfsdata = new GTFSData();
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
		
		// Initialize bus stop objects
		bStop_22nd = new BusStopTrackThread(bStop_22nd_name, gtfsURL);
		
		// Initialize URL
		try {
			url = new URL(gtfsURL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
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