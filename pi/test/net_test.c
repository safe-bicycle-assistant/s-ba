#include <sys/stat.h>
#include <sys/types.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <time.h>
#include <pthread.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <time.h>
#include <linux/tcp.h>
#define BUFFER_MAX 1024
#define DIRECTION_MAX 1024
#define IN 0
#define OUT 1
#define VALUE_MAX 1024
#define LOW 0
#define HIGH 1

void* write_thd(void* data);
void* read_thd(void* data);
void* cadence_thd(void* data);
void error_handling(char* msg)
{
	fprintf(stderr,"%s\n",msg);
	return;
}

typedef struct data{
    float cadence;
    int detection;
    
}Data;


int lock = 0;

int* sock;
int main(void)
{
    int status;
    pthread_t p_thread[3];
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
        
        

        clnt_addr_size = sizeof(clnt_addr);
        clnt_sock = accept(serv_sock, (struct sockaddr*)&clnt_addr,&clnt_addr_size);

    }
    if(clnt_sock == -1)
        error_handling("accept() error");
    int opt_val = 1;
    setsockopt(clnt_sock,IPPROTO_TCP,TCP_NODELAY,&opt_val,sizeof(opt_val));

    sock = &clnt_sock;
    
    
    printf("connection established\n");
    int started = 0;
    
    Data data;
    data.cadence = 10.3;
    pthread_create(&p_thread[0],NULL,read_thd,(void*)&data);

    pthread_create(&p_thread[1],NULL,write_thd,(void*)&data);
    pthread_create(&p_thread[2],NULL,cadence_thd,(void*)&data);
    
    
    pthread_join(p_thread[0],(void**)&status);
    pthread_join(p_thread[1],(void**)&status);
    pthread_join(p_thread[2],(void**)&status);
    

    
    
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
	

	//printf("closing socket\n");
	//close(sock);
	return 0;
}
void* cadence_thd(void* data)
{
    Data* datas = data;
    srand(time(NULL));
    while(1)
    {
        sleep(1);
        // while(GPIORead(17) == 1);
        float a = (float)rand() / 18000000;
        // float duration = ((float)end-start)/CLOCKS_PER_SEC;
        // float rpm = 50/duration;
        datas->cadence = a;
    }
}

void* write_thd(void* data)
{
	// int* sock = (int*)socket;
    Data* datas = data;
    
    // memcpy(buffer,&cadencebit,sizeof(cadencebit));
    // memcpy(buffer+sizeof(cadencebit),&detectionbit,sizeof(detectionbit));
	//sprintf(buffer,"%.5f,%d");
    char msg[100];
	while(1)
	{
		while(lock == 0);
		lock = 0;
        float cadencebit = datas->cadence;
        int detectionbit = datas->detection;
        char buffer[100] = {0,};
        
        buffer[3] = *((int*)(&cadencebit)) ;
        buffer[2] = *((int*)(&cadencebit)) >> 8;
        buffer[1] = *((int*)(&cadencebit)) >> 16;
        buffer[0] = *((int*)(&cadencebit)) >> 24;

        buffer[7] = *((int*)(&detectionbit));
        buffer[6] = *((int*)(&detectionbit)) >> 8;
        buffer[5] = *((int*)(&detectionbit)) >> 16;
        buffer[4] = *((int*)(&detectionbit)) >> 24;
		//sleep(0.05);
			//send
            // send(*sock,&cadencebit,sizeof(cadencebit),0);
            // send(*sock,&detectionbit,sizeof(detectionbit),0);
            // for(int i = 0; i< 100; i++)
            //     printf("%d",msg[i]);
            // printf("\n");
        write(*sock,buffer,sizeof(buffer));
        // FILE* fd = sock;
        // fflush(fd);
        
        // fflush(*sock);
            // write(*sock,&detectionbit,sizeof(detectionbit));

		
		// write(*sock,msg,sizeof(msg));
		printf("Sent data to server : [%f,%d]\n",cadencebit,detectionbit);
	}
	close(*sock);
	printf("closing socket\n");
}
void* read_thd(void* data)
{
	Data* datas = data;
	char line[100];
    FILE* fp;
    char* tok;
    fp = popen("python3 ../rpi_road_object_detection/TFLite_detection_webcam_loop.py --modeldir=TFLite_model_bbd --output_path=processed_images","r");
    if (fp == NULL) {
        printf("Failed to run command\n" );
        exit(1);
    }
    while (fgets(line, sizeof(line), fp) != NULL)
    {
          // Read the output.
        int x;
        int y;
        strtok(line,",()");
        tok = strtok(NULL,",()");
        x = atoi(tok);
        tok = strtok(NULL,",()");
        y = atoi(tok);
        printf("C received %d, %d\n", x,y);
        datas->detection = ((x/10)*1000) + (y/10);
		lock = 1;
    }
        

        pclose(fp);  // close 

    return 0;
}
