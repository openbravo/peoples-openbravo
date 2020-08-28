/*
 ************************************************************************************
 * Copyright (C) 2015-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Promise*/

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.ModalSelectOpenedReceipt_btnApply',
  classes: 'obUiModalSelectOpenedReceiptBtnApply',
  isDefaultAction: true,
  i18nContent: 'OBPOS_LblApplyButton',
  tap: function() {
    this.doHideThisPopup();
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.ModalSelectOpenedReceipt_btnCancel',
  classes: 'obUiModalSelectOpenedReceiptBtnCancel',
  isDefaultAction: false,
  i18nContent: 'OBMOBC_LblCancel',
  tap: function() {
    this.doHideThisPopup();
  }
});

enyo.kind({
  kind: 'OB.UI.Modal',
  name: 'OB.UI.ModalSelectOpenedReceipt',
  classes: 'obUiModalSelectOpenedReceipt',
  i18nHeader: 'OBPOS_lblHeaderSelectOpenedReceiptModal',
  //body of the popup
  body: {
    classes: 'obUiModalSelectOpenedReceipt-body',
    components: [
      {
        classes:
          'obUiModalSelectOpenedReceipt-body-lblSelectOpenedReceiptModal',
        name: 'lblSelectOpenedReceiptModal'
      },
      {
        classes:
          'obUiModalSelectOpenedReceipt-body-listSelectOpenedReceiptModal',
        name: 'listSelectOpenedReceiptModal',
        kind: 'OB.UI.OpenedReceiptsList'
      },
      {
        classes: 'obUiModalSelectOpenedReceipt-body-container1',
        components: [
          {
            name: 'chkSelectOpenedReceiptModal',
            classes:
              'obUiModalSelectOpenedReceipt-container1-chkSelectOpenedReceiptModal',
            kind: 'OB.UI.CheckboxButton'
          },
          {
            name: 'lblSelectOpenedReceiptModalChk',
            classes:
              'obUiModalSelectOpenedReceipt-container1-lblSelectOpenedReceiptModalChk',
            initComponents: function() {
              this.setContent(
                OB.I18N.getLabel('OBPOS_lblSelectOpenedReceiptModalChk')
              );
            }
          }
        ]
      }
    ]
  },
  //buttons of the popup
  footer: {
    classes: 'obUiModalSelectOpenedReceipt-footer',
    components: [
      {
        classes:
          'obUiModalSelectOpenedReceipt-footer-obUiModalSelectOpenedReceiptBtnCancel',
        kind: 'OB.UI.ModalSelectOpenedReceipt_btnCancel'
      },
      {
        classes:
          'obUiModalSelectOpenedReceipt-footer-obUiModalSelectOpenedReceiptBtnApply',
        kind: 'OB.UI.ModalSelectOpenedReceipt_btnApply',
        disabled: true,
        checkModifyTax: function(params) {
          return new Promise(async function(resolve, reject) {
            function promiseResolve() {
              resolve(params);
            }

            if (
              params.product.get('modifyTax') &&
              params.attrs.relatedLines &&
              params.attrs.relatedLines.length > 0
            ) {
              var checkCategory = function(data) {
                var i, j, model;
                for (i = 0; i < params.attrs.relatedLines.length; i++) {
                  for (j = 0; j < data.length; j++) {
                    if (data[j] instanceof Backbone.Model) {
                      model = data[j];
                    } else {
                      model = OB.Dal.transform(
                        OB.Model.ProductServiceLinked,
                        data[j]
                      );
                    }
                    if (
                      params.attrs.relatedLines[i].productCategory ===
                      model.get('productCategory')
                    ) {
                      // Found taxes modification configuration
                      // resolve after displaying confirmation message
                      OB.UTIL.showConfirmation.display(
                        OB.I18N.getLabel(
                          'OBPOS_lblHeaderSelectOpenedReceiptModal'
                        ),
                        OB.I18N.getLabel('OBPOS_WillNotModifyTax'),
                        [
                          {
                            label: OB.I18N.getLabel('OBMOBC_LblOk'),
                            isConfirmButton: true,
                            action: promiseResolve
                          }
                        ],
                        {
                          autoDismiss: false,
                          onHideFunction: promiseResolve
                        }
                      );
                      return;
                    }
                  }
                }
                // Not found taxes modification configuration
                // resolve silently
                promiseResolve();
              };
              if (
                OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)
              ) {
                const criteria = {
                  product: params.product.get('id'),
                  remoteFilters: [
                    {
                      columns: ['product'],
                      operator: 'equals',
                      value: params.product.get('id')
                    }
                  ]
                };
                OB.Dal.find(
                  OB.Model.ProductServiceLinked,
                  criteria,
                  function(data) {
                    checkCategory(data.models);
                  },
                  reject
                );
              } else {
                const criteria = new OB.App.Class.Criteria().criterion(
                  'product',
                  params.product.get('id')
                );
                try {
                  const data = await OB.App.MasterdataModels.ProductServiceLinked.find(
                    criteria.build()
                  );
                  checkCategory(data);
                } catch (error) {
                  OB.error(error.message);
                  reject();
                }
              }
            } else {
              // Product to add does not modify taxes
              // resolve silently
              promiseResolve();
            }
          });
        },
        tap: async function() {
          // TODO: Check the behavior with the receipt multi-line selection case.
          // TODO: The 'Undo' button doesn't work in the case the target receipt is opened.
          var me = this;
          var orderModel = this.owner.owner.selectedLine.model;
          var product = this.owner.owner.args.product;
          var attrs = this.owner.owner.args.attrs;

          await this.checkModifyTax({
            product: product,
            attrs: attrs
          });

          if (
            me.owner.owner.selectedLine.id.indexOf('openedReceiptsListLine') ===
            -1
          ) {
            // 'Create New One' case
            OB.MobileApp.model.receipt.trigger('updateView');
            const payload = OB.UTIL.TicketUtils.addTicketCreationDataToPayload();
            payload.ticketExtraProperties.deferredOrder = true;
            payload.businessPartner = JSON.parse(
              JSON.stringify(OB.MobileApp.model.receipt.get('bp'))
            );

            const newDeferredTicket = OB.App.State.Ticket.Utils.newTicket(
              payload
            );
            await OB.App.State.TicketList.saveTicket(newDeferredTicket);
            orderModel = OB.App.StateBackwardCompatibility.getInstance(
              'Ticket'
            ).toBackboneObject(newDeferredTicket);
          }
          const bpClone = new OB.Model.BusinessPartner();
          OB.UTIL.clone(OB.MobileApp.model.receipt.get('bp'), bpClone);
          const current = OB.MobileApp.model.receipt;
          // Change the UI receipt to add the product on the newly created ticket
          await me.owner.owner.doChangeCurrentOrder({
            newCurrentOrder: orderModel,
            callback: function() {
              current.set('bp', bpClone);
              me.owner.owner.doAddProduct({
                targetOrder: orderModel,
                product: product,
                attrs: attrs,
                options: {
                  blockAddProduct: true
                },
                context: me.owner.owner.args.context,
                callback: function() {
                  if (me.owner.owner.args.callback) {
                    me.owner.owner.args.callback();
                  }
                  if (
                    me.owner.owner.$.body.$.chkSelectOpenedReceiptModal.checked
                  ) {
                    me.owner.owner.owner.model
                      .get('order')
                      .calculateReceipt(function() {
                        me.owner.owner.owner.model
                          .get('order')
                          .get('lines')
                          .trigger('updateRelations');
                      });
                  } else {
                    // The UI receipt should be restored
                    me.owner.owner.doChangeCurrentOrder({
                      newCurrentOrder: current,
                      callback: function() {
                        //Hack to calculate totals even if the receipt is not the UI receipt
                        orderModel.setIsCalculateReceiptLockState(false);
                        orderModel.setIsCalculateGrossLockState(false);
                        orderModel.set('belongsToMultiOrder', true);
                        orderModel.calculateReceipt(function() {
                          orderModel.trigger('updateServicePrices');
                          orderModel.set('belongsToMultiOrder', false);
                        });
                      }
                    });
                  }
                }
              });
              me.owner.owner.args.callback = null;
              me.owner.owner.doHideThisPopup();
            }
          });
        }
      }
    ]
  },
  executeOnHide: function() {
    //executed when popup is hiden.
    //to access to argumens -> this.args
    if (this.args.callback) {
      this.args.callback.call(this.args.context, false);
    }
  },
  executeOnShow: function() {
    //executed when popup is shown.
    //to access to argumens -> this.args
    this.uncheckAllItems();
    this.$.body.$.chkSelectOpenedReceiptModal.check();
    this.$.footer.$.modalSelectOpenedReceipt_btnApply.setDisabled(true);
    this.$.body.$.lblSelectOpenedReceiptModal.setContent(
      OB.I18N.getLabel('OBPOS_LblSelectOpenedReceiptModal', [
        this.args.product.attributes._identifier
      ])
    );
  },

  published: {
    receiptsList: null
  },
  receiptsListChanged: function(oldValue) {
    this.$.body.$.listSelectOpenedReceiptModal.setReceiptsList(
      this.receiptsList
    );
  },

  init: function(model) {
    this.$.footer.setStyle('padding-top: 5px');
  },
  events: {
    onChangeCurrentOrder: '',
    onHideThisPopup: '',
    onAddProduct: ''
  },

  selectedLine: null,
  uncheckAllItems: function() {
    var items = this.$.body.$.listSelectOpenedReceiptModal.$
        .openedreceiptslistitemprinter.$.tbody.$,
      buttonContainer,
      control,
      openedReceiptsListLine;

    // Remove grey background to 'Create New Receipt' button
    // TODO: Remove this style
    this.$.body.$.listSelectOpenedReceiptModal.$.button.removeClass('selected');

    // Remove grey background to opened receipts list
    for (control in items) {
      if (items.hasOwnProperty(control)) {
        if (control.substring(0, 7) === 'control') {
          buttonContainer = items[control].$;
          for (openedReceiptsListLine in buttonContainer) {
            if (buttonContainer.hasOwnProperty(openedReceiptsListLine)) {
              if (
                openedReceiptsListLine.substring(0, 22) ===
                'openedReceiptsListLine'
              ) {
                // TODO: Remove this style
                buttonContainer[openedReceiptsListLine].removeClass('selected');
              }
            }
          }
        }
      }
    }
  },
  checkItem: function(line) {
    this.selectedLine = line;
    this.uncheckAllItems();

    // Add grey background to the new selected line
    line.addClass('selected');

    // Enable 'Apply' button
    if (this.$.footer.$.modalSelectOpenedReceipt_btnApply.disabled) {
      this.$.footer.$.modalSelectOpenedReceipt_btnApply.setDisabled(false);
    }
  }
});

enyo.kind({
  name: 'OB.UI.OpenedReceiptsList',
  classes: 'obUiOpenedReceiptsList row-fluid',
  published: {
    receiptsList: null
  },
  components: [
    {
      classes: 'obUiOpenedReceiptsList-conteiner1 span12',
      components: [
        {
          classes: 'obUiOpenedReceiptsList-conteiner1-container1',
          components: [
            {
              kind: 'OB.UI.Button',
              classes:
                'obUiOpenedReceiptsList-conteiner1-container1-obUiButton',
              components: [
                {
                  classes:
                    'obUiOpenedReceiptsList-conteiner1-container1-obUiButton-container1',
                  components: [
                    {
                      classes:
                        'obUiOpenedReceiptsList-conteiner1-container1-obUiButton-container1-container1',
                      components: [
                        {
                          classes:
                            'obUiOpenedReceiptsList-conteiner1-container1-obUiButton-container1-container1-element1',
                          tag: 'img',
                          attributes: {
                            src:
                              '../org.openbravo.mobile.core/assets/img/iconCreateNew-alt.svg'
                          }
                        }
                      ]
                    },
                    {
                      classes:
                        'obUiOpenedReceiptsList-conteiner1-container1-obUiButton-container1-container2',
                      initComponents: function() {
                        this.setContent(
                          OB.I18N.getLabel('OBPOS_LblCreateNewReceipt')
                        );
                      }
                    },
                    {
                      classes:
                        'obUiOpenedReceiptsList-conteiner1-container1-obUiButton-container1-container3',
                      content: '.'
                    },
                    {
                      classes:
                        'obUiOpenedReceiptsList-conteiner1-container1-obUiButton-container1-container4'
                    }
                  ]
                }
              ],
              tap: function() {
                if (this.owner.owner.owner.checkItem) {
                  this.owner.owner.owner.checkItem(this);
                }
              }
            },
            {
              name: 'openedreceiptslistitemprinter',
              classes:
                'obUiOpenedReceiptsList-conteiner1-container1-openedreceiptslistitemprinter',
              kind: 'OB.UI.ScrollableTable',
              //scrollAreaMaxHeight: '189px',
              renderLine: 'OB.UI.OpenedReceiptsListLine',
              renderEmpty: 'OB.UI.RenderEmpty'
            }
          ]
        }
      ]
    }
  ],
  receiptsListChanged: function(oldValue) {
    this.$.openedreceiptslistitemprinter.setCollection(this.receiptsList);
  }
});

enyo.kind({
  name: 'OB.UI.OpenedReceiptsListLine',
  kind: 'OB.UI.SelectButton',
  classes: 'obUiOpenedReceiptsListLine',
  tap: function() {
    this.inherited(arguments);
    if (this.owner.owner.owner.owner.owner.owner.checkItem) {
      this.owner.owner.owner.owner.owner.owner.checkItem(this);
    }
  },
  components: [
    {
      name: 'line',
      classes: 'obUiOpenedReceiptsListLine',
      components: [
        {
          classes: 'obUiOpenedReceiptsListLine-container1',
          components: [
            {
              classes: 'obUiOpenedReceiptsListLine-container1-time',
              name: 'time'
            },
            {
              classes: 'obUiOpenedReceiptsListLine-container1-orderNo',
              name: 'orderNo'
            },
            {
              classes: 'obUiOpenedReceiptsListLine-container1-bp',
              name: 'bp'
            },
            {
              classes: 'obUiOpenedReceiptsListLine-container1-element1'
            }
          ]
        },
        {
          classes: 'obUiOpenedReceiptsListLine-container2',
          components: [
            {
              classes: 'obUiOpenedReceiptsListLine-container2-element1'
            },
            {
              classes: 'obUiOpenedReceiptsListLine-container2-element2'
            },
            {
              classes: 'obUiOpenedReceiptsListLine-container2-total',
              name: 'total'
            },
            {
              classes: 'obUiOpenedReceiptsListLine-container2-element3'
            }
          ]
        }
      ]
    }
  ],
  create: function() {
    this.inherited(arguments);
    if (this.model.get('isPaid') || this.model.get('isLayaway')) {
      this.addClass('u-hideFromUI');
    }
    if (this.model.get('isPaid') || this.model.get('isLayaway')) {
      this.$.time.setContent(OB.I18N.formatDate(this.model.get('orderDate')));
    } else {
      this.$.time.setContent(OB.I18N.formatHour(this.model.get('orderDate')));
    }
    this.$.orderNo.setContent(this.model.get('documentNo'));
    this.$.bp.setContent(this.model.get('bp').get('_identifier'));
    this.$.total.setContent(this.model.printTotal());
    OB.UTIL.HookManager.executeHooks('OBPOS_RenderListReceiptLine', {
      listReceiptLine: this
    });
  }
});
