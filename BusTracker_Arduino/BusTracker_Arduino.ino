#include "BusStop.h"
#include "Display.h"
#include <Ethernet.h>
#include "Adafruit_LEDBackpack.h"
#include "Adafruit_GFX.h"
#include <Wire.h>

#define INT0_PIN             2          /* Interruption pins */
#define INT1_PIN             3

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

/*
 * Declare functions
 */
void MAIN_retrieveBusStopInformation(String busStopID, BusStop *bs1 = NULL, BusStop *bs2 = NULL);
extern String INET_getBusStopWebsite(String busStopID);
extern void SC_retrieveData(String sourcecode, BusStop *bStop, tm *currentTimeDate);

/*
 * Index of the bus stop currently being displayed
 */
int currentBusStop;

/*
 * Wait time currently being displayed
 */
int currentWaitTime;

/*
 * Array of BusStop pointers cointaining pointers to all BusStops to Downtown
 */
BusStop *allStops_DT[3] = { &farmersMarket_DT_9,
                            &avenueC20th_DT_2,
                            &avenueC20th_DT_10 };

/*
 * Number of all stops to Downtown
 */
int nAllStops_DT = sizeof(allStops_DT)/sizeof(allStops_DT[0]);

/*
 * Setup function
 */
void setup(){

    // Initialize Serial
    Serial.begin(9600);

    // Initialize displayed bus stop and wait time indexes
    currentBusStop = 0;
    currentWaitTime = 0;

    // Setup interrupts
    // If using Arduino, trigger interrupt with RISING instead of CHANGE
    attachInterrupt(INT0_PIN, int0_handler, CHANGE);
    attachInterrupt(INT1_PIN, int1_handler, CHANGE);
    
    // Display Init
    DISP_init();
}

/*
 * Main Loop. Each bus stop information is retrieve one after another
 */
void loop(){

    // Farmer Market stop
    MAIN_retrieveBusStopInformation(farmersMarket_DT_ID, &farmersMarket_DT_9);

    // Ave C / 20th
    MAIN_retrieveBusStopInformation(avenueC20th_DT_ID, &avenueC20th_DT_2, &avenueC20th_DT_10);
}


/*
 * Retrieves information from the website, stores it and displays it
 * 
 * @param busStopID String with the bus stop ID
 * @param bs1 BusStop object where the info will be stored. If this is null, then all BusStop objects will be considered
 * @param bs2 Optional additional BusStop if more than one line's info is wanted
 */
void MAIN_retrieveBusStopInformation(String busStopID, BusStop *bs1, BusStop *bs2){

    BusStop *array[nAllStops_DT];                                                   // Create array of pointers to BusStop objects
    if( bs1 == NULL ){                                                              // If the first BusStop* argument is null
        memcpy( array, allStops_DT, sizeof(allStops_DT) );                          // Then consider all BusStop objects. Duplicate arrays 
    } else { 
        array[0] = bs1;                                                             // Otherwise take BusStop objects given as arguments
        array[1] = bs2;
    }

    String webContent = INET_getBusStopWebsite(busStopID);                          // Get the HTML code of the website

    int arraySize = sizeof(array)/sizeof(array[0]);                                 // Get array size

    for( int i=0 ; i<arraySize ; i++ ){                                             // Iterate through all desired BusStop objects
        if( array[i] != NULL && webContent.indexOf("</html>") != -1 ){              // Check if the BusStop object is not NULL and whether we got full info from website. Otherwise do nothing
            array[i]->BSTOP_setEmptyTime();                                         // Reset wait times
            SC_retrieveData(webContent, array[i], &currentTimeDate);                // Parse data from the website
        }
    }
    
    DISP_showWaitTime(*allStops_DT[currentBusStop], currentWaitTime);               // Always Display waiting time at the end
}


/*
 * Handler for GPIO interrupt 0
 * Changes the displayed bus stop
 */
void int0_handler(){

    if( !digitalRead(INT0_PIN) ) return;                                // Only for Intel Galileo

    // Update bus stop index
    currentBusStop = (currentBusStop + 1) % nAllStops_DT; 

    // Display most recent wait time
    currentWaitTime = 0;

    // Update display
    DISP_showWaitTime(*allStops_DT[currentBusStop], currentWaitTime);
}


/*
 * Handler for GPIO interrupt 1
 * Changes the displayed wait time index
 */
void int1_handler(){

    if( !digitalRead(INT1_PIN) ) return;                                // Only for Intel Galileo

    // Update bus stop index
    currentWaitTime = (currentWaitTime + 1) % WAITTIMES_N; 

    // Update display
    DISP_showWaitTime(*allStops_DT[currentBusStop], currentWaitTime);
}


/*
 * Getter
 */
tm getCurrentTimeDate(){ return currentTimeDate; }


