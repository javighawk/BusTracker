#ifndef DISPLAY_H
#define DISPLAY_H

#include "BusStop.h"

void DISP_init();
void DISP_showWaitTime(BusStop bStop);
void DISP_showTimeOut();
void DISP_showError();
void DISP_showEthernetError();
void DISP_showConnectionError();
void DISP_clear();

#endif
