/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distribfuted  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

OB.OBEXPE = {};

OB.OBEXPE.validate = function (item, validator, value, record) {
  // item has access to grid: item.grid
  // from the grid you can get all selected records and edited values, e.g.
  //   * item.grid.getSelection()
  //   * item.grid.getEditedRecord()
  // grid has access to view: grid.view
  // view has access to parentWindow: view.parentWindow (the window running the process)
  // parentWindow has access to currentView
  // currentView has getContextInfo
  // debugger;
  return true;
};