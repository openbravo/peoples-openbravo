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
 * All portions are Copyright (C) 2001-2006 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
package org.openbravo.erpCommon.ad_actionButton;

import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import java.sql.Connection;

public class ImportProduct extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strProcessId = vars.getStringParameter("inpProcessId");
      String strWindow = vars.getStringParameter("inpwindowId");
      String strTab = vars.getStringParameter("inpTabId");
      String strKey = vars.getRequiredGlobalVariable("inpadClientId", strWindow + "|AD_Client_ID");
      printPage(response, vars, strKey, strWindow, strTab, strProcessId);
    } else if (vars.commandIn("SAVE")) {
      String strWindow = vars.getStringParameter("inpwindowId");
      String strDeleteOldImported = vars.getStringParameter("inpDeleteOldImported", "");
      String strKey = vars.getRequiredGlobalVariable("inpadClientId", strWindow + "|AD_Client_ID");
      String strTab = vars.getStringParameter("inpTabId");
      ActionButtonDefaultData[] tab = ActionButtonDefaultData.windowName(this, strTab);
      String strWindowPath="", strTabName="";
      if (tab!=null && tab.length!=0) {
        strTabName = FormatUtilities.replace(tab[0].name);
        strWindowPath = "../" + FormatUtilities.replace(tab[0].description) + "/" + strTabName + "_Relation.html";
      } else strWindowPath = strDefaultServlet;
      String messageResult = processButton(vars, strKey, strDeleteOldImported);
      vars.setSessionValue(strWindow + "|" + strTabName + ".message", messageResult);
      printPageClosePopUp(response, vars, strWindowPath);
    } else pageErrorPopUp(response);
  }

  String processButton(VariablesSecureApp vars, String strKey, String strDeleteOldImported) {
      Connection conn = null;
    try {
      conn = this.getTransactionConnection();
    //  Delete Old Imported
      int no=0;
      if (strDeleteOldImported.equals("Y")){
        no = ImportProductData.deleteOld(conn,this, strKey);
        if (log4j.isDebugEnabled()) log4j.debug("ImportProduct Delete ld Imported = " + no);
      }

      //  Set Client, Org, IaActive, Created/Updated,   ProductType
      no = ImportProductData.updateRecords(conn,this, strKey);
      if (log4j.isDebugEnabled()) log4j.debug("ImportProduct Reset = " + no);

      //  Set Optional BPartner
      no = ImportProductData.updateBPartner(conn,this, strKey);
      if (log4j.isDebugEnabled()) log4j.debug("ImportProduct BPartner = " + no);

      //
      no = ImportProductData.updateIsImported(conn,this, strKey);
      if (log4j.isDebugEnabled()) log4j.debug("ImportProduct Invalid BPartner = " + no);

      //  ****  Find Product
      //  EAN/UPC
      no = ImportProductData.updateProductExistingUPC(conn,this, strKey);
      if (log4j.isDebugEnabled()) log4j.debug("ImportProduct Product Existing UPC = " + no);

      //  Value

      no = ImportProductData.updateProductExistingValue(conn,this, strKey);
      if (log4j.isDebugEnabled()) log4j.debug("ImportProduct Product Existing Value = " + no);

      //  BP ProdNo
      no = ImportProductData.updateProductExistingVendor(conn,this, strKey);
      if (log4j.isDebugEnabled()) log4j.debug("ImportProduct Product Existing Vendor ProductNo = " + no);

      //  Copy From Product if Import does not have value
      String[] strFields = new String[] {"Value","Name","Description","DocumentNote","Help",
        "UPC","SKU","Classification","ProductType",
        "Discontinued","DiscontinuedBy","ImageURL","DescriptionURL"};
      for (int i = 0; i < strFields.length; i++){
        no = ImportProductData.updateProductField(conn,this, strFields[i], strKey);
        if (log4j.isDebugEnabled()) log4j.debug("ImportProduct" + strFields[i] + " Default from existing Product=" + no);
      }
      String[] numFields = new String[] {"C_UOM_ID","M_Product_Category_ID",
        "Volume","Weight","ShelfWidth","ShelfHeight","ShelfDepth","UnitsPerPallet"};
      for (int i = 0; i < numFields.length; i++){
        no = ImportProductData.updateProductNumField(conn,this, numFields[i], strKey);
        if (log4j.isDebugEnabled()) log4j.debug("ImportProduct" + numFields[i] + " Default from existing Product=" + no);
      }

      //  Copy From Product_PO if Import does not have value
      String[] strFieldsPO = new String[] {"UPC",
        "PriceEffective","VendorProductNo","VendorCategory","Manufacturer",
        "Discontinued","DiscontinuedBy"};
      for (int i = 0; i < strFieldsPO.length; i++){
        no = ImportProductData.updateProductFieldPO(conn,this, strFieldsPO[i], strKey);
        if (no != 0)
          if (log4j.isDebugEnabled()) log4j.debug("ImportProduct" + strFieldsPO[i] + " Default from existing Product=" + no);
      }
      String[] numFieldsPO = new String[] {"C_UOM_ID","C_Currency_ID",
        "PriceList","PricePO","RoyaltyAmt",
        "Order_Min","Order_Pack","CostPerOrder","DeliveryTime_Promised"};
      for (int i = 0; i < numFieldsPO.length; i++){
        no = ImportProductData.updateProductNumFieldPO(conn,this, numFieldsPO[i], strKey);
        if (no != 0)
          if (log4j.isDebugEnabled()) log4j.debug("ImportProduct" + numFieldsPO[i] + " Default from existing Product=" + no);
      }


      //  Set UOM (System/own)
      no = ImportProductData.updateX12DE355(conn,this, strKey);
      if (log4j.isDebugEnabled()) log4j.debug("ImportProduct Set UOM Default=" + no);
      //
      no = ImportProductData.updateProductUOM(conn,this, strKey);
      if (log4j.isDebugEnabled()) log4j.debug("ImportProduct Set UOM =" + no);
      //
      no = ImportProductData.updateProductInvalidUOM(conn,this, strKey);
      if (log4j.isDebugEnabled()) log4j.debug("ImportProduct Invalid UOM =" + no);

      //  Set Product Logger (own)
      no = ImportProductData.updateProductCategoryDefault(conn,this, strKey);
      if (log4j.isDebugEnabled()) log4j.debug("ImportProduct Set Logger Default =" + no);
      //
      no = ImportProductData.updateProductCategory(conn,this, strKey);
      if (log4j.isDebugEnabled()) log4j.debug("ImportProduct Set Logger =" + no);
      //
      no = ImportProductData.updateInvalidCategory(conn,this, strKey);
      if (log4j.isDebugEnabled()) log4j.debug("ImportProduct Invalid Logger =" + no);

      //  Set Currency
      no = ImportProductData.updateCurrencyDefault(conn,this, strKey);
      if (log4j.isDebugEnabled()) log4j.debug("ImportProduct Set Currency Default =" + no);
      //
      no = ImportProductData.updateCurrency(conn,this, strKey);
      if (log4j.isDebugEnabled()) log4j.debug("ImportProduct Set Currency =" + no);
      //
      no = ImportProductData.updateInvalidCurrency(conn,this, strKey);
      if (log4j.isDebugEnabled()) log4j.debug("ImportProduct Invalid Currency =" + no);

      //  Verify ProductType
      no = ImportProductData.updateInvalidProductType(conn,this, strKey);
      if (log4j.isDebugEnabled()) log4j.debug("ImportProduct Invalid ProductType =" + no);

      //  Unique UPC/Value
      no = ImportProductData.updateNotUniqueValue(conn,this, strKey);
      if (log4j.isDebugEnabled()) log4j.debug("ImportProduct Not Unique Value =" + no);
      //
      no = ImportProductData.updateNotUniqueUPC(conn,this, strKey);
      if (log4j.isDebugEnabled()) log4j.debug("ImportProduct Not Unique UPC =" + no);

      //  Mandatory Value
      no = ImportProductData.updateNoMandatoryValue(conn,this, strKey);
      if (log4j.isDebugEnabled()) log4j.debug("ImportProduct No Mandatory Value =" + no);

      //  Vendor Product No
      no = ImportProductData.updateVendorProductNoSetToValue(conn,this, strKey);
      if (log4j.isDebugEnabled()) log4j.debug("ImportProduct VendorProductNo Set to Value =" + no);
      //
      no = ImportProductData.updateNotUniqueVendorProductNo(conn,this, strKey);
      if (log4j.isDebugEnabled()) log4j.debug("ImportProduct Not Unique VendorProductNo =" + no);

      //  Get Default Tax Category
      String strcTaxcategoryId = ImportProductData.selectTaxCategory(this, strKey);
      if (log4j.isDebugEnabled()) log4j.debug("ImportProduct C_TaxCategory_ID =" + strcTaxcategoryId);

      releaseCommitConnection(conn);


      //  -------------------------------------------------------------------
      int noInsert = 0;
      int noUpdate = 0;
      int noInsertPO = 0;
      int noUpdatePO = 0;

      //  Go through Records
      ImportProductData [] data = ImportProductData.selectRecords(this, strKey);
      for(int i =0;i<data.length;i++){
        String I_Product_ID = data[i].iProductId;
        String M_Product_ID = data[i].mProductId;
        String C_BPartner_ID = data[i].cBpartnerId;
        conn = this.getTransactionConnection();
        boolean newProduct = M_Product_ID.equals("");
        //  Product
        if (newProduct){     //  Insert new Product
          M_Product_ID = SequenceIdData.getSequence(this, "M_Product", vars.getClient());
          try {
            no = ImportProductData.insertProductImport(conn,this, M_Product_ID, strcTaxcategoryId, I_Product_ID);
            if (log4j.isDebugEnabled()) log4j.debug("Insert Product = " + no);
            noInsert++;
          }
          catch (ServletException ex){
            if (log4j.isDebugEnabled()) log4j.debug("Insert Product - " + ex.toString());
            releaseRollbackConnection(conn);
            conn = this.getTransactionConnection();
            ImportProductData.insertProductError(this, ex.toString(), I_Product_ID);
            continue;
          }
        }else {          //  Update Product
          try {
            no = ImportProductData.updateProductImport(conn,this, I_Product_ID, M_Product_ID);
            if (log4j.isDebugEnabled()) log4j.debug("Update Product = " + no);
            noUpdate++;
          }
          catch (ServletException ex){
            if (log4j.isDebugEnabled()) log4j.debug("Update Product - " + ex.toString());
            releaseRollbackConnection(conn);
            ImportProductData.updateProductError(this, ex.toString(), I_Product_ID);
            continue;
          }
        }
        //  Do we have PO Info
        if (C_BPartner_ID.equals("")){
          no = 0;
          //  If Product existed, Try to Update first
          if (!newProduct){
            try{
              no = ImportProductData.updateProductPOImport(conn,this, I_Product_ID, M_Product_ID, C_BPartner_ID);
              if (log4j.isDebugEnabled()) log4j.debug("Update Product_PO = " + no);
              noUpdatePO++;
            }
            catch (ServletException ex){
              if (log4j.isDebugEnabled()) log4j.debug("Update Product_PO - " + ex.toString());
              noUpdate--;
              releaseRollbackConnection(conn);
              ImportProductData.updateProductPOError(this, ex.toString(), I_Product_ID);
              continue;
            }
          }
          if (no == 0){    //  Insert PO
            try{
              no = ImportProductData.insertProductPOImport(conn,this, M_Product_ID, C_BPartner_ID, I_Product_ID);
              if (log4j.isDebugEnabled()) log4j.debug("Insert Product_PO = " + no);
              noInsertPO++;
            }
            catch (ServletException ex){
              if (log4j.isDebugEnabled()) log4j.debug("Insert Product_PO - " + ex.toString());
              noInsert--;     //  assume that product also did not exist
              releaseRollbackConnection(conn);
              ImportProductData.insertProductPOError(this, ex.toString(), I_Product_ID);
              continue;
            }
          }
        } //  C_BPartner_ID != 0
        //  Update I_Product
        no = ImportProductData.updateProductSetImportY(conn,this, M_Product_ID, I_Product_ID);
        releaseCommitConnection(conn);//
      } //  for all I_Product

      //  Set Error to indicator to not imported
      ImportProductData.updateNotImported(this,strKey);
      if (log4j.isDebugEnabled()) log4j.debug("@Errors@ = " + no);
      if (log4j.isDebugEnabled()) log4j.debug("@M_Product_ID@: @Inserted@ = " + noInsert);
      if (log4j.isDebugEnabled()) log4j.debug("@M_Product_ID@: @Updated@ = " + noUpdate);
      if (log4j.isDebugEnabled()) log4j.debug("@M_Product_ID@ @Purchase@: @Inserted@ = " + noInsertPO);
      if (log4j.isDebugEnabled()) log4j.debug("@M_Product_ID@ @Purchase@: @Updated@ = " + noUpdatePO);
      return "";
    } catch (Exception e) {
      log4j.warn(e);
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {}
      return Utility.messageBD(this, "ProcessRunError", vars.getLanguage());
    }
  }



  void printPage(HttpServletResponse response, VariablesSecureApp vars, String strKey, String windowId, String strTab, String strProcessId) throws IOException, ServletException {
      if (log4j.isDebugEnabled()) log4j.debug("Output: Button import product");

      ActionButtonDefaultData[] data = null;
      String strHelp="", strDescription="";
      if (vars.getLanguage().equals("en_US")) data = ActionButtonDefaultData.select(this, strProcessId);
      else data = ActionButtonDefaultData.selectLanguage(this, vars.getLanguage(), strProcessId);

      if (data!=null && data.length!=0) {
        strDescription = data[0].description;
        strHelp = data[0].help;
      }
      String[] discard = {""};
      if (strHelp.equals("")) discard[0] = new String("helpDiscard");
      XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_actionButton/ImportProduct", discard).createXmlDocument();
      xmlDocument.setParameter("key", strKey);
      xmlDocument.setParameter("window", windowId);
      xmlDocument.setParameter("tab", strTab);
      xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("question", Utility.messageBD(this, "StartProcess?", vars.getLanguage()));
      xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("description", strDescription);
      xmlDocument.setParameter("help", strHelp);

      xmlDocument.setData("reportadClientId", "liststructure", ImportProductData.selectClient(this));

      response.setContentType("text/html; charset=UTF-8");
      PrintWriter out = response.getWriter();
      out.println(xmlDocument.print());
      out.close();
    }

  public String getServletInfo() {
    return "Servlet Import Product";
  } // end of getServletInfo() method
}

