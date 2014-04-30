/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use. this
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
 ************************************************************************
 */


// == OBPickEditGridItem ==
isc.ClassFactory.defineClass('OBPickEditGridItem', isc.CanvasItem);

isc.OBPickEditGridItem.addProperties({
  rowSpan: 4,
  colSpan: 4,
  defaultFilter: null,

  init: function () {
    if (this.view.popup) {
      this.canvas = isc.OBPickAndExecuteView.create({
        viewProperties: this.viewProperties,
        view: this.view,
        parameterName: this.name,
        height: this.view.height - 315,
        width: this.view.width - 155
      });
    } else {
      this.canvas = isc.OBPickAndExecuteView.create({
        viewProperties: this.viewProperties,
        view: this.view,
        parameterName: this.name
      });
    }
    this.Super('init', arguments);
    this.selectionLayout = this.canvas;
  },

  getValue: function () {
    var allProperties = {};
    allProperties._selection = [];
    allProperties._allRows = [];
    allProperties._allRows = this.canvas.viewGrid.data.allRows || this.canvas.viewGrid.data.localData || this.canvas.viewGrid.data;
    allProperties._selection = this.canvas.viewGrid.getSelectedRecords();
    return (allProperties);
  },

  setDisabled: function (newState) {
    this.Super('setDisabled', arguments);
    if (newState === true) {
      this.setDisabled(false);
      this.canvas.viewGrid.setCanEdit(false);
    } else {
      this.canvas.viewGrid.setCanEdit(true);
    }
  },

  setDefaultFilter: function (defaultFilter) {
    this.defaultFilter = defaultFilter;
  },

  destroy: function () {
    this.canvas.destroy();
    this.Super('destroy', arguments);
  }
});