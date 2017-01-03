/*
 ************************************************************************************
 * Copyright (C) 2012-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.codehaus.jettison.json.JSONException;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;

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

    // Make sure the currency has conversion rate.
    if (!POSUtils.hasCurrencyRate(posTerminal.getId())) {
      throw new JSONException("OBPOS_ConversionRateNotConfigured");
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

    String whereclausePM = " as e where e.obposApplications=:terminal and e.financialAccount is not null "
        + "and not exists (select 1 from FinancialMgmtFinAccPaymentMethod as pmacc where "
        + "pmacc.paymentMethod = e.paymentMethod.paymentMethod and pmacc.account = e.financialAccount"
        + ")";
    OBQuery<OBPOSAppPayment> queryFinAccounts = OBDal.getInstance().createQuery(
        OBPOSAppPayment.class, whereclausePM);
    queryFinAccounts.setNamedParameter("terminal", posTerminal);
    if (queryFinAccounts.list().size() > 0) {
      throw new JSONException("OBPOS_PayMethodNotConfiguredInAccount");
    }

    String whereclauseCMEV = " as e where e.obposApplications=:terminal and e.financialAccount is not null and exists "
        + "(select 1 from OBRETCO_CashManagementEvents as cmev where "
        + "cmev.financialAccount = e.financialAccount)";
    OBQuery<OBPOSAppPayment> queryEventAccounts = OBDal.getInstance().createQuery(
        OBPOSAppPayment.class, whereclauseCMEV);
    queryEventAccounts.setNamedParameter("terminal", posTerminal);
    if (queryEventAccounts.list().size() > 0) {
      throw new JSONException("OBPOS_CMEVAccountIsUsedInPayMethod");
    }

    String whereclauseLAC = " as e where e.obposApplications=:terminal and ((e.financialAccount is null "
        + "and e.paymentMethod.leaveascredit = false) or (e.financialAccount is not null and e.paymentMethod.leaveascredit = true))";
    OBQuery<OBPOSAppPayment> queryLeaveAsCredit = OBDal.getInstance().createQuery(
        OBPOSAppPayment.class, whereclauseLAC);
    queryLeaveAsCredit.setNamedParameter("terminal", posTerminal);
    if (queryLeaveAsCredit.list().size() > 0) {
      throw new JSONException("OBPOS_LeaveAsCreditNotConfigured");
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
          boolean validation = false, nameValidation = false;
          for (int j = 0; j < sharedPayments.size(); j++) {
            OBPOSAppPayment sharedPayment = sharedPayments.get(j);
            if (sharedPayment.getPaymentMethod() == appPayment.getPaymentMethod()
                && appPayment.getFinancialAccount() == sharedPayment.getFinancialAccount()) {
              validation = true;
              if (sharedPayment.getSearchKey().equals(appPayment.getSearchKey())
                  && sharedPayment.getCommercialName().equals(appPayment.getCommercialName())) {
                nameValidation = true;
              }
              break;
            }
          }
          if (!validation) {
            throw new JSONException("OBPOS_FinAccSharedPayment");
          }
          if (!nameValidation) {
            throw new JSONException("OBPOS_SharedPaymentNameConfiguration");
          }
        }
      }
    }

    if (posTerminal.getObposTerminaltype().isLayawayorder()) {
      try {
        String generateLayawaysPref = Preferences
            .getPreferenceValue("OBPOS_receipt.layawayReceipt", true, OBContext.getOBContext()
                .getCurrentClient().getId(), OBContext.getOBContext().getCurrentOrganization()
                .getId(), OBContext.getOBContext().getUser().getId(), OBContext.getOBContext()
                .getRole().getId(), null);
        if (generateLayawaysPref.equals("N")) {
          throw new JSONException("OBPOS_LayawayTerminalNoLayawayPermission");
        }
      } catch (PropertyException e) {
        // Preferences wrongly defined. Process will fail later on, so no need to do anything here.
      }
    }

    if (posTerminal.isMaster()) {
      OBCriteria<OBPOSApplications> obCriteria = OBDal.getInstance().createCriteria(
          OBPOSApplications.class);
      obCriteria.add(Restrictions.eq(OBPOSApplications.PROPERTY_MASTERTERMINAL, posTerminal));
      obCriteria.addOrderBy(OBPOSApplications.PROPERTY_SEARCHKEY, true);
      if (obCriteria.list().size() == 0) {
        throw new JSONException("OBPOS_NoSlaveTerminal");
      }
    }

    for (CustomInitialValidation customInitialValidation : getCustomInitialValidations()) {
      customInitialValidation.validation(posTerminal);
    }

  }

  private static List<CustomInitialValidation> getCustomInitialValidations() {
    List<CustomInitialValidation> result = new ArrayList<CustomInitialValidation>();
    BeanManager bm = WeldUtils.getStaticInstanceBeanManager();
    for (Bean<?> restrictionBean : bm.getBeans(CustomInitialValidation.class)) {
      result.add((CustomInitialValidation) bm.getReference(restrictionBean,
          CustomInitialValidation.class, bm.createCreationalContext(restrictionBean)));
    }
    return result;
  }
}