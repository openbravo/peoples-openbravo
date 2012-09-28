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
package org.openbravo.modulescript;

import org.openbravo.database.ConnectionProvider;
import javax.servlet.ServletException;

public class UpdateADClientInfo extends ModuleScript {

  //This module script has ben created due to issue 18407 and related to issue 19697
  @Override
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();
      UpdateADClientInfoData[] clientsID = UpdateADClientInfoData.selectClientsID(cp);
      for (UpdateADClientInfoData clientID : clientsID) {
	// MC tree
        UpdateADClientInfoData.update(cp,clientID.adClientId);
      }
      // Asset tree
      createTreeAndUpdateClientInfo(cp, "Asset", "AS", "AD_TREE_ASSET_ID");
      // Product Category tree
      updateClientInfo(cp, "AD_TREE_PRODUCT_CATEGORY_ID", "PC");
      // Cost Center Tree
      updateClientInfo(cp, "AD_TREE_COSTCENTER_ID", "CC");
      //User Defined Dimension 1 Tree
      updateClientInfo(cp, "AD_TREE_USER1_ID", "U1");
      //User Defined Dimension 2 Tree
      updateClientInfo(cp, "AD_TREE_USER2_ID", "U2");

      // Insert Missing Treenodes for Assets
      UpdateADClientInfoData.insertMissingTreeNodes(cp, "AS", "A_ASSET");
      // Insert Missing Treenodes for Product Categories
      UpdateADClientInfoData.insertMissingTreeNodes(cp, "PC", "M_PRODUCT_CATEGORY");
      //Insert Missing Treenodes for Cost Center
      UpdateADClientInfoData.insertMissingCostcenterNodes(cp, "CC", "C_COSTCENTER");
      //Insert Missing Treenodes for User Defined Dimension 1
      UpdateADClientInfoData.insertMissingUser1Nodes(cp, "U1", "C_USER1");
      //Insert Missing Treenodes for User Defined Dimension 1
      UpdateADClientInfoData.insertMissingUser2Nodes(cp, "U2", "C_USER2");
    } catch (Exception e) {
      handleError(e);
    }
  }

    private void createTreeAndUpdateClientInfo(final ConnectionProvider cp, final String treeTypeName, final String treeTypeValue, final String columnName)
	throws ServletException {
      UpdateADClientInfoData[] clientsID = UpdateADClientInfoData.selectClientsMissingTree(cp, columnName);
      for (UpdateADClientInfoData clientID: clientsID) {
	final String treeId = UpdateADClientInfoData.getUUID(cp);
	final String nameAndDesc = clientID.clientname + " " + treeTypeName;
        UpdateADClientInfoData.createTree(cp, treeId, clientID.adClientId, nameAndDesc, treeTypeValue);	
	UpdateADClientInfoData.updateClientTree(cp, columnName, treeId, clientID.adClientId);
      }
    }

    private void updateClientInfo(final ConnectionProvider cp, final String columnName, final String treeTypeValue)
	throws ServletException {
      if (treeTypeValue.equals("PC")) {
        UpdateADClientInfoData[] clientsID = UpdateADClientInfoData.selectClientsWithoutTree(cp, columnName);
        for (UpdateADClientInfoData clientID : clientsID) {
          UpdateADClientInfoData.updateClientTreeAuto(cp, columnName, treeTypeValue, clientID.adClientId);
        }
      }
      else if (treeTypeValue.equals("CC")) {
        UpdateADClientInfoData[] clientsID = UpdateADClientInfoData.selectClientsMissingCostcenterTree(cp, columnName);
        for (UpdateADClientInfoData clientID : clientsID) {
          UpdateADClientInfoData.updateCostcenterTreeAuto(cp, columnName, treeTypeValue, clientID.adClientId);
        }
      }
      else if (treeTypeValue.equals("U1")) {
        UpdateADClientInfoData[] clientsID = UpdateADClientInfoData.selectClientsMissingUser1Tree(cp, columnName);
        for (UpdateADClientInfoData clientID : clientsID) {
          UpdateADClientInfoData.updateUser1TreeAuto(cp, columnName, treeTypeValue, clientID.adClientId);
        }
      }
      else if (treeTypeValue.equals("U2")) {
        UpdateADClientInfoData[] clientsID = UpdateADClientInfoData.selectClientsMissingUser2Tree(cp, columnName);
        for (UpdateADClientInfoData clientID : clientsID) {
          UpdateADClientInfoData.updateUser2TreeAuto(cp, columnName, treeTypeValue, clientID.adClientId);
        }
      }
    }
}
