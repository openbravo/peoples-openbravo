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
 * All portions are Copyright (C) 2022 Openbravo SLU 
 * All Rights Reserved. 
 ************************************************************************
 */
package org.openbravo.financial;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.dal.core.SQLFunctionRegister;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.pricing.pricelist.PriceListVersion;

@ComponentProvider.Qualifier("4CCE605CBB914CFAB01005FBD0A8C259")
public class CreatePolinesTransformer extends PriceExceptionAbstractTransformer
    implements SQLFunctionRegister {

  @Override
  public String transformHqlQuery(String hqlQuery, Map<String, String> requestParameters,
      Map<String, Object> queryNamedParameters) {

    String documentDate = getDocumentDate(requestParameters, "@Order.orderDate@");
    String orgList = getOrganizationsList(requestParameters, "@Order.organization@");

    String priceListId = requestParameters.get("@Order.priceList@");
    String transformedHql = hqlQuery.replace("@Order.priceList@", "'" + priceListId + "'");
    transformedHql = transformedHql.replace("@Max_ValidFromDate@",
        this.getMaxValidDate(priceListId));
    transformedHql = transformedHql.replace("@leftJoinPriceExceptions@",
        getLeftJoinPriceExceptions());
    transformedHql = transformedHql.replace("@documentDate@", documentDate);
    transformedHql = transformedHql.replace("@orgList@", orgList);

    return transformedHql;
  }

  private String getMaxValidDate(String plId) {
   // @formatter:off
    final String whereClause =
            " where priceList.id = :plId" +
            "   and validFromDate <= now()"+
            " order by validFromDate desc";
    // @formatter:on
    final OBQuery<PriceListVersion> criteria = OBDal.getInstance()
        .createQuery(PriceListVersion.class, whereClause);
    criteria.setNamedParameter("plId", plId);
    criteria.setMaxResult(1);
    return "'" + criteria.uniqueResult().getValidFromDate() + "'";
  }

  @Override
  public Map<String, SQLFunction> getSQLFunctions() {
    Map<String, SQLFunction> sqlFunctions = new HashMap<>();
    sqlFunctions.put("m_get_default_aum_for_document",
        new StandardSQLFunction("m_get_default_aum_for_document", StandardBasicTypes.STRING));
    return sqlFunctions;
  }

  private String getLeftJoinPriceExceptions() {
    return "left join PricingProductPriceException ppe\n" + "    on (\n"
        + "        pp.id = ppe.productPrice.id\n" + "        and @documentDate@ is not null\n"
        + "        and ppe.organization.id in (@orgList@)\n"
        + "        and ppe.validFromDate <= @documentDate@\n"
        + "        and ppe.validToDate >= @documentDate@\n" + "        and ppe.orgdepth = ( \n"
        + "            select max(ppe2.orgdepth)\n"
        + "            from PricingProductPriceException ppe2\n"
        + "            where ppe.productPrice.id = ppe2.productPrice.id\n"
        + "            and ppe2.organization.id in (@orgList@)\n"
        + "            and ppe2.validFromDate <= @documentDate@\n"
        + "            and ppe2.validToDate >= @documentDate@\n" + "        )\n" + "    )";
  }

}
