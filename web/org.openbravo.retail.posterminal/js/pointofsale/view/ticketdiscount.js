/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, _*/
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.Discounts',
  handlers: {
    onApllyDiscounts: 'applyDiscounts',
    onDiscountsClose: 'closingDiscounts',
    onDiscountQtyChanged: 'discountQtyChanged'
  },
  events: {
    onDiscountsModeFinished: '',
    onDisableKeyboard: '',
    onDiscountsModeKeyboard: ''
  },
  style: 'position:relative; background-color: orange; background-size: cover; color: white; height: 200px; margin: 5px; padding: 5px',
  components: [{
    components: [{
      style: 'border: 1px solid #F0F0F0; background-color: #E2E2E2; color: black; width: 170px; height: 40px; float: left; text-align: left',
      components: [{
        style: 'padding: 5px 8px 0px 3px;',
        content: OB.I18N.getLabel('OBPOS_LineDiscount')
      }]
    }, {
      style: 'border: 1px solid #F0F0F0; float: left;',
      components: [{
        kind: 'OB.UI.List',
        name: 'discountsList',
        tag: 'select',
        onchange: 'discountChanged',
        classes: 'modal-dialog-profile-combo',
        renderEmpty: enyo.Control,
        renderLine: enyo.kind({
          kind: 'enyo.Option',
          initComponents: function () {
            this.setValue(this.model.get('id'));
            this.setContent(this.model.get('_identifier'));
            this.originalText = this.model.get('_identifier');
            if (this.model.get('discountType') === 'D1D193305A6443B09B299259493B272A' || this.model.get('discountType') === '20E4EC27397344309A2185097392D964') {
              this.requiresQty = true;
              if (this.model.get('discountType') === '20E4EC27397344309A2185097392D964') {
                this.units = '%';
              } else {
                this.units = OB.POS.modelterminal.get('terminal').currency$_identifier;
              }
            }
          }
        })
      }]
    }]
  }, {
    style: 'clear: both'
  }, {
    components: [{
      style: 'border: 1px solid #F0F0F0; background-color: #E2E2E2; color: black; width: 170px; height: 40px; float: left; text-align: left',
      components: [{
        style: 'padding: 5px 8px 0px 3px;',
        content: OB.I18N.getLabel('OBPOS_overridePromotions')
      }]
    }, {
      style: 'border: 1px solid #F0F0F0; float: left;',
      components: [{
        classes: 'modal-dialog-profile-checkbox',
        components: [{
          kind: 'OB.OBPOSPointOfSale.UI.Discounts.btnCheckOverride',
          name: 'checkOverride',
          classes: 'modal-dialog-btn-check'
        }]
      }]
    }]
  }, {
    style: 'clear: both'
  }, {
    components: [{
      style: 'border: 1px solid #F0F0F0; background-color: #E2E2E2; color: black; width: 170px; height: 40px; float: left;  text-align: left',
      components: [{
        style: 'padding: 5px 8px 0px 3px;',
        content: OB.I18N.getLabel('OBPOS_applyToAllLines')
      }]
    }, {
      style: 'border: 1px solid #F0F0F0; float: left;',
      components: [{
        classes: 'modal-dialog-profile-checkbox',
        components: [{
          kind: 'OB.OBPOSPointOfSale.UI.Discounts.btnCheckAll',
          name: 'checkSelectAll',
          classes: 'modal-dialog-btn-check'
        }]
      }]
    }]
  }, {
    style: 'clear: both'
  }, {
    style: 'padding: 10px;',
    components: [{
      style: 'text-align: center;',
      components: [{
        kind: 'OB.OBPOSPointOfSale.UI.Discounts.btnDiscountsApply'
      }, {
        kind: 'OB.OBPOSPointOfSale.UI.Discounts.btnDiscountsCancel'
      }]
    }]
  }],
  show: function () {
    var me = this;
    me.discounts.reset();
    //load discounts
    OB.Dal.find(OB.Model.Discount, {
      _whereClause: "where m_offer_type_id in ('D1D193305A6443B09B299259493B272A', '20E4EC27397344309A2185097392D964', '7B49D8CC4E084A75B7CB4D85A6A3A578', '8338556C0FBF45249512DB343FEFD280')"
    }, function (promos) {
      me.discounts.reset(promos.models);
      //set the keyboard for selected discount 
      me.discountChanged({}, {
        originator: me.$.discountsList
      });
    }, function () {
      //show an error in combo
      var tr;
      me.discounts.reset();
      tr = me.$.discountsList.createComponent({
        kind: 'enyo.Option',
        text: OB.I18N.getLabel('OBPOS_errorGettingDiscounts'),
        value: 'error',
        initComponents: function () {
          this.setValue(this.value);
          this.setContent(this.text);
        }
      });
      tr.render();
    });
    this.inherited(arguments);
  },
  disableKeyboard: function () {
    this.doDiscountsModeKeyboard({
      status: true,
      writable: false
    });
  },
  enableKeyboard: function () {
    this.doDiscountsModeKeyboard({
      status: true,
      writable: true
    });
  },
  _searchSelectedComponent: function (selectedId) {
    return _.find(this.$.discountsList.getComponents(), function (comp) {
      if (comp.getValue() === selectedId) {
        return true;
      }
    }, this);
  },
  discountQtyChanged: function (inSender, inEvent) {
    var comp = this._searchSelectedComponent(this.$.discountsList.getValue());
    comp.setContent(comp.originalText + ' - ' + inEvent.qty + ' ' + comp.units);
    comp.amt = inEvent.qty;
  },
  initComponents: function () {
    var discountsModel = Backbone.Collection.extend({
      model: OB.Model.Discounts
    });
    this.inherited(arguments);

    this.discounts = new discountsModel();
    this.$.discountsList.setCollection(this.discounts);
  },
  discountChanged: function (inSender, inEvent) {
    var selectedDiscount = inEvent.originator.collection.find(function (discount) {
      if (discount.get('id') === inEvent.originator.getValue()) {
        return true;
      }
    }, this);
    if (selectedDiscount.get('discountType') === "8338556C0FBF45249512DB343FEFD280" || selectedDiscount.get('discountType') === "7B49D8CC4E084A75B7CB4D85A6A3A578") {
      //no keyboard
      this.disableKeyboard();
    } else {
      //enable keyboard
      this.enableKeyboard();
    }
  },
  closingDiscounts: function (inSender, inEvent) {
    this.$.checkSelectAll.unCheck();
    this.doDiscountsModeFinished({
      tabPanel: 'scan',
      keyboard: 'toolbarscan',
      edit: false,
      options: {
        discounts: false
      }
    });
  },
  applyDiscounts: function (inSender, inEvent) {
    var promotionToAplly = {},
        comp = this._searchSelectedComponent(this.$.discountsList.getValue());
    promotionToAplly.rule = comp.model;
    promotionToAplly.definition = {};
    promotionToAplly.definition.userAmt = comp.amt;
    promotionToAplly.definition.applyNext = !this.$.checkOverride.checked;

    if (comp.requiresQty && !comp.amt) {
      console.log('error -> this discount requires a qty');
      return true;
    }

    //TODO Get selected lines
    OB.Model.Discounts.addManualPromotion(this.order, this.order.get('lines'), promotionToAplly);

    this.closingDiscounts();
  },
  init: function (model) {
    this.order = model.get('order');
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.OBPOSPointOfSale.UI.Discounts.btnDiscountsApply',
  style: 'color: orange; font-weight: bold;',
  content: OB.I18N.getLabel('OBPOS_LblApply'),
  events: {
    onApplyDiscounts: ''
  },
  tap: function () {
    this.doApplyDiscounts();
  }
});

enyo.kind({
  kind: 'OB.UI.CheckboxButton',
  name: 'OB.OBPOSPointOfSale.UI.Discounts.btnCheckAll',
  events: {
    onCheckAllTicketLines: ''
  },
  checked: false,
  tap: function () {
    this.inherited(arguments);
    this.doCheckAllTicketLines({
      status: this.checked
    });
  }
});

enyo.kind({
  kind: 'OB.UI.CheckboxButton',
  name: 'OB.OBPOSPointOfSale.UI.Discounts.btnCheckOverride',
  checked: false
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.Discounts.btnDiscountsCancel',
  kind: 'OB.UI.ModalDialogButton',
  style: 'color: orange; font-weight: bold;',
  events: {
    onDiscountsClose: ''
  },
  content: OB.I18N.getLabel('OBPOS_LblCancel'),
  tap: function () {
    this.doDiscountsClose();
  }
});