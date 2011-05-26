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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

// == OBMiniDateRangeItem ==
// OBMiniDateRangeItem inherits from SmartClient MiniDateRangeItem
// Is used for filtering dates in the grid.
isc.ClassFactory.defineClass('OBDateRangeDialog', isc.DateRangeDialog);

isc.OBDateRangeDialog.addProperties({
  initWidget: function() {
    this.Super('initWidget', arguments);
    this.rangeForm.setFocusItem(this.rangeItem);
   },
  
  show: function() {
    this.Super('show', arguments);
    this.rangeForm.items[0].fromField.calculatedDateField.canFocus = false;
    this.rangeForm.items[0].toField.calculatedDateField.canFocus = false;
    this.rangeForm.items[0].fromField.valueField.focusInItem();
    this.rangeForm.focus();
  },
  
  // trick: overridden to let the ok and clear button change places
  addAutoChild: function(name, props) {
    if (name === 'okButton') {
      return this.Super('addAutoChild', ['clearButton', {canFocus:true, title: this.clearButtonTitle}]);
    } else if (name === 'clearButton') {
      return this.Super('addAutoChild', ['okButton', {canFocus:true, title: this.okButtonTitle}]);
    } else {
      return this.Super('addAutoChild', arguments);
    }
  }

});

isc.ClassFactory.defineClass('OBMiniDateRangeItem', isc.MiniDateRangeItem);

isc.OBMiniDateRangeItem.addProperties({
  // note this one needs to be set to let the formatDate be called below
  dateDisplayFormat: OB.Format.date,
  rangeDialogConstructor: isc.OBDateRangeDialog,
  
  // prevent illegal values from showing up
  updateValue: function(data) {
    var illegalStart = data && data.start && !this.isCorrectRangeValue(data.start);
    var illegalEnd = data && data.end && !this.isCorrectRangeValue(data.end);
    if (illegalStart || illegalEnd) {
      return;
    }
    this.Super('updateValue', arguments);
  },
  
  isCorrectRangeValue: function(value) {
    if (!value) {
      return false;
    }
    if (isc.isA.Date(value)) {
      return true;
    }
    if (value._constructor && value._constructor === 'RelativeDate') {
      return true;
    }
    return false;
  },
  
  keyPress: function(item, form, keyName, characterValue){
    if (keyName === 'Enter') {
      this.showRangeDialog();
      return false;
    }
    return true;
  },
  
  formatDate: function(dt) {
    return OB.Utilities.Date.JSToOB(dt, OB.Format.date);
  }

});

