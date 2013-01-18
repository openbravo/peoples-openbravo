/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.Property;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.service.OBDal;
import org.openbravo.service.json.JsonToDataConverter;

public class JSONPropertyToEntity {

  private static final Logger log = Logger.getLogger(CustomerLoader.class);

  public static void fillBobFromJSON(Entity entity, BaseOBObject bob, JSONObject json)
      throws JSONException {
    JSONPropertyToEntity.fillBobFromJSON(entity, bob, json, 0L);
  }

  public static void fillBobFromJSON(Entity entity, BaseOBObject bob, JSONObject json,
      Long dateOffset) throws JSONException {
    @SuppressWarnings("unchecked")
    Iterator<String> keys = json.keys();
    while (keys.hasNext()) {
      String key = keys.next();
      if (key.equals("id")) {
        continue;
      }
      String oldKey = key;
      if (entity.hasProperty(key)) {
        log.debug("Found property: " + key + " in entity " + entity.getName());
      } else {
        key = getEquivalentKey(key);
        if (key == null) {
          log.debug("Did not find property: " + oldKey);
          continue;
        } else {
          if (entity.hasProperty(key)) {
            log.debug("Found equivalent key: " + key);
          } else {
            log.debug("Did not find property: " + oldKey);
            continue;
          }
        }
      }

      Property p = entity.getProperty(key);
      Object value;
      if (json.isNull(oldKey)) {
        continue;
      } else {
        value = json.get(oldKey);
      }
      if (p.isPrimitive()) {
        if (p.isDate()) {
          Date serverDate = (Date) JsonToDataConverter.convertJsonToPropertyValue(
              PropertyByType.DATETIME,
              ((String) value).subSequence(0, ((String) value).lastIndexOf(".")));
          // date is the date in the server timezone, we need to convert it to the date in the
          // original time zone
          Date dateUTC = convertToUTC(serverDate);
          Date clientDate = new Date();
          clientDate.setTime(dateUTC.getTime() - dateOffset * 60 * 1000);
          bob.set(p.getName(), stripTime(clientDate));
        } else if (p.isDatetime()) {
          String strValue = (String) value;
          String transformedValue = (String) strValue.subSequence(0, strValue.lastIndexOf("."))
              + "+0000";
          bob.set(p.getName(), (Date) JsonToDataConverter.convertJsonToPropertyValue(
              PropertyByType.DATETIME, transformedValue));
        } else if (p.getPrimitiveType() == Long.class) {
          bob.set(p.getName(), json.getLong(oldKey));
        } else if (p.isNumericType()) {
          value = json.getString(oldKey);
          bob.set(key, new BigDecimal((String) value));
        } else {
          bob.set(p.getName(), value);
        }
      } else {
        Property refProp = p.getReferencedProperty();
        Entity refEntity = refProp.getEntity();
        if (value instanceof JSONObject) {
          value = ((JSONObject) value).getString("id");
        }
        BaseOBObject refBob = OBDal.getInstance().getProxy(refEntity.getName(), value.toString());
        bob.set(p.getName(), refBob);
      }

    }
  }

  private static Date convertToUTC(Date localTime) {
    Calendar now = Calendar.getInstance();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(localTime);
    calendar.set(Calendar.DATE, now.get(Calendar.DATE));
    calendar.set(Calendar.MONTH, now.get(Calendar.MONTH));
    calendar.set(Calendar.YEAR, now.get(Calendar.YEAR));

    int gmtMillisecondOffset = (now.get(Calendar.ZONE_OFFSET) + now.get(Calendar.DST_OFFSET));
    calendar.add(Calendar.MILLISECOND, -gmtMillisecondOffset);

    return calendar.getTime();
  }

  private static Date stripTime(Date dateWithTime) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(dateWithTime);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    return calendar.getTime();
  }

  private static String getEquivalentKey(String key) {
    if (key.equals("bp")) {
      return "businessPartner";
    } else if (key.equals("bploc")) {
      return "partnerAddress";
    } else if (key.equals("qty")) {
      return "orderedQuantity";
    } else if (key.equals("price")) {
      return "baseGrossUnitPrice";
    } else if (key.equals("posTerminal")) {
      return "obposApplications";
    } else if (key.equals("pricenet")) {
      return "unitPrice";
    } else if (key.equals("discountPercentage")) {
      return "discount";
    }

    // Mappings for promotions
    else if (key.equals("ruleId")) {
      return "priceAdjustment";
    } else if (key.equals("basePrice")) {
      return "baseGrossUnitPrice";
    } else if (key.equals("unitDiscount")) {
      return "priceAdjustmentAmt";
    }

    return null;
  }
}
