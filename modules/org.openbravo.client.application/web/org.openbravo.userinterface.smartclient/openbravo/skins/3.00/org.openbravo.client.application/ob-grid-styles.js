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


// Styling properties for main grid shown in a standard view
isc.OBViewGrid.addProperties({
  hoverWidth: 200,
  editLinkColumnWidth: 60
});

isc.OBGridToolStripIcon.addProperties({
  width: 21,
  height: 19,
  showRollOver: true,
  showDown: true,
  showDisabled: false,
  showFocused: false,
  showFocusedAsOver: true,
  baseStyle: 'OBGridToolStripIcon',
  initWidget: function() {
    this.setSrc('[SKIN]/../../org.openbravo.client.application/images/grid/gridButton-' + this.buttonType + '.png'); /* this.buttonType could be: edit - form - cancel - save */
    this.Super('initWidget', arguments);
  }
});

isc.OBGridToolStripSeparator.addProperties({
  width: 1,
  height: 11,
  imageType: 'normal',
  src: '[SKIN]/../../org.openbravo.client.application/images/grid/gridButton-separator.png'
});

isc.OBGridToolStrip.addProperties({
  height: '100%',
  width: '100%',
  styleName: 'OBGridToolStrip',
  membersMargin: 5
});

isc.OBGridButtonsComponent.addProperties({
  height: 1,
  width: '100%',
  visible: 'overflow',
  align: 'center'
});