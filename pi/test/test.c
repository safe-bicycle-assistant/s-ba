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

void* write_thd(void* socket);
void* read_thd(void* socket);
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

int main(void)
{
    int i = 0;
    scanf("%d",&i);
    GPIOExport(i);
    sleep(0.5);
    GPIODirection(i,IN);
    while(1)
    {
        sleep(1);
        // while(GPIORead(17) == 1);
        clock_t start = clock();
        printf("%d\n",GPIORead(i));//(float)1.0;
        // printf("%d",GPIOExport(17));
        clock_t end = clock();
        // float duration = ((float)end-start)/CLOCKS_PER_SEC;
        // float rpm = 50/duration;
        // datas->cadence = a;
    }
}
