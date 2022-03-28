package org.openbravo.cache;

import java.time.Duration;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;

class TimeInvalidatedCacheBuilder<T, V> {
  private static Logger logger = LogManager.getLogger();

  private Duration expireDuration;
  private Ticker ticker;
  private String name;

  TimeInvalidatedCacheBuilder() {
  }

  /**
   * Builds the TimeInvalidatedCache and initializes it. Expects to be called as follows:
   *
   * <pre>
   * TimeInvalidatedCache.newInstance()
   *     .setName("CacheName")
   *     .expireAfterDuration(Duration.ofMinutes(5)) // Could be any Duration, not necessarily in
   *                                                 // minutes. If not executed, 1 minute default
   *                                                 // is assumed
   *     .build(key -> generateKeyValue(key)) // This is a lambda that initializes the key if it
   *                                          // expired or is the first time is read
   * </pre>
   *
   * @param loader
   *          - lambda that initializes the key if it expired or is the first time it is read. It
   *          should receive a key and return the value corresponding to it
   * @return this object
   */
  public <T1 extends T, V1 extends V> TimeInvalidatedCache<T1, V1> build(
      Function<? super T1, V1> loader) {
    if (expireDuration == null) {
      this.expireDuration = Duration.ofMinutes(1);
    }
    Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder();
    cacheBuilder.expireAfterWrite(expireDuration);
    if (this.ticker != null) {
      cacheBuilder.ticker(ticker);
    }

    CacheLoader<T1, V1> cacheLoader = new CacheLoader<>() {
      @Override
      public V1 load(T1 key) {
        return loader.apply(key);
      }
    };

    TimeInvalidatedCache<T1, V1> cache = new TimeInvalidatedCache<>(name,
        cacheBuilder.build(cacheLoader), expireDuration);
    logger.trace("Cache {} has been built with expireDuration {} ms.", name,
        expireDuration.toMillis());
    return cache;
  }

  /**
   * Sets the name of the cache, used for logging purposes
   * 
   * @param name
   *          - Cache name
   * @return this object
   */
  public TimeInvalidatedCacheBuilder<T, V> setName(String name) {
    this.name = name;
    return this;
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
  public TimeInvalidatedCacheBuilder<T, V> expireAfterDuration(Duration duration) {
    this.expireDuration = duration;
    return this;
  }

  /**
   * Internal API, used only for testing
   *
   * @param tickerToSet
   *          - Ticker to be used instead of the default system one
   * @return this object
   */
  TimeInvalidatedCacheBuilder<T, V> ticker(Ticker tickerToSet) {
    this.ticker = tickerToSet;
    return this;
  }
}
