# Magnetic Card Reader
The first step to digitizing your attendance system is to interface with the identity of your users digitally. There are many likely ways this can be done, specific to your usecase, so this will be an example of how this was achieved in our usecase. This is a specific example meant to be included as part of a larger, edge computing-based solution.

### Setup



### Building
To build the .JAR file, use the following command:
```bash
$ mvn clean package
```
When the build completes successfully, you'll have a `target/CardReaderApp.jar`. To run the application, use the following command:

### Running
To run this module independently for testing, use the following command:
```bash
$ [sudo] java -jar target/CardReaderApp.jar
```
**NOTE: The reader interface requires root access to read, so you must either run the program as root or use _sudo_.**
