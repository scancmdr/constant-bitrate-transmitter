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

/**
 * Abstraction for CBR transmitters
 * 
 * @author Jay Houghton
 * @see <a href="https://www.firebind.com/blog/">Firebind blog article for
 * more details</a>
 *
 */
public interface Transmitter {

  /**
   * Perform constant bitrate (CBR) streaming
   * 
   * @param parameters parameter set to use for transmission
   * @throws IOException when any network-related error occurs during transmit
   */
  void transmit(Parameters parameters) throws IOException;

}
