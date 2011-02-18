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

isc.ClassFactory.defineClass("OBUserProfile", isc.OBQuickRun);

// = OBUserProfile =
// The OBUserProfile implements a widget which displays the currently logged in
// user. By clicking the widget a form is opened which allows to edit the 
// user/role information and change the password.
isc.OBUserProfile.addProperties({

  layoutProperties: {
  },

  baseStyle: 'navBarButton',

  // ** {{{ title }}} **
  //
  // Contains the user name of the user      
  title: OB.User.userName,
    
  // ** {{{ src }}} **
  //
  // Set to empty to prevent an icon from being displayed on the button.      
  src: "",
  
  // ** {{{ icon settings }}} **
  //
  // The green triangle icon on the right of the button. 
  iconHeight: 6,
  iconWidth: 10,
  iconSpacing: 10,
  icon: {src: "[SKINIMG]../../org.openbravo.client.navigationbarcomponents/images/ico-green-arrow-down.gif"},
  iconOrientation: 'right',
  
  // ** {{{ prompt }}} **
  //
  // Shown on hover, shows some user information.      
  prompt: "<b>" + OB.I18N.getLabel('UINAVBA_Role') + "</b>: " + OB.User.roleName + "<br/>" 
    + "<b>" + OB.I18N.getLabel('UINAVBA_Client') + "</b>: " + OB.User.clientName + "<br/>"
    + "<b>" + OB.I18N.getLabel('UINAVBA_Organization') + "</b>: " + OB.User.organizationName,
  hoverWidth: 200,
  
  showTitle: true,
  
  // correct for the situation that the focus box is over the log out button as well
  // the flyout should start from the left of the log out button
  getLeftPosition: function () {
    return this.parentElement.parentElement.getPageLeft();
  },
  
  //** {{{ doShow() }}} **
  //
  // Is called when the forms are shown.      
  doShow : function() {
    // reset before showing
    this.roleForm.reset();
    this.roleForm.focusInItem("role");
    this.tabSet.selectTab(0);
    this.passwordForm.reset();
    this.Super("doShow", arguments);
  },
  
  getLayoutContainer: function() {
    return this.parentElement.parentElement;
  },
  
  //** {{{ initWidget() }}} **
  //
  // Creates the forms, fields and buttons.      
  initWidget : function() {
  
    OB.Layout.userProfileWidget = this;
    
    // have a pointer to this instance
    var widgetInstance = this;
    
    // create a default form field types
    var ComboBoxField = function(props) {      
      if (props) {
        isc.addProperties(this, props);
      }
    };
    ComboBoxField.prototype = {
    	cellStyle: "navBarComponentFormField",
    	titleStyle: "navBarComponentFormFieldLabel",
    	textBoxStyle: "navBarComponentFormFieldInput",
    	width: "*",
		  pickListBaseStyle: "navBarComponentFormFieldPickListCell",
    	pickListProperties: {
        	bodyStyleName:"navBarComponentPickListBody"},
        titleOrientation: 'top',
        showFocused: true,
        editorType: "select",
        selectOnFocus: true,
        addUnknownValues: false,
        allowEmptyValue: false,
        defaultToFirstOption: true
    };
    
    var roleField = new ComboBoxField({name: "role", title: OB.I18N.getLabel('UINAVBA_Role')});
    var orgField = new ComboBoxField({name: "organization", title: OB.I18N.getLabel('UINAVBA_Organization')});
    var warehouseField = new ComboBoxField({name: "warehouse", title: OB.I18N.getLabel('UINAVBA_Warehouse')}); 
    var languageField = new ComboBoxField({name: "language", title: OB.I18N.getLabel('UINAVBA_Language')}); 
    var defaultField = { 
    	cellStyle: "navBarComponentFormField",
    	titleStyle: "navBarComponentFormFieldLabel",
    	textBoxStyle: "navBarComponentFormFieldLabel",    	
        name: "default",
        showFocused: true,
        titleOrientation: 'right',        
        title: OB.I18N.getLabel('UINAVBA_SetAsDefault'),
        editorType: "checkbox" };

    var clientField = { 
        	cellStyle: "navBarComponentFormField",
        	titleStyle: "navBarComponentFormFieldLabel",
        	textBoxStyle: "navBarComponentFormFieldInput",
            showFocused: true,
            showDisabled: false,
            disabled: true,
            showIcons: false,
            name: "client",
            width: "*",
            titleOrientation: 'top',
            title: OB.I18N.getLabel('UINAVBA_Client'),
            editorType: "TextItem" };
     
    // create the form for the role information
    var roleForm = isc.DynamicForm.create({
      autoFocus: true, 
      overflow: "visible",
      numCols: 1,
      width: "100%",
      titleSuffix: "",
      errorsPreamble: "",
      showInlineErrors: false,
      formActionHandler: "org.openbravo.client.navigationbarcomponents.UserInfoWidgetActionHandler",
      initWidget: function() {
        this.Super("initWidget", arguments);
        OB.RemoteCallManager.call(this.formActionHandler, {}, {"command": "data"}, this.setInitialData);
      },
      
      itemKeyPress: function (item, keyName, characterValue) {
          if (keyName === "Escape") {
            if (isc.OBQuickRun.currentQuickRun) {
              isc.OBQuickRun.currentQuickRun.doHide();
            }
          }

          this.Super("itemKeyPress", arguments);
      },
      
      localFormData: null,
      reset: function() {
    	  // note order is important, first order item then do ValueMaps 
    	  // then do setValues
    	  // this is needed because the select items will reject values
    	  // if the valuemap is not yet set
          roleForm.setValue("role", roleForm.localFormData.initialValues.role);    	  
          roleForm.setValueMaps();
          // note, need to make a copy of the initial values
          // otherwise they are updated when the form values change!
          roleForm.setValues(isc.addProperties({}, roleForm.localFormData.initialValues));
      },
      setInitialData: function(rpcResponse, data, rpcRequest) {
    	// order of these statements is important see comments in reset function
        roleForm.localFormData = data;
        roleForm.setValueMap("language", data.language.valueMap);
        roleForm.setValueMap("role", data.role.valueMap);
        roleForm.setValue("role", data.initialValues.role);
        roleForm.setValue("client", data.initialValues.client);
        roleForm.setValueMaps();
        roleForm.setValues(isc.addProperties({}, data.initialValues));
      },
      // updates the dependent combos
      itemChanged: function(item, newValue) {
    	  this.setValueMaps();
    	  if (item.name === "role") {
    		  if (roleForm.getItem("organization").getClientPickListData().length > 0) {
    			  roleForm.getItem("organization").moveToFirstValue();
    		  }
    		  if (roleForm.getItem("warehouse").getClientPickListData().length > 0) {
    			  roleForm.getItem("warehouse").moveToFirstValue();
    		  }
    	  }
      },
      setValueMaps : function() {
        var roleId = roleForm.getValue("role");
        for (var i = 0; i < roleForm.localFormData.role.roles.length; i++) {
        	var role = roleForm.localFormData.role.roles[i];
        	if (role.id === roleId) {
        		roleForm.setValueMap("warehouse", role.warehouseValueMap);
        		roleForm.setValueMap("organization", role.organizationValueMap);
        		roleForm.setValue("client", role.client);
        	}
        }
      },
      
      // call the server to save the information
      doSave: function () {
        OB.RemoteCallManager.call(this.formActionHandler, this.getValues(), {"command": "save"}, this.doSaveCallback);
      },
      
      // and reload
      doSaveCallback: function(rpcResponse, data, rpcRequest) {
        if (data.result === OB.Constants.SUCCESS) {
          // reload the window to reflect the changed role etc.
          window.location.href = OB.Utilities.getLocationUrlWithoutFragment();
        } else {
          // an error, can not really occur
          // is handled as an exception is returned anyway          
        }
      },
      
      fields: [roleField, clientField, orgField, warehouseField, languageField, defaultField]});
    
    // create the form layout which contains both the form and the buttons
    var formLayout = isc.VStack.create({align: "center", overflow: "visible", height: 1, width: "100%"});
    formLayout.addMembers(roleForm);

    // pointer to the form
    widgetInstance.roleForm = roleForm;
    
    // create the buttons
    var buttonLayout = isc.HStack.create({layoutTopMargin: 10, membersMargin: 10, align: "center", overflow: "visible", height: 1, width: "100%"});
    buttonLayout.addMembers(isc.Button.create({baseStyle: "navBarFormButton", title:OB.I18N.getLabel('UINAVBA_Save'), click: function(){roleForm.doSave()}}));
    buttonLayout.addMembers(isc.Button.create({baseStyle: "navBarFormButton", title:OB.I18N.getLabel('UINAVBA_Cancel'), click: isc.OBQuickRun.hide}));
    formLayout.addMembers(buttonLayout);
    
    // now create the fields for the password form
    var currentPasswordField = {
    	name:"currentPwd",
    	cellStyle: "navBarComponentFormField",
    	titleStyle: "navBarComponentFormFieldLabel",
    	textBoxStyle: "navBarComponentFormFieldInput",
        titleOrientation: 'top',
    	width: "*",
        showFocused: true,
        title: OB.I18N.getLabel('UINAVBA_CurrentPwd'),
        required: true,
        editorType: "PasswordItem" };
    var newPasswordField = { 
        name:"newPwd",
    	cellStyle: "navBarComponentFormField",
    	titleStyle: "navBarComponentFormFieldLabel",
    	textBoxStyle: "navBarComponentFormFieldInput",
        titleOrientation: 'top',
    	width: "*",
        showFocused: true,
        title: OB.I18N.getLabel('UINAVBA_NewPwd'),
        required: true,
        editorType: "PasswordItem" };
    var confirmPasswordField = { 
        name:"confirmPwd",
    	cellStyle: "navBarComponentFormField",
    	titleStyle: "navBarComponentFormFieldLabel",
    	textBoxStyle: "navBarComponentFormFieldInput",
        titleOrientation: 'top',
        showFocused: true,
    	width: "*",
        title: OB.I18N.getLabel('UINAVBA_ConfirmPwd'),
        required: true,
        editorType: "PasswordItem" };

    // create the password form
    var passwordForm = isc.DynamicForm.create({
      autoFocus: true, 
      overflow: "visible",
      width: "100%",
      titleSuffix: "",
      requiredTitleSuffix: "",
      numCols: 1,
      errorOrientation: "right",
      
      itemKeyPress: function (item, keyName, characterValue) {
          if (keyName === "Escape") {
            if (isc.OBQuickRun.currentQuickRun) {
              isc.OBQuickRun.currentQuickRun.doHide();
            }
          }

          this.Super("itemKeyPress", arguments);
      },

      // call the server
      formActionHandler: "org.openbravo.client.navigationbarcomponents.UserInfoWidgetActionHandler",
      doSave: function () {
        OB.RemoteCallManager.call(passwordForm.formActionHandler, passwordForm.getValues(), {"command": "changePwd"}, passwordForm.doSaveCallback);
      },
      
      // the callback displays an info dialog and then hides the form
      doSaveCallback: function(rpcResponse, data, rpcRequest) {
        if (data.result === OB.Constants.SUCCESS) {
          isc.OBQuickRun.hide();
        	isc.say(OB.I18N.getLabel('UINAVBA_PasswordChanged'));
        } else {
        	if (data.messageCode) {
            isc.showPrompt(OB.I18N.getLabel(data.message));
        	}
        	if (data.fields) {
            for (var i = 0; i < data.fields.length; i++) {
          	  var field = data.fields[i];
          	  passwordForm.addFieldErrors(field.field, OB.I18N.getLabel(field.messageCode), true);
            }
        	}
        }
      },
      
      // enable/disable the save button, show an error if the two values are unequal
      itemChanged: function(item, newValue) {
        var currentPwd = this.getValue("currentPwd");
        var newPwd =  this.getValue("newPwd");
        var confirmPwd =  this.getValue("confirmPwd");
        if (OB.Utilities.isNonEmptyString(currentPwd) && OB.Utilities.isNonEmptyString(newPwd) && OB.Utilities.isNonEmptyString(confirmPwd) &&
            OB.Utilities.areEqualWithTrim(newPwd, confirmPwd)) {
          if (pwdSaveButton.isDisabled()) {
            pwdSaveButton.enable();
          }
          passwordForm.clearFieldErrors("confirmPwd", true);
        } else if (pwdSaveButton.isEnabled()) {
          pwdSaveButton.disable();
        }
        if (item.name === "newPwd" || item.name === "confirmPwd") {
          if (!OB.Utilities.areEqualWithTrim(newPwd, confirmPwd)) {
            passwordForm.addFieldErrors("confirmPwd", OB.I18N.getLabel("UINAVBA_UnequalPwd"), true);
          }
        }
         passwordForm.focusInItem(item.name);
      },
      fields: [currentPasswordField, newPasswordField, confirmPasswordField]});

    // create the layout that holds the form and the buttons
    var pwdFormLayout = isc.VStack.create({overflow: "visible", height: 1, width: "100%", align: "center"});
    pwdFormLayout.addMembers(passwordForm);

    widgetInstance.passwordForm = passwordForm;

    var pwdSaveButton = isc.Button.create({baseStyle: "navBarFormButton", title:OB.I18N.getLabel('UINAVBA_Save'), action: passwordForm.doSave, disabled: true});
    var pwdButtonLayout = isc.HStack.create({layoutTopMargin: 10, membersMargin: 10, width: "100%", align: "center", overflow: "visible", height: 1});
    pwdButtonLayout.addMembers(pwdSaveButton);
    pwdButtonLayout.addMembers(isc.Button.create({baseStyle: "navBarFormButton", title:OB.I18N.getLabel('UINAVBA_Cancel'), click: isc.OBQuickRun.hide}));
    pwdFormLayout.addMembers(pwdButtonLayout);
    
    // and create the tabset
    var tabSet = isc.TabSet.create({
      paneContainerOverflow: "visible",
      overflow: "visible",
      useSimpleTabs: true,
      simpleTabBaseStyle: "navBarComponentFormTabButton",
      paneContainerClassName: "navBarComponentFormTabSetContainer",
      width: 250,
      tabs: [
          {title: "Profile", pane: formLayout, overflow: "visible"},
          {title: "Change Password", pane: pwdFormLayout, overflow: "visible"}
      ]
  });
    widgetInstance.tabSet = tabSet;

    this.members = [tabSet];
    this.Super("initWidget", arguments);
  }
});

