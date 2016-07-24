#include "BusStop.h"
#include "Display.h"
#include "ParsingManager.h"
#include <avr/wdt.h>

/* Defines */
#define INT0_PIN              2           // Interrupt pin
#define INT_PERIOD_MS         200         // Minimum time between interrupts in milliseconds (used to avoid interrupt bouncing)
#define AveM_22nd_DT_ID       "3968"      // Bus Stop ID (Ave M & 22nd St, headed to downtown)
#define AveM_22nd_FDT_ID      "3017"      // Bus Stop ID (Ave M & 22nd St, headed to confed. mall)
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
BusStop AveM_22nd_FDT(AveM_22nd_FDT_ID);


/* Current time & date extracted from website */
int8_t currentHour, currentMin;

/* Index of the bus stop currently being displayed */
uint8_t currentBusStop;

/* Wait time currently being displayed */
uint8_t currentWaitTime;

/* Wait time currently being displayed */
boolean currentDirectionDT = true;

/* Time since last reading in millisceonds */
unsigned long lastTime = 0;

/* Variable to avoud interrupt bouncing */
unsigned long lastTimeInterrupt = 0;

/* Array of BusStop pointers cointaining pointers to all BusStops TO Downtown */
BusStop *allStops_DT[2] = {&AveM_22nd_DT};

/* Array of BusStop pointers cointaining pointers to all BusStops FROM Downtown */
BusStop *allStops_FDT[2] = {&AveM_22nd_FDT};

/* Number of all stops TO Downtown */
int nAllStops_DT = sizeof(allStops_DT)/sizeof(allStops_DT[0]);

/* Number of all stops FROM Downtown */
int nAllStops_FDT = sizeof(allStops_DT)/sizeof(allStops_DT[0]);

/* Time since colon was last changed */
long lastTimeColon = 0;


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

    // Setup interrupts
    attachInterrupt(digitalPinToInterrupt(INT0_PIN), int0_handler, RISING);

    // Enable watchdog
    wdt_enable(WATCHDOG_PERIOD);
}

/*
 * Main Loop. Each bus stop information is retrieve one after another
 */
void loop(){
    // Retrieve updated bus stop information each PERIOD_MS milliseconds
    if( millis() - lastTime > PERIOD_MS ){
        for( int i=0 ; i<nAllStops_DT ; i++ ) 
            MAIN_retrieveBusStopInformation(allStops_DT[i]);
        lastTime = millis();
    }

    // Switch colon
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
 * Handler for GPIO interrupt
 * Changes the displayed wait time index
 */
void int0_handler(){
    // Check time from last interrupt
    unsigned long nowTime = millis();
    if( millis() - lastTimeInterrupt <= INT_PERIOD_MS )
        return;

    // Update bus stop index
    currentWaitTime = (currentWaitTime + 1) % WAITTIMES_N; 

    // Update last time interrupt was executed
    lastTimeInterrupt = nowTime;
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

