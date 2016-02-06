#include <time.h>
#include "BusStop.h"

using namespace std;

/*
 * String to look for in order to find the date
 */
String str_toFindDate = "name=\"Date\" value=\"";

/*
 * String to look for in order to find the current time
 */
String str_toFindTime = "name=\"FromTime\" value=\"";

/*
 * String to look for in order to find the scheduled/real time
 */
String str_toFindWaitTime = "showVeh(busLat, busLon, routeNum, routeName, '";

/*
 * String to look for in order to find the bus line
 */
String str_toFindBusLine = "><td valign=\"top\" class=\"text\">";


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
int SC_getString(String *source, String *dest, String lookfor, int length, int erase){

  	int pos;
  
  	// Make a copy to not modify the original
  	String sourcecopy = *source;
  	pos = sourcecopy.indexOf(lookfor);
  
  	// Check whether there has been success
  	if( pos == -1 )
  		  return( 0 );
  
  	sourcecopy = sourcecopy.substring(pos + lookfor.length(),sourcecopy.length());
  	*dest = sourcecopy.substring(0,length);
  
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
void SC_retrieveDateTime(tm *t, String date, String time){
  
  	t->tm_mon= date.substring(0,2).toInt();
  	t->tm_mday = date.substring(3,5).toInt();
  	t->tm_year = date.substring(6,10).toInt();
  	t->tm_hour = time.substring(0,2).toInt();
  	t->tm_min = time.substring(3,5).toInt();
  
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
void SC_retrieveData(String sourcecode, BusStop *bStop, tm *currentTimeDate){

  	String date, time;
  	String waitTime;
    String line;
    int i = 0;
  
  	// Look for date and give format
  	if( SC_getString(&sourcecode, &date, str_toFindDate, 10, 1) == 0 ) return;
  
  	// Look for current time
  	if( SC_getString(&sourcecode, &time, str_toFindTime, 6, 1) == 0 ) return;
  
  	// Parse date and time data
  	SC_retrieveDateTime(currentTimeDate, date, time);
  
  	// Look for real and scheduled waiting times
  	while( i<WAITTIMES_N ){
    		if( SC_getString(&sourcecode, &waitTime, str_toFindWaitTime, 16, 1) == 0 ) break;
        if( SC_getString(&sourcecode, &line, str_toFindBusLine, 2, 1) == 0 ) break;
        if( line.toInt() != bStop->BSTOP_getBusLine() ) continue;
  	    bStop->BSTOP_setSTime(waitTime,i);
        i++;
  	}
}
