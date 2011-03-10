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

isc.OBStandardWindow.addClassProperties({
  COMMAND_NEW: 'NEW' // tells the window to open the first tab in new mode
});

// = OBStandardWindow =
//
// Represents the root container for an Openbravo window consisting of a
// hierarchy of tabs. Each tab is represented with an instance of the
// OBStandardView.
//
// The standard window can be opened as a result of a click on a link
// in another tab. In this case the window should open all tabs from the
// root to the target record, i.e. the target record can be in a grand-child
// tab. The flow goes through the following steps:
// 1- compute which tabs should be opened and on each tab which record (the
// tabInfo list),
// this is done in the draw method with a call to the
// org.openbravo.client.application.window.ComputeSelectedRecordActionHandler
// actionHandler
// 2- this actionhandler returns a list of tab and record id's which should be
// opened in sequence.
// 3- the first tab is opened by calling view.openDirectTab()
// 4- this loads the data in the grid, in the viewGrid.dataArrived method the
// method delayedHandleTargetRecord is called to open a child tab or the direct
// requested record.
// 5- opening a child tab is done by calling openDirectChildTab on the view,
// which again calls openDirectTab
// for the tab in the tabInfo list (computed in step 1)
//
// Note that some parts of the flow are done asynchronously to give the system
// time to
// draw all the components.
// 
isc.OBStandardWindow.addProperties({
  toolBarLayout: null,
  view: null,
  
  viewProperties: null,
  
  activeView: null,
  
  views: [],
  
  // is set when a form or grid editing results in dirty data
  // in the window
  dirtyEditForm: null,
  
  initWidget: function(){
    var standardWindow = this;
    
    this.views = [];
    
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
    if (this.command === isc.OBStandardWindow.COMMAND_NEW) {
      this.viewProperties.allowDefaultEditMode = false;
    }
    this.viewState = OB.PropertyStore.get("OBUIAPP_GridConfiguration", this.windowId);
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
    this.getClass().uiPattern = data.uiPattern;
    this.getClass().autoSave = data.autoSave;
    // set the views to readonly
    for (var i = 0; i < this.views.length; i++) {
      this.views[i].setReadOnly(data.uiPattern[this.views[i].tabId] === isc.OBStandardView.UI_PATTERN_READONLY);
      this.views[i].setSingleRecord(data.uiPattern[this.views[i].tabId] === isc.OBStandardView.UI_PATTERN_SINGLERECORD);
      
      this.views[i].toolBar.updateButtonState(true);
    }
  },
  
  isAutoSaveEnabled: function(){
    return this.getClass().autoSave;
  },
  
  isDirty: function() {
    return this.dirtyEditForm;
  },
  
  getDirtyEditForm: function() {
    return this.dirtyEditForm;
  },
  
  setDirtyEditForm: function (editObject) {
    this.dirtyEditForm = editObject;
    if (!editObject) {
      this.cleanUpAutoSaveProperties();
    }
  },

  autoSave: function() {
    this.doActionAfterAutoSave(null, true);
  },
  
  doActionAfterAutoSave: function(action, forceDialogOnFailure) {
    // if not dirty or we know that the object has errors
    if (!this.isDirty() || (this.getDirtyEditForm() && this.getDirtyEditForm().hasErrors())) {
      
      // clean up before calling the action, as the action
      // can set dirty form again
      this.cleanUpAutoSaveProperties();
      
      // nothing to do, execute immediately
      OB.Utilities.callAction(action);
      return;
    }

    if (action) {
      this.autoSaveAction = action;
    }
    
    // saving stuff already, go away
    if (this.isAutoSaving) {
      return;
    }
    
    if (!this.isAutoSaveEnabled()) {
      this.autoSaveConfirmAction();
      return;
    }

    this.isAutoSaving = true;
    this.forceDialogOnFailure = forceDialogOnFailure;
    this.getDirtyEditForm().autoSave();
  },
  
  callAutoSaveAction: function() {
    var action = this.autoSaveAction;
    this.cleanUpAutoSaveProperties();
    if (!action) {
      return;
    }
    OB.Utilities.callAction(action);
  },

  cleanUpAutoSaveProperties: function() {
    delete this.dirtyEditForm;
    delete this.isAutoSaving;
    delete this.autoSaveAction;
    delete this.forceDialogOnFailure;
  },
  
  autoSaveDone: function(view, success) {
    if (!this.isAutoSaving) {
      this.cleanUpAutoSaveProperties();
      return;
    }
    
    if (success) {
      this.callAutoSaveAction();
    } else if (!view.isVisible() || this.forceDialogOnFailure) {
      isc.warn(OB.I18N.getLabel('OBUIAPP_AutoSaveError', [view.tabTitle]));
    } else if (!this.isAutoSaveEnabled()) {
      this.autoSaveConfirmAction();
    }
    this.cleanUpAutoSaveProperties();
  },
  
  autoSaveConfirmAction: function(){
    var action = this.autoSaveAction, me = this;
    this.autoSaveAction = null;
    if (!action) {
      return;
    }

    // clean up everything
    me.cleanUpAutoSaveProperties();

    var callback = function(ok){
      if (ok) {
        me.getDirtyEditForm().resetForm();
        OB.Utilities.callAction(action);
      } else {
        // and focus to the first error field
        me.getDirtyEditForm().setFocusInErrorField(true);
        me.getDirtyEditForm().focus();
      }
    };
    isc.ask(OB.I18N.getLabel('OBUIAPP_AutoSaveNotPossibleExecuteAction'), callback);
  },
  
  addView: function(view){
    view.standardWindow = this;
    this.views.push(view);
    this.toolBarLayout.addMember(view.toolBar);
    if (this.getClass().readOnlyTabDefinition) {
      view.setReadOnly(this.getClass().readOnlyTabDefinition[view.tabId]);
    }
  },
  
  // is called from the main app tabset
  tabDeselected: function(tabNum, tabPane, ID, tab, newTab){
    this.wasDeselected = true;
  },
  
  closeClick: function(tab, tabSet){
    if (!this.activeView.viewForm.hasChanged && this.activeView.viewForm.isNew) {
      this.view.standardWindow.setDirtyEditForm(null);
    }

    var actionObject = {
      target: tabSet,
      method: tabSet.doCloseClick,
      parameters: [tab]
    };
    this.doActionAfterAutoSave(actionObject, true);
  },
  
  setActiveView: function(view){
    if (!this.isDrawn()) {
      return;
    }
    if (this.activeView === view) {
      return;
    }
    
    var currentView = this.activeView;
    // note the new activeView must be set before disabling
    // the other one
    this.activeView = view;
    if (currentView) {
      currentView.setActiveViewProps(false);
    }
    view.setActiveViewProps(true);
  },
  
  setFocusInView: function(view){
    var currentView = view || this.activeView || this.view;
    this.setActiveView(currentView);
  },

  show: function() {
    var ret = this.Super('show', arguments);
    this.setFocusInView();
    return ret;
  },
  
  draw: function(){
    var standardWindow = this, targetEntity;
    var ret = this.Super('draw', arguments);
    if (this.targetTabId) {
      for (var i = 0; i < this.views.length; i++) {
        if (this.views[i].tabId === this.targetTabId) {
          targetEntity = this.views[i].entity;
          break;
        }
      }
      if (targetEntity) {
        OB.RemoteCallManager.call('org.openbravo.client.application.window.ComputeSelectedRecordActionHandler', null, {
          targetEntity: targetEntity,
          targetRecordId: (this.targetRecordId ? this.targetRecordId : null),
          windowId: this.windowId
        }, function(response, data, request){
          standardWindow.directTabInfo = data.result;
          standardWindow.view.openDirectTab();
        });
        delete this.targetRecordId;
        delete this.targetTabId;
      }
    } else if (this.command === isc.OBStandardWindow.COMMAND_NEW) {
      var currentView = this.activeView || this.view;
      currentView.editRecord();
      this.command = null;
    }
    
    this.setFocusInView(this.view);
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
    this.setActiveView(this.view);
    this.view.doHandleClick();
  },
  
  doHandleDoubleClick: function(){
    // happens when we are getting selected
    // then don't change state
    if (this.wasDeselected) {
      this.wasDeselected = false;
      return;
    }
    this.setActiveView(this.view);
    this.view.doHandleDoubleClick();
  },
  
  // +++++++++++++ Methods for the main tab handling +++++++++++++++++++++
  
  getHelpView: function(){
    // tabTitle is set in the viewManager
    return {
        viewId: 'ClassicOBHelp',
        tabTitle: this.tabTitle + ' - ' + OB.I18N.getLabel('UINAVBA_Help'),
        windowId: this.windowId,
        windowType: 'W',
        windowName: this.tabTitle
    };
  },
  
  getBookMarkParams: function(){
    var result = {};
    result.windowId = this.windowId;
    result.viewId = this.getClassName();
    result.tabTitle = this.tabTitle;
    if (this.targetTabId) {
      result.targetTabId = this.targetTabId;
      result.targetRecordId = this.targetRecordId;
    }
    return result;
  },
  
  isEqualParams: function(params){
    var equalTab = params.windowId && params.windowId === this.windowId;
    return equalTab;
  },
  
  isSameTab: function(viewName, params){
    // always return false to force new tabs
    if (this.multiDocumentEnabled) {
      return false;
    }
    return this.isEqualParams(params) && viewName === this.getClassName();
  },
  
  setTargetInformation: function(tabId, recordId) {
    this.targetTabId = tabId;
    this.targetRecordId = recordId;
    OB.Layout.HistoryManager.updateHistory();
  },
  
  storeViewState: function(){
	var result={};
    for (var i = 0; i < this.views.length; i++) {
      if(this.views[i].viewGrid){
        result[this.views[i].tabId]=this.views[i].viewGrid.getViewState();
      }
    }
	OB.PropertyStore.set('OBUIAPP_GridConfiguration', result, this.windowId);
  }
});
