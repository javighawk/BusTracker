#include <sys/socket.h>
#include <netdb.h>
#include <arpa/inet.h>
#include "BusStop.h"
#include <Ethernet.h>

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
        Serial.println("Failed to configure Ethernet using DHCP");
        return 1;
    }

    // Bring up eth0 and wait 5 seconds to continue
    system("ifup eth0");
    delay(5000);

    // Connect to the server
    if( ethClient.connect(webIP,80) != 1 ){
        Serial.println("Connection failed");
        return 1;
    }
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
 * @return The answer of the server.
 */
String INET_getWebsite(String stop_id){

    String resp;

    // Create the message to request the website
    String request = "GET /hiwire?.a=iNextBusResults&StopId=" + stop_id + " HTTP/1.1\r\nHost: " + webURL + "\r\nConnection: close\r\n\r\n";

    // Send request
    ethClient.print(request);

    // Read answer
    while( ethClient.connected() ){

        // Record answer
        if( ethClient.available() ){
            char c = ethClient.read();
            resp += c;
        }

        // Check if HTML file is over
        if( resp.indexOf("</html>") != -1 ){
            ethClient.stop();
            break;
        }
    }
    return resp;
}
