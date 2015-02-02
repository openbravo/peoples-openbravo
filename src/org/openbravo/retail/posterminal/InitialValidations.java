/*
 ************************************************************************************
 * Copyright (C) 2012-2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;

public class InitialValidations {

  public static void validateTerminal(OBPOSApplications posTerminal) throws JSONException {

    if (posTerminal == null || null == POSUtils.getTerminal(posTerminal.getSearchKey())) {
      throw new JSONException("OBPOS_TerminalNotFound");
    }

    // Make sure the store is associated with a PriceList.
    if (POSUtils.getPriceListByTerminal(posTerminal.getSearchKey()) == null) {
      throw new JSONException("OBPOS_errorLoadingPriceList");
    }

    // Make sure the store is associated with an Assortment.
    if (POSUtils.getProductListByOrgId(posTerminal.getOrganization().getId()) == null) {
      throw new JSONException("OBPOS_errorLoadingProductList");
    }

    if (POSUtils.getWarehouseForTerminal(posTerminal) == null) {
      throw new JSONException("OBPOS_WarehouseNotConfigured");
    }

    if (posTerminal.getOrganization().getCurrency() == null) {
      throw new JSONException("OBPOS_OrgCurrencyConfigured");
    }

    if (posTerminal.getOrganization().getOrganizationInformationList().get(0).getLocationAddress() == null) {
      throw new JSONException("OBPOS_OrgLocationConfigured");
    }

    if (!posTerminal.getOrganization().getId()
        .equals(OBContext.getOBContext().getCurrentOrganization().getId())) {
      throw new JSONException("OBPOS_OrgDesynchronization");
    }

    if (posTerminal.isMaster() && posTerminal.getMasterterminal() != null) {
      throw new JSONException("OBPOS_NotAllowSlaveAndMaster");
    }

    String whereclausePM = " as e where e.obposApplications=:terminal and not exists "
        + "(select 1 from FinancialMgmtFinAccPaymentMethod as pmacc where "
        + "pmacc.paymentMethod = e.paymentMethod.paymentMethod and pmacc.account = e.financialAccount"
        + ")";
    OBQuery<OBPOSAppPayment> queryFinAccounts = OBDal.getInstance().createQuery(
        OBPOSAppPayment.class, whereclausePM);
    queryFinAccounts.setNamedParameter("terminal", posTerminal);
    if (queryFinAccounts.list().size() > 0) {
      throw new JSONException("OBPOS_PayMethodNotConfiguredInAccount");
    }

    String whereclauseCMEV = " as e where e.obposApplications=:terminal and exists "
        + "(select 1 from OBRETCO_CashManagementEvents as cmev where "
        + "cmev.financialAccount = e.financialAccount)";
    OBQuery<OBPOSAppPayment> queryEventAccounts = OBDal.getInstance().createQuery(
        OBPOSAppPayment.class, whereclauseCMEV);
    queryEventAccounts.setNamedParameter("terminal", posTerminal);
    if (queryEventAccounts.list().size() > 0) {
      throw new JSONException("OBPOS_CMEVAccountIsUsedInPayMethod");
    }

    if (posTerminal.getMasterterminal() != null) {
      String whereclauseAppPayment = " as e where e.obposApplications=:terminal and "
          + " e.paymentMethod.isshared = 'Y' ";
      OBQuery<OBPOSAppPayment> queryAppPayment = OBDal.getInstance().createQuery(
          OBPOSAppPayment.class, whereclauseAppPayment);
      queryAppPayment.setNamedParameter("terminal", posTerminal.getMasterterminal());
      List<OBPOSAppPayment> sharedPayments = queryAppPayment.list();
      for (int i = 0; i < posTerminal.getOBPOSAppPaymentList().size(); i++) {
        OBPOSAppPayment appPayment = posTerminal.getOBPOSAppPaymentList().get(i);
        if (appPayment.getPaymentMethod().isShared()) {
          boolean validation = false;
          for (int j = 0; j < sharedPayments.size(); j++) {
            OBPOSAppPayment sharedPayment = sharedPayments.get(j);
            if (sharedPayment.getPaymentMethod() == appPayment.getPaymentMethod()
                && appPayment.getFinancialAccount() == sharedPayment.getFinancialAccount()) {
              validation = true;
              break;
            }
          }
          if (!validation) {
            throw new JSONException("OBPOS_FinAccSharedPayment");
          }
        }
      }
    }
  }
}
