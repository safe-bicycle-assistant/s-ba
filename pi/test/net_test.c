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
static int GPIOExport(int pin)
{
	char buffer[BUFFER_MAX];
	ssize_t bytes_written;
	int fd;
	fd = open("/sys/class/gpio/export",O_WRONLY);
	if(-1 ==fd)
	{
		fprintf(stderr,"Failed to open export for writing!\n");
		return(-1);
	}
	bytes_written = snprintf(buffer,BUFFER_MAX,"%d",pin);
	write(fd,buffer,bytes_written);
	close(fd);
	return 0;
}

static int GPIOUnexport(int pin)
{
	char buffer[BUFFER_MAX];
	ssize_t bytes_written;
	int fd;
	fd = open("/sys/class/gpio/unexport",O_WRONLY);
	if(-1 == fd)
	{
		fprintf(stderr,"Failed to open unexport\n");
		return -1;
	}
	bytes_written = snprintf(buffer,BUFFER_MAX,"%d",pin);
	write(fd,buffer,bytes_written);
	close(fd);
	return 0;
}

static int GPIODirection(int pin, int dir)
{
	static const char s_directions_str[] = "in\0out";

	char path[DIRECTION_MAX]="/sys/class/gpio/gpio%d/direction";
	int fd;
	snprintf(path,DIRECTION_MAX,"/sys/class/gpio/gpio%d/direction",pin);
	fd = open(path,O_WRONLY);
	if(-1 == fd)
	{
		fprintf(stderr,"Failed to open\n");
		return -1;
	}
	if(-1== write(fd,&s_directions_str[IN==dir?0:3],IN == dir?2:3))
	{
		fprintf(stderr,"Failed to set dir\n");
		close(fd);
		return -1;
	}
	close(fd);
	return 0;
}

static int GPIOWrite(int pin,int value)
{
	static const char s_values_str[]="01";
	char path[VALUE_MAX];
	int fd;
	snprintf(path,VALUE_MAX,"/sys/class/gpio/gpio%d/value",pin);
	fd = open(path,O_WRONLY);
	if(-1 == fd)
	{
		fprintf(stderr,"Failed to open gpio value for writing\n");
		return -1;
	}
	if(1 != write(fd,&s_values_str[LOW == value? 0:1],1))
	{
		fprintf(stderr,"Failed to write value!\n");
		close(fd);
		return -1;
	}
	close(fd);
	return 0;
}

static int GPIORead(int pin)
{
	char path[VALUE_MAX];
	char value_str[3];
	int fd;
	snprintf(path,VALUE_MAX,"/sys/class/gpio/gpio%d/value",pin);
	fd = open(path, O_RDONLY);
	if(-1 == fd)
	{
		fprintf(stderr, "Failed to open gpio value for reading!\n");
		return (-1);
	}
	if(-1 == read(fd,value_str,3))
	{
		fprintf(stderr, "Failed to read value\n");
		return (-1);
	}
	close(fd);
	return(atoi(value_str));
}



typedef struct data{
    float cadence;
    int detection;
    
}Data;

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
    GPIOExport(17);
    sleep(0.5);
    GPIODirection(17,OUT);
    while(1)
    {
        // while(GPIORead(17) == 1);
        clock_t start = clock();
        while(GPIORead(17) == 1);
        while(GPIORead(17) == 0);
        clock_t end = clock();
        float duration = ((float)end-start)/CLOCKS_PER_SEC;
        float rpm = 50/duration;
        datas->cadence = rpm;
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
		sleep(0.3);
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
    }
        

        pclose(fp);  // close 

    return 0;
}
