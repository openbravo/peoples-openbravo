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

// load the stylesheet used here
isc.Page.loadStyleSheet('[SKIN]../org.openbravo.client.navigationbarcomponents/navigation_bar_component_styles.css?' + OB.Application.moduleVersionParameters['0BFC35F28CFF48E2B9A1923A8C2924AA'] );

// test to see if we can show the heartbeat or registration popups (or not)
new function _OB_checkHeartBeatRegistration() {
 var handleReturn = function(response, data, request) {
     if (data.showHeartBeat) {
       OB.Utilities.openProcessPopup(OB.Application.contextUrl + '/ad_forms/Heartbeat.html', true);
     } else if (data.showRegistration) {
       OB.Utilities.openProcessPopup(OB.Application.contextUrl + '/ad_forms/Registration.html', true);
     }
 };

 OB.RemoteCallManager.call('org.openbravo.client.application.HeartBeatPopupActionHandler', {}, {}, handleReturn);

}();

// needed for backward compatibility... to open the registration form
function openRegistration() {
  OB.Utilities.openProcessPopup(OB.Application.contextUrl + '/ad_forms/Registration.html', true);
}