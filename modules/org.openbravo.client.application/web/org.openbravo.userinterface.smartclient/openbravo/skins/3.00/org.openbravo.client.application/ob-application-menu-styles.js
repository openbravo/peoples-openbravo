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


isc.OBApplicationMenuTreeChild.addProperties({
  styleName: 'OBApplicationMenuTree',
  baseStyle: 'OBApplicationMenuTreeItemCell',
  bodyStyleName: 'OBApplicationMenuTreeBody',
  iconBodyStyleName: 'OBApplicationMenuTreeIconBody',
  tableStyle: "OBApplicationMenuTreeTable"
});


isc.OBApplicationMenuTree.addProperties({
  styleName: 'OBApplicationMenuTree',
  baseStyle: 'OBApplicationMenuTreeItemCell',
  bodyStyleName: 'OBApplicationMenuTreeBody',
  iconBodyStyleName: 'OBApplicationMenuTreeIconBody',
  tableStyle: "OBApplicationMenuTreeTable",
  hideButtonLineStyle: 'OBNavBarComponentHideLine',
  submenuOffset: -6,
  drawStyle: function() {
    //this.setStyleName(this.styleName);
  },
  showStyle: function() {
    this.menuButton.parentElement.setStyleName('OBNavBarComponentSelected');
  },
  hideStyle: function() {
    this.menuButton.parentElement.setStyleName('OBNavBarComponent');
  }
});


isc.OBApplicationMenuButton.addProperties({
  baseStyle: 'OBNavBarTextButton',
  showMenuButtonImage: false,
  align: 'center',
  height: 26,
  iconHeight: 6,
  iconWidth: 10,
  iconSpacing: 10,
  iconAlign: 'left',
  iconOrientation: 'right',
  nodeIcons: {
    Window: '[SKINIMG]../../org.openbravo.client.application/images/application-menu/iconWindow.png',
    Process: '[SKINIMG]../../org.openbravo.client.application/images/application-menu/iconProcess.png',
    ProcessManual: '[SKINIMG]../../org.openbravo.client.application/images/application-menu/iconProcess.png',
    Report: '[SKINIMG]../../org.openbravo.client.application/images/application-menu/iconReport.png',
    Task: '[SKINIMG]../../org.openbravo.client.application/images/application-menu/iconTask.png',
    Form: '[SKINIMG]../../org.openbravo.client.application/images/application-menu/iconForm.png',
    ExternalLink: '[SKINIMG]../../org.openbravo.client.application/images/application-menu/iconExternalLink.png',
    Folder: '[SKINIMG]../../org.openbravo.client.application/images/application-menu/iconFolderOpened.png',
    View: '[SKINIMG]../../org.openbravo.client.application/images/application-menu/iconForm.png'
  },
  icon: {
    src: '[SKINIMG]../../org.openbravo.client.application/images/navbar/ico-green-arrow-down.png'
  },
  showMenuStyle: function() {
    this.parentElement.setStyleName('OBNavBarComponentSelected');
  }
});