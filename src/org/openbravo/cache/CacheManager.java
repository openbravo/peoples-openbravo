package org.openbravo.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Date;

import javax.enterprise.util.AnnotationLiteral;

/**
 * A CacheManager holds frequently used data in memory and provides the means of removing it when
 * the data has changed in the underlying data store.
 * 
 * Invalidation will usually be invoked by the CacheInvalidationBackgroundManager, a process that
 * checks for generic cache invalidation events and works across several, non-clustered, application
 * servers.
 * 
 * For a CacheManager to be injected into the CacheInvalidationBackgroundManager it must:
 * <li>Be annotated with @ApplicationContext.
 * <li>Be annotated with a @CacheManager.Qualifier whose String parameter corresponds to the search
 * key specified in the AD_CACHE table.
 * <li>Implement the getEntry and putEntry methods in a thread safe manner.
 * 
 * @see org.openbravo.cache.CacheInvalidationBackgroundManager
 */
public interface CacheManager<K, V> {

  V getEntry(K key);

  void putEntry(K key, V value);

  void invalidate();

  void invalidateIfExpired(Date expirationDate);

  @javax.inject.Qualifier
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.TYPE })
  public @interface Qualifier {
    String value();
  }

  @SuppressWarnings("all")
  public static class Selector extends AnnotationLiteral<CacheManager.Qualifier>
      implements CacheManager.Qualifier {
    private static final long serialVersionUID = 1L;

    final String value;

    public Selector(String value) {
      this.value = value;
    }

    @Override
    public String value() {
      return value;
    }
  }

}
