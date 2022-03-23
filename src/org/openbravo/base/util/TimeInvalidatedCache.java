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
package org.openbravo.base.util;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.Ticker;
import org.openbravo.base.exception.OBException;

/**
 * Cache API that allows creating a cache that will be invalidated after a period of time
 * 
 * This Cache will be invalidated after a period x of time after a write action on the cache due to
 * a read key. That means this cache is lazy, it will only compute a value of a given key if this
 * key is accessed.
 * 
 * Use the newInstance static function to create a new cache and then follow their builder
 * structure.
 * 
 * @param <T>
 *          - Key used by the Cache(for example String for a UUID)
 * @param <V>
 *          - Value used by the Cache
 */
public class TimeInvalidatedCache<T, V> {
  private LoadingCache<T, V> cache;
  private Duration expireDuration;
  // Internal ticker, do not use
  private Ticker ticker;

  /**
   * Creates a new instance of TimeInvalidatedCache with the types provided. Usage as follows:
   * 
   * <pre>
   * TimeInvalidatedCache<KeyType, ValueType>
   *   .newInstance()
   *   .expireAfterDuration(Duration.ofMinutes(5)) // Could be any Duration, not necessarily in minutes. If not executed, 1 minute default is assumed
   *   .build(key -> generateKeyValue(key)) // This is a lambda that initializes the key if it expired or is the first time is read
   * </pre>
   * 
   * @return this object, to be followed by an expireAfterDuration or build calls.
   */
  public static TimeInvalidatedCache<Object, Object> newInstance() {
    return new TimeInvalidatedCache<>();
  }

  /**
   * Sets the expiration duration, after this period the key is considered expired and reloaded on
   * the next access
   * 
   * @param duration
   *          - Duration of time after which is considered expired
   * @return this object, to follow with build() call
   */
  public TimeInvalidatedCache<T, V> expireAfterDuration(Duration duration) {
    this.expireDuration = duration;
    return this;
  }

  /**
   * Builds the TimeInvalidatedCache and initializes it. Expects to be called as follows:
   *
   * <pre>
   * TimeInvalidatedCache<KeyType, ValueType>
   *   .newInstance()
   *   .expireAfterDuration(Duration.ofMinutes(5)) // Could be any Duration, not necessarily in minutes. If not executed, 1 minute default is assumed
   *   .build(key -> generateKeyValue(key)) // This is a lambda that initializes the key if it expired or is the first time is read
   * </pre>
   * 
   * @param loader
   *          - lambda that initializes the key if it expired or is the first time it is read. It
   *          should receive a key and return the value corresponding to it
   * @return this object
   */
  public <T1 extends T, V1 extends V> TimeInvalidatedCache<T1, V1> build(
      CacheLoader<? super T1, V1> loader) {
    if (expireDuration == null) {
      this.expireDuration = Duration.ofMinutes(1);
    }
    @SuppressWarnings("unchecked")
    TimeInvalidatedCache<T1, V1> self = (TimeInvalidatedCache<T1, V1>) this;
    Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder();
    cacheBuilder.expireAfterWrite(expireDuration);
    if (this.ticker != null) {
      cacheBuilder.ticker(ticker);
    }
    self.cache = cacheBuilder.build(loader);

    return self;
  }

  private TimeInvalidatedCache() {
  }

  /**
   * Returns a value from the cache associated to a given key
   * 
   * @param key
   *          - Key to retrieve corresponding value
   * @return - Value corresponding to given key
   */
  public V get(T key) {
    checkCacheBuilt();
    return cache.get(key);
  }

  /**
   * Returns a value from the cache associated to a given key. If the key is not in the cache and is
   * not computable, it will return the result of executing the provided mappingFunction.
   *
   * @param key
   *          - Key to retrieve corresponding value
   * @param mappingFunction
   *          - Mapping function that will compute the value of the key if not present in the cache
   * @return - Value corresponding to given key
   */
  public V get(T key, Function<? super T, ? extends V> mappingFunction) {
    checkCacheBuilt();
    V result = cache.get(key);
    if (result != null) {
      return result;
    }
    return cache.get(key, mappingFunction);
  }

  /**
   * Returns a map of Key-Value of all the given keys
   * 
   * @param keys
   *          - Collection of keys to retrieve values from
   * @return - map of Key-Value of all the given keys
   */
  public Map<T, V> getAll(Collection<T> keys) {
    checkCacheBuilt();
    return cache.getAll(keys);
  }

  /**
   * Returns a map of Key-Value of all the given keys. If the keys are not in the cache and some are
   * not computable, it will return the result of executing the provided mappingFunction for those
   * keys.
   *
   * @param keys
   *          - Collection of keys to retrieve values from
   * @param mappingFunction
   *          - Mapping function that will compute the values of the keys if not present in the
   *          cache
   * @return - map of Key-Value of all the given keys
   */
  public Map<T, V> getAll(Collection<T> keys,
      Function<? super Set<? extends T>, ? extends Map<? extends T, ? extends V>> mappingFunction) {
    checkCacheBuilt();
    Map<T, V> cachedKeys = cache.getAll(keys);
    if (cachedKeys.keySet().containsAll(keys)) {
      return cachedKeys;
    }
    return cache.getAll(keys, mappingFunction);
  }

  /**
   * Invalidates all the keys in the cache
   */
  public void invalidate() {
    checkCacheBuilt();
    cache.invalidateAll();
  }

  /**
   * Checks if the cache has been build, if not, it will throw an Exception
   * 
   * @throws OBException
   *           - if the cache has not been build yet
   */
  private void checkCacheBuilt() throws OBException {
    if (this.cache == null) {
      throw new OBException(
          "TimeInvalidatedCache has been accessed before being properly built. build() is required before accessing it.");
    }
  }

  /**
   * Internal API, used only for testing
   * 
   * @param ticker
   *          - Ticker to be used instead of the default system one
   */
  TimeInvalidatedCache<T, V> ticker(Ticker ticker) {
    this.ticker = ticker;
    return this;
  }
}
