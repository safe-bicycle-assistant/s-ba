#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <unistd.h>
#include <pthread.h>

void* write_thd(void* socket);
void* read_thd(void* socket);
void error_handling(char* msg)
{
	fprintf(stderr,"%s\n",msg);
	return;
}
int sock ;
int main(void)
{
    // printf("sizeof float int %d %d\n",sizeof(float), sizeof(int));
	char port[]="33333";
	char msg[100];
	int serv_sock, clnt_sock = -1;
	struct sockaddr_in serv_addr,clnt_addr;
	socklen_t clnt_addr_size;
	serv_sock = socket(PF_INET, SOCK_STREAM,0);
    if(serv_sock == -1)
            error_handling("socket() error");
    memset(&serv_addr,0,sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = htonl(INADDR_ANY);
    serv_addr.sin_port = htons(atoi(port));
    if(bind(serv_sock,(struct sockaddr*) &serv_addr,sizeof(serv_addr))== -1)
            error_handling("bind() error");
    if(listen(serv_sock,5) == -1)
            error_handling("listen() error");
    if(clnt_sock<0)
    {
        while(1){
            clnt_addr_size = sizeof(clnt_addr);
            clnt_sock = accept(serv_sock, (struct sockaddr*)&clnt_addr,&clnt_addr_size);
            if(clnt_sock == -1)
                    error_handling("accept() error");
            sock = clnt_sock;
            printf("connection established\n");    
            pthread_t p_thread[1];
            int status;
            pthread_create(&p_thread[0],NULL,write_thd,(void*)&sock);    
        }
    }
	
	/*
	char port[6]="5672";
	char msg[100];
	char ip[16]="192.168.0.28";
	printf("Insert ip addr : ");
	printf("192.168.0.22\n");
	//scanf("%s",ip);
	printf("Insert port num : %s\n",port);
	//scanf("%s",port);
	struct sockaddr_in serv_addr;
	int sock = socket(PF_INET,SOCK_STREAM,0);
	if(sock == -1)
	{
		printf("socket error\n");
	}
	memset(&serv_addr,0,sizeof(serv_addr));
	serv_addr.sin_family = AF_INET;
	serv_addr.sin_addr.s_addr = inet_addr(ip);
	serv_addr.sin_port = htons(atoi(port));
	if(connect(sock,(struct sockaddr*)&serv_addr,sizeof(serv_addr)) == -1)
	{
		printf("connect() error\n");
		return 0;
	}
			//printf("connect() error\n");
	else
	{
		printf("Connection with %s:%s successfully done!\n",ip,port);
	}
	
	while(1)
	{
		printf("type in message (maxlen = 100) (EnD to end)\n");
		scanf("%s",msg);
		if(strcmp("EnD",msg)==0)
		{
			write(sock,msg,sizeof(msg));

			break;
		
		}
		write(sock,msg,sizeof(msg));
	}
	*/
	pthread_t p_thread[1];
	int status;
	pthread_create(&p_thread[0],NULL,write_thd,(void*)&sock);
	// pthread_create(&p_thread[1],NULL,read_thd,(void*)&sock);

	pthread_join(p_thread[0],(void**)&status);
	// pthread_join(p_thread[1],(void**)&status);

	//printf("closing socket\n");
	//close(sock);
	return 0;
}
void* write_thd(void* socket)
{
	int* sock = (int*)socket;
    float cadencebit = 10.3;
    int detectionbit = 30;
    char buffer[100] = {0,};
    
    buffer[3] = *((int*)(&cadencebit)) ;
	buffer[2] = *((int*)(&cadencebit)) >> 8;
	buffer[1] = *((int*)(&cadencebit)) >> 16;
	buffer[0] = *((int*)(&cadencebit)) >> 24;

	buffer[7] = *((int*)(&detectionbit));
	buffer[6] = *((int*)(&detectionbit)) >> 8;
	buffer[5] = *((int*)(&detectionbit)) >> 16;
	buffer[4] = *((int*)(&detectionbit)) >> 24;
    // memcpy(buffer,&cadencebit,sizeof(cadencebit));
    // memcpy(buffer+sizeof(cadencebit),&detectionbit,sizeof(detectionbit));
	//sprintf(buffer,"%.5f,%d");
    char msg[100];
	while(1)
	{
		scanf("%s",msg);
        
		if(strcmp("s",msg)==0)
		{
			//send
            // send(*sock,&cadencebit,sizeof(cadencebit),0);
            // send(*sock,&detectionbit,sizeof(detectionbit),0);
            // for(int i = 0; i< 100; i++)
            //     printf("%d",msg[i]);
            // printf("\n");
			write(*sock,buffer,sizeof(buffer));
            // write(*sock,&detectionbit,sizeof(detectionbit));

		}
		// write(*sock,msg,sizeof(msg));
		printf("Sent data to server : %s\n",msg);
	}
	close(*sock);
	printf("closing socket\n");
}
void* read_thd(void* socket)
{
	int * sock = (int*)socket;
	char msg[100];
	int len;
	while(1)
	{
		len = read(*sock,msg,sizeof(msg));
		printf("Received data from server : \n");
		for(int i = 0; i< 100; i++)
		{
			printf("%d",msg[i]);
		}
		printf("\n");
		//printf("Received data from server : %s\n",msg);
	}
}
