/*
  Parsing Manager.
  
  This file contains all functions that manipulate strings.
  Their objective is to find desired data in the string that is
  being downloaded by InetManager.cpp

  Before starting to parse data, we need to associate this file
  with a BusStop object (the object corresponding to the bus stop
  we are looking at). This object is being pointed by *bStop, and it
  has to be set with the function STR_setBusStop() always before parsing

  Author: Javier Garcia  
 */
 
#include "ParsingManager.h"

/* Strings to look for in order to find the info wanted */
#define STR_TOFINDTIME            "mTime\""
#define STR_TOFINDBUSLINE         "\"><td"
#define STR_TOFINDARRIVALTIME     "\"top\">"

/* Length of strings */
#define STR_TOFINDTIMELEN             6
#define STR_TOFINDBUSLINELEN          5
#define STR_TOFINDARRIVALTIMELEN      6
#define STR_TIMELEN                   6

/* String offset: Distance from beginning of string to start of data of interest */
#define STR_TOFINDTIMEOFFSET          STR_TOFINDTIMELEN + 8
#define STR_TOFINDBUSLINEOFFSET       STR_TOFINDBUSLINELEN + 27
#define STR_TOFINDARRIVALTIMEOFFSET   STR_TOFINDARRIVALTIMELEN
#define STR_TIMELEN                   6

/*
 * Extern functions
 */
void MAIN_setCurrentHour(int8_t hour);
void MAIN_setCurrentMinute(int8_t min);



/* Pointer to the bus stop for which we are looking the information */
BusStop *bStop;

/* Pair of bus line & waiting time that it's being looked for */
uint8_t busLine = 0;
uint8_t waitTime = 0;


/*
 * Find the valuable data. InetManager passes this function chunks of   
 * 50 characters and depending on what it finds, it will behave 
 * one way or another.
 * 
 * The order in which data is being looked for is as follows:
 * 1. Current time. We decide to use the website's current time  
 *    because all waiting times are calculated having this time  
 *    as reference.
 * 2. Bus line. This is the next bus to come. If we have already  
 *    retrieved the information about the upcoming bus, then we  
 *    will look for the following bus to come, and so on until  
 *    there's no information on further buses.
 * 3. Arrival time of the bus line found at point 2. If this  
 *    arrival time is "NA", it means that there's no real  
 *    data for this bus, so we disregard it.
 *    
 * The procedure to run in each of the previous points is the following:
 * - If the key string (defined at the top of this file as const String)
 *   is at the beginning (i.e. indexOf() == 0) then proceed to extract
 *   the information.
 * - If the key string is in the string passed as parameter, but not
 *   at the beginning (i.e. indexOf() > 0), erase whatever is before the
 *   key string and let InetManager complete the rest of the chunk.
 * - If the key string is not detected (i.e. indexOf() == -1), go to
 *   the next point. If we are already in the last point (point 3),
 *   erase the whole chunk and let InetManager give us a new one.
 * 
 * param source Pointer to the string to be inspected. This string
 *              should always have the same length
 */
void STR_findData(String *source){
  
    int idx;
    int len = source->length();

    // Look first for the current time
    if( (idx = source->indexOf(STR_TOFINDTIME)) != -1 ){

        // If the text of interest is at the beginning of the String, retrieve useful info and reset String
        if( idx == 0 ){
            STR_retrieveCurrentTime(source->substring(STR_TOFINDTIMEOFFSET, STR_TOFINDTIMEOFFSET + STR_TIMELEN));
            *source = String();

        // Otherwise, remove what's before the text of interest.
        } else {
            *source = source->substring(idx,len);
        }

    // Look for the bus line
    } else if( (idx = source->indexOf(STR_TOFINDBUSLINE)) != -1 ){
        if( idx == 0 ){
            busLine = (uint8_t) source->substring(STR_TOFINDBUSLINEOFFSET, STR_TOFINDBUSLINEOFFSET + 2).toInt();
            *source = String();
        } else {
            *source = source->substring(idx,len);
        }


    // Look for the wait time
    } else if( (idx = source->indexOf(STR_TOFINDARRIVALTIME)) != -1 ){
        if( idx == 0 ){
            String waitData = source->substring(STR_TOFINDARRIVALTIMEOFFSET, STR_TOFINDARRIVALTIMEOFFSET + STR_TIMELEN);
            
            // If waittime is not available, continue
            if( waitData.substring(0,2) != "NA" )
                bStop->setWTime(waitData, busLine);
                
            *source = String();
        } else {
            *source = source->substring(idx,len);
        }

    // Otherwise erase what's not useful information (keep some of the last characters
    // lest we erase part of a string that we are looking for)
    } else {
        *source = source->substring(len - STR_TOFINDTIMELEN,len);
    }
}


/*
 * Get date from text
 *
 * @param time The time as a string
 */
void STR_retrieveCurrentTime(String time){

    int8_t h, m;

    // Parse current hour and minute
  	h = (int8_t) time.substring(0,2).toInt();
  	m = (int8_t) time.substring(3,6).toInt();

    // Change hour to 24h format
  	if( time[5] == 'p' && h != 12 )
  		  h += 12;
    if( time[5] == 'a' && h == 12 )
        h = 0;

    // Set new current time
    MAIN_setCurrentHour(h);
    MAIN_setCurrentMinute(m);
}


/*
 * Store on this file the Bus Stop of interest
 */
void STR_setBusStop(BusStop *b){
    bStop = b;
}
