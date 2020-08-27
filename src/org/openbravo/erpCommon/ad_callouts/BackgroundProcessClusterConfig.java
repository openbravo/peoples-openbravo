package org.openbravo.erpCommon.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.erpCommon.utility.Utility;

/**
 * Generates a warning message on BackgroundProcess cluster config modification to inform user to
 * reschedule affected Process Requests
 */
public class BackgroundProcessClusterConfig extends SimpleCallout {
  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    info.showWarning(
        Utility.messageBD(this, "ShouldRescheduleProcessRequests", info.vars.getLanguage()));
  }
}
