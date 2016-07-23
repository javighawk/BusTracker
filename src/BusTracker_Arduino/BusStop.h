#ifndef BUSSTOP_HPP
#define BUSSTOP_HPP

#include "Arduino.h"

#define WAITTIMES_N		              3         /* Number of waittimes per BusStop object */
#define LASTUPDATE_THRESHOLD    60000         /* We consider the data not updated if a minute has passed since last update */


using namespace std;

class BusStop{
public:
  	BusStop(char *id);
  	void resetTimes();
    void setWTime(String st, uint8_t busLine);
    char *getId(){ return id; }
    long getLastUpdated(){ return lastUpdated; }
  	uint8_t getWTime(uint8_t p){ return wTime[p]; }
    uint8_t getWBusLine(uint8_t p){ return wBusLine[p]; }
private:
  	char *id;
    long lastUpdated;
    uint8_t wBusLine[WAITTIMES_N];
  	uint8_t wTime[WAITTIMES_N];
};

#endif
