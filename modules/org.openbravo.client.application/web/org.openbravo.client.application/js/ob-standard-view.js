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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

isc.ClassFactory.defineClass('OBStandardWindow', isc.VLayout);

// The OBStandardWindow contains the toolbar and the root view.
isc.OBStandardWindow.addProperties({
  toolBar: null,
  view: null,
  
  viewProperties: null,
  
  initWidget: function(){
    var standardWindow = this;
    this.toolBar = isc.OBToolbar.create({
      height: this.toolBarHeight,
      minHeight: this.toolBarHeight,
      maxHeight: this.toolBarHeight,
      leftMembers: [isc.OBToolbarIconButton.create({
        action: 'alert(\'New Document\')',
        buttonType: 'newDoc',
        prompt: 'New Document'
      }), isc.OBToolbarIconButton.create({
        action: 'alert(\'New Row\')',
        buttonType: 'newRow',
        prompt: 'New Row'
      })],
      rightMembers: [isc.OBToolbarTextButton.create({
        action: 'alert(\'Button A\')',
        title: 'Button A'
      })]
    });
    
    this.toolBar.addLeftMembers([isc.OBToolbarIconButton.create({
      action: 'alert(\'Save\')',
      buttonType: 'save',
      prompt: 'Save'
    }), isc.OBToolbarIconButton.create({
      action: 'alert(\'Create Copy\')',
      buttonType: 'createCopy',
      prompt: 'Create Copy'
    })]);
    
    this.toolBar.addRightMembers([isc.OBToolbarTextButton.create({
      action: 'alert(\'Button B\')',
      title: 'Button B'
    })]);
    
    this.addMember(this.toolBar);
    
    this.viewProperties.standardWindow = this;
    
    this.view = isc.OBStandardView.create(this.viewProperties);
    this.addMember(this.view);
    this.Super('initWidget', arguments);
    this.view.toolBar = this.toolBar;
    
    // is set later after creation
    this.view.tabTitle = this.tabTitle;
  },
  
  draw: function(){
    var standardWindow = this;
    var ret = this.Super('draw', arguments);
    
    if (this.targetRecordId) {
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
    
    return ret;
  },
  
  setViewTabId: function(viewTabId){
    this.view.viewTabId = viewTabId;
    this.viewTabId = viewTabId;
  },
  
  doHandleClick: function(){
    this.view.doHandleClick();
  },
  
  doHandleDoubleClick: function(){
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

// Note: currently this class covers both the top-level view as well as child views
// in child tabs. If there is a lot of separate code then maybe the 2 types of usages
// have to be factored out in separate classes.
isc.ClassFactory.defineClass('OBStandardView', isc.VLayout);

isc.OBStandardView.addClassProperties({
  STATE_TOP_MAX: 'TopMax', // the part in the top is maximized, meaning
  // that the tabset in the bottom is minimized
  STATE_BOTTOM_MAX: 'BottomMax', // the tabset part is maximized, the
  // the top has height 0
  STATE_MID: 'Mid', // the view is split in the middle, the top part has
  // 50%, the tabset also
  STATE_IN_MID: 'InMid', // state of the tabset which is shown in the middle,
  // the parent of the tabset has state
  // isc.OBStandardView.STATE_MID
  STATE_MIN: 'Min' // minimized state, the parent has
  // isc.OBStandardView.STATE_TOP_MAX or
  // isc.OBStandardView.STATE_IN_MID
});

// = OBStandardView =
// The OBStandardView is the composite canvas which contains a form/grid and
// child tabs.

isc.OBStandardView.addProperties({

  // properties used by the ViewManager, only relevant in case this is the
  // top
  // view shown directly in the main tab
  showsItself: false,
  tabTitle: null,
  
  // ** {{{ windowId }}} **
  // The id of the window shown here, only set for the top view in the
  // hierarchy
  // and if this is a window/tab view.
  windowId: null,
  
  // ** {{{ tabId }}} **
  // The id of the tab shown here, set in case of a window/tab view.
  tabId: null,
  
  // ** {{{ processId }}} **
  // The id of the process shown here, set in case of a process view.
  processId: null,
  
  // ** {{{ formId }}} **
  // The id of the form shown here, set in case of a form view.
  formId: null,
  
  // ** {{{ parentView }}} **
  // The parentView if this view is a child in a parent child structure.
  parentView: null,
  
  // ** {{{ parentTabSet }}} **
  // The tabSet which shows this view. If the parentView is null then this
  // is the
  // top tabSet.
  parentTabSet: null,
  tab: null,
  
  // ** {{{ toolbar }}} **
  // The toolbar canvas.
  toolBar: null,
  
  // ** {{{ formGridLayout }}} **
  // The layout which holds the form and grid.
  formGridLayout: null,
  
  // ** {{{ childTabSet }}} **
  // The tabSet holding the child tabs with the OBView instances.
  childTabSet: null,
  
  // ** {{{ hasChildTabs }}} **
  // Is set to true if there are child tabs.
  hasChildTabs: false,
  
  // ** {{{ dataSource }}} **
  // The dataSource used to fill the data in the grid/form.
  dataSource: null,
  
  // ** {{{ viewForm }}} **
  // The viewForm used to display single records
  viewForm: null,
  
  // ** {{{ viewGrid }}} **
  // The viewGrid used to display multiple records
  viewGrid: null,
  
  // ** {{{ parentProperty }}} **
  // The name of the property refering to the parent record, if any
  parentProperty: null,
  
  // ** {{{ targetRecordId }}} **
  // The id of the record to initially show.
  targetRecordId: null,
  
  // ** {{{ targetEntity }}} **
  // The entity to show.
  entity: null,
  
  width: '100%',
  height: '100%',
  margin: 0,
  padding: 0,
  overflow: 'hidden',
  
  // set if one record has been selected
  lastRecordSelected: null,
  
  // ** {{{ refreshContents }}} **
  // Should the contents listgrid/forms be refreshed when the tab
  // gets selected and shown to the user.
  refreshContents: true,
  
  state: isc.OBStandardView.STATE_MID,
  previousState: isc.OBStandardView.STATE_TOP_MAX,
  
  initWidget: function(properties){
    var isRootView = !this.parentProperty;
    
    if (isRootView) {
      this.buildStructure();
    }
    
    OB.TestRegistry.register('org.openbravo.client.application.ViewGrid_' + this.tabId, this.viewGrid);
    OB.TestRegistry.register('org.openbravo.client.application.ViewForm_' + this.tabId, this.viewForm);
    
    this.Super('initWidget', arguments);
  },
  
  buildStructure: function(){
    var isRootView = !this.parentProperty;
    // if (isRootView) {
    // this.setOverflow('hidden');
    // } else {
    // this.setOverflow('auto');
    // }
    this.createMainParts();
    this.createViewStructure();
    this.dataSource = OB.Datasource.get(this.dataSourceId, this);
    
    if (isRootView) {
      if (this.childTabSet) {
        this.members[0].setHeight('50%');
        this.members[1].setHeight('50%');
        this.childTabSet.setState(isc.OBStandardView.STATE_IN_MID);
        this.childTabSet.selectTab(this.childTabSet.tabs[0]);
        
        OB.TestRegistry.register('org.openbravo.client.application.ChildTabSet_' + this.tabId, this.viewForm);
      } else {
        this.members[0].setHeight('100%');
      }
    }
  },
  
  setDataSource: function(ds){
  //Wrap DataSource with OBDataSource which overrides methods to set tab info7
    var obDsClassname = 'OBDataSource'+this.tabId;
    isc.defineClass(obDsClassname, ds.getClass());
    
    var modifiedDs = isc.addProperties({},ds,{
        updateData: function(updatedRecord, callback, requestProperties){
        var newRequestProperties = OB.Utilities._getTabInfoRequestProperties(this.view, requestProperties);
        //standard update is not sent with operationType
        var additionalPara = {
          _operationType: 'update'
        };
        isc.addProperties(requestProperties.params, additionalPara);
        this.Super('updateData', [updatedRecord, callback, newRequestProperties]);
      },
      
      addData : function(updatedRecord, callback, requestProperties){
        var newRequestProperties = OB.Utilities._getTabInfoRequestProperties(this.view, requestProperties);
        //standard update is not sent with operationType
        var additionalPara = {
          _operationType: 'add'
        };
        isc.addProperties(requestProperties.params, additionalPara);
        this.Super('addData', [updatedRecord, callback, newRequestProperties]);
      },
      
      removeData: function(updatedRecord, callback, requestProperties){
        var newRequestProperties = OB.Utilities._getTabInfoRequestProperties(this.view, requestProperties);
        //standard update is not sent with operationType
        var additionalPara = {
          _operationType: 'remove'
        };
        isc.addProperties(requestProperties.params, additionalPara);
        this.Super('removeData', [updatedRecord, callback, newRequestProperties]);
      },

			transformResponse: function (dsResponse, dsRequest, jsonData) {
        if (!jsonData.response || jsonData.response.status === 'undefined' || jsonData.response.status !== 0){ //0 is success
          if (jsonData.response && jsonData.response.error) {
            var error = jsonData.response.error;
            if (error.type && error.type === 'user') {
              OB.KernelUtilities.handleUserException(error.message, error.params);
            }
            else {
              OB.KernelUtilities.handleSystemException(error.message);
            }
          } else {
            OB.KernelUtilities.handleSystemException('Error occured');
          }
        }
        return this.Super('transformResponse', arguments);  
      },
			
      view: this
    });
    
    var myDs = isc[obDsClassname].create(modifiedDs);
  
    this.dataSource = myDs;
    
    if (this.viewGrid) {
      if (this.targetRecordId) {
        this.viewGrid.targetRecordId = this.targetRecordId;
      }
      this.viewGrid.setDataSource(this.dataSource, this.viewGrid.completeFields || this.viewGrid.fields);
      if (!this.parentProperty) {
        this.viewGrid.fetchData();
        this.refreshContents = false;
      }
    }
    if (this.viewForm) {
      this.viewForm.setDataSource(this.dataSource, this.viewForm.fields);
    }
    // open default edit view if this is the rootview
    if (this.defaultEditMode && !this.parentProperty) {
      this.openDefaultEditView();
    }
  },
  
  draw: function(){
    var result = this.Super('draw', arguments);
    if (!this.viewGrid || !this.viewGrid.filterEditor) {
      return result;
    }
    this.viewGrid.focusInFilterEditor();
    return result;
  },
  
  // ** {{{ createViewStructure }}} **
  // Is to be overridden, is called in initWidget.
  createViewStructure: function(){
  },
  
  // ** {{{ createMainParts }}} **
  // Creates the main layout components of this view.
  createMainParts: function(){
    var isRootView = !this.parentProperty;
    var me = this;
    if (this.tabId && this.tabId.length > 0) {
      this.formGridLayout = isc.HLayout.create({
        width: '100%',
        height: '*',
        overflow: 'auto',
        view: this,
        focusChanged: function(hasFocus){
//          console.log("Tab " + this.view.tabTitle + ' --> ' + hasFocus + ' ' + this.view.containsFocus());
        }
      });
      
      if (this.viewGrid) {
        this.viewGrid.setWidth('100%');
        this.viewGrid.view = this;
        this.formGridLayout.addMember(this.viewGrid);
      }
      
      if (this.viewForm) {
        this.viewForm.setWidth('100%');
        this.formGridLayout.addMember(this.viewForm);
        this.viewForm.view = this;
      }
      
      this.statusBar = this.createStatusBar();
      
      this.statusBarFormLayout = isc.VLayout.create({
        width: '100%',
        height: '*',
        visibility: 'hidden',
        leaveScrollbarGap: true,
        overflow: 'auto'
      });
      this.statusBarFormLayout.addMember(this.statusBar);
      this.statusBarFormLayout.addMember(this.viewForm);
      
      this.formGridLayout.addMember(this.statusBarFormLayout);
      this.addMember(this.formGridLayout);
    }
    if (this.hasChildTabs) {
      this.childTabSet = isc.OBStandardViewTabSet.create({
        parentContainer: this,
        parentTabSet: this.parentTabSet
      });
      this.addMember(this.childTabSet);
    }
  },
  
  // ** {{{ addChildView }}} **
  // The addChildView creates the child tab and sets the pointer back to
  // this
  // parent.
  addChildView: function(childView){
    childView.standardWindow = this.standardWindow;
    
    childView.parentView = this;
    childView.parentTabSet = this.childTabSet;
    
    // build the structure of the children
    childView.buildStructure();
    
    var childTabDef = {
      title: childView.tabTitle,
      pane: childView
    };
    
    this.childTabSet.addTab(childTabDef);
    
    childView.tab = this.childTabSet.tabs[this.childTabSet.tabs.length - 1];
    
    OB.TestRegistry.register('org.openbravo.client.application.ChildTab_' + this.tabId + "_" + childView.tabId, childView.tab);
    
  },
  
  doRefreshContents: function(){
    // refresh when shown
    if (this.parentTabSet && this.parentTabSet.state === isc.OBStandardView.STATE_MIN) {
      return;
    }
    if (!this.refreshContents) {
      return;
    }
    var me = this;
    this.viewForm.clearErrors();
    this.viewForm.clearValues();
    // open default edit view if there is no parent view or if there is at least
    // one parent record selected
    if (this.shouldOpenDefaultEditMode()) {
      this.openDefaultEditView();
    } else if (!this.viewGrid.isVisible()) {
      this.switchFormGridVisibility();
    }
    this.viewGrid.refreshContents();
    this.refreshContents = false;
  },
  
  shouldOpenDefaultEditMode: function(){
    // can open default edit mode if defaultEditMode is set
    // and this is the root view or a child view with a selected parent.
    return this.defaultEditMode && (!this.parentProperty || this.parentView.viewGrid.getSelectedRecords().length === 1)
  },
  
  openDefaultEditView: function(record){
    if (!this.shouldOpenDefaultEditMode()) {
      return;
    }
    
    // open form in insert mode
    if (record) {
      this.editRecord(record);
    } else if (!this.viewGrid.data || this.viewGrid.data.getLength() === 0) {
      // open in insert mode
      this.viewGrid.hide();
      this.statusBarFormLayout.show();
      this.statusBarFormLayout.setHeight('100%');
    } else {
      // edit the first record
      this.editRecord(this.viewGrid.getRecord(0));
    }
  },
  
  // ** {{{ switchFormGridVisibility }}} **
  // Switch from form to grid view or the other way around
  switchFormGridVisibility: function(){
    if (this.viewGrid.isVisible()) {
      this.viewGrid.hide();
      this.statusBarFormLayout.show();
      this.statusBarFormLayout.setHeight('100%');
    } else {
      this.statusBarFormLayout.hide();
      this.viewGrid.show();
      this.viewGrid.setHeight('100%');
    }
    this.updateTabTitle();
  },
  
  doHandleClick: function(){
    if (!this.childTabSet) {
      return;
    }
    if (this.state !== isc.OBStandardView.STATE_BOTTOM_MAX) {
      this.setHalfSplit();
      this.previousState = isc.OBStandardView.STATE_TOP_MAX;
      this.state = isc.OBStandardView.STATE_MID;
    }
  },
  
  doHandleDoubleClick: function(){
    var tempState;
    if (!this.childTabSet) {
      return;
    }
    tempState = this.state;
    this.state = this.previousState;
    if (this.previousState === isc.OBStandardView.STATE_BOTTOM_MAX) {
      this.setBottomMaximum();
    } else if (this.previousState === isc.OBStandardView.STATE_MID) {
      this.setHalfSplit();
    } else if (this.previousState === isc.OBStandardView.STATE_TOP_MAX) {
      this.setTopMaximum();
    } else {
      isc.warn(this.previousState + ' not supported ');
    }
    this.previousState = tempState;
  },
  
  // ** {{{ editRecord }}} **
  // Opens the edit form and selects the record in the grid, will refresh
  // child views also
  editRecord: function(record){
  
    // this.recordSelected(record);
    this.viewForm.editRecord(record);
    this.viewForm.clearErrors();
    if (this.viewGrid.isVisible()) {
      this.switchFormGridVisibility();
    }
    this.viewGrid.doSelectSingleRecord(record);
  },
  
  // check if a child tab should be opened directly
  openDirectChildTab: function(){
    if (this.childTabSet) {
      var i, tabs = this.childTabSet.tabs;
      for (i = 0; i < tabs.length; i++) {
        if (tabs[i].pane.openDirectTab()) {
          return;
        }
      }
    }
    
    // no child tabs to open anymore, show ourselves as the default view
    // open this view
    if (this.parentTabSet) {
      this.parentTabSet.setState(isc.OBStandardView.STATE_MID);
    } else {
      this.doHandleClick();
    }
    var gridRecord = this.viewGrid.getSelectedRecord();
    this.editRecord(gridRecord);
    
    // remove this info
    delete this.standardWindow.directTabInfo;
  },
  
  openDirectTab: function(){
    if (!this.dataSource) {
      // wait for the datasource to arrive
      this.delayCall('openDirectTab', null, 200, this);
      return;
    }
    var i, thisView = this, tabInfos = this.standardWindow.directTabInfo;
    if (!tabInfos) {
      return;
    }
    for (i = 0; i < tabInfos.length; i++) {
      if (tabInfos[i].targetTabId === this.tabId) {
        // found it...
        this.viewGrid.targetRecordId = tabInfos[i].targetRecordId;
        
        if (this.parentTabSet && this.parentTabSet.getSelectedTab() !== this.tab) {
          this.parentTabSet.selectTab(this.tab);
        } else {
          // make sure that the content gets refreshed
          this.refreshContents = true;
          // refresh and open a child view when all is done
          this.doRefreshContents();
        }
        return true;
      }
    }
    return false;
  },
  
  // ** {{{ recordSelected }}} **
  // Is called when a record get's selected. Will refresh direct child views
  // which will again refresh their children.
  recordSelected: function(){
    this.fireOnPause("recordSelected", {
      target: this,
      methodName: "doRecordSelected",
      args: []
    }, this.fetchDelay);
  },
  
  doRecordSelected: function(){
    // no change go away
    if (this.viewGrid.getSelectedRecords().length === 1 && this.viewGrid.getSelectedRecord() === this.lastRecordSelected) {
      return;
    }
    
    var tabViewPane = null;
    
    // refresh the tabs
    if (this.childTabSet) {
      for (var i = 0; i < this.childTabSet.tabs.length; i++) {
        tabViewPane = this.childTabSet.tabs[i].pane;
        tabViewPane.parentRecordSelected();
      }
    }
    // and recompute the count:
    this.updateChildCount();
    this.updateTabTitle();
    this.lastRecordSelected = this.viewGrid.getSelectedRecord();
  },
  
  // ** {{{ parentRecordSelected }}} **
  // Is called when a selection change occurs in the parent.
  parentRecordSelected: function(){
  
    // clear all our selections..
    this.viewGrid.deselectAllRecords();
    
    // switch back to the grid or form
    if (this.shouldOpenDefaultEditMode()) {
      if (this.viewGrid.isVisible()) {
        this.switchFormGridVisibility();
      }
    } else if (!this.viewGrid.isVisible()) {
      this.switchFormGridVisibility();
    }
    
    // clear the count from the tabtitle, will be recomputed
    this.updateTabTitle();
    
    // if not visible or the parent also needs to be refreshed
    if (!this.isViewVisible() ||
    (this.parentView && this.parentView.refreshContents)) {
      isc.Log.logDebug('ParentRecordSelected: View not visible ' + this.tabTitle, 'OB');
      // refresh when the view get's shown
      this.refreshContents = true;
    } else {
      isc.Log.logDebug('ParentRecordSelected: View visible ' + this.tabTitle, 'OB');
      var me = this;
      if (this.viewGrid) {
        this.viewGrid.refreshContents();
      }
      if (this.viewForm) {
        this.viewForm.clearValues();
        this.viewForm.clearErrors();
      }
    }
    // enable the following code if we don't automatically select the first
    // record
    if (this.childTabSet) {
      for (var i = 0; i < this.childTabSet.tabs.length; i++) {
        tabViewPane = this.childTabSet.tabs[i].pane;
        tabViewPane.parentRecordSelected();
      }
    }
  },
  
  updateChildCount: function(){
    if (!this.childTabSet) {
      return;
    }
    if (this.viewGrid.getSelectedRecords().length !== 1) {
      return;
    }
    
    var infoByTab = [], tabInfo, childView, data = {}, me = this, callback;
    
    data.parentId = this.viewGrid.getSelectedRecords()[0][OB.Constants.ID];
    
    for (var i = 0; i < this.childTabSet.tabs.length; i++) {
      tabInfo = {};
      childView = this.childTabSet.tabs[i].pane;
      tabInfo.parentProperty = childView.parentProperty;
      tabInfo.tabId = childView.tabId;
      tabInfo.entity = childView.entity;
      if (childView.viewGrid.whereClause) {
        tabInfo.whereClause = childView.viewGrid.whereClause;
      }
      infoByTab.push(tabInfo);
    }
    data.tabs = infoByTab;
    
    // walks through the tabs and sets the title
    callback = function(resp, data, req){
      var tab, tabPane;
      var tabInfos = data.result;
      if (!tabInfos || tabInfos.length !== me.childTabSet.tabs.length) {
        // error, something has changed
        return;
      }
      for (var i = 0; i < me.childTabSet.tabs.length; i++) {
        childView = me.childTabSet.tabs[i].pane;
        tab = me.childTabSet.getTab(i);
        if (childView.tabId === tabInfos[i].tabId) {
          tabPane = me.childTabSet.getTabPane(tab);
          tabPane.recordCount = tabInfos[i].count;
          tabPane.updateTabTitle();
        }
      }
    };
    
    OB.RemoteCallManager.call('org.openbravo.client.application.ChildTabRecordCounterActionHandler', data, {}, callback, null);
  },
  
  updateTabTitle: function(){
  
    // store the original tab title
    if (!this.originalTabTitle) {
      this.originalTabTitle = this.tabTitle;
    }
    
    var identifier, tab;
    // showing the form
    if (!this.viewGrid.isVisible() && this.viewGrid.getSelectedRecord() && this.viewGrid.getSelectedRecord()[OB.Constants.IDENTIFIER]) {
      identifier = this.viewGrid.getSelectedRecord()[OB.Constants.IDENTIFIER];
      if (!this.parentTabSet && this.viewTabId) {
        tab = OB.MainView.TabSet.getTab(this.viewTabId);
        OB.MainView.TabSet.setTabTitle(tab, this.originalTabTitle + ' - ' + identifier);
      } else if (this.parentTabSet && this.tab) {
        this.parentTabSet.setTabTitle(this.tab, this.originalTabTitle + ' - ' + identifier);
      }
    } else if (!this.parentTabSet && this.viewTabId) {
      // the root view
      tab = OB.MainView.TabSet.getTab(this.viewTabId);
      OB.MainView.TabSet.setTabTitle(tab, this.originalTabTitle);
    } else if (this.parentTabSet && this.tab) {
      // the check on this.tab is required for the initialization phase
      // only show a count if there is one parent
      if (this.parentView.viewGrid.getSelectedRecords().length !== 1) {
        this.parentTabSet.setTabTitle(this.tab, this.originalTabTitle);
      } else if (this.recordCount) {
        this.parentTabSet.setTabTitle(this.tab, this.originalTabTitle + ' (' + this.recordCount + ')');
      } else {
        this.parentTabSet.setTabTitle(this.tab, this.originalTabTitle);
      }
    }
  },
  
  isViewVisible: function(){
    return this.parentTabSet.getSelectedTabNumber() ===
    this.parentTabSet.getTabNumber(this.tab);
  },
  
  /* ++++++++++++++++++++ Status Bar ++++++++++++++++++++++++++ */
  
  statusBarCloseIconButtonProperties: {
    view: null,
    imageType: 'center',
    showRollOver: false,
    src: '[SKINIMG]../../org.openbravo.client.application/images/statusbar/ico-x.png',
    action: function(){
      this.view.switchFormGridVisibility();
    }
  },
  
  statusBarPrevIconButtonProperties: {
    view: null,
    imageType: 'center',
    showRollOver: false,
    src: '[SKINIMG]../../org.openbravo.client.application/images/statusbar/sn-previous.gif',
    action: function(){
      var rowNum = this.view.viewGrid.data.indexOf(this.view.viewGrid.getSelectedRecord());
      var newRowNum = rowNum - 1;
      if (newRowNum > -1) {
        var newRecord = this.view.viewGrid.getRecord(newRowNum);
        this.view.viewGrid.scrollRecordToTop(newRowNum);
        this.view.editRecord(newRecord);
        this.view.updateTabTitle();
      }
    }
  },
  
  statusBarNextIconButtonProperties: {
    view: null,
    imageType: 'center',
    showRollOver: false,
    src: '[SKINIMG]../../org.openbravo.client.application/images/statusbar/sn-next.gif',
    action: function(){
      var rowNum = this.view.viewGrid.data.indexOf(this.view.viewGrid.getSelectedRecord());
      var newRowNum = rowNum + 1;
      // if there is data move to it
      if (this.view.viewGrid.data.get(newRowNum)) {
        var newRecord = this.view.viewGrid.getRecord(newRowNum);
        this.view.viewGrid.scrollRecordToTop(newRowNum);
        this.view.editRecord(newRecord);
        this.view.updateTabTitle();
      }
    }
  },
  
  createStatusBar: function(){
    var statusBar = isc.HLayout.create({
      width: '100%',
      height: '30',
      overflow: 'auto'
    });
    var messageBar = isc.HLayout.create({
      width: '*',
      align: 'left',
      overflow: 'visible'
    });
    messageBar.addMember(isc.Label.create({
      contents: 'message'
    }));
    
    var prevButton = isc.ImgButton.create(this.statusBarPrevIconButtonProperties);
    var nextButton = isc.ImgButton.create(this.statusBarNextIconButtonProperties);
    var closeButton = isc.ImgButton.create(this.statusBarCloseIconButtonProperties);
    var buttonBar = isc.HLayout.create({
      width: '100',
      align: 'right',
      overflow: 'visible'
    });
    
    prevButton.view = this;
    nextButton.view = this;
    closeButton.view = this;
    
    buttonBar.addMembers([prevButton, nextButton, closeButton]);
    statusBar.addMembers([messageBar, buttonBar]);
    return statusBar;
  },
  
  /*
   * ++++++++++++++++++++ Parent-Child Tab Handling
   * ++++++++++++++++++++++++++
   */
  convertToPercentageHeights: function(){
    if (!this.members[1]) {
      return;
    }
    var height = this.members[1].getHeight();
    var percentage = ((height / this.getHeight()) * 100);
    // this.members[0].setHeight((100 - percentage) + '%');
    this.members[0].setHeight('*');
    this.members[1].setHeight(percentage + '%');
  },
  
  setTopMaximum: function(){
    this.setHeight('100%');
    if (this.members[1]) {
      this.members[0].setHeight('*');
      this.members[1].setState(isc.OBStandardView.STATE_MIN);
      this.members[1].show();
      this.members[0].show();
      this.convertToPercentageHeights();
    } else {
      this.members[0].setHeight('100%');
      this.members[0].show();
    }
  },
  
  setBottomMaximum: function(){
    this.setHeight('100%');
    if (this.members[1]) {
      this.members[0].hide();
      this.members[0].setHeight(0);
      this.members[1].setHeight('100%');
      this.members[1].show();
    } else {
      this.members[0].setHeight('100%');
      this.members[0].show();
    }
  },
  
  setHalfSplit: function(){
    this.setHeight('100%');
    var i, tab, pane;
    if (this.members[1]) {
      // divide the space between the first and second level
      if (this.members[1].draggedHeight) {
        this.members[0].setHeight('*');
        this.members[1].setHeight(this.members[1].draggedHeight);
        this.members[0].show();
        this.members[1].show();
        this.convertToPercentageHeights();
        this.members[1].setState(isc.OBStandardView.STATE_IN_MID);
      } else {
        // NOTE: noticed that when resizing multiple members in a layout, that it 
        // makes a difference what the order of resizing is, first resize the 
        // one which will be larger, then the one which will be smaller.
        // also do the STATE_IN_MID before resizing
        this.members[1].setState(isc.OBStandardView.STATE_IN_MID);
        this.members[1].setHeight('50%');
        this.members[0].setHeight('50%');
        this.members[1].show();
        this.members[0].show();
      }
    } else {
      this.members[0].setHeight('100%');
      this.members[0].show();
    }
  }
});

// = OBStandardViewTabSet =
// The OBStandardViewTabSet is the tabset used within the standard view to
// display child tabs
isc.ClassFactory.defineClass('OBStandardViewTabSet', isc.TabSet);

isc.OBStandardViewTabSet.addClassProperties({

  TABBARPROPERTIES: {
    dblClickWaiting: false,
    
    canDrag: false,
    dragAppearance: 'none',
    dragStartDistance: 1,
    overflow: 'hidden',
    
    itemClick: function(item, itemNum){
      var me = this;
      me.dblClickWaiting = true;
      isc.Timer.setTimeout(function(){
        // if no double click happened then do the single click
        if (me.dblClickWaiting) {
          me.dblClickWaiting = false;
          me.tabSet.doHandleClick();
        }
      }, OB.Constants.DBL_CLICK_DELAY);
      
    },
    
    itemDoubleClick: function(item, itemNum){
      this.dblClickWaiting = false;
      this.tabSet.doHandleDoubleClick();
    },
    
    dragStop: function(){
      // change the height to percentage based to handle resizing of browser:
      this.tabSet.parentContainer.convertToPercentageHeights();
      return true;
    },
    
    dragStart: function(){
      // -2 to prevent scrollbar
      this.tabSet.maxHeight = this.tabSet.parentContainer.getHeight() - 2;
      this.tabSet.minHeight = (this.getHeight() * 2) + 15;
      return true;
    },
    
    dragMove: function(){
      var offset = (0 - isc.EH.dragOffsetY);
      this.resizeTarget(this.tabSet, true, true, offset, -1 * this.getHeight(), null, true);
      this.tabSet.draggedHeight = this.tabSet.getHeight();
      // if (this.tabSet.getHeight() === this.getHeight()) {
      // // set the parent to top-max
      // this.tabSet.parentTabSet.setState(isc.OBStandardView.STATE_TOP_MAX);
      // this.tabSet.draggedHeight = null;
      // }
      return true;
    }
  }
});

isc.OBStandardViewTabSet.addProperties({
  tabBarProperties: isc.addProperties({}, isc.OBStandardViewTabSet.TABBARPROPERTIES),
  tabBarPosition: 'top',
  width: '100%',
  height: '*',
  overflow: 'hidden',
  
  // get rid of the margin around the content of a pane
  paneMargin: 0,
  paneContainerMargin: 0,
  paneContainerPadding: 0,
  showPaneContainerEdges: false,
  useSimpleTabs: true,
  
  state: null,
  previousState: null,
  
  // keeps track of the previous dragged height, to restore it
  draggedHeight: null,
  
  setDraggable: function(draggable){
    if (draggable) {
      this.tabBar.canDrag = true;
      this.tabBar.cursor = isc.Canvas.HAND;
    } else {
      this.tabBar.canDrag = false;
      this.tabBar.cursor = isc.Canvas.DEFAULT;
    }
  },
  
  doHandleClick: function(){
    if (this.state === isc.OBStandardView.STATE_MIN) {
      // we are minimized, there must be a parent then
      if (this.parentTabSet) {
        this.parentTabSet.setState(isc.OBStandardView.STATE_MID);
      } else {
        this.parentContainer.setHalfSplit();
      }
    } else if (this.state === isc.OBStandardView.STATE_BOTTOM_MAX) {
      this.setState(isc.OBStandardView.STATE_MID);
    }
  },
  
  doHandleDoubleClick: function(){
    if (this.state === isc.OBStandardView.STATE_TOP_MAX) {
      // we are maximized go back to the previous state
      if (this.previousState && this.previousState !== this.state) {
        if (this.previousState === isc.OBStandardView.STATE_IN_MID) {
          this.parentContainer.setHalfSplit();
        } else if (this.previousState === isc.OBStandardView.STATE_MIN) {
          if (this.parentTabSet) {
            this.parentTabSet.setState(isc.OBStandardView.STATE_TOP_MAX);
          } else {
            this.parentContainer.setTopMaximum();
          }
        } else {
          this.setState(this.previousState);
        }
      } else {
        this.setState(isc.OBStandardView.STATE_BOTTOM_MAX);
      }
    } else {
      // first set to IN_MID, to prevent empty tab displays
      this.setState(isc.OBStandardView.STATE_IN_MID);
      this.setState(isc.OBStandardView.STATE_TOP_MAX);
    }
  },
  
  getState: function(){
    return this.state;
  },
  
  setState: function(newState){
  
    // disabled this as sometimes states have
    // to be reset to recompute heights changed automatically
    // if (this.state === newState) {
    // return;
    // }
    
    var tab, i, pane;
    var tmpPreviousState = this.state;
    
    // is corrected below for one state
    this.setDraggable(false);
    
    if (newState === isc.OBStandardView.STATE_TOP_MAX) {
      this.state = newState;
      
      // minimize the ancestors
      if (this.parentTabSet) {
        this.parentTabSet.setState(isc.OBStandardView.STATE_BOTTOM_MAX);
      } else if (this.parentContainer) {
        this.parentContainer.setBottomMaximum();
      }
      
      // note this for loop must be done before the parent's are
      // done otherwise the content is not drawn
      // the top member in each tab is maximized
      // the bottom member in each tab is set to the tabbar height
      for (i = 0; i < this.tabs.length; i++) {
        tab = this.tabs[i];
        this.makeTabVisible(tab);
        pane = this.getTabPane(tab);
        pane.setTopMaximum();
      }
      
    } else if (newState === isc.OBStandardView.STATE_MIN) {
      // the height is set to the height of the tabbar
      this.setHeight(this.tabBar.getHeight());
      for (i = 0; i < this.tabs.length; i++) {
        tab = this.tabs[i];
        this.getTabPane(tab).hide();
      }
      this.state = newState;
    } else if (newState === isc.OBStandardView.STATE_BOTTOM_MAX) {
      // the top part in each layout is set to 0%, and the bottom to max
      this.state = newState;
      if (this.parentTabSet) {
        this.parentTabSet.setState(isc.OBStandardView.STATE_BOTTOM_MAX);
      } else if (this.parentContainer) {
        this.parentContainer.setBottomMaximum();
      }
      for (i = 0; i < this.tabs.length; i++) {
        tab = this.tabs[i];
        this.makeTabVisible(tab);
        pane = this.getTabPane(tab);
        pane.setBottomMaximum();
      }
    } else if (newState === isc.OBStandardView.STATE_IN_MID) {
      this.state = newState;
      this.setDraggable(true);
      // minimize the third level
      for (i = 0; i < this.tabs.length; i++) {
        tab = this.tabs[i];
        pane = this.getTabPane(tab);
        this.makeTabVisible(tab);
        pane.members[0].setHeight('*');
        if (pane.members[1]) {
          pane.members[1].setState(isc.OBStandardView.STATE_MIN);
        }
      }
    } else if (newState === isc.OBStandardView.STATE_MID) {
      if (this.parentTabSet) {
        this.parentTabSet.setState(isc.OBStandardView.STATE_BOTTOM_MAX);
      } else if (this.parentContainer) {
        this.parentContainer.setBottomMaximum();
      }
      // the content of the tabs is split in 2
      this.state = newState;
      for (i = 0; i < this.tabs.length; i++) {
        tab = this.tabs[i];
        pane = this.getTabPane(tab);
        pane.setHalfSplit();
        this.makeTabVisible(tab);
      }
    }
    
    this.previousState = tmpPreviousState;
  },
  
  makeTabVisible: function(tab){
    if (tab === this.getSelectedTab()) {
      pane = this.getTabPane(tab);
      pane.show();
      if (pane.doRefreshContents) {
        pane.doRefreshContents();
      }
      if (pane.members[0]) {
        pane.members[0].show();
      }
      if (pane.members[1]) {
        pane.members[1].show();
      }
      this.selectTab(tab);
    }
  },
  
  tabSelected: function(tabNum, tabPane, ID, tab){
    if (tabPane.refreshContents) {
      tabPane.doRefreshContents();
    }
  },
  
  initWidget: function(){
    this.tabBarProperties.tabSet = this;
    this.Super('initWidget', arguments);
  }
});

// TODO: move this to a central location
isc.Canvas.addProperties({
  // let focuschanged go up to the parent
  focusChanged: function(hasFocus){
    if (this.parentElement && this.parentElement.focusChanged) {
      this.parentElement.focusChanged(hasFocus);
    }
  }
});
