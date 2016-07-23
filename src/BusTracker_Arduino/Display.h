#ifndef DISPLAY_H
#define DISPLAY_H

#include "BusStop.h"

void DISP_init();
void DISP_showWaitTime(BusStop *bStop, uint8_t waitTimeIndex);
void DISP_showTimeOut();
void DISP_showError();
void DISP_showEthernetError();
void DISP_showConnectionError();
void DISP_clear();
void DISP_switchColon();
void DISP_clearConnectionError();

#endif
