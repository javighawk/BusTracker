# BusTracker
Bus tracker in Intel Galileo for Saskatoon transit. It will check the waiting time on a desired stop and display it on a 7-segment display.

The Arduino folder contains the version that works with the 7-segment display. For this version, these libraries are needed:
- Arduino Ethernet library
- Adafruit GFX library: https://github.com/adafruit/Adafruit-GFX-Library
- Adafruit LED Backpack library: https://github.com/adafruit/Adafruit_LED_Backpack
The display used is:
- Adafruit 1.2" 7-segment red: https://learn.adafruit.com/adafruit-led-backpack/1-2-inch-7-segment-backpack

The Eclipse folder is an Eclipse project that prints the waiting time for the bus.
