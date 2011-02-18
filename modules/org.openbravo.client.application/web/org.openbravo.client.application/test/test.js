/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
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

// should be moved to client.kernel component
// placed here to prevent dependencies of client.kernel on Preferences
OB.Application.startPage = '/openbravo//default/Menu.html';
OB.Application.loginPage = '/openbravo//security/Login_FS.html';

// load the stylesheet used here
isc.Page.loadStyleSheet('[SKIN]../org.openbravo.client.application/navigation_bar_styles.css');

isc.defineClass("OBNavBarSeparator", "Img").addProperties({
    src: '[SKINIMG]../../org.openbravo.client.application/images/navbar-separator.gif',
    height: 11,
    layoutAlign: 'center'
});

/* jslint */
OB.Layout = isc.VLayout.create({
  width: "100%",
  height: "100%"
});
OB.Toolbar = isc.ToolStrip.create({
  width: 1,
  overflow: "visible",
  layoutLeftMargin: 2,
  layoutTopMargin: 2,
  separatorSize: 0,
  height: 28,
  defaultLayoutAlign: "center",
  styleName: "navBarToolStrip",
  
  addMembers: function(members) {
    // encapsulate the members
    var newMembers = new Array();
    for (var i = 0; i < members.length; i++) {
        var newMember = isc.HLayout.create({layoutLeftMargin: 10, layoutRightMargin: 10, styleName: 'navBarComponent', members:[members[i]]}); 
        newMembers[i] = newMember;
    }
    // note the array has to be placed in an array otherwise the newMembers
    // is considered to the argument list
    this.Super("addMembers", [newMembers]);
  }
});
OB.Layout.addMember(OB.Toolbar);

OB.Layout.addMember(isc.LayoutSpacer.create({height: 20}));

OB.MainView = isc.VLayout.create({
  width: "100%",
  height: "100%",
  layoutMargin:5
});
OB.Layout.addMember(OB.MainView);

OB.MainView.TabSet = isc.TabSet.create({
  tabBarPosition: "top",
  width: "100%",
  height: "100%",
  border:"1px solid green", // TODO: Remove border used for testing
  tabSelected: function(tab) {
    return;
  },
  closeClick: function(tab) {
    //Removing from opening views cache
    OB.Layout.ViewManager.views.removeTab(tab.ID);
    this.removeTab(tab);
  }});
OB.MainView.addMember(OB.MainView.TabSet);

OB.Toolbar.addMembers([
/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
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

/* jslint */
isc.OBQuickRun.create({
     title: "",
     prompt: OB.I18N.getLabel('UINAVBA_QUICK_CREATE'),
     src: '[SKINIMG]../../org.openbravo.client.navigationbarcomponents/images/ico-asterisk.gif',
     layoutProperties: {width: 250},
     

     recentPropertyName: 'UINAVBA_RecentCreateList',

     beforeShow : function() {
        var recent = OB.RecentUtilities.getRecentValue(this.recentPropertyName);
        
        var RecentFieldType = function(recentObject) {
            this.linkTitle = recentObject.tabTitle;
            this.value = recentObject.tabTitle;
            this.recentObject = recentObject;
        };
        RecentFieldType.prototype = {
            editorType: "link",
            showTitle: false,
            value: null,
            target: "javascript",
            handleClick: function() {
                OB.RecentUtilities.addRecent('UINAVBA_RecentCreateList', this.recentObject);
                if (this.recentObject.viewId) {
                    OB.Layout.ViewManager.openView(this.recentObject.viewId, this.recentObject);
                } else {
                    OB.Layout.ViewManager.openView("ClassicOBWindow", this.recentObject);
                }
                
                if (isc.OBQuickRun.currentQuickRun) {
                  isc.OBQuickRun.currentQuickRun.doHide();
                }
            }
        };
        
        if (recent && recent.length > 0) {
          var newFields = [];
          var index = 0;
          for (var recentIndex = 0; recentIndex < recent.length; recentIndex++) {
            if (recent[recentIndex]) {
                newFields[index] = new RecentFieldType(recent[recentIndex]);
                index++;
            }
          }
          this.members[0].setFields(newFields);
          this.layout.showMember(this.members[0]);
          this.members[1].getField("value").setValue(null);
        }
     },
     
     members: [ isc.DynamicForm.create({ visibility: 'hidden', numCols: 1}),
     isc.DynamicForm.create({
       initWidget: function() {
        this.Super("initWidget", arguments);
        OB.Utilities.getDataSource('C17951F970E942FD9F3771B7BE91D049', this.getItem("value"));
       },
       autoFocus: true,
       fields: [
       {name: "value",
         selectOnFocus: true,
         autoFetchData : false,
         fetchDelay: 400,
         width: 200,
         titleOrientation: 'top',
         title: OB.I18N.getLabel('UINAVBA_QUICK_CREATE'),
         prompt: OB.I18N.getLabel('UINAVBA_QUICK_CREATE'),
         editorType: "comboBox",
         displayField : OB.Constants.IDENTIFIER,
         valueField : OB.Constants.ID,
         optionDataSource: null, 
         pickListWidth: 200,
         changed: function(form, item, value) {
           if(!value) {
             return;
           }
           if (this.getSelectedRecord()) {
             isc.OBQuickRun.currentQuickRun.doHide();
             var openObject = null; 
             if (this.getSelectedRecord().optionType && this.getSelectedRecord().optionType === 'tab') {
                openObject = {viewId: "ClassicOBWindow", windowId: this.getSelectedRecord().windowId, id: value, tabId: value, command: 'NEW', tabTitle: item.getDisplayValue()};
             } else if (this.getSelectedRecord().optionType && this.getSelectedRecord().optionType === 'process') {
                openObject = {viewId: "PopupClassicOBWindow", id: this.getSelectedRecord().processId, obManualURL: value, command: 'BUTTON' + this.getSelectedRecord().processId, tabTitle: item.getDisplayValue()};
             } else if (this.getSelectedRecord().viewId) {
                openObject = this.getSelectedRecord(); 
             } else {
                openObject = {viewId: "ClassicOBWindow", id: value, obManualURL: value, command: 'NEW', tabTitle: item.getDisplayValue()};
             }
             OB.Layout.ViewManager.openView(openObject.viewId, openObject);
             
             OB.RecentUtilities.addRecent('UINAVBA_RecentCreateList', openObject);
             
             this.setValue(null);
           }
         },
         
         keyPress : function() {
           var key = EventHandler.getKey();
           if (key === "Escape" || key === "Enter") {
             if (isc.OBQuickRun.currentQuickRun) {
               isc.OBQuickRun.currentQuickRun.doHide();
             }
           }
           return true;    
         }         
         }]
         })]
})
,
/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
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

/* jslint */
isc.OBQuickRun.create({
     title: "",
     prompt: OB.I18N.getLabel('UINAVBA_QUICK_LAUNCH'),
     src: '[SKINIMG]../../org.openbravo.client.navigationbarcomponents/images/ico-forward.gif',
     layoutProperties: {width: 250},

     recentPropertyName: 'UINAVBA_RecentLaunchList',

     beforeShow : function() {
        var recent = OB.RecentUtilities.getRecentValue(this.recentPropertyName);
        
        var RecentFieldType = function(recentObject) {
            this.linkTitle = recentObject.tabTitle;
            this.value = recentObject.tabTitle;
            this.recentObject = recentObject;
        };
        RecentFieldType.prototype = {
            editorType: "link",
            showTitle: false,
            value: null,
            target: "javascript",
            handleClick: function() {
                OB.RecentUtilities.addRecent('UINAVBA_RecentLaunchList', this.recentObject);
                if (this.recentObject.viewId) {
                    OB.Layout.ViewManager.openView(this.recentObject.viewId, this.recentObject);
                } else {
                    OB.Layout.ViewManager.openView("ClassicOBWindow", this.recentObject);
                }
                
                if (isc.OBQuickRun.currentQuickRun) {
                  isc.OBQuickRun.currentQuickRun.doHide();
                }
            }
        };
        
        if (recent && recent.length > 0) {
          var newFields = [];
          var index = 0;
          for (var recentIndex = 0; recentIndex < recent.length; recentIndex++) {
            if (recent[recentIndex]) {
                newFields[index] = new RecentFieldType(recent[recentIndex]);
                index++;
            }
          }
          this.members[0].setFields(newFields);
          this.layout.showMember(this.members[0]);
          this.members[1].getField("value").setValue(null);
        }
     },
     
     members: [ isc.DynamicForm.create({ visibility: 'hidden', numCols: 1}),
     isc.DynamicForm.create({
       initWidget: function() {
        this.Super("initWidget", arguments);
        OB.Utilities.getDataSource('99B9CC42FDEA4CA7A4EE35BC49D61E0E', this.getItem("value"));
       },
       autoFocus: true,
       fields: [
       {name: "value",
         selectOnFocus: true,
         autoFetchData : false,
         fetchDelay: 400,
         width: 200,
         titleOrientation: 'top',
         title: OB.I18N.getLabel('UINAVBA_QUICK_LAUNCH'),
         prompt: OB.I18N.getLabel('UINAVBA_QUICK_LAUNCH'),
         editorType: "comboBox",
         displayField : OB.Constants.IDENTIFIER,
         valueField : OB.Constants.ID,
         optionDataSource: null, 
         pickListWidth: 200,
         changed: function(form, item, value) {
           if(!value) {
             return;
           }
           if (this.getSelectedRecord()) {
             isc.OBQuickRun.currentQuickRun.doHide();
             var openObject = null; 
             if (this.getSelectedRecord().optionType && this.getSelectedRecord().optionType === 'tab') {
                openObject = {viewId: "ClassicOBWindow", windowId: this.getSelectedRecord().windowId, id: value, tabId: value, command: 'DEFAULT', tabTitle: item.getDisplayValue()};
             } else if (this.getSelectedRecord().optionType && this.getSelectedRecord().optionType === 'process') {
                openObject = {viewId: "PopupClassicOBWindow", id: this.getSelectedRecord().processId, obManualURL: value, command: 'BUTTON' + this.getSelectedRecord().processId, tabTitle: item.getDisplayValue()};
             } else if (this.getSelectedRecord().viewId) {
                openObject = this.getSelectedRecord(); 
             } else {
                openObject = {viewId: "ClassicOBWindow", id: value, obManualURL: value, command: 'DEFAULT', tabTitle: item.getDisplayValue()};
             }
             OB.Layout.ViewManager.openView(openObject.viewId, openObject);
             
             OB.RecentUtilities.addRecent('UINAVBA_RecentLaunchList', openObject);
             
             this.setValue(null);
           }
         },
         
         keyPress : function() {
           var key = EventHandler.getKey();
           if (key === "Escape" || key === "Enter") {
             if (isc.OBQuickRun.currentQuickRun) {
               isc.OBQuickRun.currentQuickRun.doHide();
             }
           }
           return true;    
         }         
         }]
         })]
})
,
/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
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

/* jslint */
isc.MenuButton.create({
    title: OB.I18N.getLabel('UINAVBA_APPLICATION_MENU'),
    baseStyle: 'navBarButton',
    showMenuButtonImage: false,
    align: "center",
    height: 26,
    iconHeight: 6,
    iconWidth: 10,
    iconSpacing: 10,
    iconAlign: "left",
    iconOrientation: 'right',
    icon: {src: "[SKINIMG]../../org.openbravo.client.navigationbarcomponents/images/ico-green-arrow-down.gif"},

    // put something in the array, otherwise there 
    // are small styling issues
    baseData: [        
            {title: 'Application Dictionary'
    , submenu: [
            {title: 'Module'
        , tabId: 'F53E35A11C564F20BE4082A7B8CFF6B7'
        , windowId: 'D586192D06C14EC182B44CAD34CA4295'
    }
,        
                {title: 'Tables and Columns'
        , tabId: '100'
        , windowId: '100'
    }
,        
                {title: 'Update Audit Trail Infrastructure'
        , manualUrl: '/ad_actionButton/ActionButtonJava_Responser.html', processId: '58763832F5F3485CAD33B8B9FCD6C640'
    }
,        
                {title: 'Dataset'
        , tabId: 'C53967BA96E64FC6B2E4166A7C945168'
        , windowId: '2CC1DC1EDEA2454F987E7F2BBF48A4AE'
    }
,        
                {title: 'Windows, Tabs, and Fields'
        , tabId: '105'
        , windowId: '102'
    }
,        
                {title: 'Reference'
        , tabId: '102'
        , windowId: '101'
    }
,        
                {title: 'Report and Process'
        , tabId: '245'
        , windowId: '165'
    }
,        
                {title: 'Form'
        , tabId: '302'
        , windowId: '187'
    }
,        
                {title: 'Message'
        , tabId: '109'
        , windowId: '104'
    }
,        
                {title: 'Text Interfaces'
        , tabId: '800167'
        , windowId: '800068'
    }
,        
                {title: 'Synchronize Terminology'
        , manualUrl: '/ad_actionButton/ActionButton_Responser.html', processId: '172'
    }
,        
                {title: 'AD Implementation Mapping'
        , tabId: '5B6FF3B9E2B4423EB3EE4B2666D7E918'
        , windowId: '08F09D2A7C774585B014C06C0F44F591'
    }
,        
                {title: 'User Interface'
    , submenu: [
            {title: 'Datasource'
        , tabId: 'EFA7EFCFC6E14827B109D88F236A0B6C'
        , windowId: '67AD3A287B7F4577A1534C8430E9DB2E'
    }
,        
                {title: 'Template'
        , tabId: '0424D6B4F7FF46A6A4B4960F410144B6'
        , windowId: 'CB53174675F84DCEAA13D2BED48F820C'
    }
,        
                {title: 'Navigation Bar Components'
        , tabId: '25EB9212730C4E88B95C75BFCD6F5EBF'
        , windowId: '54AE252A40C34DA285FC48DA94EB3847'
    }
,        
                {title: 'Navigation Bar Component Role Access'
        , tabId: '1111A4FA15DF4D43A6319185B4FF4EB7'
        , windowId: '35F98E0EFD464789A52561E7B7192361'
    }
,        
                {title: 'View Implementation'
        , tabId: 'F648835984F842AF906FA5F97EF6641B'
        , windowId: 'EBA40241D46D4FA4A24E4A09C61994AA'
    }
        
    ]
    }
,        
                {title: 'Setup'
    , submenu: [
            {title: 'Element'
        , tabId: '203'
        , windowId: '151'
    }
,        
                {title: 'Field Category'
        , tabId: '342'
        , windowId: '200'
    }
,        
                {title: 'Auxiliary Input'
        , tabId: '800001'
        , windowId: '800000'
    }
,        
                {title: 'Callout'
        , tabId: '800176'
        , windowId: '800070'
    }
,        
                {title: 'Validation Setup'
        , tabId: '108'
        , windowId: '103'
    }
,        
                {title: 'Month'
        , tabId: '800063'
        , windowId: '800019'
    }
,        
                {title: 'Dimension'
        , tabId: '800064'
        , windowId: '800020'
    }
,        
                {title: 'Extension Points'
        , tabId: '6349699C48D74C72BF982551E927AF1D'
        , windowId: '44C2799AA0644D9BB8C018933BF83F16'
    }
        
    ]
    }
,        
                {title: 'Maintenance'
    , submenu: [
            {title: 'Test'
        , tabId: '152'
        , windowId: '127'
    }
,        
                {title: 'Update Table Identifiers'
        , manualUrl: '/ad_actionButton/ActionButton_Responser.html', processId: '800087'
    }
,        
                {title: 'Recompile DB Objects'
        , manualUrl: '/ad_actionButton/ActionButton_Responser.html', processId: '185'
    }
,        
                {title: 'SQL Query'
        , manualUrl: '/ad_forms/SQLExecutor.html'
    }
        
    ]
    }
,        
                {title: 'Automated Test'
    , submenu: [
            {title: 'Document'
        , tabId: '800066'
        , windowId: '800022'
    }
,        
                {title: 'Command'
        , tabId: '800071'
        , windowId: '800025'
    }
,        
                {title: 'Instructions'
        , tabId: '800069'
        , windowId: '800024'
    }
,        
                {title: 'Role Login'
        , tabId: '800067'
        , windowId: '800023'
    }
,        
                {title: 'Create Document'
        , manualUrl: '/ad_process/CreateTest.html', processId: '800098'
    }
        
    ]
    }
        
    ]
    }
,        
            {title: 'General Setup'
    , submenu: [
            {title: 'Application'
    , submenu: [
            {title: 'Instance Activation'
        , manualUrl: '/ad_forms/InstanceManagement.html'
    }
,        
                {title: 'Module Management'
        , manualUrl: '/ad_forms/ModuleManagement.html'
    }
,        
                {title: 'System Info'
        , tabId: '8652BB9E40664869A5132CEB8A907B9B'
        , windowId: '578DD45CF2BF4D3CA27E4C0EEEF5E5E6'
    }
,        
                {title: 'Language'
        , tabId: '112'
        , windowId: '106'
    }
,        
                {title: 'Import/Export Translations'
        , manualUrl: '/ad_forms/Translation.html'
    }
,        
                {title: 'Application Translation Check'
        , tabId: '445'
        , windowId: '250'
    }
,        
                {title: 'Currency'
        , tabId: '151'
        , windowId: '115'
    }
,        
                {title: 'Conversion Rates'
        , tabId: '198'
        , windowId: '116'
    }
,        
                {title: 'Country Region and City'
        , tabId: '135'
        , windowId: '122'
    }
,        
                {title: 'Location'
        , tabId: '154'
        , windowId: '121'
    }
,        
                {title: 'Preference'
        , tabId: '156'
        , windowId: '129'
    }
,        
                {title: 'Session Preferences'
        , manualUrl: '/ad_forms/ShowSessionPreferences.html'
    }
,        
                {title: 'Session Variables'
        , manualUrl: '/ad_forms/ShowSessionVariables.html'
    }
,        
                {title: 'Application Image'
        , tabId: '391'
        , windowId: '227'
    }
,        
                {title: 'Menu'
        , tabId: '110'
        , windowId: '105'
    }
,        
                {title: 'Workflow'
        , tabId: '148'
        , windowId: '113'
    }
,        
                {title: 'Tree and Node Image'
        , tabId: '243'
        , windowId: '163'
    }
,        
                {title: 'Task'
        , tabId: '150'
        , windowId: '114'
    }
,        
                {title: 'Data File Type'
        , tabId: '800061'
        , windowId: '800015'
    }
,        
                {title: 'Heartbeat Configuration'
        , tabId: '1005400006'
        , windowId: '1005400002'
    }
,        
                {title: 'Alert'
        , tabId: '800265'
        , windowId: '276'
    }
,        
                {title: 'Register'
        , manualUrl: '/ad_forms/Register.html'
    }
,        
                {title: 'Alert Management'
        , manualUrl: '/ad_forms/AlertManagement.html'
    }
        
    ]
    }
,        
                {title: 'Client'
    , submenu: [
            {title: 'Initial Client Setup'
        , manualUrl: '/ad_forms/InitialClientSetup.html'
    }
,        
                {title: 'Delete Client'
        , manualUrl: '/ad_process/DeleteClient.html', processId: '800147'
    }
,        
                {title: 'Client'
        , tabId: '145'
        , windowId: '109'
    }
,        
                {title: 'Export Client'
        , manualUrl: '/ad_actionButton/ActionButtonJava_Responser.html', processId: 'D85D5B5E368A49B1A6293BA4AE15F0F9'
    }
,        
                {title: 'Import Client'
        , manualUrl: '/ad_actionButton/ActionButtonJava_Responser.html', processId: '970EAD9B846648A7AB1F0CCA5058356C'
    }
        
    ]
    }
,        
                {title: 'Security'
    , submenu: [
            {title: 'User'
        , tabId: '118'
        , windowId: '108'
    }
,        
                {title: 'Role'
        , tabId: '119'
        , windowId: '111'
    }
,        
                {title: 'Role Access'
        , tabId: '485'
        , windowId: '268'
    }
,        
                {title: 'Session'
        , tabId: '475'
        , windowId: '264'
    }
,        
                {title: 'Audit Trail'
        , tabId: '3690EB6BA1614375A6F058BBA61B19BC'
        , windowId: 'FEB8679CAA0D47E5978F10E22566FCEA'
    }
        
    ]
    }
,        
                {title: 'Enterprise'
    , submenu: [
            {title: 'Organization Type'
        , tabId: '5DD4A4D36D71411FB3BE2783C3D55473'
        , windowId: '11E11FE8445B4621A9989DD406C1B374'
    }
,        
                {title: 'Organization'
        , tabId: '143'
        , windowId: '110'
    }
        
    ]
    }
,        
                {title: 'Process Scheduling'
    , submenu: [
            {title: 'Process Request'
        , tabId: 'CD573DF1E351485EA2B2DE487DCACA6F'
        , windowId: '48E7EDE7D1104A59B46FC7449D9FB267'
    }
,        
                {title: 'Process Monitor'
        , tabId: '8E5972CF3664486D9D887BDEDA88627D'
        , windowId: 'EF3E837705944F4DBF398D683D36ACE0'
    }
        
    ]
    }
        
    ]
    }
,        
            {title: 'Master Data Management'
    , submenu: [
            {title: 'Send Mail Text'
        , manualUrl: '/ad_process/SendMailText.html', processId: '209'
    }
,        
                {title: 'Business Partner Setup'
    , submenu: [
            {title: 'Title'
        , tabId: '282'
        , windowId: '178'
    }
,        
                {title: 'Areas of Interest'
        , tabId: '438'
        , windowId: '245'
    }
        
    ]
    }
,        
                {title: 'Product Setup'
    , submenu: [
            {title: 'Unit of Measure'
        , tabId: '133'
        , windowId: '120'
    }
        
    ]
    }
,        
                {title: 'Import Data'
    , submenu: [
            {title: 'Import Loader Format'
        , tabId: '315'
        , windowId: '189'
    }
,        
                {title: 'Import File Loader'
        , manualUrl: '/ad_forms/FileImport.html'
    }
,        
                {title: 'Import Products'
        , tabId: '442'
        , windowId: '247'
    }
,        
                {title: 'Import Business Partner'
        , tabId: '441'
        , windowId: '172'
    }
,        
                {title: 'Import Account'
        , tabId: '443'
        , windowId: '248'
    }
,        
                {title: 'Import Orders'
        , tabId: '512'
        , windowId: '281'
    }
,        
                {title: 'Import Budget'
        , tabId: '800208'
        , windowId: '800079'
    }
,        
                {title: 'Import Taxes'
        , tabId: '800260'
        , windowId: '800098'
    }
        
    ]
    }
        
    ]
    }
,        
            {title: 'Procurement Management'
    , submenu: [
            {title: 'Analysis Tools'
    , submenu: [
            {title: 'Vendor Invoice Report'
        , manualUrl: '/ad_reports/ReportInvoiceVendorFilterJR.html', processId: '800180'
    }
        
    ]
    }
        
    ]
    }
,        
            {title: 'Warehouse Management'
    , submenu: [
            {title: 'Transactions'
    , submenu: [
            {title: 'Generate Average Costs'
        , manualUrl: '/ad_actionButton/ActionButton_Responser.html', processId: '800085'
    }
        
    ]
    }
,        
                {title: 'Analysis Tools'
    , submenu: [
            {title: 'Expiration Date Report'
        , manualUrl: '/ad_reports/ReportGuaranteeDateJR.html', processId: '800179'
    }
,        
                {title: 'Production Report'
        , manualUrl: '/ad_reports/ReportProductionJR.html', processId: '800174'
    }
        
    ]
    }
        
    ]
    }
,        
            {title: 'Production Management'
    , submenu: [
            {title: 'Analysis Tools'
    , submenu: [
            {title: 'Daily Work Requirements Report'
        , manualUrl: '/ad_reports/ReportWorkRequirementDaily.html', processId: '800197'
    }
,        
                {title: 'Production Run Status Report'
        , manualUrl: '/ad_reports/ReportProductionRunJR.html', processId: '800173'
    }
        
    ]
    }
        
    ]
    }
,        
            {title: 'Sales Management'
    , submenu: [
            {title: 'Analysis Tools'
    , submenu: [
            {title: 'Sales Order Report'
        , manualUrl: '/ad_reports/ReportSalesOrderFilterJR.html', processId: '800176'
    }
,        
                {title: 'Invoiced Sales Order Report'
        , manualUrl: '/ad_reports/ReportSalesOrderInvoicedJasper.html', processId: '800190'
    }
,        
                {title: 'Orders Awaiting Invoice Report'
        , manualUrl: '/ad_reports/ReportOrderNotInvoiceFilterJR.html', processId: '800178'
    }
,        
                {title: 'Delivered Sales Order Report'
        , manualUrl: '/ad_reports/ReportSalesOrderProvidedJR.html', processId: '800192'
    }
,        
                {title: 'Shipment Report'
        , manualUrl: '/ad_reports/ReportShipmentEditionJR.html', processId: '800189'
    }
,        
                {title: 'Customer Invoice Report'
        , manualUrl: '/ad_reports/ReportInvoiceCustomerFilterJR.html', processId: '800175'
    }
,        
                {title: 'Sales Report by Partner and Product'
        , manualUrl: '/ad_actionButton/ActionButton_Responser.html', processId: '800172'
    }
        
    ]
    }
,        
                {title: 'Setup'
    , submenu: [
            {title: 'Mail Template'
        , tabId: '347'
        , windowId: '204'
    }
        
    ]
    }
        
    ]
    }
,        
            {title: 'Financial Management'
    , submenu: [
            {title: 'Accounting'
    , submenu: [
            {title: 'Transactions'
    , submenu: [
            {title: 'Reset Accounting'
        , manualUrl: '/ad_actionButton/ActionButton_Responser.html', processId: '176'
    }
        
    ]
    }
,        
                {title: 'Analysis Tools'
    , submenu: [
            {title: 'User Defined Accounting Report'
        , manualUrl: '/ad_process/CreateAccountingReport.html', processId: '800144'
    }
        
    ]
    }
,        
                {title: 'Setup'
    , submenu: [
            {title: 'Accounting Dimension'
        , tabId: '520'
        , windowId: '283'
    }
,        
                {title: 'Accounting Process'
        , tabId: 'AB9A0625167C4B169E0DAACBF82B1A8B'
        , windowId: 'A9FD6A5AED3546EC83DB567882646D2E'
    }
,        
                {title: 'Account Combination'
        , tabId: '207'
        , windowId: '153'
    }
,        
                {title: 'G/L Category'
        , tabId: '158'
        , windowId: '131'
    }
,        
                {title: 'Document Type'
        , tabId: '167'
        , windowId: '135'
    }
,        
                {title: 'Document Sequence'
        , tabId: '146'
        , windowId: '112'
    }
,        
                {title: 'ABC Activity'
        , tabId: '249'
        , windowId: '134'
    }
,        
                {title: 'Accounting templates'
        , tabId: '81F3CB82FABC4208BA76DEBB3728A14B'
        , windowId: 'B15DDE526F724EBCB11439BDDB483FA6'
    }
        
    ]
    }
        
    ]
    }
        
    ]
    }
,        
            {title: 'Openbravo ERP'
        , externalUrl: 'http://www.openbravo.com'
    }
        
    ],
    
    showMenu: function() {
        var recent = OB.RecentUtilities.getRecentValue('UINAVBA_MenuRecentList');
        var recentEntries = [];
        if (recent && recent.length > 0) {
          for (var recentIndex = 0; recentIndex < recent.length; recentIndex++) {
            var recentEntry = recent[recentIndex];
            if (recentEntry) {            
                recentEntries[recentIndex] = {title: recentEntry.tabTitle, recentObject: recentEntry};
            }
          }
          recentEntries[recent.length] = {isSeparator: true};
        }
        this.menu.showShadow = true;
        this.menu.shadowDepth = 10;
        this.menu.setData(recentEntries.concat(this.baseData));
        this.menu.markForRedraw();
        this.Super("showMenu", arguments);
    },

    menu: isc.Menu.create({
    
    autoDraw: false,
    autoFitData: "both",
    canHover: true,
        
    itemClick: function(item, colNum) {
        var goto = null;
        if (item.viewId) {
            goto = item;
        } else if (item.recentObject) {
            goto = item.recentObject;
            if (!goto.viewId) {
                goto.viewId = "ClassicOBWindow"; 
            }
        } else if (item.tabId) {
            goto = {viewId: "ClassicOBWindow", windowId: item.windowId, tabId: item.tabId, id: item.tabId, command: "DEFAULT", tabTitle: item.title};
        } else if (item.manualUrl) {
            if (item.processId) {
                goto = {viewId: "PopupClassicOBWindow", obManualURL: item.manualUrl, id: item.processId, command: "BUTTON" + item.processId, tabTitle: item.title};
            } else {
                goto = {viewId: "ClassicOBWindow", obManualURL: item.manualUrl, id: item.manualUrl, command: "DEFAULT", tabTitle: item.title};
            }
        } else if (item.externalUrl) {
            goto = {viewId: "External", obManualURL: item.externalUrl, id: item.externalUrl, command: "DEFAULT", tabTitle: item.title};
        }
        OB.RecentUtilities.addRecent('UINAVBA_MenuRecentList', goto);        
        OB.Layout.ViewManager.openView(goto.viewId, goto);
    }
    })
})

,
/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
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

/* jslint */
isc.Button.create({
  baseStyle: 'navBarButton',
  title: OB.I18N.getLabel('OBAPPEX_HelloWorld'),
  overflow: "visible",
  width: 100,
  layoutAlign: "center",
  showRollOver: false,
  showFocused: false,
  showDown: false,
  click: function() {
    isc.say(OB.I18N.getLabel('OBAPPEX_SayHello', ['Openbravo']));
  }
})

,
/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
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

/* jslint */
    OBAlertIcon.create({}),
    isc.HLayout.create({membersMargin: 5, layoutLeftMargin: 10, layoutRightMargin: 10, height: "100%", defaultLayoutAlign: "center", members:[
    isc.ImgButton.create({
      baseStyle: 'navBarButton',
      prompt: OB.I18N.getLabel('UINAVBA_EndSession'),
      showTitle: false,
      imageType: "normal",
      height: 13,
      width: 13,
      layoutAlign: "center",
      overflow: "visible",
      showRollOver: false,
      showFocused: false,
      showDown: false,
      src: "[SKINIMG]../../org.openbravo.client.navigationbarcomponents/images/ico-close-red.gif",
      click: function() {
        OB.Utilities.logout();
      }
    }),
    OBUserProfile.create({})]})


]);
   
OB.Layout.draw();


