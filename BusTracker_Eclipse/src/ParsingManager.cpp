#include <stdlib.h>
#include <stdio.h>
#include <string>
#include <time.h>
#include <iostream>
#include "BusStop.hpp"

using namespace std;

/*
 * String to look for in order to find the date
 */
string str_toFindDate = "name=\"Date\" value=\"";

/*
 * String to look for in order to find the current time
 */
string str_toFindTime = "name=\"FromTime\" value=\"";

/*
 * String to look for in order to find the scheduled/real time
 */
string str_toFindWaitTime = "showVeh(busLat, busLon, routeNum, routeName, '";


/*
 * Looks for a desired string in a given text (first appearance).
 *
 * @param source Pointer to the original text
 * @param dest Pointer to the destination string
 * @param lookfor The string used to look for the desired piece
 * @param length Length of the text to be extracted
 * @param erase 1 if anything before the found text needs to be removed. 0 otherwise.
 *
 * @return 1 if successful. 0 otherwise
 */
int SC_getString(string *source, string *dest, string lookfor, int length, int erase){

	int pos;

	// Make a copy to not modify the original
	string sourcecopy = *source;
	pos = sourcecopy.find(lookfor);

	// Check whether there has been success
	if( pos == int(string::npos) )
		return( 0 );

	sourcecopy.erase(0, pos + lookfor.length());
	*dest = sourcecopy.substr(0,length);

	if( erase ){
		*source = sourcecopy;
	}

	return 1;
}

/*
 * Gets date from text
 *
 * @param *t Pointer to the time struct that will contain the data
 * @param date The date in string format
 * @param time The time in string format
 */
void SC_retrieveDateTime(tm *t, string date, string time){
	t->tm_mon= atoi(date.substr(0,2).c_str());
	t->tm_mday = atoi(date.substr(3,2).c_str());
	t->tm_year = atoi(date.substr(6,4).c_str());
	t->tm_hour = atoi(time.substr(0,2).c_str());
	t->tm_min = atoi(time.substr(3,2).c_str());

	if( time[5] == 'p' )
		t->tm_hour += 12;
}


/*
 * Gets the useful data from the sourcecode.
 * Must look first for Date, then Time and then Wait times.
 *
 * @param sourcecode The source code from the website
 * @param bStop Pointer to the BusStop object that will contain the wait times
 */
void SC_retrieveData(string sourcecode, BusStop *bStop, tm *currentTimeDate){

	string date, time;
	string waitTime [WAITTIMES_N];

	// Look for date and give format
	if( SC_getString(&sourcecode, &date, str_toFindDate, 10, 1) == 0 ) return;

	// Look for current time
	if( SC_getString(&sourcecode, &time, str_toFindTime, 6, 1) == 0 ) return;

	// Parse date and time data
	SC_retrieveDateTime(currentTimeDate, date, time);

	// Look for real and scheduled waiting times
	for( int i=0 ; i<WAITTIMES_N ; i++ ){
		if( SC_getString(&sourcecode, &waitTime[i], str_toFindWaitTime, 16, 1) == 0 ){

			// No wait time info. Set the times in the BusStop object as errors
			break;
		}
		bStop->BSTOP_setSTime(waitTime[i],i);
	}
}
