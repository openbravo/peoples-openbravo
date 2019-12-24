package org.openbravo.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Date;

import javax.enterprise.util.AnnotationLiteral;

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
