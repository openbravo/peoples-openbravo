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

// NOTE BEWARE: methods/props added here will overwrite and NOT extend FormItem
// properties! 
isc.FormItem.addProperties({
  titleClick: function(form, item){
    item.focusInItem();
  },
  
  changed: function(){
    this._hasChanged = true;
  },
  
  focus: function(form, item){
    var view = this.getView();
    if (!view) {
      return;
    }
    view.lastFocusedItem = this;
    view.setAsActiveView();
  },

  getView: function() {
    var form = this.form;
    if (form.view) {
      return form.view;
    } else if (form.grid) {
      if (form.grid.view) {
        return form.grid.view;
      } else if (isc.isA.RecordEditor(form.grid) && form.grid.sourceWidget && form.grid.sourceWidget.view) {
        return form.grid.sourceWidget.view;
      }
    }
    return null;
  },
  
  click: function() {
    var view = this.getView();
    if (view) {
      view.lastFocusedItem = this;
      this.focusInItem();
    }
  },
  
  // get the original focusInItem
  _originalFocusInItem: isc.FormItem.getInstanceProperty('focusInItem'),
  focusInItem: function() {
    if (this._inFocusInItem) {
      return;
    }
    this._inFocusInItem = true;
    var view = this.getView();
    if (view) {
      view._inFocusInItem = this;
    }
    // forward to the original one
    this._originalFocusInItem();
    this._inFocusInItem = false;
    if (view) {
      this._inFocusInItem = null;
    }
  },
  
  blur: function(form, item){
    if (item._hasChanged && form && form.handleItemChange) {
      form.handleItemChange(this);
    }
    return;
  },
  
  isDisabled: function(){
    return this.form.readOnly || this.disabled;
  },
  
  // return all relevant focus condition
  isFocusable: function(){    
    return this.getCanFocus() && this.isDrawn() &&
        this.isVisible() && !this.isDisabled();
  }
});

isc.Canvas.addProperties({
  // let focuschanged go up to the parent, or handle it here
  focusChanged: function(hasFocus){
    var view = this.getView();
    if (hasFocus && view) {
      view.setAsActiveView();
      return;
    }
    
    if (this.parentElement && this.parentElement.focusChanged) {
      this.parentElement.focusChanged(hasFocus);
    }
  },
  
  getView: function() {
    if (this.view && this.view.setAsActiveView) {
      return this.view;
    }
    if (this.grid && this.grid.view && this.grid.view.setAsActiveView) {
      return this.grid.view;
    }
    return null;
  }
});

// overridden to never show a prompt. A prompt can be created manually 
// when overriding for example the DataSource (see the OBStandardView).
isc.RPCManager.showPrompt = false;
isc.RPCManager.neverShowPrompt = true;

// Overrides hasFireBug function to always return false,
// the SmartClient code has too many trace() calls that result in worse
// performance when using Firefox/Firebug
isc.Log.hasFireBug = function() { return false; };

