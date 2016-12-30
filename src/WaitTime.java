import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Map;

public class WaitTime {
	
	/* Attributes */
	private String trip_id = null;
	private String stop_name = null;
	private boolean cityCentre;
	private Map<String, Boolean> calendar;
	private Map<String, LocalDate> start_end_dates;
	private LocalTime schedTime;
	private long delay = 0;

	
	/*
	 * Constructor
	 * @param trip_id Trip ID
	 * @param stop_name Name of the bus stop
	 * @param schedTime Scheduled arrival time as String
	 * @param calendar Map indicating the weekdays where this trip runs
	 * @param start_end_dates Map indicating the starting and ending date of this trip
	 * @throws Exception Thrown by GTFSData
	 */
	public WaitTime(String trip_id, String stop_name, String schedTime, Map<String, Boolean> calendar, Map<String, LocalDate> start_end_dates) throws Exception{
		// Save parameters
		this.trip_id = trip_id;
		this.stop_name = stop_name;
		this.calendar = calendar;

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
	public boolean isRunningToday(String weekday){return this.calendar.get(weekday);}
	
	
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