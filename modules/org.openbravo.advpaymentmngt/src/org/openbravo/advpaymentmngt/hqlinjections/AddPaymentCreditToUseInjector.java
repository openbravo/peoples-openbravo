package org.openbravo.advpaymentmngt.hqlinjections;

import java.util.Map;

import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.service.datasource.hql.HQLInserterQualifier;
import org.openbravo.service.datasource.hql.HqlInserter;

@HQLInserterQualifier.Qualifier(tableId = "59ED9B23854A4B048CBBAE38436B99C2", injectionId = "0")
public class AddPaymentCreditToUseInjector extends HqlInserter {

  @Override
  public String insertHql(Map<String, String> requestParameters,
      Map<String, Object> queryNamedParameters) {
    final String strBusinessPartnerId = requestParameters.get("received_from");
    final BusinessPartner businessPartner = OBDal.getInstance().get(BusinessPartner.class,
        strBusinessPartnerId);
    boolean isSalesTransaction = "true".equals(requestParameters.get("issotrx")) ? true : false;
    queryNamedParameters.put("bp", businessPartner.getId());
    queryNamedParameters.put("issotrx", isSalesTransaction);
    return "f.businessPartner.id = :bp and f.receipt = :issotrx";
  }
}