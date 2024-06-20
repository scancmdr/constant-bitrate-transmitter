# Constant Bit Rate Transmission - An Adaptive Send and Sleep Approach
This is an example implementation of the *Send and Compare* and *Send and Sleep* approaches to streaming data at a constant bitrate (CBR). See the [USAGE](USAGE.md) for how to build and run.

Constant Bit Rate (CBR) transmission is sending a set of data at a constant transmission rate (measured in bits per second). CBR an important concept for dealing with data compression and network-based concerns such as streaming media. Of particular importance is real-time media and a network’s ability to support a data rate that allows for high quality voice and video. 

Firebind employs UDP CBR transmission as part of simulating a variety of real-time traffic such
as those for voice and video (e.g. VOIP, MPEG4). There are various approaches for simulating a CBR stream including bucket algorithms and packet scheduling (such as NS3​[^1]​).

Another approach is ​ _Send and Sleep_ as will be presented here. The basic principle is that we send a calculated number of datagrams then pause (sleep) just long enough such that the entire duration of the send and sleep equal the period of one cycle of the corresponding data rate. This approach requires calculating the period associated with a data rate.

Given a desired data rate and datagram payload size we can calculate a period (cycle time) and datagram count that need to be sent (per cycle) and then pause (sleep) for the remaining time in the cycle. The pseudo-code looks like this:

```pseudocode
packets_per_cycle = 1
bits_per_cycle = payload_size * packets_per_cycle * 8
period = bits_per_cycle / data_rate
while (not done) {
  send_start_time = now()
  send(datagrams)
  send_duration = now() - send_start_time
  sleep(period - send_time)
}
```

The inputs for this approach are the:

- **Data Rate** ​ - Rate to perform CBR stream, in bits per second (bps).
- **Payload Size** ​ - Size of UDP datagram payload in bytes. Typical values are below the MTU and chosen based on the source of the traffic. Some typical VOIP payloads average around 200 bytes.

Not shown above are possible inputs that would bound the CBR streaming activity. An overall duration or maximum byte count could be employed to stop transmission. In practice a loop like this would be fed by a queue or buffer with content ready to transmitted (e.g. from a packet generator, or input device driver).

The ​ **Packets Per Cycle** ​ variable drives the period sizing. A small packet count here ensures a desired small period for slow data rates (in the 10k bps range). For example, a 10k bps data rate with 16K bytes worth of packets per cycle results in a 13 second period ( 8 * 16K bytes / 10k). So depending upon your traffic pattern a maximum period value may be in order. The average period should be below a second for VOIP applications, although there could be cases with limited computing power that may require larger periods.

For this example and test results I’ve used a value of 1 packet per cycle. Increasing this value to fill the socket send buffer will yield only mildly better results for the higher data rates. To use a 16K byte socket buffer (default on Linux) you can do this:

```
packets_per_cycle = 16384 / (payload_size + 8 + 20)
```

This is only an estimate as the size of the IP and UDP headers can vary. The sample code on Github uses a more sophisticated calculation that puts a ceiling on the period for small data rates.

Values derived from these inputs are

- **Bits Per Cycle** ​ - The number of bits to be sent per cycle.
- **Period** ​ - The average duration a cycle should take to achieve data rate.

**Testing**
A full Java-based implementation for UDP is available in this repository here. Using this code and a laboratory test rig comprised of two 64-bit Ubuntu 14.04.4 LTS machines (kernel version 3.13.0-86), one of these being the transmitter and the other the receiver. These both have GigE network interfaces connected by a GigE switch (Xytel).

Given this we can try a few data rates and observe the performance:

| Rate (bps)     | Packet Size (bytes) | packets/cycle | Result (bps) | Accuracy |
| -------------- | ------------------- | ------------- | ------------ | -------- |
| 10,000         | 200                 | 1             | 10,161       | 98.39%   |
| 1,000,000      | 200                 | 71            | 1,011,399    | 98.86%   |
| 100,000,000    | 200                 | 71            | 99,934,183   | 99.93%   |
| 1,000,000,000  | 200                 | 71            | 726,826,893  | 72.68%   |
| 1,000,000,000  | 1200                | 13            | 939,121,731  | 93.91%   |
| 10,000,000,000 | 1200                | 13            | 939,231,796  | 9.39%    |

Some notes about these results:

- These were obtained by taking the middle value of 3 runs of the CBR transmitter each of overall duration of 10s.
- The result column is the rate observed at the receiver. The actual transmit rate as measured by the transmitter is almost always of higher accuracy (especially for UDP).
- The Packets Per Cycle number was calculated using a formula involving a maximum period constraint.
- The maximum efficiency of a GigE Ethernet link is around 94%​[^2]​. We can see the receiver-side rate measurement for 1200 byte packets is approaching this value.

We can see here that this algorithm can perform well if tuned. Results for 1 Gbps with 200 byte packets are interesting. Results for 10 Gbps are understandable given the GigE link is only capable of 1 Gbps.

This is of course a simplification of the algorithm. For a practical implementation of this there are some things to consider:

- Timestamp Resolution - A timestamp that is small enough for larger data rates. For example a 100M bps rate with 16K bytes worth of packets per cycle results in a 1.3 ms period (8*16K/100M). A timer resolution below a millisecond is required here. Rates in the area of 100 Gbps will require nanosecond resolution.
- Packets Per Cycle - How to calculate the amount of data to send per cycle.
- Buffer Effects - sizing application and socket buffers to optimize transmit at target data rate.

**Adaptive Behavior**
The algorithm can already adapt to sub-period variations in send duration. What if the call to send takes longer than a period? This behavior can happen during buffer fill and flush operations but may be transient (e.g. bursty). To accommodate this we can dynamically adjust the period using a deficit counter. Basically, add to this deficit counter when we know we’re working overtime during the send call.

Repeat this process, using an adaptive approach to constantly adjust the sleep time to iteratively correct the data rate.

```pseudocode
packets_per_cycle = 1
bits_per_cycle = payload_size * packets_per_cycle * 8
period = bits_per_cycle / data_rate
while (not done) {
  send_start_time = now()
  send(datagrams)
  send_duration = now() - send_start_time
  if (send_duration > period) {
    sleep_time = 0
    deficit += send_duration - period
  } else {
    sleep_time = period - send_duration
    if (deficit > 0) {
      if (deficit > sleep_time) {
        deficit -= sleep_time
        sleep_time = 0
      } else {
        sleep_time -= deficit
        deficit = 0
      }
    }
  }
  sleep(sleep_time)
}
```

This technique can also be applied to systems with imprecise sleep timers (such as Java’s Thread.sleep() call). Measure the sleep duration and adjust the deficit if you overslept.

**Other Thoughts**
Some things to consider here are:

- There are several buffers that a CBR stream will encounter along its way to the destination. There isn’t much control  to be had on how these will affect your data rate asmany of them can be on ISP and backbone network gear.
- Datagram size and how many datagrams are being sent at once (application buffering) can influence a buffers  decision to flush now or later. 
- Memory models for any language or VM can impose their own buffering (e.g. Java’s non-direct ByteBuffer or other virtualization layers).
- Socket buffer sizing can sometimes be adjusted either by the application or OS settings. Be sure to consider both socket buffers: the send buffer on source machine and the receive buffer on target machine. Typical defaults are in the neighborhood of 16k bytes and so the examples here are based on that.
- Variable Payload Size 

Firebind employs a similar mechanism for the synthetic voice testing. When combined with the Firebind Protocol Script the CBR stream can carry any payload such as G.711 encoded voice. This approach results in network traffic that is indistinguishable from a real VOIP call, by either content or transmission rate.

References
[^1]: https://www.nsnam.org/ns-3-26/​ see `onoff-application.cc` (OnOffApplication::ScheduleNextTx() - schedules next packet on a size divided by rate basis)
[^2]: http://rickardnobel.se/actual-throughput-on-gigabit-ethernet/



