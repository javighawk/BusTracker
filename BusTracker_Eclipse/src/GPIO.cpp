#include "GPIO.hpp"

using namespace std;

/*
 * Output pin
 */
mraa::Gpio* d_pin = NULL;

/*
 * Constructor
 */
GPIO::GPIO(){

	d_pin = new mraa::Gpio(13, true, false );;

	if (d_pin == NULL) {
		printf("Can't create mraa::Gpio object, exiting");
		return;
	}

	// set the pin as output
	if (d_pin->dir(mraa::DIR_OUT) != mraa::SUCCESS) {
		printf("Can't set digital pin as output, exiting");
		return;
	}
}

/*
 * Sample function
 */
void GPIO::toggleLed(){
	if( d_pin->read() == 1 )
		d_pin->write(0);
	else d_pin->write(1);
}
