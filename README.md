# constant-bitrate-transmitter
This is an example implementation of the *Send and Compare* and *Send and Sleep* approaches to streaming data at a constant bitrate (CBR). The corresponding article for this code can be found here: https://www.firebind.com/blog/.

### Building

The requirements for building are:

1. Java 8 JDK (http://www.oracle.com/technetwork/java/javase/downloads/) 
2. ANT (http://ant.apache.org/). 

There are no other dependencies. The repository is a simple ANT project. Simply type `ant` in the project directory and the build will produce a runnable JAR called `constant-bitrate-transmitter.jar`

### Running

Once you have built the JAR file. Start the receiver that will measure the receive data rate, then start the transmitter. Of course you'll need to specify the location (IP address and port) of the receiver to the transmitter.

### The Receiver
There is a simple UDP receiver bundled as a runnable JAR file called `udp-receiver.jar`. Given an IP address and port it will open a datagram socket and read incoming UDP and measure the data rate. The data received is discarded immediately. After a period of 5 seconds with no incoming data it will print the data rate to `stdout` and continue to listen for another stream. You can terminate the program with a `CTRL-C` or by killing the process.

**Usage**
```
 java -jar receiver.jar <listen_address> <port>
```

**Example**
This example will listen on port 50001 on the loopback address.
```
 java -jar udp-receiver.jar 127.0.0.1 50001
```
**Output**
```
jay@spock:~$ java -jar udp-receiver.jar 127.0.0.1 50001
15:31:32.466 - listening on /127.0.0.1:50001 timeout=5000ms
15:31:35.197 - stream detected from /127.0.0.1:42375
15:31:50.137 - stream concluded at 15:31:45.136, overall rate: 651,466 bps
```


### The Transmitter
As described in the original article, the transmitter supports a number of approaches to achieving a constant bitrate. 

* `SendCompareTransmitter` - the *Send and Compare* approach, a popular algorithm used in many network tools.
* `SendSleepTransmitter` - An alternative approach that can be less CPU intensive.

**Usage**
To run the transmitter simply specify these arguments on the command line when running the jar:
1. Approach to use, either `SendCompareTransmitter` or `SendSleepTransmitter`
2. Datagram payload size in bytes
3. Data rate in bits per second
4. Duration to perform the overall transmission, after which it will terminate
5. IP address of the receiver
6. UDP port of the receiver

```
java -jar constant-bitrate-transmitter.jar <SendCompareTransmitter|SendSleepTransmitter> <datagramPayloadSizeBytes> <rateBitsPerSecondString> <durationSecondsString> <targetAddress> <targetPort> 
```

**Example**
```
java -jar constant-bitrate-transmitter.jar 200 10000 10 127.0.0.1 50001
```

**Output**
```
jay@spock:~$ java -jar constant-bitrate-transmitter.jar 200 10000 10 127.0.0.1 50001
Overall rate is 647,577 bps (101.2%) with 71 packets/cycle
```

Send any questions or comments to jay@firebind.com

