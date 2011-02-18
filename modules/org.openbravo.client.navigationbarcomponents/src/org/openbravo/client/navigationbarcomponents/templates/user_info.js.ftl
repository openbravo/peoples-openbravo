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
