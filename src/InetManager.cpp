#include <sys/socket.h>
#include <stdio.h>
#include <stdlib.h>
#include <string>
#include <string.h>
#include <netdb.h>
#include <unistd.h>
#include <arpa/inet.h>
#include "BusStop.hpp"

#define CONNECT_PERIOD_SEC	60

/*
 * On board LED blink C++ example
 *
 * Demonstrate how to blink the on board LED, writing a digital value to an
 * output pin using the MRAA library.
 * No external hardware is needed.
 *
 * - digital out: on board LED
 *
 * Additional linker flags: none
 */

using namespace std;

/*
 * Descriptor of the socket we will use to communicate with the server
 */
int socket_desc;

/*
 * Website to which we connect
 */
string webURL = "transitego.saskatoon.ca";

/*
 * Current time & date extracted from website
 */
tm currentTimeDate;

/*
 * Bus stop IDs
 */
const string farmersMarket_DT_ID = "5755";
const string farmersMarket_RD_ID = "5758";
const string avenueC20th_DT_ID = "3088";
const string avenueC20th_PH_ID = "3083";

extern void SC_retrieveData(string sourcecode, BusStop *bStop, tm *currentTimeDate);


/*
 * Initializes socket to communicate with the website
 *
 * @param ip String containing the IP address to connect to
 *
 * @return 0 if correct initialization. 1 otherwise.
 */
int INET_initSocket(string ip){

    struct sockaddr_in server;

    //Create socket
    socket_desc = socket(AF_INET , SOCK_STREAM , 0);
    if (socket_desc == -1)
    {
        printf("Could not create socket");
    }

    // Configure sockaddr struct
    server.sin_addr.s_addr = inet_addr(ip.c_str());
    server.sin_family = AF_INET;
    server.sin_port = htons( 80 );

    //Connect to remote server
    if (connect(socket_desc , (struct sockaddr *)&server , sizeof(server)) < 0)
    {
    	// Need to implement timeout
        printf("connect error");
        return 1;
    }

    //printf("Connected\n");
    return 0;
}


/*
 * Converts the domain into an IP
 *
 * @param domain The URL of the website.
 *
 * @return The IP in a string.
 */
string INET_getIPFromDomain(string domain){

    char ip[100];
    struct hostent *he;
    struct in_addr **addr_list;
    int i;

    if ( (he = gethostbyname( domain.c_str() ) ) == NULL)
    {
        //gethostbyname failed
        herror("gethostbyname");
        return string();
    }

    //Cast the h_addr_list to in_addr , since h_addr_list also has the ip address in long format only
    addr_list = (struct in_addr **) he->h_addr_list;

    for(i = 0; addr_list[i] != NULL; i++)
    {
        //Return the first one;
        strcpy(ip , inet_ntoa(*addr_list[i]) );
    }

    return string(ip);
}

/*
 * Sends a request to the server and records the answer
 *
 * @return The answer of the server.
 */
string INET_getWebsite(string stop_id){

    char server_reply[30000];

    string message = "GET /hiwire?.a=iNextBusResults&StopId=";
    message += stop_id;
    message += " HTTP/1.1\r\n";
    message += "Host: " + webURL + "\r\nConnection: close\r\n\r\n";

    // Send the request to the server
    if( send(socket_desc , message.c_str() , message.length() , 0) < 0)
    {
        printf("Send failed");
        return string();
    }
    //printf("Data Send\n");

    // Receive a reply from the server
    int read, index = 0;
    while( (read = recv(socket_desc, (void *)(server_reply + index), sizeof(server_reply) - index, 0)) > 0)
    {
    	if( read < 0 ){
			printf("Error receiving");
			return string();
    	}

    	index += read;
    }

    //printf("Reply received\n");

    return string(server_reply);
}

/*
 * Getter
 */
tm INET_getCurrentTimeDate(){
	return currentTimeDate;
}


/*
 * Main function
 */
int main(){

	// Create bus stops
	BusStop farmersMarket_DT = BusStop(atoi(farmersMarket_DT_ID.c_str()));
	BusStop farmersMarket_RD = BusStop(atoi(farmersMarket_DT_ID.c_str()));
	BusStop avenueC20th_DT = BusStop(atoi(avenueC20th_DT_ID.c_str()));
	BusStop avenueC20th_PH = BusStop(atoi(avenueC20th_PH_ID.c_str()));

	// Get the IP from the URL
	string IP = INET_getIPFromDomain(webURL);

	while(true){

		// Open the socket
		if( INET_initSocket( IP ) != 0 ){
			// Need to implement timeout case
			continue;
		}

		// Get the content of the website
		string webCurrentContent = INET_getWebsite(farmersMarket_DT_ID);

		// Close socket
		close(socket_desc);

		// Get data from the website
		SC_retrieveData(webCurrentContent, &farmersMarket_DT, &currentTimeDate);



		// Testing: Print data
		tm realTimes[3];
		realTimes[0] = farmersMarket_DT.BSTOP_getSTime(0);
		realTimes[1] = farmersMarket_DT.BSTOP_getSTime(1);
		realTimes[2] = farmersMarket_DT.BSTOP_getSTime(2);

		printf("Now: %d/%d/%d , %d:%d\n", currentTimeDate.tm_mon, currentTimeDate.tm_mday, currentTimeDate.tm_year, currentTimeDate.tm_hour, currentTimeDate.tm_min);
		printf("Real times: ");
		for( int i=0 ; i<3 ; i++ ){
			if( realTimes[i].tm_hour == -1 )
				break;
			printf("%d:%d  ", realTimes[i].tm_hour, realTimes[i].tm_min);
		}
		printf("\n");
		printf("Wait times: ");
		for( int i=0 ; i<3 ; i++ ){
			printf("%dmin  ", farmersMarket_DT.BSTOP_getWaitTime(i));
		}
		printf("\n\n");



		// Reset wait times
		farmersMarket_DT.BSTOP_setEmptyTime();

		// Delay
		sleep(CONNECT_PERIOD_SEC);
	}

    return 0;
}
