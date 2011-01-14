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

/* =====================================================================
 * Styling properties for:
 * 1) OB Form items
 * 2) Main layout
 * 3) Main components (navbar flyout, main grid, form)
 * 4) Changes to standard isc.Dialog buttons
 =======================================================================*/
/* =====================================================================
 * FormItem styling properties
 =======================================================================*/
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
  width: '100%',
  cellStyle: 'OBFormField',
  titleStyle: 'OBFormFieldLabel',
  textBoxStyle: 'OBFormFieldInput'
});

OB.DefaultPickListStyleProperties = {
  pickListTallBaseStyle: 'OBFormField',
  cellStyle: 'OBFormField',
  titleStyle: 'OBFormFieldLabel',
  textBoxStyle: 'OBFormFieldSelectInput',
  controlStyle: 'OBFormFieldSelectControl',
  pickerIconStyle: 'OBFormFieldSelectPickerIcon',
  pickListBaseStyle: 'OBFormFieldPickListCell',
  pickerIconSrc: '[SKIN]/../../org.openbravo.client.application/images/form/comboBoxPicker.png',
  height: 21,
  pickerIconWidth: 21,
  pickerIconHeight: 21,
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

isc.OBYesNoItem.addProperties(isc.addProperties({}, OB.DefaultPickListStyleProperties));


isc.DateChooser.addProperties({  //TODO : Changed to 'isc.OBDateChooser.addProperties'
  headerStyle: 'OBDateChooserButton',
  weekendHeaderStyle: 'OBDateChooserWeekendButton',
  baseNavButtonStyle: 'OBDateChooserNavButton',
  baseWeekdayStyle: 'OBDateChooserWeekday',
  baseWeekendStyle: 'OBDateChooserWeekend',
  baseBottomButtonStyle: 'OBDateChooserBottomButton',
  alternateWeekStyles: false,

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
 * Main layout styling properties
 =======================================================================*/
// note main layout styling is done a bit differently 
// as this needs to be set when the layout gets created
OB.MainLayoutStylingProperties = {};

// Styling of the tab headers of the main views in the main tabset
OB.MainLayoutStylingProperties.OBTabHeaderButton = {
  align: 'right',
  width: 1,
  overflow: 'visible',
  capSize: 8,
  baseStyle: 'OBMainTabBarButton',
  titleStyle: 'OBMainTabBarButtonTitle'
};

// The toolbar showing the navigation bar components
OB.MainLayoutStylingProperties.Toolbar = {
  overflow: 'visible',
  defaultLayoutAlign: 'center',
  styleName: 'OBNavBarToolStrip',
  width: 1,
  layoutLeftMargin: 2,
  separatorSize: 0,
  height: 28
};

// Styling of the main layout containing everything
OB.MainLayoutStylingProperties.TopLayout = {
  width: '100%',
  height: '1',
  overflow: 'visible',
  layoutTopMargin: 4
};

// Properties for the custom company image
OB.MainLayoutStylingProperties.CompanyImageLogo = {
  width: 122,
  height: 34,
  src: OB.Application.contextUrl + 'utility/ShowImageLogo?logo=yourcompanymenu'
};

// The main tabset showing the tabs with views
OB.MainLayoutStylingProperties.TabSet = {
  tabBarPosition: 'top',
  
  paneContainerClassName: 'OBMainTabPaneContainer',
  
  // get rid of the margin around the content of a pane
  paneMargin: 0,
  paneContainerMargin: 0,
  paneContainerPadding: 0,
  showPaneContainerEdges: false,
  
  closeTabIcon: '[SKINIMG]../../org.openbravo.client.application/images/tab/ico-close-tab.png',
  closeTabIconSize: 18,
  
  useSimpleTabs: true,
  styleName: 'OBMainTab',
  simpleTabBaseStyle: 'OBMainTab'
};

// The tab bar of the main tabset
OB.MainLayoutStylingProperties.TabSet_tabBarProperties = {
  height: 30,
  styleName: 'OBMainTabBar',
  baseStyle: 'OBMainTab'
};

// The tab styling properties of the main tabset
OB.MainLayoutStylingProperties.TabSet_tabProperties = {
  styleName: 'OBMainTabBar',
  baseStyle: 'OBMainTab',
  margin: 0,
  padding: 0
};

/* =====================================================================
 * Main components styling properties
 =======================================================================*/
// The quick run widget is used for flyouts in the navigation bar
isc.OBQuickRun.addProperties({

  // ** {{{ baseStyle }}} **
  // The base style for the quick run launch button. All other styles are
  // derived
  // from this base style.
  baseStyle: 'OBNavBarButton'
});

isc.OBPopup.addProperties({
  width: 600,
  height: 500
});

isc.OBStandardWindow.addProperties({
  toolBarHeight: 40
});

// Styling properties for the application menu button present in the navbar
OB.ApplicationMenuButtonStylingProperties = {
  baseStyle: 'OBNavBarButton',
  showMenuButtonImage: false,
  align: 'center',
  height: 26,
  iconHeight: 6,
  iconWidth: 10,
  iconSpacing: 10,
  iconAlign: 'left',
  iconOrientation: 'right',
  icon: {
    src: '[SKINIMG]../../org.openbravo.client.application/images/navbar/ico-green-arrow-down.gif'
  }
};

// Styling properties for the application menu
OB.ApplicationMenuStylingProperties = {
  baseStyle: 'OBNavBarComponentMenuItemCell',
  styleName: 'OBNavBarComponentMenu',
  bodyStyleName: 'OBNavBarComponentMenuBody',
  submenuOffset: -6
};

// Styling properties for the help/about navigation bar component
isc.OBHelpAbout.addProperties({
  baseStyle: 'OBNavBarButton',
  iconHeight: 6,
  iconWidth: 10,
  iconSpacing: 10,
  icon: {
    src: '[SKINIMG]../../org.openbravo.client.application/images/navbar/ico-green-arrow-down.gif'
  },
  iconOrientation: 'right'
});

// Styling properties for the logout button in the navbar
OB.LogoutNavbarComponentStylingProperties = {
  baseStyle: 'OBNavBarButton',
  height: 14,
  width: 14,
  src: '[SKINIMG]../../org.openbravo.client.application/images/navbar/iconClose.png',
  showTitle: false,
  imageType: 'normal',
  layoutAlign: 'center',
  overflow: 'visible',
  showRollOver: false,
  showFocused: false,
  showDown: false
};

// Styling properties for the quick launch and quick create components
// See also isc.OBQuickRun styling properties
OB.QuickLaunchNavbarComponentStylingProperties = {
  // todo: it is nicer to move this to a style but then this issue occurs:
  // https://issues.openbravo.com/view.php?id=13786
  width: 37,
  
  layoutProperties: {
    width: 250,
    membersMargin: 10
  }
};

/* =====================================================================
 * Width of the active bar on the left in the main view
 =======================================================================*/
OB.ActiveBarStyling = {
  width: 6,
  activeStyleName: 'OBViewActive',
  inActiveStyleName: 'OBViewInActive'
};

/* =====================================================================
 * Changed styling of the standard dialogs
 =======================================================================*/
isc.Dialog.OK.buttonConstructor = isc.OBFormButton;
isc.Dialog.OK.baseStyle = 'OBFormButton';
isc.Dialog.OK.titleStyle = 'OBFormButtonTitle';
isc.Dialog.CANCEL.buttonConstructor = isc.OBFormButton;
isc.Dialog.CANCEL.baseStyle = 'OBFormButton';
isc.Dialog.CANCEL.titleStyle = 'OBFormButtonTitle';
isc.Dialog.YES.buttonConstructor = isc.OBFormButton;
isc.Dialog.YES.baseStyle = 'OBFormButton';
isc.Dialog.YES.titleStyle = 'OBFormButtonTitle';
isc.Dialog.NO.buttonConstructor = isc.OBFormButton;
isc.Dialog.NO.baseStyle = 'OBFormButton';
isc.Dialog.NO.titleStyle = 'OBFormButtonTitle';
isc.Dialog.APPLY.buttonConstructor = isc.OBFormButton;
isc.Dialog.APPLY.baseStyle = 'OBFormButton';
isc.Dialog.APPLY.titleStyle = 'OBFormButtonTitle';
isc.Dialog.DONE.buttonConstructor = isc.OBFormButton;
isc.Dialog.DONE.baseStyle = 'OBFormButton';
isc.Dialog.DONE.titleStyle = 'OBFormButtonTitle';

isc.ListGrid.addProperties({
  alternateRecordStyles: true
});

// override the standard show prompt to show a more custom Openbravo
// loading prompt
// note the loading image is set in the index.html
isc._orginal_showPrompt = isc.showPrompt;
isc.showPrompt = function(prompt){
  var width, height, top, left, props = {}, dialog = isc.Dialog.Prompt, modalTarget;
  if (OB.OBModalTarget) {
    props = {
      showEdges: false,
      showModalMask: true,
      isModal: true,
      hiliteBodyColor: null,
      bodyColor: null,
      bodyStyle: 'OBLoadingPromptBody'
    };
    props.isModal = true;
    modalTarget = OB.OBModalTarget;
    props.modalTarget = modalTarget;
    isc.Dialog.OBModalTarget = null;
    
    // find the top/left position, center in the modalTarget
    width = dialog.getVisibleWidth();
    height = dialog.getVisibleHeight();
    left = modalTarget.getPageLeft() + ((modalTarget.getWidth() - width) / 2) + modalTarget.getScrollLeft();
    top = modalTarget.getPageTop() + ((modalTarget.getHeight() - height) / 2) + modalTarget.getScrollTop();
    props.left = Math.round(left);
    props.top = Math.max(Math.round(top), 0);
    props.autoCenter = false;
  }
  
  isc._orginal_showPrompt(prompt, props);
};

