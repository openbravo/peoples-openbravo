/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.term;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.model.ad.domain.ModelImplementation;
import org.openbravo.model.ad.domain.ModelImplementationParameter;
import org.openbravo.model.common.enterprise.OrganizationInformation;
import org.openbravo.retail.posterminal.InitialValidations;
import org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;
import org.openbravo.service.json.JsonConstants;

public class Terminal extends ProcessHQLQuery {
  public static final String terminalPropertyExtension = "OBPOS_TerminalExtension";

  @Inject
  @Any
  @Qualifier(terminalPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected boolean isAdminMode() {
    return true;
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    String posId = RequestContext.get().getSessionAttribute("POSTerminal").toString();
    OBPOSApplications pOSTerminal = POSUtils.getTerminalById(posId);

    // INITIAL VALIDATIONS
    InitialValidations.validateTerminal(pOSTerminal);

    // saving quotations doc id to prevent session to be lost in getLastDocumentNumberForPOS
    String quotationsDocTypeId = pOSTerminal.getObposTerminaltype().getDocumentTypeForQuotations() == null ? null
        : pOSTerminal.getObposTerminaltype().getDocumentTypeForQuotations().getId();
    List<String> doctypeIds = new ArrayList<String>();
    doctypeIds.add(pOSTerminal.getObposTerminaltype().getDocumentType().getId());
    doctypeIds.add(pOSTerminal.getObposTerminaltype().getDocumentTypeForReturns().getId());
    int lastDocumentNumber = POSUtils.getLastDocumentNumberForPOS(pOSTerminal.getSearchKey(),
        doctypeIds);
    int lastQuotationDocumentNumber = 0;
    if (quotationsDocTypeId != null) {
      lastQuotationDocumentNumber = POSUtils.getLastDocumentNumberQuotationForPOS(
          pOSTerminal.getSearchKey(), quotationsDocTypeId);
    }
    String warehouseId = POSUtils.getWarehouseForTerminal(pOSTerminal).getId();
    final org.openbravo.model.pricing.pricelist.PriceList pricesList = POSUtils
        .getPriceListByTerminal(pOSTerminal.getSearchKey());

    HQLPropertyList regularTerminalHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    final OrganizationInformation myOrgInfo = pOSTerminal.getOrganization()
        .getOrganizationInformationList().get(0);
    String regionId = "";
    String countryId = "";
    if (myOrgInfo.getLocationAddress().getRegion() != null) {
      regionId = myOrgInfo.getLocationAddress().getRegion().getId();
    }

    if (myOrgInfo.getLocationAddress().getCountry() != null) {
      countryId = myOrgInfo.getLocationAddress().getCountry().getId();
    }
    String selectOrgImage = "";
    String fromOrgImage = "";
    String whereOrgImage = "";
    if (myOrgInfo.getYourCompanyDocumentImage() != null) {

      selectOrgImage = " image.bindaryData as organizationImage,"
          + "image.mimetype as organizationImageMime,";
      fromOrgImage = ", ADImage image ";
      whereOrgImage = " and image.id ='" + myOrgInfo.getYourCompanyDocumentImage().getId() + "'";
    }

    int sessionTimeout;
    try {
      String sessionShouldExpire = Preferences.getPreferenceValue("OBPOS_SessionExpiration", true,
          OBContext.getOBContext().getCurrentClient(), OBContext.getOBContext()
              .getCurrentOrganization(), OBContext.getOBContext().getUser(), OBContext
              .getOBContext().getRole(), null);
      if (sessionShouldExpire.equalsIgnoreCase("Y")) {
        sessionTimeout = 0;
      } else {
        sessionTimeout = getSessionTimeoutFromDatabase();
      }
    } catch (PropertyException e) {
      sessionTimeout = getSessionTimeoutFromDatabase();
    }

    return Arrays.asList(new String[] { "select " + "'" + pricesList.getId() + "' as priceList, "
        + "'" + pricesList.getCurrency().getId() + "' as currency, " + "'"
        + pricesList.getCurrency().getIdentifier() + "' as " + getIdentifierAlias("currency")
        + ", " + "'" + pricesList.getCurrency().isCurrencySymbolAtTheRight()
        + "' as currencySymbolAtTheRight, " + "'" + pricesList.getCurrency().getSymbol()
        + "' as symbol, " + "'" + warehouseId + "' as warehouse, " + lastDocumentNumber
        + " as lastDocumentNumber, " + lastQuotationDocumentNumber
        + " as lastQuotationDocumentNumber, " + "'" + regionId + "'" + " as organizationRegionId, "
        + "'" + countryId + "'" + " as organizationCountryId, " + sessionTimeout
        + " as sessionTimeout, " + selectOrgImage + regularTerminalHQLProperties.getHqlSelect()
        + " from OBPOS_Applications AS pos inner join pos.obposTerminaltype as postype "
        + fromOrgImage
        + " where pos.$readableCriteria and pos.$activeCriteria and pos.searchKey = '"
        + pOSTerminal.getSearchKey() + "' " + whereOrgImage });
  }

  private String getIdentifierAlias(String propertyName) {
    return propertyName + DalUtil.FIELDSEPARATOR + JsonConstants.IDENTIFIER;
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }

  private int getSessionTimeoutFromDatabase() {
    OBCriteria<ModelImplementation> timeoutModelObjectCrit = OBDal.getInstance().createCriteria(
        ModelImplementation.class);
    timeoutModelObjectCrit.add(Restrictions.eq(ModelImplementation.PROPERTY_OBJECTTYPE, "ST"));
    ModelImplementation timeoutModelObj = (ModelImplementation) timeoutModelObjectCrit
        .uniqueResult();
    for (ModelImplementationParameter param : timeoutModelObj.getModelImplementationParameterList()) {
      if (param.getName().equalsIgnoreCase("Timeout")) {
        return Integer.parseInt(param.getSearchKey()) - 1;
      }
    }
    return 59;
  }
}
