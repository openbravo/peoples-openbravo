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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.domaintype.BigDecimalDomainType;
import org.openbravo.base.model.domaintype.DateDomainType;
import org.openbravo.base.model.domaintype.DomainType;
import org.openbravo.base.model.domaintype.LongDomainType;
import org.openbravo.base.model.domaintype.StringDomainType;
import org.openbravo.base.util.Check;

/**
 * Utility class for Parameters handling
 * 
 * @author iperdomo
 */
public class ParameterUtils {
  private static Logger log = Logger.getLogger(ParameterUtils.class);

  public static void setParameterValue(Parameter parameter, ParameterValue parameterValue,
      JSONObject requestValue) {
    try {
      setValue(parameterValue, getParameterDomainType(parameter), requestValue.getString("value"));
    } catch (Exception e) {
      log.error("Error trying to set value for paramter: "
          + parameterValue.getParameter().getName(), e);
    }
  }

  public static void setDefaultParameterValue(ParameterValue value) {
    Check.isNotNull(value, "Default value is based on Parameter defintion");
    setValue(value, getParameterDomainType(value.getParameter()), value.getParameter()
        .getDefaultValue());
  }

  private static void setValue(ParameterValue parameterValue, DomainType domainType,
      String stringValue) {
    final SimpleDateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    try {
      if (domainType.getClass().equals(StringDomainType.class)) {
        parameterValue.setValueString(stringValue);
      } else if (domainType.getClass().equals(DateDomainType.class)) {
        Date date = xmlDateFormat.parse(stringValue);
        parameterValue.setValueDate(date);
      } else if (domainType.getClass().getSuperclass().equals(BigDecimalDomainType.class)
          || domainType.getClass().equals(LongDomainType.class)) {
        parameterValue.setValueNumber(new BigDecimal(stringValue));
      } else { // default
        parameterValue.setValueString(stringValue);
      }
    } catch (Exception e) {
      log.error("Error trying to set value for paramter: "
          + parameterValue.getParameter().getName(), e);
    }
  }

  private static DomainType getParameterDomainType(Parameter parameter) {
    return ModelProvider.getInstance().getReference(parameter.getReference().getId())
        .getDomainType();
  }
}
