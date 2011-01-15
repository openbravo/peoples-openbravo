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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
// This file contains direct overrides of Smartclient types.
// Normally we introduce new subtypes of Smartclient types. However for
// some cases it makes sense to directly set properties on top Smartclient
// types. This is done in this file.

// is placed here because the smartclient labels are loaded just after the smartclient
// core
isc.setAutoDraw(false);

isc.FormItem.addProperties({
  titleClick: function(form, item){
    item.focusInItem();
  },
  
  changed: function(){
    var ret = this.Super('changed', arguments);
    this._hasChanged = true;
    return ret;
  },
  
  focus: function(form, item){
    if (form.view) {
      form.view.lastFocusedItem = this;
      form.view.setAsActiveView();
    } else if (form.grid) {
      if (form.grid.view) {
        form.grid.view.lastFocusedItem = this;
        form.grid.view.setAsActiveView();
      } else if (isc.isA.RecordEditor(form.grid) && form.grid.sourceWidget && form.grid.sourceWidget.view) {
        form.grid.sourceWidget.view.lastFocusedItem = this;
        form.grid.sourceWidget.view.setAsActiveView();
      }
    }
  },
  
  blur: function(form, item){
    if (form && form.handleItemChange) {
      form.handleItemChange(this);
    }
    this._hasChanged = false;
    if (this._originalBlur) {
      return this._originalBlur(form, item);
    }
    return;
  },
  
  isDisabled: function(){
    return this.form.readOnly;
  },
  
  // return all relevant focus condition
  isFocusable: function(){
    return this.canFocus && this.isDrawn() &&
    this.isVisible() &&
    !this.isDisabled();
  }
});

isc.Canvas.addProperties({
  // let focuschanged go up to the parent, or handle it here
  focusChanged: function(hasFocus){
    if (this.view && this.view.setAsActiveView) {
      this.view.setAsActiveView();
      return this.Super('focusChanged', arguments);
    }
    
    if (this.parentElement && this.parentElement.focusChanged) {
      this.parentElement.focusChanged(hasFocus);
    }
  }
});

// overridden to never show a prompt. A prompt can be created manually 
// when overriding for example the DataSource (see the OBStandardView).
isc.RPCManager.showPrompt = false;
isc.RPCManager.neverShowPrompt = true;

