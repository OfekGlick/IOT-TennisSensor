
#include "BluetoothSerial.h"
#include <Wire.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_LSM303_U.h>
#include <Adafruit_L3GD20_U.h>

Adafruit_LSM303_Accel_Unified accel = Adafruit_LSM303_Accel_Unified(54321);
Adafruit_L3GD20_Unified gyro = Adafruit_L3GD20_Unified(20);
Adafruit_LSM303_Mag_Unified mag = Adafruit_LSM303_Mag_Unified(12345);

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif
float time_zero = millis();
BluetoothSerial SerialBT;



void setup()
{
  Serial.begin(115200);
  SerialBT.begin("King Matan's Device"); //Bluetooth device name
  
  time_zero = millis();
  #ifndef ESP8266
  while (!Serial);     // will pause Zero, Leonardo, etc until serial console opens
  #endif
//
  mag.enableAutoRange(true);
  gyro.enableAutoRange(true);

  /* Initialise the sensor */
  if(!accel.begin())
  {
    /* There was a problem detecting the ADXL345 ... check your connections */
    Serial.println("Ooops, no LSM303 detected ... Check your wiring!");
    while(1);
  }
 if(!mag.begin())
  {
    /* There was a problem detecting the LSM303 ... check your connections */
    Serial.println("Ooops, no LSM303 detected ... Check your wiring!");
    while(1);
  }
  
  /* Initialise the sensor */
  if(!gyro.begin())
  {
    /* There was a problem detecting the L3GD20 ... check your connections */
    Serial.println("Ooops, no L3GD20 detected ... Check your wiring!");
    while(1);
  }  
}

void loop()
{
  
  /* Get a new sensor event */
  sensors_event_t event;
  
  float my_time = (millis() - time_zero)/1000;
  float x_acc=0;
  float y_acc=0;
  float z_acc=0;

  float x_gyro=0;
  float y_gyro=0;
  float z_gyro=0;

  float x_mag=0;
  float y_mag=0;
  float z_mag=0;
  int times = 4;
  for (int i=0; i<times; i++){
    accel.getEvent(&event);
    x_acc +=event.acceleration.x;
    y_acc +=event.acceleration.y;
    z_acc +=event.acceleration.z;

    gyro.getEvent(&event);
    x_gyro +=event.gyro.x;
    y_gyro +=event.gyro.y;
    z_gyro +=event.gyro.z;

    mag.getEvent(&event);
    x_mag +=event.magnetic.x;
    y_mag +=event.magnetic.y;
    z_mag +=event.magnetic.z;
    delay(5);
  }
  Serial.print((String)my_time + "," + (String)(x_acc/times) + "," + (String)(y_acc/times) + "," + (String)(z_acc/times)+ ","); 
  Serial.print((String)(x_gyro/times) + "," + (String)(y_gyro/times) + "," + (String)(z_gyro/times)+ ","); 
  Serial.println((String)(x_mag/times) + "," + (String)(y_mag/times) + "," + (String)(z_mag/times)); 
  
  SerialBT.print((String)my_time + "," + (String)(x_acc/times) + "," + (String)(y_acc/times) + "," + (String)(z_acc/times)+ "," + (String)(x_gyro/times) + "," + (String)(y_gyro/times) + "," + (String)(z_gyro/times)+ "," + (String)(x_mag/times) + "," + (String)(y_mag/times) + "," + (String)(z_mag/times)); 


}
