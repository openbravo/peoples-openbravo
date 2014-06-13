/* 
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.advpaymentmngt.modulescript;

import javax.servlet.ServletException;

import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;

public class AlertsForWrongOrders extends ModuleScript {
  
  final static private String ALERT_RULE_SQL = "select distinct ad_column_identifier('C_Order', fin_payment_schedule.c_order_id, 'en_US') as record_id, fin_payment_schedule.c_order_id  as referencekey_id, 0 as ad_role_id, null as ad_user_id, 'This order needs to be checked due to wrong payment info.' as description, 'Y' as isActive, fin_payment_schedule.ad_org_id, fin_payment_schedule.ad_client_id, now() as created, 0 as createdBy, now() as updated, 0 as updatedBy from fin_payment_schedule, fin_payment_scheduledetail where fin_payment_schedule.fin_payment_schedule_id = fin_payment_scheduledetail.fin_payment_schedule_order and fin_payment_scheduledetail.iscanceled = 'N' and fin_payment_scheduledetail.fin_payment_detail_id IS NOT NULL group by fin_payment_schedule.ad_org_id, fin_payment_schedule.ad_client_id, fin_payment_schedule.fin_payment_schedule_id, fin_payment_schedule.c_order_id, fin_payment_schedule.paidamt having fin_payment_schedule.paidamt <> sum(fin_payment_scheduledetail.amount + coalesce(fin_payment_scheduledetail.writeoffamt,0)) order by 1";
  
  @Override
  // Inserting Alerts for orders which needs to be recalculated/processed
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();
      AlertsForWrongOrdersData[] data = AlertsForWrongOrdersData.select(cp);
      for (AlertsForWrongOrdersData wrongOrder : data) {
        createAlert(cp, wrongOrder);
      }
     } catch (Exception e) {
      handleError(e);
    }
  }

  private void createAlert(ConnectionProvider cp, AlertsForWrongOrdersData wrongOrder)
	      throws ServletException {
	  	final String SALES_ORDER_WINDOW = "143";
	    final String PURCHASE_ORDER_WINDOW = "181";
	    final String SALES_ORDER_TAB = "186";
	    final String PURCHASE_ORDER_TAB = "294";
	    String WindowOrderId = PURCHASE_ORDER_WINDOW;
	    String strTabId = PURCHASE_ORDER_TAB;
	    String ALERT_RULE = "Wrong Purchase Order Payment Plan";
	    if ("Y".equals(wrongOrder.issotrx)) {
	      strTabId = SALES_ORDER_TAB;
	      WindowOrderId = SALES_ORDER_WINDOW;	    
	      ALERT_RULE = "Wrong Sales Order Payment Plan";
	    }
	    String strName = "Order: '" + wrongOrder.orderinfo
	            + "' needs to be checked due to wrong payment info.";
	    String oldAlertRuleId = AlertsForWrongOrdersData.getAlertRuleId(cp, ALERT_RULE,
	            wrongOrder.adClientId);
	    if (!AlertsForWrongOrdersData.existsAlert(cp, oldAlertRuleId, wrongOrder.cOrderId)) {
	    if (!AlertsForWrongOrdersData.existsAlertRule(cp, ALERT_RULE, wrongOrder.adClientId)) {
	    	AlertsForWrongOrdersData.insertAlertRule(cp, wrongOrder.adClientId, ALERT_RULE, strTabId, "");
	        AlertsForWrongOrdersData[] roles = AlertsForWrongOrdersData.getRoleId(cp,
	            WindowOrderId, wrongOrder.adClientId);
	        for (AlertsForWrongOrdersData role : roles) {
	          AlertsForWrongOrdersData.insertAlertRecipient(cp, wrongOrder.adClientId,
	              wrongOrder.adOrgId,
	              AlertsForWrongOrdersData.getAlertRuleId(cp, ALERT_RULE, wrongOrder.adClientId),
	              role.adRoleId);
	        }
	      } else {
	        AlertsForWrongOrdersData.updateAlertRule(cp, ALERT_RULE, wrongOrder.adClientId);
	      }
	    	String alertRuleId = AlertsForWrongOrdersData.getAlertRuleId(cp, ALERT_RULE,
	            wrongOrder.adClientId);
	      AlertsForWrongOrdersData.insertAlert(cp, wrongOrder.adClientId, wrongOrder.adOrgId,
	          strName, alertRuleId, wrongOrder.orderinfo, wrongOrder.cOrderId);
	    } else {
              AlertsForWrongOrdersData.updateAlertRule(cp, ALERT_RULE, wrongOrder.adClientId);
            }
  	}
}
