# IOT - TenniSense

## Description
This repository contains the entire codebase for a sensor for tennis rackets. <br>
This project contains code for two elements:
- An IMU sensor, that is to be attached to the throat of a tennis racket
- An accompanying andriod app that delivers statistics to the player regarding their athletic abilities.

The intended use of the product was for the player to record a session of himself/herself playing
tennis and the tennis player would interact with the app to receive statistics regarding the recorded
session. 


## Background
### Hardware Component
The hardware of the project consists of the following:
1. Tennis Racket
3. LSM303DLHC Accelerator & Magnetometer
4. L3GD20 Gyroscope
5. 9V Battery
6. ESP32 Arduino

The components were attached like so:
![image](https://github.com/user-attachments/assets/6f1e77cc-ea38-4b93-bd5f-7afac8849e62)

### Software Component
The software component consists of a frontend Android App and a backend python script. The
Android App serves as an interface with the sensor and allows the user to utilize the backend
python scripts which performs an analysis of a tennis session and outputs detailed statistics
regarding the tennis session.<br><br>
Screenshots of the app:
![image](https://github.com/user-attachments/assets/39713d94-34e5-402a-9c19-6e638fee9140)
![image](https://github.com/user-attachments/assets/64353232-d59e-4e77-b065-a054b9dde517)



## Demo
https://github.com/user-attachments/assets/ade98bc0-0949-4cd1-926b-d113f8834d64

