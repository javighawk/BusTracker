#include "BusStop.hpp"
#include <stdlib.h>

extern tm INET_getCurrentTimeDate();

/*
 * Constructor
 */
BusStop::BusStop(int id){
	BSTOP_id = id;
	BSTOP_setEmptyTime();
}

/*
 * Returns the waiting time for the bus
 *
 * @param p The number of the bus to wait (0 = most recent)
 * @param real 1 if real wait time. 0 otherwise.
 *
 * @return The waiting time in minutes. -1 if error occurred.
 */
int BusStop::BSTOP_getWaitTime(int p, int real){

	if( p >= WAITTIMES_N )
		return -1;

	time_t endTime;
	tm originTime = INET_getCurrentTimeDate();

	if( real == 1 )
		endTime = mktime(&BSTOP_sTime[p].realTime);
	else if( real == 0 )
		endTime = mktime(&BSTOP_sTime[p].schedTime);

	double diff = difftime(endTime, mktime(&originTime));

	return (int)(diff/60);
}

/*
 * Setter
 */
void BusStop::BSTOP_setSTime(string st, int p){

	if( p >= WAITTIMES_N )
		return;

	// Get current date and erase time
	tm date = INET_getCurrentTimeDate();
	date.tm_hour = 0;
	date.tm_min = 0;

	// Assign the date to the stop time
	BSTOP_sTime[p].schedTime = date;
	BSTOP_sTime[p].realTime = date;

	// Get Scheduled time
	BSTOP_sTime[p].schedTime.tm_hour = atoi(st.substr(0,2).c_str());
	BSTOP_sTime[p].schedTime.tm_min = atoi(st.substr(3,2).c_str());
	if( st[5] == 'p' ) BSTOP_sTime[p].schedTime.tm_hour += 12;
	if( BSTOP_sTime[p].schedTime.tm_hour == 0 ) BSTOP_sTime[p].schedTime.tm_mday += 1;

	// Get Real time
	BSTOP_sTime[p].realTime.tm_hour = atoi(st.substr(10,2).c_str());
	BSTOP_sTime[p].realTime.tm_min = atoi(st.substr(13,2).c_str());
	if( st[15] == 'p' ) BSTOP_sTime[p].realTime.tm_hour += 12;
	if( BSTOP_sTime[p].realTime.tm_hour == 0 ) BSTOP_sTime[p].realTime.tm_mday += 1;

	// Give format
	mktime(&BSTOP_sTime[p].schedTime);
	mktime(&BSTOP_sTime[p].realTime);
}

/*
 * Set error time
 */
void BusStop::BSTOP_setEmptyTime(){

	for( int i=0 ; i<WAITTIMES_N ; i++ ){
		BSTOP_sTime[i].realTime.tm_hour = -1;
		BSTOP_sTime[i].realTime.tm_min = -1;
		BSTOP_sTime[i].schedTime.tm_hour = -1;
		BSTOP_sTime[i].schedTime.tm_min = -1;
	}
}
