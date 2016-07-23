#include "ParsingManager.h"

#define str_timeLength                  6 

/*
 * Extern functions
 */
void MAIN_setCurrentHour(int8_t hour);
void MAIN_setCurrentMinute(int8_t min);

/*
 * String to look for in order to find the info wanted
 */
const String str_toFindTime         = "FromTime\" value=\"";
const String str_toFindBusLine      = "class=\"text\">";
const String str_toFindArrivalTime  = "\"top\">";

/*
 * Pointer to the bus stop for which we are looking the information
 */
BusStop *bStop;

/*
 * Pair of bus line & waiting time that it's being looked for
 */
uint8_t busLine = 0;
uint8_t waitTime = 0;


/*
 * Finds valuable data in the string passed as a parameter.
 * Modifies the string leaving only potential valuable data
 * and erasing non-valuable data and valuable data found.
 * 
 * param source Pointer to the string to be inspected
 * 
 */
void STR_findData(String *source){
  
    int idx;
    int len = source->length();

    // Look first for the current time
    if( (idx = source->indexOf(str_toFindTime)) != -1 ){

        // If the text of interest is at the beginning of the String, retrieve useful info and reset String
        if( idx == 0 ){
            STR_retrieveCurrentTime(source->substring(str_toFindTime.length(), str_toFindTime.length() + str_timeLength));
            *source = String();

        // Otherwise, remove what's before the text of interest.
        } else {
            *source = source->substring(idx,len);
        }

    // Look for the bus line
    } else if( (idx = source->indexOf(str_toFindBusLine)) != -1 ){
        if( idx == 0 ){
            busLine = (uint8_t) source->substring(str_toFindBusLine.length(), str_toFindBusLine.length() + 2).toInt();
            *source = String();
        } else {
            *source = source->substring(idx,len);
        }


    // Look for the wait time
    } else if( (idx = source->indexOf(str_toFindArrivalTime)) != -1 ){
        if( idx == 0 ){
            String waitData = source->substring(str_toFindArrivalTime.length(), str_toFindArrivalTime.length() + str_timeLength);
            
            // If waittime is not available, continue
            if( waitData.substring(0,2) != "NA" )
                bStop->setWTime(waitData, busLine);
                
            *source = String();
        } else {
            *source = source->substring(idx,len);
        }

    // Otherwise erase what's not useful information
    } else {
        *source = source->substring(len - str_toFindTime.length(),len);
    }
}


/*
 * Gets date from text
 *
 * @param time The time in string format
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
 * Set the Bus Stop of interest
 */
void STR_setBusStop(BusStop *b){
    bStop = b;
}
