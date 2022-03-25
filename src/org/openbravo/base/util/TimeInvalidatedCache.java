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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.base.exception.OBException;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.Ticker;

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
  private static Logger logger = LogManager.getLogger();

  private LoadingCache<T, V> cache;
  private Duration expireDuration;
  // Internal ticker, do not use
  private Ticker ticker;
  private final String name;

  /**
   * Creates a new instance of TimeInvalidatedCache with the types provided. Usage as follows:
   * 
   * <pre>
   * TimeInvalidatedCache<KeyType, ValueType>
   *   .newInstance(cacheName)
   *   .expireAfterDuration(Duration.ofMinutes(5)) // Could be any Duration, not necessarily in minutes. If not executed, 1 minute default is assumed
   *   .build(key -> generateKeyValue(key)) // This is a lambda that initializes the key if it expired or is the first time is read
   * </pre>
   * 
   * @param name
   *          - Name of the Cache, will be used in the logging processes
   * @return this object, to be followed by an expireAfterDuration or build calls.
   */
  public static TimeInvalidatedCache<Object, Object> newInstance(String name) {
    return new TimeInvalidatedCache<>(name);
  }

  /**
   * Sets the expiration duration, after this period the key is considered expired and reloaded on
   * the next access
   * 
   * @param duration
   *          - Duration of time after which is considered expired
   * @return this object, to follow with build() call
   * @throws IllegalArgumentException
   *           – if duration is negative (thrown when executing build method)
   * @throws IllegalStateException
   *           – if the time to live or variable expiration was already set (thrown when executing
   *           build method)
   * @throws ArithmeticException
   *           – for durations greater than +/- approximately 292 year (thrown when executing build
   *           method)
   */
  public TimeInvalidatedCache<T, V> expireAfterDuration(Duration duration) {
    if (this.cache != null) {
      // If cache was already built, throw exception, as this function would not change the duration
      throw new OBException(String.format(
          "Cache %s has already been built, it is not possible to set expireAfterDuration after initialization.",
          name));
    }
    this.expireDuration = duration;
    return this;
  }

  /**
   * Builds the TimeInvalidatedCache and initializes it. Expects to be called as follows:
   *
   * <pre>
   * TimeInvalidatedCache<KeyType, ValueType>
   *   .newInstance(cacheName)
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
    logger.trace("Cache {} has been built with expireDuration {} ms.", name,
        expireDuration.toMillis());
    return self;
  }

  private TimeInvalidatedCache(String name) {
    this.name = name;
  }

  /**
   * Returns a value from the cache associated to a given key
   * 
   * @param key
   *          - Key to retrieve corresponding value
   * @return - Value corresponding to given key
   * @throws NullPointerException
   *           – if the specified key is null (not the value associated with the key)
   */
  public V get(T key) {
    checkCacheBuilt();
    logger.trace("Cache {} get key {} has been executed.", name, key);
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
   * @throws NullPointerException
   *           – if the specified key is null (not the value associated with the key)
   */
  public V get(T key, Function<? super T, ? extends V> mappingFunction) {
    checkCacheBuilt();
    V result = cache.get(key);
    logger.trace("Cache {} getAll with mappingFunction, has been executed with key {}.", name, key);
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
   * @throws NullPointerException
   *           – if any of the specified keys is null (not the value associated with the key)
   */
  public Map<T, V> getAll(Collection<T> keys) {
    checkCacheBuilt();
    logger.trace("Cache {} getAll keys {} has been executed.", name, keys);
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
   * @throws NullPointerException
   *           – if any of the specified keys is null (not the value associated with the key)
   */
  public Map<T, V> getAll(Collection<T> keys,
      Function<? super Set<? extends T>, ? extends Map<? extends T, ? extends V>> mappingFunction) {
    checkCacheBuilt();
    Map<T, V> cachedKeys = cache.getAll(keys);
    logger.trace("Cache {} getAll with mappingFunction, has been executed with keys {}.", name,
        keys);
    if (cachedKeys.keySet().containsAll(keys)) {
      return cachedKeys;
    }
    return cache.getAll(keys, mappingFunction);
  }

  /**
   * Invalidates given key in the cache
   */
  public void invalidate(T key) {
    checkCacheBuilt();
    cache.invalidate(key);
    logger.trace("{} key in cache {} has been invalidated.", key, name);
  }

  /**
   * Invalidates all the keys in the cache
   */
  public void invalidateAll() {
    checkCacheBuilt();
    cache.invalidateAll();
    logger.trace("Cache {} has been invalidated(all keys).", name);
  }

  public String getName() {
    return name;
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
   * @param tickerToSet
   *          - Ticker to be used instead of the default system one
   */
  TimeInvalidatedCache<T, V> ticker(Ticker tickerToSet) {
    if (this.cache != null) {
      // If cache was already built, throw exception, as this function would not change the duration
      throw new OBException(String.format(
          "Cache %s has already been built, it is not possible to set ticker after initialization.",
          name));
    }
    this.ticker = tickerToSet;
    return this;
  }
}
