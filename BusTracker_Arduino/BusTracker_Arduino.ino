#include "BusStop.h"
#include "Display.h"
#include <Ethernet.h>
#include "Adafruit_LEDBackpack.h"
#include "Adafruit_GFX.h"
#include <Wire.h>

#define CONNECT_PERIOD_MSEC  30000

/*
 * Bus stop IDs
 */
const String farmersMarket_DT_ID = "5755";
const String farmersMarket_RD_ID = "5758";
const String avenueC20th_DT_ID = "3088";
const String avenueC20th_PH_ID = "3083";

/*
 * BusStop objects
 */
BusStop farmersMarket_DT(farmersMarket_DT_ID.toInt());
BusStop farmersMarket_RD(farmersMarket_DT_ID.toInt());
BusStop avenueC20th_DT(avenueC20th_DT_ID.toInt());
BusStop avenueC20th_PH(avenueC20th_PH_ID.toInt());

/*
 * Current time & date extracted from website
 */
tm currentTimeDate;

/*
 * 7-segment display manager
 */
Display disp;
 

extern int INET_initClientSocket();
extern String INET_getWebsite(String stop_id);
extern void SC_retrieveData(String sourcecode, BusStop *bStop, tm *currentTimeDate);

/*
 * Setup function
 */
void setup(){

    // Initialize Serial
    Serial.begin(9600);

    // Clear display
    disp = Display();
}

/*
 * Main Loop
 */
void loop(){

        // Open the socket
        if( INET_initClientSocket() != 0 ){
          // Need to implement timeout case
          return;
        }
    
        // Get the content of the website
        String webCurrentContent = INET_getWebsite(farmersMarket_DT_ID);
    
        // Close socket
    
        // Get data from the website
        SC_retrieveData(webCurrentContent, &farmersMarket_DT, &currentTimeDate);
    
        // Get waiting time
        int waitTime[3];
        waitTime[0] = farmersMarket_DT.BSTOP_getWaitTime(0);
        waitTime[1] = farmersMarket_DT.BSTOP_getWaitTime(1);
        waitTime[2] = farmersMarket_DT.BSTOP_getWaitTime(2);

        disp.showWaitTime(farmersMarket_DT.BSTOP_getWaitTime(0));
    
        // Reset wait times
        farmersMarket_DT.BSTOP_setEmptyTime();
    
        // Delay
        delay(CONNECT_PERIOD_MSEC);
}

/*
 * Getter
 */
tm getCurrentTimeDate(){
    return currentTimeDate;
}
