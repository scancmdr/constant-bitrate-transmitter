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

import java.io.IOException;

import com.firebind.cbr.transmit.Parameters;
import com.firebind.cbr.transmit.SendCompareTransmitter;
import com.firebind.cbr.transmit.SendSleepTransmitter;
import com.firebind.cbr.transmit.Transmitter;

/**
 * Main program for running the different CBR transmitters
 * 
 * <p>Use the simple name of the transmitter class as the first command
 * line argument to select which approach to run. The rest of the arguments
 * specify the payload size, rate, duration, address, and port respectively.
 * </p> 
 * 
 * @author Jay Houghton
 */
public class Transmit {
  
  /**
   * Usage help text
   */
  final static String USAGE = "Usage: Transmit <SendCompareTransmitter|"
      +"SendSleepTransmitter|SendSleepDeficitTransmitter> "
      +"<datagramPayloadSizeBytes> <rateBitsPerSecondString> "
      +"<durationSecondsString> <targetAddress> <targetPort>";

  /**
   * @param args
   * @throws IOException 
   */
  public static void main(String[] args) throws IOException {
    
    // basic validation, could be much better
    if (args.length != 6) {
      System.out.println(USAGE);
      return;
    }
    
    String type = args[0];
    Parameters parameters = Parameters.fromStrings(args[1], args[2], args[3], 
                                                   args[4], args[5]);
    Transmitter transmitter;
    switch (type) {
    case "SendCompareTransmitter":
      transmitter = new SendCompareTransmitter();
      break;
    case "SendSleepTransmitter":
      transmitter = new SendSleepTransmitter();
      break;
    default:
      // unknown transmitter type
      System.out.println(USAGE);
      return;
    }
    transmitter.transmit(parameters);
  }

}
