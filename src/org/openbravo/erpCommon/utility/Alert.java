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
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2008 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
package org.openbravo.erpCommon.utility;

import org.openbravo.database.ConnectionProvider;
import org.apache.log4j.Logger;

public class Alert {
		
	private int alertRuleId;
	private String description;
	private String note;
	
	static Logger log4j = Logger.getLogger(Alert.class);
	public static final char DATA_DRIVEN = 'D';
	public static final char EXTERNAL = 'E';
	
	public Alert() {
		this(0);
	}
	
	public Alert(int ruleId) {
		this.alertRuleId = ruleId;
	}
	
	public int getAlertRuleId() {
		return alertRuleId;
	}
	
	public void setAlertRuleId(int value) {
		alertRuleId = value;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String value) {
		this.description = value;
	}
	
	public String getNote() {
		return note;
	}
	
	public void setNote(String value) {
		this.note = value;
	}
	
	public boolean save(ConnectionProvider conn) {
		if(alertRuleId == 0 || description.equals(""))
			return false;
		
		try {
			AlertData.insert(conn, description, String.valueOf(alertRuleId), note);
		}
		catch(Exception e) {
			log4j.error("Error saving an alert instance: " + e.getMessage());
			return false;
		}
		return true;
	}
}