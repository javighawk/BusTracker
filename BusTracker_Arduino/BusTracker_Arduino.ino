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
BusStop farmersMarket_DT_9(farmersMarket_DT_ID.toInt(), 9);
BusStop farmersMarket_RD_9(farmersMarket_RD_ID.toInt(), 9);
BusStop avenueC20th_DT_2(avenueC20th_DT_ID.toInt(), 2);
BusStop avenueC20th_PH_2(avenueC20th_PH_ID.toInt(), 2);
BusStop avenueC20th_DT_10(avenueC20th_DT_ID.toInt(), 10);
BusStop avenueC20th_PH_10(avenueC20th_PH_ID.toInt(), 10);

/*
 * Current time & date extracted from website
 */
tm currentTimeDate;

extern int INET_initClientSocket();
extern String INET_getWebsite(String stop_id, bool closeConnection);
extern void SC_retrieveData(String sourcecode, BusStop *bStop, tm *currentTimeDate);

/*
 * Setup function
 */
void setup(){

    // Initialize Serial
    Serial.begin(9600);

    // Clear display
    DISP_init();
}

/*
 * Main Loop
 */
void loop(){

        // Open the socket
        if( INET_initClientSocket() != 0 ){

            // Delay 5 seconds and restart display
            delay(5000);
            DISP_clear();
            return;
        }
    
        // Get the content of the website for the stops needed
        //String farmersMarket_DT_webContent = INET_getWebsite(farmersMarket_DT_ID, 1);
        String avenueC20th_DT_webContent = INET_getWebsite(avenueC20th_DT_ID, 1);
    
        // Close socket
    
        // Get data from the website
        SC_retrieveData(avenueC20th_DT_webContent, &avenueC20th_DT_2, &currentTimeDate);
        SC_retrieveData(avenueC20th_DT_webContent, &avenueC20th_DT_10, &currentTimeDate);
    
        // Display wait time
        DISP_showWaitTime(avenueC20th_DT_10);
    
        // Reset wait times
        avenueC20th_DT_2.BSTOP_setEmptyTime();
        avenueC20th_DT_10.BSTOP_setEmptyTime();
}

/*
 * Getter
 */
tm getCurrentTimeDate(){
    return currentTimeDate;
}
