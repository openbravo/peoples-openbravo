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

// The OBStandardWindow contains the toolbar and the root view.
isc.OBStandardWindow.addProperties({
  toolBarLayout: null,
  view: null,
  
  viewProperties: null,
  
  focusedView: null,
  
  initWidget: function(){
    var standardWindow = this;
    
    this.toolBarLayout = isc.HLayout.create({
      width: '100%',
      height: 1, // is set by its content
      overflow: 'visible'
    });
    
    this.addMember(this.toolBarLayout);
    
    this.viewProperties.standardWindow = this;
    
    this.view = isc.OBStandardView.create(this.viewProperties);
    this.addMember(this.view);
    this.Super('initWidget', arguments);
    
    // is set later after creation
    this.view.tabTitle = this.tabTitle;
  },
  
  show: function(){
    var ret = this.Super('show', arguments);
    this.setFocusInView();
    return ret;
  },
  
  setFocusInView: function(){
    var currentView = this.focusedView || this.view;
    currentView.setViewFocus();
    currentView.setActiveView(true, true);
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
    
    this.setFocusInView();
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
  STATE_MIN: 'Min', // minimized state, the parent has
  // isc.OBStandardView.STATE_TOP_MAX or
  // isc.OBStandardView.STATE_IN_MID
  
  // the inactive state does not show an orange hat on the tab button
  MODE_INACTIVE: 'Inactive'
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
  
  messageBar: null,
  
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
  
  lastFocusedItem: null,
  
  // initially set to true, is set to false after the 
  // first time default edit mode is opened or a new parent 
  // is selected.
  allowDefaultEditMode: true,
  
  initWidget: function(properties){
    var isRootView = !this.parentProperty;
    
    this.messageBar = isc.OBMessageBar.create({
      visibility: 'hidden'
    });
    
    if (isRootView) {
      this.buildStructure();
    }
    
    OB.TestRegistry.register('org.openbravo.client.application.ViewGrid_' + this.tabId, this.viewGrid);
    OB.TestRegistry.register('org.openbravo.client.application.ViewForm_' + this.tabId, this.viewForm);
    
    this.toolBar = isc.OBToolbar.create({
      view: this,
      visibility: 'hidden',
      leftMembers: [isc.OBToolbarIconButton.create(isc.OBToolbar.NEW_BUTTON_PROPERTIES), isc.OBToolbarIconButton.create(isc.OBToolbar.SAVE_BUTTON_PROPERTIES), isc.OBToolbarIconButton.create(isc.OBToolbar.UNDO_BUTTON_PROPERTIES), isc.OBToolbarIconButton.create(isc.OBToolbar.DELETE_BUTTON_PROPERTIES), isc.OBToolbarIconButton.create(isc.OBToolbar.REFRESH_BUTTON_PROPERTIES)],
      rightMembers: [isc.OBToolbarTextButton.create({
        action: 'OB.Utilities.openActionButton(this, {viewId: "OBPopupClassicWindow", obManualURL: "TablesandColumns/Table_Edition.html", processId: "173", id: "173", command: "BUTTONImportTable173", tabTitle: "Testing"});',
        title: 'Button A'
      })]
    });
    
    if (isRootView) {
      this.standardWindow.toolBarLayout.addMember(this.toolBar);
    }
    
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
    var obDsClassname = 'OBDataSource' + this.tabId;
    isc.defineClass(obDsClassname, ds.getClass());
    
    var modifiedDs = isc.addProperties({}, ds, {
      fetchData: function(criteria, callback, requestProperties){
        var newRequestProperties = OB.Utilities._getTabInfoRequestProperties(this.view, requestProperties);
        var additionalPara = {
          _operationType: 'fetch',
          _noActiveFilter: true
        };
        isc.addProperties(newRequestProperties.params, additionalPara);
        return this.Super('fetchData', [criteria, callback, newRequestProperties]);
      },
      
      updateData: function(updatedRecord, callback, requestProperties){
        var newRequestProperties = OB.Utilities._getTabInfoRequestProperties(this.view, requestProperties);
        //standard update is not sent with operationType
        var additionalPara = {
          _operationType: 'update',
          _noActiveFilter: true
        };
        isc.addProperties(newRequestProperties.params, additionalPara);
        this.Super('updateData', [updatedRecord, callback, newRequestProperties]);
      },
      
      addData: function(updatedRecord, callback, requestProperties){
        var newRequestProperties = OB.Utilities._getTabInfoRequestProperties(this.view, requestProperties);
        //standard update is not sent with operationType
        var additionalPara = {
          _operationType: 'add',
          _noActiveFilter: true
        };
        isc.addProperties(newRequestProperties.params, additionalPara);
        this.Super('addData', [updatedRecord, callback, newRequestProperties]);
      },
      
      removeData: function(updatedRecord, callback, requestProperties){
        var newRequestProperties = OB.Utilities._getTabInfoRequestProperties(this.view, requestProperties);
        //standard update is not sent with operationType
        var additionalPara = {
          _operationType: 'remove',
          _noActiveFilter: true
        };
        isc.addProperties(newRequestProperties.params, additionalPara);
        this.Super('removeData', [updatedRecord, callback, newRequestProperties]);
      },
      
      transformResponse: function(dsResponse, dsRequest, jsonData){
        if (!dsRequest.willHandleError) {
          if (!jsonData.response || jsonData.response.status === 'undefined' || jsonData.response.status !== 0) { //0 is success
            if (jsonData.response && jsonData.response.error) {
              var error = jsonData.response.error;
              if (error.type && error.type === 'user') {
                OB.KernelUtilities.handleUserException(error.message, error.params);
              } else {
                OB.KernelUtilities.handleSystemException(error.message);
              }
            } else if (!dsRequest.willHandleError) {
              OB.KernelUtilities.handleSystemException('Error occured');
            } else if (dsResponse.status && dsResponse.status === isc.RPCResponse.STATUS_FAILURE) {
              this.view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, null, jsonData.data);
            } else {
              this.view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, null, OB.I18N.getLabel('OBUIAPP_ErrorInFields'));
            }
          }
        } else {
          // there are some cases where the jsonData is not passed, in case of errors
          // make it available through the response object
          dsResponse.dataObject = jsonData;
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
  },
  
  draw: function(){
    var result = this.Super('draw', arguments);
    if (!this.viewGrid || !this.viewGrid.filterEditor) {
      return result;
    }
    return result;
  },
  
  // ** {{{ createViewStructure }}} **
  // Is to be overridden, is called in initWidget.
  createViewStructure: function(){
  },
  
  // ** {{{ createMainParts }}} **
  // Creates the main layout components of this view.
  createMainParts: function(){
    var isRootView = !this.parentProperty, formContainerLayout;
    var me = this;
    if (this.tabId && this.tabId.length > 0) {
      this.formGridLayout = isc.HLayout.create({
        width: '100%',
        height: '*',
        overflow: 'visible',
        view: this
      });
      
      this.activeBar = isc.HLayout.create({
        height: '100%',
        canFocus: true, // to set active view when it gets clicked
        contents: '&nbsp;',
        width: OB.ActiveBarStyling.width,
        styleName: OB.ActiveBarStyling.inActiveStyleName,
        activeStyleName: OB.ActiveBarStyling.activeStyleName,
        inActiveStyleName: OB.ActiveBarStyling.inActiveStyleName,
        
        setActive: function(active){
          if (active) {
            this.setStyleName(this.activeStyleName);
          } else {
            this.setStyleName(this.inActiveStyleName);
          }
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
      
      this.statusBar = isc.OBStatusBar.create({
        view: this.viewForm.view
      });
      
      this.statusBarFormLayout = isc.VLayout.create({
        width: '100%',
        height: '*',
        visibility: 'hidden',
        overflow: 'hidden'
      });
      
      // to make sure that the form gets the correct scrollbars
      formContainerLayout = isc.VLayout.create({
        width: '100%',
        height: '*',
        overflow: 'auto'
      });
      formContainerLayout.addMember(this.viewForm);
      
      this.statusBarFormLayout.addMember(this.statusBar);
      this.statusBarFormLayout.addMember(formContainerLayout);
      
      this.formGridLayout.addMember(this.statusBarFormLayout);
      
      // wrap the messagebar and the formgridlayout in a VLayout
      var gridFormMessageLayout = isc.VLayout.create({
        height: '100%',
        width: '100%',
        overflow: 'auto'
      });
      gridFormMessageLayout.addMember(this.messageBar);
      gridFormMessageLayout.addMember(this.formGridLayout);
      
      // and place the active bar to the left of the form/grid/messagebar
      var activeGridFormMessageLayout = isc.HLayout.create({
        height: '100%',
        width: '100%',
        overflow: 'hidden'
      });
      
      activeGridFormMessageLayout.addMember(this.activeBar);
      activeGridFormMessageLayout.addMember(gridFormMessageLayout);
      
      this.addMember(activeGridFormMessageLayout);
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
    this.standardWindow.toolBarLayout.addMember(childView.toolBar);
    
    childView.parentView = this;
    childView.parentTabSet = this.childTabSet;
    
    // build the structure of the children
    childView.buildStructure();
    
    var childTabDef = {
      title: childView.tabTitle,
      pane: childView
    };
    
    this.childTabSet.addTab(childTabDef);
    
    childView.tab = this.childTabSet.getTab(this.childTabSet.tabs.length - 1);
    // start inactive
    childView.tab.setCustomState(isc.OBStandardView.MODE_INACTIVE);
    
    OB.TestRegistry.register('org.openbravo.client.application.ChildTab_' + this.tabId + '_' + childView.tabId, childView.tab);
    
  },
  
  setViewFocus: function(){
    var object, functionName;
    
    // clear for a non-focusable item
    if (this.lastFocusedItem && !this.lastFocusedItem.getCanFocus()) {
      this.lastFocusedItem = null;
    }
    
    if (this.lastFocusedItem) {
      object = this.lastFocusedItem;
      functionName = 'focusInItem';
    } else if (this.viewGrid && this.viewGrid.isVisible()) {
      object = this.viewGrid;
      functionName = 'focusInFilterEditor';
    } else if (this.viewForm && this.viewForm.getFocusItem()) {
      object = this.viewForm;
      functionName = 'focus';
    }
    
    isc.Page.setEvent(isc.EH.IDLE, object, isc.Page.FIRE_ONCE, functionName);
  },
  
  setTabButtonState: function(active){
    var tabButton;
    if (this.tab) {
      tabButton = this.tab;
    } else {
      // don't like to use the global window object, but okay..
      tabButton = window[this.standardWindow.viewTabId];
    }
    // enable this code to set the styleclass changes
    if (active) {
      tabButton.setCustomState('');
    } else {
      tabButton.setCustomState(isc.OBStandardView.MODE_INACTIVE);
    }
  },
  
  // called when this view becomes the focused view or looses the focus
  // the parameter is true if it gets the focus and false otherwise
  setActiveView: function(active, ignoreRefreshContents){
    var object, functionName;
    // don't change active when refreshing 
    if (this.refreshContents && !ignoreRefreshContents) {
      return;
    }
    if (active) {
      if (this.standardWindow.focusedView === this) {
        return;
      }
      if (this.standardWindow.focusedView) {
        this.standardWindow.focusedView.setActiveView(false);
      }
      //      console.log("Tab " + this.tabTitle + ' --> receives focus ');
      this.standardWindow.focusedView = this;
      this.toolBar.show();
      this.activeBar.setActive(true);
      this.setViewFocus();
    } else {
      //      console.log("Tab " + this.tabTitle + ' --> looses focus ');
      this.activeBar.setActive(false);
      this.toolBar.hide();
      this.standardWindow.focusedView = null;
    }
    this.setTabButtonState(active);
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
    return this.allowDefaultEditMode && this.defaultEditMode && (!this.parentProperty || this.parentView.viewGrid.getSelectedRecords().length === 1);
  },
  
  // opendefaultedit view for a child view is only called
  // when a new parent is selected, in that case the 
  // edit view should be opened without setting the focus in the form
  openDefaultEditView: function(record){
    if (!this.shouldOpenDefaultEditMode()) {
      return;
    }
    // preventFocus is treated as a boolean later
    var preventFocus = this.parentProperty;
    
    // don't open it again
    this.allowDefaultEditMode = false;
    
    // open form in insert mode
    if (record) {
      this.editRecord(record, preventFocus);
    } else if (!this.viewGrid.data || this.viewGrid.data.getLength() === 0) {
      // purposely not passing a record, to open new mode
      this.editRecord(null, preventFocus);
    } else {
      // edit the first record
      this.editRecord(this.viewGrid.getRecord(0), preventFocus);
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
  editRecord: function(record, preventFocus){
  
    this.messageBar.hide();
    
    if (!record) { //  new case
      this.viewGrid.deselectAllRecords();
      this.viewForm.editNewRecord(preventFocus);
      if (this.viewGrid.isVisible()) {
        this.switchFormGridVisibility();
      }
    } else {
      this.viewForm.editRecord(record, preventFocus);
      if (this.viewGrid.isVisible()) {
        this.switchFormGridVisibility();
      }
      this.viewGrid.doSelectSingleRecord(record);
    }
    
    isc.Page.setEvent(isc.EH.IDLE, this.viewForm, isc.Page.FIRE_ONCE, 'focus');
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
    if (!this.viewForm.newRecordSavedEvent || !this.viewForm.isVisible()) {
      var gridRecord = this.viewGrid.getSelectedRecord();
      this.editRecord(gridRecord);
      this.viewForm.newRecordSavedEvent = false;
    }
    this.recordSelected();
    
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
    this.fireOnPause('recordSelected', {
      target: this,
      methodName: 'doRecordSelected',
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
    
    // allow default edit mode again
    this.allowDefaultEditMode = true;
    
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
  
  getParentId: function(){
    if (!this.parentView || !this.parentView.viewGrid.getSelectedRecord()) {
      return null;
    }
    return this.parentView.viewGrid.getSelectedRecord()[OB.Constants.ID];
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
    
    var props = {};
    this.getContextInfo({}, props, false);
    
    OB.RemoteCallManager.call('org.openbravo.client.application.ChildTabRecordCounterActionHandler', data, props, callback, null);
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
  
  // ++++++++++++++++++++ Button Actions ++++++++++++++++++++++++++
  
  refresh: function(refreshCallback){
    if (this.viewGrid.isVisible()) {
      this.viewGrid.filterData(this.viewGrid.getCriteria(), refreshCallback);
    } else {
      var view = this;
      if (this.viewForm.valuesHaveChanged()) {
        var callback = function(ok){
          if (ok) {
            var criteria = [];
            criteria[OB.Constants.ID] = view.viewGrid.getSelectedRecord()[OB.Constants.ID];
            view.viewForm.fetchData(criteria, refreshCallback);
          }
        };
        isc.ask(OB.I18N.getLabel('OBUIAPP_ConfirmRefresh'), callback);
      } else {
        var criteria = [];
        criteria[OB.Constants.ID] = view.viewGrid.getSelectedRecord()[OB.Constants.ID];
        view.viewForm.fetchData(criteria, refreshCallback);
       }
    }
  },
  
  saveRow: function(){
    this.viewForm.saveRow();
  },
  
  deleteRow: function(){
    var msg, view = this, deleteCount = this.viewGrid.getSelection().length;
    if (deleteCount === 1) {
      msg = OB.I18N.getLabel('OBUIAPP_DeleteConfirmationSingle');
    } else {
      msg = OB.I18N.getLabel('OBUIAPP_DeleteConfirmationMultiple', [this.viewGrid.getSelection().length]);
    }
    
    var callback = function(ok){
      var error, removeCallBack = function(resp, data, req){
        //        console.log(req);
        //        console.log(data);
        //        console.log(resp);
        if (resp.status === isc.RPCResponse.STATUS_SUCCESS) {
          if (!view.viewGrid.isVisible()) {
            view.switchFormGridVisibility();
          }
          view.messageBar.setMessage(isc.OBMessageBar.TYPE_SUCCESS, null, OB.I18N.getLabel('OBUIAPP_DeleteResult', [deleteCount]));
          view.viewGrid.filterData(view.viewGrid.getCriteria());
          view.viewGrid.updateRowCountDisplay();
        } else {
          // get the error message from the dataObject 
          if (resp.dataObject && resp.dataObject.response && resp.dataObject.response.error && resp.dataObject.response.error.message) {
            error = resp.dataObject.response.error;
            if (error.type && error.type === 'user') {
              view.messageBar.setLabel(isc.OBMessageBar.TYPE_ERROR, null, error.message, error.params);
            } else {
              view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, null, OB.I18N.getLabel('OBUIAPP_DeleteResult', [0]));
            }
          }
        }
      };
      
      if (ok) {
        var selection = view.viewGrid.getSelection().duplicate();
        // deselect the current records
        view.viewGrid.deselectAllRecords();
        
        view.viewGrid.removeData(selection[0], removeCallBack, {
          willHandleError: true
        });
      }
    };
    isc.ask(msg, callback);
  },
  
  newRow: function(){
    this.editRecord(null);
  },
  
  undo: function(){
    var view = this, callback;
    if (this.viewForm.valuesHaveChanged()) {
      callback = function(ok){
        if (ok) {
          view.viewForm.undo();
        }
      };
      isc.ask(OB.I18N.getLabel('OBUIAPP_ConfirmUndo', callback), callback);
    }
    throw {
      message: 'Undo should only be enabled if the form has changed.'
    };
  },
  
  // ++++++++++++++++++++ Parent-Child Tab Handling ++++++++++++++++++++++++++
  
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
        this.members[1].setHeight('50%');
        this.members[0].setHeight('50%');
        this.members[1].show();
        this.members[0].show();
        this.members[1].setState(isc.OBStandardView.STATE_IN_MID);
      }
    } else {
      this.members[0].setHeight('100%');
      this.members[0].show();
    }
  },
  
  //++++++++++++++++++ Reading context ++++++++++++++++++++++++++++++
  
  getContextInfo: function(allProperties, sessionProperties, classicMode){
    // if classicmode is undefined then both classic and new props are used
    var classicModeUndefined = (typeof classicMode === 'undefined');
    if (classicModeUndefined) {
      classicMode = true;
    }
    var value, field, record, component;
    // different modes:
    // 1) showing grid with one record selected
    // 2) showing form with aux inputs
    if (this.viewGrid.isVisible()) {
      record = this.viewGrid.getSelectedRecord();
      component = this.viewGrid;
    } else {
      record = this.viewForm.getValues();
      component = this.viewForm;
    }
    
    var properties = this.propertyToColumns;
    
    if (record) {
    
      // add the id of the record itself also if not set
      if (!record[OB.Constants.ID] && this.viewGrid.getSelectedRecord()) {
        // if in edit mode then the grid always has the current record selected
        record[OB.Constants.ID] = this.viewGrid.getSelectedRecord()[OB.Constants.ID];
      }
      
      for (var i = 0; i < properties.length; i++) {
        value = record[properties[i].property];
        field = component.getField(properties[i].property);
        if (typeof value !== 'undefined') {
          if (classicMode || classicModeUndefined) {
            allProperties[properties[i].column] = value;
          }
          if (!classicMode || classicModeUndefined) {
            // surround the property name with @ symbols to make them different
            // from filter criteria and such          
            allProperties['@' + this.entity + '.' + properties[i].property + '@'] = value;
          }
          if (properties[i].sessionProperty) {
            if (classicMode || classicModeUndefined) {
              sessionProperties[properties[i].dbColumn] = value;
            }
            if (!classicMode || classicModeUndefined) {
              sessionProperties['@' + this.entity + '.' + properties[i].property + '@'] = value;
            }
          }
        }
      }
    }
    if (this.viewForm.isVisible()) {
      isc.addProperties(allProperties, this.viewForm.auxInputs);
      isc.addProperties(allProperties, this.viewForm.hiddenInputs);
      isc.addProperties(sessionProperties, this.viewForm.auxInputs);
      isc.addProperties(sessionProperties, this.viewForm.hiddenInputs);
    }
    
    if (this.parentView) {
      this.parentView.getContextInfo(allProperties, sessionProperties, classicMode);
    }
  },
  
  setContextInfo: function(sessionProperties, callbackFunction){
    var allProperties = {};
    if (!sessionProperties) {
      sessionProperties = {};
      this.getContextInfo(allProperties, sessionProperties, true);
    }
    OB.RemoteCallManager.call('org.openbravo.client.application.window.FormInitializationComponent', sessionProperties, {
      MODE: 'SETSESSION',
      TAB_ID: this.viewGrid.view.tabId,
      ROW_ID: this.viewGrid.getSelectedRecord().id
    }, callbackFunction);
  }
});

// = OBStandardViewTabSet =
// The OBStandardViewTabSet is the tabset used within the standard view to
// display child tabs
isc.ClassFactory.defineClass('OBStandardViewTabSet', isc.OBTabSetChild);

isc.OBStandardViewTabSet.addClassProperties({

  TABBARPROPERTIES: {
    dblClickWaiting: false,
    
    canDrag: false,
    dragAppearance: 'none',
    dragStartDistance: 1,
    overflow: 'hidden',
    
    itemClick: function(item, itemNum){
      var me = this, tab = item;
      me.dblClickWaiting = true;
      isc.Timer.setTimeout(function(){
        // if no double click happened then do the single click
        if (me.dblClickWaiting) {
          me.dblClickWaiting = false;
          
          // set the active tab
          if (tab.pane && tab.pane.setActiveView) {
            tab.pane.setActiveView(true);
          }
          
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
  tabBarPosition: 'top',
  width: '100%',
  height: '*',
  overflow: 'hidden',
  
  // get rid of the margin around the content of a pane
  paneMargin: 0,
  paneContainerMargin: 0,
  paneContainerPadding: 0,
  showPaneContainerEdges: false,
  
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
    this.tabBarProperties = isc.addProperties({}, isc.OBStandardViewTabSet.TABBARPROPERTIES);
    this.tabBarProperties.tabSet = this;
    this.Super('initWidget', arguments);
  }
});

// TODO: move this to a central location
isc.Canvas.addProperties({
  // let focuschanged go up to the parent, or handle it here
  focusChanged: function(hasFocus){
    if (this.view && this.view.setActiveView) {
      this.view.setActiveView(true);
      return this.Super('focusChanged', arguments);
    }
    
    if (this.parentElement && this.parentElement.focusChanged) {
      this.parentElement.focusChanged(hasFocus);
    }
  }
});
