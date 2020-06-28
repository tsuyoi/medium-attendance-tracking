# 1. Magnetic Card Reader
The first step to digitizing your attendance system is to interface with the identity 
of your users digitally. There are many likely ways this can be done, specific to your 
usecase, so this will be an example of how this was achieved in our usecase. This is a 
specific example meant to be included as part of a larger, edge computing-based solution.

### Setup
This module designed to read the stripes of a magnetized card. A quick 
[Google search](https://www.google.com/search?q=HID+magnetic+card+reader) can provide a 
number of products that meet this description. For this project, we chose 
[a cheap version from Amazon](https://www.amazon.com/gp/product/B00E85TH9I/ref=ppx_yo_dt_b_asin_title_o07_s00) 
that works for our simple badges.

The only other tools you need are a magnetized card for testing and a computer with a USB port 
to compile and run the JAR file. **NOTE: These USB devices can run in either 
[HID](https://en.wikipedia.org/wiki/Human_interface_device) or keyboard emulation mode, but this 
code depends on using HID, so ensure the reader is attached to your selected computer in 
the appropriate mode.**

### The reader

For our reader, we will use three parts:
* A main class, [ReaderApp](src/main/java/org/tsuyoi/edgecomp/ReaderApp.java), that creates and runs an 
instance of our reader
* The reader class, [CardReader](src/main/java/org/tsuyoi/edgecomp/reader/CardReader.java), that controls 
connecting to the device and looping through reading the swipe data
* The task interface class, [CardReaderTask](src/main/java/org/tsuyoi/edgecomp/reader/CardReaderTask.java), that 
defines placeholders for actions to be performed during a card swipe

The ReaderApp is the entrypoint to the program. It instantiates the CardReader and starts it, beginning
the reading process. Each read process triggers the appropriate action defined in the CardReaderTask assigned.

Additionally, a simple CardReaderTask, [AppCardReaderTask](src/main/java/org/tsuyoi/edgecomp/AppCardReaderTask.java),
is provided to test and demonstrate reading and displaying swipe data.

### Building
To build the .JAR file, use the following command:
```bash
$ mvn clean package
```
When the build completes successfully, you'll have a `target/CardReaderApp.jar`. To run the application, 
use the following command:

### Running
To run this module independently for testing, use the following command:
```bash
$ [sudo] java -jar target/CardReaderApp.jar
```
**NOTE: The reader interface requires root access to read, so you must either run the program as root 
or use _sudo_.**

#### Scanning a card

Once the JAR file is running, it will check to see if the HID reader is available and attach to it. 
Success or failure will be displayed via the console. If the reader is successfully attached, you 
will be notified that the program is `Now reading...` along with notifications during each read loop. 
Once you see that message, swipe your card and the data stored in it should be displayed.  
