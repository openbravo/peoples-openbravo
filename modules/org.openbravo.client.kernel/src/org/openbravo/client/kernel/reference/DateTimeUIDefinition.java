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
 * All portions are Copyright (C) 2010-2014 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.kernel.reference;

import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.domaintype.PrimitiveDomainType;
import org.openbravo.client.kernel.RequestContext;

/**
 * Implementation of the date time ui definition.
 * 
 * @author mtaal
 */
public class DateTimeUIDefinition extends DateUIDefinition {
  private String lastUsedPattern = null;
  private SimpleDateFormat dateFormat = null;
  private static final SimpleDateFormat ficDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  @Override
  public String getParentType() {
    return "datetime";
  }

  @Override
  public String getFormEditorType() {
    return "OBDateTimeItem";
  }

  protected String getClientFormatObject() {
    return "OB.Format.dateTime";
  }

  @Override
  public String convertToClassicString(Object value) {
    if (value == null || value == "") {
      return "";
    }

    if (value instanceof String) {
      return (String) value;
    }

    StringBuffer convertedValue = convertLocalTimeToUTC((Date) value);
    return convertedValue.toString();
  }

  private StringBuffer convertLocalTimeToUTC(Date UTCDate) {
    StringBuffer localTimeColumnValue = null;
    Calendar now = Calendar.getInstance();

    Calendar calendar = Calendar.getInstance();
    calendar.setTime(UTCDate);
    calendar.set(Calendar.DATE, now.get(Calendar.DATE));
    calendar.set(Calendar.MONTH, now.get(Calendar.MONTH));
    calendar.set(Calendar.YEAR, now.get(Calendar.YEAR));

    int gmtMillisecondOffset = (now.get(Calendar.ZONE_OFFSET) + now.get(Calendar.DST_OFFSET));
    calendar.add(Calendar.MILLISECOND, -gmtMillisecondOffset);
    localTimeColumnValue = getClassicFormat().format(calendar.getTime(), new StringBuffer(),
        new FieldPosition(0));

    return localTimeColumnValue;
  }

  @Override
  protected SimpleDateFormat getClassicFormat() {
    String pattern = RequestContext.get().getSessionAttribute("#AD_JavaDateTimeFormat").toString();
    if (dateFormat == null || !pattern.equals(lastUsedPattern)) {
      dateFormat = new SimpleDateFormat(pattern);
      lastUsedPattern = pattern;
      dateFormat.setLenient(true);
    }
    return dateFormat;
  }

  @Override
  public synchronized Object createFromClassicString(String value) {
    try {
      if (value == null || value.length() == 0 || value.equals("null")) {
        return null;
      }
      if (value.contains("T")) {
        return value;
      }
      Calendar now = Calendar.getInstance();
      final Date date = getClassicFormat().parse(value);
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);
      // Applies the zone offset and the dst offset to convert the time from local to UTC
      int gmtMillisecondOffset = (now.get(Calendar.ZONE_OFFSET) + now.get(Calendar.DST_OFFSET));
      calendar.add(Calendar.MILLISECOND, -gmtMillisecondOffset);
      return ((PrimitiveDomainType) getDomainType()).convertToString(date);
    } catch (Exception e) {
      throw new OBException(e);
    }
  }

}
