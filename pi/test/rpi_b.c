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
#define DELAY 10000
#define BUFFER_MAX 3
#define DIRECTION_MAX 45

#define IN 0
#define OUT 1
#define PWM 0
#define LOW 0
#define HIGH 1
#define VALUE_MAX 256

#include <wiringPiI2C.h>
#include <wiringPi.h>
#include <linux/i2c-dev.h>
#include <sys/ioctl.h>
#define i2c_addr 0x3F
#define LCD_WIDTH 16
#define LCD_CHR 1
#define LCD_CMD 0
#define LINE_1 0x80
#define LINE_2 0xC0

#define LCD_ON 0x08
#define ENABLE 0b00000100
#define E_delay 0.0005
#define E_pulse 0.0005


#define DELIM ",\n"


static int PWMExport(int pwmnum)
{
	#define BUFFER_MAX 3
	char buffer[BUFFER_MAX];
	int bytes_written;
	int fd;

	fd = open("/sys/class/pwm/pwmchip0/unexport",O_WRONLY);
	if(-1 == fd)
	{
		fprintf(stderr, "Failed to open in unexport!\n");
		return (-1);
	}

	bytes_written = snprintf(buffer, BUFFER_MAX, "%d",pwmnum);
	write(fd,buffer,bytes_written);
	close(fd);
	sleep(1);
	fd = open("/sys/class/pwm/pwmchip0/export",O_WRONLY);
	if(-1 == fd)
	{
		fprintf(stderr, "Failed to open in export!\n");
		return (-1);
	}
	bytes_written = snprintf(buffer, BUFFER_MAX, "%d",pwmnum);
	write(fd,buffer,bytes_written);
	close(fd);
	sleep(1);
	return(0);
}

static int PWMUnexport(int pwmnum)
{
	char buffer[BUFFER_MAX];
	ssize_t bytes_written;
	int fd = open("/sys/class/pwm/pwmchip0/unexport",O_WRONLY);
	if(-1 == fd)
	{
		fprintf(stderr,"Failed to open in unexport\n");
		return (-1);
	}
	bytes_written = snprintf(buffer,BUFFER_MAX,"%d",pwmnum);
	write(fd,buffer,bytes_written);
	close(fd);
	sleep(1);
	return 0;
}

static int PWMEnable(int pwmnum)
{
	static const char s_unenable_str[] = "0";
	static const char s_enable_str[] = "1";

//#define DIRECTION_MAX 45
	char path[DIRECTION_MAX];
	int fd;
	snprintf(path,DIRECTION_MAX,"/sys/class/pwm/pwmchip0/pwm%d/enable",pwmnum);
	fd = open(path, O_WRONLY);
	if(-1 ==fd)
	{
		fprintf(stderr,"Failed to open in enable!\n");
		return (-1);
	}
	write(fd,s_unenable_str,strlen(s_unenable_str));
	close(fd);
	fd = open(path, O_WRONLY);
	if( -1 == fd)
	{
		fprintf(stderr, "Failed to open in enable2!\n");
		return (-1);
	}
	write(fd, s_enable_str,strlen(s_enable_str));
	close(fd);
	return 0;
}

static int PWMWritePeriod(int pwmnum, int value)
{
	char s_values_str[VALUE_MAX];
	char path[VALUE_MAX];
	int fd,byte;
	snprintf(path, VALUE_MAX,"/sys/class/pwm/pwmchip0/pwm%d/period",pwmnum);
	fd = open(path, O_WRONLY);
	if(-1 == fd)
	{
		fprintf(stderr, "period1 error\n");
		return(-1);

	}
	byte = snprintf(s_values_str,VALUE_MAX,"%d",value);
	if(-1 == write(fd, s_values_str,byte))
	{
		fprintf(stderr,"period2 error\n");
		close(fd);
		return (-1);
	}
	close(fd);
	return 0;
}

static int PWMWriteDutyCycle(int pwmnum, int value)
{
	char path[VALUE_MAX];
	char s_values_str[VALUE_MAX];
	int fd,byte;
	snprintf(path,VALUE_MAX,"/sys/class/pwm/pwmchip0/pwm%d/duty_cycle",pwmnum);
	fd = open(path, O_WRONLY);
	if(-1 == fd)
	{
		fprintf(stderr,"duty_cycle error1\n");
		return (-1);
	}
	byte = snprintf(s_values_str,VALUE_MAX,"%d",value);
	if(-1 == write(fd , s_values_str,byte))
	{
		fprintf(stderr, "duty_cycle error2\n");
		close(fd);
		return (-1);
	}
	close(fd);
	return 0;
}

static int PWMUnable(int pwmnum)
{
	static const char s_unable_str[] = "0";
	char path[DIRECTION_MAX];
	int fd;
	snprintf(path,DIRECTION_MAX,"/sys/class/pwm/pwmchip0/pwm%d/enable",pwmnum);
	fd = open(path, O_WRONLY);
	if(-1 == fd)
	{
		fprintf(stderr,"Failed to open in unable\n");
		return (-1);
	}
	write(fd, s_unable_str,strlen(s_unable_str));
	close(fd);
	return 0;
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


void error_handling(char *message)
{
	fputs(message,stderr);
	fputc('\n',stderr);
	exit(1);
}

int file_i2c;
int length = 0;
static int WRITE_BYTES(int addr, int output)
{
	length = 1;
	ioctl(file_i2c, 0, i2c_addr);
	if(write(file_i2c,&output,length)!=length)
	{
		printf("failed to write to i2c bus\n");
	}
	return 0;
}
static void lcd_toggle_enable(int bits)
{
	usleep(E_delay);
	wiringPiI2CReadReg8(file_i2c,(bits|ENABLE));
	usleep(E_pulse);
	wiringPiI2CReadReg8(file_i2c,(bits & ~ENABLE));
	usleep(E_delay);
}
static void LCD_BYTE(int bits, int mode)
{
	unsigned int bits_high = mode | (bits & 0xF0) | LCD_ON;
	unsigned int bits_low = mode | ((bits << 4)&0xF0)|LCD_ON;
	wiringPiI2CReadReg8(file_i2c,bits_high);
	lcd_toggle_enable(bits_high);
	wiringPiI2CReadReg8(file_i2c,bits_low);
	lcd_toggle_enable(bits_low);

}
static void LCD_INIT()
{
	LCD_BYTE(0x33,LCD_CMD);
	LCD_BYTE(0x32,LCD_CMD);
	LCD_BYTE(0x06,LCD_CMD);
	LCD_BYTE(0x0C,LCD_CMD);
	LCD_BYTE(0x28,LCD_CMD);
	LCD_BYTE(0x01,LCD_CMD);
	usleep(E_delay);
}
static void print_lcd(char buffer[], int line)
{
	LCD_BYTE(line, LCD_CMD);
	char value;
	for(int i = 0;i<LCD_WIDTH;i++)
	{
		if(i>=strlen(buffer))
				value = ' ';
	
		else 
				value= (char)buffer[i];
		LCD_BYTE(value,LCD_CHR);
	}
}



int main(void)
{
	file_i2c = wiringPiI2CSetup(0x3F);
	LCD_INIT();
	LCD_BYTE(0x01,LCD_CMD);
	LCD_INIT();
	PWMExport(0);
	PWMExport(1);
	GPIOExport(17);
	sleep(1);
	PWMWritePeriod(0,20000000);
	PWMWritePeriod(1,20000000);
	PWMWriteDutyCycle(0,500000);
	PWMWriteDutyCycle(1,1400000);
	PWMEnable(0);
	PWMEnable(1);
	GPIODirection(17,OUT);
	char addr[]="192.168.30.3";
	char port[]="8082";
	int strlen;
	char* temp;
	char lcd1[16];
	char lcd2[16];
	char buffer[60];
	int PWMR;
	int PWML;
	struct sockaddr_in serv_addr;
	int sock = socket(PF_INET,SOCK_STREAM,0);
	int LEDstat;
	if(sock==-1)
			printf("socket error\n");
	memset(&serv_addr,0,sizeof(serv_addr));
	serv_addr.sin_family = AF_INET;
	serv_addr.sin_addr.s_addr = inet_addr(addr);
	serv_addr.sin_port = htons(atoi(port));
	if(connect(sock,(struct sockaddr*)&serv_addr,sizeof(serv_addr))==-1)
		printf("connect() error\n");
	int i = 0;
	while(1)
	{
		strlen = read(sock,buffer,sizeof(buffer));
		printf("%s\n",buffer);
		temp = strtok(buffer,DELIM);
		PWML = atoi(temp);
		temp = strtok(NULL,DELIM);
		PWMR = atoi(temp);
		temp = strtok(NULL,DELIM);
		//printf("1");
		strcpy(lcd1,temp);
		//printf("2");
		temp = strtok(NULL,DELIM);

		strcpy(lcd2,temp);
		temp = strtok(NULL,DELIM);
		LEDstat = atoi(temp);
		GPIOWrite(17,LEDstat);
		printf("lcd1 : %s\n",lcd1);
		printf("lcd2 : %s\n",lcd2);
		//i = 0;
		printf("%d\n",i);
		i++;
		print_lcd(lcd1,LINE_1);
		print_lcd(lcd2,LINE_2);
		PWMWriteDutyCycle(0,PWMR);
		PWMWriteDutyCycle(1,PWML);
		usleep(2000);
	}
}
