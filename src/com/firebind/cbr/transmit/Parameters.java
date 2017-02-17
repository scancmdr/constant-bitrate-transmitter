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

import java.net.InetSocketAddress;

/**
 * Data object for common parameters of CBR tests
 * 
 * @see <a href="https://www.firebind.com/blog/">Firebind blog article for
 * more details</a>
 * @author Jay Houghton
 *
 */
public class Parameters {
  
  /**
   * size of the datagram payload in bytes
   */
  protected final int datagramPayloadSizeBytes;
  
  /**
   * rate to maintain during test in bits per second
   */
  protected final long rateBitsPerSecond;
  
  /**
   * number of seconds to perform the transmit test
   */
  protected final int durationSeconds;
  
  /**
   * IP address and port to send the transmit stream to
   */
  protected final InetSocketAddress target;
  
  /**
   * Data object to hold common parameters for testing
   * 
   * @param datagramPayloadSizeBytes size of datagram payload in bytes
   * @param rateBitsPerSecond transmit rate in bits per second
   * @param durationSeconds duration of test in seconds
   * @param target IP address and port to transmit to
   */
  public Parameters(int datagramPayloadSizeBytes, 
                    long rateBitsPerSecond, 
                    int durationSeconds,
                    InetSocketAddress target) {
    super();
    this.datagramPayloadSizeBytes = datagramPayloadSizeBytes;
    this.rateBitsPerSecond = rateBitsPerSecond;
    this.durationSeconds = durationSeconds;
    this.target = target;
  }

  /**
   * @return datagram payload size in bytes
   */
  public int getDatagramPayloadSizeBytes() {
    return datagramPayloadSizeBytes;
  }

  /**
   * @return transmit rate in bits per second
   */
  public long getRateBitsPerSecond() {
    return rateBitsPerSecond;
  }

  /**
   * @return duration of test in seconds
   */
  public int getDurationSeconds() {
    return durationSeconds;
  }

  /**
   * @return IP address and port to send data to
   */
  public InetSocketAddress getTarget() {
    return target;
  }

  /**
   * @param datagramPayloadSizeBytesString datagram payload size in bytes
   * @param rateBitsPerSecondString transmit rate in bits per second
   * @param durationSecondsString duration of test in seconds
   * @param targetAddress IP address to send data to
   * @param targetPort Port to send data to
   * @return
   */
  public static Parameters fromStrings(String datagramPayloadSizeBytesString,
                                       String rateBitsPerSecondString, 
                                       String durationSecondsString,
                                       String targetAddress,
                                       String targetPort) {
    
    int datagramPayloadSizeBytes 
        = Integer.valueOf(datagramPayloadSizeBytesString);
    long rateBitsPerSecond = Long.parseLong(rateBitsPerSecondString);
    int durationSeconds = Integer.parseInt(durationSecondsString);
    InetSocketAddress target 
        = new InetSocketAddress(targetAddress,Integer.parseInt(targetPort));
    
    return new Parameters(datagramPayloadSizeBytes, 
                          rateBitsPerSecond, 
                          durationSeconds, 
                          target);
  }
}
