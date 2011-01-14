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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
isc.OBViewForm.addProperties({
  styleName: 'OBViewForm',
  width: '100%',
  overflow: 'visible',
  //cellBorder: 1, // debug layout
  cellPadding: 0
});

isc.OBSectionItem.addProperties({
  sectionHeaderClass: 'OBSectionItemButton',
  height: 24
});

isc.ClassFactory.defineClass('OBSectionItemButton', ImgSectionHeader);
isc.OBSectionItemButton.changeDefaults('backgroundDefaults', {
  showRollOver: true,
  showDown: false,
  showDisabledIcon: false,
  showRollOverIcon: false,
  src: '[SKIN]/../../org.openbravo.client.application/images/form/sectionItem-bg.png',
  icon: '[SKIN]/../../org.openbravo.client.application/images/form/sectionItem-ico.png',
  iconSize: 12,
  capSize: 12,
  titleStyle: 'OBSectionItemButton_Title_',
  backgroundColor: 'transparent'
});

isc.OBSearchItem.addProperties({
  cellStyle: 'OBFormField',
  titleStyle: 'OBFormFieldLabel',
  textBoxStyle: 'OBFormFieldInput',
  pickerIconHeight: 21,
  pickerIconWidth: 21,
  height: 21,
  pickerIconSrc: '[SKINIMG]../../org.openbravo.client.application/images/form/search_picker.png',
  clearIcon: {
    showHover: true,
    height: 15,
    width: 15,
    src: '[SKINIMG]../../org.openbravo.client.application/images/form/clear-field.png',    
    prompt: OB.I18N.getLabel('OBUIAPP_ClearIconPrompt')
  },
  newTabIconSrc: '[SKINIMG]../../org.openbravo.client.application/images/form/ico-to-new-tab.png',
  newTabIconSize: 8
});

isc.OBFKItem.addProperties({
  newTabIconSrc: '[SKINIMG]../../org.openbravo.client.application/images/form/ico-to-new-tab.png',
  newTabIconSize: 8
});
