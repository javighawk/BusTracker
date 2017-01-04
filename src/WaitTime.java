import static java.time.temporal.ChronoUnit.MINUTES;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

public class WaitTime {
	
	/* Attributes */
	private String trip_id = null;
	private String stop_name = null;
	private boolean cityCentre;
	private Map<String, Boolean> calendar;
	private Map<String, LocalDate> start_end_dates;
	private String schedTime_str;
	private LocalTime schedTime;
	private long delay = 0;

	
	/*
	 * Constructor
	 * @param trip_id Trip ID
	 * @param stop_name Name of the bus stop
	 * @param schedTime Scheduled arrival time as String
	 * @param cal Calendar data for this trip
	 * @throws Exception Thrown by GTFSData
	 */
	public WaitTime(String trip_id, String stop_name, String schedTime, Map<String, String> cal) throws Exception{
		// Save parameters
		this.trip_id = trip_id;
		this.stop_name = stop_name;
		this.schedTime_str = schedTime;

		// Save wait time
		try {
			// Parse scheduled arrival (this is in the bus agency's time zone)
			this.schedTime = LocalTime.parse(schedTime);								
		} catch (DateTimeParseException e) {
			// Extract the hour value and subtract "24"
			int hour = Integer.parseInt(schedTime.substring(0,2)) - 24;
			
			// Parse scheduled arrival (this is in the bus agency's time zone)
			this.schedTime = LocalTime.parse(String.format("%02d", hour) + schedTime.substring(2));
		}
		
		// Get direction
		this.cityCentre = Main.gtfsdata.getTripDirection(trip_id).equals("City Centre");
		
		// Initialize calendar dates
		this.calendar = new HashMap<String, Boolean>();
		
		// Add entries to calendar
		this.calendar.put("monday", cal.get("monday").equals("1"));
		this.calendar.put("tuesday", cal.get("tuesday").equals("1"));
		this.calendar.put("wednesday", cal.get("wednesday").equals("1"));
		this.calendar.put("thursday", cal.get("thursday").equals("1"));
		this.calendar.put("friday", cal.get("friday").equals("1"));
		this.calendar.put("saturday", cal.get("saturday").equals("1"));
		this.calendar.put("sunday", cal.get("sunday").equals("1"));
		
		// Initialize start/end date
		this.start_end_dates = new HashMap<String, LocalDate>();
		
		// Add start/end dates
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
		this.start_end_dates.put("start_date", LocalDate.parse(cal.get("start_date"), dtf));
		this.start_end_dates.put("end_date", LocalDate.parse(cal.get("end_date"), dtf));
	}

	
	/*
	 * Add seconds to the scheduled time
	 * @param seconds Seconds to add
	 */
	public void addTime(long seconds){
		this.schedTime = this.schedTime.plusSeconds(seconds);
	}
	
	
	/* Getters */
	public String getTripID(){return this.trip_id;}
	public String getStopName(){return this.stop_name;}
	public boolean getCityCentre(){return this.cityCentre;}
	public LocalTime getSchedTime(){return this.schedTime;}
	public long getDelay(){return this.delay;}
	
	
	/*
	 * Get real arrival time
	 * @return Arrival time as LocalTime object
	 */
	public LocalTime getRealTime(){
		return this.schedTime.plusSeconds(this.delay);
	}
	
	
	/*
	 * Get if this trip is running for a given date
	 * @param date Date
	 * @return
	 */
	public boolean isRunning(LocalDate date){
		// Check if date is within start/end dates
		if (date.compareTo(this.start_end_dates.get("start_date")) >= 0 &&
			date.compareTo(this.start_end_dates.get("end_date")) <= 0){
			
			// Get weekday
			String weekday = date.getDayOfWeek().name().toLowerCase();
			
			return this.calendar.get(weekday);
		}
		
		return false;
	}
	
	
	/*
	 * Get waiting time given a scheduled arrival and a delay
	 * @return 
	 */
	public long getWaitingTime(){
		// Get current time in the Bus agency local time
		LocalTime now = LocalTime.now(ZoneId.of(Main.gtfsdata.getTimeZone()));
		
		// Initialize time difference in minutes
		long diff;
		
		try {
			// Parse scheduled arrival (this is in the bus agency's time zone)
			LocalTime schedTime = LocalTime.parse(this.schedTime_str);					
			
			// Get the difference in time
			diff = now.until(schedTime, MINUTES);
			
		} catch (DateTimeParseException e) {
			// Extract the hour value and subtract "24"
			int hour = Integer.parseInt(this.schedTime_str.substring(0,2)) - 24;
			
			// Parse scheduled arrival (this is in the bus agency's time zone)
			LocalTime schedTime = LocalTime.parse(String.format("%02d", hour) + this.schedTime_str.substring(2));
			
			// Get the difference in time (add the 24 hours that we subtracted before)
			diff = now.until(schedTime, MINUTES) + 24*60;
		}
		
		// Add delay
		diff += (int)(delay/60);
		
		// Subtract 1 minute to wait time to avoid unwanted missed buses!
		if (diff > 0)
			diff -= 1;
		
		return diff;		
	}
	
	
	
	/* Setters */
	public void setTripID(String trip_id){this.trip_id = trip_id;}
	public void setStopName(String stop_name){this.stop_name = stop_name;}
	public void setCityCentre(boolean cityCentre){this.cityCentre = cityCentre;}
	public void setDelay(long delay){this.delay = delay;}
	public void setCalendar(Map<String, Boolean> calendar){this.calendar = calendar;}
	
	public boolean equals(Object other){
		return (this.trip_id.equals(((WaitTime)other).trip_id) &&
				this.stop_name.equals(((WaitTime)other).stop_name) &&
				this.calendar.equals(((WaitTime)other).calendar) &&
				this.delay == ((WaitTime)other).delay);
	}
}