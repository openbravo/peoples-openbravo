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
 * All portions are Copyright (C) 2012 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.utility;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.ADClientAcctDimension;
import org.openbravo.model.ad.domain.Reference;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.ui.Field;
import org.openbravo.model.ad.ui.Tab;

public class DimensionDisplayUtility {

  public static Logger log4j = Logger.getLogger(DimensionDisplayUtility.class);

  /** Accounting Dimensions **/
  public static final String DIM_Header = "H";
  public static final String DIM_Lines = "L";
  public static final String DIM_BreakDown = "BD";
  public static final String DIM_Project = "PJ";
  public static final String DIM_BPartner = "BP";
  public static final String DIM_Product = "PR";
  public static final String DIM_CostCenter = "CC";
  public static final String DIM_User1 = "U1";
  public static final String DIM_User2 = "U2";

  /** Document Base Types with accounting dimensions **/
  public static final String ARProFormaInvoice = "ARF";
  public static final String ARReturnMaterialInvoice = "ARI_RM";
  public static final String APPayment = "APP";
  public static final String ARInvoice = "ARI";
  public static final String MaterialDelivery = "MMS";
  public static final String APCreditMemo = "APC";
  public static final String FinancialAccountTransaction = "FAT";
  public static final String MaterialMovement = "MMM";
  public static final String Amortization = "AMZ";
  public static final String SalesOrder = "SOO";
  public static final String APInvoice = "API";
  public static final String GLJournal = "GLJ";
  public static final String MaterialPhysicalInventory = "MMI";
  public static final String MaterialReceipt = "MMR";
  public static final String PurchaseOrder = "POO";
  public static final String ARCreditMemo = "ARC";
  public static final String Reconciliation = "REC";
  public static final String ARReceipt = "ARR";

  /** Session variable **/
  public static final String IsAcctDimCentrally = "$IsAcctDimCentrally";
  /** Display logic for accounting dimensions **/
  public static final String DIM_DISPLAYLOGIC = "@ACCT_DIMENSION_DISPLAY@";
  /** Document Base Type auxiliary input **/
  public static final String DIM_AUXILIAR_INPUT = "DOCBASETYPE";

  private static Map<String, String> columnDimensionMap = null;

  private static void initialize() {
    columnDimensionMap.put("C_PROJECT_ID", DIM_Project);
    columnDimensionMap.put("C_BPARTNER_ID", DIM_BPartner);
    columnDimensionMap.put("M_PRODUCT_ID", DIM_Product);
    columnDimensionMap.put("C_COSTCENTER_ID", DIM_CostCenter);
    columnDimensionMap.put("USER1_ID", DIM_User1);
    columnDimensionMap.put("USER2_ID", DIM_User2);
  }

  public static String displayAcctDimensions(String centrally, String dimemsion,
      String docBaseType, String level) {
    String var = "";
    if (centrally.equals("N")) {
      var = "$Element_" + dimemsion;
    } else {
      var = "$Element_" + dimemsion + "_" + docBaseType + "_" + level;
    }
    return var;
  }

  public static String computeAccountingDimensionDisplayLogic(Field field, Tab tab) {
    // Example
    // (context.$IsAcctDimCentrally === 'N' && context.$Element_U2 === 'Y') ||
    // (context.$IsAcctDimCentrally === 'Y' && context['$Element_U2_ +
    // OB.Utilities.getValue(currentValues, 'DOCBASETYPE') + _H'] === 'Y')
    String displayLogic = "(context."
        + IsAcctDimCentrally
        + " === 'N' && context.$Element_%s === 'Y') || (context."
        + IsAcctDimCentrally
        + " === 'Y' && context['$Element_%s_ + OB.Utilities.getValue(currentValues, \"%s\") + _%s'] === 'Y')";

    try {
      OBContext.setAdminMode(true);
      if (columnDimensionMap == null) {
        initialize();
      }
      String columnName = "";
      if (field.getColumn() != null) {
        columnName = field.getColumn().getDBColumnName();
      } else {
        return "";
      }
      String dimension = columnDimensionMap.get(columnName.toUpperCase());
      if (dimension == null) {
        return "";
      }
      // TODO Get level from new AD_DIMENSION table
      int tabLevel = tab.getTabLevel().intValue();
      String level = tabLevel == 0 ? DIM_Header : (tabLevel == 1 ? DIM_Lines
          : (tabLevel == 2 ? DIM_BreakDown : ""));
      displayLogic = String.format(displayLogic, dimension, dimension, DIM_AUXILIAR_INPUT, level);
    } catch (Exception e) {
      log4j.error("Not possible to compute display logic.", e);
      return "";
    } finally {
      OBContext.restorePreviousMode();
    }
    return displayLogic;
  }

  /**
   * Compute the accounting dimensions visibility session variables.
   * 
   * @param client
   *          Client.
   * @return Map containing all the accounting dimension visilibity session variables and the
   *         corresponding value ('Y', 'N')
   */
  public static Map<String, String> getAccountingDimensionConfiguration(Client client) {
    final String DIMENSIONS_REF = "181";
    final String DOCBASETYPES_REF = "FBC599C796664DD49AD002C61DAFF813";
    final String ELEMENT = "$Element";
    final String[] LEVELS = new String[] { DIM_Header, DIM_Lines, DIM_BreakDown };
    Map<String, String> sessionMap = new HashMap<String, String>();
    String aux = "";

    try {
      OBContext.setAdminMode(true);
      Reference dimRef = OBDal.getInstance().get(Reference.class, DIMENSIONS_REF);
      Reference docBaseTypeRef = OBDal.getInstance().get(Reference.class, DOCBASETYPES_REF);

      String isDisplayed = null;
      Map<String, String> clientAcctDimensionCache = new HashMap<String, String>();
      for (ADClientAcctDimension cad : client.getADClientAcctDimensionList()) {
        clientAcctDimensionCache.put(cad.getDimension() + "_" + cad.getDocBaseType() + "_"
            + DIM_Header, cad.isShowInHeader() ? "Y" : "N");
        clientAcctDimensionCache.put(cad.getDimension() + "_" + cad.getDocBaseType() + "_"
            + DIM_Lines, cad.isShowInLines() ? "Y" : "N");
        clientAcctDimensionCache.put(cad.getDimension() + "_" + cad.getDocBaseType() + "_"
            + DIM_BreakDown, cad.isShowInBreakdown() ? "Y" : "N");
      }

      for (org.openbravo.model.ad.domain.List dim : dimRef.getADListList()) {
        for (org.openbravo.model.ad.domain.List doc : docBaseTypeRef.getADListList()) {
          for (String level : LEVELS) {
            String docValue = doc.getSearchKey();
            String dimValue = dim.getSearchKey();
            aux = ELEMENT + "_" + dimValue + "_" + docValue + "_" + level;

            if (DIM_Project.equals(dimValue)) {
              if (client.isProjectAcctdimIsenable()) {
                isDisplayed = clientAcctDimensionCache.get(dimValue + "_" + docValue + "_" + level);
                if (isDisplayed == null) {
                  if (DIM_Header.equals(level)) {
                    isDisplayed = client.isProjectAcctdimHeader() ? "Y" : "N";
                  } else if (DIM_Lines.equals(level)) {
                    isDisplayed = client.isProjectAcctdimLines() ? "Y" : "N";
                  } else if (DIM_BreakDown.equals(level)) {
                    isDisplayed = client.isProjectAcctdimBreakdown() ? "Y" : "N";
                  }
                }
              } else {
                isDisplayed = "N";
              }
            } else if (DIM_BPartner.equals(dimValue)) {
              if (client.isBpartnerAcctdimIsenable()) {
                isDisplayed = clientAcctDimensionCache.get(dimValue + "_" + docValue + "_" + level);
                if (isDisplayed == null) {
                  if (DIM_Header.equals(level)) {
                    isDisplayed = client.isBpartnerAcctdimHeader() ? "Y" : "N";
                  } else if (DIM_Lines.equals(level)) {
                    isDisplayed = client.isBpartnerAcctdimLines() ? "Y" : "N";
                  } else if (DIM_BreakDown.equals(level)) {
                    isDisplayed = client.isBpartnerAcctdimBreakdown() ? "Y" : "N";
                  }
                }
              } else {
                isDisplayed = "N";
              }
            } else if (DIM_Product.equals(dimValue)) {
              if (client.isProductAcctdimIsenable()) {
                isDisplayed = clientAcctDimensionCache.get(dimValue + "_" + docValue + "_" + level);
                if (isDisplayed == null) {
                  if (DIM_Header.equals(level)) {
                    isDisplayed = client.isProductAcctdimHeader() ? "Y" : "N";
                  } else if (DIM_Lines.equals(level)) {
                    isDisplayed = client.isProductAcctdimLines() ? "Y" : "N";
                  } else if (DIM_BreakDown.equals(level)) {
                    isDisplayed = client.isProductAcctdimBreakdown() ? "Y" : "N";
                  }
                }
              } else {
                isDisplayed = "N";
              }
            } else if (DIM_CostCenter.equals(dimValue)) {
              if (client.isCostcenterAcctdimIsenable()) {
                isDisplayed = clientAcctDimensionCache.get(dimValue + "_" + docValue + "_" + level);
                if (isDisplayed == null) {
                  if (DIM_Header.equals(level)) {
                    isDisplayed = client.isCostcenterAcctdimHeader() ? "Y" : "N";
                  } else if (DIM_Lines.equals(level)) {
                    isDisplayed = client.isCostcenterAcctdimLines() ? "Y" : "N";
                  } else if (DIM_BreakDown.equals(level)) {
                    isDisplayed = client.isCostcenterAcctdimBreakdown() ? "Y" : "N";
                  }
                }
              } else {

              }
            } else if (DIM_User1.equals(dimValue)) {
              if (client.isUser1AcctdimIsenable()) {
                isDisplayed = clientAcctDimensionCache.get(dimValue + "_" + docValue + "" + level);
                if (isDisplayed == null) {
                  if (DIM_Header.equals(level)) {
                    isDisplayed = client.isUser1AcctdimHeader() ? "Y" : "N";
                  } else if (DIM_Lines.equals(level)) {
                    isDisplayed = client.isUser1AcctdimHeader() ? "Y" : "N";
                  } else if (DIM_BreakDown.equals(level)) {
                    isDisplayed = client.isUser1AcctdimHeader() ? "Y" : "N";
                  }
                }
              } else {
                isDisplayed = "N";
              }
            } else if (DIM_User2.equals(dimValue)) {
              if (client.isUser2AcctdimIsenable()) {
                isDisplayed = clientAcctDimensionCache.get(dimValue + "_" + docValue + "" + level);
                if (isDisplayed == null) {
                  if (DIM_Header.equals(level)) {
                    isDisplayed = client.isUser2AcctdimHeader() ? "Y" : "N";
                  } else if (DIM_Lines.equals(level)) {
                    isDisplayed = client.isUser2AcctdimLines() ? "Y" : "N";
                  } else if (DIM_BreakDown.equals(level)) {
                    isDisplayed = client.isUser2AcctdimBreakdown() ? "Y" : "N";
                  }
                }
              } else {
                isDisplayed = "N";
              }
            }

            if (isDisplayed != null) {
              sessionMap.put(aux, isDisplayed);
              isDisplayed = null;
              aux = "";
            }
          }
        }
      }
    } catch (Exception e) {
      log4j.error("Not possible to load accounting dimensions visibility session variables", e);
      return new HashMap<String, String>();
    } finally {
      OBContext.restorePreviousMode();
    }
    return sessionMap;
  }
}
