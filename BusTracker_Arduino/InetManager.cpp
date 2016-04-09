#include "BusStop.h"
#include <Ethernet.h>
#include "Display.h"

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
   - The MAC address of your Ethernet interface.
 */

/*
 * Descriptor of the socket we will use to communicate with the server
 */
int socket_desc_client;

/*
 * Descriptor of the socket that will act as server
 */
int socket_desc_server;

/*
 * Website to which we connect
 */
String webURL = "transitego.saskatoon.ca";

/*
 * IP address of the website
 */
IPAddress webIP(167, 129, 248, 111);

/*
 * Ethernet port MAC address
 */
byte eth0_mac[] = {  0x98, 0x4F, 0xEE, 0x01, 0x3F, 0x04 };

/*
 * Ethernet client
 */
EthernetClient ethClient;


extern void SC_retrieveData(String sourcecode, BusStop *bStop, tm *currentTimeDate);


/*
 * Initializes socket to communicate with the website
 *
 * @param ip String containing the IP address to connect to
 *
 * @return 0 if correct initialization. 1 otherwise.
 */
int INET_initClientSocket(){

    // Initialize Ethernet
    if( Ethernet.begin(eth0_mac) == 0 ){
        DISP_showError();
        return 1;
    }

    // Use static address

    // Bring up eth0 and wait 5 seconds to continue
    system("ifup eth0");
    delay(1000);

    // Connect to the server
    int error;
    if( (error = ethClient.connect(webIP,80)) != 1 ){
        DISP_showConnectionError(error);
        ethClient.stop();
        return 1;
    }
    return 0;
}


/*
 * Initializes socket to hear from clients
 *
 * @param port Port to which we will bind the socket
 *
 * @return 0 if correct initialization. 1 otherwise.
 */
int INET_initServer(int port){
}


/*
 * Sends a request to the server and records the answer
 * 
 * @param stop_id The ID of the bus stop to look at
 *
 * @return The answer of the server.
 */
String INET_getWebsite(String stop_id){

    String resp;

    // Create the message to request the website. Trying to download the website:
    // transitego.saskatoon.ca/hiwire?.a=iNextBusResults&StopId=stop_id
    // Changing "stop_id" by the actual id.
    String request = "GET /hiwire?.a=iNextBusResults&StopId=" + stop_id + " HTTP/1.1\r\nHost: " + webURL + "\r\nConnection: close\r\n\r\n";

    // Send request
    ethClient.print(request);

    // Read answer
    while( ethClient.available() ){

        // Record answer
        char c = ethClient.read();
        resp += c;

        // Check if HTML file is over
        if( resp.indexOf("</html>") != -1 )
            break;
    }

    ethClient.stop();
    return resp;
}


/*
 * Connects to website and retrieves HTML code
 * 
 * @param busStopID The ID of the bus stop we want to look at
 * 
 * @return The website's source code
 */
String INET_getBusStopWebsite(String busStopID){
  
    // Open the socket
    if( INET_initClientSocket() != 0 ){

        // Turn down Ethernet port
        system("ifdown eth0");
        return "";
    }  

    // Return the website
    return INET_getWebsite(busStopID);
}
