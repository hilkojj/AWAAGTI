#include <stdio.h>
#include <string.h>   //strlen
#include <stdlib.h>
#include <errno.h>
#include <unistd.h>   //close
#include <arpa/inet.h>    //close
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <sys/time.h> //FD_SET, FD_ISSET, FD_ZERO macros
#include <netdb.h>
#include <strings.h>
#include <time.h>

#define TRUE   1
#define FALSE  0

#define SENSORPORT 7789
#define DBPORT 12345
#define DBIP "192.168.188.26"

char *mock = "START\n2019-01-04,17:11:08\n748482,-3.1\n729837,12.3\n987386,-12.9\nEND\n";

char bla()
{
	srand(time(NULL));
	return rand();
}

short SocketCreate(void)
{
        short hSocket;
        hSocket = socket(AF_INET, SOCK_STREAM, 0);
        return hSocket;
}

//try to connect with server
int SocketConnect(int hSocket)
{
        int iRetval=-1;
        struct sockaddr_in remote={0};

        remote.sin_addr.s_addr = inet_addr(DBIP);
        remote.sin_family = AF_INET;
        remote.sin_port = htons(DBPORT);

        iRetval = connect(hSocket , (struct sockaddr *)&remote , sizeof(struct sockaddr_in));


        return iRetval;
}

int SocketSend(int hSocket,char* Rqst,short lenRqst)

{
        int shortRetval = -1;
        struct timeval tv;
        tv.tv_sec = 20;  /* 20 Secs Timeout */
        tv.tv_usec = 0;

        if(setsockopt(hSocket, SOL_SOCKET, SO_SNDTIMEO, (char *)&tv,sizeof(tv)) < 0)
        {
          printf("Time Out\n");
          return -1;
        }
        shortRetval = send(hSocket , Rqst , lenRqst , 0);

        return shortRetval;
 }

void stuurMock(char *bericht)
{
				int hSocket;
				hSocket = SocketCreate();

				if(hSocket == -1)
					puts("socket error");
				puts("socket created");
				if(SocketConnect(hSocket) < 0)
				{
					puts("connect error");
					return;
				}
				puts("socket connected");
					/* send(hSocket, bericht, strlen(bericht), 0); */
					/* memset(&bericht[0], 0, sizeof(bericht)); */
				SocketSend(hSocket, bericht, strlen(bericht));
				close(hSocket);
				puts("closed");
}

int main(int argc , char *argv[])
{
    int opt = TRUE;
    int master_socket , addrlen , new_socket , client_socket[3000] ,
          max_clients = 3000 , activity, i , valread , sd;
    int max_sd;
    struct sockaddr_in address;

    char buffer[50000];

    //set of socket descriptors
    fd_set readfds;

    //a message
    char *message = "Dit is een test\n";

	int lijn = 0;
	char *lijnen[100];

    //initialise all client_socket[] to 0 so not checked
    for (i = 0; i < max_clients; i++)
    {
        client_socket[i] = 0;
    }

    //create a master socket
    if( (master_socket = socket(AF_INET , SOCK_STREAM , 0)) == 0)
    {
        perror("socket failed");
        exit(EXIT_FAILURE);
    }

    //set master socket to allow multiple connections ,
    //this is just a good habit, it will work without this
    if( setsockopt(master_socket, SOL_SOCKET, SO_REUSEADDR, (char *)&opt,
          sizeof(opt)) < 0 )
    {
        perror("setsockopt");
        exit(EXIT_FAILURE);
    }

    //type of socket created
    address.sin_family = AF_INET;
    address.sin_addr.s_addr = INADDR_ANY;
    address.sin_port = htons( SENSORPORT );

    //bind the socket to localhost
    if (bind(master_socket, (struct sockaddr *)&address, sizeof(address))<0)
    {
        perror("bind failed");
        exit(EXIT_FAILURE);
    }
    printf("Listener on port %d \n", SENSORPORT);

    //try to specify maximum of 3 pending connections for the master socket
    if (listen(master_socket, 3) < 0)
    {
        perror("listen");
        exit(EXIT_FAILURE);
    }

    //accept the incoming connection
    addrlen = sizeof(address);

	/* for (int i = 0; i < 10; ++i) { */
	/* 	stuurMock(mock); */
	/* } */
	puts("Waiting for connections ...");
    while(TRUE)
    {
        //clear the socket set
        FD_ZERO(&readfds);

        //add master socket to set
        FD_SET(master_socket, &readfds);
        max_sd = master_socket;

        //add child sockets to set
        for ( i = 0 ; i < max_clients ; i++)
        {
            //socket descriptor
            sd = client_socket[i];

            //if valid socket descriptor then add to read list
            if(sd > 0)
                FD_SET( sd , &readfds);

            //highest file descriptor number, need it for the select function
            if(sd > max_sd)
                max_sd = sd;
        }

        //wait for an activity on one of the sockets , timeout is NULL ,
        //so wait indefinitely
        activity = select( max_sd + 1 , &readfds , NULL , NULL , NULL);

        if ((activity < 0) && (errno!=EINTR))
        {
            printf("select error");
        }

        //If something happened on the master socket ,
        //then its an incoming connection
        if (FD_ISSET(master_socket, &readfds))
        {
            if ((new_socket = accept(master_socket,
                    (struct sockaddr *)&address, (socklen_t*)&addrlen))<0)
            {
                perror("accept");
                exit(EXIT_FAILURE);
            }

            //inform user of socket number - used in send and receive commands
            printf("New connection , socket fd is %d , ip is : %s , port : %d\n" , new_socket , inet_ntoa(address.sin_addr) , ntohs
                  (address.sin_port));

            //send new connection greeting message
            if( send(new_socket, message, strlen(message), 0) != strlen(message) )
            {
                perror("send");
            }

            puts("Welcome message sent successfully");

            //add new socket to array of sockets
            for (i = 0; i < max_clients; i++)
            {
                //if position is empty
                if( client_socket[i] == 0 )
                {
                    client_socket[i] = new_socket;
                    printf("Adding to list of sockets as %d\n" , i);

                    break;
                }
            }
        }

        /* else its some IO operation on some other socket */
        for (i = 0; i < max_clients; i++)
        {
            sd = client_socket[i];

            if (FD_ISSET( sd , &readfds))
            {
				if(read(sd, buffer, 50000) != 0)
				{
					/* char *copy = strdup(mock); */
					/* lijnen[lijn] = strtok(copy, "\n"); */
                    /*  */
					/* while(lijnen[lijn] != NULL) */
					/* 	lijnen[++lijn] = strtok(NULL,"\n"); */
					printf("%s", buffer);
					stuurMock(buffer);
					memset(&buffer[0], 0, sizeof(buffer));
				}
				/* for (i = 0; i < 6; ++i) { */
				/* 	puts(lijnen[i]); */
				/* } */



                //Check if it was for closing , and also read the
                //incoming message
                /* if ((valread = read( sd , buffer, 5000)) == 0) */
                /* { */
                /*     //Somebody disconnected , get his details and print */
                /*     getpeername(sd , (struct sockaddr*)&address , \ */
                /*         (socklen_t*)&addrlen); */
                /*     printf("Host disconnected , ip %s , port %d \n" , */
                /*           inet_ntoa(address.sin_addr) , ntohs(address.sin_port)); */
                /*  */
                /*     //Close the socket and mark as 0 in list for reuse */
                /*     close( sd ); */
                /*     client_socket[i] = 0; */
                /* } */
                /*  */
                /* //Echo back the message that came in */
                /* else */
                /* { */
                /*     //set the string terminating NULL byte on the end */
                /*     //of the data read */
                /*     buffer[valread] = '\0'; */
                /*     send(sd , buffer , strlen(buffer) , 0 ); */
				/* 	puts(buffer); */
				/* 	while(1) */
				/* 	{ */
				/* 		send(sd , mock , strlen(mock) , 0 ); */
				/* 		puts(mock); */
				/* 		sleep(1); */
				/* 	} */
                /* } */



            }
        }
		/* sleep(0.1); */
    }

    return 0;
}
