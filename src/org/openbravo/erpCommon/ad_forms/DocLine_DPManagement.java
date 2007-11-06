/*
 ******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): Openbravo SL
 * Contributions are Copyright (C) 2001-2006 Openbravo S.L.
 ******************************************************************************
*/
package org.openbravo.erpCommon.ad_forms;

import org.apache.log4j.Logger;



public class DocLine_DPManagement extends DocLine {
  static Logger log4jDocLine_Amortization = Logger.getLogger(DocLine_Amortization.class);

  public String Amount;
  public String Isreceipt;
  public String StatusTo;
  public String StatusFrom;

	public DocLine_DPManagement (String DocumentType, String TrxHeader_ID, String TrxLine_ID){
		super(DocumentType, TrxHeader_ID, TrxLine_ID);
	}

	public String getServletInfo() {
    return "Servlet for the accounting";
  } // end of getServletInfo() method
}
