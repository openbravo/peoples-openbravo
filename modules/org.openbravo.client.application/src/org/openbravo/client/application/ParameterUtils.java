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
 * All portions are Copyright (C) 2010-2015 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.domaintype.BigDecimalDomainType;
import org.openbravo.base.model.domaintype.BooleanDomainType;
import org.openbravo.base.model.domaintype.DateDomainType;
import org.openbravo.base.model.domaintype.DomainType;
import org.openbravo.base.model.domaintype.LongDomainType;
import org.openbravo.base.model.domaintype.StringDomainType;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.util.Check;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.utility.Attachment;
import org.openbravo.model.ad.utility.AttachmentMetadata;

/**
 * Utility class for Parameters handling
 * 
 * @author iperdomo
 */
public class ParameterUtils {
  private static Logger log = Logger.getLogger(ParameterUtils.class);

  public static final String REFERENCE_INTEGER = "11";
  public static final String REFERENCE_AMOUNT = "12";
  public static final String REFERENCE_DATE = "15";
  public static final String REFERENCE_DATETIME = "16";
  public static final String REFERENCE_QUANTITY = "29";
  public static final String REFERENCE_ABSOLUTEDATETIME = "478169542A1747BD942DD70C8B45089C";

  public static void setParameterValue(ParameterValue parameterValue, JSONObject requestValue) {
    try {
      setValue(parameterValue, requestValue.getString("value"));
    } catch (Exception e) {
      log.error("Error trying to set value for paramter: "
          + parameterValue.getParameter().getName(), e);
    }
  }

  public static void setDefaultParameterValue(ParameterValue value) {
    Check.isNotNull(value, "Default value is based on Parameter defintion");
    setValue(value, value.getParameter().getDefaultValue());
  }

  private static void setValue(ParameterValue parameterValue, String stringValue) {
    DomainType domainType = getParameterDomainType(parameterValue.getParameter());
    try {
      if (domainType.getClass().equals(StringDomainType.class)) {
        parameterValue.setValueString(stringValue);
      } else if (domainType.getClass().equals(DateDomainType.class)) {
        DateDomainType dateDomainType = (DateDomainType) domainType;
        Date date = (Date) dateDomainType.createFromString(stringValue);
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

  /**
   * Returns an Object with the Value of the Parameter Value. This object can be a String, a
   * java.util.Data or a BigDecimal.
   * 
   * @param parameterValue
   *          the Parameter Value we want to get the Value from.
   * @return the Value of the Parameter Value.
   */
  public static Object getParameterValue(ParameterValue parameterValue) {
    DomainType domainType = getParameterDomainType(parameterValue.getParameter());
    if (domainType.getClass().equals(StringDomainType.class)) {
      return parameterValue.getValueString();
    } else if (domainType.getClass().equals(DateDomainType.class)) {
      return parameterValue.getValueDate();
    } else if (domainType.getClass().getSuperclass().equals(BigDecimalDomainType.class)
        || domainType.getClass().equals(LongDomainType.class)) {
      return parameterValue.getValueNumber();
    } else if (domainType.getClass().equals(BooleanDomainType.class)) {
      return "true".equals(parameterValue.getValueString());
    } else { // default
      return parameterValue.getValueString();
    }
  }

  /**
   * Returns the Fixed value of the given parameter. If the value is a JS expression it returns the
   * result of the expression based on the parameters passed in from the request.
   * 
   * @param parameters
   *          the parameters passed in from the request
   * @param parameter
   *          the parameter we want to get the Fixed Value from
   * @return the Fixed Value of the parameter
   */
  public static Object getParameterFixedValue(Map<String, String> parameters, Parameter parameter) {
    if (parameter.isEvaluateFixedValue()) {
      try {
        return getJSExpressionResult(parameters, null, parameter.getFixedValue());
      } catch (Exception e) {
        // log.error(e.getMessage(), e);
        return null;
      }
    } else {
      return parameter.getFixedValue();
    }
  }

  /**
   * Returns the result of evaluating the given JavaScript expression.
   * 
   * @param parameters
   *          Map of Strings with the request map parameters.
   * @param session
   *          optional HttpSession object.
   * @param expression
   *          String with the JavaScript expression to be evaluated.
   * @return an Object with the result of the expression evaluation.
   * @throws ScriptException
   */
  @SuppressWarnings("rawtypes")
  public static Object getJSExpressionResult(Map<String, String> parameters, HttpSession session,
      String expression) throws ScriptException {
    final ScriptEngineManager manager = new ScriptEngineManager();
    final ScriptEngine engine = manager.getEngineByName("js");

    if (session != null) {
      engine.put("OB", new OBBindings(OBContext.getOBContext(), parameters, session));
    } else {
      engine.put("OB", new OBBindings(OBContext.getOBContext(), parameters));
    }

    Object result = engine.eval(expression);
    if (result instanceof Map) {
      // complex js object, convert it into a JSON
      result = new JSONObject((Map) result);
    }
    return result;
  }

  /**
   * Save metadata in C_File_Metadata records.
   * 
   * @param attachment
   *          attachment is saving metadata to.
   * @param metadata
   *          metadata values to save.
   * @param exists
   *          true if the attachment already exists (if exists, metadata should exist too)
   * @return
   */
  public static void saveMetadata(Attachment attachment, Map<String, String> metadata,
      boolean exists) {
    try {
      for (Map.Entry<String, String> entry : metadata.entrySet()) {
        final Parameter parameter = OBDal.getInstance().get(Parameter.class, entry.getKey());
        AttachmentMetadata attachmentMetadata;
        if (exists) {
          final OBCriteria<AttachmentMetadata> attachmentMetadataCriteria = OBDal.getInstance()
              .createCriteria(AttachmentMetadata.class);
          attachmentMetadataCriteria.add(Restrictions.eq(AttachmentMetadata.PROPERTY_FILE,
              attachment));
          attachmentMetadataCriteria.add(Restrictions.eq(
              AttachmentMetadata.PROPERTY_OBUIAPPPARAMETER, parameter));
          if (attachmentMetadataCriteria.list().isEmpty()) {
            attachmentMetadata = OBProvider.getInstance().get(AttachmentMetadata.class);
          } else if (attachmentMetadataCriteria.list().size() == 1) {
            attachmentMetadata = attachmentMetadataCriteria.list().get(0);
          } else {
            throw new OBException();
          }
        } else {
          attachmentMetadata = OBProvider.getInstance().get(AttachmentMetadata.class);
        }

        attachmentMetadata.setFile(attachment);
        attachmentMetadata.setObuiappParameter(parameter);
        if (parameter.isUserEditable() && parameter.getPropertyPath() != null
            && !parameter.getPropertyPath().equals("")) {
          // if has a property path
        } else {
          if (parameter.getReference().getId().equals(REFERENCE_DATE)
              || parameter.getReference().getId().equals(REFERENCE_DATETIME)
              || parameter.getReference().getId().equals(REFERENCE_ABSOLUTEDATETIME)) {
            attachmentMetadata.setValuationDate(OBDateUtils.getDate(entry.getValue()));
          } else if (parameter.getReference().getId().equals(REFERENCE_INTEGER)
              || parameter.getReference().getId().equals(REFERENCE_QUANTITY)
              || parameter.getReference().getId().equals(REFERENCE_AMOUNT)) {
            attachmentMetadata.setNumericValue(new BigDecimal(entry.getValue()));
          } else {
            attachmentMetadata.setStringValue(entry.getValue());
          }
        }
        OBDal.getInstance().save(attachmentMetadata);
      }
    } catch (Exception e) {
      throw new OBException(OBMessageUtils.getI18NMessage("OBUIAPP_ErrorInsertMetadata", null), e);
    }
  }
}
