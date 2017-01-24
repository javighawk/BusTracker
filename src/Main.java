import java.io.IOException;
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
	public static List<BusStopThread> bStops;
	public static Comparator<Bus> waitTimeComp = new Comparator<Bus>(){
		@Override
		public int compare(Bus o1, Bus o2) {
			return o1.getSchedTime().plusSeconds(o1.getDelay()).compareTo(o2.getSchedTime().plusSeconds(o2.getDelay()));
		}		
	};
	
	/* Shared variables */
	public static GTFSData gtfsdata = new GTFSData();
	public static Display disp;
	public static URL url;
	public static GPIO gpio;
	
	
	/*
	 * Initialize bus tracker 
	 * @param gtfsPath Path to the static GTFS files
	 */
	public static void initialize(String gtfsPath){
		// Initialize display
		try {
			disp = new Display();
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		// Load GTFS data
		disp.showParse();
		gtfsdata.parseFromPath(gtfsPath);
		
		// Initialize ArrayList with all the bus stops
		bStops = new ArrayList<BusStopThread>();
		
		// Initialize bus stop objects and add them to the set
		disp.showbstp();
		for (String name : bStop_names)
			bStops.add(new BusStopThread(name));
		
		// Initialize URL
		try {
			url = new URL(gtfsURL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		gpio = new GPIO();		
		disp.clearAndWrite();
	}
	
	
	/*
	 * Main function
	 * @param args Arguments. Path to the GTFS static data
	 */
	public static void main(String[] args){

		// Initialize system
		initialize(args[0]);
		
		// Start threads
		for(BusStopThread bstt : bStops)
			bstt.start();
		
		disp.startDisplayBusStops();
		

	}
}