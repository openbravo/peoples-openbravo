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

// We have dates/times in the database without timezone, we assume GMT therefore 
// for all our date/times we use GMT on both the server and the client
// NOTE: causes issue https://issues.openbravo.com/view.php?id=16014
//Time.setDefaultDisplayTimezone(0);

//Let the click on an ImgButton and Button fall through to its action method 
isc.ImgButton.addProperties({
  click: function() {
    if (this.action) {
      this.action();
    }
  }
});

isc.Button.addProperties({
  click: function() {
    if (this.action) {
      this.action();
    }
  }
});

// NOTE BEWARE: methods/props added here will overwrite and NOT extend FormItem
// properties! 
isc.FormItem.addProperties({
  // always take up space when an item is hidden in a form
  alwaysTakeSpace: true,

  // disable tab to icons
  canTabToIcons: false,
  
  _original_init: isc.FormItem.getPrototype().init,
  init: function() {
    OB.Utilities.addRequiredSuffixToBaseStyle(this);
    // and continue with the original init
    this._original_init();
  },
  
  _handleTitleClick: isc.FormItem.getPrototype().handleTitleClick,
  
  handleTitleClick: function() {
    // always titleclick directly as sc won't call titleclick
    // in that case
    if (this.isDisabled()) {
      this.titleClick(this.form, this);
      return false;
    }
    // forward to the original method
    return this._handleTitleClick();
  },
  
  titleClick: function(form, item){
    item.focusInItem();
    if (item.linkButtonClick) {
      item.linkButtonClick();
    }
  },
  
  changed: function(){
    this._hasChanged = true;
    this.clearErrors();
  },
  
  focus: function(form, item){
    var view = OB.Utilities.determineViewOfFormItem(item);
    if (view) {
      view.lastFocusedItem = this;
    }
  },
  
  click: function() {
    var view = OB.Utilities.determineViewOfFormItem(this);
    if (view) {
      view.lastFocusedItem = this;
      // this handles the case that there was a direct click on picker icon
      // don't set the focus when a picker is shown as it will remove the 
      // picker directly
      if (!this.picker || !this.picker.isVisible || !this.picker.isVisible()) {
        this.focusInItem();
      }
    }
  },

  blur: function(form, item){
    if (item._hasChanged && form && form.handleItemChange) {
      form.handleItemChange(this);
    }
    return;
  },
  
  isDisabled: function(){
    // disabled if the property can not be updated and the form or record is new
    // explicitly comparing with false as it is only set for edit form fields
    if (this.updatable === false && !(this.form.isNew || this.form.getValue('_new'))) {
      this.canFocus = false;
      return true;
    }
    var disabled = this.form.readOnly || this.disabled;
    // allow focus if all items are disabled
    this.canFocus = this.form.allItemsDisabled || !disabled;
    return disabled || this.form.allItemsDisabled;
  },
  
  // return all relevant focus condition
  isFocusable: function(){    
    return this.getCanFocus() && this.isDrawn() &&
        this.isVisible() && !this.isDisabled();
  },
  
  // overridden to never use the forms datasource for fields
  getOptionDataSource : function () {
    var ods = this.optionDataSource;

    if (isc.isA.String(ods)) {
      ods = isc.DataSource.getDataSource(ods);
    }
    
    return ods;
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

// prevent caching of picklists globally to prevent js error 
// when a picklist has been detached from a formitem
isc.PickList.getPrototype().cachePickListResults = false;

// allow max 10000 days/years/quarters in the past/future
isc.RelativeDateItem.changeDefaults('quantityFieldDefaults', {max: 1000});

isc.RelativeDateItem.addProperties({
  // overridden as the displayDateFormat does not seem to work fine
  formatDate: function(dt) {
    return OB.Utilities.Date.JSToOB(dt, OB.Format.date);
  }

});

// uncomment this code and put a breakpoint to get a better control
// on from where async operations are started
//isc.Class._fireOnPause = isc.Class.fireOnPause;
//isc.Class.fireOnPause = function(id, callback, delay, target, instanceID) {
//  isc.Class._fireOnPause(id, callback, delay, target, instanceID);
//};