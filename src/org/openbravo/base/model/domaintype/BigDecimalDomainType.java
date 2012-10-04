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
 * All portions are Copyright (C) 2009-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.base.model.domaintype;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.openbravo.base.session.OBPropertiesProvider;

/**
 * The type for a decimal column.
 * 
 * @author mtaal
 */

public class BigDecimalDomainType extends BasePrimitiveDomainType {

  @Override
  public String convertToString(Object value) {
    if (value == null) {
      return EMPTY_STRING;
    }
    Map<String, String> localFormatDefinitions = new HashMap<String, String>();
    Document doc = OBPropertiesProvider.getInstance().getFormatXMLDocument();
    Element root = doc.getRootElement();
    for (Object object : root.elements()) {
      final Element element = (Element) object;
      String formatDefinition = new String();

      formatDefinition = element.attributeValue("formatInternal").replace(",", "");
      localFormatDefinitions.put(element.attributeValue("name"), formatDefinition);
    }

    DecimalFormat format = new DecimalFormat(localFormatDefinitions.get(this.getFormatId()
        + "Edition"));

    return format.format(value).toString().replace(",", ".");
  }

  /**
   * @return class of the {@link BigDecimal}
   */
  public Class<?> getPrimitiveType() {
    return BigDecimal.class;
  }

  public static class Quantity extends BigDecimalDomainType {
    public String getFormatId() {
      return "qty";
    }
  }

  public static class GeneralQuantity extends BigDecimalDomainType {
    public String getFormatId() {
      return "generalQty";
    }
  }

  public static class Number extends BigDecimalDomainType {
    public String getFormatId() {
      return "euro";
    }
  }

  public static class Amount extends BigDecimalDomainType {
    public String getFormatId() {
      return "euro";
    }
  }

  public static class Price extends BigDecimalDomainType {
    public String getFormatId() {
      return "price";
    }
  }

  @Override
  public Object createFromString(String strValue) {
    if (strValue == null || strValue.trim().length() == 0) {
      return null;
    }
    return new BigDecimal(strValue);
  }

  @Override
  public String getXMLSchemaType() {
    return "ob:decimal";
  }
}
