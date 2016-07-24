#include "BusStop.h"
#include "Display.h"
#include "ParsingManager.h"
#include <avr/wdt.h>

/* Defines */
#define ANLG_PIN              A0          // Analog pin
#define AveM_22nd_DT_ID       "3968"      // Bus Stop ID (Ave M & 22nd St, headed to downtown)
#define PERIOD_MS             10000       // Period between website reading
#define WATCHDOG_PERIOD       WDTO_8S     // Watchdog reset period (8 seconds)

/*
 * Declare functions
 */
void MAIN_retrieveBusStopInformation(BusStop *bs);
extern int INET_getBusStopWebsite(char *busStopID);
extern int INET_init();


/* Bus stop objects */
BusStop AveM_22nd_DT(AveM_22nd_DT_ID);


/* Current time & date extracted from website */
int8_t currentHour, currentMin;

/* Index of the bus stop currently being displayed */
uint8_t currentBusStop;

/* Wait time currently being displayed */
uint8_t currentWaitTime;

/* Time since last reading in millisceonds */
long lastTime = 0;

/* Time since colon was last changed */
long lastTimeColon = 0;

/* Array of BusStop pointers cointaining pointers to all BusStops to Downtown */
BusStop *allStops_DT[1] = {&AveM_22nd_DT};

/* Number of all stops to Downtown */
int nAllStops_DT = sizeof(allStops_DT)/sizeof(allStops_DT[0]);

/*
 * Setup function
 */
void setup(){

    // Initialize displayed bus stop and wait time indexes
    currentBusStop = 0;
    currentWaitTime = 0;
    
    // Display Init
    DISP_init();

    // Connect to Internet
    while( INET_init() );

    // Enable watchdog
    wdt_enable(WATCHDOG_PERIOD);
}

/*
 * Main Loop. Each bus stop information is retrieve one after another
 */
void loop(){
    // Retrieve updated bus stop information each PERIOD_MS milliseconds
    if( millis() - lastTime > PERIOD_MS ){
        MAIN_retrieveBusStopInformation(&AveM_22nd_DT);
        lastTime = millis();
    }

    // Update the waiting time/bus stop to display by reading the variable resistor
    int anlg_read = analogRead(ANLG_PIN);
    currentWaitTime = int(anlg_read / (double(1024)/WAITTIMES_N));

    // Switch colon: This will show that the program is still running.
    if( millis() - lastTimeColon > 500 ){
        DISP_switchColon();
        lastTimeColon = millis();
    }

    // Show user-selected bus stop in the display
    DISP_showWaitTime(allStops_DT[currentBusStop], currentWaitTime);

    // Feed watchdog timer
    wdt_reset();
}


/*
 * Retrieves information from the website, stores it and displays it
 * 
 * @param bs1 BusStop object where the info will be stored. If this is null, then all BusStop objects will be considered
 * @param bs2 Optional additional BusStop if more than one line's info is wanted
 */
void MAIN_retrieveBusStopInformation(BusStop *bs){
    // Reset wait times
    bs->resetTimes();

    // Set the Bus Stop we are interested in
    STR_setBusStop(bs);

    // Get the HTML code of the website and collect data
    INET_getBusStopWebsite(bs->getId());
}


/*
 * Setters
 */
void MAIN_setCurrentHour(int8_t hour){ currentHour = hour; }
void MAIN_setCurrentMinute(int8_t min){ currentMin = min; }

/*
 * Getters
 */
int8_t *MAIN_getCurrentHour(){ return &currentHour; }
int8_t *MAIN_getCurrentMinute(){ return &currentMin; }

