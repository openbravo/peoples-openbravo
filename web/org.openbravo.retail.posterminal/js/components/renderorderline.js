/*
 ************************************************************************************
 * Copyright (C) 2013-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo */

enyo.kind({
  kind: 'OB.UI.listItemButton',
  name: 'OB.UI.RenderOrderLine',
  classes: 'obUiRenderOrderLine',
  handlers: {
    onChangeEditMode: 'changeEditMode',
    onCheckBoxBehaviorForTicketLine: 'checkBoxForTicketLines',
    onSetMultiSelected: 'setMultiSelected',
    onkeyup: 'keyupHandler'
  },
  tap: function() {
    if (OB.MobileApp.model.get('serviceSearchMode')) {
      return;
    }
    this.model.trigger('selected', this.model);
    this.model.trigger('click', this.model);
  },
  events: {
    onLineChecked: '',
    onShowPopup: '',
    onAdjustOrderCaption: ''
  },
  components: [
    {
      name: 'checkBoxColumn',
      kind: 'OB.UI.CheckboxButton',
      classes: 'obUiRenderOrderLine-checkBoxColumn',
      tag: 'div',
      tap: function() {
        var model = this.owner.model;
        if (this.checked) {
          model.trigger('uncheck', model);
        } else {
          model.trigger('check', model);
        }
        return this;
      }
    },
    {
      name: 'nameContainner',
      tag: 'div',
      classes: 'obUiRenderOrderLine-nameContainner',
      components: [
        {
          name: 'serviceIcon',
          classes: 'obUiRenderOrderLine-nameContainner-serviceIcon'
        },
        {
          name: 'product',
          classes: 'obUiRenderOrderLine-nameContainner-product'
        }
      ]
    },
    {
      kind: 'OB.UI.FitText',
      classes: 'obUiRenderOrderLine-container3 fitText',
      components: [
        {
          tag: 'span',
          name: 'quantity',
          classes: 'obUiRenderOrderLine-container3-quantity'
        }
      ]
    },
    {
      kind: 'OB.UI.FitText',
      classes: 'obUiRenderOrderLine-container4 fitText',
      components: [
        {
          tag: 'span',
          name: 'price',
          classes: 'obUiRenderOrderLine-container4-price'
        }
      ]
    },
    {
      kind: 'OB.UI.FitText',
      classes: 'obUiRenderOrderLine-container5 fitText',
      components: [
        {
          tag: 'span',
          name: 'gross',
          classes: 'obUiRenderOrderLine-container5-gross'
        }
      ]
    },
    {
      classes: 'obUiRenderOrderLine-element6 u-clearBoth'
    }
  ],
  initComponents: function() {
    var me = this,
      order = this.owner.owner.owner.owner.order;

    this.inherited(arguments);
    if (this.model.get('product').get('productType') === 'S') {
      this.$.serviceIcon.show();
      this.$.product.removeClass(
        'obUiRenderOrderLine-nameContainner-product_hideServiceIcon'
      );
      this.$.product.addClass(
        'obUiRenderOrderLine-nameContainner-product_showServiceIcon'
      );
    } else {
      this.$.serviceIcon.hide();
      this.$.product.removeClass(
        'obUiRenderOrderLine-nameContainner-product_showServiceIcon'
      );
      this.$.product.addClass(
        'obUiRenderOrderLine-nameContainner-product_hideServiceIcon'
      );
    }
    this.$.checkBoxColumn.hide();
    this.$.product.setContent(this.setIdentifierContent());
    this.$.quantity.setContent(this.model.printQty());
    this.$.price.setContent(this.model.printPrice());
    if (this.model.get('priceIncludesTax')) {
      this.$.gross.setContent(this.model.printGross());
    } else {
      this.$.gross.setContent(this.model.printNet());
    }
    if (
      OB.MobileApp.model.hasPermission(
        'OBPOS_EnableSupportForProductAttributes',
        true
      ) &&
      this.model.get('product').get('hasAttributes')
    ) {
      var attr_msg = OB.I18N.getLabel('OBPOS_AttributeValue');
      if (this.model.get('attSetInstanceDesc')) {
        attr_msg += this.model.get('attSetInstanceDesc');
      } else if (this.model.get('attributeValue')) {
        attr_msg += this.model.get('attributeValue');
      } else {
        attr_msg += OB.I18N.getLabel('OBPOS_AttributeValueMissing');
      }

      this.createComponent({
        classes: 'obUiRenderOrderLine-container7',
        components: [
          {
            name: 'productAttribute',
            content: attr_msg,
            classes: 'obUiRenderOrderLine-container7-productAttribute',
            attributes: {}
          },
          {
            classes: 'obUiRenderOrderLine-container7-element2 u-clearBoth'
          }
        ]
      });

      if (!this.model.get('attributeValue')) {
        this.$.productAttribute.addClass(
          'obUiRenderOrderLine-container7-productAttribute_red'
        );
      } else {
        this.$.productAttribute.removeClass(
          'obUiRenderOrderLine-container7-productAttribute_red'
        );
      }
    }

    if (this.model.get('product').get('characteristicDescription')) {
      this.createComponent({
        classes: 'obUiRenderOrderLine-container8',
        components: [
          {
            name: 'characteristicsDescription',
            content: OB.UTIL.getCharacteristicValues(
              this.model.get('product').get('characteristicDescription')
            ),
            classes: 'obUiRenderOrderLine-container8-characteristicsDescription'
          },
          {
            classes: 'obUiRenderOrderLine-container8-element2 u-clearBoth'
          }
        ]
      });
    }
    if (OB.UTIL.isCrossStoreLine(this.model)) {
      this.createComponent({
        classes: 'obpos-display-block',
        components: [
          {
            name: 'storeLine',
            content:
              '-- ' +
              OB.I18N.getLabel('OBPOS_LblStore') +
              ': ' +
              this.model.get('organization').name
          },
          {
            classes: 'obpos-clear-both'
          }
        ]
      });
    }
    if (this.model.get('obposSerialNumber')) {
      this.createComponent({
        classes: 'obUiRenderOrderLine-container9',
        components: [
          {
            content: OB.I18N.getLabel('OBPOS_SerialNumber', [
              this.model.get('obposSerialNumber')
            ]),
            classes: 'obUiRenderOrderLine-container9-element1',
            attributes: {}
          },
          {
            classes: 'obUiRenderOrderLine-container9-element2 u-clearBoth'
          }
        ]
      });
    }

    if (order.get('iscancelled')) {
      if (this.model.get('shippedQuantity')) {
        this.createComponent({
          classes: 'obUiRenderOrderLine-container10',
          components: [
            {
              content:
                '-- ' +
                OB.I18N.getLabel('OBPOS_DeliveredQuantity') +
                ': ' +
                this.model.get('shippedQuantity'),
              classes: 'obUiRenderOrderLine-container10-element1',
              attributes: {}
            },
            {
              classes: 'obUiRenderOrderLine-container10-element2 u-clearBoth'
            }
          ]
        });
      } else {
        this.createComponent({
          classes: 'obUiRenderOrderLine-container11',
          components: [
            {
              content: '-- ' + OB.I18N.getLabel('OBPOS_Cancelled'),
              classes: 'obUiRenderOrderLine-container11-element1',
              attributes: {}
            },
            {
              classes: 'obUiRenderOrderLine-container11-element2 u-clearBoth'
            }
          ]
        });
      }
    } else {
      if (this.model.get('deliveredQuantity')) {
        this.createComponent({
          classes: 'obUiRenderOrderLine-container12',
          components: [
            {
              content:
                '-- ' +
                OB.I18N.getLabel('OBPOS_DeliveredQuantity') +
                ': ' +
                this.model.get('deliveredQuantity'),
              classes: 'obUiRenderOrderLine-container12-element1',
              attributes: {}
            },
            {
              classes: 'obUiRenderOrderLine-container12-element2 u-clearBoth'
            }
          ]
        });
      } else if (!this.model.get('obposCanbedelivered')) {
        this.createComponent({
          classes: 'obUiRenderOrderLine-container13',
          components: [
            {
              content: '-- ' + OB.I18N.getLabel('OBPOS_NotDeliverLine'),
              classes: 'obUiRenderOrderLine-container13-element1'
            },
            {
              classes: 'obUiRenderOrderLine-container13-element2 u-clearBoth'
            }
          ]
        });
      }
    }

    if (this.model.get('promotions')) {
      enyo.forEach(
        this.model.get('promotions'),
        function(d) {
          if (d.hidden) {
            // continue
            return;
          }
          var identifierName = d.identifier || d.name;
          var nochunks = d.chunks;
          this.createComponent({
            classes: 'obUiRenderOrderLine-container14',
            components: [
              {
                content:
                  OB.UTIL.isNullOrUndefined(nochunks) || nochunks === 1
                    ? '-- ' + identifierName
                    : '-- ' + '(' + nochunks + 'x) ' + identifierName,
                classes: 'obUiRenderOrderLine-container14-element1',
                attributes: {}
              },
              {
                content: OB.I18N.formatCurrency(-d.amt),
                classes: 'obUiRenderOrderLine-container14-element2',
                attributes: {}
              },
              {
                classes: 'obUiRenderOrderLine-container14-element3 u-clearBoth'
              }
            ]
          });
        },
        this
      );
    }
    if (this.model.get('relatedLines')) {
      if (!this.$.relatedLinesContainer) {
        this.createComponent({
          name: 'relatedLinesContainer',
          classes: 'obUiRenderOrderLine-relatedLinesContainer'
        });
      }
      enyo.forEach(
        this.model.get('relatedLines'),
        function(line) {
          this.$.relatedLinesContainer.createComponent({
            classes: 'obUiRenderOrderLine-relatedLinesContainer-container1',
            components: [
              {
                content: line.otherTicket
                  ? OB.I18N.getLabel('OBPOS_lblRelatedLinesOtherTicket', [
                      line.productName,
                      line.orderDocumentNo
                    ])
                  : OB.I18N.getLabel('OBPOS_lblRelatedLines', [
                      line.productName
                    ]),
                classes:
                  'obUiRenderOrderLine-relatedLinesContainer-container1-element1',
                attributes: {}
              }
            ]
          });
        },
        this
      );
    }
    if (this.model.get('hasRelatedServices')) {
      me.createComponent({
        kind: 'OB.UI.ShowServicesButton',
        name: 'showServicesButton',
        classes: 'obUiRenderOrderLine-showServicesButton'
      });
    } else if (!this.model.has('hasRelatedServices')) {
      this.model.on('showServicesButton', function() {
        me.model.off('showServicesButton');
        me.createComponent({
          kind: 'OB.UI.ShowServicesButton',
          name: 'showServicesButton',
          classes: 'obUiRenderOrderLine-showServicesButton'
        }).render();
      });
    }
    OB.UTIL.HookManager.executeHooks('OBPOS_RenderOrderLine', {
      orderline: this,
      order: order
    });
  },
  keyupHandler: function(inSender, inEvent) {
    var keyCode = inEvent.keyCode;
    if (keyCode === 13 || keyCode === 32) {
      //Handle ENTER and SPACE keys in buttons
      this.executeTapAction();
      return true;
    }
    OB.MobileApp.view.keypressHandler(inSender, inEvent);
  },
  setMultiSelected: function(inSender, inEvent) {
    if (
      inEvent.models &&
      inEvent.models.length > 0 &&
      inEvent.models[0] instanceof OB.Model.OrderLine &&
      this.$.showServicesButton
    ) {
      if (
        inEvent.models.length > 1 ||
        OB.MobileApp.model.get('serviceSearchMode')
      ) {
        this.$.showServicesButton.hide();
      } else {
        this.$.showServicesButton.show();
      }
    }
  },
  changeEditMode: function(inSender, inEvent) {
    this.addRemoveClass('obUiRenderOrderLine_edit', inEvent.edit);
    this.bubble('onShowColumn', {
      colNum: 1
    });
  },
  checkBoxForTicketLines: function(inSender, inEvent) {
    if (inEvent.status) {
      // These inline styles are allowed
      this.$.gross.hasNode().style.width = '18%';
      this.$.quantity.hasNode().style.width = '16%';
      this.$.price.hasNode().style.width = '18%';
      this.$.nameContainner.hasNode().style.width = '37%';
      this.doAdjustOrderCaption({ status: true });

      if (this.$.characteristicsDescription) {
        this.$.characteristicsDescription.addClass(
          'obUiRenderOrderLine-characteristicsDescription_withStatus'
        );
        this.$.characteristicsDescription.removeClass(
          'obUiRenderOrderLine-characteristicsDescription_withoutStatus'
        );
      }
      if (this.$.relatedLinesContainer) {
        this.$.relatedLinesContainer.addClass(
          'obUiRenderOrderLine-relatedLinesContainer_withStatus'
        );
        this.$.relatedLinesContainer.removeClass(
          'obUiRenderOrderLine-relatedLinesContainer_withoutStatus'
        );
      }
      this.$.checkBoxColumn.show();
      this.changeEditMode(this, inEvent.status);
    } else {
      this.$.gross.hasNode().style.removeProperty('width');
      this.$.quantity.hasNode().style.removeProperty('width');
      this.$.price.hasNode().style.removeProperty('width');
      this.$.nameContainner.hasNode().style.removeProperty('width');
      this.doAdjustOrderCaption({ status: false });

      if (this.$.characteristicsDescription) {
        this.$.characteristicsDescription.addClass(
          'obUiRenderOrderLine-characteristicsDescription_withoutStatus'
        );
        this.$.characteristicsDescription.removeClass(
          'obUiRenderOrderLine-characteristicsDescription_withStatus'
        );
      }
      if (this.$.relatedLinesContainer) {
        this.$.relatedLinesContainer.addClass(
          'obUiRenderOrderLine-relatedLinesContainer_withoutStatus'
        );
        this.$.relatedLinesContainer.removeClass(
          'obUiRenderOrderLine-relatedLinesContainer_withStatus'
        );
      }
      this.$.checkBoxColumn.hide();
      this.changeEditMode(this, false);
    }
  },
  setIdentifierContent: function() {
    return this.model.get('product').get('_identifier');
  }
});

enyo.kind({
  name: 'OB.UI.RenderOrderLineEmpty',
  classes: 'obUiRenderOrderLineEmpty',
  initComponents: function() {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel('OBPOS_ReceiptNew'));
  }
});
enyo.kind({
  name: 'OB.UI.RenderTaxLineEmpty',
  classes: 'obUiRenderTaxLineEmpty',
  initComponents: function() {
    this.inherited(arguments);
  }
});

enyo.kind({
  name: 'OB.UI.ShowServicesButton',
  kind: 'OB.UI.Button',
  i18nLabel: 'OBPOS_Services',
  classes: 'obUiShowServicesButton',
  published: {
    disabled: false
  },
  handlers: {
    onRightToolbarDisabled: 'toggleVisibility',
    ontap: 'showServices'
  },
  addServicesFilter: function(orderline) {
    var product = orderline.get('product');
    OB.UI.SearchProductCharacteristic.prototype.filtersCustomClear();
    OB.UI.SearchProductCharacteristic.prototype.filtersCustomAdd(
      new OB.UI.SearchServicesFilter({
        text: product.get('_identifier'),
        productId: product.id,
        productList: null,
        orderline: orderline,
        orderlineList: null,
        extraParams: this.extraParams
      })
    );
    this.bubble('onTabChange', {
      tabPanel: 'searchCharacteristic'
    });
    this.bubble('onSelectFilter', {
      params: {
        skipProductCharacteristic: true,
        crossStore: product.get('crossStore') === true,
        crossStoreProductOrg:
          product.get('crossStore') === true
            ? product.get('organization')
            : undefined,
        crossStoreProductWhs:
          product.get('crossStore') === true
            ? orderline.get('warehouse')
            : undefined
      }
    });
  },
  showServices: function(inSender, inEvent) {
    var product = this.owner.model.get('product'),
      orderline = this.owner.model;
    if (product) {
      this.addServicesFilter(orderline);
      orderline.set('obposServiceProposed', true);
      OB.MobileApp.model.receipt.save();
    }
    return true;
  },
  toggleVisibility: function(inSender, inEvent) {
    this.isVisible = !inEvent.status;
    if (this.isVisible) {
      this.show();
    } else {
      this.hide();
    }
  },
  initComponents: function() {
    this.inherited(arguments);
    if (this.owner.model.get('obposServiceProposed')) {
      this.addRemoveClass('obUiShowServicesButton_unreviewed', false);
      this.addRemoveClass('obUiShowServicesButton_reviewed', true);
    } else {
      this.addRemoveClass('obUiShowServicesButton_unreviewed', true);
      this.addRemoveClass('obUiShowServicesButton_reviewed', false);
    }
    if (OB.MobileApp.model.get('serviceSearchMode')) {
      this.hide();
    }
  }
});

enyo.kind({
  kind: 'OB.UI.listItemButton',
  name: 'OB.UI.RenderTaxLine',
  classes: 'obUiRenderTaxLine',
  tap: function() {
    return this;
  },
  components: [
    {
      name: 'tax',
      classes: 'obUiRenderTaxLine-tax'
    },
    {
      kind: 'OB.UI.FitText',
      classes: 'obUiRenderTaxLine-container1 fitText',
      components: [
        {
          tag: 'span',
          name: 'base',
          classes: 'obUiRenderTaxLine-container1-base'
        }
      ]
    },
    {
      kind: 'OB.UI.FitText',
      classes: 'obUiRenderTaxLine-container2 fitText',
      components: [
        {
          tag: 'span',
          name: 'totaltax',
          classes: 'obUiRenderTaxLine-container2-totaltax'
        }
      ]
    },
    {
      classes: 'obUiRenderTaxLine-container3 u-clearBoth'
    }
  ],
  selected: function() {
    return this;
  },
  initComponents: function() {
    this.inherited(arguments);
    this.$.tax.setContent(this.model.get('name'));
    this.$.base.setContent(OB.I18N.formatCurrency(this.model.get('net')));
    this.$.totaltax.setContent(
      OB.I18N.formatCurrency(this.model.get('amount'))
    );
  }
});

enyo.kind({
  kind: 'OB.UI.SelectButton',
  name: 'OB.UI.RenderPaymentLine',
  classes: 'obUiRenderPaymentLine',
  handlers: {
    onRenderPaymentLine: 'renderPaymentLine'
  },
  tap: function() {
    return this;
  },
  components: [
    {
      name: 'name',
      classes: 'obUiRenderPaymentLine-name',
      attributes: {}
    },
    {
      name: 'date',
      classes: 'obUiRenderPaymentLine-date',
      attributes: {}
    },
    {
      name: 'foreignAmount',
      classes: 'obUiRenderPaymentLine-foreignAmount',
      attributes: {}
    },
    {
      name: 'amount',
      classes: 'obUiRenderPaymentLine-amount',
      attributes: {}
    },
    {
      classes: 'obUiRenderPaymentLine-element5 u-clearBoth'
    }
  ],
  selected: function() {
    return this;
  },
  renderPaymentLine: function(inSender, inEvent) {
    var paymentDate;
    if (this.model.get('reversedPaymentId')) {
      this.$.name.setContent(
        (OB.MobileApp.model.getPaymentName(this.model.get('kind')) ||
          this.model.get('name')) + OB.I18N.getLabel('OBPOS_ReversedPayment')
      );
      this.$.amount.setContent(this.model.printAmount());
    } else if (this.model.get('isReversed')) {
      this.$.name.setContent(
        '*' +
          (OB.MobileApp.model.getPaymentName(this.model.get('kind')) ||
            this.model.get('name'))
      );
      this.$.amount.setContent(this.model.printAmount());
    } else {
      if (!OB.UTIL.isNullOrUndefined(this.owner.owner)) {
        var receipt = this.owner.owner.owner.owner.order;
        this.$.name.setContent(
          OB.MobileApp.model.getPaymentName(this.model.get('kind')) ||
            this.model.get('name')
        );
        this.$.amount.setContent(this.model.printAmountWithSignum(receipt));
      }
    }
    if (
      this &&
      this.model &&
      this.model.get('paymentData') &&
      this.model.get('paymentData').name &&
      this.model.get('paymentData').name.length > 0
    ) {
      this.$.name.setContent(
        (OB.MobileApp.model.getPaymentName(this.model.get('kind')) ||
          this.model.get('name')) +
          ' (' +
          this.model.get('paymentData').name +
          ')'
      );
    }
    if (!this.model.get('paymentAmount') && this.model.get('isPrePayment')) {
      this.$.name.setContent(OB.I18N.getLabel('OBPOS_Cancelled'));
    }
    if (OB.UTIL.isNullOrUndefined(this.model.get('paymentDate'))) {
      paymentDate = new Date();
    } else {
      paymentDate = this.model.get('paymentDate');
      if (typeof this.model.get('paymentDate') === 'string') {
        paymentDate = new Date(paymentDate);
      }
    }
    this.$.date.setContent(OB.I18N.formatDate(paymentDate));
    if (this.model.get('rate') && this.model.get('rate') !== '1') {
      this.$.foreignAmount.setContent(this.model.printForeignAmount());
    } else {
      this.$.foreignAmount.setContent('');
    }
  },
  initComponents: function() {
    this.inherited(arguments);
    this.renderPaymentLine();
  }
});
enyo.kind({
  name: 'OB.UI.RenderPaymentLineEmpty',
  classes: 'obUiRenderPaymentLineEmpty',
  initComponents: function() {
    this.inherited(arguments);
  }
});
