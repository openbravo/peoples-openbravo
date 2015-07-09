/*
 ************************************************************************************
 * Copyright (C) 2013-2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, moment, enyo, OB_UI_SearchServicesFilter */

enyo.kind({
  kind: 'OB.UI.listItemButton',
  name: 'OB.UI.RenderOrderLine',
  classes: 'btnselect-orderline',
  handlers: {
    onChangeEditMode: 'changeEditMode',
    onCheckBoxBehaviorForTicketLine: 'checkBoxForTicketLines'
  },
  events: {
    onLineChecked: ''
  },
  components: [{
    name: 'checkBoxColumn',
    kind: 'OB.UI.CheckboxButton',
    tag: 'div',
    tap: function () {

    },
    style: 'float: left; width: 10%;'
  }, {
    name: 'serviceIcon',
    kind: 'Image',
    src: 'img/iconService_ticketline.png',
    sizing: "cover",
    width: 36,
    height: 26,
    style: 'float: left;'
  }, {
    name: 'product',
    style: 'float: left; '
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
      this.$.product.addStyles('width: 36%');
    } else {
      this.$.serviceIcon.hide();
      this.$.product.addStyles('width: 40%');
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
          content: OB.UTIL.getCharacteristicValues(this.model.get('product').get('characteristicDescription')),
          attributes: {
            style: 'float: left; width: 60%; color:grey'
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
        this.createComponent({
          style: 'display: block;',
          components: [{
            content: '-- ' + d.identifier,
            attributes: {
              style: 'float: left; width: 80%;'
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
          style: 'display: block; float: left; width: 80%;'
        });
      }
      enyo.forEach(this.model.get('relatedLines'), function (line) {
        this.$.relatedLinesContainer.createComponent({
          components: [{
            content: 'for ' + line.productName,
            attributes: {
              style: 'font-size: 14px; font-style: italic'
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
    }, function (args) {
      //All should be done in module side
    });
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
      if (this.model.get('product').get('productType') === 'S') {
        this.$.product.hasNode().style.width = '34%';
      } else {
        this.$.product.hasNode().style.width = '38%';
      }
      this.$.checkBoxColumn.show();
      this.changeEditMode(this, inEvent.status);
    } else {
      this.$.gross.hasNode().style.width = '20%';
      this.$.quantity.hasNode().style.width = '20%';
      this.$.price.hasNode().style.width = '20%';
      if (this.model.get('product').get('productType') === 'S') {
        this.$.product.hasNode().style.width = '36%';
      } else {
        this.$.product.hasNode().style.width = '40%';
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
      OB.UI.SearchProductCharacteristic.prototype.filtersCustomAdd(new OB_UI_SearchServicesFilter({
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
    if (OB.MobileApp.model.get('serviceSearchMode') === 'mandatory') {
      this.hide();
    }
  }
});

enyo.kind({
  kind: 'OB.UI.listItemButton',
  name: 'OB.UI.RenderTaxLine',
  classes: 'btnselect-orderline',
  tap: function () {

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

  },
  initComponents: function () {
    var paymentDate;
    this.inherited(arguments);
    this.$.name.setContent(OB.MobileApp.model.getPaymentName(this.model.get('kind')) || this.model.get('name'));
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