package org.openbravo.authentication.hashing;

/**
 * Passwords are hashed with SHA-1 algorithm represented as a {@code String} encoded in base 64.
 * <p>
 * Algorithm used before 3.0PR20Q1.
 */
class SHA1 extends HashingAlgorithm {
  @Override
  protected String getHashingBaseAlgorithm() {
    return "SHA-1";
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