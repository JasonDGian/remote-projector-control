# ⛰️ Challenges encountered.
Here I will gather my discoveries and what i learn during my adventure with this new technology and project.

## 📍 Setting up the development environment.
Most guides online suggest to start using the Arduino IDE with the ESP32 and therefore this is what i did.
Before i could start programming the board I needed to get the ESP32 Board PAckage. 

To do that, click on the Board icon (`Board manager`) on the left panel.    
   
![imagen](https://github.com/user-attachments/assets/2ae11bce-a9cd-4083-b0d3-069a8a35cc16)

Make sure to choose the right option.   
   
![imagen](https://github.com/user-attachments/assets/61963493-aea2-4381-a567-e3e3cb0b3a40)

The IDE will start downloading the needed packages.    
   
![imagen](https://github.com/user-attachments/assets/bc720ce5-250e-4377-93d1-eccdc987b414)

Once installed the packages we will be able to select our board. **Remember to plug it in!**   
   
![imagen](https://github.com/user-attachments/assets/b3553df3-c3e5-4f34-b46d-4f2644b252d7)

Select the 'ESP32 Dev Module`.    
   
![imagen](https://github.com/user-attachments/assets/8c5fe0a7-f1ec-4814-9060-4cce1bd7fc60)

>[!CAUTION]
>On windows an additional step would be required:
>The USB to serial port chip of this control board is CP2102-GMR. So you need to install the driver for the chip.   
>You can click the driver tool download link:    
>https://www.silabs.com/products/development-tools/software/usb-to-uart-bridge-vcp-drivers

**Check if the driver for the board is installed.**    
On linux, many drivers come preinstalled with the Kernel, therefore before we start the installation process we should check if the needed driver for the board is already present.
In my case the board uses a CP2102-GMR, to check for the drivers in my kernel i used the following commands.
```bash
lsmod | grep cp201x
```
   
Giving me the postive result, the presence of this line means the drivers are currently installed.   
   
![imagen](https://github.com/user-attachments/assets/5c82aaed-f698-4756-971a-5bf2a21c0a31)

Afterwards I checked if the board was being recognized - also a positive result.

```bash
sudo dmesg | grep cp201x
```
    
![imagen](https://github.com/user-attachments/assets/971c7a81-e77c-482d-8f74-9b8e158052d5)
   
---
   
## 📍 Permission denied on output to board.
Since i work on a Linux Arch device and not a Microsoft Windows, i encountered a problem related to permissions to directly write on ports. This was a pain in the ass to diagnose and easy to resolve.
To cut it short, the solution was to add my user `jaydee` to the group that handles the direct access to the host ports. .

>[!NOTE]
>This configuration is necessary because the Arduino IDE in most cases cannot be run as a super user and even when possible is not a good idea to do so. 

#### 1 - Find out the port that requires access.
To find out  the port go to your arduino and look for the board, it will show the connection port.   

![imagen](https://github.com/user-attachments/assets/8cb9636f-0769-4113-a724-a3195c7b2abc)
   
#### 2 - Find the group that handles the port.
To to that type the following command in your terminal `ls -l <your port>`.
```bash
ls -l /dev/ttyUSB0
```

This will give the following result.
![imagen](https://github.com/user-attachments/assets/fdbd6b06-476e-46ec-a3b8-de3352950a82)

#### 3 - Add the user to the found group.
Now we just need to add our current user to the group that we have discovered.
To do this use the following syntax.
```bash
sudo usermod -a -G <gruop> <user>
```
#### 4 - Restart the computer to test the configuration.
The changes will not be effective until we reset our computer so this step is needed to make things work.

https://support.arduino.cc/hc/en-us/articles/360016495679-Fix-port-access-on-Linux

Restar to test the device.
```c++
int ledPin = 2; // Pin where the LED is connected

void setup() {
  // put your setup code here, to run once:
  pinMode(ledPin, OUTPUT);  // Set the LED pin as an output
  digitalWrite(ledPin, HIGH);  // Turn the LED on initially
}

void loop() {
  // put your main code here, to run repeatedly:
  digitalWrite(ledPin, HIGH);  // Turn the LED on
  delay(1000);  // Wait for 1 second (1000 milliseconds)
  
  digitalWrite(ledPin, LOW);  // Turn the LED off
  delay(1000);  // Wait for 1 second (1000 milliseconds)
}

```
   
---
   
## 📍 The IDE Serial monitor is not displaying characters correctly.
When wworking with output to the serial monitor for testing, the result was not what I expected. All I got was a mess of garbled characters.

![imagen](https://github.com/user-attachments/assets/23b68269-b40f-4c73-a408-21feac2c55e7)

Potential reasons for this issue were the desyncronized BAUD rate from the programme, chip and serial monitor. But in my case this had nothing to do with it.
To check the BAUD rate of my specific unity, i ran the command `sudo dmesg | grep tty`.    
   
![imagen](https://github.com/user-attachments/assets/0227d738-3ccf-43ec-bd60-c6b42f461615)

TURNS OUT, I only needed to push the 'EN' button on the board, which resets the board to make it work propperly.
   
---
   
## 📍 Connecting to the wifi network as a client.
To utilize the Wi-Fi capabilities, the device must first connect to a Wi-Fi network. To achieve this, I modified an example script to report connection status directly to the serial monitor. After making the necessary changes, I verified the device's successful connection by checking its presence on my router, ensuring the program and device were functioning correctly.
```c++
#include <WiFi.h>

void setup() {

  Serial.begin(115200);
  WiFi.begin("mySSID", "myPass");
  Serial.print("Connecting to WiFi");

  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(500);
  }

  Serial.print("\nConnected to the wifi.");
  Serial.print("\nIP ADDRESS: ");
  Serial.println(WiFi.localIP());
}

void loop() {

  if ((WiFi.status() == WL_CONNECTED)) {
    Serial.print("\nConnection online.");
    Serial.flush();
  } else {
    Serial.print("\nConnection lost.");
    Serial.flush();
  }
  delay(2000);
}
```

And once the serial monitor was configured i pushed the EN button and i got the expected result.    
   
![imagen](https://github.com/user-attachments/assets/c36515af-1969-4d9b-ab33-2c6f0eb9d82c)
   
---
   
## 📍 Finding the MAC address of the device.
Once the device was connected to my network, I simply searched for it among the devices listed in my Wireless LAN users.    
    
![imagen](https://github.com/user-attachments/assets/7ad795ea-f9d1-4880-b2ad-af1d1d1354ae)
