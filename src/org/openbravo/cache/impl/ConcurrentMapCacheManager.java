package org.openbravo.cache.impl;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.enterprise.context.ApplicationScoped;

import org.openbravo.cache.CacheManager;

@ApplicationScoped
public abstract class ConcurrentMapCacheManager<K, V> implements CacheManager<K, V> {

  private final ConcurrentMap<K, V> cache = new ConcurrentHashMap<K, V>();
  private Date lastInvalidation;

  public abstract String getCacheIdentifier();

  public boolean containsKey(K key) {
    return cache.containsKey(key);
  }

  @Override
  public V getEntry(K key) {
    return cache.get(key);
  }

  @Override
  public void putEntry(K key, V value) {
    cache.put(key, value);
  }

  @Override
  public void invalidate() {
    cache.clear();
    lastInvalidation = new Date();
  }

  @Override
  public void invalidateIfExpired(Date expirationDate) {
    if (lastInvalidation == null || lastInvalidation.compareTo(expirationDate) < 0) {
      cache.clear();
      lastInvalidation = expirationDate;
    }
  }

}
