# BusTracker
Bus tracker in Raspberry Pi originally designed for Saskatoon transit. It will check the waiting time on desired stop(s) and display it on a 7-segment display.

For this version, these dependencies are needed:
- Google GTFS Realtime (Download JAR at http://www.java2s.com/Code/Jar/o/Downloadonebusawaygtfsrealtimeapi110jar.htm)
- Adafruit LED Backpack library for Raspberry Pi in Java (https://github.com/slintes/RaspiAdafruitLEDBackpack)
- Pi4J library (Follow http://pi4j.com/install.html to install)

The material used is:
- Raspberry Pi 3
- Adafruit 1.2" 7-segment red
- 2x Buttons
- 1x Switch
- 2x LEDs
- 2x 330 ohm resistors

Description of Java modules
- Main: Contains the initialization of all modules and the main execution of the program.
- Bus: Representation of a bus and a bus stop by its route. Contains the days in which this route is running, the scheduled arrival time to the associated bus stop and the delay.
- BusStopThread: Represents a bus stop in both directions (from and to City Centre). Contains all the routes going through that bus stop represented as Bus objects. This is a thread that each 10 seconds checks the last available info. Downloads the realtime feed from the transit's website, applies the delays of the routes that pass through this bus stop and returns the N next upcoming buses in each direction.
- Display: Contains all functions related to the displaying of the waiting time in the 7-segment display
- GPIO: Contains all functions related to the GPIO handling
- GTFSData: Contains the GTFS static data parsed from the txt files available on the transit's website. Place all these txt files into one folder, whose path will be needed as an argument on the main execution.

To run the program:
- Run Main.java with the path to the GTFS static data as the argument. For example, I store the GTFS static data at PROJECT_FOLDER/gtfs/. So when executing the project from PROJECT_FOLDER, the argument passed should be "gtfs/"

Additional info:
- For Saskatoon's static GTFS data, check here: https://transitfeeds.com/p/city-of-saskatoon/264
- For Saskatoon's GTFS-realtime data, check here: https://transit.saskatoon.ca/about-us/open-data-saskatoon-transit (check the different alerts).
