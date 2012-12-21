isc.ClassFactory.defineClass('OBMultiSelectorItem', isc.CanvasItem);
isc.OBMultiSelectorItem.addProperties({
  rowSpan: 2,
  canvasConstructor: 'OBMultiSelectorSelectorLayout',
  selectionLayout: null,
  selectorGridFields: [{
    title: OB.I18N.getLabel('OBUISC_Identifier'),
    name: OB.Constants.IDENTIFIER
  }],

  init: function () {
    this.icons = [{
      selector: this,
      src: this.popupIconSrc,
      width: this.popupIconWidth,
      height: this.popupIconHeight,
      hspace: this.popupIconHspace,
      keyPress: function (keyName, character, form, item, icon) {
        var response = OB.KeyboardManager.Shortcuts.monitor('OBSelectorItem.icon', this.selector);
        if (response !== false) {
          response = this.Super('keyPress', arguments);
        }
        return response;
      },
      click: function (form, item, icon) {
        item.openSelectorWindow();
      }
    }];


    if (this.disabled) {
      // TODO: disable, remove icons
      this.icons = null;
    }
    if (!this.showSelectorGrid) {
      this.icons = null;
    }

    if (this.showSelectorGrid && !this.form.isPreviewForm) {
      // adds pin field, which is marked as pin whenever the
      // record is part of the selection
      this.selectorGridFields.unshift({
        name: '_pin',
        type: 'boolean',
        title: '&nbsp;',
        canEdit: false,
        canFilter: false,
        canSort: false,
        canReorder: false,
        canHide: false,
        frozen: true,
        canFreeze: false,
        canDragResize: false,
        canGroupBy: false,
        autoExpand: false,
        width: OB.Styles.Process.PickAndExecute.pinColumnWidth,
        formatCellValue: function (value, record, rowNum, colNum, grid) {
          if (grid.selector.selectorWindow.selectedIds.contains(record[OB.Constants.ID])) {
            return '<img src="' + OB.Styles.Process.PickAndExecute.iconPinSrc + '" />';
          }
          return '';
        },
        formatEditorValue: function (value, record, rowNum, colNum, grid) {
          return this.formatCellValue(arguments);
        }
      });

      this.selectorWindow = isc.OBSelectorPopupWindow.create({
        // solves issue: https://issues.openbravo.com/view.php?id=17268
        title: (this.form && this.form.grid ? this.form.grid.getField(this.name).title : this.title),
        dataSource: this.optionDataSource,
        selector: this,
        valueField: this.valueField,
        displayField: this.displayField,
        selectorGridFields: isc.shallowClone(this.selectorGridFields),
        selectionAppearance: 'checkbox',
        multiselect: true,
        selectedIds: this.getValue() || [],
        selectId: function (id) {
          if (!this.selectedIds.contains(id)) {
            this.selectedIds.push(id);
          }
        }
      });
    }

    this.optionCriteria = {
      _selectorDefinitionId: this.selectorDefinitionId
    };

    this.Super('init', arguments);

    this.selectionLayout = this.canvas;

    if (this.initStyle) {
      this.initStyle();
    }
  },

  // resets whole selection to the records passed as parameter
  setSelectedRecords: function (records) {
    var i;
    this.storeValue([]);
    this.selectionLayout.removeMembers(this.selectionLayout.getMembers());
    for (i = 0; i < records.length; i++) {
      this.setValueFromRecord(records[i]);
    }
  },

  // adds a new record to the selection
  setValueFromRecord: function (record) {
    var me = this,
        selectedElement, currentValue = this.getValue() || [];

    // add record to selected values
    currentValue.push(record[OB.Constants.ID]);
    this.storeValue(currentValue);

    // display it in the layout
    selectedElement = isc.Label.create({
      contents: record[OB.Constants.IDENTIFIER],
      icon: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/form/clearField.png',
      height: 1,
      value: record[OB.Constants.ID],
      iconClick: function () {
        var currentValues = me.getValue() || [];
        currentValues.remove(this.value);
        me.selectionLayout.removeMember(this);
      }
    });
    this.selectionLayout.addMember(selectedElement);

    if (this.form && this.form.handleItemChange) {
      this._hasChanged = true;
      this.form.handleItemChange(this);
    }

    if (this.form.focusInNextItem && isc.EH.getKeyName() !== 'Tab') {
      this.form.focusInNextItem(this.name);
    }
  },

  openSelectorWindow: function () {
    // always refresh the content of the grid to force a reload
    // if the organization has changed
    if (this.selectorWindow.selectorGrid) {
      this.selectorWindow.selectorGrid.invalidateCache();
    }
    this.selectorWindow.selectedIds = this.getValue() || [];
    this.selectorWindow.open();
  },

  destroy: function () {
    // Explicitly destroy the selector window to avoid memory leaks
    if (this.selectorWindow) {
      this.selectorWindow.destroy();
      this.selectorWindow = null;
    }
    this.Super('destroy', arguments);
  }
});

isc.ClassFactory.defineClass('OBMultiSelectorSelectorLayout', isc.VStack);

isc.OBMultiSelectorSelectorLayout.addProperties({
  popupTextMatchStyle: 'startswith',
  suggestionTextMatchStyle: 'startswith',
  showOptionsFromDataSource: true,

  autoDraw: false,
  overflow: 'auto',
  members: [],
  animateMembers: true,
  animateMemberTime: 100
});