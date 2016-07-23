#include "BusStop.h"
#include "Display.h"
#include "ParsingManager.h"

#define INT0_PIN             2          /* Interruption pins */
#define INT1_PIN             4
#define ANLG_PIN             A0         /* Analog pin. Temporary */

#define AveM_22nd_LKVW_ID    "3968"       /* Bus stops ID */

#define PERIOD_MS            10000      /* Period of website reading in milliseconds */

#define ON true


/*
 * Declare functions
 */
void MAIN_retrieveBusStopInformation(BusStop *bs);
extern int INET_getBusStopWebsite(char *busStopID);
extern int INET_init();


/*
 * Bus stop objects
 */
BusStop AveM_22nd_LKVW(AveM_22nd_LKVW_ID);


/*
 * Current time & date extracted from website
 */
int8_t currentHour, currentMin;

/*
 * Index of the bus stop currently being displayed
 */
uint8_t currentBusStop;

/*
 * Wait time currently being displayed
 */
uint8_t currentWaitTime;

/*
 * Time since last reading in millisceonds
 */
long lastTime = 0;

/*
 * Time since colon was last changed
 */
long lastTimeColon = 0;

/*
 * Array of BusStop pointers cointaining pointers to all BusStops to Downtown
 */
BusStop *allStops_DT[1] = {&AveM_22nd_LKVW};

/*
 * Number of all stops to Downtown
 */
int nAllStops_DT = sizeof(allStops_DT)/sizeof(allStops_DT[0]);

/*
 * Setup function
 */
void setup(){

    // Initialize Serial
    Serial.begin(115200);

    // Initialize displayed bus stop and wait time indexes
    currentBusStop = 0;
    currentWaitTime = 0;

    // Setup interrupts
    // If using Arduino, trigger interrupt with RISING instead of CHANGE
    attachInterrupt(INT0_PIN, int0_handler, CHANGE);
    attachInterrupt(INT1_PIN, int1_handler, CHANGE);
    
    // Display Init
    DISP_init();

    // Connect to Internet
    INET_init();
}

/*
 * Main Loop. Each bus stop information is retrieve one after another
 */
void loop(){

    if( millis() - lastTime > PERIOD_MS ){
        // Ave M & 22nd
        MAIN_retrieveBusStopInformation(&AveM_22nd_LKVW);
        lastTime = millis();
    }

    // TEMPORARY. ERASE WHEN INTERRUPTS WORK
    int anlg_read = analogRead(ANLG_PIN);
    if( anlg_read < 1024/3 ) currentWaitTime = 0;
    else if( anlg_read < 2*1024/3 ) currentWaitTime = 1;
    else currentWaitTime = 2;

    // Switch colon
    if( millis() - lastTimeColon > 500 ){
        DISP_switchColon();
        lastTimeColon = millis();
    }

    DISP_showWaitTime(allStops_DT[currentBusStop], currentWaitTime);
}


/*
 * Retrieves information from the website, stores it and displays it
 * 
 * @param bs1 BusStop object where the info will be stored. If this is null, then all BusStop objects will be considered
 * @param bs2 Optional additional BusStop if more than one line's info is wanted
 */
void MAIN_retrieveBusStopInformation(BusStop *bs){

    bs->resetTimes();                                                    // Reset wait times
    STR_setBusStop(bs);                                                  // Set the Bus Stop we are interested in
    INET_getBusStopWebsite(bs->getId());                                 // Get the HTML code of the website and collect data
}


/*
 * Handler for GPIO interrupt 0
 * Changes the displayed bus stop
 */
void int0_handler(){

    // Update bus stop index
    currentBusStop = (currentBusStop + 1) % nAllStops_DT; 

    // Display most recent wait time
    currentWaitTime = 0;

    // Update display
    DISP_showWaitTime(allStops_DT[currentBusStop], currentWaitTime);
}


/*
 * Handler for GPIO interrupt 1
 * Changes the displayed wait time index
 */
void int1_handler(){

    // Update bus stop index
    currentWaitTime = (currentWaitTime + 1) % WAITTIMES_N; 

    // Update display
    DISP_showWaitTime(allStops_DT[currentBusStop], currentWaitTime);
}


/*
 * Setters
 */
void MAIN_setCurrentHour(int8_t hour){ currentHour = hour; }
void MAIN_setCurrentMinute(int8_t min){ currentMin = min; }

/*
 * 
 */
int8_t *MAIN_getCurrentHour(){ return &currentHour; }
int8_t *MAIN_getCurrentMinute(){ return &currentMin; }

