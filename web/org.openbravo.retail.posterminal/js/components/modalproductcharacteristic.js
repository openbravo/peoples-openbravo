/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, _ */

/*header of scrollable table*/
enyo.kind({
  name: 'OB.UI.ModalProductChHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  events: {
    onSearchAction: '',
    onClearAction: '',
    onGetPrevCollection: ''
  },
  handlers: {
    onSearchActionByKey: 'searchAction',
    onFiltered: 'searchAction'
  },
  components: [{
    style: 'padding: 10px;',
    components: [{
      style: 'display: table;',
      components: [{
        style: 'display: table-cell; width: 100%;',
        components: [{
          kind: 'OB.UI.SearchInputAutoFilter',
          name: 'filterText',
          style: 'width: 100%',
          isFirstFocus: true
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.SmallButton',
          classes: 'btnlink-gray btn-icon-small btn-icon-clear',
          style: 'width: 100px; margin: 0px 5px 8px 19px;',
          ontap: 'clearAction'
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.SmallButton',
          classes: 'btnlink-yellow btn-icon-small btn-icon-search',
          style: 'width: 100px; margin: 0px 0px 8px 5px;',
          ontap: 'searchAction'
        }]
      }]
    }, {
      style: 'margin: 0px;',
      classes: 'btnlink-gray',
      name: 'backChButton',
      kind: 'OB.UI.SmallButton',
      showing: false,
      ontap: 'backAction'
    }]
  }],
  clearAction: function () {
    this.$.filterText.setValue('');
    this.doClearAction();
  },
  searchAction: function () {
    this.doSearchAction({
      valueName: this.$.filterText.getValue()
    });
    return true;
  },
  backAction: function () {
    this.doGetPrevCollection();
  },
  initComponents: function () {
    this.inherited(arguments);
    this.$.backChButton.setContent(OB.I18N.getLabel('OBMOBC_LblBack'));
  }
});

/*items of collection*/
enyo.kind({
  name: 'OB.UI.ListValuesLineCheck',
  kind: 'OB.UI.CheckboxButton',
  classes: 'modal-dialog-btn-check',
  style: 'width: 86%; text-align: left; padding-left: 70px;',
  events: {
    onAddToSelected: ''
  },
  tap: function () {
    this.inherited(arguments);
    var me = this;
    this.doAddToSelected({
      value: me.parent.model,
      checked: !this.parent.model.get('checked')
    });
  },
  create: function () {
    this.inherited(arguments);
    this.setContent(this.parent.model.get('name'));
    if (this.parent.model.get('selected')) {
      this.addClass('active');
    } else {
      this.removeClass('active');
    }
  }
});
enyo.kind({
  name: 'OB.UI.ListValuesLineChildren',
  kind: 'OB.UI.SmallButton',
  classes: 'btnlink-yellow btn-icon-small btn-icon-search',
  style: 'width: 10%; margin: 0px;',
  showing: false,
  childrenArray: [],
  events: {
    onSetCollection: ''
  },
  create: function () {
    this.inherited(arguments);
    var me = this;
    OB.Dal.query(OB.Model.ProductChValue, "select distinct(id), name, characteristic_id from m_ch_value where parent = '" + this.parent.model.get('id') + "'", [], function (dataValues, me) {
      if (dataValues && dataValues.length > 0) {
        me.childrenArray = dataValues.models;
        me.show();
      }
    }, function (tx, error) {
      OB.UTIL.showError("OBDAL error: " + error);
    }, this);
  },
  tap: function () {
    this.doSetCollection({
      value: this.childrenArray,
      parentValue: this.parent.model.get('id')
    });
  }
});
enyo.kind({
  name: 'OB.UI.ListValuesLine',
  style: 'border-bottom: 1px solid #cccccc',
  components: [{
    kind: 'OB.UI.ListValuesLineCheck'
  }, {
    kind: 'OB.UI.ListValuesLineChildren'
  }]
});

/*scrollable table (body of modal)*/
enyo.kind({
  name: 'OB.UI.ListValues',
  classes: 'row-fluid',
  handlers: {
    onSearchAction: 'searchAction',
    onClearAction: 'clearAction',
    onSetCollection: 'setCollection',
    onGetPrevCollection: 'getPrevCollection'
  },
  components: [{
    classes: 'span12',
    components: [{
      classes: 'row-fluid',
      components: [{
        classes: 'span12',
        components: [{
          name: 'valueslistitemprinter',
          kind: 'OB.UI.ScrollableTable',
          scrollAreaMaxHeight: '400px',
          renderHeader: 'OB.UI.ModalProductChHeader',
          renderLine: 'OB.UI.ListValuesLine',
          renderEmpty: 'OB.UI.RenderEmpty'
        }]
      }]
    }]
  }],
  clearAction: function (inSender, inEvent) {
    this.valuesList.reset();
    return true;
  },
  searchAction: function (inSender, inEvent) {
    var me = this,
        i, j, whereClause = '',
        params = [],
        filter = inEvent.valueName;
    params.push(this.parent.parent.characteristic.get('characteristic_id'));
    if (filter) {
      whereClause = whereClause + ' and name like ?';
      params.push('%' + filter + '%');
    }
    OB.Dal.query(OB.Model.ProductChValue, "select distinct(id), name, characteristic_id, parent from m_ch_value where parent = '" + this.parentValue + "' and characteristic_id = ?" + whereClause, params, function (dataValues, me) {
      if (dataValues && dataValues.length > 0) {
        for (i = 0; i < dataValues.length; i++) {
          for (j = 0; j < me.parent.parent.model.get('filter').length; j++) {
            if (dataValues.models[i].get('id') === me.parent.parent.model.get('filter')[j].id) {
              dataValues.models[i].set('checked', true);
              dataValues.models[i].set('selected', me.parent.parent.model.get('filter')[j].selected);
              break;
            } else {
              dataValues.models[i].set('checked', false);
            }
          }
        }
        me.parent.parent.$.body.$.listValues.valuesList.reset(dataValues.models);
      } else {
        me.parent.parent.$.body.$.listValues.valuesList.reset();
      }
    }, function (tx, error) {
      OB.UTIL.showError("OBDAL error: " + error);
    }, this);
    return true;
  },
  parentValue: 0,
  setCollection: function (inSender, inEvent) {
    var i, j, k;
    if (inEvent.parentValue !== 0) {
      this.$.valueslistitemprinter.$.theader.$.modalProductChHeader.$.backChButton.show();
    }

    this.parentValue = inEvent.parentValue;
    for (i = 0; i < inEvent.value.length; i++) {
      for (j = 0; j < this.parent.parent.model.get('filter').length; j++) {
        if (inEvent.value[i].get('id') === this.parent.parent.model.get('filter')[j].id) {
          inEvent.value[i].set('checked', this.parent.parent.model.get('filter')[j].cheked);
          inEvent.value[i].set('selected', this.parent.parent.model.get('filter')[j].selected);
        }
      }
      for (k = 0; k < this.parent.parent.selected.length; k++) {
        if (inEvent.value[i].get('id') === this.parent.parent.selected[k].id) {
          inEvent.value[i].set('checked', this.parent.parent.selected[k].get('cheked'));
          inEvent.value[i].set('selected', this.parent.parent.selected[k].get('selected'));
        }
      }
    }
    this.valuesList.reset(inEvent.value);
  },
  getPrevCollection: function (inSender, inEvent) {
    var me = this;
    OB.Dal.query(OB.Model.ProductChValue, "select distinct(id) , name , characteristic_id, parent as parent from m_ch_value " + "where parent = (select parent from m_ch_value where id = '" + this.parentValue + "') and " + "characteristic_id = (select characteristic_id from m_ch_value where id = '" + this.parentValue + "')", [], function (dataValues, me) {
      if (dataValues && dataValues.length > 0) {
        me.valuesList.reset(dataValues.models);
        //We take the first to know the parent
        me.parentValue = dataValues.models[0].get('parent');
        if (me.parentValue === 0) {
          me.$.valueslistitemprinter.$.theader.$.modalProductChHeader.$.backChButton.hide();
        }
      }
    }, function (tx, error) {
      OB.UTIL.showError("OBDAL error: " + error);
    }, this);
  },
  valuesList: null,
  init: function (model) {
    this.valuesList = new Backbone.Collection();
    this.$.valueslistitemprinter.setCollection(this.valuesList);
  }
});

enyo.kind({
  name: 'OB.UI.ModalProductChTopHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  events: {
    onHideThisPopup: '',
    onSelectCharacteristicValue: ''
  },
  components: [{
    style: 'display: table;',
    components: [{
      style: 'display: table-cell; float:left',
      name: 'doneChButton',
      kind: 'OB.UI.SmallButton',
      ontap: 'doneAction'
    }, {
      name: 'title',
      style: 'display: table-cell; width: 100%; text-align: center; vertical-align: middle'
    }, {
      style: 'display: table-cell; float:right',
      classes: 'btnlink-gray',
      name: 'cancelChButton',
      kind: 'OB.UI.SmallButton',
      ontap: 'cancelAction'
    }]
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.doneChButton.setContent(OB.I18N.getLabel('OBMOBC_LblDone'));
    this.$.cancelChButton.setContent(OB.I18N.getLabel('OBMOBC_LblCancel'));
    this.selectedToSend = [];
  },
  doneAction: function () {
    var me = this;
    this.countingValues = this.countingValues + me.parent.parent.parent.selected.length;
    if (me.parent.parent.parent.selected.length > 0) {
      this.inspectTree(me.parent.parent.parent.selected);
      OB.UTIL.showLoading(true);
    } else {
      this.doHideThisPopup();
    }
  },
  cancelAction: function () {
    this.doHideThisPopup();
  },
  checkFinished: function () {
    var me = this;
    if (this.parent.parent.parent.countedValues === this.countingValues) {
      this.doSelectCharacteristicValue({
        value: me.selectedToSend
      });
      this.parent.parent.parent.selected = [];
      this.selectedToSend = [];
      this.parent.parent.parent.countedValues = 0;
      this.countingValues = 0;
      OB.UTIL.showLoading(false);
      this.doHideThisPopup();
    }
  },
  countingValues: 0,
  inspectTree: function (selected, checkedParent) {
    var aux;
    for (aux = 0; aux < selected.length; aux++) {
      this.getChildren(selected, aux, checkedParent, this);
    }
  },
  getChildren: function (selected, aux, checkedParent, me) {
    OB.Dal.query(OB.Model.ProductChValue, "select distinct(id), name, characteristic_id, parent " + "from m_ch_value where parent = '" + selected[aux].get('id') + "' ", [], function (dataValues, me) {
      if (dataValues && dataValues.length > 0) {
        me.selectedToSend.push(selected[aux]);
        if (!_.isUndefined(checkedParent)) {
          me.inspectTree(dataValues.models, checkedParent);
        } else {
          me.inspectTree(dataValues.models, selected[aux].get('checked'));
        }
      } else {
        if (!_.isUndefined(checkedParent)) {
          selected[aux].set('checked', checkedParent);
        }
        me.selectedToSend.push(selected[aux]);
        me.countingValues++;
        me.checkFinished();
      }
    }, function (tx, error) {
      OB.UTIL.showError("OBDAL error: " + error);
    }, this);
  }

});

/*Modal definiton*/
enyo.kind({
  name: 'OB.UI.ModalProductCharacteristic',
  topPosition: '125px',
  kind: 'OB.UI.Modal',
  published: {
    characteristic: null,
    selected: []
  },
  handlers: {
    onAddToSelected: 'addToSelected'
  },
  executeOnHide: function () {
    this.$.body.$.listValues.$.valueslistitemprinter.$.theader.$.modalProductChHeader.clearAction();
  },
  executeOnShow: function () {
    var i, j;
    this.characteristic = this.args.model;
    this.$.header.$.modalProductChTopHeader.$.title.setContent(this.args.model.get('_identifier'));
    this.waterfall('onSearchAction', {
      valueName: this.$.body.$.listValues.$.valueslistitemprinter.$.theader.$.modalProductChHeader.$.filterText.getValue()
    });
  },
  i18nHeader: '',
  body: {
    kind: 'OB.UI.ListValues'
  },
  initComponents: function () {
    this.inherited(arguments);
    this.$.closebutton.hide();
    this.$.header.createComponent({
      kind: 'OB.UI.ModalProductChTopHeader'
    });
  },
  addToSelected: function (inSender, inEvent) {
    var index = this.selected.map(function (e) {
      return e.get('id');
    }).indexOf(inEvent.value.get('id'));
    if (index !== -1) {
      inEvent.value.set('checked', inEvent.checked);
      inEvent.value.set('selected', inEvent.checked);
      this.selected[index].set('checked', inEvent.checked);
      this.selected[index].set('selected', inEvent.checked);
    } else {
      inEvent.value.set('checked', inEvent.checked);
      inEvent.value.set('selected', inEvent.checked);
      this.selected.push(inEvent.value);
      this.inspectCountTree([inEvent.value]);
      this.countedValues++;
    }

  },
  countedValues: 0,
  inspectCountTree: function (selected) {
    var aux;
    for (aux = 0; aux < selected.length; aux++) {
      this.countChildren(selected, aux, this);
    }
  },
  countChildren: function (selected, aux, me) {
    OB.Dal.query(OB.Model.ProductChValue, "select distinct(id), name, characteristic_id, parent " + "from m_ch_value where parent = '" + selected[aux].get('id') + "' ", [], function (dataValues, me) {
      if (dataValues && dataValues.length > 0) {
        me.inspectCountTree(dataValues.models);
      } else {
        me.countedValues++;
      }
    }, function (tx, error) {
      OB.UTIL.showError("OBDAL error: " + error);
    }, this);
  },
  init: function (model) {
    this.model = model;
    this.waterfall('onSetModel', {
      model: this.model
    });
  }
});