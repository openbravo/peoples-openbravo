package org.openbravo.cache;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public abstract class CacheManager<K, V> {

  private final ConcurrentMap<K, V> cache = new ConcurrentHashMap<K, V>();
  private Date lastInvalidation;

  public abstract String getCacheIdentifier();

  public boolean containsKey(K key) {
    return cache.containsKey(key);
  }

  public V getEntry(K key) {
    return cache.get(key);
  }

  public void putEntry(K key, V value) {
    cache.put(key, value);
  }

  public void invalidate() {
    cache.clear();
    lastInvalidation = new Date();
  }

  public void invalidateIfExpired(Date expirationDate) {
    if (lastInvalidation == null || lastInvalidation.compareTo(expirationDate) < 0) {
      cache.clear();
      lastInvalidation = expirationDate;
    }
  }

}
