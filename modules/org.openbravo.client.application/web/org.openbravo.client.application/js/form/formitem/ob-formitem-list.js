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

// == OBListItem ==
// Combo box for list references, note is extended by OBFKItem again.
isc.ClassFactory.defineClass('OBListItem', ComboBoxItem);

isc.OBListItem.addProperties({
  operator: 'equals',
  hasPickList: true,
  showPickListOnKeypress: true,  
  cachePickListResults: false,
  completeOnTab: true,
  
  // without this in chrome the content is sorted according to the id/value
  // not the displayfield
  sortField: 0,
  
  // textMatchStyle is used for the client-side picklist
  textMatchStyle: 'substring',

  // NOTE: Setting this property to false fixes the issue when using the mouse to pick a value
  // FIXME: Sometimes the field label gets a red color (a blink)
  // addUnknownValues: false,

  selectOnFocus: true,
  moveFocusOnPickValue: true,
  
  // is overridden to keep track that a value has been explicitly picked
  pickValue: function (value) {
    this._pickedValue = true;
    this.Super('pickValue', arguments);
    delete this._pickedValue;
    if (this.moveFocusOnPickValue && this.form.focusInNextItem) {
      this.form.focusInNextItem(this.name);
    }
  },

  changed: function() {
    this.Super('changed', arguments);
    // if not picking a value then don't do a fic call
    // otherwise every keypress would result in a fic call
    if (!this._pickedValue) {
      return;
    }
    if (this._hasChanged && this.form && this.form.handleItemChange) {
      this.form.handleItemChange(this);
    }
  },
  
  blur: function(form, item){
    // always validate if not part of a filter editor
    if (!this.form.grid || !this.form.grid.isAFilterEditor || !this.form.grid.isAFilterEditor()) {
      this.validate();
    }
    return this.Super('blur', arguments);
  },
  
  pickListProperties: {
    showHeaderContextMenu: false
  },

  // prevent ids from showing up
  mapValueToDisplay : function (value) {
    var ret = this.Super('mapValueToDisplay', arguments);
    if (ret === value && this.isDisabled()) {
      return '';
    }
    if (ret === value && !this.valueMap) {
      this.valueMap = {};
      this.valueMap[value] = '';
      return '';
    }
    return ret;
  }
  
});

