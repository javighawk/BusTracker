#ifndef BUSSTOP_HPP
#define BUSSTOP_HPP

#include <time.h>
#include <string>

#define WAITTIMES_N		3

using namespace std;

class BusStop{
public:
	BusStop(int id);
	int BSTOP_getWaitTime(int p);
	void BSTOP_setSTime(string st, int p);
	void BSTOP_setEmptyTime();
	int BSTOP_getId(){ return BSTOP_id; }
	tm BSTOP_getSTime(int p){ return BSTOP_sTime[p]; }
private:
	int BSTOP_id;
	tm BSTOP_sTime[WAITTIMES_N];
};

#endif
