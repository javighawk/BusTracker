#ifndef BUSSTOP_HPP
#define BUSSTOP_HPP

#include "Arduino.h"
#include <time.h>

#define WAITTIMES_N		              3
#define LASTUPDATE_THRESHOLD    60000


using namespace std;

class BusStop{
public:
  	BusStop(int id, int busStop);
  	int BSTOP_getWaitTime(int p);
  	void BSTOP_setSTime(String st, int p);
  	void BSTOP_setEmptyTime();
  	int BSTOP_getId(){ return BSTOP_id; }
    int BSTOP_getBusLine(){ return BSTOP_busLine; }
    long BSTOP_getLastUpdated(){ return BSTOP_lastUpdated; }
  	tm BSTOP_getSTime(int p){ return BSTOP_sTime[p]; }
    void BSTOP_setLastUpdated(){ BSTOP_lastUpdated = millis(); }
private:
  	int BSTOP_id;
    int BSTOP_busLine;
    long BSTOP_lastUpdated;
  	tm BSTOP_sTime[WAITTIMES_N];
};

#endif
