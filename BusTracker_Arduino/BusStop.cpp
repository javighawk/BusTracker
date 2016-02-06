#include "BusStop.h"
#include <stdlib.h>

extern tm getCurrentTimeDate();

/*
 * Constructor
 */
BusStop::BusStop(int id, int busLine){
  	BSTOP_id = id;
    BSTOP_busLine = busLine;
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
  
  	return minWait;
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
  	if( BSTOP_sTime[p].tm_hour == 0 ) BSTOP_sTime[p].tm_mday += 1;
  
  	// Give format
	  mktime(&BSTOP_sTime[p]);
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
