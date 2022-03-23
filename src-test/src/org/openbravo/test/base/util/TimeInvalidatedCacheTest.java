/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2022 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.test.base.util;

import static org.junit.Assert.assertEquals;

import com.github.benmanes.caffeine.cache.Ticker;
import org.junit.Test;
import org.openbravo.base.util.TimeInvalidatedCache;

import java.time.Duration;

public class TimeInvalidatedCacheTest {
  @Test
  public void CacheShouldBeCorrectlyInitialized() {
    TimeInvalidatedCache<String, String> cache = TimeInvalidatedCache.newInstance()
        .expireAfterDuration(Duration.ofSeconds(5))
        .build(key -> "TestValue");
    assertEquals("TestValue", cache.get("testKey"));
  }

  @Test
  public void CacheShouldBeInvalidatedAndValueChange() {
    FakeTicker ticker = new FakeTicker();
    ValueTest.value = "oldValue";

    TimeInvalidatedCache<String, String> cache = TimeInvalidatedCache.newInstance()
        .expireAfterDuration(Duration.ofSeconds(5))
        .ticker(ticker)
        .build(key -> ValueTest.value);
    assertEquals("oldValue", cache.get("testKey"));
    ValueTest.value = "newValue";
    // Make sure second all to .get still gets the same value
    assertEquals("oldValue", cache.get("testKey"));

    ticker.advance(Duration.ofSeconds(5));
    assertEquals("newValue", cache.get("testKey"));
  }

  private static class ValueTest{
    public static String value;
  }

  /**
   * Fake ticker implementation that allows testing ticker sensitive cache. Includes a current time
   * in nanoseconds and allows advancing it by a given amount of time.
   */
  public class FakeTicker implements Ticker {
    // Current time, set in nanoseconds
    private long currentTime = 0;

    public FakeTicker() {
      super();
    }

    @Override
    public long read() {
      return currentTime;
    }

    /**
     * Advances the fake timer by a given duration. The duration is transformed into nanoseconds
     * 
     * @param duration
     *          - Duration to advance the fake timer
     */
    public void advance(Duration duration) {
      currentTime += duration.toNanos();
    }
  }
}
