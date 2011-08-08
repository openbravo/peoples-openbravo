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

OB.Styles.Personalization = {};

OB.Styles.Personalization.Icons = {
  fieldGroup: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/personalization/iconFolder.png',
  field:  OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/personalization/item.png',
  fieldDisplayLogic:  OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/personalization/itemDisplayLogic.png',
  fieldDisplayLogicHidden:  OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/personalization/itemDisplayLogicHidden.png',
  fieldHidden:  OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/personalization/itemHidden.png',
  fieldRequired:  OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/personalization/itemRequired.png',
  fieldRequiredDisplayLogic:  OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/personalization/itemRequiredDisplayLogic.png',
  fieldRequiredDisplayLogicHidden:  OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/personalization/itemRequiredDisplayLogicHidden.png',
  fieldRequiredHidden:  OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/personalization/itemRequiredHidden.png'
};

OB.Styles.Personalization.closeButtonProperties = {
  width: 18,
  height: 18
};

OB.Styles.Personalization.FormPersonalizerLeftPane = {
  width: 200
};

OB.Styles.Personalization.FieldsLayout = {
  styleName: 'OBFieldsPane'
};

OB.Styles.Personalization.Preview = {
  styleName: 'OBFormPersonalizerPreviewPanel'
};

OB.Styles.Personalization.PropertiesTabSet = {
  expandedHeight: 225,
  collapsedHeight: 35
};

OB.Styles.Personalization.PropertiesLayout = {
  styleName: 'OBFormPersonalizerPropertiesPane'
};

// used to display a tab header above sections of the personalization form
OB.Styles.Personalization.TabSet = {
  tabBarProperties: {
    styleName: 'OBTabBarChild',
    simpleTabBaseStyle: 'OBTabBarButtonChild',
    paneContainerClassName: 'OBTabSetChildContainer',
    buttonConstructor: isc.OBTabBarButton,

    buttonProperties: {
      // prevent the orange hats, TODO: this not work
      // don't know why...
      // another solution is to do a custom style but that's a lot of work
      src: '',
      capSize: 14,
      titleStyle: 'OBTabBarButtonChildTitle',
      showSelected: false,
      showFocused: false
    }
  },
  tabBarPosition: 'top',
  width: '100%',
  height: '100%',
  overflow: 'hidden',
  
  showTabPicker: false,

  // get rid of the margin around the content of a pane
  paneMargin: 0,
  paneContainerMargin: 0,
  paneContainerPadding: 0,
  showPaneContainerEdges: false,

  useSimpleTabs: true,
  tabBarThickness: 38,
  styleName: 'OBTabSetChild',
  simpleTabBaseStyle: 'OBTabBarButtonChild',
  paneContainerClassName: 'OBTabSetChildContainer',

  scrollerSrc: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/tab/tabBarButtonChild_OverflowIcon.png',
  pickerButtonSrc: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/tab/tabBarButtonChild_OverflowIconPicker.png'
};


