/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone */

/*header of scrollable table*/
enyo.kind({
  name: 'OB.UI.ModalProductChHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  events: {
    onSearchAction: '',
    onClearAction: ''
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
  }
});

/*items of collection*/
enyo.kind({
  name: 'OB.UI.ListValuesLine',
  kind: 'OB.UI.CheckboxButton',
  classes: 'modal-dialog-btn-check',
  style: 'border-bottom: 1px solid #cccccc;text-align: left; padding-left: 70px;',
  events: {
    onHideThisPopup: ''
  },
  tap: function () {
    this.inherited(arguments);
    this.model.set('checked', !this.model.get('checked'));
  },
  create: function () {
    this.inherited(arguments);
    this.setContent(this.model.get('ch_value'));
    if (this.model.get('checked')) {
      this.addClass('active');
    } else {
      this.removeClass('active');
    }
  }
});

/*scrollable table (body of modal)*/
enyo.kind({
  name: 'OB.UI.ListValues',
  classes: 'row-fluid',
  handlers: {
    onSearchAction: 'searchAction',
    onClearAction: 'clearAction'
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
      whereClause = whereClause + ' and ch_value like ?';
      params.push('%' + filter + '%');
    }
    OB.Dal.query(OB.Model.ProductCharacteristic, 'select distinct(ch_value_id), ch_value, characteristic_id from m_product_ch where characteristic_id = ?' + whereClause, params, function (dataValues, me) {
      if (dataValues && dataValues.length > 0) {
        for (i = 0; i < dataValues.length; i++) {
          for (j = 0; j < me.parent.parent.model.get('filter').length; j++) {
            if (dataValues.models[i].get('ch_value_id') === me.parent.parent.model.get('filter')[j].ch_value_id) {
              dataValues.models[i].set('checked', true);
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
    this.$.doneChButton.setContent('Done');
    this.$.cancelChButton.setContent('Cancel');
  },
  doneAction: function () {
    var selectedValues = _.compact(this.parent.parent.parent.$.body.$.listValues.valuesList.map(function (e) {
      return e;
    }));
    this.doSelectCharacteristicValue({
      value: selectedValues
    });
    this.doHideThisPopup();
  },
  cancelAction: function () {
    this.doHideThisPopup();
  }
});

/*Modal definiton*/
enyo.kind({
  name: 'OB.UI.ModalProductCharacteristic',
  topPosition: '125px',
  kind: 'OB.UI.Modal',
  published: {
    characteristic: null
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
  init: function (model) {
    this.model = model;
    this.waterfall('onSetModel', {
      model: this.model
    });
  }
});