#include "BusStop.h"
#include "Display.h"
#include "ParsingManager.h"
#include <avr/wdt.h>

/* Defines */
#define INT0_PIN              2           // Interrupt pin
#define INT1_PIN              4           // Fake interrupt pin
#define DIRECTION_PIN         6           // This pin determines if we are showing bus stops TO or FROM downtown.
#define GREENLED_PIN          8           // Pin for red led (showing direction = FROM downtown)
#define REDLED_PIN            9           // Pin for green led (showing direction = TO downtown)
#define INT_PERIOD_MS         200         // Minimum time between interrupts in milliseconds (used to avoid interrupt bouncing)
#define AveM_22nd_DT_ID       "3968"      // Bus Stop ID (Ave M & 22nd St, headed to downtown)
#define AveM_22nd_FDT_ID      "3017"      // Bus Stop ID (Ave M & 22nd St, headed to confed. mall)
#define PERIOD_MS             10000       // Period between website reading
#define WATCHDOG_PERIOD       WDTO_8S     // Watchdog reset period (8 seconds)

/* Declare functions */
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

/* Time since colon was last changed */
unsigned long lastTimeColon = 0;

/* Array of BusStop pointers cointaining pointers to all BusStops TO Downtown */
BusStop *allStops_DT[1] = {&AveM_22nd_DT};

/* Array of BusStop pointers cointaining pointers to all BusStops FROM Downtown */
BusStop *allStops_FDT[1] = {&AveM_22nd_FDT};

/* Number of all stops TO Downtown */
int nAllStops_DT = sizeof(allStops_DT)/sizeof(allStops_DT[0]);

/* Number of all stops FROM Downtown */
int nAllStops_FDT = sizeof(allStops_FDT)/sizeof(allStops_FDT[0]);


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

    // Setup output pins
    pinMode(REDLED_PIN, OUTPUT);
    pinMode(GREENLED_PIN, OUTPUT);

    // Set LEDs to initial direction
    digitalWrite(REDLED_PIN, !currentDirectionDT);
    digitalWrite(GREENLED_PIN, currentDirectionDT);

    // Enable watchdog
    wdt_enable(WATCHDOG_PERIOD);
}


/*
 * Loop function.
 */
void loop(){
    // Update the direction of interest (TO/FROM downtown)
    if( boolean(digitalRead(DIRECTION_PIN)) != currentDirectionDT ){
        // Shift current direction
        currentDirectionDT = !currentDirectionDT;
      
        // If direction has changed, force to download the new displayed bus stop
        lastTime = 0;

        // Set LEDs
        digitalWrite(REDLED_PIN, !currentDirectionDT);
        digitalWrite(GREENLED_PIN, currentDirectionDT);
    }

    // Trigger our fake interrupt if INT1_PIN is high
    if( digitalRead(INT1_PIN) )
        int1_handler();
    
    // Retrieve updated bus stop information each PERIOD_MS milliseconds.
    // Only the currently shown bus stop is updated
    if( millis() - lastTime > PERIOD_MS ){
        MAIN_retrieveBusStopInformation();
        lastTime = millis();
    }
    
    // Show user-selected bus stop in the display
    if( currentDirectionDT )
        DISP_showWaitTime(allStops_DT[currentBusStop], currentWaitTime);
    else
        DISP_showWaitTime(allStops_FDT[currentBusStop], currentWaitTime);

    // Switch colon
    if( millis() - lastTimeColon > 500 ){
        DISP_switchColon();
        lastTimeColon = millis();
    }

    // Feed watchdog timer
    wdt_reset();
}


/*
 * Retrieves information from the website, stores it and displays it
 */
void MAIN_retrieveBusStopInformation(){
    // Get the bus stop of interest
    BusStop *bs;
    if( currentDirectionDT )
        bs = allStops_DT[currentBusStop];
    else
        bs = allStops_FDT[currentBusStop];
    
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

    // Update waiting time index
    currentWaitTime = (currentWaitTime + 1) % WAITTIMES_N; 

    // Update last time interrupt was executed
    lastTimeInterrupt = nowTime;
}


/*
 * Handler for a fake interrupt
 * Changes the bus stop displayed
 */
void int1_handler(){
    // Check time from last interrupt
    unsigned long nowTime = millis();
    if( millis() - lastTimeInterrupt <= INT_PERIOD_MS )
        return;

    // Update bus stop index
    uint8_t nStops = (nAllStops_DT * currentDirectionDT) + (nAllStops_FDT * !currentDirectionDT);
    currentBusStop = (currentBusStop + 1) % nStops;
    if( nStops > 1 ){
        // Force to download new bus stop information (if there's more than 1 bus stop)
        lastTime = 0;
    }
    
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

