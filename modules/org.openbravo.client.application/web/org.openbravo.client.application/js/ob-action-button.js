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

OB.ActionButton = {};
OB.ActionButton.executingProcess = null;

isc.ClassFactory.defineClass('OBToolbarActionButton', isc.OBToolbarTextButton);

isc.OBToolbarActionButton.addProperties( {
  visible: false,
  modal: true,
  
  action : function() {
    this.runProcess();
  },

  runProcess : function() {
    var theView = this.view;
    
    //TODO: Currently autosave is only supported in form view, once it is supported
    //in grid, make use of it here.
    if (this.autosave && theView.viewForm.hasChanged) {
      var actionObject = {
        target: this,
        method: this.doAction,
        parameters: []
      };
      theView.viewForm.autoSave(actionObject);
    } else {
      // If no changes, execute action directly
      this.doAction();
    }
  },
  
  doAction: function(){
    var theView = this.view;

    var allProperties = theView.getContextInfo(false, true);
    var sessionProperties = theView.getContextInfo(true, true);
    var me = this, callbackFunction;

    OB.ActionButton.executingProcess = this;

    for ( var param in allProperties) {
      // TODO: these transformations shoulnd't be needed here as soon as getContextInfo returns 
      // the transformed values.
      
      if (allProperties.hasOwnProperty(param) && typeof allProperties[param] === 'boolean') {
        allProperties[param] = allProperties[param]?'Y':'N';
      }
    }
    
    if (this.modal){
      allProperties.Command = this.command;
      callbackFunction = function(){
        OB.Layout.ClassicOBCompatibility.Popup.open('process', 625, 450,  OB.Application.contextUrl + me.obManualURL, '', null, false, false, true, allProperties);
      };
    } else {
      var popupParams = {
            viewId: 'OBPopupClassicWindow',
            obManualURL: this.obManualURL, 
            processId: this.id,
            id: this.id,
            command: this.command,
            tabTitle: this.title,
            postParams: allProperties
          };
      callbackFunction = function(){
        OB.Layout.ViewManager.openView('OBPopupClassicWindow', popupParams);
      };
    }

    theView.setContextInfo(sessionProperties, callbackFunction);
  },
  
  closeProcessPopup: function(newWindow) {
    //Keep current view for the callback function. Refresh and look for tab message.
    var theView = this.view;
    this.view.refresh(function(){
        theView.getTabMessage();
        theView.toolBar.refreshCustomButtons();
      });

    OB.ActionButton.executingProcess = null;
    
    if (newWindow) {
      if (newWindow.indexOf(location.origin) !== -1){
        newWindow = newWindow.substr(location.origin.length);
      }
      
      if (newWindow.startsWith(OB.Application.contextUrl)){
        newWindow = newWindow.substr(OB.Application.contextUrl.length);
      }
      
      if (!newWindow.startsWith('/')){
        newWindow = '/'+newWindow;
      }
      
      var windowParams = {
          viewId : this.title,
          tabTitle: this.title,
          obManualURL : newWindow  
        };
      OB.Layout.ViewManager.openView('OBClassicWindow', windowParams);
    }
  },
  
  updateState: function(record, hide) {
    if (hide || !record) {
      this.hide();
      return;
    }
    
    this.visible = !this.displayIf || this.displayIf(null, null, this.view.viewForm, record);
    
    // Even visible is correctly set, it is necessary to execute show() or hide()
    if (this.visible){
      this.show();
    } else {
      this.hide();
    }
    
    var readonly = this.readOnlyIf && this.readOnlyIf(null, null, this.view.viewForm, record);
    if (readonly) {
      this.disable();
    } else {
      this.enable();
    }
    
    var buttonValue = record[this.property];
    if (buttonValue === '--') {
      buttonValue = 'CL';
    }
    
    var label = this.labelValue[buttonValue];
    if (!label){
      label = this.title;
    }
    this.setTitle(label);
  }
  
});
