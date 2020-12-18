/*
 ************************************************************************************
 * Copyright (C) 2013-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo */

/*items of collection*/
enyo.kind({
  name: 'OB.UI.ListValuesLineCheck',
  kind: 'OB.UI.Button',
  classes: 'obUiListValuesLineCheck',
  events: {
    onAddToSelected: ''
  },
  tap: function() {
    this.inherited(arguments);
    var me = this;
    if (!this.parent.model.get('childrenSelected')) {
      this.removeClass('obUiListValuesLineCheck_half-active');
    }
    this.doAddToSelected({
      value: me.parent.model,
      checked: !this.parent.model.get('checked'),
      selected: !this.parent.model.get('selected')
    });
  },
  create: function() {
    this.inherited(arguments);
    this.setLabel(this.parent.model.get('name'));
    if (this.parent.model.get('selected')) {
      this.addClass('obUiListValuesLineCheck_active');
    } else {
      this.removeClass('obUiListValuesLineCheck_active');
      if (this.parent.model.get('childrenSelected')) {
        this.addClass('obUiListValuesLineCheck_half-active');
      }
    }
  }
});

enyo.kind({
  name: 'OB.UI.ListValuesLineChildren',
  kind: 'OB.UI.Button',
  classes: 'obUiListValuesLineChildren',
  i18nLabel: 'OBMOBC_LblMore',
  showing: false,
  childrenArray: [],
  events: {
    onSetCollection: ''
  },
  create: function() {
    this.inherited(arguments);
    if (
      this.parent.model.get('childrenList') &&
      this.parent.model.get('childrenList').length > 0 &&
      this.parent.model.get('showChildren')
    ) {
      this.childrenArray = this.parent.model.get('childrenList');
      this.show();
    }
  },
  tap: function() {
    this.doSetCollection({
      value: this.childrenArray,
      parentValue: this.parent.model.get('id')
    });
  }
});

enyo.kind({
  name: 'OB.UI.ListValuesLine',
  classes: 'obUiListValuesLine',
  components: [
    {
      kind: 'OB.UI.ListValuesLineCheck',
      classes: 'obUiListValuesLine-obUiListValuesLineCheck'
    },
    {
      kind: 'OB.UI.ListValuesLineChildren',
      classes: 'obUiListValuesLine-obUiListValuesLineChildren'
    }
  ]
});

/*scrollable table (body of modal)*/
enyo.kind({
  name: 'OB.UI.ListValues',
  classes: 'obUiListValues',
  handlers: {
    onSearchAction: 'searchAction',
    onClearAction: 'clearAction',
    onSetCollection: 'setCollection'
  },
  components: [
    {
      classes: 'obUiListValues-container1',
      components: [
        {
          name: 'valueslistitemprinter',
          kind: 'OB.UI.ScrollableTable',
          classes: 'obUiListValues-container1-container1-valueslistitemprinter',
          renderLine: 'OB.UI.ListValuesLine',
          renderEmpty: 'OB.UI.RenderEmpty'
        }
      ]
    }
  ],
  clearAction: function(inSender, inEvent) {
    this.valuesList.reset();
    this.initialValuesList = null;
    return true;
  },
  searchAction: async function(inSender, inEvent) {
    let me = this,
      productCharacteristic =
        inSender.parent.parent.$.multiColumn.$.rightPanel.$.toolbarpane.$
          .searchCharacteristic.$.searchCharacteristicTabContent.$
          .searchProductCharacteristicHeader.parent,
      forceRemote = false,
      resetValueList,
      crossStoreSearch;

    crossStoreSearch =
      inSender.parent.parent.$.multiColumn.$.rightPanel.$.toolbarpane.$
        .searchCharacteristic.$.searchCharacteristicTabContent.$
        .searchProductCharacteristicHeader.$.formElementCrossStoreSearch
        .coreElement;
    forceRemote = crossStoreSearch && crossStoreSearch.checked;

    productCharacteristic.customFilters.forEach(function(hqlFilter) {
      if (
        !_.isUndefined(hqlFilter.hqlCriteriaCharacteristicsValue) &&
        !_.isUndefined(hqlFilter.forceRemote)
      ) {
        var hqlCriteriaFilter = hqlFilter.hqlCriteriaCharacteristicsValue();
        if (!_.isUndefined(hqlCriteriaFilter)) {
          hqlCriteriaFilter.forEach(function(filter) {
            if (filter && forceRemote === false) {
              forceRemote = hqlFilter.forceRemote;
            }
          });
        }
      }
    });

    resetValueList = function(dataValues) {
      if (dataValues && dataValues.length > 0) {
        let modelsList, initialModelsList;
        if (
          OB.MobileApp.model.hasPermission('OBPOS_remote.product', true) ||
          forceRemote
        ) {
          initialModelsList = dataValues.models;
        } else {
          initialModelsList = dataValues;
        }
        // Remove Characteristic Parent with No Child
        me.validateChildrenTree(initialModelsList, '0');
        initialModelsList = _.filter(initialModelsList, function(model) {
          return model.get('summaryLevel') ? model.get('hasChildren') : true;
        });
        // Set Children List
        _.each(
          initialModelsList,
          function(model) {
            if (model.get('summaryLevel')) {
              model.set(
                'childrenList',
                _.filter(initialModelsList, function(childModel) {
                  return childModel.get('parent') === model.get('id');
                })
              );
              model.set('showChildren', true);
            }
            model.unset('hasChildren');
          },
          this
        );
        me.initialValuesList = initialModelsList;
        me.updateListSelection(initialModelsList);

        // Get First Parent
        modelsList = _.filter(initialModelsList, function(model) {
          return model.get('parent') === '0';
        });
        me.hasSelectedChildrenTree(modelsList);
        me.valuesList.reset(modelsList);
      } else {
        me.valuesList.reset();
        if (
          OB.MobileApp.model.hasPermission('OBPOS_remote.product', true) ||
          forceRemote
        ) {
          OB.UTIL.showWarning(
            OB.I18N.getLabel('OBPOS_NoCharacteriticValue', [
              me.parent.parent.characteristic.get('_identifier')
            ])
          );
        }
      }
    };

    if (
      !OB.MobileApp.model.hasPermission('OBPOS_remote.product', true) &&
      !forceRemote
    ) {
      try {
        const criteria = new OB.App.Class.Criteria()
          .orderBy('name', 'asc')
          .criterion(
            'characteristic_id',
            this.parent.parent.characteristic.get('id')
          )
          .build();
        const characteristics = await OB.App.MasterdataModels.CharacteristicValue.find(
          criteria
        );
        let dataValues = [];
        for (let i = 0; i < characteristics.length; i++) {
          dataValues.push(
            OB.Dal.transform(OB.Model.CharacteristicValue, characteristics[i])
          );
        }
        resetValueList(dataValues);
      } catch (error) {
        OB.UTIL.showError(error);
      }
      return true;
    } else {
      let remoteCriteria = [],
        criteria = {},
        characteristicfilter = {
          columns: ['characteristic_id'],
          operator: 'equals',
          value: this.parent.parent.characteristic.get('id'),
          isId: true
        };
      remoteCriteria.push(characteristicfilter);
      criteria.remoteFilters = remoteCriteria;
      criteria.forceRemote = forceRemote;
      criteria.remoteParams = {};
      criteria.remoteParams.crossStoreSearch =
        crossStoreSearch && crossStoreSearch.checked;
      OB.Dal.find(
        OB.Model.CharacteristicValue,
        criteria,
        function(dataValues) {
          resetValueList(dataValues);
        },
        function(tx, error) {
          OB.UTIL.showError(error);
        },
        this
      );
      return true;
    }
  },
  parentValue: '0',
  setCollection: function(inSender, inEvent) {
    if (inEvent.parentValue === '0') {
      this.parent.parent.$.footer.$.modalProductChFooter.$.backChButton.hide();
    } else {
      this.parent.parent.$.footer.$.modalProductChFooter.$.backChButton.show();
    }
    this.parentValue = inEvent.parentValue;
    this.updateListSelection(inEvent.value);
    this.hasSelectedChildrenTree(inEvent.value);
    this.valuesList.reset(inEvent.value);
  },
  updateListSelection: function(modelsList) {
    var i,
      j,
      filterList = this.parent.parent.model.get('filter'),
      selectedList = this.parent.parent.selected;
    for (i = 0; i < modelsList.length; i++) {
      for (j = 0; j < filterList.length; j++) {
        if (modelsList[i].get('id') === filterList[j].id) {
          modelsList[i].set('checked', true);
          modelsList[i].set('selected', filterList[j].selected);
          if (modelsList[i].get('summaryLevel')) {
            modelsList[i].set('showChildren', false);
          }
          break;
        } else {
          modelsList[i].set('checked', false);
        }
      }
      for (j = 0; j < selectedList.length; j++) {
        if (modelsList[i].get('id') === selectedList[j].id) {
          modelsList[i].set('checked', selectedList[j].get('checked'));
          modelsList[i].set('selected', selectedList[j].get('selected'));
        }
      }
      modelsList[i].set('childrenSelected', null);
    }
  },
  hasSelectedChildrenTree: function(modelsList) {
    var i;
    if (!modelsList || modelsList.length === 0) {
      return false;
    }
    for (i = 0; i < modelsList.length; i++) {
      if (modelsList[i].get('summaryLevel')) {
        if (
          modelsList[i].get('selected') &&
          modelsList[i].get('parent') !== this.parentValue
        ) {
          return true;
        }
        if (this.hasSelectedChildrenTree(modelsList[i].get('childrenList'))) {
          modelsList[i].set('childrenSelected', true);
          return true;
        }
      } else if (
        modelsList[i].get('selected') &&
        modelsList[i].get('parent') !== this.parentValue
      ) {
        return true;
      }
    }
    return false;
  },
  validateChildrenTree: function(initialModelsList, parentValue) {
    var i,
      modelsList,
      exists = false;

    modelsList = _.filter(initialModelsList, function(model) {
      return model.get('parent') === parentValue;
    });
    if (!modelsList || modelsList.length === 0) {
      return false;
    }
    for (i = 0; i < modelsList.length; i++) {
      if (modelsList[i].get('summaryLevel')) {
        if (
          this.validateChildrenTree(initialModelsList, modelsList[i].get('id'))
        ) {
          modelsList[i].set('hasChildren', true);
          exists = true;
        }
      } else {
        exists = true;
      }
    }
    return exists;
  },
  valuesList: null,
  init: function(model) {
    this.valuesList = new Backbone.Collection();
    this.$.valueslistitemprinter.setCollection(this.valuesList);
  }
});

enyo.kind({
  name: 'OB.UI.ModalProductChFooter',
  classes: 'obUiModalProductChFooter',
  events: {
    onHideThisPopup: '',
    onSelectCharacteristicValue: '',
    onGetPrevCollection: ''
  },
  components: [
    {
      classes: 'obUiModalProductChFooter-container1',
      components: [
        {
          classes:
            'obUiModal-footer-secondaryButtons obUiModalProductChFooter-container1-container1',
          components: [
            {
              classes:
                'obUiModalProductChFooter-container1-container1-backChButton',
              name: 'backChButton',
              kind: 'OB.UI.ModalDialogButton',
              i18nContent: 'OBMOBC_LblBack',
              showing: false,
              ontap: 'backAction'
            }
          ]
        }
      ]
    },
    {
      classes:
        'obUiModal-footer-mainButtons obUiModalProductChFooter-container2',
      components: [
        {
          classes: 'obUiModalProductChFooter-container2-container1',
          components: [
            {
              classes:
                'obUiModalProductChFooter-container2-container1-cancelChButton',
              name: 'cancelChButton',
              kind: 'OB.UI.ModalDialogButton',
              i18nContent: 'OBMOBC_LblCancel',
              ontap: 'cancelAction'
            }
          ]
        },
        {
          classes: 'obUiModalProductChFooter-container2-container2',
          components: [
            {
              name: 'doneChButton',
              kind: 'OB.UI.ModalDialogButton',
              i18nContent: 'OBMOBC_LblDone',
              classes:
                'obUiModalProductChFooter-container2-container2-doneChButton',
              isDefaultAction: true,
              ontap: 'doneAction'
            }
          ]
        }
      ]
    }
  ],
  initComponents: function() {
    this.inherited(arguments);
    this.selectedToSend = [];
    this.parent.parent.selected = [];
  },
  backAction: function() {
    this.doGetPrevCollection();
  },
  doneAction: function() {
    var me = this;
    this.countingValues =
      this.countingValues + me.parent.parent.selected.length;
    if (me.parent.parent.selected.length > 0) {
      OB.UTIL.showLoading(true);
      this.inspectTree(me.parent.parent.selected);
    } else {
      this.doHideThisPopup();
    }
  },
  cancelAction: function() {
    this.parent.parent.selected = [];
    this.parent.parent.countedValues = 0;
    this.doHideThisPopup();
  },
  checkFinished: function() {
    var me = this;
    if (this.parent.parent.countedValues === this.countingValues) {
      this.doSelectCharacteristicValue({
        value: me.selectedToSend
      });
      this.parent.parent.selected = [];
      this.selectedToSend = [];
      this.parent.parent.countedValues = 0;
      this.countingValues = 0;
      this.doHideThisPopup();
      OB.UTIL.showLoading(false);
    }
  },
  countingValues: 0,
  inspectTree: function(selected, checkedParent) {
    var aux;
    for (aux = 0; aux < selected.length; aux++) {
      this.getChildren(selected, aux, checkedParent, this);
    }
  },
  getChildren: function(selected, aux, checkedParent, me) {
    var index,
      dataValues = selected[aux].get('childrenList');
    if (dataValues && dataValues.length > 0) {
      if (!_.isUndefined(checkedParent)) {
        selected[aux].set('checked', checkedParent);
      }
      index = me.selectedToSend
        .map(function(e) {
          return e.id;
        })
        .indexOf(selected[aux].id);
      if (index === -1) {
        me.selectedToSend.push(selected[aux]);
      } else if (
        !_.isNull(selected[aux].get('selected')) &&
        !_.isUndefined(selected[aux].get('selected'))
      ) {
        me.selectedToSend[index] = selected[aux];
      }
      if (!_.isUndefined(checkedParent)) {
        me.inspectTree(dataValues, checkedParent);
      } else {
        me.inspectTree(dataValues, selected[aux].get('checked'));
      }
    } else {
      if (!_.isUndefined(checkedParent)) {
        selected[aux].set('checked', checkedParent);
      }
      index = me.selectedToSend
        .map(function(e) {
          return e.id;
        })
        .indexOf(selected[aux].id);
      if (index === -1) {
        me.selectedToSend.push(selected[aux]);
      } else if (
        !_.isNull(selected[aux].get('selected')) &&
        !_.isUndefined(selected[aux].get('selected'))
      ) {
        me.selectedToSend[index] = selected[aux];
      }
      me.countingValues++;
      me.checkFinished();
    }
  }
});

/*Modal definiton*/
enyo.kind({
  name: 'OB.UI.ModalProductCharacteristic',
  classes: 'obUiModalProductCharacteristic',
  kind: 'OB.UI.Modal',
  published: {
    characteristic: null,
    selected: []
  },
  events: {
    onAddToSelected: ''
  },
  handlers: {
    onAddToSelected: 'addToSelected',
    onGetPrevCollection: 'getPrevCollection'
  },
  executeOnShow: function() {
    this.$.body.$.listValues.parentValue = '0';
    this.$.header.parent.addClass('obUiModalProductCharacteristic_border');
    this.$.footer.$.modalProductChFooter.$.backChButton.hide();
    this.characteristic = this.args.model;
    this.setHeader(this.args.model.get('_identifier'));
    this.waterfall('onSearchAction');
  },
  i18nHeader: '',
  body: {
    kind: 'OB.UI.ListValues',
    classes: 'obUiModalProductCharacteristic-body-obUiListValues'
  },
  footer: {
    kind: 'OB.UI.ModalProductChFooter',
    classes: 'obUiModalProductCharacteristic-footer-obUiModalProductChFooter'
  },
  initComponents: function() {
    this.inherited(arguments);
  },
  addToSelected: function(inSender, inEvent) {
    var index = this.selected
      .map(function(e) {
        return e.get('id');
      })
      .indexOf(inEvent.value.get('id'));
    if (!inEvent.checked) {
      inEvent.value.set('childrenSelected', false);
      this.inspectDeselectTree([inEvent.value], inEvent.value);
    }
    if (index !== -1) {
      inEvent.value.set('checked', inEvent.checked);
      inEvent.value.set('selected', inEvent.selected);
      this.selected[index].set('checked', inEvent.checked);
      this.selected[index].set('selected', inEvent.selected);
    } else {
      inEvent.value.set('checked', inEvent.checked);
      inEvent.value.set('selected', inEvent.selected);
      this.selected.push(inEvent.value);
      this.inspectCountTree([inEvent.value]);
      this.countedValues++;
    }
  },
  getPrevCollection: function(inSender, inEvent) {
    var me = this,
      dataValues;
    dataValues = _.filter(
      this.$.body.$.listValues.initialValuesList,
      function(model) {
        return (
          model.get('parent') ===
          _.find(me.$.body.$.listValues.initialValuesList, function(l) {
            return l.get('id') === me.$.body.$.listValues.parentValue;
          }).get('parent')
        );
      },
      this
    );

    if (dataValues && dataValues.length > 0) {
      //We take the first to know the parent
      this.$.body.$.listValues.parentValue = dataValues[0].get('parent');
      this.$.body.$.listValues.updateListSelection(dataValues);
      this.$.body.$.listValues.hasSelectedChildrenTree(dataValues);
      this.$.body.$.listValues.valuesList.reset(dataValues);
    } else {
      this.$.body.$.listValues.parentValue = '0';
      this.$.body.$.listValues.valuesList.reset();
    }
    if (this.$.body.$.listValues.parentValue === '0') {
      //root
      this.$.footer.$.modalProductChFooter.$.backChButton.hide();
    } else {
      this.$.footer.$.modalProductChFooter.$.backChButton.show();
    }
  },
  countedValues: 0,
  inspectCountTree: function(selected) {
    var aux;
    for (aux = 0; aux < selected.length; aux++) {
      this.countChildren(selected, aux, this);
    }
  },
  countChildren: function(selected, aux, me) {
    var dataValues = selected[aux].get('childrenList');
    if (dataValues && dataValues.length > 0) {
      me.inspectCountTree(dataValues);
    } else {
      me.countedValues++;
    }
  },
  inspectDeselectTree: function(selected, rootObject) {
    var aux;
    for (aux = 0; aux < selected.length; aux++) {
      this.deselectChildren(selected, aux, rootObject, this);
    }
  },
  deselectChildren: function(selected, aux, rootObject, me) {
    var index,
      dataValues = selected[aux].get('childrenList');
    index = me.selected
      .map(function(e) {
        return e.id;
      })
      .indexOf(selected[aux].id);
    if (
      !rootObject.get('selected') &&
      rootObject.get('id') !== selected[aux].get('id')
    ) {
      selected[aux].set('selected', rootObject.get('selected'));
      if (index === -1) {
        me.doAddToSelected({
          value: selected[aux],
          checked: selected[aux].get('checked'),
          selected: selected[aux].get('selected')
        });
      } else {
        me.selected[index] = selected[aux];
      }
    }
    if (dataValues && dataValues.length > 0) {
      me.inspectDeselectTree(dataValues, rootObject);
    }
  },
  init: function(model) {
    this.model = model;
    this.waterfall('onSetModel', {
      model: this.model
    });
  }
});
