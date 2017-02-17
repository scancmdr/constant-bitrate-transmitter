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

package com.firebind.cbr.sleep;

import java.util.concurrent.TimeUnit;

/**
 * Use the Thread.sleep() mechanism to pause execution on this thread
 * 
 * @author Jay Houghton
 * @see java.util.concurrent.TimeUnit#sleep(long)
 * @see java.lang.Thread#sleep(long, int)
 *
 */
public class ThreadSleepStrategy implements SleepStrategy {

    /* (non-Javadoc)
     * @see com.firebind.sleep.SleepStrategy#sleep(long)
     */
    @Override
    public void sleep(long nanoseconds) throws InterruptedException {
        if (nanoseconds > 0) {
            /* TimeUnit will automatically convert to millis and nanos */
            TimeUnit.NANOSECONDS.sleep(nanoseconds);
        }
    }

}
