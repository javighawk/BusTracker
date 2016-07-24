/*
  This file contains functions that manage the BCD display. 
  in order to show the bus waiting time and the bus line.

  Author: Javier Garcia  
 */

#include "Display.h"
#include <Adafruit_GFX.h>
#include <Adafruit_LEDBackpack.h>
#include <Wire.h>

/* Number-BCD code conversion */
#define DISP_NUM0           0b00111111
#define DISP_NUM1           0b00000110
#define DISP_NUM2           0b01011011
#define DISP_NUM3           0b01001111
#define DISP_NUM4           0b01100110
#define DISP_NUM5           0b01101101
#define DISP_NUM6           0b01111101
#define DISP_NUM7           0b00000111
#define DISP_NUM8           0b01111111
#define DISP_NUM9           0b01101111

/* Letter-BCD code conversion */
#define DISP_A              0b01110111
#define DISP_B              0b01111100
#define DISP_C              0b01011000
#define DISP_D              0b01011110
#define DISP_E              0b01111001
#define DISP_F              0b01110001
#define DISP_G              0b01111101
#define DISP_H              0b01110100
#define DISP_I              0b00110000
#define DISP_J              0b00001110
#define DISP_L              0b00111000
#define DISP_N              0b01010100
#define DISP_O              0b00111111
#define DISP_P              0b01110011
#define DISP_R              0b01010000
#define DISP_S              0b01101101
#define DISP_T              0b01111000
#define DISP_U              0b00111110
#define DISP_V              0b00011100
#define DISP_Y              0b01101110
#define DISP_Z              0b01011011

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
    // Return corresponding code
    switch(num){
        case 0: return DISP_NUM0;
        case 1: return DISP_NUM1;
        case 2: return DISP_NUM2;
        case 3: return DISP_NUM3;
        case 4: return DISP_NUM4;
        case 5: return DISP_NUM5;
        case 6: return DISP_NUM6;
        case 7: return DISP_NUM7;
        case 8: return DISP_NUM8;
        case 9: return DISP_NUM9;
        default: return 0;
    }
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
    // Get the closest waiting time
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
