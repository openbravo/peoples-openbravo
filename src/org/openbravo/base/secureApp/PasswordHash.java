package org.openbravo.base.secureApp;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.jboss.weld.exceptions.IllegalStateException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.User;

public abstract class PasswordHash {
  public static final Logger log = LogManager.getLogger();
  private static final int DEFAULT_CURRENT_ALGORITHM_VERSION = 1;

  private static final Map<Integer, PasswordHash> INSTANCES = Map.of(0, new SHA1(), 1,
      new SHA512Salt());

  private PasswordHash() {
  }

  public static PasswordHash getAlgorithm(String hash) {
    PasswordHash algorithm = INSTANCES.get(getVersion(hash));

    if (algorithm == null) {
      throw new IllegalStateException(
          "Hashing alorightm version " + getVersion(hash) + " is not implemented");
    }

    return algorithm;
  }

  public static User getUser(String userName, String password) {
    OBContext.setAdminMode(false);
    try {
      // TODO: ensure we can use DAL at this point
      User user = (User) OBDal.getInstance()
          .createCriteria(User.class)
          .add(Restrictions.eq(User.PROPERTY_USERNAME, userName))
          .uniqueResult();

      if (user == null) {
        return null;
      }

      PasswordHash algorithm = getAlgorithm(user.getPassword());
      if (algorithm.check(password, user.getPassword())) {
        if (algorithm.getAlgorithmVersion() < DEFAULT_CURRENT_ALGORITHM_VERSION) {
          log.debug("Upgrading password hash for user {}, from algorithm version {} to {}.",
              user.getUsername(), algorithm.getAlgorithmVersion(),
              DEFAULT_CURRENT_ALGORITHM_VERSION);
          String newPassword = INSTANCES.get(DEFAULT_CURRENT_ALGORITHM_VERSION)
              .generateHash(password);
          user.setPassword(newPassword);
        }
        return user;
      } else {
        return null;
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public static PasswordHash getDefaultAlgorithm() {
    return INSTANCES.get(DEFAULT_CURRENT_ALGORITHM_VERSION);
  }

  public abstract String generateHash(String password);

  protected abstract int getAlgorithmVersion();

  public static boolean matches(String plainTextPassword, String hashedPassword) {
    PasswordHash algorithm = getAlgorithm(hashedPassword);
    log.trace("Checking password with algorithm {}", () -> algorithm.getClass().getSimpleName());
    return algorithm.check(plainTextPassword, hashedPassword);
  }

  protected abstract boolean check(String plainTextPassowed, String hashedPassword);

  protected String hash(String plainText, String salt) {
    try {
      MessageDigest md = MessageDigest.getInstance(getHashingBaseAlgorithm());
      if (salt != null) {
        md.update(salt.getBytes(StandardCharsets.UTF_8));
      }

      byte[] bytes = md.digest(plainText.getBytes(StandardCharsets.UTF_8));
      return new String(org.apache.commons.codec.binary.Base64.encodeBase64(bytes),
          StandardCharsets.UTF_8);
    } catch (NoSuchAlgorithmException e) {
      log.error("Error getting hashing algorithm", e);
      return "";
    }
  }

  protected abstract String getHashingBaseAlgorithm();

  private static int getVersion(String hash) {
    int idx = hash.indexOf('$');
    if (idx == -1) {
      return 0;
    }
    return Integer.parseInt(hash.substring(0, idx));
  }

  private static class SHA1 extends PasswordHash {
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

  private static class SHA512Salt extends PasswordHash {
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
      String salt = "someRandom"; // TODO: define salt
      String hash = hash(password, salt);
      return getAlgorithmVersion() + "$" + salt + "$" + hash;
    }
  }
}
