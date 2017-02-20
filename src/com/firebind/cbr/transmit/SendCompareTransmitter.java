/*
 * Copyright (C) 2017 Firebind Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.firebind.cbr.transmit;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import com.firebind.cbr.Utils;

/**
 * An implementation of the Send and Compare approach to CBR streaming. The
 * pseudocode for this approach looks like this:
 * <pre>
 *   start_time = now()
 *   while (not done) {
 *     rate = byte_count * 8 / (now() - start_time) / 10^9
 *     if (rate < target_rate) {
 *       byte_count = send(datagrams)
 *     }
 *   }
 * </pre>
 * 
 * @see <a href="https://www.firebind.com/blog/">Firebind blog article for
 * more details</a>
 * @author Jay Houghton
 *
 */
public class SendCompareTransmitter implements Transmitter {

  /**
   * configuration parameters for this transmitter
   */
  protected Parameters parameters;
  
  /**
   * datagrams to send per period
   */
  protected int datagramsPerCycle;

  /**
   * I/O channel for transmitting
   */
  protected DatagramChannel channel;
  
  /**
   * working buffer, contains exactly one datagram
   */
  protected ByteBuffer buffer;

  /* (non-Javadoc)
   * @see 
   * com.firebind.cbr.transmit.Transmitter#transmit(com.firebind.cbr.transmit.Parameters)
   */
  @Override
  public void transmit(Parameters parameters) throws IOException {
    this.parameters = parameters;
    try {
      setup();
      perform();
    } finally {
      teardown();
    }
  }

  /**
   * Calculate our data sizing, setup UDP channel and initialize a send buffer.
   * @throws IOException 
   */
  protected void setup() throws IOException {
    datagramsPerCycle = Utils.calculateDatagramsPerCycle(
        parameters.getDatagramPayloadSizeBytes(), 
        parameters.getRateBitsPerSecond(), 0.250);
    channel = DatagramChannel.open();
    channel.configureBlocking(false);

    /*
     * Size our transmit buffer to our datagram size. You may choose to use a
     * direct buffer for possible better performance at higher transmit rates,
     * However be aware that direct buffers are usually allocated off-heap and
     * hence subject to special treatment by the garbage collector.
     * 
     */
    buffer = ByteBuffer.allocate(parameters.getDatagramPayloadSizeBytes());
    Utils.fill(buffer); // initialize with your favorite payload
  }

  /**
   * Perform the Send and Compare approach to CBR streaming
   * 
   * @throws IOException when an error occurs during networking
   */
  protected void perform() throws IOException {

    long byteCount = 0;
    long transmitterStart = System.currentTimeMillis();
    long transmitterEndTime 
        = transmitterStart + parameters.getDurationSeconds() * 1000;
    long startTime = System.nanoTime();

    while (System.currentTimeMillis() < transmitterEndTime) {
      /*
       * calculate our instant rate over the entire transmit
       * duration
       */
      double rate = ((double) (byteCount * 8)) 
          / ((double) (System.nanoTime() - startTime) / 1000000000d);
                                                                                                           
      // here is the rate comparison, send only if we're below target rate
      if (rate < parameters.getRateBitsPerSecond()) {
        for (int datagram = 0; datagram < datagramsPerCycle; datagram++) {
          
          /*
           * If there is sufficient room in the underlying send buffer, then 
           * the bytes in the buffer are transmitted as a single datagram. 
           * Which means there is no guarantee that a datagram will be sent. 
           * So aggressively (repeatedly) try to send the datagram, eventually
           * the send buffer will have room. We could check for zero bytes 
           * sent but its just as easy to use the buffer mechanics.
           */
          while (buffer.hasRemaining()) {
            byteCount += channel.send(buffer, parameters.getTarget());
          }
          buffer.flip(); // flip only because buffer is 100% drained
          // optionally refill buffer here to send different data
        }
      }

    }

    long stopTime = System.nanoTime();
    long transmitterDuration = stopTime - startTime;
    
    double overallRate = 
        Utils.calculateRate(byteCount, transmitterDuration);
    
    // accuracy is the observed rate with respect to configured rate (percent)
    double overallAccuracy = 
       Utils.calculateAccuracy(overallRate, parameters.getRateBitsPerSecond());
    
    // basic output for results, real result is at the receiver
    System.out.println("Overall rate is " 
        + Utils.commaIntegerFormat(overallRate) + " bps ("
        + Utils.tenths(overallAccuracy)
        + "%) with " + datagramsPerCycle + " packets/cycle");
  }

  /**
   * Teardown and close out our resources
   * 
   * @throws IOException when an error during channel close occurs
   */
  protected void teardown() throws IOException {
    buffer.clear();
    channel.close();
  }

}
