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
 * Contributor(s): Openbravo SLU
 * Contributions are Copyright (C) 2001-2006 Openbravo S.L.U.
 ******************************************************************************
 */
package org.openbravo.erpCommon.ad_process;

import java.sql.Connection;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;

public class ImportProduct extends ImportProcess {
  static Logger log4j = Logger.getLogger(ImportProduct.class);

  private String m_AD_Process_ID = "";
  private boolean m_deleteOldImported;

  public ImportProduct(ConnectionProvider conn, String AD_Process_ID, boolean deleteOld) {
    super(conn);
    m_AD_Process_ID = AD_Process_ID;
    m_deleteOldImported = deleteOld;
  }

  protected String getAD_Process_ID() {
    return m_AD_Process_ID;
  }

  protected String getRecord_ID() {
    return "0";
  }

  protected void createInstanceParams(VariablesSecureApp vars) throws ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Creating parameters");
  }

  protected OBError doIt(VariablesSecureApp vars) throws ServletException {
    int no = 0;
    OBError myError = new OBError();

    ConnectionProvider conn = null;
    Connection con = null;
    String strcTaxcategoryId = null;
    try {
      conn = getConnection();
      con = conn.getTransactionConnection();
      if (m_deleteOldImported) {
        no = ImportProductData.deleteOld(con, conn, getAD_Client_ID());
        if (log4j.isDebugEnabled())
          log4j.debug("Delete Old Imported = " + no);
      }
      // Set Client, Org, IaActive, Created/Updated, ProductType
      no = ImportProductData.updateRecords(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportProduct Reset = " + no);

      // Set Optional BPartner
      no = ImportProductData.updateBPartner(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportProduct BPartner = " + no);

      //
      no = ImportProductData.updateBPartnerError(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportProduct Invalid BPartner = " + no);

      // EAN/UPC
      no = ImportProductData.updateProductByUPC(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportProduct Product Existing UPC = " + no);

      // Value
      no = ImportProductData.updateProductByValue(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportProduct Product Existing Value = " + no);

      // BP ProdNo
      no = ImportProductData.updateProductByVendor(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportProduct Product Existing Vendor ProductNo = " + no);

      // Copy From Product if Import does not have value
      String[] strFields = new String[] { "Value", "Name", "Description", "DocumentNote", "Help",
          "UPC", "SKU", "Classification", "ProductType", "Discontinued", "DiscontinuedBy",
          "ImageURL", "DescriptionURL" };
      for (int i = 0; i < strFields.length; i++) {
        no = ImportProductData.updateProductField(con, conn, strFields[i], getAD_Client_ID());
        if (log4j.isDebugEnabled())
          log4j.debug("ImportProduct" + strFields[i] + " Default from existing Product=" + no);
      }
      String[] numFields = new String[] { "C_UOM_ID", "M_Product_Category_ID", "Volume", "Weight",
          "ShelfWidth", "ShelfHeight", "ShelfDepth", "UnitsPerPallet" };
      for (int i = 0; i < numFields.length; i++) {
        no = ImportProductData.updateProductNumField(con, conn, numFields[i], getAD_Client_ID());
        if (log4j.isDebugEnabled())
          log4j.debug("ImportProduct" + numFields[i] + " Default from existing Product=" + no);
      }

      // Copy From Product_PO if Import does not have value
      String[] strFieldsPO = new String[] { "UPC", "PriceEffective", "VendorProductNo",
          "VendorCategory", "Manufacturer", "Discontinued", "DiscontinuedBy" };
      for (int i = 0; i < strFieldsPO.length; i++) {
        no = ImportProductData.updateProductFieldPO(con, conn, strFieldsPO[i], getAD_Client_ID());
        if (no != 0)
          if (log4j.isDebugEnabled())
            log4j.debug("ImportProduct" + strFieldsPO[i] + " Default from existing Product=" + no);
      }
      String[] numFieldsPO = new String[] { "C_UOM_ID", "C_Currency_ID", "PriceList", "PricePO",
          "RoyaltyAmt", "Order_Min", "Order_Pack", "CostPerOrder", "DeliveryTime_Promised" };
      for (int i = 0; i < numFieldsPO.length; i++) {
        no = ImportProductData
            .updateProductNumFieldPO(con, conn, numFieldsPO[i], getAD_Client_ID());
        if (no != 0)
          if (log4j.isDebugEnabled())
            log4j.debug("ImportProduct" + numFieldsPO[i] + " Default from existing Product=" + no);
      }

      // Set UOM (System/own)
      no = ImportProductData.updateX12DE355(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportProduct Set UOM Default=" + no);
      //
      no = ImportProductData.updateProductUOM(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportProduct Set UOM =" + no);
      //
      no = ImportProductData.updateProductInvalidUOM(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportProduct Invalid UOM =" + no);

      // Set Product Logger (own)
      no = ImportProductData.updateProductCategoryDefault(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportProduct Set Product Category Default =" + no);
      //
      no = ImportProductData.updateProductCategory(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportProduct Set Product Category =" + no);
      //
      no = ImportProductData.updateInvalidCategory(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportProduct Invalid Product Category =" + no);

      // Set Currency
      no = ImportProductData.updateCurrencyDefault(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportProduct Set Currency Default =" + no);
      //
      no = ImportProductData.updateCurrency(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportProduct Set Currency =" + no);
      //
      no = ImportProductData.updateInvalidCurrency(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportProduct Invalid Currency =" + no);

      // Verify ProductType
      no = ImportProductData.updateInvalidProductType(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportProduct Invalid ProductType =" + no);

      // Unique UPC/Value
      no = ImportProductData.updateNotUniqueValue(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportProduct Not Unique Value =" + no);
      //
      no = ImportProductData.updateNotUniqueUPC(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportProduct Not Unique UPC =" + no);

      // Mandatory Value
      no = ImportProductData.updateNoMandatoryValue(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportProduct No Mandatory Value =" + no);

      // Vendor Product No
      no = ImportProductData.updateVendorProductNoSetToValue(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportProduct VendorProductNo Set to Value =" + no);
      //
      no = ImportProductData.updateNotUniqueVendorProductNo(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportProduct Not Unique VendorProductNo =" + no);

      // Get Default Tax Category
      strcTaxcategoryId = ImportProductData.selectTaxCategory(conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportProduct C_TaxCategory_ID =" + strcTaxcategoryId);
      conn.releaseCommitConnection(con);
      if (strcTaxcategoryId == null || strcTaxcategoryId.equals("")) {
        // Default Tax category doesn't exist
        addLog(Utility.messageBD(conn, "MarkDefaultTaxCategory", vars.getLanguage()));
        myError.setType("Error");
        myError.setTitle(Utility.messageBD(conn, "Error", vars.getLanguage()));
        myError.setMessage(Utility.messageBD(conn, "MarkDefaultTaxCategory", vars.getLanguage()));
        return myError;
      }

    } catch (Exception se) {
      try {
        conn.releaseRollbackConnection(con);
      } catch (Exception ignored) {
      }
      se.printStackTrace();
      addLog(Utility.messageBD(conn, "ProcessRunError", vars.getLanguage()));
      myError.setType("Error");
      myError.setTitle(Utility.messageBD(conn, "Error", vars.getLanguage()));
      myError.setMessage(Utility.messageBD(conn, "ProcessRunError", vars.getLanguage()));
      return myError;
    }
    // till here, the edition of the I_ImportProduct table
    // now, the insertion from I_ImportProduct table in M_Product,
    // M_Product_PO...

    int noInsert = 0;
    int noUpdate = 0;
    int noInsertPO = 0;
    int noUpdatePO = 0;
    int noProductError = 0;

    try {
      // Go through Records
      ImportProductData[] data = ImportProductData.selectRecords(conn, getAD_Client_ID());
      for (int i = 0; i < data.length; i++) {
        String I_Product_ID = data[i].iProductId;
        String M_Product_ID = data[i].mProductId;
        String C_BPartner_ID = data[i].cBpartnerId;
        con = conn.getTransactionConnection();
        boolean newProduct = M_Product_ID.equals("");
        // Product
        if (newProduct) { // Insert new Product
          M_Product_ID = SequenceIdData.getUUID();
          try {
            if (log4j.isDebugEnabled())
              log4j.debug("before insert");
            no = ImportProductData.insertProductImport(con, conn, M_Product_ID, strcTaxcategoryId,
                I_Product_ID);
            if (log4j.isDebugEnabled())
              log4j.debug("Insert Product = " + no);
            noInsert++;
          } catch (ServletException ex) {
            if (log4j.isDebugEnabled())
              log4j.debug("Insert Product - " + ex.toString());
            conn.releaseRollbackConnection(con);
            ImportProductData.insertProductError(conn, ex.toString(), I_Product_ID);
            continue;
          }
        } else { // Update Product
          try {
            no = ImportProductData.updateProductImport(con, conn, I_Product_ID, M_Product_ID);
            if (log4j.isDebugEnabled())
              log4j.debug("Update Product = " + no);
            noUpdate++;
          } catch (ServletException ex) {
            if (log4j.isDebugEnabled())
              log4j.debug("Update Product - " + ex.toString());
            conn.releaseRollbackConnection(con);
            ImportProductData.updateProductError(conn, ex.toString(), I_Product_ID);
            continue;
          }
        }
        // Do we have PO Info
        if (!C_BPartner_ID.equals("")) {
          no = 0;
          // If Product existed, Try to Update first
          if (!newProduct) {
            try {
              no = ImportProductData.updateProductPOImport(con, conn, I_Product_ID, M_Product_ID,
                  C_BPartner_ID);
              if (log4j.isDebugEnabled())
                log4j.debug("Update Product_PO = " + no);
              noUpdatePO++;
            } catch (ServletException ex) {
              if (log4j.isDebugEnabled())
                log4j.debug("Update Product_PO - " + ex.toString());
              noUpdate--;
              conn.releaseRollbackConnection(con);
              ImportProductData.updateProductPOError(conn, ex.toString(), I_Product_ID);
              continue;
            }
          }
          try {
            if (no == 0) { // Insert PO
              no = ImportProductData.insertProductPOImport(con, conn, M_Product_ID, C_BPartner_ID,
                  I_Product_ID);
              if (log4j.isDebugEnabled())
                log4j.debug("Insert Product_PO = " + no);
              noInsertPO++;
            }
          } catch (ServletException ex) {
            if (log4j.isDebugEnabled())
              log4j.debug("Insert Product_PO - " + ex.toString());
            noInsert--; // assume that product also did not exist
            ImportProductData.insertProductPOError(conn, ex.toString(), I_Product_ID);
            conn.releaseRollbackConnection(con);
            continue;
          }
        } // C_BPartner_ID != 0
        conn.releaseCommitConnection(con);
        // Update I_Product
        if (log4j.isDebugEnabled())
          log4j.debug("before update: " + M_Product_ID + " -- " + I_Product_ID);
        no = ImportProductData.updateProductSetImportY(conn, M_Product_ID, I_Product_ID);
        if (log4j.isDebugEnabled())
          log4j.debug("after update: " + M_Product_ID + " -- " + I_Product_ID);
      } // for all I_Product
    } catch (Exception se) {
      se.printStackTrace();
      addLog(Utility.messageBD(conn, "ProcessRunError", vars.getLanguage()));
      myError.setType("Error");
      myError.setTitle(Utility.messageBD(conn, "Error", vars.getLanguage()));
      myError.setMessage(Utility.messageBD(conn, "ProcessRunError", vars.getLanguage()));
      return myError;
    }

    // Set Error to indicator to not imported
    noProductError = ImportProductData.updateNotImported(conn, getAD_Client_ID());
    if (log4j.isDebugEnabled())
      log4j.debug("Errors = " + noProductError);
    addLog(Utility.messageBD(conn, "Products not imported", vars.getLanguage()) + ": "
        + noProductError + "; ");
    addLog("Product inserted: " + noInsert + "; ");
    addLog("Product updated: " + noUpdate + "; ");
    addLog("ProductPO inserted: " + noInsertPO + "; ");
    addLog("ProductPO updated: " + noUpdatePO);

    if (noProductError == 0) {
      myError.setType("Success");
      myError.setTitle(Utility.messageBD(conn, "Success", vars.getLanguage()));
    } else if (noInsert > 0 || noUpdate > 0 || noInsertPO > 0 || noUpdatePO > 0) {
      myError.setType("Warning");
      myError.setTitle(Utility.messageBD(conn, "Some products could not be imported", vars
          .getLanguage()));
    } else {
      myError.setType("Error");
      myError.setTitle(Utility
          .messageBD(conn, " No products could be imported", vars.getLanguage()));
    }
    myError.setMessage(Utility.messageBD(conn, getLog(), vars.getLanguage()));
    return myError;
  }
}
