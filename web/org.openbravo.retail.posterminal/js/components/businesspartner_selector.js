/*
 ************************************************************************************
 * Copyright (C) 2016-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, OB, _ */

OB.UTIL.BusinessPartnerSelector = {
  cloneAndPush: function(list, value) {
    var result = _.clone(list || []);
    result.push(value);
    return result;
  },
  cloneAndPop: function(list) {
    var result = _.clone(list || []);
    result.pop();
    return result;
  }
};

enyo.kind({
  name: 'OB.UI.BusinessPartnerSelector',
  kind: 'OB.UI.FormElement.Selector',
  classes: 'obUiBusinessPartnerSelector',
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
        popup: 'modalcustomer',
        args: {
          presetCustomerId: OB.MobileApp.model.receipt.get('bp').id,
          target: 'order',
          clean: true,
          navigationPath: []
        }
      });
    }
  },
  initComponents: function() {
    return this;
  },
  renderCustomer: function(newCustomerId, newCustomerName) {
    this.setValue(newCustomerId, newCustomerName);
  },
  orderChanged: function(oldValue) {
    if (this.order.get('bp')) {
      this.renderCustomer(
        this.order.get('bp').get('id'),
        this.order.get('bp').get('_identifier')
      );
    } else {
      this.renderCustomer(null, '');
    }

    this.order.on(
      'change:bp',
      function(model) {
        if (model.get('bp')) {
          model.setFullInvoice(
            model.get('fullInvoice') && model.checkFullInvoice(false)
          );

          if (
            model.get('isEditable') &&
            !model.get('cloningReceipt') &&
            OB.MobileApp.model.hasPermission('EnableMultiPriceList', true) &&
            (model.get('priceIncludesTax') !==
              OB.MobileApp.model.get('pricelist').priceIncludesTax ||
              model.get('bp').get('priceListCurrency') !==
                OB.MobileApp.model.get('pricelist').currency)
          ) {
            model.set('priceList', OB.MobileApp.model.get('pricelist').id, {
              silent: true
            });
            model.set(
              'priceIncludesTax',
              OB.MobileApp.model.get('pricelist').priceIncludesTax
            );
            model.set('currency', OB.MobileApp.model.get('pricelist').currency);
            OB.UTIL.showConfirmation.display(
              OB.I18N.getLabel('OBPOS_ChangeOfPriceList'),
              OB.I18N.getLabel('OBPOS_ChangeOfPriceListConfig', [
                model.get('bp').get('priceListName'),
                OB.MobileApp.model.get('pricelist')._identifier
              ]),
              null,
              {
                onHideFunction: function() {
                  model.trigger('change:documentNo', model);
                }
              }
            );
          }

          model.set('invoiceTerms', model.get('bp').get('invoiceTerms'));
          this.renderCustomer(
            model.get('bp').get('id'),
            model.get('bp').get('_identifier')
          );
        } else {
          this.renderCustomer(null, '');
        }
      },
      this
    );
  }
});

/*header of scrollable table*/
enyo.kind({
  name: 'OB.UI.NewCustomerButton',
  kind: 'OB.UI.ModalDialogButton',
  events: {
    onShowPopup: '',
    onHideSelector: ''
  },
  disabled: false,
  classes: 'obUiNewCustomerButton',
  i18nLabel: 'OBPOS_LblNewCustomer',
  handlers: {
    onSetModel: 'setModel',
    onNewBPDisabled: 'doDisableNewBP'
  },
  initComponents: function() {
    this.popup = OB.UTIL.getPopupFromComponent(this);
    this.inherited(arguments);
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
    this.doHideSelector({
      selectorHide: true
    });
    var navigationPath = OB.UTIL.BusinessPartnerSelector.cloneAndPush(
      this.popup.args.navigationPath,
      'modalcustomer'
    );
    this.doShowPopup({
      popup: 'customerCreateAndEdit',
      args: {
        businessPartner: null,
        target: this.popup.target,
        navigationPath: OB.UTIL.BusinessPartnerSelector.cloneAndPush(
          navigationPath,
          'customerView'
        ),
        cancelNavigationPath: navigationPath
      }
    });
  }
});

enyo.kind({
  name: 'OB.UI.AdvancedFilterButton',
  kind: 'OB.UI.ButtonAdvancedFilter',
  classes: 'obUiAdvancedFilterButton',
  dialog: 'modalAdvancedFilterBP',
  i18nLabel: 'OBPOS_LblAdvancedFilter',
  disabled: false,
  handlers: {
    onNewBPDisabled: 'doDisableNewBP'
  },
  doDisableNewBP: function(inSender, inEvent) {
    this.setDisabled(inEvent.status);
  },
  initComponents: function() {
    this.inherited(arguments);
    this.setDisabled(
      !OB.MobileApp.model.hasPermission(
        'OBPOS_retail.customer_advanced_filters',
        true
      )
    );
  }
});

enyo.kind({
  name: 'OB.UI.ModalBpSelectorScrollableHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  classes: 'obUiModalBpSelectorScrollableHeader',
  filterModel: OB.Model.BPartnerFilter,
  components: [
    {
      name: 'filterSelector',
      kind: 'OB.UI.FilterSelectorTableHeader',
      classes: 'obUiModalBpSelectorScrollableHeader-filterSelector'
    }
  ],
  initComponents: function() {
    var filterProperties = this.filterModel.getFilterPropertiesWithSelectorPreference();
    _.each(
      filterProperties,
      function(prop) {
        // Set filter options for bpCategory and taxID
        if (prop.name === 'bpCategory') {
          prop.filter = OB.MobileApp.model.get(
            'terminal'
          ).bp_showcategoryselector;
        }
        if (prop.name === 'taxID') {
          prop.filter = OB.MobileApp.model.get('terminal').bp_showtaxid;
        }
      },
      this
    );
    this.filters = filterProperties;
    this.inherited(arguments);
  }
});

enyo.kind({
  name: 'OB.UI.ModalBpSelectorFooter',
  classes: 'obUiModalBpSelectorFooter',
  components: [
    {
      classes: 'obUiModalBpSelectorFooter-container1',
      showing: true,
      handlers: {
        onSetShow: 'setShow'
      },
      setShow: function(inSender, inEvent) {
        this.setShowing(inEvent.visibility);
        return true;
      },
      components: [
        {
          classes:
            'obUiModal-footer-secondaryButtons obUiModalBpSelectorFooter-container1-container1',
          components: [
            {
              kind: 'OB.UI.AdvancedFilterButton',
              classes:
                'obUiModalBpSelectorFooter-container1-container1-element1'
            }
          ]
        },
        {
          classes:
            'obUiModal-footer-mainButtons obUiModalBpSelectorFooter-container1-container2',
          components: [
            {
              kind: 'OB.UI.NewCustomerButton',
              name: 'newAction',
              classes:
                'obUiModalBpSelectorFooter-container1-container2-newAction'
            },
            {
              kind: 'OB.UI.ModalDialogButton',
              classes: 'obUiModalBpSelectorFooter-container1-container2-close',
              i18nLabel: 'OBRDM_LblClose',
              tap: function() {
                if (this.disabled === false) {
                  this.doHideThisPopup();
                }
              }
            }
          ]
        }
      ]
    }
  ],
  initComponents: function() {
    this.inherited(arguments);
  }
});

enyo.kind({
  name: 'OB.UI.BPDetailsContextMenuItem',
  kind: 'OB.UI.ListContextMenuItem',
  classes: 'obUiBpDetailsContextMenuItem',
  i18NLabel: 'OBPOS_BPViewDetails',
  selectItem: function(bpartner) {
    bpartner.set('ignoreSetBP', true, {
      silent: true
    });
    var dialog = this.owner.owner.dialog;
    OB.Dal.get(OB.Model.BusinessPartner, bpartner.get('bpartnerId'), function(
      bp
    ) {
      dialog.bubble('onShowPopup', {
        popup: 'customerView',
        args: {
          businessPartner: bp,
          target: dialog.target,
          navigationPath: OB.UTIL.BusinessPartnerSelector.cloneAndPush(
            dialog.owner.owner.args.navigationPath,
            'modalcustomer'
          )
        }
      });
    });
    return true;
  },
  create: function() {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel(this.i18NLabel));
  }
});

enyo.kind({
  name: 'OB.UI.BPEditContextMenuItem',
  kind: 'OB.UI.ListContextMenuItem',
  classes: 'obUiBpEditContextMenuItem',
  i18NLabel: 'OBPOS_BPEdit',
  selectItem: function(bpartner) {
    bpartner.set('ignoreSetBP', true, {
      silent: true
    });

    var dialog = this.owner.owner.dialog,
      navigationPath = OB.UTIL.BusinessPartnerSelector.cloneAndPush(
        dialog.owner.owner.args.navigationPath,
        'modalcustomer'
      );

    OB.Dal.get(OB.Model.BusinessPartner, bpartner.get('bpartnerId'), function(
      bp
    ) {
      dialog.bubble('onShowPopup', {
        popup: 'customerCreateAndEdit',
        args: {
          businessPartner: bp,
          target: dialog.target,
          navigationPath: OB.UTIL.BusinessPartnerSelector.cloneAndPush(
            navigationPath,
            'customerView'
          ),
          cancelNavigationPath: navigationPath
        }
      });
    });
    return true;
  },
  create: function() {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel(this.i18NLabel));
  }
});

enyo.kind({
  name: 'OB.UI.BPAddressContextMenuItem',
  kind: 'OB.UI.ListContextMenuItem',
  classes: 'obUiBpAddressContextMenuItem',
  i18NLabel: 'OBPOS_BPAddress',
  selectItem: function(bpartner) {
    var dialog = this.owner.owner.dialog;
    bpartner.set('ignoreSetBP', true, {
      silent: true
    });
    OB.Dal.get(OB.Model.BusinessPartner, bpartner.get('bpartnerId'), function(
      bp
    ) {
      OB.MobileApp.view.$.containerWindow.getRoot().bubble('onShowPopup', {
        popup: 'modalcustomeraddress',
        args: {
          target: 'order',
          businessPartner: bp,
          manageAddress: true,
          clean: true,
          navigationPath: OB.UTIL.BusinessPartnerSelector.cloneAndPush(
            dialog.owner.owner.args.navigationPath,
            'modalcustomer'
          )
        }
      });
    });
    return true;
  },
  create: function() {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel(this.i18NLabel));
  }
});

enyo.kind({
  name: 'OB.UI.BPReceiptsContextMenuItem',
  kind: 'OB.UI.ListContextMenuItem',
  classes: 'obUiBpReceiptsContextMenuItem',
  i18NLabel: 'OBPOS_BPReceipts',
  events: {
    onShowPopup: ''
  },
  selectItem: function(bpartner) {
    if (OB.MobileApp.model.get('connectedToERP')) {
      var me = this,
        dialog = this.owner.owner.dialog;
      bpartner.set('ignoreSetBP', true, {
        silent: true
      });
      dialog.owner.owner.hide();
      OB.Dal.get(
        OB.Model.BusinessPartner,
        bpartner.get('bpartnerId'),
        function(bp) {
          me.doShowPopup({
            popup: 'modalReceiptSelectorCustomerView',
            args: {
              multiselect: true,
              target: dialog.target,
              businessPartner: bp,
              navigationPath: OB.UTIL.BusinessPartnerSelector.cloneAndPush(
                dialog.owner.owner.args.navigationPath,
                'modalcustomer'
              )
            }
          });
        },
        function() {
          OB.UTIL.showConfirmation.display(
            OB.I18N.getLabel('OBMOBC_Error'),
            OB.I18N.getLabel('OBPOS_CustomerNotFound')
          );
        },
        function() {
          OB.UTIL.showConfirmation.display(
            OB.I18N.getLabel('OBMOBC_Error'),
            OB.I18N.getLabel('OBPOS_CustomerNotFound')
          );
        }
      );
    } else {
      OB.UTIL.showError(OB.I18N.getLabel('OBPOS_OfflineWindowRequiresOnline'));
    }
    return true;
  },
  create: function() {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel(this.i18NLabel));
  }
});

enyo.kind({
  name: 'OB.UI.BusinessPartnerContextMenu',
  kind: 'OB.UI.ListContextMenu',
  classes: 'obUiBusinessPartnerContextMenu',
  initComponents: function() {
    this.inherited(arguments);
    var menuOptions = [],
      extraOptions = OB.MobileApp.model.get('extraBPContextMenuOptions') || [];

    menuOptions.push({
      kind: 'OB.UI.BPDetailsContextMenuItem',
      permission: 'OBPOS_receipt.customers'
    });
    if (
      OB.MobileApp.model.hasPermission('OBPOS_retail.editCustomerButton', true)
    ) {
      menuOptions.push({
        kind: 'OB.UI.BPEditContextMenuItem',
        classes: 'obUiBusinessPartnerContextMenu-obUiBpEditContextMenuItem',
        permission: 'OBPOS_retail.editCustomerButton'
      });
    }
    if (
      OB.MobileApp.model.hasPermission(
        'OBPOS_retail.editCustomerLocationButton',
        true
      )
    ) {
      menuOptions.push({
        kind: 'OB.UI.BPAddressContextMenuItem',
        classes: 'obUiBusinessPartnerContextMenu-obUiBpAddressContextMenuItem',
        permission: 'OBPOS_retail.assignToReceiptAddress'
      });
    }

    if (
      this.owner.model.get('bpartnerId') !==
        OB.MobileApp.model.get('businesspartner') &&
      this.owner.owner.owner.owner.owner.target.indexOf(
        'filterSelectorButton_'
      ) < 0
    ) {
      menuOptions.push({
        kind: 'OB.UI.BPReceiptsContextMenuItem',
        classes: 'obUiBusinessPartnerContextMenu-obUiBpReceiptsContextMenuItem',
        permission: 'OBPOS_retail.assignToReceiptAddress'
      });
    }
    menuOptions = menuOptions.concat(extraOptions);
    this.$.menu.setItems(menuOptions);
  }
});

/*items of collection*/
enyo.kind({
  name: 'OB.UI.ListBpsSelectorLine',
  kind: 'OB.UI.ListSelectorLine',
  classes: 'obUiListBpsSelectorLine',
  components: [
    {
      name: 'line',
      classes: 'obUiListBpsSelectorLine-line',
      components: [
        {
          name: 'textInfo',
          classes: 'obUiListBpsSelectorLine-line-textInfo',
          components: [
            {
              classes: 'obUiListBpsSelectorLine-line-textInfo-container1',
              components: [
                {
                  tag: 'span',
                  name: 'identifier',
                  classes:
                    'obUiListBpsSelectorLine-line-textInfo-container1-identifier'
                },
                {
                  tag: 'span',
                  name: 'filter',
                  classes:
                    'obUiListBpsSelectorLine-line-textInfo-container1-filter'
                },
                {
                  tag: 'span',
                  name: 'onHold',
                  classes:
                    'obUiListBpsSelectorLine-line-textInfo-container1-onHold'
                }
              ]
            },
            {
              name: 'bottomShipIcon',
              classes: 'obUiListBpsSelectorLine-line-textInfo-bottomShipIcon'
            },
            {
              name: 'bottomBillIcon',
              classes: 'obUiListBpsSelectorLine-line-textInfo-bottomBillIcon'
            },
            {
              classes: 'obUiListBpsSelectorLine-line-textInfo-element1'
            }
          ]
        },
        {
          classes: 'obUiListBpsSelectorLine-line-container1',
          components: [
            {
              name: 'btnContextMenu',
              kind: 'OB.UI.BusinessPartnerContextMenu',
              classes: 'obUiListBpsSelectorLine-line-container1-btnContextMenu'
            }
          ]
        }
      ]
    }
  ],
  create: function() {
    this.inherited(arguments);
    this.$.identifier.setContent(this.model.get('_identifier'));
    this.$.filter.setContent(this.model.get('filter'));
    if (
      this.model.get('customerBlocking') &&
      this.model.get('salesOrderBlocking')
    ) {
      this.$.onHold.setContent(' (' + OB.I18N.getLabel('OBPOS_OnHold') + ')');
    }
    if (this.model.get('bpartnerId') === this.owner.owner.owner.selectedValue) {
      this.addClass('obUiListBpsSelectorLine_selectedItem');
    }
    this.$.bottomShipIcon.show();
    this.$.bottomBillIcon.show();
    if (this.model.get('isBillTo') && this.model.get('isShipTo')) {
      this.$.bottomShipIcon.addClass('u-showComponent');
      this.$.bottomBillIcon.addClass('u-showComponent');
    } else if (this.model.get('isBillTo')) {
      this.$.bottomShipIcon.addClass('u-hiddeComponent');
      this.$.bottomBillIcon.addClass('u-showComponent');
    } else if (this.model.get('isShipTo')) {
      this.$.bottomShipIcon.addClass('u-showComponent');
      this.$.bottomBillIcon.addClass('u-hiddeComponent');
    } else {
      this.$.bottomShipIcon.hide();
      this.$.bottomBillIcon.hide();
    }
    var bPartner = this.owner.owner.owner.bPartner;
    if (bPartner && bPartner.get('id') === this.model.get('bpartnerId')) {
      this.addClass('obUiListBpsSelectorLine_selectedItem');
    }
    // Context menu
    this.$.btnContextMenu.dialog = this.owner.owner.owner.owner;
    if (this.$.btnContextMenu.$.menu.itemsCount === 0) {
      this.$.btnContextMenu.hide();
    } else {
      this.$.btnContextMenu.setModel(this.model);
    }
  }
});

/*scrollable table (body of modal)*/
enyo.kind({
  name: 'OB.UI.ListBpsSelector',
  classes: 'obUiListBpsSelector',
  handlers: {
    onSearchAction: 'searchAction',
    onClearFilterSelector: 'clearAction',
    onSetBusinessPartnerTarget: 'setBusinessPartnerTarget'
  },
  events: {
    onChangeBusinessPartner: '',
    onChangeFilterSelector: '',
    onHideSelector: '',
    onShowSelector: ''
  },
  components: [
    {
      classes: 'obUiListBpsSelector-container1',
      components: [
        {
          name: 'stBPAssignToReceipt',
          kind: 'OB.UI.ScrollableTable',
          classes: 'obUiListBpsSelector-container1-stBpAssignToReceipt',
          renderHeader: 'OB.UI.ModalBpSelectorScrollableHeader',
          renderLine: 'OB.UI.ListBpsSelectorLine',
          renderEmpty: 'OB.UI.RenderEmpty'
        },
        {
          name: 'renderLoading',
          classes: 'obUiListBpsSelector-container1-renderLoading',
          showing: false,
          initComponents: function() {
            this.setContent(OB.I18N.getLabel('OBPOS_LblLoading'));
          }
        }
      ]
    }
  ],
  setBusinessPartnerTarget: function(inSender, inEvent) {
    this.target = inEvent.target;
  },
  loadPresetCustomer: function(bpartnerId) {
    var me = this;
    OB.Dal.get(OB.Model.BusinessPartner, bpartnerId, function(bp) {
      bp.set('bpartnerId', bpartnerId, {
        silent: true
      });
      me.bpsList.reset([bp]);
      me.$.stBPAssignToReceipt.$.tbody.show();
    });
  },
  clearAction: function(inSender, inEvent) {
    this.bpsList.reset();
    return true;
  },
  searchAction: function(inSender, inEvent) {
    var execution = OB.UTIL.ProcessController.start('searchCustomer');
    var me = this;

    if (
      OB.MobileApp.model.hasPermission(
        'OBPOS_retail.createCustomerButton',
        true
      )
    ) {
      this.popup.$.footer.$.modalBpSelectorFooter.$.newAction.setDisabled(
        false
      );
    }

    this.$.stBPAssignToReceipt.$.tempty.hide();
    this.$.stBPAssignToReceipt.$.tbody.hide();
    this.$.stBPAssignToReceipt.$.tlimit.hide();
    this.$.renderLoading.show();

    function hasLocationInFilter() {
      if (
        OB.MobileApp.model.hasPermission('OBPOS_FilterAlwaysBPByAddress', true)
      ) {
        return true;
      }
      return _.some(inEvent.filters, function(flt) {
        var column = _.find(OB.Model.BPartnerFilter.getProperties(), function(
          col
        ) {
          return col.column === flt.column;
        });
        return column && column.location;
      });
    }

    function errorCallback(tx, error) {
      me.$.renderLoading.hide();
      me.$.stBPAssignToReceipt.$.tempty.show();
      me.doHideSelector();
      var i, message, tokens;

      function getProperty(property) {
        return OB.Model.BPartnerFilter.getProperties().find(function(prop) {
          return prop.name === property;
        });
      }

      // Generate a generic message if error is not defined
      if (
        OB.UTIL.isNullOrUndefined(error) ||
        OB.UTIL.isNullOrUndefined(error.message)
      ) {
        error = {
          message: OB.I18N.getLabel('OBMOBC_MsgApplicationServerNotAvailable')
        };
      }

      if (error.message.startsWith('###')) {
        tokens = error.message.split('###');
        message = [];
        for (i = 0; i < tokens.length; i++) {
          if (tokens[i] !== '') {
            if (
              tokens[i] === 'OBMOBC_FilteringNotAllowed' ||
              tokens[i] === 'OBMOBC_SortingNotAllowed'
            ) {
              message.push({
                content: OB.I18N.getLabel(tokens[i]),
                classes: 'obUiListBpsSelector-errorMessage'
              });
            } else {
              var property = getProperty(tokens[i]);
              if (property) {
                message.push({
                  content: OB.I18N.getLabel(property.caption),
                  classes: 'obUiListBpsSelector-errorMessage',
                  tag: 'li'
                });
              }
            }
          }
        }
      } else {
        message = error.message;
      }

      OB.UTIL.showConfirmation.display(
        OB.I18N.getLabel('OBMOBC_Error'),
        message,
        null,
        {
          onHideFunction: function() {
            me.doShowSelector();
          }
        }
      );
    }

    function successCallbackBPs(dataBps) {
      me.$.renderLoading.hide();
      if (dataBps && dataBps.length > 0) {
        _.each(dataBps.models, function(bp) {
          var filter = '',
            filterObj;
          if (
            hasLocationInFilter() ||
            !OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)
          ) {
            filter = ' / ' + bp.get('locName');
          }
          _.each(inEvent.filters, function(flt, index) {
            filterObj = OB.Model.BPartnerFilter.getProperties().find(function(
              filter
            ) {
              return filter.column === flt.column;
            });
            if (
              flt.column !== 'bp.name' &&
              flt.column !== 'loc.name' &&
              !filterObj.hideFilterResult
            ) {
              var column = _.find(
                OB.Model.BPartnerFilter.getProperties(),
                function(col) {
                  return col.column === flt.column;
                }
              );
              if (column) {
                filter +=
                  ' / ' + (bp.get(column.name) ? bp.get(column.name) : '');
              }
            }
          });
          bp.set('_identifier', bp.get('bpName'));
          bp.set('filter', filter);
        });
        me.bpsList.reset(dataBps.models);
        me.$.stBPAssignToReceipt.$.tbody.show();
      } else {
        me.bpsList.reset();
        me.$.stBPAssignToReceipt.$.tempty.show();
      }
    }

    if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
      var criteria = {
        _orderByClause: ''
      };
      criteria.remoteFilters = [];
      _.each(inEvent.filters, function(flt) {
        var column = _.find(OB.Model.BPartnerFilter.getProperties(), function(
          col
        ) {
          return col.column === flt.column;
        });
        if (column) {
          if (column.hqlFilter) {
            criteria.remoteFilters.push({
              value: flt.hqlFilter,
              columns: [column.name],
              operator: OB.Dal.FILTER,
              params: [flt.value],
              location: column.location
            });
          } else {
            var operator = column.operator
              ? column.operator
              : OB.MobileApp.model.hasPermission(
                  'OBPOS_remote.customer_usesContains',
                  true
                )
              ? OB.Dal.CONTAINS
              : OB.Dal.STARTSWITH;
            criteria.remoteFilters.push({
              columns: [column.name],
              operator: operator,
              value: flt.value,
              location: column.location
            });
          }
        }
      });
      if (OB.MobileApp.model.hasPermission('OBPOS_customerLimit', true)) {
        criteria._limit = OB.DEC.abs(
          OB.MobileApp.model.hasPermission('OBPOS_customerLimit', true)
        );
      }
      if (inEvent.orderby) {
        criteria._orderByProperties = [
          {
            property: inEvent.orderby.name,
            sorting: inEvent.orderby.direction
          }
        ];
      } else {
        criteria._orderByProperties = [
          {
            property: 'bpName',
            sorting: 'asc'
          }
        ];
      }
      OB.Dal.find(
        OB.Model.BPartnerFilter,
        criteria,
        successCallbackBPs,
        errorCallback,
        this
      );
    } else {
      var limit,
        index = 0,
        params = [],
        select = 'select ',
        orderby =
          ' order by ' +
          (inEvent.advanced && inEvent.orderby
            ? inEvent.orderby.column + ' ' + inEvent.orderby.direction
            : 'bp.name');

      _.each(OB.Model.BPartnerFilter.getProperties(), function(prop) {
        if (
          prop.column !== '_filter' &&
          prop.column !== '_idx' &&
          prop.column !== '_identifier'
        ) {
          if (index !== 0) {
            select += ', ';
          }
          if (prop.column === 'id') {
            select +=
              (location ? 'loc.c_bpartner_location_id' : 'bp.c_bpartner_id') +
              ' as ' +
              prop.name;
          } else {
            if (!location && prop.location) {
              select += "'' as " + prop.name;
            } else {
              select += prop.column + ' as ' + prop.name;
            }
          }
          index++;
        }
      });
      select +=
        ' from c_bpartner bp left join c_bpartner_location loc on bp.c_bpartner_id = loc.c_bpartner_id ';

      if (inEvent.advanced) {
        if (inEvent.filters.length > 0) {
          select += 'where ';
          _.each(inEvent.filters, function(flt, index) {
            if (index !== 0) {
              select += ' and ';
            }
            select += flt.column + ' like ? ';
            params.push('%' + flt.value + '%');
            if (!inEvent.advanced && flt.orderby) {
              orderby = ' order by ' + flt.column;
            }
          });
        }
      } else if (inEvent.filters.length > 0) {
        select += 'where bp._filter like ? or loc._filter like ?';
        var text = OB.UTIL.unAccent(inEvent.filters[0].value);
        params.push('%' + text + '%');
        params.push('%' + text + '%');
      }
      if (OB.MobileApp.model.hasPermission('OBPOS_customerLimit', true)) {
        limit = OB.DEC.abs(
          OB.MobileApp.model.hasPermission('OBPOS_customerLimit', true)
        );
      }
      OB.Dal.query(
        OB.Model.BPartnerFilter,
        select + orderby,
        params,
        successCallbackBPs,
        errorCallback,
        null,
        null,
        limit
      );
    }
    OB.UTIL.ProcessController.finish('searchCustomer', execution);
    return true;
  },
  bpsList: null,
  initComponents: function() {
    this.popup = OB.UTIL.getPopupFromComponent(this);
    this.inherited(arguments);
  },
  init: function(model) {
    this.bpsList = new Backbone.Collection();
    this.$.stBPAssignToReceipt.setCollection(this.bpsList);
    this.bpsList.on(
      'click',
      function(model) {
        if (model.get('customerBlocking') && model.get('salesOrderBlocking')) {
          OB.UTIL.showError(
            OB.I18N.getLabel('OBPOS_BPartnerOnHold', [model.get('_identifier')])
          );
        } else if (!model.get('ignoreSetBP')) {
          var me = this;
          if (model.get('bpLocactionId')) {
            OB.Dal.get(
              OB.Model.BPLocation,
              model.get('bpLocactionId'),
              function(loc) {
                var shipping = null,
                  billing = null;
                if (loc) {
                  if (!billing && loc.get('isBillTo')) {
                    billing = loc;
                  }
                  if (!shipping && loc.get('isShipTo')) {
                    shipping = loc;
                  }
                }
                me.loadBPLocations(model, shipping, billing);
              }
            );
          } else {
            me.loadBPLocations(model, null, null);
          }
        }
      },
      this
    );
  },
  loadBPLocations: function(bpartner, shipping, billing) {
    var me = this;
    if (shipping && billing) {
      this.setBPLocation(bpartner, shipping, billing);
    } else {
      var bp = new OB.Model.BusinessPartner({
        id: bpartner.get('bpartnerId')
      });
      bp.loadBPLocations(shipping, billing, function(
        shipping,
        billing,
        locations
      ) {
        me.setBPLocation(bpartner, shipping, billing, locations);
      });
    }
  },
  setBPLocation: function(bpartner, shipping, billing, locations) {
    if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
      if (!shipping) {
        OB.UTIL.showError(
          OB.I18N.getLabel('OBPOS_BPartnerNoShippingAddress', [
            bpartner.get('_identifier')
          ])
        );
        return;
      }
      if (!billing) {
        OB.UTIL.showError(
          OB.I18N.getLabel('OBPOS_BPartnerNoInvoiceAddress', [
            bpartner.get('_identifier')
          ])
        );
        return;
      }
    }
    var me = this;
    OB.Dal.get(OB.Model.BusinessPartner, bpartner.get('bpartnerId'), function(
      bp
    ) {
      bp.setBPLocations(
        shipping,
        billing,
        OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)
      );
      bp.set('locations', locations);
      if (me.target.startsWith('filterSelectorButton_')) {
        me.doChangeFilterSelector({
          selector: {
            name: me.target.substring('filterSelectorButton_'.length),
            value: bp.get('id'),
            text: bp.get('_identifier'),
            businessPartner: bp
          }
        });
      } else {
        me.doChangeBusinessPartner({
          businessPartner: bp,
          target: me.target
        });
      }
    });
  }
});

/*Modal definition*/
enyo.kind({
  name: 'OB.UI.ModalSelectorBusinessPartners',
  kind: 'OB.UI.ModalSelector',
  classes: 'obUiModalSelectorBusinessPartners',
  i18nHeader: 'OBPOS_LblAssignCustomer',
  events: {
    onShowPopup: ''
  },
  body: {
    kind: 'OB.UI.ListBpsSelector'
  },
  footer: {
    kind: 'OB.UI.ModalBpSelectorFooter'
  },
  executeOnShow: function() {
    if (!this.isInitialized()) {
      this.inherited(arguments);
      this.$.header.setContent(
        OB.I18N.getLabel(
          this.args.target.indexOf('filterSelectorButton_') === 0
            ? 'OBPOS_LblSelectCustomer'
            : 'OBPOS_LblAssignCustomer'
        )
      );
      if (_.isUndefined(this.args.visibilityButtons)) {
        this.args.visibilityButtons = true;
      }
      if (_.isUndefined(this.args.target)) {
        this.args.target = 'order';
      }
      this.waterfall('onSetShow', {
        visibility: this.args.visibilityButtons
      });
      this.bubble('onSetBusinessPartnerTarget', {
        target: this.args.target
      });
      this.waterfall('onSetBusinessPartnerTarget', {
        target: this.args.target
      });
      this.$.footer.$.modalBpSelectorFooter.$.newAction.setDisabled(
        !OB.MobileApp.model.hasPermission(
          'OBPOS_retail.createCustomerButton',
          true
        )
      );
      if (!OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
        this.$.body.$.listBpsSelector.$.stBPAssignToReceipt.$.theader.$.modalBpSelectorScrollableHeader.$.filterSelector.hideFilterCombo();
      }
      if (
        OB.MobileApp.model.hasPermission(
          'OBPOS_retail.disableNewBPButton',
          true
        )
      ) {
        this.$.footer.$.modalBpSelectorFooter.$.newAction.setDisabled(true);
      }
      this.$.body.$.listBpsSelector.$.stBPAssignToReceipt.$.theader.$.modalBpSelectorScrollableHeader.$.filterSelector.clearFilter();
      if (this.args.businessPartner) {
        this.$.body.$.listBpsSelector.$.stBPAssignToReceipt.$.theader.$.modalBpSelectorScrollableHeader.$.filterSelector.searchAction();
        this.$.body.$.listBpsSelector.$.stBPAssignToReceipt.bPartner = this.args.businessPartner;
      } else {
        this.$.body.$.listBpsSelector.$.stBPAssignToReceipt.bPartner = null;
      }
      if (this.args.presetCustomerId) {
        this.$.body.$.listBpsSelector.loadPresetCustomer(
          this.args.presetCustomerId
        );
      }
    } else {
      if (OB.UTIL.isNullOrUndefined(this.args.target)) {
        this.args.target = 'order';
      }
      this.bubble('onSetBusinessPartnerTarget', {
        target: this.args.target
      });
      this.waterfall('onSetBusinessPartnerTarget', {
        target: this.args.target
      });
      if (this.keepFiltersOnClose) {
        this.doSetSelectorAdvancedSearch({
          isAdvanced: this.advancedFilterShowing
        });
      }
      this.$.body.$.listBpsSelector.$.stBPAssignToReceipt.selectedValue = this.args.selectedValue;
      if (this.args.makeSearch) {
        this.$.body.$.listBpsSelector.$.stBPAssignToReceipt.$.theader.$.modalBpSelectorScrollableHeader.$.filterSelector.searchAction();
      } else {
        this.$.body.$.listBpsSelector.bpsList.reset(
          this.$.body.$.listBpsSelector.bpsList.models
        );
      }
    }
    return true;
  },
  executeOnHide: function() {
    var selectorHide = this.selectorHide;
    if (this.keepFiltersOnClose) {
      this.advancedFilterShowing = this.$.body.$.listBpsSelector.$.stBPAssignToReceipt.$.theader.$.modalBpSelectorScrollableHeader.$.filterSelector.$.advancedFilterInfo.showing;
    }
    this.inherited(arguments);
    if (
      !selectorHide &&
      this.args.navigationPath &&
      this.args.navigationPath.length > 0
    ) {
      this.doShowPopup({
        popup: this.args.navigationPath[this.args.navigationPath.length - 1],
        args: {
          businessPartner: this.args.businessPartner,
          target: this.args.target,
          navigationPath: OB.UTIL.BusinessPartnerSelector.cloneAndPop(
            this.args.navigationPath
          ),
          makeSearch: this.args.makeSearch
        }
      });
    }
  },
  getScrollableTable: function() {
    return this.$.body.$.listBpsSelector.$.stBPAssignToReceipt;
  },
  getFilterSelectorTableHeader: function() {
    return this.$.body.$.listBpsSelector.$.stBPAssignToReceipt.$.theader.$
      .modalBpSelectorScrollableHeader.$.filterSelector;
  },
  getAdvancedFilterBtn: function() {
    return this.$.footer.$.modalBpSelectorFooter.$.advancedFilterButton;
  },
  getAdvancedFilterDialog: function() {
    return 'modalAdvancedFilterBP';
  },
  init: function(model) {
    this.inherited(arguments);
    this.model = model;
    this.waterfall('onSetModel', {
      model: this.model
    });
    if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
      this.$.body.$.listBpsSelector.$.stBPAssignToReceipt.$.theader.$.modalBpSelectorScrollableHeader.$.filterSelector.$.formElementEntityFilterText.coreElement.skipAutoFilterPref = true;
    }
  }
});

enyo.kind({
  name: 'OB.UI.ModalAdvancedFilterBP',
  kind: 'OB.UI.ModalAdvancedFilters',
  classes: 'obUiModalAdvancedFilterBp',
  model: OB.Model.BPartnerFilter,
  initComponents: function() {
    this.inherited(arguments);
    var filterProperties = this.model.getFilterPropertiesWithSelectorPreference();
    _.each(
      filterProperties,
      function(prop) {
        // Set filter options for bpCategory and taxID
        if (prop.name === 'bpCategory') {
          prop.filter = OB.MobileApp.model.get(
            'terminal'
          ).bp_showcategoryselector;
        }
        if (prop.name === 'taxID') {
          prop.filter = OB.MobileApp.model.get('terminal').bp_showtaxid;
        }
      },
      this
    );
    this.setFilters(filterProperties);
  }
});
