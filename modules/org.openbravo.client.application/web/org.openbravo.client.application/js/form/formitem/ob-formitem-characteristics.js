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
  destroyOnClose: false,

  mainLayoutDefaults: {
    _constructor: 'VLayout',
    width: 380,
    height: 105,
    layoutMargin: 5
  },

  buttonLayoutDefaults: {
    _constructor: 'HLayout',
    width: '100%',
    height: 22,
    layoutAlign: 'right',
    align: 'right',
    membersMargin: 5,
    autoParent: 'mainLayout'
  },

  okButtonDefaults: {
    _constructor: 'OBFormButton',
    height: 22,
    width: 80,
    canFocus: true,
    autoParent: 'buttonLayout',
    click: function () {
      this.creator.accept();
    }
  },

  clearButtonDefaults: {
    _constructor: 'OBFormButton',
    height: 22,
    width: 80,
    canFocus: true,
    autoParent: 'buttonLayout',
    click: function () {
      this.creator.clearValues();
    }
  },

  cancelButtonDefaults: {
    _constructor: 'OBFormButton',
    height: 22,
    width: 80,
    canFocus: true,
    autoParent: 'buttonLayout',
    click: function () {
      this.creator.cancel();
    }
  },

  /**
   * Based on values selected in the tree, returns the ones that are
   * going to be used for visualization and/or filtering:
   * 
   *   -Filtering: includes all selected leaf nodes
   *   -Visualization: includes the top in branch fully selected nodes
   */
  getValue: function () {
    var selection = this.tree.getSelection(),
        result = {},
        i, c, chars = {},
        values = {},
        completeParentNodes = [],
        node, currentChar, grandParent;

    for (i = 0; i < selection.length; i++) {
      node = selection[i];
      if (node.isCharacteristic) {
        continue;
      }

      if (!result[node.characteristic]) {
        result[node.characteristic] = {
          name: node.characteristic$_identifier,
          values: []
        };
      }

      currentChar = result[node.characteristic];

      if (node.children) {
        // parent node, include it only if fully selected
        if (!this.tree.isPartiallySelected(node)) {
          // this is a fully selected group value
          grandParent = false;
          for (c = 0; c < node.children.length; c++) {
            if (node.children[c].children) {
              grandParent = true;
              break;
            }
          }

          if (!grandParent) {
            completeParentNodes.push(node.id);
            currentChar.values.push({
              value: node._identifier,
              filter: false,
              visualize: true
            });
          }
        }
      } else {
        // leaf node: always filters, visualized if parent is not fully selected
        currentChar.values.push({
          value: node.id,
          shownValue: node._identifier,
          filter: true,
          visualize: completeParentNodes.indexOf(node.parentId) === -1
        });
      }
    }

    return result;
  },

  accept: function () {
    if (this.callback) {
      this.fireCallback(this.callback, 'value', [this.getValue()]);
    }
    this.hide();
  },

  clearValues: function () {
    this.tree.deselectAllRecords();
  },

  cancel: function () {
    this.hide();
  },

  initWidget: function () {
    var me = this,
        dataArrived, checkInitialNodes, getNodeByID;

    this.Super('initWidget', arguments);

    this.addAutoChild('mainLayout');

    this.selectionVisualization = isc.Label.create({
      contents: null
    });
    this.mainLayout.addMember(this.selectionVisualization);



    /**
     * Overrides dataArrived to initialize the tree initial selection
     * based on the filter initial criteria
     */
    dataArrived = function () {
      var internalValue, nodeList, i, j;
      this.Super('dataArrived', arguments);
      if (this.topElement && this.topElement.creator && this.topElement.creator.internalValue) {
        this.checkInitialNodes(this.topElement.creator.internalValue);
      }

    };

    /**
     * Marks the checkboxes of the nodes that
     * are present in the initial criteria
     */
    checkInitialNodes = function (internalValue) {
      var c, v, value, node, characteristic;
      for (c in internalValue) {
        if (internalValue.hasOwnProperty(c)) {
          characteristic = internalValue[c];
          for (v = 0; v < characteristic.values.length; v++) {
            value = characteristic.values[v];
            if (value.filter) {
              node = this.getNodeByID(value.value);
              if (node) {
                this.selectRecord(node);
              }
            }
          }
        }
      }
    };

    /**
     * Returns a tree node given its id
     */
    getNodeByID = function (nodeId) {
      var i, node, nodeList = this.data.getNodeList();
      for (i = 0; i < nodeList.length; i++) {
        node = nodeList[i];
        if (node.id === nodeId) {
          return node;
        }
      }
      return null;
    };

    this.tree = isc.TreeGrid.create({
      showHeader: false,
      autoFetchData: true,
      dataArrived: dataArrived,
      checkInitialNodes: checkInitialNodes,
      getNodeByID: getNodeByID,
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
      cascadeSelection: true,
      selectionChanged: function () {
        me.fireOnPause('updateCharacteristicsText', function () {
          //fire on pause because selecting a node raises several time selectionChanged to select its parants
          me.selectionVisualization.setContents(isc.OBCharacteristicsFilterItem.getDisplayValue(me.getValue()));
        }, 100);
      }
    });

    OB.Datasource.get('BE2735798ECC4EF88D131F16F1C4EC72', this.tree, null, true);

    this.mainLayout.addMember(this.tree);
    this.addAutoChild('buttonLayout');
    this.addAutoChild('okButton', {
      canFocus: true,
      title: OB.I18N.getLabel('OBUISC_Dialog.OK_BUTTON_TITLE')
    });
    this.addAutoChild('clearButton', {
      canFocus: true,
      title: OB.I18N.getLabel('OBUIAPP_Clear')
    });
    this.addAutoChild('cancelButton', {
      canFocus: true,
      title: OB.I18N.getLabel('OBUISC_Dialog.CANCEL_BUTTON_TITLE')
    });
    this.addItem(this.mainLayout);
  }
});


isc.ClassFactory.defineClass('OBCharacteristicsFilterItem', isc.OBTextItem);


isc.OBCharacteristicsFilterItem.addClassProperties({
  getDisplayValue: function (displayValue) {
    var c, characteristic, v, value, hasAny = false,
        result = '';

    for (c in displayValue) {
      if (displayValue.hasOwnProperty(c)) {
        characteristic = displayValue[c];
        result += (hasAny ? '], ' : '') + characteristic.name + ':[';
        hasAny = true;

        for (v = 0; v < characteristic.values.length; v++) {
          value = characteristic.values[v];
          if (value.visualize) {
            result += (v > 0 ? ' - ' : '') + (value.shownValue || value.value);
          }
        }
      }
    }
    result += hasAny ? ']' : '';
    return result;
  }
});

isc.OBCharacteristicsFilterItem.addProperties({
  defaultHqlExists: 'exists (from ProductCharacteristicValue v where e = v.product and v.characteristicValue.id in ($value))',
  productSelectorHqlExists: 'exists (from ProductCharacteristicValue v where e.product = v.product and v.characteristicValue.id in ($value))',
  productSelectorID: '2E64F551C7C4470C80C29DBA24B34A5F',
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


  setCriterion: function (criterion) {
    if (criterion && criterion.internalValue) {
      this.internalValue = criterion.internalValue;
    }
  },

  /**
   * Criterion obtained queries the text field with the concatenation of all characteristics.
   * 
   * It might be changed to query actual table of characteristic values, but this would make it
   * not usable in other views than Product
   */
  getCriterion: function () {
    var c, characteristic, v, value, charCriteria, fieldName = this.getCriteriaFieldName(),
        inValues;
    if (!this.internalValue) {
      return;
    }

    var result;
    result = {
      _constructor: 'AdvancedCriteria',
      operator: 'and',
      criteria: []
    };

    for (c in this.internalValue) {
      if (this.internalValue.hasOwnProperty(c)) {
        characteristic = this.internalValue[c];

        inValues = [];
        for (v = 0; v < characteristic.values.length; v++) {
          value = characteristic.values[v];
          if (value.filter) {
            inValues.push(value.value);
          }
        }

        charCriteria = {
          operator: 'exists',
          fieldName: this.getCriteriaFieldName(),
          value: inValues,
          internalValue: this.internalValue
        };

        if (this.selectorWindow && this.selectorWindow.selector && this.selectorWindow.selector.selectorDefinitionId === this.productSelectorID) {
          charCriteria.existsQuery = this.productSelectorHqlExists;
        } else {
          charCriteria.existsQuery = this.defaultHqlExists;
        }

        result.criteria.push(charCriteria);
      }
    }

    return result;
  },

  setValue: function (value) {
    this.Super('setValue', isc.OBCharacteristicsFilterItem.getDisplayValue(this.internalValue));
  },

  /**
   * Reusing same method as in OBMiniDateRangeItem. It is invoked when filter is removed
   * from grid.
   */
  clearFilterValues: function () {
    this.filterDialog.tree.deselectAllRecords();
    delete this.internalValue;
  },

  filterDialogCallback: function (value) {
    this.internalValue = value;
    this.setElementValue(isc.OBCharacteristicsFilterItem.getDisplayValue(value));
    this.form.grid.performAction();
  },

  init: function () {
    this.addAutoChild('filterDialog', {
      title: this.title,
      callback: this.getID() + '.filterDialogCallback(value)'
    });

    this.icons = [isc.addProperties({
      prompt: this.pickerIconPrompt
    }, this.pickerIconDefaults, this.pickerIconProperties)];

    this.Super('init', arguments);
  },

  showDialog: function () {
    this.filterDialog.show();
  }
});