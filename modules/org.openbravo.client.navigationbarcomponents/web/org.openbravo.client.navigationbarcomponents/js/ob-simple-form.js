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

isc.ClassFactory.defineClass("OBSimpleForm", isc.DynamicForm);

// = OBSimpleForm =
// The OBSimpleForm consists of a set of fields, a data provider which is called to retrieve the data 
// from the backend and 
// user/role information.
isc.OBUserProfile.addProperties({
  
  title: OB.User.userName,
  
  src: "",
  
  iconHeight: 6,
  iconWidth: 10,
  iconSpacing: 10,
  icon: {src: "[SKINIMG]../../org.openbravo.client.navigationbarcomponents/images/ico-arrow-down.gif"},
  iconOrientation: 'right',
  
  prompt: "<b>" + OB.I18N.getLabel('UINAVBA_Role') + "</b>: " + OB.User.roleName + "<br/>" +  
    "<b>" + OB.I18N.getLabel('UINAVBA_Client') + "</b>: " + OB.User.clientName + "<br/>" +
    "<b>" + OB.I18N.getLabel('UINAVBA_Organization') + "</b>: " + OB.User.organizationName,
  hoverWidth: 100,
  
  showTitle: true,
  
  initWidget : function() {
  
    var ComboBoxField = function(props, dsName) {      
      if (props) {
        isc.addProperties(this, props);
      }
    };
    ComboBoxField.prototype = {
        titleOrientation: 'top',
        editorType: "comboBox",
        selectOnFocus: true,
        autoFetchData : false,
        fetchDelay: 400,
        width: 200,
        displayField : OB.Constants.IDENTIFIER,
        valueField : OB.Constants.ID,
        pickListWidth: 200,
        showOptionsFromDataSource: true
    };
    
    var roleField = new ComboBoxField({name: "role", title: OB.I18N.getLabel('UINAVBA_Role')}, "ADRole");
    var clientField = new ComboBoxField({name: "client", title: OB.I18N.getLabel('UINAVBA_Client')}, "ADClient");
    var orgField = new ComboBoxField({name: "organization", title: OB.I18N.getLabel('UINAVBA_Organization')}, "Organization");
    var warehouseField = { 
        titleOrientation: 'top',
        title: 'Warehouse',
        editorType: "comboBox" };
    var languageField = { 
        titleOrientation: 'top',
        title: 'Language',
        editorType: "comboBox" };
    var defaultField = { 
        titleOrientation: 'right',
        title: 'Set as default',
        editorType: "checkbox" };
     
    var roleForm = isc.DynamicForm.create({
      autoFocus: true, 
      overflow: "visible",
      numCols: 1,
      fields: [roleField, clientField, orgField, warehouseField, languageField, defaultField]});

    OB.Utilities.getDataSource("ADRole", roleForm.getField("role"), "optionDataSource");
    OB.Utilities.getDataSource("ADClient", roleForm.getField("client"), "optionDataSource");
    OB.Utilities.getDataSource("Organization", roleForm.getField("organization"), "optionDataSource");
    
    var formLayout = isc.VStack.create({overflow: "visible", height: 1, width: 1});
    formLayout.addMembers(roleForm);

    var buttonLayout = isc.HStack.create({overflow: "visible", height: 1, width: 1});
    buttonLayout.addMembers(isc.Button.create({title:"Save"}));
    buttonLayout.addMembers(isc.Button.create({title:"Cancel", click: isc.OBQuickRun.hide}));
    formLayout.addMembers(buttonLayout);
    
    var currentPasswordField = { 
        titleOrientation: 'top',
        title: "Current Password",
        editorType: "text" };
    var newPasswordField = { 
        titleOrientation: 'top',
        title: "New Password",
        editorType: "text" };
    var confirmPasswordField = { 
        titleOrientation: 'top',
        title: "Confirm Password",
        editorType: "text" };
    
    var passwordForm = isc.DynamicForm.create({
      autoFocus: true, 
      overflow: "visible",
      numCols: 1,
      fields: [currentPasswordField, newPasswordField, confirmPasswordField]});

    var pwdFormLayout = isc.VStack.create({overflow: "visible", height: 1, width: 1});
    pwdFormLayout.addMembers(passwordForm);

    var pwdButtonLayout = isc.HStack.create({overflow: "visible", height: 1, width: 1});
    pwdButtonLayout.addMembers(isc.Button.create({title:"Save"}));
    pwdButtonLayout.addMembers(isc.Button.create({title:"Cancel", click: isc.OBQuickRun.hide}));
    pwdFormLayout.addMembers(pwdButtonLayout);
    
    var tabSet = isc.TabSet.create({
      paneContainerOverflow: "visible",
      overflow: "visible",
      width: 250,
      tabs: [
          {title: "Profile", pane: formLayout, overflow: "visible"},
          {title: "Change Password", pane: pwdFormLayout, overflow: "visible"}
      ]
  });  
  
    this.members = [tabSet];
    this.Super("initWidget", arguments);
  }
});

