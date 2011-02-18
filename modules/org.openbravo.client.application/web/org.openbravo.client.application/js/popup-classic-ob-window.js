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

// = Popup Classic OB Window =
//
// Opens a popup to show a url. Is used to show a classic OB process window.
//

isc.defineClass("PopupClassicOBWindow", isc.Class).addProperties({
  showsItself: true,
  command : 'DEFAULT',
  appURL : OB.Application.contextUrl + 'security/Menu.html',
  obManualURL: ''});

isc.PopupClassicOBWindow.addMethods({
  show: function() {
    var urlCharacter = '?';
    if (this.appURL.indexOf('?') !== -1) {
        urlCharacter = '&';
    }
    var contentsURL;
    if(this.obManualURL !== '') {
      contentsURL = '../../' + this.obManualURL + '?Command=' + this.command;
//      contentsURL = this.appURL + urlCharacter + 'url=' + this.obManualURL + '&noprefs=true&Command=' + this.command + '&hideMenu=true';
    } else {
      contentsURL = this.appURL + urlCharacter + 'Command=' + this.command + '&noprefs=true&tabId=' + this.tabId + '&hideMenu=true';
    }
    
    OB.Utilities.openProcessPopup(contentsURL);
  }
});
