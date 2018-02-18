// This #include statement was automatically added by the Particle IDE.
#include <ThingSpeak.h>

// This #include statement was automatically added by the Particle IDE.
#include <adxl362.h>

// This #include statement was automatically added by the Particle IDE.
#include <rht03-humidity-temperature-sensor.h>

// This #include statement was automatically added by the Particle IDE.

#include "cellular_hal.h"
#include "math.h"
// Set the TELUS APN
void Motion();
void distance();
void temperature();
void accelerometer();
float calcspeed(float& v0);
void vibration();
void readXYZ();
int distanceT=0;

STARTUP(cellular_credentials_set("isp.telus.com", "", "", NULL));
ADXL362 accel;
RHT03HumidityTemperatureSensor sensor(D5);
double temp;
char temper[10];

int16_t x,y,z,T;
float fx,fy,fz;
const float factor=136;
char result[10];
char speedR[10];

void setup() {
    pinMode(D0, INPUT_PULLDOWN);
    pinMode(D2, INPUT_PULLDOWN);
    //pinMode(D7, OUTPUT);
    Particle.keepAlive(30);
    accel.begin(SS);
    accel.beginMeasure();
     pinMode(D1, INPUT_PULLDOWN);
     pinMode(D7, OUTPUT);
}

void loop() {
    for(int i=0;i<10;i++){
        distance();
        accelerometer();

    }
    temperature();
    //delay(2000);
}


 
 void Motion() {
    //Motion
    if (digitalRead(D1) == HIGH) {
        Particle.publish("motion", "high", 60);  
        //while (digitalRead(D1) == HIGH); // wait for the sensor to return back to normal
    }
}    
 //Distance (infared)
void distance() {
     if (digitalRead(D0) == HIGH&&distanceT==0) {
        distanceT=1;
        digitalWrite(D7,HIGH);
        Particle.publish("distance", "close", 60);  
    }
    else if(distanceT==1&&digitalRead(D0)==LOW){
        distanceT=0;
        digitalWrite(D7,LOW);
        Particle.publish("distance", "far", 60);  
    }
}

void temperature() {
     //Temperature
    sensor.update();
    temp = sensor.getTemperature();
    snprintf(temper,sizeof temper, "%.2lf °C", temp);
    Particle.publish("Temperature", temper);
}
    //Accelerometer
void accelerometer() {
    //sprintf(result, "x: %f\ty: %f\tz: %f", fx, fy, fz);
    float speed=0, initialspeed = 0;
    for(int i = 0; i < 10; i++) {
        speed+=calcspeed(initialspeed);
        delay(10);
    }
 
    if(speed > 0) {
        //Particle.publish("Acceleration", result);
        snprintf(speedR, sizeof speedR, "%.2lf m/s", speed);
        Particle.publish("Speed", speedR);
    }
}

float calcspeed(float& v0) {
    readXYZ();
    float ac = sqrt(fx*fx+fy*fy); // Acceleration in m/s^2
    if(ac > 1.3 || ac < -1.3) {
        float speed = v0 + ac/100; 
        return v0 = speed;
    }
    return 0;
}

void readXYZ() {
    accel.readXYZTData(x, y, z, T);
    fx = x/factor, fy = y/factor, fz = z/factor;
}