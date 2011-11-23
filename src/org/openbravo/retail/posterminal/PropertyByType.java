/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.math.BigDecimal;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.model.Property;
import org.openbravo.base.model.domaintype.BigDecimalDomainType;
import org.openbravo.base.model.domaintype.BinaryDomainType;
import org.openbravo.base.model.domaintype.BooleanDomainType;
import org.openbravo.base.model.domaintype.DateDomainType;
import org.openbravo.base.model.domaintype.DatetimeDomainType;
import org.openbravo.base.model.domaintype.EncryptedStringDomainType;
import org.openbravo.base.model.domaintype.HashedStringDomainType;
import org.openbravo.base.model.domaintype.LongDomainType;
import org.openbravo.base.model.domaintype.StringDomainType;
import org.openbravo.base.model.domaintype.TimestampDomainType;

public class PropertyByType {

  public final static Property STRING = createString();
  public final static Property HASHEDSTRING = createHashedString();
  public final static Property ENCRYPTEDSTRING = createEncriptedString();
  public final static Property LONG = createLong();
  public final static Property FLOAT = createFloat();
  public final static Property BIGDECIMAL = createBigDecimal();
  public final static Property BOOLEAN = createBoolean();
  public final static Property BINARY = createBinary();
  public final static Property DATETIME = createDateTime();
  public final static Property DATE = createDate();
  public final static Property TIME = createTime();

  public static Property infer(Object value) {
    if (isEmptyOrNull(value) || value instanceof String) {
      return STRING;
    } else if (value instanceof Integer || value instanceof Long) {
      return LONG;
    } else if (value instanceof Number) {
      return BIGDECIMAL;
    } else if (value instanceof Boolean) {
      return BOOLEAN;
    } else {
      return STRING;
    }
  }

  public static Property get(String type) {
    String t = type.trim().toUpperCase();
    if ("STRING".equals(t)) {
      return STRING;
    } else if ("HASHEDSTRING".equals(t)) {
      return HASHEDSTRING;
    } else if ("ENCRYPTEDSTRING".equals(t)) {
      return ENCRYPTEDSTRING;
    } else if ("LONG".equals(t)) {
      return LONG;
    } else if ("FLOAT".equals(t)) {
      return FLOAT;
    } else if ("BIGDECIMAL".equals(t)) {
      return BIGDECIMAL;
    } else if ("BOOLEAN".equals(t)) {
      return BOOLEAN;
    } else if ("BINARY".equals(t)) {
      return BINARY;
    } else if ("DATETIME".equals(t)) {
      return DATETIME;
    } else if ("DATE".equals(t)) {
      return DATE;
    } else if ("TIME".equals(t)) {
      return TIME;
    } else {
      return null;
    }
  }

  private static boolean isEmptyOrNull(Object value) {
    if (JSONObject.NULL.equals(value)) { // note JSONObject.NULL.equals(null) == true
      return true;
    }
    if (value == null) {
      return true;
    }
    if (value instanceof String && ((String) value).trim().length() == 0) {
      return true;
    }
    return false;
  }

  public static Property createString() {
    Property prop = new Property();
    prop.setPrimitive(true);
    prop.setPrimitiveType(String.class);
    prop.setDomainType(new StringDomainType());
    return prop;
  }

  public static Property createHashedString() {
    Property prop = new Property();
    prop.setPrimitive(true);
    prop.setPrimitiveType(String.class);
    prop.setDomainType(new HashedStringDomainType());
    return prop;
  }

  public static Property createEncriptedString() {
    Property prop = new Property();
    prop.setPrimitive(true);
    prop.setPrimitiveType(String.class);
    prop.setDomainType(new EncryptedStringDomainType());
    return prop;
  }

  public static Property createLong() {
    Property prop = new Property();
    prop.setPrimitive(true);
    prop.setPrimitiveType(Long.class);
    prop.setDomainType(new LongDomainType());
    return prop;
  }

  public static Property createFloat() {
    Property prop = new Property();
    prop.setPrimitive(true);
    prop.setPrimitiveType(Float.class);
    prop.setDomainType(new BigDecimalDomainType());
    return prop;
  }

  public static Property createBigDecimal() {
    Property prop = new Property();
    prop.setPrimitive(true);
    prop.setPrimitiveType(BigDecimal.class);
    prop.setDomainType(new BigDecimalDomainType());
    return prop;
  }

  public static Property createBoolean() {
    Property prop = new Property();
    prop.setPrimitive(true);
    prop.setPrimitiveType(Boolean.class);
    prop.setDomainType(new BooleanDomainType());
    return prop;
  }

  public static Property createBinary() {
    Property prop = new Property();
    prop.setPrimitive(true);
    prop.setPrimitiveType(byte[].class);
    prop.setDomainType(new BinaryDomainType());
    return prop;
  }

  public static Property createDateTime() {
    Property prop = new Property();
    prop.setPrimitive(true);
    prop.setPrimitiveType(java.sql.Timestamp.class);
    prop.setDomainType(new DatetimeDomainType());
    return prop;
  }

  public static Property createDate() {
    Property prop = new Property();
    prop.setPrimitive(true);
    prop.setPrimitiveType(java.sql.Date.class);
    prop.setDomainType(new DateDomainType());
    return prop;
  }

  public static Property createTime() {
    Property prop = new Property();
    prop.setPrimitive(true);
    prop.setPrimitiveType(java.sql.Time.class);
    prop.setDomainType(new TimestampDomainType());
    return prop;
  }
}
