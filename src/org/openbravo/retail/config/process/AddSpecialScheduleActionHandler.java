/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.config.process;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.client.application.process.ResponseActionsBuilder.MessageType;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.retail.config.OBRETCOOrgSpecialSchedule;
import org.openbravo.service.db.DbUtility;
import org.openbravo.service.json.JsonUtils;

public class AddSpecialScheduleActionHandler extends BaseProcessActionHandler {
  private static final Logger log = LogManager.getLogger();

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
    OBContext.setAdminMode(true);
    try {
      final JSONObject jsonData = new JSONObject(content);
      final JSONObject params = jsonData.getJSONObject("_params");
      final JSONArray dateJsonArray = params.getJSONObject("Date").getJSONArray("_allRows");
      final JSONArray storeJsonArray = params.getJSONObject("Store").getJSONArray("_selection");

      int numDays = 0;
      for (int i = 0; i < dateJsonArray.length(); i++) {
        final JSONObject dateJsonObject = dateJsonArray.getJSONObject(i);
        final Date specialDate = getDate(dateJsonObject.optString("specialdate"));
        final boolean open = dateJsonObject.optBoolean("open");
        final Timestamp startingTime = getTime(dateJsonObject.optString("startingTime"));
        final Timestamp endingTime = getTime(dateJsonObject.optString("endingTime"));
        if (specialDate != null) {
          numDays++;
          for (int j = 0; j < storeJsonArray.length(); j++) {
            final String orgId = storeJsonArray.getJSONObject(j).getString("id");
            createSpecialDate(orgId, specialDate, open, startingTime, endingTime);
          }
        }
      }

      return getResponseBuilder()
          .showMsgInProcessView(MessageType.SUCCESS, OBMessageUtils.messageBD("Success"),
              String.format(OBMessageUtils.messageBD("OBRETCO_SpecialScheduleSuccess"), numDays,
                  storeJsonArray.length()))
          .build();

    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      log.error(e.getMessage(), e);
      Throwable ex = DbUtility.getUnderlyingSQLException(e);
      String msgText = OBMessageUtils.translateError(ex.getMessage()).getMessage();
      return getResponseBuilder()
          .showMsgInProcessView(MessageType.ERROR, OBMessageUtils.messageBD("Error"), msgText)
          .build();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private void createSpecialDate(final String orgId, final Date date, final boolean open,
      final Timestamp startingTime, final Timestamp endingTime) {
    final OBRETCOOrgSpecialSchedule specialDate = OBProvider.getInstance()
        .get(OBRETCOOrgSpecialSchedule.class);
    specialDate.setOrganization(OBDal.getInstance().getProxy(Organization.class, orgId));
    specialDate.setSpecialdate(date);
    specialDate.setOpen(open);
    specialDate.setStartingTime(startingTime);
    specialDate.setEndingTime(endingTime);
    OBDal.getInstance().save(specialDate);
  }

  private Date getDate(final String date) throws ParseException {
    if (StringUtils.isEmpty(date)) {
      return null;
    }

    return JsonUtils.createDateFormat().parse(date);
  }

  private Timestamp getTime(final String time) {
    if (StringUtils.isEmpty(time)) {
      return null;
    }

    final SimpleDateFormat timeFormat = JsonUtils.createJSTimeFormat();
    final OffsetDateTime offsetDateTime = LocalDateTime
        .parse(time, DateTimeFormatter.ofPattern(timeFormat.toPattern()))
        .atOffset(ZoneOffset.UTC);
    final Instant instant = offsetDateTime.atZoneSameInstant(ZoneId.systemDefault()).toInstant();
    return new Timestamp(Date.from(instant).getTime());
  }

}
