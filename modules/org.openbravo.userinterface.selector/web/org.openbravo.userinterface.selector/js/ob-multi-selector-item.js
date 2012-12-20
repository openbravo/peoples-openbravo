isc.ClassFactory.defineClass('OBMultiSelectorItem', isc.CanvasItem);


isc.OBMultiSelectorItem.addProperties({
  hasPickList: true,
  popupTextMatchStyle: 'startswith',
  suggestionTextMatchStyle: 'startswith',
  showOptionsFromDataSource: true,
  rowSpan: 2,

  selectionLayout: null,

  // https://issues.openbravo.com/view.php?id=18739
  selectOnFocus: false,
  // still do select on focus initially
  doInitialSelectOnFocus: true,

  // Setting this to false results in the picklist to be shown 
  // on focus, specific SC logic
  //  addUnknownValues: false,
  // ** {{{ selectorGridFields }}} **
  // the definition of the columns in the popup window
  selectorGridFields: [{
    title: OB.I18N.getLabel('OBUISC_Identifier'),
    name: OB.Constants.IDENTIFIER
  }],

  selectFirstPickListOption: function () {
    var firstRecord;
    if (this.pickList) {
      if (this.pickList.data && (this.pickList.data.totalRows > 0)) {
        firstRecord = this.pickList.data.get(0);
        this.pickList.selection.selectSingle(firstRecord);
        this.pickList.clearLastHilite();
        this.pickList.scrollRecordIntoView(0);
      }
    }
  },

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
      this.selectorWindow = isc.OBSelectorPopupWindow.create({
        // solves issue: https://issues.openbravo.com/view.php?id=17268
        title: (this.form && this.form.grid ? this.form.grid.getField(this.name).title : this.title),
        dataSource: this.optionDataSource,
        selector: this,
        valueField: this.valueField,
        displayField: this.displayField,
        selectorGridFields: isc.shallowClone(this.selectorGridFields),
        selectionAppearance: 'checkbox',
        multiselect: true
      });
    }

    this.optionCriteria = {
      _selectorDefinitionId: this.selectorDefinitionId
    };

    this.Super('init', arguments);
    if (this.initStyle) {
      this.initStyle();
    }
  },

  setValueFromRecord: function (record, fromPopup) {
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
    this.selectorWindow.open();
  },

  destroy: function () {
    // Explicitly destroy the selector window to avoid memory leaks
    if (this.selectorWindow) {
      this.selectorWindow.destroy();
      this.selectorWindow = null;
    }
    this.Super('destroy', arguments);
  },

  _createCanvas: function () {
    this.selectionLayout = isc.VStack.create({
      autoDraw: false,
      overflow: 'auto',
      members: [],
      animateMembers: true,
      animateMemberTime: 100
    });
    this.canvas = this.selectionLayout;
    this.Super('_createCanvas', arguments);
  }
});