/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, OB */

enyo.kind({
  kind: 'OB.UI.SmallButton',
  name: 'OB.UI.BusinessPartner',
  classes: 'obUiBusinessPartner',
  published: {
    order: null
  },
  events: {
    onShowPopup: ''
  },
  handlers: {
    onBPSelectionDisabled: 'buttonDisabled'
  },
  buttonDisabled: function(inSender, inEvent) {
    this.isEnabled = !inEvent.status;
    this.setDisabled(inEvent.status);
    if (!this.isEnabled) {
      this.removeClass('obUiBusinessPartner_enabled');
      this.addClass('obUiBusinessPartner_disabled');
    } else {
      this.removeClass('obUiBusinessPartner_disabled');
      this.addClass('obUiBusinessPartner_enabled');
    }
  },
  tap: function() {
    var qty = 0;
    enyo.forEach(this.order.get('lines').models, function(l) {
      if (l.get('originalOrderLineId')) {
        qty = qty + 1;
        return;
      }
    });
    if (
      qty !== 0 &&
      !OB.MobileApp.model.hasPermission(
        'OBPOS_AllowChangeCustomerVerifiedReturns',
        true
      )
    ) {
      OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_Cannot_Change_BPartner'));
      return;
    }

    if (!this.disabled) {
      this.doShowPopup({
        popup: 'modalcustomer'
      });
    }
  },
  initComponents: function() {
    return this;
  },
  renderCustomer: function(newCustomer) {
    this.setContent(newCustomer);
  },
  orderChanged: function(oldValue) {
    if (this.order.get('bp')) {
      this.renderCustomer(this.order.get('bp').get('_identifier'));
    } else {
      this.renderCustomer('');
    }

    this.order.on(
      'change:bp',
      function(model) {
        if (model.get('bp')) {
          if (OB.MobileApp.model.hasPermission('OBPOS_receipt.invoice')) {
            if (
              OB.MobileApp.model.hasPermission(
                'OBPOS_retail.restricttaxidinvoice',
                true
              )
            ) {
              if (!model.get('bp').get('taxID')) {
                if (
                  OB.MobileApp.model.get('terminal').terminalType
                    .generateInvoice
                ) {
                  OB.UTIL.showError(OB.I18N.getLabel('OBPOS_BP_No_Taxid'));
                } else {
                  OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_BP_No_Taxid'));
                }
                model.set('generateInvoice', false);
              } else {
                model.set(
                  'generateInvoice',
                  OB.MobileApp.model.get('terminal').terminalType
                    .generateInvoice
                );
              }
            }
          } else {
            model.set('generateInvoice', false);
          }
          this.renderCustomer(model.get('bp').get('_identifier'));
        } else {
          this.renderCustomer('');
        }
      },
      this
    );
  }
});

/*Modal*/

/*header of scrollable table*/
enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.UI.NewCustomerWindowButton',
  events: {
    onChangeSubWindow: '',
    onHideThisPopup: ''
  },
  disabled: false,
  classes:
    'obUiNewCustomerWindowButton businesspartner-obUiButton-generic_yellow',
  i18nLabel: 'OBPOS_LblNewCustomer',
  handlers: {
    onSetModel: 'setModel',
    onNewBPDisabled: 'doDisableNewBP'
  },
  setModel: function(inSender, inEvent) {
    this.model = inEvent.model;
  },
  doDisableNewBP: function(inSender, inEvent) {
    this.setDisabled(inEvent.status);
  },
  tap: function(model) {
    if (this.disabled) {
      return true;
    }
    this.doHideThisPopup();
    this.doChangeSubWindow({
      newWindow: {
        name: 'customerCreateAndEdit',
        params: {
          navigateOnClose: 'mainSubWindow'
        }
      }
    });
  }
});

enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.UI.AdvancedSearchCustomerWindowButton',
  classes:
    'obUiAdvancedSearchCustomerWindowButton businesspartner-obUiButton-generic_yellow',
  i18nLabel: 'OBPOS_LblAdvancedSearch',
  disabled: false,
  handlers: {
    onSetModel: 'setModel',
    onNewBPDisabled: 'doDisableNewBP'
  },
  setModel: function(inSender, inEvent) {
    this.model = inEvent.model;
  },
  doDisableNewBP: function(inSender, inEvent) {
    this.setDisabled(inEvent.status);
  },
  events: {
    onHideThisPopup: ''
  },
  tap: function() {
    if (this.disabled) {
      return true;
    }
    this.doHideThisPopup();
    this.model.get('subWindowManager').set('currentWindow', {
      name: 'customerAdvancedSearch',
      params: {
        caller: 'mainSubWindow'
      }
    });
  },
  initComponents: function() {
    this.inherited(arguments);
    this.setDisabled(
      !OB.MobileApp.model.hasPermission('OBPOS_receipt.customers')
    );
  }
});

enyo.kind({
  name: 'OB.UI.ModalBpScrollableHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  classes: 'obUiModalBpScrollableHeader',
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
      classes: 'obUiModalBpScrollableHeader-container1',
      components: [
        {
          classes: 'obUiModalBpScrollableHeader-container1-container1',
          components: [
            {
              classes:
                'obUiModalBpScrollableHeader-container1-container1-container1',
              components: [
                {
                  kind: 'OB.UI.SearchInputAutoFilter',
                  name: 'customerFilterText',
                  classes:
                    'obUiModalBpScrollableHeader-container1-container1-container1-customerFilterText',
                  skipAutoFilterPref: 'OBPOS_remote.customer'
                }
              ]
            },
            {
              classes:
                'obUiModalBpScrollableHeader-container1-container1-container2',
              components: [
                {
                  kind: 'OB.UI.SmallButton',
                  name: 'OB.UI.Bp.Modal.search',
                  classes:
                    'obUiModalBpScrollableHeader-container1-container1-container2-obUiBpModalSearch',
                  ontap: 'clearAction'
                }
              ]
            },
            {
              classes:
                'obUiModalBpScrollableHeader-container1-container1-container3',
              components: [
                {
                  kind: 'OB.UI.SmallButton',
                  classes:
                    'obUiModalBpScrollableHeader-container1-container1-container3-obUiSmallButton',
                  ontap: 'searchAction'
                }
              ]
            }
          ]
        }
      ]
    },
    {
      classes: 'obUiModalBpScrollableHeader-container2',
      components: [
        {
          classes: 'obUiModalBpScrollableHeader-container2-container1',
          components: [
            {
              classes:
                'obUiModalBpScrollableHeader-container2-container1-container1',
              components: [
                {
                  kind: 'OB.UI.NewCustomerWindowButton',
                  name: 'newAction',
                  classes:
                    'obUiModalBpScrollableHeader-container2-container1-container1-newAction'
                }
              ]
            },
            {
              classes:
                'obUiModalBpScrollableHeader-container2-container1-container2',
              components: [
                {
                  kind: 'OB.UI.AdvancedSearchCustomerWindowButton',
                  classes:
                    'obUiModalBpScrollableHeader-container2-container1-container2-obUiAdvancedSearchCustomerWindowButton'
                }
              ]
            }
          ]
        }
      ]
    }
  ],
  clearAction: function() {
    this.$.customerFilterText.setValue('');
    this.doClearAction();
  },
  searchAction: function() {
    this.doSearchAction({
      bpName: this.$.customerFilterText.getValue()
    });
    return true;
  }
});

/*items of collection*/
enyo.kind({
  name: 'OB.UI.ListBpsLine',
  kind: 'OB.UI.listItemButton',
  classes: 'obUiListBpsLine',
  components: [
    {
      name: 'line',
      classes: 'obUiListBpsLine-line',
      components: [
        {
          name: 'identifier',
          classes: 'obUiListBpsLine-line-identifier'
        },
        {
          name: 'onHold',
          classes: 'obUiListBpsLine-line-onHold'
        },
        {
          name: 'address',
          classes: 'obUiListBpsLine-line-address'
        },
        {
          classes: 'obUiListBpsLine-line-element4 u-clearBoth'
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
    this.$.identifier.setContent(this.model.get('_identifier'));
    if (
      this.model.get('customerBlocking') &&
      this.model.get('salesOrderBlocking')
    ) {
      this.$.onHold.setContent('(' + OB.I18N.getLabel('OBPOS_OnHold') + ')');
    }
    this.$.address.setContent(this.model.get('locName'));
  }
});

/*scrollable table (body of modal)*/
enyo.kind({
  name: 'OB.UI.ListBps',
  classes: 'obUiListBps',
  handlers: {
    onSearchAction: 'searchAction',
    onClearAction: 'clearAction'
  },
  events: {
    onChangeBusinessPartner: ''
  },
  components: [
    {
      classes: 'obUiListBps-container1',
      components: [
        {
          classes: 'obUiListBps-container1-container1',
          components: [
            {
              classes: 'obUiListBps-container1-container1-container1',
              components: [
                {
                  name: 'stBPAssignToReceipt',
                  kind: 'OB.UI.ScrollableTable',
                  classes:
                    'obUiListBps-container1-container1-container1-stBPAssignToReceipt',
                  renderHeader: 'OB.UI.ModalBpScrollableHeader',
                  renderLine: 'OB.UI.ListBpsLine',
                  renderEmpty: 'OB.UI.RenderEmpty'
                },
                {
                  name: 'renderLoading',
                  classes:
                    'obUiListBps-container1-container1-container1-renderLoading',
                  showing: false,
                  initComponents: function() {
                    this.setContent(OB.I18N.getLabel('OBPOS_LblLoading'));
                  }
                }
              ]
            }
          ]
        }
      ]
    }
  ],
  clearAction: function(inSender, inEvent) {
    this.bpsList.reset();
    return true;
  },
  searchAction: function(inSender, inEvent) {
    var me = this,
      filter = OB.UTIL.unAccent(inEvent.bpName);

    this.$.stBPAssignToReceipt.$.tempty.hide();
    this.$.stBPAssignToReceipt.$.tbody.hide();
    this.$.stBPAssignToReceipt.$.tlimit.hide();
    this.$.renderLoading.show();

    function errorCallback(tx, error) {
      OB.UTIL.showError(error);
    }

    function successCallbackBPs(dataBps) {
      me.$.renderLoading.hide();
      if (dataBps && dataBps.length > 0) {
        me.bpsList.reset(dataBps.models);
        me.$.stBPAssignToReceipt.$.tbody.show();
      } else {
        me.bpsList.reset();
        me.$.stBPAssignToReceipt.$.tempty.show();
      }
    }

    var criteria = {};
    if (filter && filter !== '') {
      criteria._filter = {
        operator: OB.Dal.CONTAINS,
        value: filter
      };
    }
    if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
      var filterIdentifier = {
        columns: ['_filter'],
        operator: 'startsWith',
        value: filter
      };
      var remoteCriteria = [filterIdentifier];
      criteria.remoteFilters = remoteCriteria;
    }
    if (OB.MobileApp.model.hasPermission('OBPOS_customerLimit', true)) {
      criteria._limit = OB.DEC.abs(
        OB.MobileApp.model.hasPermission('OBPOS_customerLimit', true)
      );
    }
    OB.Dal.find(
      OB.Model.BusinessPartner,
      criteria,
      successCallbackBPs,
      errorCallback
    );
    return true;
  },
  bpsList: null,
  init: function(model) {
    this.bpsList = new Backbone.Collection();
    this.$.stBPAssignToReceipt.setCollection(this.bpsList);
    this.bpsList.on(
      'click',
      function(model) {
        this.doChangeBusinessPartner({
          businessPartner: model
        });
      },
      this
    );
  }
});

/*Modal definiton*/
enyo.kind({
  name: 'OB.UI.ModalBusinessPartners',
  topPosition: '125px',
  kind: 'OB.UI.Modal',
  classes: 'obUiModalBusinessPartners',
  executeOnShow: function() {
    this.$.body.$.listBps.$.stBPAssignToReceipt.$.theader.$.modalBpScrollableHeader.$.newAction.setDisabled(
      !OB.MobileApp.model.hasPermission('OBPOS_retail.editCustomerButton')
    );
    return true;
  },

  executeOnHide: function() {
    this.$.body.$.listBps.$.stBPAssignToReceipt.$.theader.$.modalBpScrollableHeader.clearAction();
  },
  i18nHeader: 'OBPOS_LblAssignCustomer',
  body: {
    kind: 'OB.UI.ListBps',
    classes: 'obUiModalBusinessPartners-body-obUiListBps'
  },
  init: function(model) {
    this.model = model;
    this.waterfall('onSetModel', {
      model: this.model
    });
  }
});
