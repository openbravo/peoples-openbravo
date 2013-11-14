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
 * All portions are Copyright (C) 2013 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

// Base OBTreeGrid class
// This class is extended by the tree used in the Tree Windows and in the Tree References
isc.ClassFactory.defineClass('OBTreeGrid', isc.TreeGrid);

isc.OBTreeGrid.addProperties({
  showOpenIcons: false,
  showDropIcons: false,
  nodeIcon: null,
  folderIcon: null,
  showSortArrow: false,
  dataProperties: {
    modelType: "parent",
    rootValue: "0",
    idField: "nodeId",
    parentIdField: "parentId",
    openProperty: "isOpen"
  },

  /**
   * When the grid is filtered, show the records that did not comply with the filter (but were ancestors of nodes that did) in grey
   */
  getCellCSSText: function (record, rowNum, colNum) {
    if (record.notFilterHit) {
      return "color:#606060;";
    } else {
      return "";
    }
  },

  clearFilter: function (keepFilterClause, noPerformAction) {
    var i = 0,
        fld, length;
    this.view.messageBar.hide();
    if (!keepFilterClause) {
      delete this.filterClause;
      delete this.sqlFilterClause;
    }
    this.forceRefresh = true;
    if (this.filterEditor) {
      if (this.filterEditor.getEditForm()) {
        this.filterEditor.getEditForm().clearValues();
        // clear the date values in a different way
        length = this.filterEditor.getEditForm().getFields().length;

        for (i = 0; i < length; i++) {
          fld = this.filterEditor.getEditForm().getFields()[i];
          if (fld.clearFilterValues) {
            fld.clearFilterValues();
          }
        }
      } else {
        this.filterEditor.setValuesAsCriteria(null);
      }
    }
    if (!noPerformAction) {
      this.filterEditor.performAction();
    }
  }
});