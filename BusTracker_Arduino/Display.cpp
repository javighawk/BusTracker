#include "Display.h"
#include "Adafruit_LEDBackpack.h"
#include "Adafruit_GFX.h"
#include <Wire.h>

/*
  This class manages the BCD display. Wraps up the functions
  needed to display the bus waiting time and the bus line.
 */


/*
 * 7-segment object
 */
Adafruit_7segment matrix7 = Adafruit_7segment();

/*
 * Initialize display
 */
void DISP_init(){

    // Initialize display with I2C address = 0x70
    matrix7.begin(0x70);

    matrix7.writeDigitRaw(0,0b00110000);     // I
    matrix7.writeDigitRaw(1,0b01010100);     // n
    matrix7.writeDigitRaw(3,0b00010000);     // i
    matrix7.writeDigitRaw(4,0b01111000);     // t
    matrix7.writeDisplay();
}


/*
 * Shows the waiting time in the display
 * 
 * @param bStop The display will show the waiting time for this bus stop
 */
void DISP_showWaitTime(BusStop bStop, int waitTimeIndex){
    
    // Get the closest waiting timt
    int waitTime = bStop.BSTOP_getWaitTime(waitTimeIndex);

    // Get the bus line
    int busLine = bStop.BSTOP_getBusLine();

    // Show wait time (only if less than 100 minutes)
    if( waitTime == -1 || waitTime >= 100 )
        DISP_clear();
    else matrix7.print(waitTime);

    // Show bus line
    if( busLine < 10 ){
        matrix7.writeDigitNum(0, busLine, false);
    }else{
        matrix7.writeDigitNum(0, int(busLine/10), false);
        matrix7.writeDigitNum(1, busLine % 10, false);
    }

    // Show the index of the wait time in the semicolon on the left
    matrix7.writeDigitRaw(2, waitTimeIndex << 2);

    // If the information is out of date, show semicolons
    long t = millis() - bStop.BSTOP_getLastUpdated();
    if( t > LASTUPDATE_THRESHOLD )
        matrix7.writeDigitRaw(2, 0x02);
    
    matrix7.writeDisplay();    
}


/*
 * Displays timeout error
 * 
 */
void DISP_showTimeOut(){
    
    matrix7.writeDigitRaw(0, 0b01111000);     // t 
    matrix7.writeDigitRaw(1, 0b01011100);     // o
    matrix7.writeDigitRaw(3, 0b00011100);     // u
    matrix7.writeDigitRaw(4, 0b01111000);     // t
    matrix7.writeDisplay();    
}


/*
 * Displays error
 */
void DISP_showError(){
    
    matrix7.writeDigitRaw(0, 0b01111001);     // E 
    matrix7.writeDigitRaw(1, 0b01010000);     // r
    matrix7.writeDigitRaw(3, 0b01010000);     // r
    matrix7.writeDigitRaw(4, 0);
    matrix7.writeDisplay();    
}


/*
 * Displays error with Ethernet port initialization
 */
void DISP_showEthernetError(){
    
    matrix7.writeDigitRaw(0, 0b01111001);     // E 
    matrix7.writeDigitRaw(1, 0b01111000);     // t
    matrix7.writeDigitRaw(3, 0b01110100);     // h
    matrix7.writeDigitRaw(4, 0);
    matrix7.writeDisplay();    
}


/*
 * Displays error with Ethernet port initialization
 */
void DISP_showConnectionError(){
    
    matrix7.writeDigitRaw(2, 0x10);
    matrix7.writeDisplay();
    delay(100);
}


/*
 * Displays error with Ethernet port initialization
 */
void DISP_clear(){
    
    matrix7.writeDigitRaw(0,0);
    matrix7.writeDigitRaw(1,0);
    matrix7.writeDigitRaw(2,0);
    matrix7.writeDigitRaw(3,0);
    matrix7.writeDigitRaw(4,0);
    matrix7.writeDisplay(); 
}
