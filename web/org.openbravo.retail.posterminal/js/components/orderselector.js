/*
 ************************************************************************************
 * Copyright (C) 2019-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, Backbone, moment, OBRDM, _ */

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OBRDM.UI.ButtonSelectAll',
  classes: 'obrdmUiButtonSelectAll',
  i18nLabel: 'OBPOS_lblSelectAll',
  selectAll: true,
  initComponents: function() {
    this.popup = OB.UTIL.getPopupFromComponent(this);
    this.inherited(arguments);
  },
  changeTo: function(select) {
    if (!select) {
      this.setContent(OB.I18N.getLabel('OBRDM_LblUnSelectAll'));
    } else {
      this.setContent(OB.I18N.getLabel('OBPOS_lblSelectAll'));
    }
    this.selectAll = select;
  },
  tap: function() {
    if (this.disabled) {
      return;
    }
    this.popup.$.body.$.listOrders.selectAll(null, {
      selectAll: this.selectAll
    });
    this.changeTo(!this.selectAll);
  }
});

enyo.kind({
  kind: 'OB.UI.ButtonAdvancedFilter',
  name: 'OBRDM.UI.ButtonAdvancedFilter',
  classes: 'obrdmUiButtonAdvancedFilter',
  dialog: 'OBRDM_ModalAdvancedFilterOrder'
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OBRDM.UI.ButtonPrepareSelected',
  classes: 'obrdmUiButtonPrepareSelected',
  i18nLabel: 'OBRDM_LblPrepareSelected',
  initComponents: function() {
    this.popup = OB.UTIL.getPopupFromComponent(this);
    this.inherited(arguments);
  },
  tap: function() {
    if (this.disabled) {
      return;
    }
    this.disableButton(true);
    this.popup.$.body.$.listOrders.prepareSelected();
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
  classes: 'obrdmUiModalOrderScrollableHeader',
  components: [
    {
      name: 'filterSelector',
      kind: 'OB.UI.FilterSelectorTableHeader',
      classes: 'obrdmUiModalOrderScrollableHeader-filterSelector',
      filters: OB.Model.OBRDM_OrderFilter.getProperties()
    }
  ]
});

enyo.kind({
  name: 'OBRDM.UI.ModalOrderFooter',
  classes: 'obrdmUiModalOrderFooter',
  components: [
    {
      classes:
        'obUiModal-footer-secondaryButtons obrdmUiModalOrderFooter-container1',
      components: [
        {
          kind: 'OBRDM.UI.ButtonAdvancedFilter',
          classes:
            'obrdmUiModalOrderFooter-container1-obrdmUiButtonAdvancedFilter'
        },
        {
          kind: 'OBRDM.UI.ButtonSelectAll',
          classes: 'obrdmUiModalOrderFooter-container1-obrdmUiButtonSelectAll'
        }
      ]
    },
    {
      classes:
        'obUiModal-footer-mainButtons obrdmUiModalOrderFooter-container2',
      components: [
        {
          kind: 'OB.UI.ModalDialogButton',
          i18nContent: 'OBMOBC_LblCancel',
          classes: 'obrdmUiModalOrderFooter-container2-cancel',
          tap: function() {
            this.doHideThisPopup();
          }
        },
        {
          kind: 'OBRDM.UI.ButtonPrepareSelected',
          classes:
            'obrdmUiModalOrderFooter-container2-obrdmUiButtonPrepareSelected',
          isDefaultAction: true
        }
      ]
    }
  ]
});

/* items of collection */
enyo.kind({
  name: 'OBRDM.UI.ListOrdersLine',
  kind: 'OB.UI.listItemButton',
  classes: 'obrdmUiListOrdersLine',
  components: [
    {
      name: 'order',
      classes: 'obrdmUiListOrdersLine-order',
      components: [
        {
          name: 'iconOrder',
          classes: 'obrdmUiListOrdersLine-order-iconOrder obrdmCheckbox_halfOn',
          tap: function() {
            this.bubble('onTapOrderIcon');
          }
        },
        {
          classes: 'obrdmUiListOrdersLine-order-documentNo',
          name: 'documentNo'
        },
        {
          name: 'bpName',
          classes: 'obrdmUiListOrdersLine-order-bpName'
        },
        {
          name: 'orderedDate',
          classes: 'obrdmUiListOrdersLine-order-orderedDate'
        },
        {
          classes: 'obrdmUiListOrdersLine-order-element1'
        }
      ]
    },
    {
      name: 'orderline',
      classes: 'obrdmUiListOrdersLine-orderLine',
      components: [
        {
          classes: 'obrdmUiListOrdersLine-orderLine-container1',
          components: [
            {
              name: 'iconOrderLine',
              classes:
                'obrdmUiListOrdersLine-orderLine-container1-iconOrderLine obrdmCheckbox_off',
              tap: function() {
                this.bubble('onTapLineIcon');
              }
            },
            {
              classes: 'obrdmUiListOrdersLine-orderLine-container1-container1',
              components: [
                {
                  name: 'orderlineInfo',
                  classes:
                    'obrdmUiListOrdersLine-orderLine-container1-container1-orderlineInfo'
                },
                {
                  name: 'orderlineProduct',
                  classes:
                    'obrdmUiListOrdersLine-orderLine-container1-container1-orderlineProduct'
                },
                {
                  name: 'orderlineInfoBtn',
                  classes:
                    'obrdmUiListOrdersLine-orderLine-container1-container1-orderlineInfoBtn',
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
          classes: 'obrdmUiListOrdersLine-orderLine-container2',
          components: [
            {
              classes:
                'obrdmUiListOrdersLine-orderLine-container2-container1 obrdm-cell-orderline-right-col1',
              components: [
                {
                  classes:
                    'obrdmUiListOrdersLine-orderLine-container2-container1-prepare',
                  initComponents: function() {
                    this.setContent(OB.I18N.getLabel('OBRDM_LblPrepare'));
                  }
                },
                {
                  classes:
                    'obrdmUiListOrdersLine-orderLine-container2-container1-container1',
                  components: [
                    {
                      classes:
                        'obrdmUiListOrdersLine-orderLine-container2-container1-container1-element1',
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
                      classes:
                        'obrdmUiListOrdersLine-orderLine-container2-container1-container1-orderlineQty',
                      min: 0
                    },
                    {
                      classes:
                        'obrdmUiListOrdersLine-orderLine-container2-container1-container1-element2',
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
              classes: 'obrdmUiListOrdersLine-orderLine-container2-container2',
              components: [
                {
                  classes:
                    'obrdmUiListOrdersLine-orderLine-container2-container2-container1',
                  components: [
                    {
                      classes:
                        'obrdmUiListOrdersLine-orderLine-container2-container2-container1-orderlineWarehouse',
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
              classes: 'obrdmUiListOrdersLine-orderLine-container2-element1'
            },
            {
              classes: 'obrdmUiListOrdersLine-orderLine-container2-container3',
              components: [
                {
                  name: 'orderlineDeliveryMode',
                  classes:
                    'obrdmUiListOrdersLine-orderLine-container2-container3-orderlineDeliveryMode'
                }
              ]
            },
            {
              classes: 'obrdmUiListOrdersLine-orderLine-container2-element2'
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
    icon.removeClass('obrdmCheckbox_halfOn');
    icon.removeClass('obrdmCheckbox_on');
    icon.removeClass('obrdmCheckbox_off');
    if (mode === 'OFF') {
      icon.addClass('obrdmCheckbox_off');
    } else if (mode === 'ON') {
      icon.addClass('obrdmCheckbox_on');
    } else {
      icon.addClass('obrdmCheckbox_halfOn');
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
      mode = iconClasses === 'obrdmCheckbox_on' ? 'OFF' : 'ON';

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
    if (iconClasses[1] === 'obrdmCheckbox_on') {
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
      if (this.model.get('characteristicDescription')) {
        this.$.orderlineProduct.setContent(
          this.$.orderlineProduct.getContent() +
            ' - ' +
            this.model.get('characteristicDescription')
        );
      }
      if (
        this.model.get('attributeDescription') &&
        OB.MobileApp.model.hasPermission(
          'OBPOS_EnableSupportForProductAttributes',
          true
        )
      ) {
        this.$.orderlineProduct.setContent(
          this.$.orderlineProduct.getContent() +
            OB.I18N.getLabel('OBRDM_LblAttribute') +
            this.model.get('attributeDescription')
        );
      }
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
        this.$.orderlineWarehouse.addClass(
          'obrdmUiListOrdersLine-orderLine-container2-container2-container1-orderlineWarehouse_disabled'
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
                'obrdmUiListOrdersLine-orderLine-container1-container1-orderlineInfoBtn_normal',
                false
              );
              me.$.orderlineInfoBtn.addRemoveClass(
                'obrdmUiListOrdersLine-orderLine-container1-container1-orderlineInfoBtn_plus',
                true
              );
            } else {
              me.$.orderlineInfoBtn.showinfoplus = false;
              me.$.orderlineInfoBtn.addRemoveClass(
                'obrdmUiListOrdersLine-orderLine-container1-container1-orderlineInfoBtn_plus',
                false
              );
              me.$.orderlineInfoBtn.addRemoveClass(
                'obrdmUiListOrdersLine-orderLine-container1-container1-orderlineInfoBtn_normal',
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
  classes: 'obrdmUiListOrders row-fluid',
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
      classes: 'obrdmUiListOrders-container1',
      components: [
        {
          name: 'stOrderSelector',
          kind: 'OB.UI.ScrollableTable',
          classes: 'obrdmUiListOrders-container1-stOrderSelector',
          renderHeader: 'OBRDM.UI.ModalOrderScrollableHeader',
          renderLine: 'OBRDM.UI.ListOrdersLine',
          renderEmpty: 'OB.UI.RenderEmpty'
        },
        {
          name: 'renderLoading',
          classes: 'obrdmUiListOrders-container1-renderLoading',
          showing: false,
          initComponents: function() {
            this.setContent(OB.I18N.getLabel('OBPOS_LblLoading'));
          }
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
    this.popup.$.footer.$.modalOrderFooter.$.buttonPrepareSelected.disableButton(
      mode === 'OFF'
    );
    return true;
  },

  prepareSelected: function(inSender, inEvent) {
    var showApproval = false,
      me = this,
      obposPrepaymentlimitamt,
      payment;

    const isInvoiceCreated = (order, orderLines) => {
      let invoiceCreated = false;
      if (order.invoiceTerms === 'O') {
        // After Order Delivered -> invoice will be generated if full order is delivered
        invoiceCreated = !orderLines.find(
          line => line.qtyPending !== line.toPrepare
        );
      } else if (order.invoiceTerms === 'D') {
        // After Delivery -> invoice will be generated for delivered lines
        invoiceCreated = true;
      }

      if (invoiceCreated && !order.bp.taxID) {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_BP_No_Taxid'));
        return false;
      }

      return invoiceCreated;
    };

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
              me.popup.$.footer.$.modalOrderFooter.$.buttonPrepareSelected.preparing = false;
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
                  classes: 'obrdmPrepareConfirmation'
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
              me.popup.$.footer.$.modalOrderFooter.$.buttonPrepareSelected.preparing = false;
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
                    invoiceTerms: line.invoiceTerms,
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

              Object.keys(
                _.groupBy(groupedLinesToPrepare, l => l.order)
              ).forEach(async key => {
                const order = _.last(
                  groupedLinesToPrepare.filter(l => l.order === key)
                );
                const orderLines = groupedLinesToPrepare
                  .filter(l => l.order === key)
                  .flatMap(l => l.lines);
                if (isInvoiceCreated(order, orderLines)) {
                  const {
                    sequenceName,
                    sequenceNumber,
                    documentNo
                  } = await OB.MobileApp.model.getDocumentNo(
                    'fullinvoiceslastassignednum'
                  );
                  order.invoiceSequenceName = sequenceName;
                  order.invoiceSequenceNumber = sequenceNumber;
                  order.invoiceDocumentNo = documentNo;
                }
              });

              OB.Dal.transaction(function(tx) {
                OB.UTIL.HookManager.executeHooks(
                  'OBPOS_PreIssueSalesOrder',
                  {
                    orders: groupedLinesToPrepare,
                    tx: tx
                  },
                  function() {
                    var process = new OB.DS.Process(
                      'org.openbravo.retail.posterminal.process.IssueSalesOrderLines'
                    );
                    process.exec(
                      {
                        orders: groupedLinesToPrepare
                      },
                      function(data) {
                        if (data && data.exception) {
                          if (data.exception.inSender !== 'timeout') {
                            groupedLinesToPrepare.forEach(async order => {
                              if (order.invoiceDocumentNo) {
                                await OB.App.State.DocumentSequence.decreaseSequence(
                                  {
                                    sequenceName: 'fullinvoiceslastassignednum'
                                  }
                                );
                              }
                            });
                          }
                          OB.UTIL.showConfirmation.display(
                            OB.I18N.getLabel('OBMOBC_Error'),
                            data.exception.status.errorMessage,
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
                              OB.MobileApp.view.$.containerWindow
                                .getRoot()
                                .$.multiColumn.$.leftPanel.$.receiptview.$.orderview.$.listOrderLines.$.tbody.getComponents(),
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

                            OB.UTIL.HookManager.executeHooks(
                              'OBPOS_PostIssueSalesOrder',
                              {
                                orders: data.deliveredOrders
                              },
                              function(args) {
                                OB.debug(
                                  'Executed hooks of OBPOS_PostIssueSalesOrder'
                                );
                                if (args.callback) {
                                  args.callback();
                                }
                              }
                            );
                          }
                        }
                      }
                    );
                  }
                );
              });
            }
          }
        );
      }
    }

    this.popup.$.footer.$.modalOrderFooter.$.buttonPrepareSelected.preparing = true;
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
              me.popup.$.footer.$.modalOrderFooter.$.buttonPrepareSelected.preparing = false;
              me.popup.$.footer.$.modalOrderFooter.$.buttonPrepareSelected.disableButton(
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
    this.popup.$.footer.$.modalOrderFooter.$.buttonSelectAll.changeTo(true);
    this.popup.$.footer.$.modalOrderFooter.$.buttonSelectAll.setDisabled(true);
    this.popup.$.footer.$.modalOrderFooter.$.buttonPrepareSelected.disableButton(
      true
    );
    return true;
  },

  searchAction: function(inSender, inEvent) {
    var me = this;
    me.popup.$.footer.$.modalOrderFooter.$.buttonSelectAll.setDisabled(true);

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
        me.popup.$.footer.$.modalOrderFooter.$.buttonSelectAll.setDisabled(
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
              payment: iter.payment,
              taxID: iter.bpTaxID
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
        me.popup.$.footer.$.modalOrderFooter.$.buttonSelectAll.changeTo(true);
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
      me.popup.$.footer.$.modalOrderFooter.$.buttonPrepareSelected.disableButton(
        disablePrepareSelected
      );
      me.popup.$.footer.$.modalOrderFooter.$.buttonSelectAll.changeTo(
        !setAllSelected
      );
    });
  }
});

/* Modal definition */
enyo.kind({
  kind: 'OB.UI.ModalSelector',
  name: 'OBRDM.UI.ModalOrderSelector',
  classes: 'obrdmUiModalOrderSelector',
  i18nHeader: 'OBRDM_LblSelectOrders',
  body: {
    kind: 'OBRDM.UI.ListOrders'
  },
  footer: {
    kind: 'OBRDM.UI.ModalOrderFooter'
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
        this.$.body.$.listOrders.popup.$.footer.$.modalOrderFooter.$.buttonPrepareSelected.disableButton(
          true
        );
        this.$.body.$.listOrders.popup.$.footer.$.modalOrderFooter.$.buttonSelectAll.setDisabled(
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
  },
  init: function(model) {
    this.inherited(arguments);
    this.$.body.$.listOrders.popup = this;
  }
});

OB.UI.WindowView.registerPopup('OB.OBPOSPointOfSale.UI.PointOfSale', {
  kind: 'OBRDM.UI.ModalOrderSelector',
  name: 'OBRDM_ModalOrderSelector'
});

/* Advanced filter definition */
enyo.kind({
  name: 'OBRDM.UI.ModalAdvancedFilterOrder',
  kind: 'OB.UI.ModalAdvancedFilters',
  classes: 'obrdmUiModalAdvancedFilterOrder',
  initComponents: function() {
    this.inherited(arguments);
    this.setFilters(OB.Model.OBRDM_OrderFilter.getProperties());
  }
});

OB.UI.WindowView.registerPopup('OB.OBPOSPointOfSale.UI.PointOfSale', {
  kind: 'OBRDM.UI.ModalAdvancedFilterOrder',
  name: 'OBRDM_ModalAdvancedFilterOrder'
});
