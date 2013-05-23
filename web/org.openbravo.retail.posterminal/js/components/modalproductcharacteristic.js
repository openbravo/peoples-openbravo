/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, _ */

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
    if (!this.parent.model.get('childrenSelected')) {
      this.removeClass('half-active');
    }
    this.doAddToSelected({
      value: me.parent.model,
      checked: !this.parent.model.get('checked'),
      selected: !this.parent.model.get('selected')
    });
  },
  create: function () {
    this.inherited(arguments);
    this.setContent(this.parent.model.get('name'));
    if (this.parent.model.get('selected')) {
      this.addClass('active');
    } else {
      this.removeClass('active');
      if (this.parent.model.get('childrenSelected')) {
        this.addClass('half-active');
      }
    }
  }
});
enyo.kind({
  name: 'OB.UI.ListValuesLineChildren',
  kind: 'OB.UI.Button',
  classes: 'btn-icon-inspectTree',
  style: 'width: 10%; margin: 0px; ',
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
    onSetCollection: 'setCollection'
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
        params = [];
    params.push(this.parent.parent.characteristic.get('characteristic_id'));
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
            dataValues.models[i].set('childrenSelected', null);
            me.hasSelectedChildrenTree([dataValues.models[i]], dataValues.models[i]);
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
      this.parent.parent.$.header.$.modalProductChTopHeader.$.backChButton.addStyles('visibility: visible');
    }

    this.parentValue = inEvent.parentValue;
    for (i = 0; i < inEvent.value.length; i++) {
      for (j = 0; j < this.parent.parent.model.get('filter').length; j++) {
        if (inEvent.value[i].get('id') === this.parent.parent.model.get('filter')[j].id) {
          inEvent.value[i].set('checked', this.parent.parent.model.get('filter')[j].selected);
          inEvent.value[i].set('selected', this.parent.parent.model.get('filter')[j].selected);
        }
      }
      for (k = 0; k < this.parent.parent.selected.length; k++) {
        if (inEvent.value[i].get('id') === this.parent.parent.selected[k].id) {
          inEvent.value[i].set('checked', this.parent.parent.selected[k].get('checked'));
          inEvent.value[i].set('selected', this.parent.parent.selected[k].get('selected'));
        }
      }
      inEvent.value[i].set('childrenSelected', null);
      this.hasSelectedChildrenTree([inEvent.value[i]], inEvent.value[i]);
    }
    this.valuesList.reset(inEvent.value);
  },
  hasSelectedChildrenTree: function (selected, rootObject) {
    var aux;
    for (aux = 0; aux < selected.length; aux++) {
      this.hasSelectedChildren(selected, aux, rootObject, this);
    }
  },
  hasSelectedChildren: function (selected, aux, rootObject, me) {
    var j, k;
    me.exist = null;
    me.selected = selected;
    me.aux = aux;
    me.rootObject = rootObject;
    OB.Dal.query(OB.Model.ProductChValue, "select distinct(id), name, characteristic_id, parent " + "from m_ch_value where parent = '" + selected[aux].get('id') + "' ", [], function (dataValues, me) {
      for (j = 0; j < me.parent.parent.model.get('filter').length; j++) {
        if (selected[aux].id === me.parent.parent.model.get('filter')[j].id && selected[aux].id !== rootObject.id && me.parent.parent.model.get('filter')[j].selected) {
          me.exist = true;
          break;
        }
      }
      for (k = 0; k < me.parent.parent.selected.length; k++) {
        if (selected[aux].id === me.parent.parent.selected[k].id && selected[aux].id !== rootObject.id) {
          me.exist = me.parent.parent.selected[k].get('selected');
          break;
        }
      }
      if (_.isNull(rootObject.get('childrenSelected')) || !rootObject.get('childrenSelected')) {
        rootObject.set('childrenSelected', me.exist);
      }
      me.exist = null;
      if (dataValues && dataValues.length > 0) {
        me.hasSelectedChildrenTree(dataValues.models, rootObject);
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
  style: '',
  events: {
    onHideThisPopup: '',
    onSelectCharacteristicValue: '',
    onGetPrevCollection: ''
  },
  components: [{
    style: 'display: table;  width: 100%;',
    components: [{
      style: 'display: table-cell; ',
      components: [{
        classes: 'btnlink-gray',
        name: 'backChButton',
        kind: 'OB.UI.SmallButton',
        ontap: 'backAction'
      }]
    }, {
      style: 'display: table-cell; width: 55%;',
      components: [{
        name: 'title',
        style: 'text-align: center; vertical-align: middle;'
      }]
    }, {
      style: 'display: table-cell; ',
      components: [{
        name: 'doneChButton',
        kind: 'OB.UI.SmallButton',
        ontap: 'doneAction'
      }]
    }, {
      style: 'display: table-cell;',
      components: [{
        classes: 'btnlink-gray',
        name: 'cancelChButton',
        kind: 'OB.UI.SmallButton',
        ontap: 'cancelAction'
      }]
    }]
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.backChButton.setContent(OB.I18N.getLabel('OBMOBC_LblBack'));
    this.$.backChButton.addStyles('visibility: hidden');
    this.$.doneChButton.setContent(OB.I18N.getLabel('OBMOBC_LblDone'));
    this.$.cancelChButton.setContent(OB.I18N.getLabel('OBMOBC_LblCancel'));
    this.selectedToSend = [];
  },
  backAction: function () {
    this.doGetPrevCollection();
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
    this.parent.parent.parent.selected = [];
    this.parent.parent.parent.countedValues = 0;
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
      var index;
      if (dataValues && dataValues.length > 0) {
        if (!_.isUndefined(checkedParent)) {
          selected[aux].set('checked', checkedParent);
        }
        index = me.selectedToSend.map(function (e) {
          return e.id;
        }).indexOf(selected[aux].id);
        if (index === -1) {
          me.selectedToSend.push(selected[aux]);
        } else if (!_.isNull(selected[aux].get('selected')) && !_.isUndefined(selected[aux].get('selected'))) {
          me.selectedToSend[index] = selected[aux];
        }
        if (!_.isUndefined(checkedParent)) {
          me.inspectTree(dataValues.models, checkedParent);
        } else {
          me.inspectTree(dataValues.models, selected[aux].get('checked'));
        }
      } else {
        if (!_.isUndefined(checkedParent)) {
          selected[aux].set('checked', checkedParent);
        }
        index = me.selectedToSend.map(function (e) {
          return e.id;
        }).indexOf(selected[aux].id);
        if (index === -1) {
          me.selectedToSend.push(selected[aux]);
        } else if (!_.isNull(selected[aux].get('selected')) && !_.isUndefined(selected[aux].get('selected'))) {
          me.selectedToSend[index] = selected[aux];
        }
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
  topPosition: '170px',
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
  executeOnShow: function () {
    var i, j;
    this.$.body.$.listValues.parentValue = 0;
    this.$.header.parent.addStyles('padding: 0px; border-bottom: 1px solid #cccccc');
    this.$.header.$.modalProductChTopHeader.$.backChButton.addStyles('visibility: hidden');
    this.characteristic = this.args.model;
    this.$.header.$.modalProductChTopHeader.$.title.setContent(this.args.model.get('_identifier'));
    this.waterfall('onSearchAction');
  },
  i18nHeader: '',
  body: {
    kind: 'OB.UI.ListValues'
  },
  initComponents: function () {
    this.inherited(arguments);
    this.$.closebutton.hide();
    this.$.header.createComponent({
      kind: 'OB.UI.ModalProductChTopHeader',
      style: 'border-bottom: 0px'
    });
  },
  addToSelected: function (inSender, inEvent) {
    var index = this.selected.map(function (e) {
      return e.get('id');
    }).indexOf(inEvent.value.get('id'));
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
  getPrevCollection: function (inSender, inEvent) {
    var me = this,
        i, j, k;
    OB.Dal.query(OB.Model.ProductChValue, "select distinct(id) , name , characteristic_id, parent as parent from m_ch_value " + "where parent = (select parent from m_ch_value where id = '" + this.$.body.$.listValues.parentValue + "') and " + "characteristic_id = (select characteristic_id from m_ch_value where id = '" + this.$.body.$.listValues.parentValue + "')", [], function (dataValues, me) {
      if (dataValues && dataValues.length > 0) {
        for (i = 0; i < dataValues.length; i++) {
          for (j = 0; j < me.model.get('filter').length; j++) {
            if (dataValues.models[i].get('id') === me.model.get('filter')[j].id) {
              dataValues.models[i].set('checked', me.model.get('filter')[j].checked);
              dataValues.models[i].set('selected', me.model.get('filter')[j].selected);
            }
          }
          for (k = 0; k < me.selected.length; k++) {
            if (dataValues.models[i].get('id') === me.selected[k].id) {
              dataValues.models[i].set('checked', me.selected[k].get('checked'));
              dataValues.models[i].set('selected', me.selected[k].get('selected'));
            }
          }
          dataValues.models[i].set('childrenSelected', null);
          me.$.body.$.listValues.hasSelectedChildrenTree([dataValues.models[i]], dataValues.models[i]);
        }
        me.$.body.$.listValues.valuesList.reset(dataValues.models);
        //We take the first to know the parent
        me.$.body.$.listValues.parentValue = dataValues.models[0].get('parent');
        if (me.$.body.$.listValues.parentValue === '0') { //root
          me.$.header.$.modalProductChTopHeader.$.backChButton.addStyles('visibility: hidden');
        }
      }
    }, function (tx, error) {
      OB.UTIL.showError("OBDAL error: " + error);
    }, this);
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
  inspectDeselectTree: function (selected, rootObject) {
    var aux;
    for (aux = 0; aux < selected.length; aux++) {
      this.deselectChildren(selected, aux, rootObject, this);
    }
  },
  deselectChildren: function (selected, aux, rootObject, me) {
    OB.Dal.query(OB.Model.ProductChValue, "select distinct(id), name, characteristic_id, parent " + "from m_ch_value where parent = '" + selected[aux].get('id') + "' ", [], function (dataValues, me) {
      var index = me.selected.map(function (e) {
        return e.id;
      }).indexOf(selected[aux].id);
      if (!rootObject.get('selected') && rootObject.get('id') !== selected[aux].get('id')) {
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
        me.inspectDeselectTree(dataValues.models, rootObject);
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