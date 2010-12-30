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

/* jslint */
    OBUserProfile.create({}),
    isc.ImgButton.create(OB.LogoutNavbarComponentStylingProperties, {
      prompt: OB.I18N.getLabel('UINAVBA_EndSession'),

      keyboardShortcutId : 'NavBar_OBLogout',

      draw : function() {
        var me = this;
        var ksAction = function() {
          OB.Utilities.logout();
        };
        OB.KeyboardManager.KS.add(this.keyboardShortcutId, ksAction);
        this.Super("draw", arguments);
        OB.TestRegistry.register('org.openbravo.client.application.navigationbarcomponents.QuitButton', this);
      },
      
      click: function() {
        var handle = this.getHandle();
        OB.Utilities.logout();
      }
    })