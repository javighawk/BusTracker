import java.util.List;
import java.util.TimeZone;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
	public static void main(String[] args){
		System.out.println("start");
		URL url;
		try {
			
			System.out.println("Initialize");
			url = new URL("http://apps2.saskatoon.ca/app/data/TripUpdate/TripUpdates.pb");
			//url = new URL("http://developer.mbta.com/lib/gtrtfs/Vehicles.pb");
			FeedMessage feed = FeedMessage.parseFrom(url.openStream());

			for (FeedEntity entity : feed.getEntityList()) {
				  if (!entity.hasTripUpdate()) {
				    continue;
				  }
				  
				  TripUpdate tu = entity.getTripUpdate();
				  
				  for (StopTimeUpdate stu : tu.getStopTimeUpdateList()){
					  if (stu.getStopId().equals("3968")) {
						  StopTimeEvent ste = stu.getArrival();
						  System.out.println(tu.getTrip().getTripId());
						  System.out.println(ste.getDelay());
					  }
				  }
				  
				 }			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		System.out.println("End");
	}
}