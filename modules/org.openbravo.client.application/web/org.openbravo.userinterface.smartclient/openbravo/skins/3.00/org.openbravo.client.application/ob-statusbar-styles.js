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
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/


isc.OBStatusBar.addProperties({
  styleName: "OBStatusBar",
  width: '100%',
  height: 30,
  leaveScrollbarGap: false,
  overflow: 'hidden',

  newIconDefaults: {
    showHover: true,
    prompt: OB.I18N.getLabel('OBUIAPP_NewIconPrompt'),
    height: 14,
    width: 14,
    src:  '[SKINIMG]../../org.openbravo.client.application/images/form/asterisk-new.png'
  },

  iconButtonGroupSpacerWidth: 5
});

isc.OBStatusBarLeftBar.addProperties({
  baseStyle: 'OBStatusBarLeftBar',
  width: '*',
  layoutLeftMargin: 5,
  defaultLayoutAlign: 'center',
  align: 'left',
  overflow: 'visible'
});

isc.OBStatusBarTextLabel.addProperties({
  baseStyle: "OBStatusBarTextLabel"
});

isc.OBStatusBarIconButtonBar.addProperties({
  styleName: "OBStatusBarIconButtonBar",
  width: 130,
  align: 'right',
  overflow: 'visible',
  membersMargin: 4
});

isc.OBStatusBarIconButton.addProperties({
  imageType: 'center',
  showRollOver: true,
  showDown: true,
  showFocused: false,
  initWidgetStyle: function() {
    this.setSrc('[SKINIMG]../../org.openbravo.client.application/images/statusbar/iconButton-' + this.buttonType + '.png');
  }
});