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
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.PrintTemplate;
import org.openbravo.retail.posterminal.PrintTemplateSubrep;
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
    list.add(new HQLProperty(
        "(COALESCE(pos.defaultCustomer.id, pos.organization.obretcoCBpartner.id))",
        "businessPartner"));
    list.add(new HQLProperty("pos.name", "_identifier"));
    list.add(new HQLProperty("pos.searchKey", "searchKey"));
    list.add(new HQLProperty(
        "(COALESCE(pos.obposCBpartnerLoc.id, pos.organization.obretcoCBpLocation.id))",
        "partnerAddress"));
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
    list.add(new HQLProperty(
        "(select pos2.organization.obretcoDbpBpcatid.name from OBPOS_Applications pos2 where pos2 = pos)",
        "defaultbp_bpcategory_name"));
    list.add(new HQLProperty("pos.organization.obretcoDbpCountryid.id", "defaultbp_bpcountry"));
    list.add(new HQLProperty(
        "(select pos2.organization.obretcoDbpCountryid.name from OBPOS_Applications pos2 where pos2 = pos)",
        "defaultbp_bpcountry_name"));
    list.add(new HQLProperty("pos.organization.obretcoDbpOrgid.id", "defaultbp_bporg"));
    list.add(new HQLProperty("pos.organization.obretcoShowtaxid", "bp_showtaxid"));
    list.add(new HQLProperty("pos.organization.obretcoShowbpcategory", "bp_showcategoryselector"));
    list.add(new HQLProperty("pos.orderdocnoPrefix", "docNoPrefix"));
    list.add(new HQLProperty("pos.quotationdocnoPrefix", "quotationDocNoPrefix"));
    list.add(new HQLProperty("pos.obposTerminaltype.allowpayoncredit", "allowpayoncredit"));
    list.add(new HQLProperty("pos.defaultwebpostab", "defaultwebpostab"));
    list.add(new HQLProperty("postype", "terminalType"));

    addTemplateProperty(Organization.PROPERTY_OBPOSCASHUPTEMPLATE, "printCashUpTemplate", list);
    addTemplateProperty(Organization.PROPERTY_OBPOSTICKETTEMPLATE, "printTicketTemplate", list);
    addTemplateProperty(Organization.PROPERTY_OBPOSRETURNTEMPLATE, "printReturnTemplate", list);
    addTemplateProperty(Organization.PROPERTY_OBPOSINVOICETEMPLATE, "printInvoiceTemplate", list);
    addTemplateProperty(Organization.PROPERTY_OBPOSRETINVTEMPLATE, "printReturnInvoiceTemplate",
        list);
    addTemplateProperty(Organization.PROPERTY_OBPOSLAYAWAYTEMPLATE, "printLayawayTemplate", list);
    addTemplateProperty(Organization.PROPERTY_OBPOSCLOSEDRECEIPTTEMPLATE,
        "printClosedReceiptTemplate", list);

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

  protected void addTemplateProperty(String propertyName, String alias, List<HQLProperty> list) {
    try {
      OBContext.setAdminMode();
      PrintTemplate value = (PrintTemplate) POSUtils.getPropertyInOrgTree(OBContext.getOBContext()
          .getCurrentOrganization(), propertyName);
      if (value != null) {
        list.add(new HQLProperty("'" + value.getTemplatePath() + "'", alias));
        list.add(new HQLProperty("'" + value.isPdf() + "'", alias + "IsPdf"));
        if (value.isPdf()) {
          list.add(new HQLProperty("'" + value.getPrinter() + "'", alias + "Printer"));
        }
        int i = 0;
        for (PrintTemplateSubrep subrep : value.getOBPOSPrintTemplateSubrepList()) {
          list.add(new HQLProperty("'" + subrep.getTemplatePath() + "'", alias + "Subrep" + i));
          i++;
        }
      }
    } catch (Exception e) {
      log.error("Error getting property " + propertyName, e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}