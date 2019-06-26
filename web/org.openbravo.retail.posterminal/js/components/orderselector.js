/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, Backbone, moment, OBRDM, _ */

enyo.kind({
  kind: 'OB.UI.SmallButton',
  name: 'OBRDM.UI.ButtonSelectAll',
  classes: 'obrdmUiButtonSelectAll',
  i18nLabel: 'OBPOS_lblSelectAll',
  events: {
    onSelectAll: ''
  },
  selectAll: true,
  changeTo: function(select) {
    if (!select) {
      this.setContent(OB.I18N.getLabel('OBRDM_LblUnSelectAll'));
    } else {
      this.setContent(OB.I18N.getLabel('OBPOS_lblSelectAll'));
    }
    this.selectAll = select;
  },
  tap: function() {
    this.doSelectAll({
      selectAll: this.selectAll
    });
    this.changeTo(!this.selectAll);
  }
});

enyo.kind({
  kind: 'OB.UI.ButtonAdvancedFilter',
  name: 'OBRDM.UI.ButtonAdvancedFilter',
  dialog: 'OBRDM_ModalAdvancedFilterOrder'
});

enyo.kind({
  kind: 'OB.UI.SmallButton',
  name: 'OBRDM.UI.ButtonPrepareSelected',
  classes: 'btnlink-green btnlink btnlink-small obrdm-button-prepared-selected',
  i18nLabel: 'OBRDM_LblPrepareSelected',
  events: {
    onPrepareSelected: ''
  },
  tap: function() {
    if (this.disabled) {
      return;
    }
    this.disableButton(true);
    this.doPrepareSelected();
  },
  preparing: false,
  disableButton: function(isDisabled) {
    isDisabled = isDisabled || this.preparing;
    this.setDisabled(isDisabled);
  }
});

enyo.kind({
  name: 'OBRDM.UI.ModalOrderScrollableHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  components: [
    {
      classes: 'brdm-filter-selector',
      kind: 'OB.UI.FilterSelectorTableHeader',
      name: 'filterSelector',
      filters: OB.Model.OBRDM_OrderFilter.getProperties()
    },
    {
      showing: true,
      classes: 'obrdm-filter-selector-inner',
      components: [
        {
          classes: 'obrdm-filter-selector-inner-1',
          components: [
            {
              classes: 'obrdm-filter-selector-inner-1-1',
              components: [
                {
                  kind: 'OBRDM.UI.ButtonSelectAll'
                }
              ]
            },
            {
              classes: 'obrdm-filter-selector-inner-2',
              components: [
                {
                  kind: 'OBRDM.UI.ButtonAdvancedFilter'
                }
              ]
            },
            {
              classes: 'obrdm-filter-selector-inner-3',
              components: [
                {
                  kind: 'OBRDM.UI.ButtonPrepareSelected'
                }
              ]
            }
          ]
        }
      ]
    }
  ]
});

/* items of collection */
enyo.kind({
  name: 'OBRDM.UI.ListOrdersLine',
  kind: 'OB.UI.listItemButton',
  classes: 'obrdm-listitembutton',
  components: [
    {
      name: 'order',
      classes: 'obrdm-row-order',
      components: [
        {
          classes: 'obrdm-checkbox-half-on',
          name: 'iconOrder',
          tap: function() {
            this.bubble('onTapOrderIcon');
          }
        },
        {
          classes: 'obrdm-row-order-text',
          name: 'documentNo'
        },
        {
          classes: 'obrdm-row-order-text obrdm-listitembutton-text-color',
          name: 'bpName'
        },
        {
          classes: 'obrdm-row-order-text obrdm-listitembutton-text-color',
          name: 'orderedDate'
        },
        {
          classes: 'obrdm-clear-both'
        }
      ]
    },
    {
      name: 'orderline',
      classes: 'obrdm-row-orderline',
      components: [
        {
          classes: 'obrdm-cell-orderline-left',
          components: [
            {
              classes: 'obrdm-orderline-icon obrdm-checkbox-off',
              name: 'iconOrderLine',
              tap: function() {
                this.bubble('onTapLineIcon');
              }
            },
            {
              classes: 'obrdm-cell-orderline-left-info',
              components: [
                {
                  classes: 'obrdm-text-line obrdm-orderline-info',
                  name: 'orderlineInfo'
                },
                {
                  classes: 'obrdm-text-line obrdm-orderline-prouct',
                  name: 'orderlineProduct'
                },
                {
                  classes: 'obrdm-button-info obrdm-button-info-normal',
                  name: 'orderlineInfoBtn',
                  tap: function() {
                    this.bubble('onHideSelector');
                    this.bubble('onShowPopup', {
                      popup: this.owner.$.orderlineInfoBtn.showinfoplus
                        ? 'OBRDM_UI_OrderLineInfoPopup'
                        : 'OBRDM_UI_OrderLineInfoPopupStandard',
                      args: {
                        context: this,
                        line: this.owner.model,
                        callback: function(context) {
                          context.bubble('onShowSelector');
                        }
                      }
                    });
                  }
                }
              ]
            }
          ]
        },
        {
          classes: 'obrdm-cell-orderline-right',
          components: [
            {
              classes: 'obrdm-cell-orderline-right-col1',
              components: [
                {
                  classes: 'obrdm-lbl-prepare',
                  initComponents: function() {
                    this.setContent(OB.I18N.getLabel('OBRDM_LblPrepare'));
                  }
                },
                {
                  classes: 'obrdm-float-left',
                  components: [
                    {
                      classes: 'obrdm-button-minus-plus obrdm-button-minus',
                      initComponents: function() {
                        this.setContent(
                          OB.I18N.getLabel('OBMOBC_Character')[3]
                        );
                      },
                      tap: function() {
                        var qty = parseInt(
                            this.owner.$.orderlineQty.getValue(),
                            10
                          ),
                          min = this.owner.$.orderlineQty.getMin();
                        if (qty > min) {
                          this.owner.$.orderlineQty.setValue(qty - 1);
                          this.bubble('onNumberChange', {
                            numberId: this.owner.name,
                            value: parseInt(
                              this.owner.$.orderlineQty.getValue(),
                              10
                            )
                          });
                        }
                      }
                    },
                    {
                      kind: 'OB.UI.EditNumber',
                      name: 'orderlineQty',
                      min: 0,
                      classes: 'obrdm-orderline-qty'
                    },
                    {
                      classes: 'obrdm-button-minus-plus obrdm-button-plus',
                      initComponents: function() {
                        this.setContent(
                          OB.I18N.getLabel('OBMOBC_Character')[4]
                        );
                      },
                      tap: function() {
                        var qty = parseInt(
                            this.owner.$.orderlineQty.getValue(),
                            10
                          ),
                          max = this.owner.$.orderlineQty.getMax();
                        if (!max || qty < max) {
                          this.owner.$.orderlineQty.setValue(qty + 1);
                          this.bubble('onNumberChange', {
                            numberId: this.owner.name,
                            value: parseInt(
                              this.owner.$.orderlineQty.getValue(),
                              10
                            )
                          });
                        }
                      }
                    }
                  ]
                }
              ]
            },
            {
              classes: 'obrdm-cell-orderline-right-col2',
              components: [
                {
                  classes: 'obrdm-cell-orderline-right-container',
                  components: [
                    {
                      classes: 'obrdm-cell-orderline-button',
                      name: 'orderlineWarehouse',
                      tap: function() {
                        this.bubble('onHideSelector');
                        this.bubble('onShowPopup', {
                          popup: 'OBRDM_ModalWarehouseSelector',
                          args: {
                            context: this,
                            product: this.owner.model.get('productId'),
                            callback: function(
                              context,
                              warehouseId,
                              warehouseName
                            ) {
                              context.bubble('onShowSelector');
                              if (warehouseId) {
                                context.owner.model.set(
                                  'warehouseId',
                                  warehouseId,
                                  {
                                    silent: true
                                  }
                                );
                                context.owner.model.set(
                                  'warehouseName',
                                  warehouseName
                                );
                              }
                            }
                          }
                        });
                      }
                    }
                  ]
                }
              ]
            },
            {
              classes: 'obrdm-clear-both'
            },
            {
              classes: 'obrdm-cell-orderline-right-col1',
              components: [
                {
                  classes: 'obrdm-text-line obrdm-orderline-deliverymode',
                  name: 'orderlineDeliveryMode'
                }
              ]
            },
            {
              classes: 'obrdm-clear-both'
            }
          ]
        }
      ]
    }
  ],
  events: {
    onHideThisPopup: '',
    onChangeLine: '',
    onChangeAllLines: ''
  },
  handlers: {
    onNumberChange: 'numberChange',
    onTapOrderIcon: 'tapOrderIcon',
    onTapLineIcon: 'tapLineIcon'
  },

  changeIconClass: function(icon, mode) {
    icon.removeClass('obrdm-checkbox-half-on');
    icon.removeClass('obrdm-checkbox-on');
    icon.removeClass('obrdm-checkbox-off');
    if (mode === 'OFF') {
      icon.addClass('obrdm-checkbox-off');
    } else if (mode === 'ON') {
      icon.addClass('obrdm-checkbox-on');
    } else {
      icon.addClass('obrdm-checkbox-half-on');
    }
  },

  numberChange: function(inSender, inEvent) {
    if (inEvent.value === 0) {
      this.changeIconClass(this.$.iconOrderLine, 'OFF');
    } else if (inEvent.value === this.model.get('qtyPending')) {
      this.changeIconClass(this.$.iconOrderLine, 'ON');
    } else {
      this.changeIconClass(this.$.iconOrderLine, 'HALF');
    }
    this.model.set('toPrepare', inEvent.value, {
      silent: true
    });
    this.doChangeLine({
      orderId: this.model.get('orderId')
    });
    this.model.trigger('verifyPrepareSelectedButton', this.model);
    return true;
  },

  tapOrderIcon: function() {
    var iconClasses = this.$.iconOrder.getClassAttribute(),
      mode = iconClasses === 'obrdm-checkbox-on' ? 'OFF' : 'ON';

    this.changeIconClass(this.$.iconOrder, mode);
    this.doChangeAllLines({
      orderId: this.model.get('id'),
      mode: mode
    });
    this.model.trigger('verifyPrepareSelectedButton', this.model);
    return true;
  },

  tapLineIcon: function() {
    var qty,
      iconClasses = this.$.iconOrderLine.getClassAttribute().split(' ');
    if (iconClasses[1] === 'obrdm-checkbox-on') {
      qty = 0;
    } else {
      qty = this.model.get('qtyPending');
    }
    this.$.orderlineQty.setValue(qty);
    this.numberChange(null, {
      value: qty
    });
    return true;
  },

  create: function() {
    var me = this;
    this.inherited(arguments);
    this.model.set('renderCmp', this, {
      silent: true
    });
    if (this.model.get('ltype') === 'ORDER') {
      this.owner.addClass('obrdm-order');
      this.$.orderline.hide();
      this.$.order.show();
      this.$.documentNo.setContent(this.model.get('documentNo'));
      this.$.bpName.setContent(' / ' + this.model.get('bpName'));
      this.$.orderedDate.setContent(' / ' + this.model.get('orderedDate'));
    } else {
      this.owner.addClass('obrdm-orderline');
      this.$.order.hide();
      this.$.orderline.show();
      this.$.orderlineInfo.setContent(
        OB.I18N.getLabel('OBRDM_LblLineInfo', [
          this.model.get('lineNo'),
          this.model.get('qtyOrdered'),
          this.model.get('qtyPending'),
          this.model.get('dateDelivered')
        ])
      );
      var deliveryMode = this.model.get('deliveryMode'),
        delivery = _.find(OB.MobileApp.model.get('deliveryModes'), function(
          dm
        ) {
          return deliveryMode === dm.id;
        });
      this.$.orderlineProduct.setContent(this.model.get('productName'));
      this.$.orderlineDeliveryMode.setContent(
        delivery ? delivery.name : deliveryMode
      );
      this.$.orderlineWarehouse.setContent(
        OB.MobileApp.model.get('warehouses').find(function(wh) {
          return wh.warehouseid === me.model.get('warehouseId');
        }).warehousename
      );
      if (
        OB.MobileApp.model.hasPermission('OBRDM_warehouse.preparation', true)
      ) {
        this.$.orderlineWarehouse.tap = this.onTapCloseButton;
        this.$.orderlineWarehouse.removeClass('obrdm-cell-orderline-button');
        this.$.orderlineWarehouse.addClass(
          'obrdm-cell-orderline-button-disabled'
        );
      }
      this.$.orderlineQty.setMax(this.model.get('qtyPending'));
      var qty = this.model.get('toPrepare') ? this.model.get('toPrepare') : 0;
      this.$.orderlineQty.setValue(qty);
      this.numberChange(null, {
        value: qty
      });
      OB.UTIL.HookManager.executeHooks(
        'OBRDM_ShowLineInfo',
        {
          line: this.model,
          showInfo: false
        },
        function(args) {
          if (me.$.orderlineInfoBtn) {
            if (args && args.showInfo) {
              me.$.orderlineInfoBtn.showinfoplus = true;
              me.$.orderlineInfoBtn.addRemoveClass(
                'obrdm-button-info-normal',
                false
              );
              me.$.orderlineInfoBtn.addRemoveClass(
                'obrdm-button-info-plus',
                true
              );
            } else {
              me.$.orderlineInfoBtn.showinfoplus = false;
              me.$.orderlineInfoBtn.addRemoveClass(
                'obrdm-button-info-plus',
                false
              );
              me.$.orderlineInfoBtn.addRemoveClass(
                'obrdm-button-info-normal',
                true
              );
            }
          }
        }
      );
    }
  }
});

/* Scrollable table (body of modal) */
enyo.kind({
  name: 'OBRDM.UI.ListOrders',
  classes: 'row-fluid',
  handlers: {
    onClearFilterSelector: 'clearAction',
    onSearchAction: 'searchAction',
    onSelectAll: 'selectAll',
    onPrepareSelected: 'prepareSelected',
    onChangeLine: 'changeLine',
    onChangeAllLines: 'changeAllLines'
  },
  events: {
    onHideSelector: '',
    onShowSelector: ''
  },
  components: [
    {
      classes: 'span12',
      components: [
        {
          classes: 'row-fluid obrdm-list-orders',
          components: [
            {
              classes: 'span12',
              components: [
                {
                  name: 'stOrderSelector',
                  kind: 'OB.UI.ScrollableTable',
                  scrollAreaMaxHeight: '420px',
                  renderHeader: 'OBRDM.UI.ModalOrderScrollableHeader',
                  renderLine: 'OBRDM.UI.ListOrdersLine',
                  renderEmpty: 'OB.UI.RenderEmpty'
                },
                {
                  name: 'renderLoading',
                  classes: 'obrdm-list-orders obrdm-list-orders-renderloading',
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

  changeAllLines: function(inSender, inEvent) {
    var order = _.find(this.$.stOrderSelector.collection.models, function(ord) {
      return ord.get('ltype') === 'ORDER' && ord.get('id') === inEvent.orderId;
    });
    if (order) {
      _.each(order.get('lines'), function(line) {
        var qty = 0,
          cmp = line.get('renderCmp');
        if (inEvent.mode === 'ON') {
          qty = line.get('qtyPending');
        }
        cmp.$.orderlineQty.setValue(qty);
        cmp.changeIconClass(cmp.$.iconOrderLine, inEvent.mode);
        line.set('toPrepare', qty, {
          silent: true
        });
      });
    }
    return true;
  },

  changeLine: function(inSender, inEvent) {
    var order = _.find(this.$.stOrderSelector.collection.models, function(ord) {
      return ord.get('ltype') === 'ORDER' && ord.get('id') === inEvent.orderId;
    });
    if (order) {
      var none = true,
        completed = true;
      _.each(order.get('lines'), function(line) {
        if (line.get('toPrepare') > 0) {
          none = false;
        }
        if (line.get('toPrepare') < line.get('qtyPending')) {
          completed = false;
        }
      });
      var status = none ? 'OFF' : completed ? 'ON' : 'HALF',
        cmp = order.get('renderCmp');
      cmp.changeIconClass(cmp.$.iconOrder, status);
    }
    return true;
  },

  selectAll: function(inSender, inEvent) {
    var mode = inEvent.selectAll ? 'ON' : 'OFF';
    _.each(
      this.$.stOrderSelector.collection.models,
      function(order) {
        var cmp = order.get('renderCmp');
        cmp.changeIconClass(cmp.$.iconOrder, mode);
        this.changeAllLines(null, {
          orderId: order.get('id'),
          mode: mode
        });
      },
      this
    );
    this.$.stOrderSelector.$.theader.$.modalOrderScrollableHeader.$.buttonPrepareSelected.disableButton(
      mode === 'OFF'
    );
    return true;
  },

  getNextOrderPreNoSuffixInPinckinglist: function(tx, callback) {
    var criteria = {
        posSearchKey: OB.MobileApp.model.get('terminalName')
      },
      suffixNext;
    OB.Dal.findInTransaction(tx, OB.Model.DocumentSequence, criteria, function(
      documentSequenceList
    ) {
      if (documentSequenceList && documentSequenceList.length !== 0) {
        suffixNext =
          documentSequenceList.at(0).get('orderPreDocumentSequence') + 1;
        callback({
          orderprenoPreffix: OB.MobileApp.model.get('terminal')
            .orderPreDocNoPrefix,
          orderprenoSuffix: suffixNext
        });
      }
    });
  },

  updateOrderPreDocSequence: function(suffix, tx) {
    var newValue = suffix - 1,
      criteria = {
        posSearchKey: OB.MobileApp.model.get('terminalName')
      },
      docSeqModel;
    OB.Dal.findInTransaction(tx, OB.Model.DocumentSequence, criteria, function(
      documentSequenceList
    ) {
      if (documentSequenceList && documentSequenceList.length !== 0) {
        docSeqModel = documentSequenceList.at(0);
        docSeqModel.set('orderPreDocumentSequence', newValue);
      }
      OB.Dal.saveInTransaction(tx, docSeqModel, null, null);
    });
  },

  prepareSelected: function(inSender, inEvent) {
    var showApproval = false,
      me = this,
      obposPrepaymentlimitamt,
      payment;

    function continueExecution(model) {
      var me = model,
        lines = [],
        documentNo,
        orderedDate,
        headerDescription,
        orderTotal,
        bpartner;
      _.each(model.ordersList.models, function(line) {
        if (line.get('ltype') === 'ORDER') {
          documentNo = line.get('documentNo');
          orderedDate = line.get('orderedDate');
          headerDescription = line.get('headerDescription');
          orderTotal = line.get('orderTotal');
          bpartner = line.get('bpartner');
          obposPrepaymentlimitamt = line.get('obposPrepaymentlimitamt');
          payment = line.get('payment');
          if (
            OB.MobileApp.model.get('terminal').terminalType
              .calculateprepayments &&
            payment < obposPrepaymentlimitamt
          ) {
            showApproval = true;
          }
        } else if (
          line.get('ltype') === 'ORDERLINE' &&
          line.get('toPrepare') > 0
        ) {
          var newline = {};
          var prop;
          for (prop in line.attributes) {
            if (line.attributes.hasOwnProperty(prop)) {
              newline[prop] = line.get(prop);
            }
          }
          delete newline.renderCmp; // remove cycles
          newline.documentNo = documentNo;
          newline.orderedDate = orderedDate;
          newline.headerDescription = headerDescription;
          newline.orderTotal = orderTotal;
          newline.bpartner = bpartner;
          lines.push(newline);
          OB.debug(
            'Prepare Order "' +
            documentNo +
            '", Line: ' +
            line.get('productName') +
            ', Qty: ' +
            line.get('qtyOrdered') +
            ', Pend: ' +
            line.get('qtyPending') + //
              ', Del: ' +
              line.get('qtyDelivered') +
              ', Prepare: ' +
              line.get('toPrepare')
          );
        }
      });
      if (lines.length > 0) {
        OB.UTIL.HookManager.executeHooks(
          'OBRDM_PrePreparedLines',
          {
            lines: lines
          },
          function(args) {
            if (args && args.cancellation && args.cancellation === true) {
              me.$.stOrderSelector.$.theader.$.modalOrderScrollableHeader.$.buttonPrepareSelected.preparing = false;
              me.doHideSelector();
              var buttons = [],
                stockAvail = false;
              _.each(args.errors, function(line) {
                if (line.stockAvail > 0) {
                  stockAvail = true;
                }
              });
              if (stockAvail || lines.length > args.errors.length) {
                buttons.push({
                  label: OB.I18N.getLabel('OBRDM_lblPrepareStockAssign'),
                  action: function() {
                    me.doShowSelector();
                    me.ordersList.trigger(
                      'verifyPrepareSelectedButton',
                      me.ordersList.at(0)
                    );
                    _.each(args.errors, function(line) {
                      var orderline = _.find(me.ordersList.models, function(
                        ol
                      ) {
                        return ol.get('orderLineId') === line.orderLineId;
                      });
                      if (orderline) {
                        var cmp = orderline.get('renderCmp');
                        cmp.$.orderlineQty.setValue(line.stockAvail);
                        cmp.changeIconClass(
                          cmp.$.iconOrderLine,
                          line.stockAvail === 0 ? 'OFF' : 'HALF'
                        );
                        orderline.set('toPrepare', line.stockAvail, {
                          silent: true
                        });
                      }
                    });
                    var lastOrderId = '';
                    _.each(args.errors, function(line) {
                      if (line.orderId !== lastOrderId) {
                        lastOrderId = line.orderId;
                        me.changeLine(null, {
                          orderId: line.orderId
                        });
                      }
                    });
                  }
                });
              }
              buttons.push({
                label: OB.I18N.getLabel('OBPOS_Cancel'),
                action: function() {
                  me.ordersList.trigger(
                    'verifyPrepareSelectedButton',
                    me.ordersList.at(0)
                  );
                  me.doShowSelector();
                }
              });
              OB.UTIL.showConfirmation.display(
                OB.I18N.getLabel('OBRDM_lblPrepareCheckCaption'),
                args.content,
                buttons,
                {
                  autoDismiss: false,
                  onShowFunction: function(popup) {
                    popup.$.bodyButtons.addClass('u-clearBoth');
                  },
                  onHideFunction: function() {
                    me.doShowSelector();
                  },
                  classes: 'obrdm-prepare-confirmation'
                }
              );
            } else {
              var prepareDate = new Date(),
                groupedLinesToPrepare = [],
                mappingGroupedDelivery = {
                  DeferredCarriedAway: 'PickupInStore',
                  DeferredCarriedAwayDate: 'PickupInStore'
                };

              me.$.stOrderSelector.$.theader.$.modalOrderScrollableHeader.$.filterSelector.clearFilter();
              me.$.stOrderSelector.$.theader.$.modalOrderScrollableHeader.$.buttonPrepareSelected.preparing = false;
              me.doHideSelector({
                selectorHide: false
              });

              _.each(lines, function(line) {
                var groupedDeliveryMode = mappingGroupedDelivery[
                  line.deliveryMode
                ]
                  ? mappingGroupedDelivery[line.deliveryMode]
                  : line.deliveryMode;
                var item = _.find(groupedLinesToPrepare, function(grp) {
                  return (
                    grp.bpId === line.bpId &&
                    grp.warehouseId === line.warehouseId &&
                    grp.deliveryMode === groupedDeliveryMode &&
                    grp.bp.bpLocId === line.bpartner.bpLocId
                  );
                });
                if (!item) {
                  item = {
                    id: OB.UTIL.get_UUID(),
                    posTerminal: OB.MobileApp.model.get('terminal').id,
                    organizationId: OB.MobileApp.model.get('terminal')
                      .organization,
                    bpId: line.bpId,
                    warehouseId: line.warehouseId,
                    prepareDate: prepareDate,
                    deliveryMode: groupedDeliveryMode,
                    bp: line.bpartner,
                    orgName: OB.MobileApp.model.get('terminal')
                      .organization$_identifier,
                    orgAddress: OB.MobileApp.model.get('terminal')
                      .organizationAddressIdentifier,
                    salesRepresentative_identifier: !OB.UTIL.isNullOrUndefined(
                      OB.MobileApp.model.receipt.get(
                        'salesRepresentative$_identifier'
                      )
                    )
                      ? OB.MobileApp.model.receipt.get(
                          'salesRepresentative$_identifier'
                        )
                      : OB.MobileApp.model.get('context').user._identifier,
                    updatedBy: line.updatedBy,
                    documentNo: line.documentNo,
                    orderedDate: line.orderedDate,
                    printDate: OB.I18N.formatDate(new Date()),
                    headerDescription: line.headerDescription,
                    orderTotal: line.orderTotal,
                    lines: [],
                    order: null
                  };
                  groupedLinesToPrepare.push(item);
                }
                var newline = {};
                var prop;
                for (prop in line) {
                  if (line.hasOwnProperty(prop)) {
                    newline[prop] = line[prop];
                  }
                }
                newline.qty = newline.toPrepare;
                newline.description = line.description;
                delete newline.renderCmp; // remove cycles
                item.lines.push(newline);
                if (item.order === null) {
                  item.order = newline.orderId;
                }
              });

              var process = new OB.DS.Process(
                'org.openbravo.retail.posterminal.process.IssueSalesOrderLines'
              );
              process.exec(
                {
                  orders: groupedLinesToPrepare
                },
                function(data) {
                  var message;
                  if (data && data.exception) {
                    message = data.exception.status.errorMessage;
                    OB.UTIL.showConfirmation.display(
                      OB.I18N.getLabel('OBMOBC_Error'),
                      message,
                      [
                        {
                          label: OB.I18N.getLabel('OBMOBC_LblOk'),
                          action: function() {
                            return true;
                          }
                        }
                      ]
                    );
                  } else {
                    if (data && data.response) {
                      OB.UTIL.showConfirmation.display(
                        OB.I18N.getLabel('OBRDM_IssueSalesOrderTitle'),
                        data.response,
                        [
                          {
                            label: OB.I18N.getLabel('OBMOBC_LblOk'),
                            action: function() {
                              return true;
                            }
                          }
                        ]
                      );

                      _.each(
                        OB.MobileApp.view.$.containerWindow.$.pointOfSale.$.multiColumn.$.leftPanel.$.receiptview.$.orderview.$.listOrderLines.$.tbody.getComponents(),
                        function(component) {
                          var renderOrderLine = component.renderline;
                          if (
                            data.qtyDeliveredByOrderLine.hasOwnProperty(
                              renderOrderLine.model.get('id')
                            )
                          ) {
                            renderOrderLine.model.set(
                              'deliveredQuantity',
                              data.qtyDeliveredByOrderLine[
                                renderOrderLine.model.get('id')
                              ]
                            );
                          }
                        }
                      );
                    }
                  }
                }
              );
            }
          }
        );
      }
    }

    this.$.stOrderSelector.$.theader.$.modalOrderScrollableHeader.$.buttonPrepareSelected.preparing = true;
    _.each(this.ordersList.models, function(line) {
      if (line.get('ltype') === 'ORDERLINE' && line.get('toPrepare') > 0) {
        obposPrepaymentlimitamt = line.get('obposPrepaymentlimitamt');
        payment = line.get('payment');
        if (
          OB.MobileApp.model.get('terminal').terminalType
            .calculateprepayments &&
          payment < obposPrepaymentlimitamt
        ) {
          showApproval = true;
        }
      }
    });
    if (showApproval && !me.owner.owner.args.directNavigation) {
      var approval = _.find(
        OB.MobileApp.model.receipt.get('approvals'),
        function(approval) {
          return (
            approval.approvalType &&
            approval.approvalType.approval ===
              'OBPOS_approval.prepaymentUnderLimit'
          );
        }
      );
      if (!approval) {
        OB.UTIL.Approval.requestApproval(
          OB.MobileApp.view.$.containerWindow.getRoot().model,
          [
            {
              approval: 'OBPOS_approval.prepaymentUnderLimit',
              message: 'OBPOS_approval.prepaymentUnderLimit'
            }
          ],
          function(approved, supervisor, approvalType) {
            if (approved) {
              continueExecution(me);
            } else {
              me.$.stOrderSelector.$.theader.$.modalOrderScrollableHeader.$.buttonPrepareSelected.preparing = false;
              me.$.stOrderSelector.$.theader.$.modalOrderScrollableHeader.$.buttonPrepareSelected.disableButton(
                false
              );
            }
          }
        );
      } else {
        continueExecution(me);
      }
    } else {
      continueExecution(me);
    }
  },

  clearAction: function() {
    this.ordersList.reset();
    this.$.stOrderSelector.$.theader.$.modalOrderScrollableHeader.$.buttonSelectAll.changeTo(
      true
    );
    this.$.stOrderSelector.$.theader.$.modalOrderScrollableHeader.$.buttonSelectAll.setDisabled(
      true
    );
    this.$.stOrderSelector.$.theader.$.modalOrderScrollableHeader.$.buttonPrepareSelected.disableButton(
      true
    );
    return true;
  },

  searchAction: function(inSender, inEvent) {
    var me = this;
    me.$.stOrderSelector.$.theader.$.modalOrderScrollableHeader.$.buttonSelectAll.setDisabled(
      true
    );

    if (!inEvent.advanced) {
      this.waterfall('onDisableSearch');
    }

    function errorCallback(error) {
      if (!inEvent.advanced) {
        me.waterfall('onEnableSearch');
      }
      if (error) {
        OB.UTIL.showError(
          OB.I18N.getLabel('OBPOS_OfflineWindowRequiresOnline')
        );
      }
    }

    function successCallbackOrders(data) {
      if (!inEvent.advanced) {
        me.waterfall('onEnableSearch');
      }
      if (data && !data.exception) {
        me.$.stOrderSelector.$.theader.$.modalOrderScrollableHeader.$.buttonSelectAll.setDisabled(
          data.length === 0
        );
        var ordersLoaded = new Backbone.Collection();
        _.each(data, function(iter) {
          var order = _.find(ordersLoaded.models, function(ord) {
            return ord.id === iter.orderId;
          });
          if (!order) {
            var bpartner = {
              name: iter.bpName,
              locName: iter.bpLocName,
              addressLine2: iter.addressLine2,
              cityName: iter.bpCityName,
              postalCode: iter.bpPostalCode,
              countryName: iter.bpCountryName,
              phone: iter.bpPhone,
              searchKey: iter.bpSearchKey,
              greetingName: iter.bpGreetingName,
              bpLocId: iter.bpLocId,
              obposPrepaymentamt: iter.obposPrepaymentamt,
              obposPrepaymentlimitamt: iter.obposPrepaymentlimitamt,
              payment: iter.payment
            };
            order = new OB.Model.OBRDM_OrderToSelectorIssue({
              id: iter.orderId,
              ltype: 'ORDER',
              documentNo: iter.documentNo,
              orderedDate: moment(iter.orderDate, 'YYYY-MM-DD').format(
                OB.Format.date.toUpperCase()
              ),
              bpName: iter.bpName,
              bpId: iter.bpId,
              headerDescription: iter.headerDescription,
              description: iter.description,
              orderTotal: iter.orderTotal,
              bpartner: bpartner,
              lines: []
            });
            ordersLoaded.add(order);
          }

          var newline = {};
          var prop;
          for (prop in iter) {
            if (iter.hasOwnProperty(prop)) {
              newline[prop] = iter[prop];
            }
          }
          delete newline.renderCmp; // remove cycles
          newline.orderLineId = iter.lineId;
          newline.ltype = 'ORDERLINE';
          newline.qtyPending = iter.qtyOrdered - iter.qtyDelivered;
          newline.dateDelivered = iter.dateDelivered
            ? moment(iter.dateDelivered, 'YYYY-MM-DD').format(
                OB.Format.date.toUpperCase()
              )
            : '';
          newline.description = iter.description;
          order.get('lines').push(new Backbone.Model(newline));
        });
        var lines = [];
        _.each(ordersLoaded.models, function(order) {
          lines.push(order);
          _.each(order.get('lines'), function(line) {
            lines.push(line);
          });
        });
        me.$.stOrderSelector.collection.reset(lines);
        me.$.renderLoading.hide();
        me.$.stOrderSelector.$.theader.$.modalOrderScrollableHeader.$.buttonSelectAll.changeTo(
          true
        );
      } else {
        OB.UTIL.showError(OB.I18N.getLabel(data.exception.message));
        me.$.stOrderSelector.collection.reset();
        me.$.renderLoading.hide();
        me.$.stOrderSelector.$.tempty.show();
      }
    }

    this.$.stOrderSelector.$.tempty.hide();
    this.$.stOrderSelector.$.tbody.hide();
    this.$.stOrderSelector.$.tlimit.hide();
    this.$.renderLoading.show();

    var filters = [];
    _.each(inEvent.filters, function(flt) {
      filters.push({
        column: flt.column,
        operator: flt.operator,
        value: flt.value
      });
    });

    var orderId = _.find(inEvent.filters, function(flt) {
      return flt.column === 'orderId';
    });
    OB.UTIL.HookManager.executeHooks(
      'OBRDM_GetExcludedDeliveryModes',
      {
        currentReceipt: orderId !== undefined,
        excludedModes: ['PickAndCarry']
      },
      function(args) {
        var process = new OB.DS.Process(
          'org.openbravo.retail.posterminal.master.PendingOrderLines'
        );
        process.exec(
          {
            excluded: OBRDM.UTIL.deliveryModesForFilter(args.excludedModes),
            storeDocTypes: _.pluck(
              OB.MobileApp.model.get('OBRDM_storeDocumentTypes'),
              'id'
            ),
            _limit: OB.Model.OrderToIssue.prototype.dataLimit,
            remoteFilters: filters,
            orderby: inEvent.advanced
              ? inEvent.orderby
              : {
                  name:
                    filters[0].column === 'orderIds'
                      ? 'documentNo'
                      : filters[0].column,
                  direction: 'asc'
                },
            parameters: {
              remoteModel: true,
              originServer: inEvent.originServer
            }
          },
          successCallbackOrders,
          errorCallback
        );
      }
    );

    return true;
  },

  ordersList: null,

  init: function(model) {
    var me = this,
      terminal = OB.POS.modelterminal.get('terminal');
    this.ordersList = new Backbone.Collection();
    this.$.stOrderSelector.setCollection(this.ordersList);
    this.templatePreparation = new OB.DS.HWResource(
      terminal.printOrderPreparationTemplate || '',
      'printOrderPreparationTemplate'
    );
    this.ordersList.on('verifyPrepareSelectedButton', function(item) {
      var disablePrepareSelected = true,
        setAllSelected = true;
      _.each(me.ordersList.models, function(e) {
        if (!OB.UTIL.isNullOrUndefined(e.get('toPrepare'))) {
          if (e.get('toPrepare') && disablePrepareSelected) {
            disablePrepareSelected = false;
          }
          if (!OB.UTIL.isNullOrUndefined(e.get('qtyOrdered'))) {
            if (e.get('toPrepare') < e.get('qtyOrdered') && setAllSelected) {
              setAllSelected = false;
            }
          }
        }
      });
      me.$.stOrderSelector.$.theader.$.modalOrderScrollableHeader.$.buttonPrepareSelected.disableButton(
        disablePrepareSelected
      );
      me.$.stOrderSelector.$.theader.$.modalOrderScrollableHeader.$.buttonSelectAll.changeTo(
        !setAllSelected
      );
    });
  }
});

/* Modal definition */
enyo.kind({
  kind: 'OB.UI.ModalSelector',
  name: 'OBRDM.UI.ModalOrderSelector',
  topPosition: '45px',
  classes: 'obrdm-modal-order-selector',
  i18nHeader: 'OBRDM_LblSelectOrders',
  body: {
    kind: 'OBRDM.UI.ListOrders'
  },
  executeOnShow: function() {
    if (!this.initialized) {
      this.inherited(arguments);
      this.getFilterSelectorTableHeader().clearFilter();
      if (this.args.orderIds) {
        this.$.body.$.listOrders.searchAction(null, {
          filters: [
            {
              column: 'orderIds',
              value: this.args.orderIds.join(','),
              operator: OB.Dal.EQ
            }
          ],
          orderby: {
            column: 'lineNo',
            name: 'lineNo',
            direction: 'asc'
          },
          advanced: false,
          originServer: this.args.originServer
        });
      }
      if (!this.notClear) {
        this.$.body.$.listOrders.$.stOrderSelector.$.theader.$.modalOrderScrollableHeader.$.buttonPrepareSelected.disableButton(
          true
        );
        this.$.body.$.listOrders.$.stOrderSelector.$.theader.$.modalOrderScrollableHeader.$.buttonSelectAll.setDisabled(
          true
        );
      }
      OB.MobileApp.view.scanningFocus(false);
    }
    if (this.args.hookCallback) {
      this.args.hookCallback();
    }
  },
  executeOnHide: function() {
    this.inherited(arguments);
    OB.MobileApp.view.scanningFocus(true);
  },
  getFilterSelectorTableHeader: function() {
    return this.$.body.$.listOrders.$.stOrderSelector.$.theader.$
      .modalOrderScrollableHeader.$.filterSelector;
  },
  getAdvancedFilterBtn: function() {
    return this.$.body.$.listOrders.$.stOrderSelector.$.theader.$
      .modalOrderScrollableHeader.$.buttonAdvancedFilter;
  },
  getAdvancedFilterDialog: function() {
    return 'OBRDM_ModalAdvancedFilterOrder';
  }
});

OB.UI.WindowView.registerPopup('OB.OBPOSPointOfSale.UI.PointOfSale', {
  kind: 'OBRDM.UI.ModalOrderSelector',
  name: 'OBRDM_ModalOrderSelector'
});

/* Advanced filter definition */
enyo.kind({
  kind: 'OB.UI.ModalAdvancedFilters',
  name: 'OBRDM.UI.ModalAdvancedFilterOrder',
  initComponents: function() {
    this.inherited(arguments);
    this.setFilters(OB.Model.OBRDM_OrderFilter.getProperties());
  }
});

OB.UI.WindowView.registerPopup('OB.OBPOSPointOfSale.UI.PointOfSale', {
  kind: 'OBRDM.UI.ModalAdvancedFilterOrder',
  name: 'OBRDM_ModalAdvancedFilterOrder'
});
