/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, _ */
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.EditLine',
  published: {
    receipt: null
  },
  events: {
    onDeleteLine: '',
    onEditLine: ''
  },
  handlers: {
    onCheckBoxBehaviorForTicketLine: 'checkBoxBehavior'
  },
  checkBoxBehavior: function (inSender, inEvent) {
    if (inEvent.status) {
      this.line = null;
      //WARN! When off is done the components which are listening to this event
      //are removed. Because of it, the callback for the selected event are saved
      //and then recovered.
      this.selectedCallbacks = this.receipt.get('lines')._callbacks.selected;
      this.receipt.get('lines').off('selected');
      this.render();
    } else {
      //WARN! recover the callbacks for the selected events
      this.receipt.get('lines')._callbacks.selected = this.selectedCallbacks;

      if (this.receipt.get('lines').length > 0) {
        var line = this.receipt.get('lines').at(0);
        line.trigger('selected', line);
      }
    }
  },
  executeOnShow: function (args) {
    if (args && args.discounts) {
      this.$.defaultEdit.hide();
      this.$.discountsEdit.show();
      return;
    }
    this.$.defaultEdit.show();
    this.$.discountsEdit.hide();
  },
  components: [{
    kind: 'OB.OBPOSPointOfSale.UI.Discounts',
    showing: false,
    name: 'discountsEdit'
  }, {
    name: 'defaultEdit',
    style: 'background-color: #ffffff; color: black; height: 200px; margin: 5px; padding: 5px',
    components: [{
      name: 'msgedit',
      classes: 'row-fluid',
      showing: false,
      components: [{
        classes: 'span12',
        components: [{
          kind: 'OB.UI.SmallButton',
          i18nContent: 'OBPOS_ButtonDelete',
          classes: 'btnlink-orange',
          tap: function () {
            this.owner.doDeleteLine({
              line: this.owner.line
            });
          },
          init: function (model) {
            this.model = model;
            this.model.get('order').on('change:isPaid change:isLayaway', function (newValue) {
              if (newValue) {
                if (newValue.get('isPaid') === true || newValue.get('isLayaway') === true) {
                  this.setShowing(false);
                  return;
                }
              }
              this.setShowing(true);
            }, this);
          }
        }, {
          kind: 'OB.UI.SmallButton',
          i18nContent: 'OBPOS_LblDescription',
          classes: 'btnlink-orange',
          tap: function () {
            this.owner.doEditLine({
              line: this.owner.line
            });
          },
          init: function (model) {
            this.model = model;
            this.model.get('order').on('change:isPaid change:isLayaway', function (newValue) {
              if (newValue) {
                if (newValue.get('isPaid') === true || newValue.get('isLayaway') === true) {
                  this.setShowing(false);
                  return;
                }
              }
              this.setShowing(true);
            }, this);
          }
        }, {
          kind: 'OB.UI.SmallButton',
          name: 'removeDiscountButton',
          i18nContent: 'OBPOS_LblRemoveDiscount',
          showing: false,
          classes: 'btnlink-orange',
          tap: function () {
            if (this.owner && this.owner.line && this.owner.line.get('promotions')) {
              this.owner.line.unset('promotions');
              this.model.get('order').calculateGross();
              this.hide();
            }
          },
          init: function (model) {
            this.model = model;
          }
        }, {
          kind: 'OB.OBPOSPointOfSale.UI.EditLine.OpenStockButton',
          name: 'checkStockButton',
          showing: false
        }]
      }, {
        classes: 'span12',
        components: [{
          classes: 'span7',
          kind: 'Scroller',
          maxHeight: '130px',
          thumb: true,
          horizontal: 'hidden',
          style: 'padding: 5px 0px 5px 10px; line-height: 135%;',
          components: [{
            classes: 'row-fluid',
            style: 'clear: both;',
            components: [{
              classes: 'span4',
              name: 'editlinenameLbl'
            }, {
              classes: 'span8',
              components: [{
                tag: 'span',
                name: 'editlinename'
              }]
            }]
          }, {
            classes: 'row-fluid',
            style: 'clear: both;',
            components: [{
              classes: 'span4',
              name: 'editlinedetailLbl'
            }, {
              classes: 'span8',
              components: [{
                tag: 'span',
                name: 'editlinedetail'
              }]
            }]
          }, {
            classes: 'row-fluid',
            style: 'clear: both;',
            components: [{
              classes: 'span4',
              name: 'editlineqtyLbl'
            }, {
              classes: 'span8',
              components: [{
                tag: 'span',
                name: 'editlineqty'
              }]
            }]
          }, {
            classes: 'row-fluid',
            style: 'clear: both;',
            components: [{
              classes: 'span4',
              name: 'editlinepriceLbl'
            }, {
              classes: 'span8',
              components: [{
                tag: 'span',
                name: 'editlineprice'
              }]
            }]
          }, {
            classes: 'row-fluid',
            style: 'clear: both;',
            components: [{
              classes: 'span4',
              name: 'editlinediscountLbl'
            }, {
              classes: 'span8',
              components: [{
                tag: 'span',
                name: 'editlinediscount'
              }]
            }]
          }, {
            classes: 'row-fluid',
            style: 'clear: both;',
            components: [{
              classes: 'span4',
              name: 'editlinegrossLbl'
            }, {
              classes: 'span8',
              components: [{
                tag: 'span',
                name: 'editlinegross'
              }]
            }]
          }]
        }, {
          classes: 'span4',
          sytle: 'text-align: right',
          components: [{
            style: 'padding: 2px 10px 10px 10px;',
            components: [{
              name: 'editlineimage',
              kind: 'OB.UI.Thumbnail',
              classes: 'image-wrap image-editline',
              width: '128px',
              height: '128px'
            }]
          }]
        }]
      }, {
        name: 'msgaction',
        style: 'padding: 10px;',
        components: [{
          name: 'txtaction',
          style: 'float:left;'
        }]
      }]
    }]
  }],
  selectedListener: function (line) {
    if (this.line) {
      this.line.off('change', this.render);
    }
    this.line = line;
    if (this.line) {
      this.line.on('change', this.render, this);
    }
    if (this.line && this.line.get('product').get('showstock') && !this.line.get('product').get('ispack') && OB.POS.modelterminal.get('connectedToERP')) {
      this.$.checkStockButton.show();
    } else {
      this.$.checkStockButton.hide();
    }
    if (this.line && this.line.get('promotions')) {
      if (this.line.get('promotions').length > 0) {
        var filtered;
        filtered = _.filter(this.line.get('promotions'), function (prom) {
          //discrectionary discounts ids
          return prom.discountType === '20E4EC27397344309A2185097392D964' || prom.discountType === 'D1D193305A6443B09B299259493B272A' || prom.discountType === '8338556C0FBF45249512DB343FEFD280' || prom.discountType === '7B49D8CC4E084A75B7CB4D85A6A3A578';
        }, this);
        if (filtered.length === this.line.get('promotions').length) {
          //lines with just discrectionary discounts can be removed.
          this.$.removeDiscountButton.show();
        }
      }
    } else {
      this.$.removeDiscountButton.hide();
    }
    this.render();
  },
  receiptChanged: function () {
    this.inherited(arguments);

    this.line = null;

    this.receipt.get('lines').on('selected', this.selectedListener, this);
  },

  render: function () {
    this.inherited(arguments);

    if (this.line) {
      this.$.msgaction.hide();
      this.$.msgedit.show();
      this.$.editlineimage.setImg(this.line.get('product').get('img'));
      this.$.editlinename.setContent(this.line.get('product').get('_identifier'));
      if (this.line.get('product').get('characteristicDescription') === null) {
        this.$.editlinedetail.hide();
        this.$.editlinedetailLbl.hide();
      } else {
        this.$.editlinedetail.show();
        this.$.editlinedetailLbl.show();
        this.$.editlinedetail.setContent(OB.UTIL.getCharacteristicValues(this.line.get('product').get('characteristicDescription')));
      }
      this.$.editlineqty.setContent(this.line.printQty());
      this.$.editlinediscount.setContent(this.line.printDiscount());
      this.$.editlineprice.setContent(this.line.printPrice());
      if (this.line.get('priceIncludesTax')) {
        this.$.editlinegross.setContent(OB.I18N.formatCurrency(this.line.getGross() - this.line.discountInTotal()));
      } else {
        this.$.editlinegross.setContent(OB.I18N.formatCurrency(this.line.getNet() - this.line.discountInTotal()));
      }
    } else {
      this.$.txtaction.setContent(OB.I18N.getLabel('OBPOS_NoLineSelected'));
      this.$.msgedit.hide();
      this.$.msgaction.show();
      this.$.editlineimage.setImg(null);
      this.$.editlinename.setContent('');
      this.$.editlinedetail.setContent('');
      this.$.editlineqty.setContent('');
      this.$.editlinediscount.setContent('');
      this.$.editlineprice.setContent('');
      this.$.editlinegross.setContent('');
    }
    this.$.editlinenameLbl.setContent(OB.I18N.getLabel('OBPOS_LineDescription'));
    this.$.editlinedetailLbl.setContent(OB.I18N.getLabel('OBPOS_LineDetail'));
    this.$.editlineqtyLbl.setContent(OB.I18N.getLabel('OBPOS_LineQuantity'));
    this.$.editlinepriceLbl.setContent(OB.I18N.getLabel('OBPOS_LinePrice'));
    this.$.editlinediscountLbl.setContent(OB.I18N.getLabel('OBPOS_LineDiscount'));
    this.$.editlinegrossLbl.setContent(OB.I18N.getLabel('OBPOS_LineTotal'));
  }
});

enyo.kind({
  kind: 'OB.UI.SmallButton',
  name: 'OB.OBPOSPointOfSale.UI.EditLine.OpenStockButton',
  events: {
    onShowLeftSubWindow: ''
  },
  content: '',
  classes: 'btnlink-orange',
  tap: function () {
    var product = this.owner.line.get('product');
    var params = {};
    //show always or just when the product has been set to show stock screen?
    if (product.get('showstock') && !product.get('ispack') && OB.POS.modelterminal.get('connectedToERP')) {
      params.leftSubWindow = OB.OBPOSPointOfSale.UICustomization.stockLeftSubWindow;
      params.product = product;
      this.doShowLeftSubWindow(params);
    }
  },
  initComponents: function () {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel('OBPOS_checkStock'));
  }
});