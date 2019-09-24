package org.openbravo.authentication.hashing;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

/**
 * Passwords are hashed using SHA-512 algorithm with a random salt of 16 bytes represented as a
 * {@code String} encoded in base 64.
 * <p>
 * The full hash looks like {@code 1$salt$hashedPassword}, where {@code 1} is this algorithm's
 * version.
 */
class SHA512Salt extends HashingAlgorithm {
  private static final Random RANDOM = new SecureRandom();

  @Override
  protected String getHashingBaseAlgorithm() {
    return "SHA-512";
  }

  @Override
  protected boolean check(String plainTextPassword, String hashedPassword) {
    String[] hashParts = hashedPassword.split("\\$");
    String salt = hashParts[1];
    String orginalHash = hashParts[2];

    return hash(plainTextPassword, salt).equals(orginalHash);
  }

  @Override
  protected int getAlgorithmVersion() {
    return 1;
  }

  @Override
  public String generateHash(String password) {
    byte[] rawSalt = new byte[16];
    RANDOM.nextBytes(rawSalt);
    String salt = Base64.getEncoder().withoutPadding().encodeToString(rawSalt);
    String hash = hash(password, salt);
    return getAlgorithmVersion() + "$" + salt + "$" + hash;
  }
}