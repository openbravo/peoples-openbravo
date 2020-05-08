/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, Backbone, _ */

enyo.kind({
  kind: 'OB.UI.FormElement.Selector',
  name: 'OB.UI.SalesRepresentative',
  classes: 'obUiSalesRepresentative',
  published: {
    order: null
  },
  events: {
    onShowPopup: ''
  },
  tap: function() {
    if (!this.disabled) {
      this.doShowPopup({
        popup: 'modalsalesrepresentative'
      });
    }
  },
  init: function(model) {
    if (!OB.MobileApp.model.hasPermission(this.permission)) {
      this.parent.parent.parent.parent.hide();
    } else {
      if (!OB.MobileApp.model.hasPermission(this.permissionOption, true)) {
        this.parent.parent.parent.parent.hide();
      }
    }
    this.setOrder(model.get('order'));
  },
  renderSalesRepresentative: function(newSalesRepresentative) {
    this.setContent(newSalesRepresentative);
  },
  orderChanged: function(oldValue) {
    if (this.order.get('salesRepresentative')) {
      this.renderSalesRepresentative(this.order.get('salesRepresentative'));
    } else {
      this.renderSalesRepresentative('');
    }

    this.order.on(
      'change:salesRepresentative$_identifier change:salesRepresentative',
      function(model) {
        if (
          !_.isUndefined(model.get('salesRepresentative$_identifier')) &&
          !_.isNull(model.get('salesRepresentative$_identifier'))
        ) {
          this.renderSalesRepresentative(
            model.get('salesRepresentative$_identifier')
          );
        } else {
          this.renderSalesRepresentative('');
        }
      },
      this
    );
  }
});

/*Modal*/

enyo.kind({
  name: 'OB.UI.ModalSrScrollableHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  classes: 'obUiModalSrScrollableHeader',
  events: {
    onSearchAction: '',
    onClearAction: ''
  },
  handlers: {
    onSearchActionByKey: 'searchAction',
    onFiltered: 'searchAction'
  },
  components: [
    {
      classes: 'obUiModalSrScrollableHeader-container1',
      components: [
        {
          classes: 'obUiModalSrScrollableHeader-container1-container1',
          components: [
            {
              classes:
                'obUiModalSrScrollableHeader-container1-container1-container1',
              components: [
                {
                  kind: 'OB.UI.SearchInputAutoFilter',
                  name: 'filterText',
                  classes:
                    'obUiModalSrScrollableHeader-container1-container1-container1-filterText'
                }
              ]
            },
            {
              classes:
                'obUiModalSrScrollableHeader-container1-container1-container2',
              components: [
                {
                  kind: 'OB.UI.SmallButton',
                  classes:
                    'obUiModalSrScrollableHeader-container1-container1-container2-obUiSmallButton',
                  ontap: 'clearAction'
                }
              ]
            },
            {
              classes:
                'obUiModalSrScrollableHeader-container1-container1-container3',
              components: [
                {
                  kind: 'OB.UI.SmallButton',
                  classes:
                    'obUiModalSrScrollableHeader-container1-container1-container3-obUiSmallButton',
                  ontap: 'searchAction'
                }
              ]
            }
          ]
        }
      ]
    }
  ],
  clearAction: function() {
    this.$.filterText.setValue('');
    this.doClearAction();
  },
  searchAction: function() {
    this.doSearchAction({
      srName: this.$.filterText.getValue()
    });
    return true;
  }
});

/*items of collection*/
enyo.kind({
  name: 'OB.UI.ListSrsLine',
  kind: 'OB.UI.SelectButton',
  classes: 'obUiListSrsLine',
  components: [
    {
      name: 'line',
      classes: 'obUiListSrsLine-line',
      components: [
        {
          name: 'name',
          classes: 'obUiListSrsLine-line-name'
        },
        {
          classes: 'obUiListSrsLine-line-element2 u-clearBoth'
        }
      ]
    }
  ],
  events: {
    onHideThisPopup: ''
  },
  tap: function() {
    this.inherited(arguments);
    this.doHideThisPopup();
  },
  create: function() {
    this.inherited(arguments);
    this.$.name.setContent(this.model.get('name'));
  }
});

/*scrollable table (body of modal)*/
enyo.kind({
  name: 'OB.UI.ListSrs',
  classes: 'obUiListSrs row-fluid',
  handlers: {
    onSearchAction: 'searchAction',
    onClearAction: 'clearAction'
  },
  events: {
    onChangeSalesRepresentative: ''
  },
  components: [
    {
      classes: 'obUiListSrs-container1 span12',
      components: [
        {
          classes: 'obUiListSrs-container1-container1 row-fluid',
          components: [
            {
              classes: 'obUiListSrs-container1-container1-container1 span12',
              components: [
                {
                  name: 'srslistitemprinter',
                  kind: 'OB.UI.ScrollableTable',
                  classes:
                    'obUiListSrs-container1-container1-container1-srslistitemprinter',
                  renderHeader: 'OB.UI.ModalSrScrollableHeader',
                  renderLine: 'OB.UI.ListSrsLine',
                  renderEmpty: 'OB.UI.RenderEmpty'
                }
              ]
            }
          ]
        }
      ]
    }
  ],
  clearAction: function(inSender, inEvent) {
    this.srsList.reset();
    return true;
  },
  searchAction: async function(inSender, inEvent) {
    var me = this,
      filter = inEvent.srName;

    function errorCallback(tx, error) {
      OB.UTIL.showError(error);
    }

    function successCallbackBPs(dataSrs) {
      if (dataSrs && dataSrs.length > 0) {
        dataSrs.unshift({
          id: null,
          _identifier: null,
          name: OB.I18N.getLabel('OBPOS_None')
        });
        me.srsList.reset(dataSrs);
      } else {
        me.srsList.reset();
      }
    }
    const criteria = new OB.App.Class.Criteria()
      .orderBy('_identifier')
      .criterion('_identifier', filter, 'includes')
      .build();
    try {
      const dataSalesRepresentative = await OB.App.MasterdataModels.SalesRepresentative.find(
        criteria
      );
      successCallbackBPs(dataSalesRepresentative);
    } catch (err) {
      errorCallback(undefined, err.message);
    }

    return true;
  },
  srsList: null,
  init: function(model) {
    this.srsList = new Backbone.Collection();
    this.$.srslistitemprinter.setCollection(this.srsList);
    this.srsList.on(
      'click',
      function(model) {
        if (model.attributes.name === OB.I18N.getLabel('OBPOS_None')) {
          model.attributes.name = null;
        }
        this.doChangeSalesRepresentative({
          salesRepresentative: model
        });
      },
      this
    );
  }
});

/*Modal definiton*/
enyo.kind({
  name: 'OB.UI.ModalSalesRepresentative',
  topPosition: '125px',
  kind: 'OB.UI.Modal',
  classes: 'obUiModalSalesRepresentative',
  executeOnHide: function() {
    this.$.body.$.listSrs.$.srslistitemprinter.$.theader.$.modalSrScrollableHeader.clearAction();
  },
  i18nHeader: 'OBPOS_LblAssignSalesRepresentative',
  body: {
    kind: 'OB.UI.ListSrs',
    classes: 'obUiModalSalesRepresentative-body-obUiListSrs'
  },
  init: function(model) {
    this.model = model;
    this.waterfall('onSetModel', {
      model: this.model
    });
  }
});
