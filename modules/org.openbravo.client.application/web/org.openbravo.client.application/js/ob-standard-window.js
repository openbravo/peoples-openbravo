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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
isc.ClassFactory.defineClass('OBStandardWindow', isc.VLayout);

// = OBStandardWindow =
//
// Represents the root container for an Openbravo window consisting of a 
// hierarchy of tabs. Each tab is represented with an instance of the 
// OBStandardView. 
//
isc.OBStandardWindow.addProperties({
  toolBarLayout: null,
  view: null,
  
  viewProperties: null,
  
  activeView: null,
  
  views: [],
  
  initWidget: function(){
    var standardWindow = this;
    
    this.toolBarLayout = isc.HLayout.create({
      width: '100%',
      height: 1, // is set by its content
      overflow: 'visible'
    });
    
    if (this.targetTabId) {
      // is used as a flag so that we are in direct link mode
      // prevents extra fetch data actions
      this.directTabInfo = {};
    }
        
    this.addMember(this.toolBarLayout);
    
    this.viewProperties.standardWindow = this;
    this.viewProperties.isRootView = true;
    this.view = isc.OBStandardView.create(this.viewProperties);
    this.addView(this.view);
    this.addMember(this.view);
    
    this.Super('initWidget', arguments);
    
    // is set later after creation
    this.view.tabTitle = this.tabTitle;
    
    // retrieve user specific window settings from the server
    // they are stored at class level to only do the call once
    if (!this.getClass().windowSettingsRead) {
      OB.RemoteCallManager.call('org.openbravo.client.application.WindowSettingsActionHandler', null, {
        windowId: this.windowId
      }, function(response, data, request){
        standardWindow.setWindowSettings(data);
      });
    }
  },
  
  // set window specific user settings, purposely set on class level
  setWindowSettings: function(data){
    if (this.getClass().windowSettingsRead) {
      return;
    }
    this.getClass().windowSettingsRead = true;
    this.getClass().readOnlyTabDefinition = data.readOnlyDefinition;
    this.getClass().autoSave = data.autoSave;
    // set the views to readonly
    for (var i = 0; i < this.views.length; i++) {
      this.views[i].setReadOnly(data.readOnlyDefinition[this.views[i].tabId]);
    }
  },
  
  isAutoSave: function(){
    if (this.getClass().autoSave) {
      return true;
    }
    return false;
  },
  
  addView: function(view){
    view.standardWindow = this;
    this.views.push(view);
    this.toolBarLayout.addMember(view.toolBar);
    if (this.getClass().readOnlyTabDefinition) {
      view.setReadOnly(this.getClass().readOnlyTabDefinition[view.tabId]);
    }
  },
  
  show: function(){
    var ret = this.Super('show', arguments);
    this.setFocusInView();
    return ret;
  },
  
  // is called from the main app tabset
  tabDeselected: function(tabNum, tabPane, ID, tab, newTab){
    // note: explicitly checking for grid visibility as the form
    // may already be hidden
    if (this.activeView && this.activeView.isShowingForm) {
      this.activeView.viewForm.autoSave(null, true);
    }
    this.wasDeselected = true;
  },
  
  closeClick: function(tab, tabSet){
    var actionObject = {
      target: tabSet,
      method: tabSet.doCloseClick,
      parameters: [tab]
    };
    this.view.viewForm.autoSave(actionObject);
  },
  
  setActiveView: function(view){
    if (!this.isDrawn()) {
      return;
    }
    var currentActiveView = this.activeView;
    if (this.activeView === view) {
      return;
    }
    if (currentActiveView) {
      currentActiveView.setActiveViewVisualState(false);
    }
    this.activeView = view;
    view.setActiveViewVisualState(true);
  },
  
  setFocusInView: function(){
    var currentView = this.activeView || this.view;
    currentView.setViewFocus();
    currentView.setAsActiveView(true);
  },
  
  draw: function(){
    var standardWindow = this;
    var ret = this.Super('draw', arguments);
    
    if (this.targetTabId) {
      OB.RemoteCallManager.call('org.openbravo.client.application.window.ComputeSelectedRecordActionHandler', null, {
        targetEntity: this.targetEntity,
        targetRecordId: this.targetRecordId,
        windowId: this.windowId
      }, function(response, data, request){
        standardWindow.directTabInfo = data.result;
        standardWindow.view.openDirectTab();
      });
      delete this.targetRecordId;
      delete this.targetTabId;
      delete this.targetEntity;
    }
    
    this.setFocusInView();
    return ret;
  },
  
  setViewTabId: function(viewTabId){
    this.view.viewTabId = viewTabId;
    this.viewTabId = viewTabId;
  },
  
  doHandleClick: function(){
    // happens when we are getting selected
    // then don't change state
    if (this.wasDeselected) {
      this.wasDeselected = false;
      return;
    }
    this.view.doHandleClick();
  },
  
  doHandleDoubleClick: function(){
    // happens when we are getting selected
    // then don't change state
    if (this.wasDeselected) {
      this.wasDeselected = false;
      return;
    }
    this.view.doHandleDoubleClick();
  },
  
  getBookMarkParams: function(){
    var result = {};
    result.windowId = this.windowId;
    result.viewId = this.getClassName();
    result.tabTitle = this.tabTitle;
    return result;
  },
  
  isEqualParams: function(params){
    var equalTab = params.windowId && params.windowId === this.windowId;
    return equalTab;
  },
  
  isSameTab: function(viewName, params){
    return this.isEqualParams(params);
  },
  
  // returns the view information for the help view.
  getHelpView: function(){
    return null;
  }
});
