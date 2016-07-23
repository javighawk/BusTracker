/*
  BusStop class. This represent a bus stop in one direction in the bus network.
  
  For each bus stop, we have the line number and a series of waiting times.

  The waiting time is the time to the closest bus that hasn't come yet. We bave
  defined 3 waiting times per bus stop, which means that for each BusStop object
  we store the waiting time for the next 3 buses that are to come. If you want to
  store more or less bus stops, you can change that parameter in the header file.

  Attributes of each BusStop object:
  id: The ID of the bus stop, specified by GTFS and the transit company.
  busLine: The number of the line.
  lastUpdated: The value in millis when the data was last updated.
  sTime[]: tmElements_t structs that store when the next buses are coming.

  Author: Javier Garcia  
 */

#include "BusStop.h"

/* Declaration of external functions used here */
extern int8_t *MAIN_getCurrentHour();
extern int8_t *MAIN_getCurrentMinute();


/*
 * Constructor
 */
BusStop::BusStop(char *id){
  	this->id = id;
    lastUpdated = 0;
  	resetTimes();
}


/*
 * Set a new waiting time for this bus stop after
 * the last waiting time stored
 * 
 * @param st String with the arrival time of the bus
 * @param busLine The line of the bus to arrive
 */
void BusStop::setWTime(String st, uint8_t busLine){
    // Initial assert: Bus line number has to be positive
    if( busLine <= 0 )
        return;

    // Look for an empy slot in the waiting times array
    uint8_t p;
    for( p=0 ; ; p++ ){
        if( p == WAITTIMES_N )
            return;
        
        if( wBusLine[p] == 0 )
            break;
    }

    int8_t h, m;
    
  	// Get Real waiting time for this bus
  	h = (int8_t) st.substring(0,2).toInt();
  	m = (int8_t) st.substring(3,5).toInt();
  	if( st[5] == 'p' && h != 12 ) h += 12;
    if( (st[5] == 'a' || st[5] == 'x') && h == 12 ) h = 0;

    // Compute time difference between bus arrival and now (a.k.a. waiting time)
    h = h - *MAIN_getCurrentHour();
    m = m - *MAIN_getCurrentMinute();
    
    // Adjust the hour wait if the result is negative
    if( h < 0 )
        h += 24;
    
    // Adjust the minutes wait (if waiting time is more than an hour, then add 60 minutes)
    // Only minutes are shown in the display
    m += h*60;

    // Store waiting time
    wTime[p] = max(0,m);
    wBusLine[p] = busLine;

    // Store when this bus stop was last updated
    lastUpdated = millis();
}


/*
 * Reset waiting times
 */
void BusStop::resetTimes(){
  	for( uint8_t i=0 ; i<WAITTIMES_N ; i++ ){
    		wTime[i] = 0;
        wBusLine[i] = 0;
  	}
}

