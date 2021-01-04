# 2. Plugin Wrapper for Magnetic Card Reader

Having interfaced with your reader, you now need to provide a mechanism by which you can make use
of the data. There are methods by which this data can be accessed: locally via manually connecting to 
the host machine, statically sending data to a central server, or — the method we shall use — dynamically
making the data available to a larger system of connected devices. 

#### Background: Edge computing

To accomplish this we'll use [Cresco](https://github.com/CrescoEdge/quickstart), an 
[edge computing](https://en.wikipedia.org/wiki/Edge_computing) framework. In edge computing, you perform
functions — such as our card reading function — on "edge of network" devices where the data you want to make
use of exists. By networking devices together, you can share and make use of this data by many devices
simultaneously, as we'll see in this article series. 

---

## The wrapper boilerplate

Our second task is to wrap our reader function in boilerplate code that enables the Cresco framework to
interact with the data — in this case, the swipe data from our card reader. The goal of this module is to
package each swipe as an individual datagram to be acted on in some way. This is accomplished by creating 
a new [CardReaderTask](../reader/src/main/java/org/tsuyoi/edgecomp/reader/CardReaderTask.java) implementation, 
[PluginReaderClass](src/main/java/org/tsuyoi/edgecomp/preader/PluginReaderTask.java). This implementation
will translate and extract the stripe data from the swipe. Using this data, the wrapper function creates a 
[SwipeRecord](../common/src/main/java/org/tsuyoi/edgecomp/common/SwipeRecord.java) dataframe to broadcast to
other available functions and broadcasts it on a predefined channel from the 
[PluginStatics](../common/src/main/java/org/tsuyoi/edgecomp/common/PluginStatics.java) class.

### Building

To build our plugin JAR file, issue the following command in the `reader-plugin/` directory:
```bash
$ mvn clean package bundle:bundle
```
This will create the file `target/preader-1.0-SNAPSHOT.jar`, which is our plugin file to be used in
our Cresco deployment.

### Running our plugin in Cresco
Cresco is designed to allow for functions to be uploaded and run on-the-fly, allowing for a more agile
and robust distributed application system.