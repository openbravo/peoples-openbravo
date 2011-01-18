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
// = OBStandardView =
//
// An OBStandardView represents a single Openbravo tab. An OBStandardView consists
// of three parts:
// 1) a grid an instance of an OBViewGrid (property: viewGrid)
// 2) a form an instance of an OBViewForm (property: viewForm)
// 3) a tab set with child OBStandardView instances (property: childTabSet)
// 
// In addition an OBStandardView has components for a message bar and other visualization.
// 
// A standard view can be opened as a result of a direct link from another window/tab. See
// the description in ob-standard-window for the flow in that case.
//
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
  MODE_INACTIVE: 'Inactive',
  
  UI_PATTERN_READONLY: 'RO',
  UI_PATTERN_SINGLERECORD: 'SR',
  UI_PATTERN_STANDARD: 'ST'  
});

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
  lastRecordSelectedCount: 0,
  
  // ** {{{ refreshContents }}} **
  // Should the contents listgrid/forms be refreshed when the tab
  // gets selected and shown to the user.
  refreshContents: true,
  
  state: isc.OBStandardView.STATE_MID,
  previousState: isc.OBStandardView.STATE_TOP_MAX,
  
  // last item in the filtergrid or the form which had focus
  // when the view is activated it will set focus here
  lastFocusedItem: null,
  
  // initially set to true, is set to false after the 
  // first time default edit mode is opened or a new parent 
  // is selected.
  // note that opening the edit view is done in the viewGrid.dataArrived
  // method
  allowDefaultEditMode: true,
  
  readOnly: false,
  singleRecord: false,
  
  isShowingForm: false,
  
  initWidget: function(properties){
    this.messageBar = isc.OBMessageBar.create({
      visibility: 'hidden'
    });
    
    if (this.isRootView) {
      this.buildStructure();
    }
    
    OB.TestRegistry.register('org.openbravo.client.application.ViewGrid_' + this.tabId, this.viewGrid);
    OB.TestRegistry.register('org.openbravo.client.application.ViewForm_' + this.tabId, this.viewForm);
    
    var rightMemberButtons = [];
    
    if (this.actionToolbarButtons) {
      for (var i = 0; i < this.actionToolbarButtons.length; i++) {
        rightMemberButtons.push(isc.OBToolbarActionButton.create(this.actionToolbarButtons[i]));
      }
    }
    
    this.toolBar = isc.OBToolbar.create({
      view: this,
      visibility: 'hidden',
      leftMembers: [isc.OBToolbarIconButton.create(isc.OBToolbar.NEW_BUTTON_PROPERTIES), isc.OBToolbarIconButton.create(isc.OBToolbar.SAVE_BUTTON_PROPERTIES), isc.OBToolbarIconButton.create(isc.OBToolbar.UNDO_BUTTON_PROPERTIES), isc.OBToolbarIconButton.create(isc.OBToolbar.DELETE_BUTTON_PROPERTIES), isc.OBToolbarIconButton.create(isc.OBToolbar.REFRESH_BUTTON_PROPERTIES)],
      rightMembers: rightMemberButtons
    });

    
    var ret = this.Super('initWidget', arguments);
    
    this.setToolBarButtonState();

    return ret;
  },
  
  buildStructure: function(){
    this.createMainParts();
    this.createViewStructure();
    this.dataSource = OB.Datasource.get(this.dataSourceId, this);
    
    if (this.isRootView) {
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
    // Wrap DataSource with OBDataSource:
    // - to set/reset the progress icons
    // - to handle error cases in the response
    // - to add info to the request
    var obDsClassname = 'OBDataSource' + this.tabId;
    isc.defineClass(obDsClassname, ds.getClass());
    
    var modifiedDs = isc.addProperties({}, ds, {
      view: this,
      
      showProgress: function(editedRecord){
      
        // don't show it, done to quickly
        if (!editedRecord._showProgressAfterDelay) {
          return;
        }
        
        if (editedRecord && editedRecord.editColumnLayout) {
          if (!this.view.isShowingForm) {
            editedRecord.editColumnLayout.toggleProgressIcon(true);
          }
        }
        
        if (this.view.isShowingForm) {
          var btn = this.view.toolBar.getLeftMember(isc.OBToolbar.TYPE_SAVE);
          btn.customState = 'Progress';
          btn.resetBaseStyle();
          btn.markForRedraw();
        }
      },
      
      hideProgress: function(editedRecord){
        editedRecord._showProgressAfterDelay = false;
        if (editedRecord && editedRecord.editColumnLayout) {
          editedRecord.editColumnLayout.toggleProgressIcon(false);
        }
        
        // always remove the progress style here anyway
        var btn = this.view.toolBar.getLeftMember(isc.OBToolbar.TYPE_SAVE);
        btn.customState = '';
        btn.resetBaseStyle();
        btn.markForRedraw();
      },
      
      performDSOperation: function(operationType, data, callback, requestProperties){
        //        requestProperties.showPrompt = false;
        // set the current selected record before the delay
        var currentRecord = this.view.viewGrid.getSelectedRecord();
        if (currentRecord) {
          // only show progress after 200ms delay
          currentRecord._showProgressAfterDelay = true;
          // keep the edited record in the client context
          if (!requestProperties.clientContext) {
            requestProperties.clientContext = {};
          }
          requestProperties.clientContext.progressIndicatorSelectedRecord = currentRecord;
          this.delayCall('showProgress', [requestProperties.clientContext.progressIndicatorSelectedRecord], 200);
        }
        
        var newRequestProperties = OB.Utilities._getTabInfoRequestProperties(this.view, requestProperties);
        //standard update is not sent with operationType
        var additionalPara = {
          _operationType: 'update',
          _noActiveFilter: true
        };
        isc.addProperties(newRequestProperties.params, additionalPara);
        this.Super('performDSOperation', [operationType, data, callback, newRequestProperties]);
      },
      
      transformResponse: function(dsResponse, dsRequest, jsonData){
        if (dsRequest.clientContext && dsRequest.clientContext.progressIndicatorSelectedRecord) {
          this.hideProgress(dsRequest.clientContext.progressIndicatorSelectedRecord);
        }
        if (jsonData) {
          var errorStatus = !jsonData.response || jsonData.response.status === 'undefined' || jsonData.response.status !== isc.RPCResponse.STATUS_SUCCESS;
          if (errorStatus) {
            var handled = this.view.setErrorMessageFromResponse(dsResponse, jsonData, dsRequest);
            
            if (!handled && !dsRequest.willHandleError) {
              OB.KernelUtilities.handleSystemException(error.message);
            }
          } else {
            // there are some cases where the jsonData is not passed, in case of errors
            // make it available through the response object
            dsResponse.dataObject = jsonData;
          }
        }
        return this.Super('transformResponse', arguments);
      }
    });
    
    var myDs = isc[obDsClassname].create(modifiedDs);
    
    this.dataSource = myDs;
    
    if (this.viewGrid) {
      if (this.targetRecordId) {
        this.viewGrid.targetRecordId = this.targetRecordId;
      }
      this.viewGrid.setDataSource(this.dataSource, this.viewGrid.completeFields || this.viewGrid.fields);
      
      // directTabInfo is set when we are in direct link mode, i.e. directly opening
      // a specific tab with a record, the direct link logic will already take care
      // of fetching data
      if (this.isRootView && !this.standardWindow.directTabInfo) {
        this.viewGrid.fetchData();
        this.refreshContents = false;
      }
    }
    if (this.viewForm) {
      // note this call messes up the focus handling also
      this.viewForm.setDataSource(this.dataSource, this.viewForm.fields);
      this.dataSourceSet = true;
    }
  },
  
  // handles different ways by which an error can be passed from the 
  // system, translates this to an object with a type, title and message
  setErrorMessageFromResponse: function(resp, data, req){
    // only handle it once
    if (resp._errorMessageHandled) {
      return true;
    }
    var msg = '', title = null, type = isc.OBMessageBar.TYPE_ERROR, isLabel = false, params = null;
    if (isc.isA.String(data)) {
      msg = data;
    } else if (data && data.response) {
      if (data.response.errors) {
        // give it to the form
        this.viewForm.handleFieldErrors(data.response.errors);
        return true;
      } else if (data.response.error) {
        var error = data.response.error;
        if (error.type && error.type === 'user') {
          isLabel = true;
          msg = error.message;
          params = error.params;
        } else if (error.message) {
          type = error.messageType || type;
          params = error.params;
          // error.messageType can be Error
          type = type.toLowerCase();
          title = error.title || title;
          msg = error.message;
        } else {
          // hope that someone else will handle it
          return false;
        }
      } else {
        // hope that someone else will handle it
        return false;
      }
    } else if (data.data) {
      // try it with data.data
      return this.setErrorMessageFromResponse(resp, data.data, req);
    } else {
      // hope that someone else will handle it
      return false;
    }
    
    req.willHandleError = true;
    resp._errorMessageHandled = true;
    if (msg.indexOf('@') !== -1) {
      index1 = msg.indexOf('@');
      index2 = msg.indexOf('@', index1 + 1);
      if (index2 !== -1) {
        errorCode = msg.substring(index1 + 1, index2);
        this.messageBar.setLabel(type, title, errorCode, params);
      }
    } else if (isLabel) {
      this.messageBar.setLabel(type, title, msg, params);
    } else {
      this.messageBar.setMessage(type, title, msg);
    }
    return true;
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
    var formContainerLayout;
    var me = this;
    if (this.tabId && this.tabId.length > 0) {
      this.formGridLayout = isc.HLayout.create({
        canFocus: true,
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
      
      // NOTE: when changing the layout structure and the scrollbar
      // location for these layouts check if the scrollTo method 
      // in ob-view-form-linked-items is still called on the correct
      // object 
      this.statusBarFormLayout = isc.VLayout.create({
        canFocus: true,
        width: '100%',
        height: '*',
        visibility: 'hidden',
        overflow: 'hidden'
      });
      
      // to make sure that the form gets the correct scrollbars
      formContainerLayout = isc.VLayout.create({
        canFocus: true,
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
        canFocus: true,
        height: '100%',
        width: '100%',
        overflow: 'auto'
      });
      gridFormMessageLayout.addMember(this.messageBar);
      gridFormMessageLayout.addMember(this.formGridLayout);
      
      // and place the active bar to the left of the form/grid/messagebar
      var activeGridFormMessageLayout = isc.HLayout.create({
        canFocus: true,
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
    } else if (this.isRootView) {
      // hide the maximize button if this is the root without
      // children
      this.statusBar.maximizeButton.hide();
    }
  },
  
  // ** {{{ addChildView }}} **
  // The addChildView creates the child tab and sets the pointer back to
  // this
  // parent.
  addChildView: function(childView){
    this.standardWindow.addView(childView);
    
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
  
  setReadOnly: function(readOnly){
    this.readOnly = readOnly;
    this.viewForm.readOnly = readOnly;
  },
  
  setSingleRecord: function(singleRecord){
    this.singleRecord = singleRecord;
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
    } else if (this.viewGrid && !this.isShowingForm) {
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
  
  setAsActiveView: function(ignoreRefreshContents){
    // don't change active when refreshing
    if (this.refreshContents && !ignoreRefreshContents) {
      return;
    }
    this.standardWindow.setActiveView(this);
  },
  
  setActiveViewVisualState: function(state){
    if (state) {
      this.toolBar.show();
      this.activeBar.setActive(true);
      this.setViewFocus();
    } else {
      this.activeBar.setActive(false);
      this.toolBar.hide();
      // note we can not check on viewForm visibility as 
      // the grid and form can both be hidden when changing
      // to another tab, this handles the case that the grid
      // is shown but the underlying form has errors
      if (this.isShowingForm) {
        this.viewForm.autoSave(null, true);
      }
    }
    this.setTabButtonState(state);
  },
  
  doRefreshContents: function(doRefreshWhenVisible){

    // update this one at least before bailing out
    this.updateTabTitle();    
    
    if (!this.isViewVisible()) {
      this.refreshContents = doRefreshWhenVisible;
      return;
    }
    
    if (!this.refreshContents && !doRefreshWhenVisible) {
      return;
    }
    // can be used by others to see that we are refreshing content
    this.refreshContents = true;

    // clear all our selections..
    this.viewGrid.deselectAllRecords();
    
    // hide the messagebar
    this.messageBar.hide();

    // allow default edit mode again
    this.allowDefaultEditMode = true;
    
    if (this.viewForm) {
      this.viewForm.resetForm();
    }
        
    if (this.shouldOpenDefaultEditMode()) {
      this.openDefaultEditView();
    } else if (this.isShowingForm) {
      this.switchFormGridVisibility();
    }
    this.viewGrid.refreshContents();

    this.setToolBarButtonState();    

    // if not visible or the parent also needs to be refreshed
    // enable the following code if we don't automatically select the first
    // record
    if (this.childTabSet) {
      for (var i = 0; i < this.childTabSet.tabs.length; i++) {
        tabViewPane = this.childTabSet.tabs[i].pane;
        // force a refresh, only the visible ones will really 
        // be refreshed
        tabViewPane.doRefreshContents(true);
      }
    }
    // set this at false at the end
    this.refreshContents = false;
  },
  
  shouldOpenDefaultEditMode: function(){
    // can open default edit mode if defaultEditMode is set
    // and this is the root view or a child view with a selected parent.
    var oneOrMoreSelected = this.viewGrid.data && this.viewGrid.data.lengthIsKnown && this.viewGrid.data.lengthIsKnown() &&
    this.viewGrid.data.getLength() >= 1;
    return this.allowDefaultEditMode && oneOrMoreSelected && this.defaultEditMode && (this.isRootView || this.parentView.viewGrid.getSelectedRecords().length === 1);
  },
  
  // opendefaultedit view for a child view is only called
  // when a new parent is selected, in that case the 
  // edit view should be opened without setting the focus in the form
  openDefaultEditView: function(record){
    if (!this.shouldOpenDefaultEditMode()) {
      return;
    }
    // preventFocus is treated as a boolean later
    var preventFocus = !this.isRootView;
    
    // don't open it again
    this.allowDefaultEditMode = false;
    
    // open form in edit mode
    if (record) {
      this.editRecord(record, preventFocus);
    } else if (this.viewGrid.data && this.viewGrid.data.getLength() > 0 && this.viewGrid.data.lengthIsKnown && this.viewGrid.data.lengthIsKnown()) {
      // edit the first record
      this.editRecord(this.viewGrid.getRecord(0), preventFocus);
    }
    // in other cases just show grid
  },
  
  // ** {{{ switchFormGridVisibility }}} **
  // Switch from form to grid view or the other way around
  switchFormGridVisibility: function(){
    if (!this.isShowingForm) {
      this.viewGrid.hide();
      this.statusBarFormLayout.show();
      this.statusBarFormLayout.setHeight('100%');
      // this member should be set after the form is shown
      this.isShowingForm = true;
    } else {
      this.statusBarFormLayout.hide();
      // clear the form    
      this.viewForm.resetForm();
      this.isShowingForm = false;
      
      this.viewGrid.show();
      this.viewGrid.setHeight('100%');
    }
    this.updateTabTitle();
  },
  
  doHandleClick: function(){
    this.setAsActiveView();
    if (!this.childTabSet) {
      return;
    }
    if (this.state !== isc.OBStandardView.STATE_MID) {
      this.setHalfSplit();
      this.previousState = this.state;
      this.state = isc.OBStandardView.STATE_MID;
    }
  },
  
  doHandleDoubleClick: function(){
    this.setAsActiveView();
    var tempState;
    if (!this.childTabSet) {
      return;
    }
    tempState = this.state;
    this.state = this.previousState;
    if (this.previousState === isc.OBStandardView.STATE_BOTTOM_MAX) {
      this.setBottomMaximum();
    } else if (tempState === isc.OBStandardView.STATE_MID && this.previousState === isc.OBStandardView.STATE_MID) {
      this.setTopMaximum();
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
    
    // wait till there is a datasource
    // this also solves focus issues as the datasource
    // set messes up focus
    if (!this.dataSourceSet) {
      this.delayCall('editRecord', arguments, 500);
      return;
    }

    this.messageBar.hide();
    
    if (!this.isShowingForm) {
      this.switchFormGridVisibility();
    }
    
    if (!record) { //  new case
      this.viewGrid.deselectAllRecords();
      this.viewForm.editNewRecord(preventFocus);
    } else {
      this.viewGrid.doSelectSingleRecord(record);
      this.viewForm.editRecord(record, preventFocus);
    }
    
    if (!preventFocus) {
      isc.Page.setEvent(isc.EH.IDLE, this.viewForm, isc.Page.FIRE_ONCE, 'focus');
    }
  },
  
  setMaximizeRestoreButtonState: function(){
    // single view, no maximize or restore
    if (!this.hasChildTabs && this.isRootView) {
      return;
    }
    // different cases:
    var theState = this.state;
    if (this.parentTabSet) {
      theState = this.parentTabSet.state;
    }
    
    if (theState === isc.OBStandardView.STATE_TOP_MAX) {
      this.statusBar.maximizeButton.hide();
      this.statusBar.restoreButton.show(true);
    } else if (theState === isc.OBStandardView.STATE_IN_MID) {
      this.statusBar.maximizeButton.show(true);
      this.statusBar.restoreButton.hide();
    } else if (!this.hasChildTabs) {
      this.statusBar.maximizeButton.hide();
      this.statusBar.restoreButton.show(true);
    } else {
      this.statusBar.maximizeButton.show(true);
      this.statusBar.restoreButton.hide();
    }
  },
  
  maximize: function(){
    if (this.parentTabSet) {
      this.parentTabSet.doHandleDoubleClick();
    } else {
      this.doHandleDoubleClick();
    }
    this.setMaximizeRestoreButtonState();
  },
  
  restore: function(){
    if (this.parentTabSet) {
      this.parentTabSet.doHandleDoubleClick();
    } else {
      this.doHandleDoubleClick();
    }
    this.setMaximizeRestoreButtonState();
  },
  
  // go to a next or previous record, if !next then the previous one is used
  editNextPreviousRecord: function(next){
    var rowNum, newRowNum, newRecord, currentSelectedRecord = this.viewGrid.getSelectedRecord();
    if (!currentSelectedRecord) {
      return;
    }
    rowNum = this.viewGrid.data.indexOf(currentSelectedRecord);
    if (next) {
      newRowNum = rowNum + 1;
    } else {
      newRowNum = rowNum - 1;
    }
    newRecord = this.viewGrid.getRecord(newRowNum);
    if (!newRecord) {
      return;
    }
    this.viewGrid.scrollRecordToTop(newRowNum);
    this.editRecord(newRecord);
  },
  
  // is part of the flow to open all correct tabs when a user goes directly
  // to a specific tab and record, for example by clicking a link in another 
  // window, see the description in ob-standard-window.js
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
        // found it..., check if a record needs to be edited
        if (tabInfos[i].targetRecordId) {
          this.viewGrid.targetRecordId = tabInfos[i].targetRecordId;
        } else {
          // signal open grid
          this.viewGrid.targetOpenGrid = true;
        }
        
        if (this.parentTabSet && this.parentTabSet.getSelectedTab() !== this.tab) {
          this.parentTabSet.selectTab(this.tab);
        } else {
          // make sure that the content gets refreshed
          // refresh and open a child view when all is done
          this.doRefreshContents(true);
        }
        return true;
      }
    }
    return false;
  },
  
  // is part of the flow to open all correct tabs when a user goes directly
  // to a specific tab and record, for example by clicking a link in another 
  // window, see the description in ob-standard-window.js
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
    this.setMaximizeRestoreButtonState();
    
    // show the form with the selected record
    if (!this.isShowingForm) {
      var gridRecord = this.viewGrid.getSelectedRecord();
      if (gridRecord) {
        this.editRecord(gridRecord);
      }
    }
    
    // remove this info
    delete this.standardWindow.directTabInfo;
  },
  
  // ** {{{ recordSelected }}} **
  // Is called when a record get's selected. Will refresh direct child views
  // which will again refresh their children.
  recordSelected: function(){
    // no change go away
    if (!this.hasSelectionStateChanged()) {
      return;
    }
    this.updateLastSelectedState();
        
    this.setToolBarButtonState();

    var tabViewPane = null;
    
    // refresh the tabs
    if (this.childTabSet) {
      for (var i = 0; i < this.childTabSet.tabs.length; i++) {
        tabViewPane = this.childTabSet.tabs[i].pane;
        tabViewPane.doRefreshContents(true);
      }
    }
    // and recompute the count:
    this.updateChildCount();
    this.updateTabTitle();
    
    this.toolBar.refreshCustomButtons();
  },
  
  setToolBarButtonState: function() {
    // validData: this is the root or there is a parent
    var validData = this.isRootView || this.getParentId(), toolBar = this.toolBar, form = this.viewForm, grid = this.viewGrid;
    if (this.isShowingForm) {
      // note on purpose checking form readonly
      toolBar.setLeftMemberDisabled(isc.OBToolbar.TYPE_NEW, form.isSaving || form.readOnly || this.singleRecord || !validData);
      toolBar.setLeftMemberDisabled(isc.OBToolbar.TYPE_SAVE, form.isSaving || form.readOnly || !validData || !form.hasChanged);
      toolBar.setLeftMemberDisabled(isc.OBToolbar.TYPE_UNDO,  form.isSaving || form.readOnly || !validData || !form.hasChanged);
      toolBar.setLeftMemberDisabled(isc.OBToolbar.TYPE_DELETE,  form.isSaving || form.readOnly || this.singleRecord || !validData || form.isNew);
      toolBar.setLeftMemberDisabled(isc.OBToolbar.TYPE_REFRESH, form.isSaving || form.isNew);
    } else {
      toolBar.setLeftMemberDisabled(isc.OBToolbar.TYPE_NEW, this.readOnly || this.singleRecord || !validData);
      // for a grid also the selected number is taken into account
      toolBar.setLeftMemberDisabled(isc.OBToolbar.TYPE_DELETE,  this.readOnly || this.singleRecord || !validData || !grid.getSelectedRecords() || grid.getSelectedRecords().length === 0);
      toolBar.setLeftMemberDisabled(isc.OBToolbar.TYPE_REFRESH, !validData);

      // implement in editable grid
      toolBar.setLeftMemberDisabled(isc.OBToolbar.TYPE_UNDO,  true);
      toolBar.setLeftMemberDisabled(isc.OBToolbar.TYPE_SAVE, true);      
    }

    // and refresh the process toolbar buttons
    this.toolBar.refreshCustomButtons();
  },
  
  hasSelectionStateChanged: function() {
    return (this.viewGrid.getSelectedRecords().length !== this.lastRecordSelectedCount || 
        (this.viewGrid.getSelectedRecord() && this.viewGrid.getSelectedRecord().id !== this.lastRecordSelected.id)) || 
      (this.lastRecordSelected && !this.viewGrid.getSelectedRecord());
  },
  
  updateLastSelectedState: function() {
    this.lastRecordSelectedCount = this.viewGrid.getSelectedRecords().length;
    this.lastRecordSelected = this.viewGrid.getSelectedRecord(); 
  },
    
  getParentId: function(){
    if (!this.parentView || !this.parentView.viewGrid.getSelectedRecords() || this.parentView.viewGrid.getSelectedRecords().length !== 1) {
      return null;
    }
    return this.parentView.viewGrid.getSelectedRecord()[OB.Constants.ID];
  },
  
  updateChildCount: function(){
    if (true) {
      return;
    }
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
    
    var props = this.getContextInfo(true, false);
    
    OB.RemoteCallManager.call('org.openbravo.client.application.ChildTabRecordCounterActionHandler', data, props, callback, null);
  },
  
  updateTabTitle: function(){
    var prefix = '';
    var suffix = '';
    
    if (this.isShowingForm && this.viewForm.isNew) {
      if (isc.Page.isRTL()) {
        suffix = ' *';
      } else {
        prefix = '* ';
      }
    }
    
    // store the original tab title
    if (!this.originalTabTitle) {
      this.originalTabTitle = this.tabTitle;
    }
    
    var identifier, tab;
    // showing the form
    if (this.isShowingForm && this.viewGrid.getSelectedRecord() && this.viewGrid.getSelectedRecord()[OB.Constants.IDENTIFIER]) {
      identifier = this.viewGrid.getSelectedRecord()[OB.Constants.IDENTIFIER];
      if (!this.parentTabSet && this.viewTabId) {
        tab = OB.MainView.TabSet.getTab(this.viewTabId);
        OB.MainView.TabSet.setTabTitle(tab, prefix + this.originalTabTitle + ' - ' + identifier + suffix);
      } else if (this.parentTabSet && this.tab) {
        this.parentTabSet.setTabTitle(this.tab, prefix + this.originalTabTitle + ' - ' + identifier + suffix);
      }
    } else if (!this.parentTabSet && this.viewTabId) {
      // the root view
      tab = OB.MainView.TabSet.getTab(this.viewTabId);
      OB.MainView.TabSet.setTabTitle(tab, prefix + this.originalTabTitle + suffix);
    } else if (this.parentTabSet && this.tab) {
      // the check on this.tab is required for the initialization phase
      // only show a count if there is one parent
      if (this.parentView.viewGrid.getSelectedRecords().length !== 1) {
        this.parentTabSet.setTabTitle(this.tab, prefix + this.originalTabTitle + suffix);
      } else if (this.recordCount) {
        this.parentTabSet.setTabTitle(this.tab, prefix + this.originalTabTitle + ' (' + this.recordCount + ')' + suffix);
      } else {
        this.parentTabSet.setTabTitle(this.tab, prefix + this.originalTabTitle + suffix);
      }
    }
  },
  
  isViewVisible: function(){
    // note this.tab.isVisible is done as the tab is visible earlier than
    // the pane
    return (!this.tab || this.tab.isVisible()) && (!this.parentTabSet || this.parentTabSet.getSelectedTabNumber() ===
              this.parentTabSet.getTabNumber(this.tab));
  },
  
  // ++++++++++++++++++++ Button Actions ++++++++++++++++++++++++++
  
  // make a special refresh:
  // - refresh the current selected record without changing the selection
  // - refresh the parent/grand-parent in the same way without changing the selection
  // - recursive to children: refresh the children, put the children in grid mode and refresh
  
  refresh: function(refreshCallback){
    if (!this.isShowingForm) {
      this.messageBar.hide();
      this.viewGrid.filterData(this.viewGrid.getCriteria(), refreshCallback);
    } else {
      var view = this;
      if (this.viewForm.hasChanged) {
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
        criteria[OB.Constants.ID] = view.viewForm.getValue(OB.Constants.ID);
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
      var i, data, error, removeCallBack = function(resp, data, req){
        if (resp.status === isc.RPCResponse.STATUS_SUCCESS) {
          if (view.isShowingForm) {
            view.switchFormGridVisibility();
            if (resp.clientContext && resp.clientContext.refreshGrid) {
              view.viewGrid.filterData();
            }
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
        
        if (selection.length > 1) {
          var deleteData = {};
          deleteData.entity = view.entity;
          deleteData.ids = [];
          for (i = 0; i < selection.length; i++) {
            deleteData.ids.push(selection[i][OB.Constants.ID]);
          }
          OB.RemoteCallManager.call('org.openbravo.client.application.MultipleDeleteActionHandler', deleteData, {}, removeCallBack, {
            refreshGrid: true
          });
        } else {
          view.viewGrid.removeData(selection[0], removeCallBack, {});
        }
      }
    };
    isc.ask(msg, callback);
  },
  
  newRow: function(){
    var actionObject = {
      target: this,
      method: this.editRecord,
      parameters: null
    };
    this.viewForm.autoSave(actionObject);
  },
  
  undo: function(){
    var view = this, callback;
    if (this.viewForm.hasChanged) {
      callback = function(ok){
        if (ok) {
          view.viewForm.undo();
        }
      };
      isc.ask(OB.I18N.getLabel('OBUIAPP_ConfirmUndo', callback), callback);
      return;
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
    this.state = isc.OBStandardView.STATE_TOP_MAX;
    this.setMaximizeRestoreButtonState();
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
    this.state = isc.OBStandardView.STATE_BOTTOM_MAX;
    this.setMaximizeRestoreButtonState();
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
    this.state = isc.OBStandardView.STATE_MID;
    this.setMaximizeRestoreButtonState();
  },
  
  getCurrentValues: function(){
    if (this.isShowingForm) {
      return this.viewForm.getValues();
    } else {
      return this.viewGrid.getSelectedRecord();
    }
  },
  
  //++++++++++++++++++ Reading context ++++++++++++++++++++++++++++++
  
  getContextInfo: function(onlySessionProperties, classicMode){
    var contextInfo = {}, addProperty;
    // if classicmode is undefined then both classic and new props are used
    var classicModeUndefined = (typeof classicMode === 'undefined');
    if (classicModeUndefined) {
      classicMode = true;
    }
    var value, field, record, component;
    // different modes:
    // 1) showing grid with one record selected
    // 2) showing form with aux inputs
    if (!this.isShowingForm) {
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
        addProperty = properties[i].sessionProperty || !onlySessionProperties;
        if (typeof value !== 'undefined' && addProperty) {
          if (classicMode) {
            contextInfo[properties[i].column] = value;
          } else {
            // surround the property name with @ symbols to make them different
            // from filter criteria and such          
            contextInfo['@' + this.entity + '.' + properties[i].property + '@'] = value;
          }
        }
      }
    }
    if (this.isShowingForm) {
      isc.addProperties(contextInfo, this.viewForm.auxInputs);
      isc.addProperties(contextInfo, this.viewForm.hiddenInputs);
      isc.addProperties(contextInfo, this.viewForm.sessionAttributes);
    }
    
    if (this.parentView) {
      isc.addProperties(contextInfo, this.parentView.getContextInfo(onlySessionProperties, classicMode));
    }
    
    return contextInfo;
  },
  
  setContextInfo: function(sessionProperties, callbackFunction){
    if (!sessionProperties) {
      sessionProperties = this.getContextInfo(true, true);
    }
    OB.RemoteCallManager.call('org.openbravo.client.application.window.FormInitializationComponent', sessionProperties, {
      MODE: 'SETSESSION',
      TAB_ID: this.viewGrid.view.tabId,
      ROW_ID: this.viewGrid.getSelectedRecord().id
    }, callbackFunction);
  },
  
  getTabMessage: function(){
    var callback = function(resp, data, req){
      if (data.type && (data.text || data.title)) {
        req.clientContext.messageBar.setMessage(OBMessageBar[data.type], data.title, data.text);
      }
    };
    
    OB.RemoteCallManager.call('org.openbravo.client.application.window.GetTabMessageActionHandler', {
      tabId: this.tabId
    }, null, callback, this);
  }
});
