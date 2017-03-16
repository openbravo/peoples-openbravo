/*
 ************************************************************************************
 * Copyright (C) 2012-2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.term;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONTokener;
import org.hibernate.Query;
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
import org.openbravo.mobile.core.process.JSONRowConverter;
import org.openbravo.mobile.core.process.SimpleQueryBuilder;
import org.openbravo.model.ad.domain.ModelImplementation;
import org.openbravo.model.ad.domain.ModelImplementationParameter;
import org.openbravo.model.common.enterprise.OrganizationInformation;
import org.openbravo.retail.posterminal.InitialValidations;
import org.openbravo.retail.posterminal.JSONProcessSimple;
import org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;
import org.openbravo.service.json.JsonConstants;

public class Terminal extends JSONProcessSimple {
  public static final String terminalPropertyExtension = "OBPOS_TerminalExtension";

  @Inject
  @Any
  @Qualifier(terminalPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Inject
  private Instance<QueryTerminalProperty> queryterminalproperties;

  @Inject
  private Instance<JSONTerminalProperty> jsonterminalproperties;

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {
    String posId = RequestContext.get().getSessionAttribute("POSTerminal").toString();
    OBPOSApplications pOSTerminal = POSUtils.getTerminalById(posId);
    try {
      OBContext.setAdminMode(false);
      // INITIAL VALIDATIONS
      InitialValidations.validateTerminal(pOSTerminal);

      // saving quotations doc id to prevent session to be lost in getLastDocumentNumberForPOS
      String quotationsDocTypeId = pOSTerminal.getObposTerminaltype()
          .getDocumentTypeForQuotations() == null ? null : pOSTerminal.getObposTerminaltype()
          .getDocumentTypeForQuotations().getId();
      // saving returns doc id to prevent session to be lost in getLastDocumentNumberForPOS
      String returnsDocTypeId = pOSTerminal.getObposTerminaltype().getDocumentTypeForReturns()
          .getId();
      List<String> doctypeIds = new ArrayList<String>();
      doctypeIds.add(pOSTerminal.getObposTerminaltype().getDocumentType().getId());
      if (pOSTerminal.getReturndocnoPrefix() == null)
        doctypeIds.add(pOSTerminal.getObposTerminaltype().getDocumentTypeForReturns().getId());
      int lastDocumentNumber = POSUtils.getLastDocumentNumberForPOS(pOSTerminal.getSearchKey(),
          doctypeIds);
      int lastQuotationDocumentNumber = 0;
      if (quotationsDocTypeId != null) {
        lastQuotationDocumentNumber = POSUtils.getLastDocumentNumberQuotationForPOS(
            pOSTerminal.getSearchKey(), quotationsDocTypeId);
      }
      int lastReturnDocumentNumber = 0;
      if (returnsDocTypeId != null) {
        lastReturnDocumentNumber = POSUtils.getLastDocumentNumberReturnForPOS(
            pOSTerminal.getSearchKey(), returnsDocTypeId);
      }
      String warehouseId = POSUtils.getWarehouseForTerminal(pOSTerminal).getId();
      final org.openbravo.model.pricing.pricelist.PriceList pricesList = POSUtils
          .getPriceListByTerminal(pOSTerminal.getSearchKey());

      HQLPropertyList regularTerminalHQLProperties = ModelExtensionUtils
          .getPropertyExtensions(extensions);

      final OrganizationInformation myOrgInfo = pOSTerminal.getOrganization()
          .getOrganizationInformationList().get(0);

      String storeAddress = "";
      String regionId = "";
      String countryId = "";

      if (myOrgInfo.getLocationAddress() != null
          && myOrgInfo.getLocationAddress().getIdentifier().length() > 0) {
        storeAddress = myOrgInfo.getLocationAddress().getIdentifier();
      }

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
        whereOrgImage = " and image.id = :imageId ";
      }

      int sessionTimeout;
      int serverTimeout;
      try {
        String sessionShouldExpire = Preferences.getPreferenceValue("OBPOS_SessionTimeout", true,
            OBContext.getOBContext().getCurrentClient(), OBContext.getOBContext()
                .getCurrentOrganization(), OBContext.getOBContext().getUser(), OBContext
                .getOBContext().getRole(), null);
        try {
          sessionTimeout = sessionShouldExpire == null ? 0 : Integer.parseInt(sessionShouldExpire);
        } catch (NumberFormatException nfe) {
          sessionTimeout = 0;
        }
      } catch (PropertyException e) {
        sessionTimeout = 0;
      }

      serverTimeout = getSessionTimeoutFromDatabase();

      String terminalhqlquery = "select " + "'"
          + pricesList.getId()
          + "' as priceList, "
          + "'"
          + pricesList.getCurrency().getId()
          + "' as currency, "
          + "'"
          + pricesList.getCurrency().getIdentifier()
          + "' as "
          + getIdentifierAlias("currency")
          + ", "
          + "'"
          + pricesList.getCurrency().isCurrencySymbolAtTheRight()
          + "' as currencySymbolAtTheRight, "
          + "'"
          + pricesList.getCurrency().getSymbol()
          + "' as symbol, "
          + "'"
          + warehouseId
          + "' as warehouse, "
          + lastDocumentNumber
          + " as lastDocumentNumber, "
          + lastQuotationDocumentNumber
          + " as lastQuotationDocumentNumber, "
          + lastReturnDocumentNumber
          + " as lastReturnDocumentNumber, "
          + "'"
          + regionId
          + "'"
          + " as organizationRegionId, "
          + "'"
          + countryId
          + "'"
          + " as organizationCountryId, '"
          + ProcessHQLQuery.escape(storeAddress)
          + "' as organizationAddressIdentifier, "
          + sessionTimeout
          + " as sessionTimeout, "
          + selectOrgImage
          + regularTerminalHQLProperties.getHqlSelect()
          + " from OBPOS_Applications AS pos inner join pos.obposTerminaltype as postype "
          + fromOrgImage
          + " where pos.$readableSimpleCriteria and pos.$activeCriteria  and pos.searchKey = :searchKey "
          + whereOrgImage;

      SimpleQueryBuilder querybuilder = new SimpleQueryBuilder(terminalhqlquery, OBContext
          .getOBContext().getCurrentClient().getId(), OBContext.getOBContext()
          .getCurrentOrganization().getId(), null, null, null);

      final Query terminalquery = querybuilder.getDalQuery();
      terminalquery.setParameter("searchKey", pOSTerminal.getSearchKey());
      if (myOrgInfo.getYourCompanyDocumentImage() != null) {
        terminalquery.setParameter("imageId", myOrgInfo.getYourCompanyDocumentImage().getId());
      }
      StringWriter w = new StringWriter();
      JSONRowConverter.startResponse(w);
      int totalRows = ProcessHQLQuery.StrategyQueryScroll.buildResponse(w, terminalquery, true);
      JSONRowConverter.endResponse(w, totalRows);
      JSONArray arrayresult = new JSONArray();
      JSONObject aux = new JSONObject();
      arrayresult.put(aux.put("terminal",
          (new JSONArray(new JSONTokener(new JSONObject("{" + w.toString() + "}").get("data")
              .toString())).get(0))));

      for (Iterator<QueryTerminalProperty> queryIter = queryterminalproperties.iterator(); queryIter
          .hasNext();) {
        StringWriter queryWriter = new StringWriter();
        QueryTerminalProperty queryterminal = queryIter.next();
        queryterminal.exec(queryWriter, jsonsent);
        JSONObject queryaux = new JSONObject();

        if (queryterminal.returnList()) {
          queryaux.put(queryterminal.getProperty(), new JSONArray(new JSONTokener(new JSONObject(
              "{" + queryWriter.toString() + "}").get("data").toString())).get(0));
        } else {
          queryaux.put(queryterminal.getProperty(), new JSONArray(new JSONTokener(new JSONObject(
              "{" + queryWriter.toString() + "}").get("data").toString())));
        }
        arrayresult.put(queryaux);
      }
      for (Iterator<JSONTerminalProperty> jsonIter = jsonterminalproperties.iterator(); jsonIter
          .hasNext();) {
        JSONTerminalProperty jsonterminal = jsonIter.next();
        JSONObject jsonobj = jsonterminal.exec(jsonsent);
        JSONObject jsonaux = new JSONObject();
        jsonaux.put(jsonterminal.getProperty(), jsonobj.get("data"));

        arrayresult.put(jsonaux);
      }
      // Set server timeout in response
      JSONObject serverTimeoutObject = new JSONObject();
      serverTimeoutObject.put("serverTimeout", serverTimeout);
      arrayresult.put(serverTimeoutObject);

      JSONObject result = new JSONObject();
      result.put("data", arrayresult);
      result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      result.put("result", "0");
      return result;

    } catch (IOException e) {
      return null;
    } finally {
      OBContext.restorePreviousMode();
    }
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
        return Integer.parseInt(param.getSearchKey());
      }
    }
    return 59;
  }

}
