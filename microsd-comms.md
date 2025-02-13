

This module is ideally suited to adding mass storage to your project letting you store your information within a micro SD card or read information from one. The module is designed for use with microcontrollers. In the case of the Arduino family, an SD library is available, please read the Arduino SD notes to prevent damage to your micro SD card or your Arduino.
Communication is handled through a 4 pin SPI interface, available on a wide range of microcontrollers. All required connections are fully labelled for easy connection.

![imagen](https://github.com/user-attachments/assets/2d517703-273e-46cf-9843-b3f56b074c7a)

![imagen](https://github.com/user-attachments/assets/b9d3420f-26ef-4e08-9700-2a3ea5d3ee13)

Truco para eperar al serial.

```c++
  while (!Serial) {
    ; // Wait for serial port to be available
  }
```
