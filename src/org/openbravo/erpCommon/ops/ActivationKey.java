package org.openbravo.erpCommon.ops;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.crypto.Cipher;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Expression;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.domain.ListTrl;
import org.openbravo.model.ad.domain.Reference;
import org.openbravo.model.ad.system.Language;

public class ActivationKey {

  private boolean isActive = false;
  private boolean hasActivationKey = false;
  private String errorMessage = "";
  private Properties instanceProperties;
  private static final Logger log = Logger.getLogger(ActivationKey.class);
  private String strPublicKey;

  public ActivationKey() {
    org.openbravo.model.ad.system.System sys = OBDal.getInstance().get(
        org.openbravo.model.ad.system.System.class, "0");
    strPublicKey = sys.getInstanceKey();
    String activationKey = sys.getActivationKey();

    if (strPublicKey == null || activationKey == null || strPublicKey.equals("")
        || activationKey.equals("")) {
      hasActivationKey = false;
      return;
    }

    PublicKey pk = getPublicKey(strPublicKey);
    if (pk == null) {
      hasActivationKey = true;
      errorMessage = "@NotAValidKey";
      return;
    }
    hasActivationKey = true;
    try {
      Cipher cipher = Cipher.getInstance("RSA");

      ByteArrayInputStream bis = new ByteArrayInputStream(org.apache.commons.codec.binary.Base64
          .decodeBase64(activationKey.getBytes()));
      ByteArrayOutputStream bos = new ByteArrayOutputStream();

      // Encryptation only accepts 128B size, it must be chuncked
      final byte[] buf = new byte[128];
      while ((bis.read(buf)) > 0) {
        cipher.init(Cipher.DECRYPT_MODE, pk);
        bos.write(cipher.doFinal(buf));
      }
      byte[] props = bos.toByteArray();

      System.out.println("prop:" + new String(props));
      ByteArrayInputStream isProps = new ByteArrayInputStream(props);
      instanceProperties = new Properties();

      instanceProperties.load(isProps);
      System.out.println("customer:" + instanceProperties.getProperty("customer"));
    } catch (Exception e) {
      isActive = false;
      errorMessage = "@NotAValidKey@";
      e.printStackTrace();
      return;
    }

    // Check for dates to know if the instance is active
    SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
    Date startDate = null;
    Date endDate = null;
    try {
      startDate = sd.parse(getProperty("startdate"));

      if (getProperty("enddate") != null)
        endDate = sd.parse(getProperty("enddate"));

      System.out.println("sd:" + startDate);
      System.out.println("ed:" + endDate);
    } catch (ParseException e) {
      errorMessage = "@ErrorReadingDates@";
      isActive = false;
      log.error(e);
      return;
    }

    Date now = new Date();
    if (startDate == null || now.before(startDate)) {
      isActive = false;
      errorMessage = "@OPSNotActiveTill@ " + startDate;
      return;
    }
    if (endDate != null && now.after(endDate)) {
      isActive = false;
      errorMessage = "@OPSActivationExpired@ " + endDate;
      return;
    }
    isActive = true;

  }

  private PublicKey getPublicKey(String strPublicKey) {
    try {
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      byte[] rawPublicKey = org.apache.commons.codec.binary.Base64.decodeBase64(strPublicKey
          .getBytes());

      X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(rawPublicKey);
      return keyFactory.generatePublic(publicKeySpec);
    } catch (Exception e) {
      log.error(e);
      return null;
    }
  }

  public String getPublicKey() {
    return strPublicKey;
  }

  public boolean hasActivationKey() {
    return hasActivationKey;
  }

  public boolean isActive() {
    return isActive;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public boolean hasActivationProperties() {
    return instanceProperties != null;
  }

  public String toString(ConnectionProvider conn, String lang) {
    StringBuffer sb = new StringBuffer();
    if (instanceProperties != null) {
      sb.append(Utility.messageBD(conn, "OPSCustomer", lang)).append(": ").append(
          getProperty("customer")).append("\n");
      sb.append(Utility.messageBD(conn, "OPSInstanceNo", lang)).append(": ").append(
          getProperty("instanceno")).append("\n");
      sb.append(Utility.messageBD(conn, "OPSLicenseType", lang)).append(": ").append(
          getListValueName("OPSLicenseType", getProperty("lincensetype"), lang)).append("\n");
      sb.append(Utility.messageBD(conn, "OPSInstancePurpose", lang)).append(": ").append(
          getListValueName("InstancePurpose", getProperty("purpose"), lang)).append("\n");
      sb.append(Utility.messageBD(conn, "OPSStartDate", lang)).append(": ").append(
          getProperty("startdate")).append("\n");
      sb.append(Utility.messageBD(conn, "OPSEndDate", lang)).append(": ").append(
          getProperty("enddate")).append("\n");
      sb.append(Utility.messageBD(conn, "OPSConcurrentUsers", lang)).append(": ").append(
          getProperty("limitusers")).append("\n");
    } else {
      sb.append(Utility.messageBD(conn, "OPSNonActiveInstance", lang));
    }
    return sb.toString();
  }

  public String getProperty(String propName) {
    return instanceProperties.getProperty(propName);
  }

  private String getListValueName(String ListName, String value, String lang) {
    OBCriteria<Reference> obCriteria = OBDal.getInstance().createCriteria(Reference.class);
    obCriteria.add(Expression.eq(Reference.PROPERTY_NAME, ListName));
    obCriteria.add(Expression.eq(Reference.PROPERTY_VALIDATIONTYPE, "L"));
    List<Reference> lRef = obCriteria.list();
    if (lRef == null || lRef.size() != 1)
      return ""; // reference not found
    Reference reference = lRef.get(0);

    // check for value
    OBCriteria<org.openbravo.model.ad.domain.List> obCriteria2 = OBDal.getInstance()
        .createCriteria(org.openbravo.model.ad.domain.List.class);
    obCriteria2
        .add(Expression.eq(org.openbravo.model.ad.domain.List.PROPERTY_REFERENCE, reference));
    obCriteria2.add(Expression.eq(org.openbravo.model.ad.domain.List.PROPERTY_SEARCHKEY, value));
    List<org.openbravo.model.ad.domain.List> lVal = obCriteria2.list();
    if (lVal == null || lVal.size() != 1)
      return ""; // value not found
    org.openbravo.model.ad.domain.List val = lVal.get(0);

    // check for translation
    if (lang == null || lang.equals(""))
      return val.getName();

    OBCriteria<Language> obCriteriaL = OBDal.getInstance().createCriteria(Language.class);
    obCriteriaL.add(Expression.eq(Language.PROPERTY_NAME, lang));
    List<Language> lLang = obCriteriaL.list();
    if (lLang == null || lLang.size() != 1)
      return val.getName();

    OBCriteria<ListTrl> obCriteria3 = OBDal.getInstance().createCriteria(ListTrl.class);
    obCriteria3.add(Expression.eq(ListTrl.PROPERTY_LISTREFERENCE, val));
    obCriteria3.add(Expression.eq(ListTrl.PROPERTY_LANGUAGE, lLang.get(0)));
    List<ListTrl> lValTrl = obCriteria3.list();
    if (lVal == null || lVal.size() != 1)
      return val.getName();
    return lValTrl.get(0).getName();

  }

}
