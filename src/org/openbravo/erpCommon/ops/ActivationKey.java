package org.openbravo.erpCommon.ops;

import java.io.ByteArrayInputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Properties;

import javax.crypto.Cipher;

import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;

public class ActivationKey {

  private boolean isActive = false;
  private String errorMessage = "";
  private Properties instanceProperties;

  public ActivationKey() {
    org.openbravo.model.ad.system.System sys = OBDal.getInstance().get(
        org.openbravo.model.ad.system.System.class, "0");
    String strPublicKey = sys.getInstanceKey();
    String activationKey = sys.getActivationKey();

    if (strPublicKey == null || activationKey == null || strPublicKey.equals("")
        || activationKey.equals("")) {
      isActive = false;
      errorMessage = "@NoKeyAvailable@";
      return;
    }

    PublicKey pk = getPublicKey(strPublicKey);
    if (pk == null) {
      isActive = false;
      errorMessage = "@NotAValidKey";
      return;
    }
    try {
      Cipher cipher = Cipher.getInstance("RSA");

      cipher.init(Cipher.DECRYPT_MODE, pk);
      byte[] props = cipher.doFinal(org.apache.commons.codec.binary.Base64
          .decodeBase64(activationKey.getBytes()));
      System.out.println("prop:" + new String(props));
      ByteArrayInputStream isProps = new ByteArrayInputStream(props);
      instanceProperties = new Properties();

      instanceProperties.load(isProps);
      System.out.println("customer:" + instanceProperties.getProperty("customer"));
      isActive = true;

    } catch (Exception e) {
      isActive = false;
      errorMessage = "@NotAValidKey";
      e.printStackTrace();
    }

  }

  private PublicKey getPublicKey(String strPublicKey) {
    try {
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      byte[] rawPublicKey = org.apache.commons.codec.binary.Base64.decodeBase64(strPublicKey
          .getBytes());

      X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(rawPublicKey);
      return keyFactory.generatePublic(publicKeySpec);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public boolean isInstanceActive() {
    return isActive;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public String toString(ConnectionProvider conn, String lang) {
    StringBuffer sb = new StringBuffer();
    sb.append(Utility.messageBD(conn, "Customer", lang)).append(": ").append(
        getProperty("customer")).append("\n");
    sb.append(Utility.messageBD(conn, "InstanceNo", lang)).append(": ").append(
        getProperty("instanceno")).append("\n");
    sb.append(Utility.messageBD(conn, "LicenseType", lang)).append(": ").append(
        getProperty("lincensetype")).append("\n");
    return sb.toString();
  }

  public String getProperty(String propName) {
    return instanceProperties.getProperty(propName);
  }

}
