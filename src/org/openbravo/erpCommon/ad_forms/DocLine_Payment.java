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
* All portions are Copyright (C) 2001-2008 Openbravo SL
* All Rights Reserved.
* Contributor(s):  ______________________________________.
************************************************************************
*/
package org.openbravo.erpCommon.ad_forms;

import org.apache.log4j.Logger ;



public class DocLine_Payment extends DocLine {
    static Logger log4jDocLine_Payment = Logger.getLogger(DocLine_Payment.class);

    String Line_ID = "";
    String Amount = "";
    String WriteOffAmt = "";
    String isManual = "";
    String isReceipt = "";
    String isPaid = "";
    String C_Settlement_Cancel_ID = "";
    String C_Settlement_Generate_ID = "";
    String C_GLItem_ID = "";
    String IsDirectPosting = "";
    String dpStatus = "";
    String C_Currency_ID_From; 
    String conversionDate; 
    String C_INVOICE_ID="";
    String C_BPARTNER_ID="";
    String C_WITHHOLDING_ID="";
    String WithHoldAmt="";
    String C_BANKACCOUNT_ID="";
    String C_BANKSTATEMENTLINE_ID="";
    String C_CASHBOOK_ID="";
    String C_CASHLINE_ID="";

    public DocLine_Payment (String DocumentType, String TrxHeader_ID, String TrxLine_ID){
        super(DocumentType, TrxHeader_ID, TrxLine_ID);
        Line_ID = TrxLine_ID;
        m_Record_Id2 = Line_ID;
    }


    public String getServletInfo() {
    return "Servlet for accounting";
  } // end of getServletInfo() method
}
