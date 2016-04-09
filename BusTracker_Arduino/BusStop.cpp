#include "BusStop.h"
#include <stdlib.h>

/*
  BusStop class. This represent a bus stop in one direction in the bus network.
  
  For each bus stop, we have the line number and a series of waiting times.

  The waiting time is the time to the closest bus that hasn't come yet. We bave
  defined 3 waiting times per bus stop, which means that for each BusStop object
  we store the waiting time for the next 3 buses that are to come. If you want to
  store more or less bus stops, you can change that parameter in the header file.

  Attributes of each BusStop object:
  BSTOP_id: The ID of the bus stop, specified by GTFS and the transit company.
  BSTOP_busLine: The number of the line.
  BSTOP_lastUpdated: The value in millis when the data was last updated.
  BSTOP_sTime[]: tm structs that store when the next buses are coming.
  
 */

extern tm getCurrentTimeDate();

/*
 * Constructor
 */
BusStop::BusStop(int id, int busLine){
  	BSTOP_id = id;
    BSTOP_busLine = busLine;
    BSTOP_lastUpdated = 0;
  	BSTOP_setEmptyTime();
}

/*
 * Returns the waiting time for the bus
 *
 * @param p The number of the bus to wait (0 = most recent)
 *
 * @return The waiting time in minutes. -1 if error occurred.
 */
int BusStop::BSTOP_getWaitTime(int p){

  	// Prevent index out of bounds
  	if( p >= WAITTIMES_N )
  		return -1;
  
  	// Get current time and bus time
  	tm now = getCurrentTimeDate();
  	tm busTime = BSTOP_sTime[p];
  
  	// If real bus time is not available, return -1
  	if( busTime.tm_hour == -1 || busTime.tm_min == -1 )
  		  return -1;
  
  	// Get differences of hour and minutes
  	int hourWait = busTime.tm_hour - now.tm_hour;
  	int minWait = busTime.tm_min - now.tm_min;
  
  	// Adjust the hour wait if the result is negative
  	while( hourWait < 0 ){
  		hourWait += 24;
  	}
  
  	// Adjust the minutes wait if the result is negative
  	minWait += hourWait*60;

    // Adjustment
    if( minWait > 0 )
        minWait -= 1;

  	return max(0,minWait);
}


/*
 * Setter
 */
void BusStop::BSTOP_setSTime(String st, int p){

  	if( p >= WAITTIMES_N )
  	  	return;
    
  	// Get Real time
  	BSTOP_sTime[p].tm_hour = st.substring(10,12).toInt();
  	BSTOP_sTime[p].tm_min = st.substring(13,15).toInt();
  	if( st[15] == 'p' && BSTOP_sTime[p].tm_hour != 12 ) BSTOP_sTime[p].tm_hour += 12;
    if( st[15] == 'a' && BSTOP_sTime[p].tm_hour == 12 ) BSTOP_sTime[p].tm_hour = 0;

    BSTOP_setLastUpdated();
}

/*
 * Set error time
 */
void BusStop::BSTOP_setEmptyTime(){

  	for( int i=0 ; i<WAITTIMES_N ; i++ ){
    		BSTOP_sTime[i].tm_hour = -1;
    		BSTOP_sTime[i].tm_min = -1;
  	}
}
