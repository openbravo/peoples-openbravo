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
 * All portions are Copyright (C) 2018 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.common.actionhandler.createlinesfromprocess;

import java.util.HashMap;

import javax.enterprise.context.Dependent;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.AccDefUtility;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.financialmgmt.calendar.Period;

@Dependent
@Qualifier(CreateLinesFromProcessImplementationInterface.CREATE_LINES_FROM_PROCESS_HOOK_QUALIFIER)
class UpdateAccAndDefPlanFromProduct implements CreateLinesFromProcessImplementationInterface {

  @Override
  public int getOrder() {
    return -20;
  }

  /**
   * Calculate Acc and Def Plan from Product
   */
  @Override
  public void exec(Invoice currentInvoice, final JSONObject pickExecuteLineValues,
      BaseOBObject copiedLine, InvoiceLine newInvoiceLine) {
    boolean isDeferred = false;
    HashMap<String, String> accDefPlanData = AccDefUtility.getDeferredPlanForInvoiceProduct(
        currentInvoice.getId(), newInvoiceLine.getProduct().getId());
    String planType = accDefPlanData.get("planType");
    String periodNumber = accDefPlanData.get("periodNumber");
    String startingPeriodId = accDefPlanData.get("startingPeriodId");
    if (StringUtils.isNotEmpty(planType) && StringUtils.isNotEmpty(periodNumber)
        && StringUtils.isNotEmpty(startingPeriodId)) {
      isDeferred = true;
      newInvoiceLine.setDeferredPlanType(planType);
      newInvoiceLine.setPeriodNumber(Long.valueOf(periodNumber));
      newInvoiceLine.setPeriod(OBDal.getInstance().get(Period.class, startingPeriodId));
    }
    newInvoiceLine.setDeferred(isDeferred);
  }
}
