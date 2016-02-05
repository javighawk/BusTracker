#include "Display.h"
#include "Adafruit_LEDBackpack.h"
#include "Adafruit_GFX.h"
#include <Wire.h>

/*
 * 7-segment object
 */
Adafruit_7segment matrix7 = Adafruit_7segment();

/*
 * Constructor
 */
Display::Display(){

    // Initialize display with I2C address = 0x70
    matrix7.begin(0x70);

    // Clear it
    matrix7.writeDigitRaw(0,0);
    matrix7.writeDigitRaw(1,0);
    matrix7.writeDigitRaw(2,0);
    matrix7.writeDigitRaw(3,0);
    matrix7.writeDigitRaw(4,0);
    matrix7.writeDisplay();
}

/*
 * Shows the waiting time in the display
 * 
 * @param waitTime The waiting time in minutes
 */
void Display::showWaitTime(int waitTime){
    
    if( waitTime == -1 ){
        matrix7.printError();
    } else {
        matrix7.print(waitTime);
        matrix7.writeDigitNum(0, 9, false);
    }
    matrix7.writeDisplay();    
}
