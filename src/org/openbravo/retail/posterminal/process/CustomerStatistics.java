/*
 ************************************************************************************
 * Copyright (C) 2012-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.process;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.retail.posterminal.JSONProcessSimple;
import org.openbravo.retail.posterminal.utility.CustomerStatisticsUtils;
import org.openbravo.service.json.JsonConstants;

public class CustomerStatistics extends JSONProcessSimple {
  private static final Logger log = LogManager.getLogger();

  public static final BigDecimal HOURS = new BigDecimal("24");
  public static final BigDecimal DAYSWEEK = new BigDecimal("7");
  public static final BigDecimal DAYSMONTH = new BigDecimal("30");
  public static final BigDecimal DAYSYEAR = new BigDecimal("365");

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {
    JSONObject response = new JSONObject();
    JSONObject result = new JSONObject();

    try {
      OBContext.setAdminMode(true);
      String orgId = jsonsent.getString("organization");
      String bpId = jsonsent.getString("bpId");
      String recencyMsg = null, frequencyMsg = null, monetaryValMsg = null, averageBasketMsg = null;

      // Get Timings from Org
      Organization organization = OBDal.getInstance().get(Organization.class, orgId);
      String recencyTiming = organization.getObposRecencytiming();
      String frequencyTiming = organization.getObposFrecuencytiming();
      Long frequencyTimingUnit = organization.getObposFrecuencytimingunit();
      String monetaryValueTiming = organization.getObposMonetarytiming();
      Long monetaryValueTimingUnit = organization.getObposMonetarytimingunit();
      String averageBasketTiming = organization.getObposAvgbaskettiming();
      Long averageBasketTimingUnit = organization.getObposAvgbaskettimingunit();

      // Get BPartner
      BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, bpId);
      if (bp != null) {
        // Recency Calculation
        if (!StringUtils.isEmpty(recencyTiming)) {
          recencyMsg = getRecencyStatistics(recencyTiming, bp);
        }

        // Frequency Calculation
        if (!StringUtils.isEmpty(frequencyTiming)) {
          frequencyMsg = getFrequencyStatistics(frequencyTiming, frequencyTimingUnit, bp);
        }

        // Monetary Value Calculation
        if (!StringUtils.isEmpty(monetaryValueTiming)) {
          monetaryValMsg = getMonetaryValueStatistics(monetaryValueTiming, monetaryValueTimingUnit,
              bp, organization);
        }

        // Average Basket Calculation
        if (!StringUtils.isEmpty(averageBasketTiming)) {
          averageBasketMsg = getAverageBasketStatistics(averageBasketTiming,
              averageBasketTimingUnit, bp, organization);
        }

        response.put("recencyMsg", recencyMsg);
        response.put("frequencyMsg", frequencyMsg);
        response.put("monetaryValMsg", monetaryValMsg);
        response.put("averageBasketMsg", averageBasketMsg);

        result.put(JsonConstants.RESPONSE_DATA, response);
        result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      } else {
        String errorMsg = "New client, no statistics. ";
        result.put(JsonConstants.RESPONSE_DATA, errorMsg);
        result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      }
      return result;
    } catch (Exception e) {
      log.error("Error while calculating statistics value", e);
      JSONObject jsonError = new JSONObject();
      String errorMsg = "Error while calculating statistics value : " + e.getMessage();
      jsonError.put("message", errorMsg);
      result.put(JsonConstants.RESPONSE_ERROR, jsonError);
      result.put(JsonConstants.RESPONSE_ERRORMESSAGE, errorMsg);
      result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_FAILURE);
      return result;
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  private String getRecencyStatistics(String recencyTiming, BusinessPartner bp) {
    String recencyMsg = null;
    BigDecimal noofRecency = BigDecimal.ZERO;

    // @formatter:off
    String recencyHQLQuery = "select orderDate from Order "
        + "where businessPartner.id=:bpartnerId order by orderDate desc";
    // @formatter:on

    final Session recencySession = OBDal.getInstance().getSession();
    final Query<Date> recencyQuery = recencySession.createQuery(recencyHQLQuery, Date.class);
    recencyQuery.setParameter("bpartnerId", bp.getId());
    recencyQuery.setMaxResults(1);
    Date lastOrderDate = recencyQuery.uniqueResult();
    Long recency = FIN_Utility.getDaysBetween(new Date(),
        lastOrderDate != null ? lastOrderDate : new Date());
    BigDecimal recencyDays = new BigDecimal(Math.abs(recency));

    recencyMsg = getRecencyMessage(recencyTiming, noofRecency, recencyDays);
    return recencyMsg;
  }

  private String getRecencyMessage(String recencyTiming, BigDecimal noofRecency,
      BigDecimal recencyDays) {
    String timingText, recencyMsg = null;
    BigDecimal recency = noofRecency;
    if (recencyDays != null) {
      if (recencyTiming.equalsIgnoreCase("H")) {
        recency = recencyDays.multiply(HOURS).setScale(1, RoundingMode.HALF_UP);
      } else if (recencyTiming.equalsIgnoreCase("D")) {
        recency = recencyDays.setScale(1, RoundingMode.HALF_UP);
      } else if (recencyTiming.equalsIgnoreCase("W")) {
        recency = recencyDays.divide(DAYSWEEK, 1, RoundingMode.HALF_UP);
      } else if (recencyTiming.equalsIgnoreCase("M")) {
        recency = recencyDays.divide(DAYSMONTH, 1, RoundingMode.HALF_UP);
      } else if (recencyTiming.equalsIgnoreCase("Y")) {
        recency = recencyDays.divide(DAYSYEAR, 1, RoundingMode.HALF_UP);
      }
      recency = roundRecency(recency);
      timingText = CustomerStatisticsUtils.getTimingText(recency.longValue(), recencyTiming);
      recencyMsg = String.format(OBMessageUtils.messageBD("OBPOS_Recency_Text"), recency,
          timingText);
    }
    return recencyMsg;
  }

  private BigDecimal roundRecency(BigDecimal noofRecency) {
    BigDecimal recency = BigDecimal.ZERO;
    int decimal = noofRecency.remainder(BigDecimal.ONE)
        .movePointRight(noofRecency.scale())
        .abs()
        .intValue();
    if (3 < decimal && decimal < 7) {
      recency = new BigDecimal(noofRecency.intValue() + 0.5);
    } else {
      recency = noofRecency.setScale(0, RoundingMode.HALF_UP);
    }
    return recency;
  }

  private String getFrequencyStatistics(String frequencyTiming, Long frequencyTimingUnit,
      BusinessPartner bp) {
    Date startDate = null;
    String frequencyMsg = null;
    Map<String, Object> parameters = new HashMap<>();
    // Get Start Date
    if (frequencyTimingUnit != null && frequencyTimingUnit.compareTo(0L) > 0) {
      startDate = CustomerStatisticsUtils.getStartDateFromTimingUnit(frequencyTiming,
          frequencyTimingUnit);
    }
    // @formatter:off
      String frequencyHQLQuery = "select count(id) as frequency from Order "
          + " where businessPartner.id=:bpartnerId "
          + " and orderDate < now() ";
      if (startDate != null) {
        frequencyHQLQuery +=  " and orderDate>= :startDate ";
        parameters.put("startDate", startDate);
      }
    // @formatter:on
    final Session frequencySession = OBDal.getInstance().getSession();
    final Query<Long> frequencyQuery = frequencySession.createQuery(frequencyHQLQuery, Long.class);
    parameters.put("bpartnerId", bp.getId());
    frequencyQuery.setProperties(parameters);
    BigDecimal frequency = new BigDecimal(frequencyQuery.uniqueResult());
    frequencyMsg = getFrecuencyMessage(frequencyTiming, frequencyTimingUnit, frequency);
    return frequencyMsg;
  }

  private String getFrecuencyMessage(String frequencyTiming, Long frequencyTimingUnit,
      BigDecimal frequency) {
    String frequencyMsg;
    if (frequency.equals(BigDecimal.ONE)) {
      frequencyMsg = getStatisticsMessage(frequencyTiming, frequencyTimingUnit, frequency,
          "OBPOS_Frequency_Simple_Text", "OBPOS_Frequency_Simple_Text_Unit",
          "OBPOS_Frequency_Simple_Text_NoTiming", null);
    } else {
      frequencyMsg = getStatisticsMessage(frequencyTiming, frequencyTimingUnit, frequency,
          "OBPOS_Frequency_Text", "OBPOS_Frequency_Text_Unit", "OBPOS_Frequency_Text_NoTiming",
          null);
    }
    return frequencyMsg;
  }

  private String getMonetaryValueStatistics(String monetaryValueTiming,
      Long monetaryValueTimingUnit, BusinessPartner bp, Organization org) {
    Date startDate = null;
    String monetaryValMsg = null, currencySymbol = "";
    Map<String, Object> parameters = new HashMap<>();

    // Get Start Date
    startDate = null;
    if (monetaryValueTimingUnit != null && monetaryValueTimingUnit.compareTo(0L) > 0) {
      startDate = CustomerStatisticsUtils.getStartDateFromTimingUnit(monetaryValueTiming,
          monetaryValueTimingUnit);
    }
    // Currency
    if (org.getCurrency() != null) {
      currencySymbol = org.getCurrency().getSymbol();
    }

    // @formatter:off
      String monetaryValueHQLQuery = "select coalesce(sum(o.grandTotalAmount), 0) from Order o "
          + " join o.documentType as dt "
          + " where o.businessPartner.id = :bpartnerId and dt.sOSubType <> 'OB' "
          + " and o.orderDate < now() ";
      if (startDate != null) {
        monetaryValueHQLQuery += " and o.orderDate>= :startDate ";
        parameters.put("startDate", startDate);
      }
      // @formatter:on
    final Session monetaryValueSession = OBDal.getInstance().getSession();
    final Query<BigDecimal> monetaryValueQuery = monetaryValueSession
        .createQuery(monetaryValueHQLQuery, BigDecimal.class);
    parameters.put("bpartnerId", bp.getId());
    monetaryValueQuery.setProperties(parameters);
    BigDecimal monetaryValue = monetaryValueQuery.uniqueResult().setScale(2, RoundingMode.HALF_UP);
    monetaryValMsg = getStatisticsMessage(monetaryValueTiming, monetaryValueTimingUnit,
        monetaryValue, "OBPOS_MonetaryText", "OBPOS_MonetaryText_Unit",
        "OBPOS_MonetaryText_NoTiming", currencySymbol);
    return monetaryValMsg;
  }

  private String getAverageBasketStatistics(String averageBasketTiming,
      Long averageBasketTimingUnit, BusinessPartner bp, Organization org) {
    Date startDate = null;
    String averageBasketMsg = null, currencySymbol = "";
    Map<String, Object> parameters = new HashMap<>();
    // Get Start Date
    startDate = null;
    if (averageBasketTimingUnit != null && averageBasketTimingUnit.compareTo(0L) > 0) {
      startDate = CustomerStatisticsUtils.getStartDateFromTimingUnit(averageBasketTiming,
          averageBasketTimingUnit);
    }
    // Currency
    if (org.getCurrency() != null) {
      currencySymbol = org.getCurrency().getSymbol();
    }
    // @formatter:off
      String averageBasketHQLQuery = "select coalesce(TRUNC((sum(o.grandTotalAmount)/count(o.id)),2), 0) from Order o "
          + " join o.documentType as dt "
          + " where o.businessPartner.id = :bpartnerId and dt.sOSubType <> 'OB' "
          + " and dt.return = 'N' "
          + " and o.orderDate < now()";
      if (startDate != null) {
        averageBasketHQLQuery += " and o.orderDate>= :startDate ";
        parameters.put("startDate", startDate);
      }
      // @formatter:on
    final Session averageBasketSession = OBDal.getInstance().getSession();
    final Query<BigDecimal> averageBasketQuery = averageBasketSession
        .createQuery(averageBasketHQLQuery, BigDecimal.class);
    parameters.put("bpartnerId", bp.getId());
    averageBasketQuery.setProperties(parameters);
    BigDecimal averageBasketValue = averageBasketQuery.uniqueResult()
        .setScale(2, RoundingMode.HALF_UP);

    averageBasketMsg = getStatisticsMessage(averageBasketTiming, averageBasketTimingUnit,
        averageBasketValue, "OBPOS_AverageBasket", "OBPOS_AverageBasket_Unit", "", currencySymbol);

    return averageBasketMsg;
  }

  private String getStatisticsMessage(String timing, Long timingUnit, BigDecimal monetaryValue,
      String messageText, String messageUnit, String messageNoTiming, String currencySymbol) {
    String timingText;
    String statisticMsg;
    if (currencySymbol == null) {
      if (timingUnit != null) {
        timingText = CustomerStatisticsUtils.getTimingText(timingUnit, timing);
        if (timingUnit.compareTo(1L) > 0) {
          statisticMsg = String.format(OBMessageUtils.messageBD(messageText), monetaryValue,
              timingUnit, timingText);
        } else {
          statisticMsg = String.format(OBMessageUtils.messageBD(messageUnit), monetaryValue,
              timingText);
        }
      } else {
        if (messageNoTiming != "") {
          statisticMsg = String.format(OBMessageUtils.messageBD(messageNoTiming), monetaryValue);
        } else {
          statisticMsg = monetaryValue.toString();
        }
      }
    } else {
      if (timingUnit != null) {
        timingText = CustomerStatisticsUtils.getTimingText(timingUnit, timing);
        if (timingUnit.compareTo(1L) > 0) {
          statisticMsg = String.format(OBMessageUtils.messageBD(messageText), monetaryValue,
              currencySymbol, timingUnit, timingText);
        } else {
          statisticMsg = String.format(OBMessageUtils.messageBD(messageUnit), monetaryValue,
              currencySymbol, timingText);
        }
      } else {
        if (messageNoTiming != "") {
          statisticMsg = String.format(OBMessageUtils.messageBD(messageNoTiming), monetaryValue,
              currencySymbol);
        } else {
          statisticMsg = monetaryValue + currencySymbol;
        }
      }
    }
    return statisticMsg;
  }
}
