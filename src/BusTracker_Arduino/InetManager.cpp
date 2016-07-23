#include <SPI.h>
#include <Adafruit_CC3000.h>
#include <Adafruit_CC3000_Server.h>
#include <ccspi.h>
#include "Display.h"
#include "ParsingManager.h"

/*
  This code is in charge of dealing with the Internet access. We use the Ethernet library
  for this, so even though a knowledge of sockets is recommended, it is not strictly
  necessary to know how to work with them.

   Adapt this code to your case by modifying:
   - The website domain you access to get the information.
   -- Check the message sent in the function INET_getWebsite. You will have to modify that
   -- part so the full URL (less domain) is included in the GET primitive. Check how HTML
   -- petitions work in case of doubt.
   - The IP of the website.
 */

#define WLAN_SSID             "WiFiSSID"
#define WLAN_PASS             "WiFiPASS"
#define WLAN_SECURITY         WLAN_SEC_WPA2

#define ADAFRUIT_CC3000_IRQ   3  
#define ADAFRUIT_CC3000_VBAT  5
#define ADAFRUIT_CC3000_CS    10

#define WEBURL                "transitego.saskatoon.ca"

/*
 * WiFi handler
 */
Adafruit_CC3000 cc3000 = Adafruit_CC3000(ADAFRUIT_CC3000_CS, ADAFRUIT_CC3000_IRQ, ADAFRUIT_CC3000_VBAT, SPI_CLOCK_DIV2);

/*
 * Client
 */
Adafruit_CC3000_Client clientTCP;

/*
 * IP address of the website
 */
const uint32_t webIP  = cc3000.IP2U32(167, 129, 248, 111);


/*
 * Initializes socket to communicate with the website
 *
 * @param ip String containing the IP address to connect to
 *
 * @return 0 if correct initialization. 1 otherwise.
 */
uint8_t INET_init(){

    // Initialize WiFi board
    if (!cc3000.begin()){
        return 1;
    }

    // Connect to WiFi network
    if (!cc3000.connectToAP(WLAN_SSID, WLAN_PASS, WLAN_SECURITY)){
        return 1;
    }
    // Request DHCP
    while (!cc3000.checkDHCP()){
        delay(100);
    } 
}

uint8_t INET_connectSocket(){
  
    // Connect to server
    clientTCP = cc3000.connectTCP(webIP, 80);

    // Check connection
    if (clientTCP.connected()){
        DISP_clearConnectionError();
    } else {
        DISP_showConnectionError();
        clientTCP.close();
        return 1;
    }  
    return 0;
}

/*
 * Sends a request to the server and records the answer
 * 
 * @param stop_id The ID of the bus stop to look at
 * @param parse The ParsingManager object where the valid info will be stored
 *
 * @return 1 if end of the website was reached, 0 otherwise.
 */
uint8_t INET_getWebsite(char *stop_id){

    String resp;
    uint8_t res = 0;

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
        return res;
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
            res = 1;
            break;
        }
    }

    // Close connection to the server
    clientTCP.close();
    return res;
}


/*
 * Connects to website and retrieves HTML code
 * 
 * @param busStopID The ID of the bus stop we want to look at
 * 
 * @return 1 if successful, 0 otherwise.
 */
uint8_t INET_getBusStopWebsite(char *busStopID){
  
    // Open the socket
    if( INET_connectSocket() != 0 )
        return 0; 

    // Return the website
    return INET_getWebsite(busStopID);
}
