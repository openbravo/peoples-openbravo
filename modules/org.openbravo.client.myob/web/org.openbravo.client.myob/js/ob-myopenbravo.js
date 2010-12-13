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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
// = My Openbravo =
//
// Implements the My Openbravo widget.
//

isc.defineClass('OBMyOpenbravo', isc.HLayout);

isc.OBMyOpenbravo.addProperties({

  // ** {{{ OBMyOpenbravo.getBookMarkParams() }}} **
  //
  // Parameters:
  // * {{{}}}: 
  getBookMarkParams: function(){
    var result = {};
    result.myOB = this.myOB;
    // are passed on to the tab
    result.canClose = false;
    return result;
  },
  
  // ** {{{ OBMyOpenbravo.getBookMarkParams() }}} **
  //
  // Used for creating bookmarks in recent quick launch/menu
  isEqualParams: function(params){
    // if the params are for a my ob return true
    if (params.myOB === this.myOB) {
      return true;
    }
    // a non my ob tab
    return false;
  },

  // ** {{{ OBMyOpenbravo.isSameTab() }}} **
  //
  // Returns true if the passed tab info corresponds to this instance,
  // if so then it is not re-opened. Calls isEqualParams.
  //
  // Parameters:
  // * {{{viewId}}}: the id of the view used by the view-manager 
  // * {{{params}}}: the parameters used to create the main tab 
  isSameTab: function(viewId, params){
    return this.isEqualParams(params);
  },

  // makes sure that only one MyOB tab is loaded, see equalParams
  // implementation above
  myOB: true,

  // the widgetinstances already created for the user
  widgets: [],

  // the allowed widget types the user may/can create
  availableWidgetClasses: [],

  // reference back to the portalLayout creating the widget
  portalLayout: null,

  // reference to the left column layout
  leftColumnLayout: null,

  isReloading: false,

  // Admin Mode
  enableAdminMode: false,
  adminModeValueMap: {},
  isValueMapTranslated: false, // Are the names of the levels translated?
  adminMode: false,
  adminLevel: '',
  adminLevelValue: '',

  initWidget: function(args){
    var me = this, i, widgetInstance, recentViewsLayout, addWidgetLayout, adminOtherMyOBLayout, refreshLayout;
    
    // TODO: styling
    // the recent view links copied from the quick launch/quick create
    // and menu
    // TODO: should this be updated every time the user makes a selection
    // in the quick launch/create and menu?
    recentViewsLayout = isc.VLayout.create({});
    recentViewsLayout.addMember(isc.Label.create({
      height: 1,
      overflow: 'visible',
      baseStyle: 'OBMyOBRecentViews',
      contents: OB.I18N.getLabel('OBKMO_RecentViews')
    }));

    recentViewsLinksLayout = isc.VLayout.create({
      height: 1,
      overflow: 'visible'
    });
    recentViewsLayout.addMember(recentViewsLinksLayout);
    this.setRecentList(recentViewsLinksLayout);

    OB.PropertyStore.addListener(function(propertyName, currentValue, newValue){
      if (propertyName === 'UINAVBA_RecentLaunchList') {
        me.setRecentList(recentViewsLinksLayout);
      }
    });

    var actionTitle = isc.VLayout.create({
      height: 1,
      overflow: 'visible'
    });
    actionTitle.addMember(isc.Label.create({
      height: 1,
      overflow: 'visible',
      baseStyle: 'OBMyOBRecentViews',
      contents: OB.I18N.getLabel('OBKMO_Manage_MyOpenbravo')
    }));

    refreshLayout = isc.VLayout.create({
      height: 1,
      overflow: 'visible'
    });
    refreshLayout.addMember(isc.Label.create({
      styleName: 'OBMyOBLeftColumnLink',
      width: '100%',
      height: 1,
      overflow: 'visible',
      contents: OB.I18N.getLabel('OBKMO_WMO_Refresh'),
      action: function(){
        OB.MyOB.reloadWidgets();
      }
    }));

    // the available widget classes the user may/can create
    addWidgetLayout = isc.VLayout.create({
      height: 1,
      overflow: 'visible'
    });
    addWidgetLayout.addMember(isc.Label.create({
      styleName: 'OBMyOBLeftColumnLink',
      width: '100%',
      height: 1,
      overflow: 'visible',
      contents: OB.I18N.getLabel('OBKMO_AddWidget') + ' »',
      addWidgetLayout: addWidgetLayout,
      action: function(){
        var addWidgetDialog;
        if (this.addWidgetLayout.getMembers().length >= 2) {
          return;
        }
        addWidgetDialog = isc.OBMyOBAddWidgetDialog.create({});
        this.addWidgetLayout.addMember(addWidgetDialog);
      }
    }));

    if (this.enableAdminMode) {
      adminOtherMyOBLayout = isc.VLayout.create({});
      adminOtherMyOBLayout.addMember(isc.Label.create({
        styleName: 'OBMyOBLeftColumnLink',
        height: 1,
        width: '100%',
        overflow: 'visible',
        contents: OB.I18N.getLabel('OBKMO_AdminOtherMyOpenbravos') + ' »',
        adminOtherMyOBLayout: adminOtherMyOBLayout,
        action: function(){
          var adminModeDialog;
          if (this.adminOtherMyOBLayout.getMembers().length >= 2) {
            return;
          }
          adminModeDialog = isc.OBMyOBAdminModeDialog.create({});
          adminOtherMyOBLayout = isc.VLayout.create({
            height: 1,
            overflow: 'visible'
          });
          this.adminOtherMyOBLayout.addMember(adminModeDialog);
        }
      }));
    }

    // the left layout containing the recent views and available widgets
    this.leftColumnLayout = isc.VStack.create({
      styleName: 'OBMyOBLeftColumn',
      width: '15%',
      height: 1,
      overflow: 'visible',
      members: [recentViewsLayout, isc.LayoutSpacer.create({
        height: 5
      }), actionTitle, refreshLayout, addWidgetLayout]
    });
    
    if (this.enableAdminMode) {
      this.leftColumnLayout.addMember(adminOtherMyOBLayout);
    }
    
    this.leftColumnLayout.recentViewsLayout = recentViewsLayout;
    this.leftColumnLayout.addWidgetLayout = addWidgetLayout;
    this.leftColumnLayout.adminOtherMyOBLayout = adminOtherMyOBLayout;
    this.leftColumnLayout.refreshLayout = refreshLayout;

    this.addMember(this.leftColumnLayout);

    // the portallayout containing the widgets
    this.portalLayout = isc.PortalLayout.create({
      styleName: 'OBMyOBPortal',
      numColumns: 2,
      width: '85%',
      showColumnMenus: false,
      canResizeColumns: false,
      canResizeRows: false,
      membersMargin: 10,
      columnBorder: 0,
      overflow: 'auto',
      height: '100%',
      sendEvents: false,

      // the PortalColumn is an autochild of the PortalLayout with the
      // child name of 'column', the properties of the PortalColumn
      // can be set like this using the AutoChild concept of SC
      columnProperties: {
        membersMargin: 12,

        // is used to prevent dropping on the left or right 
        // of a widget
        // the PortalRow is an autochild of PortalColumn, the 
        // rowProperties are used to set properties of PortalRow
        rowProperties: {
          isHDrop: function(){
            return false;
          }
        },

        // after dropping create the widget here
        getDropComponent: function(dragTarget, position){
          if (dragTarget.createWidgetInstance) {
            dragTarget.createWidgetInstance(this.colNum, position);
          } else {
            this.Super('getDropComponent', arguments);
          }
        },

        // copied from PortalColumn to set fixed row heights based
        // on the portlets height, see the NOTE line below
        addPortlet: function(portlet, position){
          var eventType = '';

          // offset position to be position within rows
          if (this.showColumnHeader) {
            position += 1;
          }

          var rows = this.getMembers();
          if (rows === null) {
            position = 0;
          } else if (position > rows.length) {
            position = rows.length;
          }

          // Copy explicit user-specified height across to the generated row (and always
          // fill that row)
          // NOTE: this was the original line in the super class
          //        var userHeight = portlet._userHeight;
          var userHeight = portlet.height;
          if (userHeight !== null) {
            portlet.setHeight('100%');
          }

          var dynamicProperties = // canResizeRows attribute derived from parent
          {
            showResizeBar: this.canResizeRows
          };
          if (userHeight !== null) {
            dynamicProperties.height = userHeight;
          }
          var portalRow = this.createAutoChild('row', dynamicProperties);

          this.addMember(portalRow, position);
          portalRow.addMember(portlet);
          
          if (this.portalLayout.sendEvents && OB.MyOB && !OB.MyOB.isReloading) {
            eventType = portlet.dbInstanceId ? 'WIDGET_MOVED' : 'WIDGET_ADDED';
            OB.MyOB.notifyEvent(eventType);
          }
        },

        getTotalHeight: function(){
          var rows = this.getMembers(), i, height = 0, row;
          for (i = 0; i < rows.length; i++) {
            row = rows[i];
            widget = row.getMembers()[0];
            if (widget.getClass().getClassName() === '') {
              continue;
            }
            height += widget.height;
          }
          return height;
        },

        removeAllRows: function(){
          while (this.getMembers().length > 0) {
            this.getMembers()[0].destroy();
          }
        }
      }
    });

    this.addMember(this.portalLayout);
    this.Super('initWidget', args);
    
    // tell each column their index number
    // is used when dragging/dropping 
    this.portalLayout.members[0].colNum = 0;
    this.portalLayout.members[1].colNum = 1;
    this.portalLayout.sendEvents = true;
    OB.MyOB = this;
    
    this.reloadWidgets();
  },

  setRecentList: function(layout){
    var recentList, newRecent, recentIndex = 0, recent, lbl, newIcon, entryLayout, icon;
    // start with a fresh content
    layout.removeMembers(layout.members);
    
    // reads the list of recents and displays them
    recentList = OB.RecentUtilities.getRecentValue('UINAVBA_RecentLaunchList');
    if (recentList && recentList.length > 0) {
    
      handleClickFunction = function(){
        if (this.updateRecent) {
          OB.RecentUtilities.addRecent('UINAVBA_RecentLaunchList', this.recent);
        }
        if (this.recent.viewId) {
          OB.Layout.ViewManager.openView(this.recent.viewId, this.recent);
        } else {
          OB.Layout.ViewManager.openView('OBClassicWindow', this.recent);
        }
      };

      for (; recentIndex < recentList.length; recentIndex++) {
        if (recentList[recentIndex]) {
          recent = recentList[recentIndex];
          icon = null;
          if (recent.icon) {
            if (recent.icon === 'Process') {
              icon = '[SKINIMG]../../org.openbravo.client.application/images/icons/iconProcess.png';
            } else if (recent.icon === 'Report') {
              icon = '[SKINIMG]../../org.openbravo.client.application/images/icons/iconReport.png';
            } else {
              icon = '[SKINIMG]../../org.openbravo.client.application/images/icons/iconAutoForm.png';
            }
          }

          lbl = isc.Label.create({
            contents: recent.tabTitle,
            recent: recent,
            width: '100%',
            updateRecent: true,
            baseStyle: 'OBMyOBRecentViewsEntry',
            handleClick: handleClickFunction,
            iconOrientation: 'left',
            icon: icon
          });

          entryLayout = isc.HLayout.create({
            defaultLayoutAlign: 'center',
            width: '100%'
          });
          entryLayout.addMember(lbl);
          // if a standard window then show the new icon
          if (recent.tabId && !recent.singleRecord && !recent.readOnly) {
            // make a copy
            newRecent = isc.addProperties({}, recent);
            newRecent.command = 'NEW';

            newIcon = isc.ImgButton.create({

              align: 'left',
              showRollOver: true,

              showTitle: false,

              showHover: true,
              prompt: OB.I18N.getLabel('OBKMO_CreateNew'),

              // todo move this to styling
              width: 11,
              height: 11,
              src: '[SKINIMG]../../org.openbravo.client.myob/images/management/iconCreateNew.png',

              recent: newRecent,
              click: handleClickFunction
            });
            entryLayout.addMember(newIcon);
          }
          layout.addMember(entryLayout);
        }
      }
      layout.markForRedraw();
    }
  },

  // ** {{{ OBMyOpenbravo.addWidget(widgetProperties) }}} **
  //
  // Will check if the widget class is already present, if so then
  // it is instantiated. If not then the widgetClass is loaded from the
  // server and when loaded, the instance is created.
  //
  // Parameters:
  // * {{{widgetProperties}}}: properties used to create an instance of the widget
  // like the widgetClassName, height and title 
  addWidget: function(widgetProperties){
    var i;
    // if not there yet load it
    if (!isc.ClassFactory.getClass(widgetProperties.widgetClassName)) {
      var rpcMgr = isc.RPCManager;
      var reqObj = {
        data: isc.JSONEncoder.create({}).encode(widgetProperties),
        callback: this.widgetLoadCallback,
        evalResult: true,
        clientContext: {
          widgetManager: this,
          widgetProperties: widgetProperties
        },
        httpMethod: 'POST',
        useSimpleHttp: true,
        actionURL: OB.Application.contextUrl + 'org.openbravo.client.kernel/OBMYOB_MyOpenbravo/MyOpenbravoWidgetComponent'
      };
      rpcMgr.sendRequest(reqObj);
    } else {
      var localWidgetProperties = isc.addProperties({}, widgetProperties);
      for (i = 0; i < this.availableWidgetClasses.length; i++) {
        if (this.availableWidgetClasses[i].widgetClassName &&
            this.availableWidgetClasses[i].widgetClassName === widgetProperties.widgetClassName) {
          localWidgetProperties.fieldDefinitions = this.availableWidgetClasses[i].fieldDefinitions;
          localWidgetProperties.parameters = isc.addProperties({}, widgetProperties.parameters);
          break;
        }
      }
      widgetInstance = isc.ClassFactory.newInstance(widgetProperties.widgetClassName, localWidgetProperties);
      widgetInstance.widgetManager = this;
      this.portalLayout.addPortlet(widgetInstance, localWidgetProperties.colNum, localWidgetProperties.rowNum);
    }
  },

  // ** {{{ OBMyOpenbravo.widgetLoadCallback(rpcResponse, data, rpcRequest) }}} **
  //
  // Is called when the widget class has been loaded from the backend, method
  // will call the addWidget method again (as now the class is loaded).
  //
  widgetLoadCallback: function(rpcResponse, data, rpcRequest){
    var widgetProperties = rpcRequest.clientContext.widgetProperties;
    var widgetManager = rpcRequest.clientContext.widgetManager;
    
    // something went wrong, give it up
    if (!isc.ClassFactory.getClass(widgetProperties.widgetClassName)) {
      return;
    }
    widgetManager.addWidget(widgetProperties);
  },

  notifyEvent: function(eventType){
    var post;
    if (!eventType) {
      return;
    }

    this.updateWidgetsCache();

    post = {
      'eventType': eventType,
      'widgets': OB.MyOB.widgets,
      'context': {
        'adminMode': OB.MyOB.adminMode
      }
    };

    if (OB.MyOB.adminMode) {
      post.context.availableAtLevel = OB.MyOB.adminLevel.toUpperCase();
      post.context.availableAtLevelValue = OB.MyOB.adminLevelValue;
    }

    OB.RemoteCallManager.call('org.openbravo.client.myob.MyOpenbravoActionHandler', post, {}, function(rpcResponse, data, rpcRequest){
      OB.MyOB.eventResponseHandler(rpcResponse, data, rpcRequest);
    });
  },
  
  eventResponseHandler: function(rpcResponse, data, rpcRequest){
    var i, j, adminLevel, adminLevelValue, publishMessage, levelKey;
    
    if (!data || !data.message || !data.context || !data.widgets) {
      isc.Log.logError('Response does not contain required data for processing');
      return;
    }

    this.updateWidgetsCache(data.eventType, data.widgets);

    if (data.eventType === 'RELOAD_WIDGETS') {

      this.updateClassesCache(data.availableWidgetClasses);

      for (i = 0; i < this.widgets.length; i++) {
        this.addWidget(this.widgets[i]);
      }

      this.isReloading = false;
      this.portalLayout.sendEvents = !this.adminMode;

      this.notifyEvent('WIDGET_MOVED');
    }

    if (data.eventType === 'PUBLISH_CHANGES') {
      if (data.message.type === 'Success') {
        publishMessage = OB.I18N.getLabel('OBKMO_PublishSuccessful');
      } else {
        publishMessage = OB.I18N.getLabel('OBKMO_PublishError');
      }

      if (OB.MyOB.adminModeValueMap.level.system) {
        publishMessage = publishMessage.replace('_level_ _levelvalue_', OB.MyOB.adminModeValueMap.level.system.toUpperCase());
      } else {
        levelKey = data.context.availableAtLevel.toLowerCase();
        levelValueKey = data.context.availableAtLevelValue;
        adminLevel = OB.MyOB.adminModeValueMap.level[levelKey];
        adminLevelValue = OB.MyOB.adminModeValueMap.levelValue[levelKey][levelValueKey];
        publishMessage = publishMessage.replace('_level_', adminLevel);
        publishMessage = publishMessage.replace('_levelvalue_', adminLevelValue);
      }

      OB.MyOB.setUserMode();

      isc.say(publishMessage, {
        title: OB.I18N.getLabel('OBKMO_PublishTitle'),
        isModal: true,
        showModalMask: true
      });
    }

    if (data.message && data.message.type !== 'Success') {
      // isc.warn(data.message.message); Note: Notify the user?
      isc.Log.logWarn(data.message.message);
    }
  },

  updateClassesCache: function(cache) {
    var i, classDef;

    if (!cache || !isc.isAn.Array(cache)) {
      isc.Log.logError('Trying to update classes cache without without a valid argument');
      return;
    }

    this.availableWidgetClasses = [];

    for(i = 0; i < cache.length; i++) {
      classDef = null;
      if (cache[i].indexOf('isc') === 0) {
        // It's a class definition using: isc.defineClass()
        eval('(' + cache[i] + ')');
      } else {
        if (window.JSON) {
          try {
            classDef = JSON.parse(cache[i]);
          } catch (e) {
            isc.Log.logError(e.message);
            continue;
          }
        } else {
          classDef = eval('(' + cache[i] + ')');
        }
        if (classDef) {
          this.availableWidgetClasses.push(classDef);
        }
      }
    }
  },

  // ** {{{ updateWidgetsCache }} **
  //
  // Is called when an widget is added/removed from the layout
  // The widgets cache is refreshed before sending the event notification to the backend
  //
  updateWidgetsCache: function(eventType, responseWidgets){
    var columns = this.portalLayout.getMembers(), i, j, k, col, rows, row, widget, newObj;

    this.widgets = []; // clear cache
    if (eventType === 'RELOAD_WIDGETS' && isc.isAn.Array(responseWidgets)) {
      this.widgets = responseWidgets.duplicate();
      this.sortWidgetsCache();
      return;
    }

    for (i = 0; i < columns.length; i++) {
      col = columns[i];
      rows = col.getMembers();
      for (j = 0; j < rows.length; j++) {
        row = rows[j];
        widget = row.getMembers()[0]; // One widget per row
        if (widget.getClass().getClassName() === 'LayoutSpacer') {
          continue;
        }

        newWidget = {};

        if (eventType === 'WIDGET_ADDED' &&
        isc.isAn.Array(responseWidgets)) {
          for (k = 0; k < responseWidgets.length; k++) {
            if (widget.isSameWidget(responseWidgets[k], true)) {
              widget.dbInstanceId = responseWidgets[k].dbInstanceId;
              break;
            }
          }
        }

        newWidget.ID = widget.ID;
        newWidget.dbInstanceId = widget.dbInstanceId || '';
        newWidget.colNum = i;
        newWidget.rowNum = j;
        newWidget.parameters = widget.parameters;
        newWidget.title = widget.title;
        newWidget.widgetClassName = widget.widgetClassName;
        this.widgets.push(newWidget);
      }
    }
    this.sortWidgetsCache();
  },

  sortWidgetsCache: function(){
    var col0 = 0, col1 = 0, i;

    if (this.widgets.length < 2) {
      return;
    }

    this.widgets.sort(function(a, b){

      if (a.priority < b.priority) {
        return -1;
      } else if (a.priority === b.priority) {
        if (a.colNum < b.colNum) {
          return -1;
        } else if (a.colNum === b.colNum) {
          if (a.rowNum < b.rowNum) {
            return -1;
          } else if (a.rowNum === b.rowNum) {
            return 0;
          } else {
            return 1;
          }
        } else {
          return 1;
        }
      }
      return 1;
    });

    for (i = 0; i < this.widgets.length; i++) {
      if (this.widgets[i].colNum === 0) {
        this.widgets[i].rowNum = col0++;
      } else {
        this.widgets[i].rowNum = col1++;
      }
    }
  },

  getNextWidgetPosition: function(){
    var height0 = this.portalLayout.getMembers()[0].getTotalHeight(), height1 = this.portalLayout.getMembers()[1].getTotalHeight(), pos = {}, rows;
    
    pos.colNum = height0 <= height1 ? 0 : 1;
    rows = this.portalLayout.getMembers();
    
    if (rows) {
      pos.rowNum = this.portalLayout.getMembers()[pos.colNum].getMembers().length;
    } else {
      pos.rowNum = 0;
    }
    
    return pos;
  },

  setAdminMode: function(level, levelValue){
    var leftColumn = this.leftColumnLayout;

    this.adminMode = true;
    this.portalLayout.sendEvents = false;
    this.adminLevel = level.getValue();
    this.adminLevelValue = levelValue.getValue ? levelValue.getValue() : '';

    leftColumn.recentViewsLayout.hide();
    leftColumn.refreshLayout.hide();
    leftColumn.adminOtherMyOBLayout.getMembers()[1].destroy(); // remove DynamicForm
    leftColumn.adminOtherMyOBLayout.hide();
    leftColumn.addMember(isc.OBMyOBPublishChangesDialog.create({
      levelLabel: level.getDisplayValue(),
      levelValueLabel: levelValue.getDisplayValue ? levelValue.getDisplayValue() : ''
    }));
    this.reloadWidgets();
  },

  setUserMode: function(){
    var leftColumn = this.leftColumnLayout, publishDialog;

    this.adminMode = false;
    this.adminLevel = '';
    this.adminLevelValue = '';

    leftColumn.recentViewsLayout.show();
    leftColumn.refreshLayout.show();
    leftColumn.adminOtherMyOBLayout.show();

    publishDialog = leftColumn.getMembers()[leftColumn.getMembers().length - 1];
    if (publishDialog.getClass().getClassName() === 'OBMyOBPublishChangesDialog') {
      publishDialog.destroy();
    }

    this.reloadWidgets();

    this.portalLayout.sendEvents = true;
  },

  reloadWidgets: function(){

    if (this.isReloading) {
      return;
    }

    this.portalLayout.sendEvents = false;
    this.isReloading = true;
    this.portalLayout.getMembers()[0].removeAllRows();
    this.portalLayout.getMembers()[1].removeAllRows();

    this.notifyEvent('RELOAD_WIDGETS');
  }
});

isc.defineClass('OBMyOBDialog', isc.Window).addProperties({
  width: '100%',
  form: null,
  title: '',
  autoSize: true,
  headerControls: ['headerLabel', 'closeButton'],
  canDragResize: false,
  buttonsLayout: null,
  loadingLabel: null,
  actionHandler: 'org.openbravo.client.myob.MyOpenbravoActionHandler',

  initWidget: function(){
    this.buttonsLayout = isc.HStack.create({
      align: 'center',
      overflow: 'visible',
      height: 1,
      width: '100%'
    });

    this.loadingLabel = isc.Label.create({
      height: 20,
      contents: OB.I18N.getLabel('OBUIAPP_Loading')
    });

    this.addItem(this.loadingLabel);

    this.Super('initWidget', arguments);
  },

  closeClick: function(){
    this.destroy();
  }
});

isc.defineClass('OBMyOBAddWidgetDialog', isc.OBMyOBDialog).addProperties({

  initWidget: function(){
    var post = {
      'ID': this.ID,
      'eventType': 'GET_AVAILABLE_WIDGET_CLASSES',
      'widgets': [],
      'context': {
        'adminMode': false
      }
    };

    this.Super('initWidget', arguments);

    OB.RemoteCallManager.call(this.actionHandler, post, {}, function(rpcResponse, data, rpcRequest){
      if (data && data.ID && window[data.ID]) {
        window[data.ID].createDialogContents(rpcResponse, data, rpcRequest);
      }
    });
  },

  createDialogContents: function(rpcResponse, data, rpcRequest){
    var i, widgetClasses, availableWidgetsMap = {};

    if (data && data.availableWidgetClasses) {
      OB.MyOB.updateClassesCache(data.availableWidgetClasses);
      widgetClasses = OB.MyOB.availableWidgetClasses;
      for (i = 0; i < widgetClasses.length; i++) {
        availableWidgetsMap[i] = widgetClasses[i].title;
      }
    }

    if (this.loadingLabel) {
      this.loadingLabel.destroy();
    }

    this.form = isc.DynamicForm.create({
      width: '100%',
      height: '100%',
      numCols: 1,
      titleSuffix: '',
      requiredTitleSuffix: '',
      autoDraw: false,
      titleOrientation: 'top',
      fields: [{
        name: 'widget',
        errorOrientation: 'left',
        cellStyle: 'OBFormField',
        titleStyle: 'OBFormFieldLabel',
        textBoxStyle: 'OBFormFieldSelectInput',
        controlStyle: 'OBFormFieldSelectControl',
        width: '*',
        pickListBaseStyle: 'OBFormFieldPickListCell',
        pickerIconSrc: '[SKIN]/../../org.openbravo.client.application/images/form/comboBoxPicker.png',
        height: 21,
        pickerIconWidth: 21,
        pickListProperties: {
          bodyStyleName: 'OBPickListBody'
        },
        title: OB.I18N.getLabel('OBKMO_WidgetLabel'),
        titleSuffix: '',
        requiredTitleSuffix: '',
        type: 'select',
        valueMap: availableWidgetsMap
      }]
    });

    this.addItem(this.form);

    this.buttonsLayout.addMember(isc.OBFormButton.create({
      autoFit: true,
      title: OB.I18N.getLabel('OBKMO_AddLabel'),
      form: this.form,
      dialog: this,
      click: function(){

        if (!this.form.getItem('widget').getValue()) {
          return;
        }

        var index = parseInt(this.form.getItem('widget').getValue(), 10), widgetInstanceProperties = isc.addProperties({}, OB.MyOB.availableWidgetClasses[index]), position = OB.MyOB.getNextWidgetPosition();
        
        widgetInstanceProperties.colNum = position.colNum;
        widgetInstanceProperties.rowNum = position.rowNum;

        OB.MyOB.addWidget(widgetInstanceProperties);
        this.dialog.destroy();
      }
    }));
    this.addItem(this.buttonsLayout);
  }
});

isc.defineClass('OBMyOBAdminModeDialog', isc.OBMyOBDialog).addProperties({
  initWidget: function(){
    var valueMap = isc.addProperties({}, OB.MyOB.adminModeValueMap.level), prop, formFields = [];

    this.Super('initWidget', arguments);

    if (this.loadingLabel) {
      this.loadingLabel.destroy();
    }

    if (!OB.MyOB.isValueMapTranslated) {
      for (prop in valueMap) {
        if (valueMap.hasOwnProperty(prop)) {
          OB.MyOB.adminModeValueMap.level[prop] = OB.I18N.getLabel(valueMap[prop]);
        }
      }
      OB.MyOB.isValueMapTranslated = true;
    }
    formFields.push({
      name: 'level',
      title: '',
      type: 'select',
      width: '*',
      errorOrientation: 'left',
      cellStyle: 'OBFormField',
      titleStyle: 'OBFormFieldLabel',
      textBoxStyle: 'OBFormFieldSelectInput',
      controlStyle: 'OBFormFieldSelectControl',
      pickListBaseStyle: 'OBFormFieldPickListCell',
      pickerIconSrc: '[SKIN]/../../org.openbravo.client.application/images/form/comboBoxPicker.png',
      height: 21,
      pickerIconWidth: 21,
      pickListProperties: {
        bodyStyleName: 'OBPickListBody'
      },
      valueMap: OB.MyOB.adminModeValueMap.level,
      changed: function(){
        var levelValue = this.form.getField('levelValue');
        if (levelValue) {
          levelValue.setValueMap(OB.MyOB.adminModeValueMap.levelValue[this.getValue()]);
        }
      }
    });

    if (!valueMap.system) {
      formFields.push({
        name: 'levelValue',
        title: '',
        type: 'select',
        errorOrientation: 'left',
        cellStyle: 'OBFormField',
        titleStyle: 'OBFormFieldLabel',
        textBoxStyle: 'OBFormFieldSelectInput',
        controlStyle: 'OBFormFieldSelectControl',
        width: '*',
        pickListBaseStyle: 'OBFormFieldPickListCell',
        pickerIconSrc: '[SKIN]/../../org.openbravo.client.application/images/form/comboBoxPicker.png',
        height: 21,
        pickerIconWidth: 21,
        pickListProperties: {
          bodyStyleName: 'OBPickListBody'
        },
        addUnknownValues: false
      });
    }

    this.form = isc.DynamicForm.create({
      width: '100%',
      height: '100%',
      titleSuffix: '',
      requiredTitleSuffix: '',
      numCols: 1,
      titleOrientation: 'top',
      fields: formFields
    });

    this.addItem(this.form);

    this.buttonsLayout.addMember(isc.OBFormButton.create({
      title: 'Edit', // FIXME
      form: this.form,
      dialog: this,
      click: function(){
        var level = this.form.getField('level'), levelValue = this.form.getField('levelValue');

        if (level.getValue() === 'system') {
          OB.MyOB.setAdminMode(level, {});
        } else if (level.getValue() && levelValue.getValue()) {
          OB.MyOB.setAdminMode(level, levelValue);
        }
      }
    }));
    this.addItem(this.buttonsLayout);
  },

  closeClick: function(){
    OB.MyOB.leftColumnLayout.recentViewsLayout.show();
    OB.MyOB.leftColumnLayout.adminOtherMyOBLayout.show();
    this.Super('closeClick', arguments);
  }
});

isc.defineClass('OBMyOBPublishChangesDialog', isc.OBMyOBDialog).addProperties({
  levelLabel: '',
  levelValueLabel: '',
  
  initWidget: function(){
    var htmlContents = '', label = OB.I18N.getLabel('OBKMO_PublishLabel');
    this.Super('initWidget', arguments);
    
    if (this.loadingLabel) {
      this.loadingLabel.destroy();
    }

    if (OB.MyOB.adminLevel === 'system') {
      label = label.replace('_level_', this.levelLabel.toUpperCase());
      label = label.replace(': _levelvalue_', '');
    } else {
      label = label.replace('_level_', this.levelLabel);
      label = label.replace('_levelvalue_', '<b>' + this.levelValueLabel + '</b>');
    }


    htmlContents = '<p>' + label + '</p>' +
    '<p style=\'color:red\'>' +
    OB.I18N.getLabel('OBKMO_PublishWarning') +
    '</p>';

    this.form = isc.HTMLFlow.create({
      width: '100%',
      styleName: 'OBMyOBPublishLegend',
      contents: htmlContents
    });

    this.addItem(this.form);

    this.buttonsLayout.addMember(isc.OBFormButton.create({
      autoFit: true,
      title: OB.I18N.getLabel('OBKMO_Publish'),
      form: this.form,
      dialog: this,
      click: function(){
        OB.MyOB.notifyEvent('PUBLISH_CHANGES');
      }
    }));

    this.buttonsLayout.addMember(isc.OBFormButton.create({
      title: OB.I18N.getLabel('UINAVBA_Cancel'),
      form: this.form,
      dialog: this,
      click: function(){
        this.dialog.closeClick();
      }
    }));
    this.addItem(this.buttonsLayout);
  },

  closeClick: function(){
    OB.MyOB.setUserMode();
    this.Super('closeClick', arguments);
  }
});
