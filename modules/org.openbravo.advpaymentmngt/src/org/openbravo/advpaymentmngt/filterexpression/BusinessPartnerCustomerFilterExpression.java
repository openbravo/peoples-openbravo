package org.openbravo.advpaymentmngt.filterexpression;

import java.util.Map;

import org.apache.log4j.Logger;
import org.openbravo.client.application.FilterExpression;
import org.openbravo.client.application.OBBindingsConstants;
import org.openbravo.client.application.ParameterUtils;
import org.openbravo.client.kernel.RequestContext;

public class BusinessPartnerCustomerFilterExpression implements FilterExpression {
  private Logger log = Logger.getLogger(BusinessPartnerCustomerFilterExpression.class);
  private Map<String, String> requestMap;
  private String tabId;
  private String transactionTabId = "23691259D1BD4496BCC5F32645BCA4B9";
  private String issotrx = null;
  String result = null;

  @Override
  public String getExpression(Map<String, String> _requestMap) {
    requestMap = _requestMap;
    tabId = requestMap.get(OBBindingsConstants.TAB_ID_PARAM);
    if (requestMap.get("issotrx") != null && (transactionTabId.equals(tabId) || tabId == null)) {
      issotrx = requestMap.get("issotrx");
      if ("true".equals(issotrx)) {
        return "true";
      } else {
        return "";
      }
    } else if (requestMap.get("IsSOTrx") != null && transactionTabId.equals(tabId)) {
      issotrx = requestMap.get("IsSOTrx");
      if ("Y".equals(issotrx)) {
        return "true";
      } else {
        return "";
      }
    } else {
      try {
        result = (String) ParameterUtils.getJSExpressionResult(_requestMap, RequestContext.get()
            .getSession(), "if (OB.isSalesTransaction() == true) { 'true' } else {''}");
        return result;
      } catch (Exception e) {
        log.error(
            "Error evaluating filter expression: if (OB.isSalesTransaction() == false) { 'true' } else {''}",
            e);
      }
      return "";
    }
  }

}
