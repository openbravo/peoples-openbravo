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

/* =====================================================================
 * Styling properties for:
 * 1) OB Form items
 * 2) SectionItem Button Styles
 =======================================================================*/

/* =====================================================================
 * FormItem styling properties
 =======================================================================*/
isc.OBViewForm.addProperties({
  styleName: 'OBViewForm',
  width: '100%',
  overflow: 'visible',
  //cellBorder: 1, // debug layout
  cellPadding: 0
});


isc.OBFormButton.addProperties({
  baseStyle: 'OBFormButton',
  titleStyle: 'OBFormButtonTitle'
});

isc.OBTextItem.addProperties({
  errorOrientation: 'left',
  height: 21,
  width: '100%',
  cellStyle: 'OBFormField',
  titleStyle: 'OBFormFieldLabel',
  textBoxStyle: 'OBFormFieldInput'
});

isc.OBTextAreaItem.addProperties({
  errorOrientation: 'left',
  height: 66,
  width: '100%',
  cellStyle: 'OBFormField',
  titleStyle: 'OBFormFieldLabel',
  textBoxStyle: 'OBFormFieldInput'
});

OB.DefaultPickListStyleProperties = {
  cellStyle: 'OBFormField',
  titleStyle: 'OBFormFieldLabel',
  textBoxStyle: 'OBFormFieldSelectInput',
  controlStyle: 'OBFormFieldSelectControl',
  pickerIconStyle: 'OBFormFieldSelectPickerIcon',
  pickListBaseStyle: 'OBFormFieldPickListCell',
  // tallbasestyle is used when the cellheight is different
  // from the standard
  pickListTallBaseStyle: 'OBFormFieldPickListCell',
  pickerIconSrc: '[SKIN]/../../org.openbravo.client.application/images/form/comboBoxPicker.png',
  height: 21,
  pickerIconWidth: 21,
  pickerIconHeight: 21, 
  
  // note the menu-rollover.png which is the background for selected rows
  // is 20
  pickListCellHeight: 22,
  
  quickRunPickListWidth: 225,
  // fixes issue https://issues.openbravo.com/view.php?id=15105
  quickRunPickListCellHeight: 22,
  pickListHeight: 200,
  autoSizePickList: false,

  pickListProperties: {
    showShadow: false,
    shadowDepth: 5,
    bodyStyleName: 'OBPickListBody'
  },
  
  errorOrientation: 'left'
};

isc.OBListItem.addProperties(isc.addProperties({}, OB.DefaultPickListStyleProperties));

isc.OBFKItem.addProperties(isc.addProperties({}, OB.DefaultPickListStyleProperties));

isc.OBFKItem.addProperties({
  newTabIconSrc: '[SKINIMG]../../org.openbravo.client.application/images/form/ico-to-new-tab.png',
  newTabIconSize: 8
});

isc.OBYesNoItem.addProperties(isc.addProperties({}, OB.DefaultPickListStyleProperties));


isc.OBSearchItem.addProperties({
  cellStyle: 'OBFormField',
  titleStyle: 'OBFormFieldLabel',
  textBoxStyle: 'OBFormFieldStatic',
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


isc.OBDateChooser.addProperties({
  headerStyle: 'OBDateChooserButton',
  weekendHeaderStyle: 'OBDateChooserWeekendButton',
  baseNavButtonStyle: 'OBDateChooserNavButton',
  baseWeekdayStyle: 'OBDateChooserWeekday',
  baseWeekendStyle: 'OBDateChooserWeekend',
  baseBottomButtonStyle: 'OBDateChooserBottomButton',
  alternateWeekStyles: false,
  firstDayOfWeek: 1,  

  showEdges: true,

  edgeImage: '[SKIN]/../../../org.openbravo.client.application/images/form/dateChooser-popup.png',
  edgeSize: 6,
  edgeTop: 26,
  edgeBottom: 5,
  edgeOffsetTop: 1,
  edgeOffsetRight: 5,
  edgeOffsetLeft: 5,
  edgeOffsetBottom: 5,

  todayButtonHeight: 20,

  headerHeight: 24,

  edgeCenterBackgroundColor: '#FFFFFF',
  backgroundColor: null,

  showShadow: false,
  shadowDepth: 6,
  shadowOffset: 5,

  showDoubleYearIcon: false,
  prevYearIcon: '[SKIN]/../../../org.openbravo.client.application/images/form/dateChooser-doubleArrow_left.png',
  prevYearIconWidth: 16,
  prevYearIconHeight: 16,
  nextYearIcon: '[SKIN]/../../../org.openbravo.client.application/images/form/dateChooser-doubleArrow_right.png',
  nextYearIconWidth: 16,
  nextYearIconHeight: 16,
  prevMonthIcon: '[SKIN]/../../../org.openbravo.client.application/images/form/dateChooser-arrow_left.png',
  prevMonthIconWidth: 16,
  prevMonthIconHeight: 16,
  nextMonthIcon: '[SKIN]/../../../org.openbravo.client.application/images/form/dateChooser-arrow_right.png',
  nextMonthIconWidth: 16,
  nextMonthIconHeight: 16
});


OB.OBDateItemStyleProperties = {
  cellStyle: 'OBFormField',
  titleStyle: 'OBFormFieldLabel',
  textBoxStyle: 'OBFormFieldInput',
  errorOrientation: 'left',

  pickerIconHSpace: '0',

  textFieldProperties: {
    type: 'OBTextField',
    textBoxStyle: 'OBFormFieldDateInput'
  },

  height: 25,

  pickerIconWidth: 21,
  pickerIconHeight: 21,
  pickerIconSrc: '[SKIN]/../../org.openbravo.client.application/images/form/date_control.png'
};

isc.OBDateItem.addProperties(isc.addProperties({}, OB.OBDateItemStyleProperties));

isc.OBDateTimeItem.addProperties(isc.addProperties({}, OB.OBDateItemStyleProperties));


isc.OBNumberItem.addProperties({
  cellStyle: 'OBFormField',
  titleStyle: 'OBFormFieldLabel',
  textBoxStyle: 'OBFormFieldInput',
  errorOrientation: 'left'
});


/* =====================================================================
 * SectionItem Button Styles
 =======================================================================*/

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
