/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.term;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.service.json.JsonConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Qualifier(Terminal.terminalPropertyExtension)
public class TerminalProperties extends ModelExtension {

  private static Logger log = LoggerFactory.getLogger(TerminalProperties.class);

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {

    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>();
    list.add(new HQLProperty("pos.id", "id"));
    list.add(new HQLProperty("pos.organization.obretcoCBpartner.id", "businessPartner"));
    list.add(new HQLProperty("pos.name", "_identifier"));
    list.add(new HQLProperty("pos.searchKey", "searchKey"));
    list.add(new HQLProperty("pos.organization.obretcoCBpLocation.id", "partnerAddress"));
    list.add(new HQLProperty("pos.organization.id", "organization"));
    list.add(new HQLProperty("pos.organization.name", getIdentifierAlias("organization")));
    list.add(new HQLProperty("pos.client.id", "client"));
    list.add(new HQLProperty("pos.client.name", getIdentifierAlias("client")));
    list.add(new HQLProperty("pos.hardwareurl", "hardwareurl"));
    list.add(new HQLProperty("pos.scaleurl", "scaleurl"));
    list.add(new HQLProperty("pos.organization.obretcoDbpIrulesid", "defaultbp_invoiceterm"));
    list.add(new HQLProperty("pos.organization.obretcoDbpPtermid.id", "defaultbp_paymentterm"));
    list.add(new HQLProperty("pos.organization.obretcoDbpPmethodid.id", "defaultbp_paymentmethod"));
    list.add(new HQLProperty("pos.organization.obretcoDbpBpcatid.id", "defaultbp_bpcategory"));
    list.add(new HQLProperty("pos.organization.obretcoDbpBpcatid.id", "defaultbp_bpcategory_name"));
    list.add(new HQLProperty("pos.organization.obretcoDbpCountryid.id", "defaultbp_bpcountry"));
    list.add(new HQLProperty("pos.organization.obretcoDbpOrgid.id", "defaultbp_bporg"));
    list.add(new HQLProperty("pos.organization.obretcoShowtaxid", "bp_showtaxid"));
    list.add(new HQLProperty("pos.organization.obretcoShowbpcategory", "bp_showcategoryselector"));
    list.add(new HQLProperty("pos.orderdocnoPrefix", "docNoPrefix"));
    list.add(new HQLProperty("pos.quotationdocnoPrefix", "quotationDocNoPrefix"));
    list.add(new HQLProperty("pos.obposTerminaltype.allowpayoncredit", "allowpayoncredit"));
    list.add(new HQLProperty("pos.defaultwebpostab", "defaultwebpostab"));
    list.add(new HQLProperty("postype", "terminalType"));

    String posId = RequestContext.get().getSessionAttribute("POSTerminal").toString();
    OBPOSApplications pOSTerminal = POSUtils.getTerminalById(posId);
    try {
      OBContext.setAdminMode();

      final List<String> orgList = POSUtils.getOrgList(pOSTerminal.getSearchKey());
      boolean foundCashUpTemplate = false;
      boolean foundTicketTemplate = false;

      for (String orgId : orgList) {
        final Organization org = OBDal.getInstance().get(Organization.class, orgId);
        if (!foundCashUpTemplate && org.getObposCashupTemplate() != null) {
          list.add(new HQLProperty("'" + org.getObposCashupTemplate().getTemplatePath() + "'",
              "printCashUpTemplate"));
          foundCashUpTemplate = true;
        }

        if (!foundTicketTemplate && org.getObposTicketTemplate() != null) {
          list.add(new HQLProperty("'" + org.getObposTicketTemplate().getTemplatePath() + "'",
              "printTicketTemplate"));
        }

        if (foundCashUpTemplate && foundTicketTemplate) {
          break;
        }
      }
    } catch (Exception e) {
      log.error("Error getting templates for terminal " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return list;
  }

  protected String getTemplateHQLForProperty(String property) {
    // cannot query directly for template path: it is a compound hql path that results in a SQL
    // inner join not returning null values
    return "(select case when t is not null then t.templatePath else null end from OBPOS_Print_Template t where t = "
        + property + ")";
  }

  private String getIdentifierAlias(String propertyName) {
    return propertyName + DalUtil.FIELDSEPARATOR + JsonConstants.IDENTIFIER;
  }
}