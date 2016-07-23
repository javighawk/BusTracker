/*
  This code is in charge of dealing with the Internet access. We use the Ethernet library
  for this, so even though a knowledge of sockets is recommended, it is not strictly
  necessary to know how to work with them.

   Adapt this code to your case by modifying:
   - The website domain you access to get the information (WEBURL).
   -- Check the message sent in the function INET_getWebsite. You will have to modify that
   -- part so the full URL (less domain) is included in the GET primitive. Check how HTML
   -- petitions work in case of doubt.
   - The IP of the website.
   - Your WiFi SSID and Password.

  Author: Javier Garcia  
 */

#include <SPI.h>
#include <Adafruit_CC3000.h>
#include <Adafruit_CC3000_Server.h>
#include <ccspi.h>
#include "Display.h"
#include "ParsingManager.h"

#define WLAN_SSID             "WiFiSSID"
#define WLAN_PASS             "WiFiPASS"
#define WLAN_SECURITY         WLAN_SEC_WPA2

#define ADAFRUIT_CC3000_IRQ   3  
#define ADAFRUIT_CC3000_VBAT  5
#define ADAFRUIT_CC3000_CS    10

#define WEBURL                "transitego.saskatoon.ca"

/* WiFi handler */
Adafruit_CC3000 cc3000 = Adafruit_CC3000(ADAFRUIT_CC3000_CS, ADAFRUIT_CC3000_IRQ, ADAFRUIT_CC3000_VBAT, SPI_CLOCK_DIV2);

/* Client */
Adafruit_CC3000_Client clientTCP;

/* IP address of the website */
const uint32_t webIP  = cc3000.IP2U32(167, 129, 248, 111);


/*
 * Initialize socket to communicate with the website
 *
 * @param ip String containing the IP address to connect to
 * @return 0 if correct initialization. 1 otherwise.
 */
uint8_t INET_init(){
    // Initialize WiFi board
    if (!cc3000.begin()){
        DISP_showError();
        return 1;
    }

    // Connect to WiFi network
    if (!cc3000.connectToAP(WLAN_SSID, WLAN_PASS, WLAN_SECURITY)){
        DISP_showRouterConnectionError();
        return 1;
    }
    
    // Request DHCP
    while (!cc3000.checkDHCP()){
        DISP_showDHCP();
        delay(100);
    } 

    // Correct initialization 
    return 0;
}


/*
 * Connect socket to the website
 */
void INET_connectSocket(){
    // Connect to server
    clientTCP = cc3000.connectTCP(webIP, 80);

    // Check connection
    if (clientTCP.connected()){
        DISP_clearConnectionError();
    } else {
        DISP_showConnectionError();
        clientTCP.close();
        
        // If connection failed, force Watchdog reset
        while(1);
    }  
}


/*
 * Send a request to the server and records the answer
 * 
 * @param stop_id The ID of the bus stop to look at
 * @param parse The ParsingManager object where the valid info will be stored
 */
void INET_getWebsite(char *stop_id){

    String resp;

    // Create the message to request the website. Trying to download the website:
    // transitego.saskatoon.ca/hiwire?.a=iNextBusResults&StopId=stop_id
    // Changing "stop_id" by the actual id.

    // Send request
    if( clientTCP.connected() ){
        clientTCP.fastrprint(F("GET /hiwire?.a=iNextBusResults&StopId="));
        clientTCP.fastrprint(stop_id);
        clientTCP.fastrprint(F(" HTTP/1.1\r\nHost: "));
        clientTCP.fastrprint(WEBURL);
        clientTCP.fastrprint(F("\r\nConnection: close\r\n\r\n"));
        clientTCP.println();
    } else {
        // Force watchdog reset
        while(1);
    }

    // Read answer
    while( clientTCP.connected() ){
        while( clientTCP.available() ){
    
            // Record answer
            char c = clientTCP.read();
            resp += c;

            // If the stored string is long enough, check for useful information
            if( resp.length() == 50 ){
                STR_findData(&resp);
            }
        }

        // Check if HTML file is over
        if( resp.indexOf("</html>") != -1 ){
            break;
        }
    }

    // Close connection to the server
    clientTCP.close();
}


/*
 * Connects to website and retrieves HTML code
 * 
 * @param busStopID The ID of the bus stop we want to look at
 * 
 * @return 1 if successful, 0 otherwise.
 */
void INET_getBusStopWebsite(char *busStopID){
    // Open the socket
    INET_connectSocket();

    // Get the website
    INET_getWebsite(busStopID);
}
