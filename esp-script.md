```cpp
// Library  for Wifi connection.
#include <WiFi.h>
// Library for HTTPS secure operations.
#include <WiFiClientSecure.h>
// Library for HTTP client operations (like requests).
#include <HTTPClient.h>
// Library for RS232 comms.
#include <HardwareSerial.h>
// Library that provides functions to interface with SD cards
#include <SD.h>
// Library for a lightweight file system designed to work with flash memory
#include <LittleFS.h>
// Includes the class DIR used to iterate through directories within the internal FS.
#include <FS.h>

// Definitions for WiFi connection
#define WifiSSID "nevertell"
#define WifiPassword "neversay"

// Definitions for HTTP Request.
#define serverTasksAddress "http://192.168.1.100:8085/server-events"

// RS232 Port definitions.
#define rxSR232Port 16
#define txSR232Port 17

// SD Card definitions.
#define sdCardMOSI 23
#define sdCardMISO 19
#define sdCardClock 18
#define sdCardChipSelect 5

// LED Status indicator definitions.
#define offlinePin 4
#define onlinePin 0
#define functionPin 2

// Debug configuration.
#define DEBUG 1

#if DEBUG == 1
#define debug(x) Serial.print(x)
#define debugln(x) Serial.println(x)
#else
#define debug(x)
#define debugln(x)
#endif

// Instantiate class for secure connections over SSL/TLS.
WiFiClientSecure client;

// SSL certificate filenames
String sdCertFilePath = "/test.txt";        // SSL certificate stored on the SD card
String littleFSCertFilePath = "/test.txt";  // The same SSL certificate to be stored in LittleFS

bool localCertificateExists = false;

bool cardIsMounted = false;

bool sdCertificateExists = false;

File localCertFile;
File sdCertFile;

void setup() {

  // INITIALIZE SERIAL MONITOR FOR DEBUG ----------------------------------------------------------------

  // Debug output monitor.
  Serial.begin(115200);
  while (!Serial) { ; }
  debugln("Serial monitor connected.");

  // CONNECT TO WIFI NETWORK ----------------------------------------------------------------------------

  // Connect to Wi-Fi
  debug("Connecting to WiFi.");
  WiFi.begin(WifiSSID, WifiPassword);
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.print(".");
  }
  // Indicate successful connection
  debugln("\nWiFi connected");
  debug("IP Address: ");
  debugln(WiFi.localIP());

  // INITIALIZE INTERNAL FILE SYSTEM  --------------------------------------------------------------------

  // Initialize LittleFS without formatting.
  if (!LittleFS.begin()) {

    debugln("INFO: Failed to mount LittleFS. Formatting now.");

    // Initialize trying to format.
    if (!LittleFS.begin(true)) {
      debugln("ERROR: Failed to mount LittleFS even after formatting.");
    } else {
      debugln("WARNING: LittleFS mounted after forced formatting.");
    }
  } else {

    debugln("INFO: LittleFS mounted successfully.");

    // Check for certificate files.
    localCertificateExists = LittleFS.exists(littleFSCertFilePath);

    if (localCertificateExists) {
      debugln("INFO: Certificate found in local file system.");
    } else {
      debugln("WARNING: Certificate NOT FOUND in internal file system.");
    }
  }

  // INITIALIZE SD CARD FILE SYSTEM  ---------------------------------------------------------------------

  // ...initialize the SD card.
  if (!SD.begin(sdCardChipSelect)) {
    debugln("ERROR: SD Card initialization failed.");
    sdCertificateExists = false;
  } 
  else {

    debugln("INFO: SD Card initialized successfully.");

    // Try to open the root directory to confirm the card is accessible
    File testFile = SD.open("/");
    if (!testFile) {
      debugln("ERROR: SD Card not accessible.");
    }
    else{
      testFile.close();
      // If we got here, the SD card is detected and accessible
      debugln("INFO: SD Card detected and accessible.");
    }

    //Check for certificate in SD Card.
    sdCertificateExists = SD.exists(sdCertFilePath);

    if (sdCertificateExists) {
      debugln("INFO: Certificate found in SD card.");
    } else {
      debugln("WARNING: Certificate NOT FOUND in SD card.");
    }
  }

  // CERTIFICATE PRESENCE CHECK BLOCK --------------------------------------------------------------------

  // Si el fichero existe...
  if (localCertificateExists) {

    // ... y existe un certificado en la SD...
    if (sdCertificateExists) {

      debugln("INFO: Reading SD Card certificate.");

      // ... comparar fechas y obtener el mas reciente.
      localCertFile = LittleFS.open(littleFSCertFilePath, "r");

      if (!localCertFile) { Serial.println("ERROR: Failed to open local certificate file."); }

      sdCertFile = SD.open(sdCertFilePath, "r");

      if (!sdCertFile) {
        Serial.println("ERROR: Failed to open SD certificate file.");
        localCertFile.close();
      }

      unsigned long localCertModStamp = localCertFile.getLastWrite();
      debugln("Local file timestamp: " + String(localCertModStamp));
      unsigned long sdCertModStamp = sdCertFile.getLastWrite();
      debugln("SD file timestamp: " + String(sdCertModStamp));

      localCertFile.close();
      sdCertFile.close();

      if (sdCertModStamp > localCertModStamp) {
        Serial.println("INFO: SD Certificate is more recent. Overwriting copy in local file system.");
        // Codigo de sobreescribir archivo.

        // Abre ficheros

        sdCertFile = SD.open(sdCertFilePath, "r");  // lectura

        if (!sdCertFile) {
          debugln("ERROR: Failed to open SD certificate file for copying.");
        }

        localCertFile = LittleFS.open(littleFSCertFilePath, "w");  // escritura

        if (!localCertFile) {
          debugln("ERROR: Failed to open local certificate file for writing.");
          sdCertFile.close();
        }

        // Read from the SD file and write to LittleFS file
        // practicamente siempre que haya una linea disponible en el certificado del SD
        while (sdCertFile.available()) {
          // la copiará en el certificado local.
          localCertFile.write(sdCertFile.read());
        }

        debugln("INFO: Certificate copied to local file system.");

        sdCertFile.close();
        localCertFile.close();

      } else if (sdCertModStamp < localCertModStamp) {
        debugln("INFO: Local file system certificate is more recent. No changes applied.");
        // ignorar certificado SD
      } else {
        debugln("INFO: Local certificate is up to date. No changes applied.");
        // ifnorar certificado SD
      }
    }
  }
  // Si el fichero no existe...
  else {
    // ... y la SD está montada. -> Copiar desde SD.
    if (sdCertificateExists) {
      debugln("INFO: No certificate found in local file system. Copying from SD Card the available copy.");

      sdCertFile = SD.open(sdCertFilePath, "r");  // lectura

      if (!sdCertFile) { debugln("ERROR: Failed to open SD certificate file for copying."); }

      localCertFile = LittleFS.open(littleFSCertFilePath, "w");  // escritura

      if (!localCertFile) {
        debugln("ERROR: Failed to open local certificate file for writing.");
        ;
      }

      // Read from the SD file and write to LittleFS file
      // practicamente siempre que haya una linea disponible en el certificado del SD
      while (sdCertFile.available()) {
        // la copiará en el certificado local.
        localCertFile.write(sdCertFile.read());
      }

      debugln("INFO: Certificate copied to local file system.");

      sdCertFile.close();
      localCertFile.close();

    } else {
      // ... y no está la SD montada -> ERROR.
      debugln("WARNING: NO CERTIFICATE IN LOCAL FILE SYSTEM AND NO FILE AVAILABLE FOR COPY.");
    }
  }
  // -----------------------
}



void loop() {
  // put your main code here, to run repeatedly:
}
```
