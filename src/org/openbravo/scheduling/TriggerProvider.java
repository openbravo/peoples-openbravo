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
 * All portions are Copyright (C) 2019 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.scheduling;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.text.ParseException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.inject.Qualifier;
import javax.servlet.ServletException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.quartz.Trigger;

/**
 * Provides {@link Trigger} instances configured with the timing options and schedules defined with
 * a process request.
 */
@ApplicationScoped
class TriggerProvider {
  private static final Logger log = LogManager.getLogger();

  @Inject
  @Any
  private Instance<TriggerGenerator> triggerGenerators;

  /**
   * Loads the trigger details from AD_PROCESS_REQUEST and converts them into a schedulable Quartz
   * Trigger instance.
   * 
   * @param requestId
   *          the ID of the AD_PROCESS_REQUEST. It is also used as the name element for the
   *          Trigger's TriggerKey
   * @param bundle
   *          the ProcessBundle to be included into the Trigger's job data map
   * @param conn
   *          the ConnectionProvider used to load the AD_PROCESS_REQUEST information
   * @return a Trigger instance configured according to the AD_PROCESS_REQUEST information retrieved
   *         from database
   */
  Trigger createTrigger(String requestId, ProcessBundle bundle, ConnectionProvider conn)
      throws ServletException {
    TriggerData data = getTriggerData(requestId, bundle, conn);
    try {
      Trigger trigger = getTriggerGenerator(data, bundle, conn).generate(requestId, data);
      trigger.getJobDataMap().put(ProcessBundle.KEY, bundle);

      log.debug("Scheduled process {} {}. Start time: {}.",
          () -> data != null ? data.processName : "",
          () -> data != null ? data.processGroupName : "", trigger::getStartTime);

      return trigger;
    } catch (ParseException e) {
      log.error("Error scheduling process {} {}", () -> data != null ? data.processName : "",
          () -> data != null ? data.processGroupName : "", () -> e);
      throw new ServletException(getErrorMessage(conn, bundle, e.getMessage()));
    }
  }

  private TriggerData getTriggerData(String requestId, ProcessBundle bundle,
      ConnectionProvider conn) throws ServletException {
    if (bundle.isGroup()) {
      return TriggerData.selectGroup(conn, requestId, GroupInfo.processGroupId);
    } else {
      return TriggerData.select(conn, requestId);
    }
  }

  private TriggerGenerator getTriggerGenerator(TriggerData data, ProcessBundle bundle,
      ConnectionProvider conn) throws ServletException {
    String timing = getTiming(data);
    try {
      return triggerGenerators.select(new Selector(timing)).get();
    } catch (UnsatisfiedResolutionException ex) {
      log.error("Couldn't get a trigger generator for timing option {}", timing, ex);
      throw new ServletException(
          getErrorMessage(conn, bundle, "Unrecognized timing option " + timing));
    }
  }

  private String getTiming(TriggerData data) {
    TimingOption timingOption = getTimingOption(data);
    if (timingOption == TimingOption.SCHEDULED) {
      return timingOption.getLabel() + Frequency.of(data.frequency).getLabel();
    } else {
      return timingOption.getLabel();
    }
  }

  private TimingOption getTimingOption(TriggerData data) {
    if (data == null || "".equals(data.timingOption)) {
      return TimingOption.IMMEDIATE;
    } else {
      return TimingOption.of(data.timingOption);
    }
  }

  private String getErrorMessage(ConnectionProvider conn, ProcessBundle bundle, String msg) {
    String language = bundle.getContext().getLanguage();
    return Utility.messageBD(conn, "TRIG_INVALID_DATA", language) + " " + msg;
  }

  /**
   * Defines the qualifier used to register a {@link TriggerGenerator}.
   */
  @Qualifier
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.TYPE })
  public @interface Timing {
    String value();
  }

  /**
   * A class used to select the correct {@link TriggerGenerator} instance.
   */
  @SuppressWarnings("all")
  private static class Selector extends AnnotationLiteral<Timing> implements Timing {
    private static final long serialVersionUID = 1L;

    private String value;

    public Selector(String value) {
      this.value = value;
    }

    @Override
    public String value() {
      return value;
    }
  }
}
