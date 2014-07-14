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

isc.ClassFactory.defineClass('OBParameterWindowForm', isc.DynamicForm);

// = OBParameterWindowForm =
// The OBParameterWindowForm is the DynamicForm used in the OBParameterWindowView
isc.OBParameterWindowForm.addProperties({
  paramWindow: null,
  width: '99%',
  titleSuffix: '',
  requiredTitleSuffix: '',
  autoFocus: true,
  titleOrientation: 'top',
  numCols: 4,
  showErrorIcons: false,
  colWidths: ['*', '*', '*', '*'],
  itemChanged: function (item, newValue) {
    this.paramWindow.handleReadOnlyLogic();
    this.paramWindow.okButton.setEnabled(this.paramWindow.allRequiredParametersSet());
  },

  // this function is invoked on the blur action of the formitems
  // this is the proper place to execute the client-side callouts
  handleItemChange: function (item) {
    var affectedParams, i, field;
    // Execute onChangeFunctions if they exist
    if (this && OB.OnChangeRegistry.hasOnChange(this.paramWindow.viewId, item)) {
      OB.OnChangeRegistry.call(this.paramWindow.viewId, item, this.paramWindow, this, this.paramWindow.viewGrid);
    }
    // Check validation rules (subordinated fields), when value of a
    // parent field is changed, all its subordinated are reset
    affectedParams = this.paramWindow.dynamicColumns[item.name];
    if (affectedParams) {
      for (i = 0; i < affectedParams.length; i++) {
        field = this.getField(affectedParams[i]);
        if (field && field.setValue) {
          field.setValue(null);
          this.itemChanged(field, null);
        }
      }
    }
    // evaluate explicitly the display logic for the grid fields
    this.paramWindow.handleDisplayLogicForGridColumns();
    // force a redraw to reevaluate the display logic of the parameters
    // if possible the redraw should be done before setting the availability of the ok button
    // if the updated item is a date/datetime, then it is not possible to do a redraw at this point, because in that case the focus does not go properly to the next parameter
    if (isc.SimpleType.getType(item.type).inheritsFrom === 'date' || isc.SimpleType.getType(item.type).inheritsFrom === 'datetime') {
      this.markForRedraw();
    } else {
      this.redraw();
    }
    this.paramWindow.okButton.setEnabled(this.paramWindow.allRequiredParametersSet());
  }
});