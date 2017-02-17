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

package com.firebind.cbr;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.Random;

/**
 * Utility routines
 * 
 * @author Jay Houghton
 */
public class Utils {

  /**
   * Fill a buffer with random bytes
   * 
   * @param buffer the buffer to fill with data
   */
  public static void fill(ByteBuffer buffer) {
    byte[] bytes = new byte[buffer.limit()];
    new Random().nextBytes(bytes);
    buffer.put(bytes).flip();
  }

  /**
   * @param number the number to format
   * @return a String representing a tenths format of number
   */
  public static String tenths(double number) {
    return String.format("%.1f", number);
  }
  
  
  static DecimalFormat COMMA_INTEGER_FORMAT = 
      new DecimalFormat("###,###,###,##0");
  /**
   * Not thread safe.
   * 
   * @param number the number to format
   * @return integer formatted with comma separator
   */
  public static String commaIntegerFormat(double number) {
      return COMMA_INTEGER_FORMAT.format(number);        
  }

  /**
   * Estimate the number datagrams we should send at once
   * http://stackoverflow.com/questions/7865069/how-to-find-the-socket-buffer-size-of-linux
   * 
   * @param datagramPayloadSizeBytes
   *          size of datagram payload in bytes
   * @return estimated number datagrams we can fit into a 16K buffer (default
   *         socket buffer size for Linux)
   */
  public static int estimateDatagramsPerCycleFullBuffer(
      int datagramPayloadSizeBytes) {
    return 16384 / (datagramPayloadSizeBytes + 8 + 20);
  }

  
  /**
   * Estimate the number of datagrams to send at once using a maximum period
   * 
   * @param datagramPayloadSizeBytes datagram payload size in bytes
   * @param rateBitsPerSecond data rate in bits per second
   * @param maximumPeriod maximum period (cycle time)
   * @return estimated number of datagrams
   */
  public static int estimateDatagramsPerCycleMaxPeriod(
      int datagramPayloadSizeBytes,
      long rateBitsPerSecond,
      double maximumPeriod) {
    return (int) ((((double) rateBitsPerSecond) * maximumPeriod) / 
        (8 * datagramPayloadSizeBytes));
  }

  /**
   * Straight calculation of period (cycle time) using the data rate and byte
   * count
   * 
   * @param datagramsPerCycle number of datagrams sent per cycle
   * @param datagramPayloadSizeBytes datagram payload size in bytes
   * @param rateBitsPerSecond data rate in bits per second
   * @return period (cycle time) in seconds
   */
  public static double calculatePeriod(double datagramsPerCycle, 
                                       double datagramPayloadSizeBytes,
                                       double rateBitsPerSecond) {
    return (datagramsPerCycle * datagramPayloadSizeBytes * 8) 
        / rateBitsPerSecond;
  }

  /**
   * Hybrid approach to estimate datagrams per cycle, if the straight 
   * calculation exceeds a maximum period value then recalculate using that
   * maximum period value as an upper bound.
   * 
   * @param datagramPayloadSizeBytes datagram payload size in bytes
   * @param rateBitsPerSecond data rate in bits per second
   * @param maximumPeriod maximum period (cycle time)
   * @return number of datagrams to send per cycle given payload, rate and 
   * maximum period
   */
  public static int calculateDatagramsPerCycle(int datagramPayloadSizeBytes, 
                                             long rateBitsPerSecond, 
                                             double maximumPeriod) {
    int datagramsPerCycle = 
        estimateDatagramsPerCycleFullBuffer(datagramPayloadSizeBytes);
    double period = calculatePeriod(datagramsPerCycle, 
                                    datagramPayloadSizeBytes, 
                                    rateBitsPerSecond);
    if (period > maximumPeriod) {
      datagramsPerCycle = 
          estimateDatagramsPerCycleMaxPeriod(datagramPayloadSizeBytes, 
                                                             rateBitsPerSecond,
                                                             maximumPeriod);
    }
    return datagramsPerCycle > 1 ? datagramsPerCycle : 1;
  }
  
  
  /**
   * Calculate data rate for bytes and nanoseconds
   * 
   * @param byteCount number of bytes transmitted
   * @param durationNano period of transmission in nanoseconds
   * @return data rate in bits per second
   */
  public static double calculateRate(double byteCount, double durationNano) {
    return (byteCount * 8) / (durationNano / 1000000000d);
  }
  
  /**
   * Calculate how accurate an observed rate matches an expected rate.
   * 
   * <p>This measurement can be less than or greater than 100%. When less than
   * 100 it means that the observed rate is lower than the expected, when more
   * than 100 it means the observed rate is higher than expected.
   * 
   * @param observedRate
   * @param expectedRate
   * @return percentage representing observed with respect to expected rate
   */
  public static double calculateAccuracy(double observedRate, 
                                         double expectedRate) {
    return (double) ((observedRate * 100d) / expectedRate);
  }

}
