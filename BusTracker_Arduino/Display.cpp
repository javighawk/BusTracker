#include "Display.h"
#include "Adafruit_LEDBackpack.h"
#include "Adafruit_GFX.h"
#include <Wire.h>

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
void DISP_showWaitTime(BusStop bStop){

    // Get the closest waiting timt
    int waitTime = bStop.BSTOP_getWaitTime(0);

    // Get the bus line
    int busLine = bStop.BSTOP_getBusLine();
    
    if( waitTime == -1 ){
        matrix7.printError();
    } else {
        matrix7.print(waitTime);

        if( busLine < 10 ){
            matrix7.writeDigitNum(0, busLine, false);
        }else{
            matrix7.writeDigitNum(0, int(busLine/10), false);
            matrix7.writeDigitNum(1, busLine % 10, false);
        }
    }
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
    
    matrix7.writeDigitRaw(0, 0b00111001);     // C
    matrix7.writeDigitRaw(1, 0b01011100);     // o
    matrix7.writeDigitRaw(3, 0b01010100);     // n
    matrix7.writeDigitRaw(4, 0b01010100);     // n
    matrix7.writeDisplay();    
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
