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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_callouts;

import java.util.List;

import javax.servlet.ServletException;

import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.system.DimensionMapping;

public class SE_DimensionDocBaseType extends SimpleCallout {

  private static final long serialVersionUID = 1L;
  // LEVELS
  final String LEVEL_HEADER = "H";
  final String LEVEL_LINE = "L";
  final String LEVEL_BREAKDOWN = "BD";
  // DIMENSIONS
  final String DIMENSION_BUSINESSPARTNER = "BP";
  final String DIMENSION_PRODUCT = "PR";
  final String DIMENSION_PROJECT = "PJ";
  final String DIMENSION_COSTCENTER = "CC";
  final String DIMENSION_USER1 = "U1";
  final String DIMENSION_USER2 = "U2";

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    final String strDocBaseType = info.getStringParameter("inpdocbasetype", null);
    final String strDimension = info.getStringParameter("inpdimension", null);
    // Compute header
    java.util.List<DimensionMapping> dMapping = getMapping(strDocBaseType, strDimension,
        LEVEL_HEADER);
    if (dMapping.size() > 0) {
      info.addResult("inpdimShowH", "Y");
      info.addResult("inpdimRoH", dMapping.get(0).isMandatory() ? "Y" : "N");
    } else {
      info.addResult("inpdimShowH", "N");
      info.addResult("inpdimRoH", "N");
    }

    // Compute Lines
    dMapping = getMapping(strDocBaseType, strDimension, LEVEL_LINE);
    if (dMapping.size() > 0) {
      info.addResult("inpdimShowL", "Y");
      info.addResult("inpdimRoL", dMapping.get(0).isMandatory() ? "Y" : "N");
    } else {
      info.addResult("inpdimShowL", "N");
      info.addResult("inpdimRoL", "N");
    }

    // Compute breakdown
    dMapping = getMapping(strDocBaseType, strDimension, LEVEL_BREAKDOWN);
    if (dMapping.size() > 0) {
      info.addResult("inpdimShowBd", "Y");
      info.addResult("inpdimRoBd", dMapping.get(0).isMandatory() ? "Y" : "N");
    } else {
      info.addResult("inpdimShowBd", "N");
      info.addResult("inpdimRoBd", "N");
    }
    Client client = OBContext.getOBContext().getCurrentClient();
    info.addResult("inpshowInHeader", getValue(client, LEVEL_HEADER, strDimension));
    info.addResult("inpshowInLines", getValue(client, LEVEL_LINE, strDimension));
    info.addResult("inpshowInBreakdown", getValue(client, LEVEL_BREAKDOWN, strDimension));

  }

  List<DimensionMapping> getMapping(String docbaseType, String dimension, String level) {
    OBCriteria<DimensionMapping> odm = OBDal.getInstance().createCriteria(DimensionMapping.class);
    odm.add(Restrictions.eq(DimensionMapping.PROPERTY_DOCUMENTCATEGORY, docbaseType));
    odm.add(Restrictions.eq(DimensionMapping.PROPERTY_ACCOUNTINGDIMENSION, dimension));
    odm.add(Restrictions.eq(DimensionMapping.PROPERTY_LEVEL, level));
    return odm.list();
  }

  private String getValue(Client client, String level, String dimension) {
    if (DIMENSION_BUSINESSPARTNER.equals(dimension)) {
      if (LEVEL_HEADER.equals(level)) {
        return client.isBpartnerAcctdimHeader() ? "Y" : "N";
      } else if (LEVEL_LINE.equals(level)) {
        return client.isBpartnerAcctdimLines() ? "Y" : "N";
      } else {
        return client.isBpartnerAcctdimLines() ? "Y" : "N";
      }
    } else if (DIMENSION_PRODUCT.equals(dimension)) {
      if (LEVEL_HEADER.equals(level)) {
        return client.isProductAcctdimHeader() ? "Y" : "N";
      } else if (LEVEL_LINE.equals(level)) {
        return client.isProductAcctdimLines() ? "Y" : "N";
      } else {
        return client.isProductAcctdimBreakdown() ? "Y" : "N";
      }
    } else if (DIMENSION_PROJECT.equals(dimension)) {
      if (LEVEL_HEADER.equals(level)) {
        return client.isProjectAcctdimHeader() ? "Y" : "N";
      } else if (LEVEL_LINE.equals(level)) {
        return client.isProjectAcctdimLines() ? "Y" : "N";
      } else {
        return client.isProjectAcctdimBreakdown() ? "Y" : "N";
      }
    } else if (DIMENSION_COSTCENTER.equals(dimension)) {
      if (LEVEL_HEADER.equals(level)) {
        return client.isCostcenterAcctdimHeader() ? "Y" : "N";
      } else if (LEVEL_LINE.equals(level)) {
        return client.isCostcenterAcctdimLines() ? "Y" : "N";
      } else {
        return client.isCostcenterAcctdimBreakdown() ? "Y" : "N";
      }
    } else if (DIMENSION_USER1.equals(dimension)) {
      if (LEVEL_HEADER.equals(level)) {
        return client.isUser1AcctdimHeader() ? "Y" : "N";
      } else if (LEVEL_LINE.equals(level)) {
        return client.isUser1AcctdimLines() ? "Y" : "N";
      } else {
        return client.isUser1AcctdimBreakdown() ? "Y" : "N";
      }
    } else if (DIMENSION_USER2.equals(dimension)) {
      if (LEVEL_HEADER.equals(level)) {
        return client.isUser2AcctdimHeader() ? "Y" : "N";
      } else if (LEVEL_LINE.equals(level)) {
        return client.isUser2AcctdimLines() ? "Y" : "N";
      } else {
        return client.isUser2AcctdimBreakdown() ? "Y" : "N";
      }
    } else {
      return "N";
    }
  }
}
