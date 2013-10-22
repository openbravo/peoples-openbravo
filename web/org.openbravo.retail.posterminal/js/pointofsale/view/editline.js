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
  name: 'OB.OBPOSPointOfSale.UI.LineProperty',
  components: [{
    classes: 'row-fluid',
    style: 'clear: both;',
    components: [{
      classes: 'span4',
      name: 'propertyLabel'
    }, {
      classes: 'span8',
      components: [{
        tag: 'span',
        name: 'propertyValue'
      }]
    }]
  }],
  render: function (model) {
    if (model) {
      this.$.propertyValue.setContent(model.get(this.propertyToPrint));
    } else {
      this.$.propertyValue.setContent('');
    }
  },
  initComponents: function () {
    this.inherited(arguments);
    this.$.propertyLabel.setContent(OB.I18N.getLabel(this.I18NLabel));
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.LinePropertyDiv',
  components: [{
    classes: 'row-fluid',
    style: 'clear: both;',
    components: [{
      classes: 'span4',
      name: 'propertyLabel'
    }, {
      classes: 'span8',
      components: [{
        tag: 'div',
        name: 'propertyValue'
      }]
    }]
  }],
  render: function (model) {
    if (model) {
      this.$.propertyValue.setContent(model.get(this.propertyToPrint));
    } else {
      this.$.propertyValue.setContent('');
    }
  },
  initComponents: function () {
    this.inherited(arguments);
    this.$.propertyLabel.setContent(OB.I18N.getLabel(this.I18NLabel));
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.EditLine',
  propertiesToShow: [{
    kind: 'OB.OBPOSPointOfSale.UI.LineProperty',
    position: 10,
    name: 'descLine',
    I18NLabel: 'OBPOS_LineDescription',
    render: function (line) {
      if (line) {
        this.$.propertyValue.setContent(line.get('product').get('_identifier'));
      } else {
        this.$.propertyValue.setContent('');
      }
    }
  }, {
    kind: 'OB.OBPOSPointOfSale.UI.LineProperty',
    position: 20,
    name: 'qtyLine',
    I18NLabel: 'OBPOS_LineQuantity',
    render: function (line) {
      if (line) {
        this.$.propertyValue.setContent(line.printQty());
      } else {
        this.$.propertyValue.setContent('');
      }
    }
  }, {
    kind: 'OB.OBPOSPointOfSale.UI.LineProperty',
    position: 30,
    name: 'priceLine',
    I18NLabel: 'OBPOS_LinePrice',
    render: function (line) {
      if (line) {
        this.$.propertyValue.setContent(line.printPrice());
      } else {
        this.$.propertyValue.setContent('');
      }
    }
  }, {
    kind: 'OB.OBPOSPointOfSale.UI.LineProperty',
    position: 40,
    name: 'discountedAmountLine',
    I18NLabel: 'OBPOS_LineDiscount',
    render: function (line) {
      if (line) {
        this.$.propertyValue.setContent(line.printDiscount());
      } else {
        this.$.propertyValue.setContent('');
      }
    }
  }, {
    kind: 'OB.OBPOSPointOfSale.UI.LineProperty',
    position: 50,
    name: 'grossLine',
    I18NLabel: 'OBPOS_LineTotal',
    render: function (line) {
      if (line) {
        if (line.get('priceIncludesTax')) {
          this.$.propertyValue.setContent(line.printGross());
        } else {
          this.$.propertyValue.setContent(line.printNet());
        }
      } else {
        this.$.propertyValue.setContent('');
      }
    }
  }],
  published: {
    receipt: null
  },
  events: {
    onDeleteLine: '',
    onEditLine: '',
    onReturnLine: ''
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
          name: 'returnLine',
          i18nContent: 'OBPOS_LblReturnLine',
          permission: 'OBPOS_ReturnLine',
          classes: 'btnlink-orange',
          showing: false,
          tap: function () {
            this.owner.doReturnLine({
              line: this.owner.line
            });
          },
          init: function (model) {
            this.model = model;
            if (OB.POS.modelterminal.hasPermission(this.permission)) {
              this.setShowing(true);
            }
            this.model.get('order').on('change:isPaid change:isLayaway', function (newValue) {
              if (newValue) {
                if (newValue.get('isPaid') === true || newValue.get('isLayaway') === true) {
                  this.setShowing(false);
                  return;
                }
              }
              if (OB.POS.modelterminal.hasPermission(this.permission)) {
                this.setShowing(true);
              }
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
        kind: 'OB.UI.List',
        name: 'returnreason',
        classes: 'combo',
        style: 'width: 100%; margin-bottom: 0px; height: 30px ',
        events: {
          onSetReason: ''
        },
        handlers: {
          onchange: 'changeReason'
        },
        changeReason: function (inSender, inEvent) {
          this.owner.line.set('returnReason', this.children[this.getSelected()].getValue());
        },
        renderHeader: enyo.kind({
          kind: 'enyo.Option',
          initComponents: function () {
            this.inherited(arguments);
            this.setValue('__all__');
            this.setContent(OB.I18N.getLabel('OBPOS_ReturnReasons'));
          }
        }),
        renderLine: enyo.kind({
          kind: 'enyo.Option',
          initComponents: function () {
            this.inherited(arguments);
            this.setValue(this.model.get('id'));
            this.setContent(this.model.get('_identifier'));
          }
        }),
        renderEmpty: 'enyo.Control'
      }, {
        classes: 'span12',
        components: [{
          classes: 'span7',
          kind: 'Scroller',
          name: 'linePropertiesContainer',
          maxHeight: '134px',
          thumb: true,
          horizontal: 'hidden',
          style: 'padding: 8px 0px 4px 25px; line-height: 120%;'
        }, {
          classes: 'span4',
          sytle: 'text-align: right',
          components: [{
            style: 'padding: 2px 10px 10px 10px;',
            components: [{
              tag: 'div',
              classes: 'image-wrap image-editline',
              contentType: 'image/png',
              style: 'width: 128px; height: 128px',
              components: [{
                tag: 'img',
                name: 'icon',
                style: 'margin: auto; height: 100%; width: 100%; background-size: contain; background-repeat:no-repeat; background-position:center;'
              }]
            }, {
              name: 'editlineimage',
              kind: 'OB.UI.Thumbnail',
              classes: 'image-wrap image-editline',
              width: '105px',
              height: '105px'
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
    this.$.returnreason.setSelected(0);
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
    if ((!_.isUndefined(line) && !_.isUndefined(line.get('originalOrderLineId'))) || this.model.get('order').get('orderType') === 1) {
      this.$.returnLine.hide();
    } else if (OB.POS.modelterminal.hasPermission(this.$.returnLine.permission)) {
      this.$.returnLine.show();
    }
    this.render();
  },
  receiptChanged: function () {
    this.inherited(arguments);

    this.line = null;

    this.receipt.get('lines').on('selected', this.selectedListener, this);
  },

  render: function () {
    var me = this,
        selectedReason;
    this.inherited(arguments);

    if (this.line) {
      this.$.msgaction.hide();
      this.$.msgedit.show();
      if (OB.MobileApp.model.get('permissions')["OBPOS_retail.productImages"]) {
        this.$.icon.applyStyle('background-image', 'url(' + OB.UTIL.getImageURL(this.line.get('product').get('id')) + '), url(' + "../org.openbravo.mobile.core/assets/img/box.png" + ')');
        this.$.editlineimage.hide();
      } else {
        this.$.editlineimage.setImg(this.line.get('product').get('img'));
        this.$.icon.parent.hide();
      }
      if (this.line.get('gross') < OB.DEC.Zero) {
        if (!_.isUndefined(this.line.get('returnReason'))) {
          selectedReason = _.filter(this.$.returnreason.children, function (reason) {
            return reason.getValue() === me.line.get('returnReason');
          })[0];
          this.$.returnreason.setSelected(selectedReason.getNodeProperty('index'));
        }
        this.$.returnreason.show();
      } else {
        this.$.returnreason.hide();
      }
    } else {
      this.$.txtaction.setContent(OB.I18N.getLabel('OBPOS_NoLineSelected'));
      this.$.msgedit.hide();
      this.$.msgaction.show();
      if (OB.MobileApp.model.get('permissions')["OBPOS_retail.productImages"]) {
        this.$.icon.applyStyle('background-image', '');
      } else {
        if (OB.MobileApp.model.get('permissions')["OBPOS_retail.productImages"]) {
          this.$.icon.applyStyle('background-image', '');
        } else {
          this.$.editlineimage.setImg(null);
        }
      }
    }
    enyo.forEach(this.$.linePropertiesContainer.getComponents(), function (compToRender) {
      if (compToRender.kindName !== 'enyo.ScrollStrategy') {
        compToRender.render(this.line);
      }
    }, this);
  },
  initComponents: function () {
    var sortedPropertiesByPosition;
    this.inherited(arguments);
    sortedPropertiesByPosition = _.sortBy(this.propertiesToShow, function (comp) {
      return (comp.position ? comp.position : (comp.position === 0 ? 0 : 999));
    });
    enyo.forEach(sortedPropertiesByPosition, function (compToCreate) {
      this.$.linePropertiesContainer.createComponent(compToCreate);
    }, this);
  },
  init: function (model) {
    this.model = model;
    this.reasons = new OB.Collection.ReturnReasonList();
    this.$.returnreason.setCollection(this.reasons);

    function errorCallback(tx, error) {
      OB.UTIL.showError("OBDAL error: " + error);
    }

    function successCallbackReasons(dataReasons, me) {
      if (dataReasons && dataReasons.length > 0) {
        me.reasons.reset(dataReasons.models);
      } else {
        me.reasons.reset();
      }
    }
    OB.Dal.find(OB.Model.ReturnReason, null, successCallbackReasons, errorCallback, this);
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