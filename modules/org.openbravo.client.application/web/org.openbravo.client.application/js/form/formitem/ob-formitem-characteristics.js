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
 * All portions are Copyright (C) 2013 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

isc.ClassFactory.defineClass('OBCharacteristicsItem', isc.CanvasItem);

isc.OBCharacteristicsItem.addProperties({
  completeValue: null,
  showTitle: false,
  init: function () {
    this.canvas = isc.OBCharacteristicsLayout.create({

    });

    this.colSpan = 4;
    this.disabled = false;

    this.Super('init', arguments);
  },

  setValue: function (value) {
    var field, formFields = [],
        itemIds = [];

    this.completeValue = value;
    if (!value || !value.characteristics) {
      if (!value) {
        this.hide();
      }
      this.Super('setValue', arguments);
      return;
    }

    this.show();

    //Remove all members the widget might have
    //this.canvas.removeMembers(this.canvas.getMembers());
    //    
    if (value.characteristics) {
      for (field in value.characteristics) {
        if (value.characteristics.hasOwnProperty(field)) {
          formFields.push({
            width: '*',
            title: field,
            disabled: true,
            name: '__Characteristic__' + field,
            type: 'OBTextItem',
            value: value.characteristics[field]
          });
          itemIds.push('__Characteristic__' + field);
        }
      }
    }

    formFields.unshift({
      defaultValue: this.title,
      type: 'OBSectionItem',
      sectionExpanded: true,
      itemIds: itemIds
    });


    this.canvas.setFields(formFields);

    // actual value is the one in DB
    this.setValue(value.dbValue);
  }
});

isc.ClassFactory.defineClass('OBCharacteristicsLayout', isc.DynamicForm);

isc.OBCharacteristicsLayout.addProperties({
  titleOrientation: 'top',
  width: '*',
  numCols: 4,
  colWidths: ['25%', '25%', '25%', '25%']
});

isc.ClassFactory.defineClass('OBCharacteristicsFilterDialog', isc.OBPopup);

isc.OBCharacteristicsFilterDialog.addProperties({
  isModal: true,
  showModalMask: true,
  dismissOnEscape: true,
  autoCenter: true,
  autoSize: true,
  vertical: true,
  showMinimizeButton: false,

  mainLayoutDefaults: {
    _constructor: 'VLayout',
    width: 380,
    height: 105,
    layoutMargin: 5
  },


  initWidget: function () {
    this.Super('initWidget', arguments);

    this.addAutoChild('mainLayout');
    this.addItem(this.mainLayout);


    var tree = isc.TreeGrid.create({
      showHeader: false,

      autoFetchData: true,
      loadDataOnDemand: false,
      // loading the whole tree in a single request
      height: 400,
      showOpenIcons: false,
      showDropIcons: false,
      nodeIcon: null,
      folderIcon: null,
      openIconSuffix: 'open',
      selectionAppearance: 'checkbox',
      showSelectedStyle: false,
      showPartialSelection: true,
      cascadeSelection: true
    });

    OB.Datasource.get('BE2735798ECC4EF88D131F16F1C4EC72', tree, null, true);

    this.mainLayout.addMember(tree);
  }
});


isc.ClassFactory.defineClass('OBCharacteristicsFilterItem', isc.OBTextItem);

isc.OBCharacteristicsFilterItem.addProperties({
  showPickerIcon: false,
  filterDialogConstructor: isc.OBCharacteristicsFilterDialog,
  pickerIconDefaults: {
    name: 'showDateRange',
    src: '[SKIN]/DynamicForm/DatePicker_icon.gif',
    width: 16,
    height: 16,
    showOver: false,
    showFocused: false,
    showFocusedWithItem: false,
    hspace: 0,
    click: function (form, item, icon) {
      if (!item.disabled) {
        item.showDialog();
      }
    }
  },

  init: function () {

    this.addAutoChild('filterDialog', {});

    this.icons = [isc.addProperties({
      prompt: this.pickerIconPrompt
    }, this.pickerIconDefaults, this.pickerIconProperties)];

    this.Super('init', arguments);
  },

  showDialog: function () {
    this.filterDialog.show();
  }
});