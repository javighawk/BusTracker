/*
  This file contains functions that manage the BCD display. 
  in order to show the bus waiting time and the bus line.

  Author: Javier Garcia  
 */

#include "Display.h"
#include <Adafruit_GFX.h>
#include <Adafruit_LEDBackpack.h>
#include <Wire.h>

/* Number-code conversion */
const uint8_t numbersCode[] = {0b00111111,  // 0
                               0b00000110,  // 1
                               0b01011011,  // 2
                               0b01001111,  // 3
                               0b01100110,  // 4
                               0b01101101,  // 5
                               0b01111101,  // 6
                               0b00000111,  // 7
                               0b01111111,  // 8
                               0b01101111}; // 9

/* Register data */
uint8_t reg[5];


/* 7-segment object */
Adafruit_7segment matrix7 = Adafruit_7segment();


/*
 * Write on the display whatever is stored in the array "reg"
 */
void DISP_write(){
    for( uint8_t i=0 ; i<sizeof(reg) ; i++ )
        matrix7.writeDigitRaw(i,reg[i]);

    matrix7.writeDisplay();
}


/*
 * Get a number code
 * 
 * @param num The number to be coded
 * @return The 8-bit code to be written in the register to show the desired number
 */
uint8_t DISP_getNumberCode(uint8_t num){
    // Check if index is out of bounds
    if( num > 9 )
        return 0;

    // Return corresponding code
    return numbersCode[num];    
}


/*
 * Initialize display
 */
void DISP_init(){
    // Initialize display with I2C address = 0x70
    matrix7.begin(0x70);

    reg[0] = 0b00110000;     // I
    reg[1] = 0b01010100;     // n
    reg[2] = 0;
    reg[3] = 0b00010000;     // i
    reg[4] = 0b01111000;     // t
    DISP_write();
}


/*
 * Show the waiting time in the display
 * 
 * @param bStop The display will show the waiting time for this bus stop
 * @param waitTimeIndex Indicates which bus' waiting time to be displayed (0 = the very next bus' waiting time)
 */
void DISP_showWaitTime(BusStop *bStop, uint8_t waitTimeIndex){
    // Get the closest waiting timt
    uint8_t waitTime = bStop->getWTime(waitTimeIndex);

    // Get the bus line
    uint8_t busLine = bStop->getWBusLine(waitTimeIndex);

    // Show the index of the wait time in the semicolon on the left
    reg[2] = (reg[2] & 0xF3) | waitTimeIndex << 2;

    // If the information is out of date, show semicolons
    long t = millis() - bStop->getLastUpdated();
    if( t > LASTUPDATE_THRESHOLD ){
        reg[2] = (reg[2] & 0xFC)| 0x02;
    }

    // Show bus line
    if( busLine == 0 ){
        DISP_clear();
        return;
    }
    else if( busLine < 10 ){
        reg[0] = DISP_getNumberCode(busLine);
        reg[1] = 0;
    }else{
        reg[0] = DISP_getNumberCode(int(busLine/10));
        reg[1] = DISP_getNumberCode(busLine % 10);
    }

    // Show wait time (only if less than 100 minutes)
    if( waitTime < 10 ){
        reg[3] = 0;
        reg[4] = DISP_getNumberCode(waitTime);
    }else if( waitTime < 100 ){
        reg[3] = DISP_getNumberCode(int(waitTime/10));
        reg[4] = DISP_getNumberCode(waitTime % 10);
    }

    // Write into display register
    DISP_write();   
}


/*
 * Display error
 */
void DISP_showError(){
    reg[0] = 0b01111001;      // E 
    reg[1] = 0b01010000;      // r
    reg[3] = 0b01010000;      // r
    reg[4] = 0;
    DISP_write();    
}


/*
 * Display router connection error
 */
void DISP_showRouterConnectionError(){
    reg[0] = 0b01010000;      // r 
    reg[1] = 0b01111000;      // t
    reg[3] = 0b01011000;      // c
    reg[4] = 0b01010100;      // n
    DISP_write();    
}


/*
 * Display error with Ethernet port initialization
 */
void DISP_showDHCP(){
    reg[0] = 0b01011110;      // d 
    reg[1] = 0b01110100;      // h
    reg[3] = 0b01011000;      // c
    reg[4] = 0b01110011;      // P
    DISP_write();    
}


/*
 * Display error with Ethernet port initialization
 */
void DISP_showConnectionError(){
    // Display dot on the upper side of the display
    reg[2] = (reg[2] & 0xEF) | 0x10;
    DISP_write();
    delay(100);
}


/*
 * Clear connection error
 */
void DISP_clearConnectionError(){
    reg[2] = reg[2] & 0xEF;
    DISP_write();
    delay(100);
}


/*
 * Clear whole display
 */
void DISP_clear(){
    
    reg[0] = 0;
    reg[1] = 0;
    reg[3] = 0;
    reg[4] = 0;
    DISP_write(); 
}


/*
 * Switch colon ON/OFF
 */
void DISP_switchColon(){
    reg[2] = reg[2] ^ 0x02;
    DISP_write();
}
