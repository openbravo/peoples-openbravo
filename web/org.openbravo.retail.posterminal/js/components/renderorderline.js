/*
 ************************************************************************************
 * Copyright (C) 2013-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, moment, enyo */

enyo.kind({
  kind: 'OB.UI.listItemButton',
  name: 'OB.UI.RenderOrderLine',
  classes: 'btnselect-orderline',
  handlers: {
    onChangeEditMode: 'changeEditMode',
    onCheckBoxBehaviorForTicketLine: 'checkBoxForTicketLines',
    onSetMultiSelected: 'setMultiSelected',
    onkeyup: 'keyupHandler'
  },
  tap: function () {
    if (OB.MobileApp.model.get('serviceSearchMode')) {
      return;
    }
    this.model.trigger('selected', this.model);
    this.model.trigger('click', this.model);
  },
  events: {
    onLineChecked: ''
  },
  components: [{
    name: 'checkBoxColumn',
    kind: 'OB.UI.CheckboxButton',
    tag: 'div',
    tap: function () {
      return this;
    },
    style: 'float: left; width: 10%;'
  }, {
    name: 'nameContainner',
    tag: 'div',
    style: 'float: left;width: 40%; padding: 0px;',
    components: [{
      name: 'serviceIcon',
      kind: 'Image',
      src: 'img/iconService_ticketline.png',
      style: 'float: left; padding-right: 5px; '
    }, {
      name: 'product'
    }]
  }, {
    name: 'quantity',
    attributes: {
      style: 'float: left; width: 20%; text-align: right;'
    }
  }, {
    name: 'price',
    attributes: {
      style: 'float: left; width: 20%; text-align: right;'
    }
  }, {
    name: 'gross',
    attributes: {
      style: 'float: left; width: 20%; text-align: right;'
    }
  }, {
    style: 'clear: both;'
  }],
  initComponents: function () {
    var me = this;
    this.inherited(arguments);
    if (this.model.get('product').get('productType') === 'S') {
      this.$.serviceIcon.show();
      this.$.product.addStyles('margin-left: 24px;');
    } else {
      this.$.serviceIcon.hide();
      this.$.product.addStyles('float: left;');
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
    if (this.model.get('product').get('characteristicDescription')) {
      this.createComponent({
        style: 'display: block; ',
        components: [{
          name: 'characteristicsDescription',
          content: OB.UTIL.getCharacteristicValues(this.model.get('product').get('characteristicDescription')),
          attributes: {
            style: 'width: 60.1%; color:grey; padding-left: 0%; clear: both; '
          }
        }, {
          style: 'clear: both;'
        }]
      });
    }
    if (this.model.get('obposSerialNumber')) {
      this.createComponent({
        style: 'display: block;',
        components: [{
          content: OB.I18N.getLabel('OBPOS_SerialNumber', [this.model.get('obposSerialNumber')]),
          attributes: {
            style: 'float: left; width: 80%;'
          }
        }, {
          style: 'clear: both;'
        }]
      });
    }

    if (this.model.get('remainingQuantity')) {
      this.createComponent({
        style: 'display: block;',
        components: [{
          content: '-- ' + OB.I18N.getLabel('OBPOS_DeliveredQuantity') + ': ' + this.model.get('remainingQuantity'),
          attributes: {
            style: 'float: left; width: 100%; clear: left;'
          }
        }, {
          style: 'clear: both;'
        }]
      });
    }

    if (this.model.get('promotions')) {
      enyo.forEach(this.model.get('promotions'), function (d) {
        if (d.hidden) {
          // continue
          return;
        }
        var identifierName = d.identifier || d.name;
        var nochunks = d.chunks;
        this.createComponent({
          style: 'display: block;',
          components: [{
            content: (OB.UTIL.isNullOrUndefined(nochunks) || nochunks === 1) ? '-- ' + identifierName : '-- ' + '(' + nochunks + 'x) ' + identifierName,
            attributes: {
              style: 'float: left; width: 80%; clear: left;'
            }
          }, {
            content: OB.I18N.formatCurrency(-d.amt),
            attributes: {
              style: 'float: right; width: 20%; text-align: right;'
            }
          }, {
            style: 'clear: both;'
          }]
        });
      }, this);

    }
    if (this.model.get('relatedLines')) {
      if (!this.$.relatedLinesContainer) {
        this.createComponent({
          name: 'relatedLinesContainer',
          style: 'clear:both; float: left; width: 80%;'
        });
      }
      enyo.forEach(this.model.get('relatedLines'), function (line) {
        this.$.relatedLinesContainer.createComponent({
          components: [{
            content: line.otherTicket ? OB.I18N.getLabel('OBPOS_lblRelatedLinesOtherTicket', [line.productName, line.orderDocumentNo]) : OB.I18N.getLabel('OBPOS_lblRelatedLines', [line.productName]),
            attributes: {
              style: 'font-size: 14px; font-style: italic; text-align: left; padding-left: 25px'
            }
          }]
        });
      }, this);
    }
    if (this.model.get('hasRelatedServices')) {
      me.createComponent({
        kind: 'OB.UI.ShowServicesButton',
        name: 'showServicesButton'
      });
    } else if (!this.model.has('hasRelatedServices')) {
      this.model.on('showServicesButton', function () {
        me.model.off('showServicesButton');
        me.createComponent({
          kind: 'OB.UI.ShowServicesButton',
          name: 'showServicesButton'
        }).render();
      });
    }
    OB.UTIL.HookManager.executeHooks('OBPOS_RenderOrderLine', {
      orderline: this
    });
  },
  keyupHandler: function (inSender, inEvent) {
    var keyCode = inEvent.keyCode;
    if (keyCode === 13 || keyCode === 32) { //Handle ENTER and SPACE keys in buttons
      this.executeTapAction();
      return true;
    }
    OB.MobileApp.view.keypressHandler(inSender, inEvent);
  },
  setMultiSelected: function (inSender, inEvent) {
    if (inEvent.models && inEvent.models.length > 0 && inEvent.models[0] instanceof OB.Model.OrderLine && this.$.showServicesButton) {
      if (inEvent.models.length > 1) {
        this.$.showServicesButton.hide();
      } else {
        this.$.showServicesButton.show();
      }
    }
  },
  changeEditMode: function (inSender, inEvent) {
    this.addRemoveClass('btnselect-orderline-edit', inEvent.edit);
    this.bubble('onShowColumn', {
      colNum: 1
    });
  },
  checkBoxForTicketLines: function (inSender, inEvent) {
    if (inEvent.status) {
      this.$.gross.hasNode().style.width = '18%';
      this.$.quantity.hasNode().style.width = '16%';
      this.$.price.hasNode().style.width = '18%';

      this.$.nameContainner.hasNode().style.width = '38%';
      if (this.$.characteristicsDescription) {
        this.$.characteristicsDescription.addStyles('padding-left: 10%; clear: both; width: 50.1%; color:grey');
      }
      if (this.$.relatedLinesContainer) {
        this.$.relatedLinesContainer.addStyles('padding-left: 10%; clear: both; float: left; width: 80%;');
      }
      this.$.checkBoxColumn.show();
      this.changeEditMode(this, inEvent.status);
    } else {
      this.$.gross.hasNode().style.width = '20%';
      this.$.quantity.hasNode().style.width = '20%';
      this.$.price.hasNode().style.width = '20%';
      this.$.nameContainner.hasNode().style.width = '40%';
      if (this.$.characteristicsDescription) {
        this.$.characteristicsDescription.addStyles('padding-left: 0%; clear: both; width: 60.1%; color:grey');
      }
      if (this.$.relatedLinesContainer) {
        this.$.relatedLinesContainer.addStyles('padding-left: 0%; clear: both; float: left; width: 80%;');
      }
      this.$.checkBoxColumn.hide();
      this.changeEditMode(this, false);
    }
  },
  setIdentifierContent: function () {
    return this.model.get('product').get('_identifier');
  }

});

enyo.kind({
  name: 'OB.UI.RenderOrderLineEmpty',
  style: 'border-bottom: 1px solid #cccccc; padding: 20px; text-align: center; font-weight: bold; font-size: 30px; color: #cccccc',
  initComponents: function () {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel('OBPOS_ReceiptNew'));
  }
});
enyo.kind({
  name: 'OB.UI.RenderTaxLineEmpty',
  style: 'border-bottom: 1px solid #cccccc; padding: 20px; text-align: center; font-weight: bold; font-size: 30px; color: #cccccc',
  initComponents: function () {
    this.inherited(arguments);
  }
});

enyo.kind({
  name: 'OB.UI.ShowServicesButton',
  style: 'float: right; display: block;',
  published: {
    disabled: false
  },
  handlers: {
    onRightToolbarDisabled: 'toggleVisibility'
  },
  tap: function (inSender, inEvent) {
    var product = this.owner.model.get('product');
    if (product) {
      OB.UI.SearchProductCharacteristic.prototype.filtersCustomClear();
      OB.UI.SearchProductCharacteristic.prototype.filtersCustomAdd(new OB.UI.SearchServicesFilter({
        text: product.get("_identifier"),
        productId: product.id,
        productList: null,
        orderline: this.owner.model,
        orderlineList: null
      }));
      var me = this;
      setTimeout(function () {
        me.bubble('onTabChange', {
          tabPanel: 'searchCharacteristic'
        });
        me.bubble('onSelectFilter', {});
        me.owner.model.set("obposServiceProposed", true);
        OB.MobileApp.model.receipt.save();
      }, 1);
    }
  },
  toggleVisibility: function (inSender, inEvent) {
    this.isVisible = !inEvent.status;
    if (this.isVisible) {
      this.show();
    } else {
      this.hide();
    }
  },
  initComponents: function () {
    this.inherited(arguments);
    if (this.owner.model.get('obposServiceProposed')) {
      this.addRemoveClass('iconServices_unreviewed', false);
      this.addRemoveClass('iconServices_reviewed', true);
    } else {
      this.addRemoveClass('iconServices_unreviewed', true);
      this.addRemoveClass('iconServices_reviewed', false);
    }
    if (OB.MobileApp.model.get('serviceSearchMode')) {
      this.hide();
    }
  }
});

enyo.kind({
  kind: 'OB.UI.listItemButton',
  name: 'OB.UI.RenderTaxLine',
  classes: 'btnselect-orderline',
  tap: function () {
    return this;
  },
  components: [{
    name: 'tax',
    attributes: {
      style: 'float: left; width: 60%;'
    }
  }, {
    name: 'base',
    attributes: {
      style: 'float: left; width: 20%; text-align: right;'
    }
  }, {
    name: 'totaltax',
    attributes: {
      style: 'float: left; width: 20%; text-align: right;'
    }
  }, {
    style: 'clear: both;'
  }],
  selected: function () {
    return this;
  },
  initComponents: function () {
    this.inherited(arguments);
    this.$.tax.setContent(this.model.get('name'));
    this.$.base.setContent(OB.I18N.formatCurrency(this.model.get('net')));
    this.$.totaltax.setContent(OB.I18N.formatCurrency(this.model.get('amount')));
  }
});


enyo.kind({
  kind: 'OB.UI.SelectButton',
  name: 'OB.UI.RenderPaymentLine',
  classes: 'btnselect-orderline',
  style: 'border-bottom: 0px',
  tap: function () {
    return this;
  },
  components: [{
    name: 'name',
    attributes: {
      style: 'float: left; width: 40%; padding: 5px 0px 0px 0px;'
    }
  }, {
    name: 'date',
    attributes: {
      style: 'float: left; width: 20%; padding: 5px 0px 0px 0px; text-align: right;'
    }
  }, {
    name: 'foreignAmount',
    attributes: {
      style: 'float: left; width: 20%; padding: 5px 0px 0px 0px; text-align: right;'
    }
  }, {
    name: 'amount',
    attributes: {
      style: 'float: left; width: 20%; padding: 5px 0px 0px 0px; text-align: right;'
    }
  }, {
    style: 'clear: both;'
  }],
  selected: function () {
    return this;
  },
  initComponents: function () {
    var paymentDate;
    this.inherited(arguments);
    this.$.name.setContent(OB.MobileApp.model.getPaymentName(this.model.get('kind')) || this.model.get('name'));
    if (!this.model.get('paymentAmount') && this.model.get('isPrePayment')) {
      this.$.name.setContent(OB.I18N.getLabel('OBPOS_Cancelled'));
    }
    if (this && this.model && this.model.get('paymentData') && this.model.get('paymentData').name && this.model.get('paymentData').name.length > 0) {
      this.$.name.setContent((OB.MobileApp.model.getPaymentName(this.model.get('kind')) || this.model.get('name')) + ' (' + this.model.get('paymentData').name + ')');
    }
    if (OB.UTIL.isNullOrUndefined(this.model.get('paymentDate'))) {
      paymentDate = new Date();
    } else {
      // Convert to UTC to properly manage browser timezone
      paymentDate = this.model.get('paymentDate');
      if (typeof (this.model.get('paymentDate')) === 'string') {
        paymentDate = new Date(paymentDate);
      }
      paymentDate = new Date(paymentDate.getTime() + (60000 * paymentDate.getTimezoneOffset()));
    }
    this.$.date.setContent(OB.I18N.formatDate(paymentDate));
    if (this.model.get('rate') && this.model.get('rate') !== '1') {
      this.$.foreignAmount.setContent(this.model.printForeignAmount());
    } else {
      this.$.foreignAmount.setContent('');
    }
    this.$.amount.setContent(this.model.printAmount());
  }
});
enyo.kind({
  name: 'OB.UI.RenderPaymentLineEmpty',
  style: 'border-bottom: 1px solid #cccccc; padding: 20px; text-align: center; font-weight: bold; font-size: 30px; color: #cccccc',
  initComponents: function () {
    this.inherited(arguments);
  }
});