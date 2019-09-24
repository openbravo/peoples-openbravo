package org.openbravo.authentication.hashing;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.jboss.weld.exceptions.IllegalStateException;

/**
 * Passwords are hashed with SHA-1 algorithm represented as a {@code String} encoded in base 64.
 * <p>
 * Algorithm used before 3.0PR20Q1.
 */
class SHA1 extends HashingAlgorithm {
  @Override
  protected MessageDigest getHashingBaseAlgorithm() {
    try {
      return MessageDigest.getInstance("SHA-1");
    } catch (NoSuchAlgorithmException wontHappen) {
      throw new IllegalStateException(wontHappen);
    }
  }

  @Override
  protected boolean check(String plainTextPassword, String hashedPassword) {
    return hash(plainTextPassword, null).equals(hashedPassword);
  }

  @Override
  protected int getAlgorithmVersion() {
    return 0;
  }

  @Override
  public String generateHash(String password) {
    return hash(password, null);
  }

}
