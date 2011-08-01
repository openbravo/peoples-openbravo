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
 * Contributor(s): ___________
 ************************************************************************
 */

// Registers a button to open the form layout manager

// put within a function to hide local vars etc.
(function () {
  
  var personalizationButtonProperties = {
    action: function() {
      var tabTitle, customizeForm;
      if (this.view === this.view.standardWindow.view) {
        tabTitle = this.view.tabTitle;
      } else {
        tabTitle = this.view.standardWindow.tabTitle + ' - ' + this.view.tabTitle;
      }
      
      customizeForm = isc.OBCustomizeFormLayout.create({
        form: this.view.viewForm,
        openedInForm: true,
        tabTitle: tabTitle,
        tabId: this.view.tabId
      });
      customizeForm.doOpen();
    },
    disabled: false,
    buttonType: 'personalization',
    prompt: OB.I18N.getLabel('OBUIAPP_Personalization_Toolbar_Button'),
    updateState: function(){
      this.show();
    },
    keyboardShortcutId: 'ToolBar_Personalization'
  };
  
  OB.ToolbarRegistry.registerButton(personalizationButtonProperties.buttonType, isc.OBToolbarIconButton, personalizationButtonProperties, 310, null);
    
}());
