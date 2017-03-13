package com.bustracker;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.bustracker.bus.TripStopImpl;
import com.bustracker.bus.BusStopImplBackup;
import com.bustracker.gtfs.GTFSStaticDataImpl;
import com.bustracker.userio.Display;
import com.bustracker.userio.ButtonsManagerImpl;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class Main {
	
	/* Initialize user variables */
	public static String gtfsPath = "gtfs/";
	public static String gtfsURL = "http://apps2.saskatoon.ca/app/data/TripUpdate/TripUpdates.pb";
	public static String[] bStop_names = {"22nd Street / Avenue M"};
	
	/* Global variables */
	public static List<BusStopImplBackup> bStops;
	public static Comparator<TripStopImpl> waitTimeComp = new Comparator<TripStopImpl>(){
		@Override
		public int compare(TripStopImpl o1, TripStopImpl o2) {
			return o1.getSchedTime().plusSeconds(o1.getDelay()).compareTo(o2.getSchedTime().plusSeconds(o2.getDelay()));
		}		
	};
	
	/* Shared variables */
	public static GTFSStaticDataImpl gtfsdata = new GTFSStaticDataImpl();
	public static Display disp;
	public static URL url;
	public static ButtonsManagerImpl gpio;
	
	
	/*
	 * Initialize bus tracker 
	 * @param gtfsPath Path to the static GTFS files
	 */
	public static void initialize(String gtfsPath){
		// Initialize display
		try {
			disp = new Display();
		} catch (IOException | UnsupportedBusNumberException e) {
			e.printStackTrace();
		}
				
		// Load GTFS data
		gtfsdata.parseFromPath(gtfsPath);
		
		// Initialize ArrayList with all the bus stops
		bStops = new ArrayList<BusStopImplBackup>();
		
		// Initialize bus stop objects and add them to the set
		for (String name : bStop_names)
			bStops.add(new BusStopImplBackup(name));
		
		// Initialize URL
		try {
			url = new URL(gtfsURL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		gpio = new ButtonsManagerImpl();		
	}
	
	/*
	 * Main function
	 * @param args Arguments. Path to the GTFS static data
	 */
	public static void main(String[] args){

		// Initialize system
		initialize(args[0]);
		
		// Start threads
		for(BusStopImplBackup bstt : bStops)
			bstt.start();
		
		disp.startDisplayBusStops();
	}
}