package org.openbravo.authentication.hashing;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/** Algorithm used to hash password to store in Database */
public abstract class HashingAlgorithm {

  /** Generates a hash using current algorithm */
  public abstract String generateHash(String password);

  /**
   * Each {@link HashingAlgorithm} must be versioned, passwords hashed in Database with older
   * algorithms can be automatically upgraded to newer ones.
   * 
   * @see PasswordHash#getUserWithPassword(String, String)
   */
  protected abstract int getAlgorithmVersion();

  /** Checks whether a plain text password matches with a hashed password */
  protected abstract boolean check(String plainTextPassowed, String hashedPassword);

  protected abstract String getHashingBaseAlgorithm();

  protected final String hash(String plainText, String salt) {
    try {
      MessageDigest md = MessageDigest.getInstance(getHashingBaseAlgorithm());
      if (salt != null) {
        md.update(salt.getBytes(StandardCharsets.UTF_8));
      }

      byte[] bytes = md.digest(plainText.getBytes(StandardCharsets.UTF_8));

      return Base64.getEncoder().encodeToString(bytes);
    } catch (NoSuchAlgorithmException e) {
      PasswordHash.log.error("Error getting hashing algorithm", e);
      return "";
    }
  }
}