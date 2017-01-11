/*
 ************************************************************************************
 * Copyright (C) 2012-2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, Backbone, _*/
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.Discounts',
  handlers: {
    onApplyDiscounts: 'applyDiscounts',
    onDiscountsClose: 'closingDiscounts',
    onDiscountChanged: 'discountChanged',
    onDiscountQtyChanged: 'discountQtyChanged',
    onCheckedTicketLine: 'ticketLineChecked'
  },
  events: {
    onDiscountsModeFinished: '',
    onDisableKeyboard: '',
    onDiscountsModeKeyboard: '',
    onShowPopup: '',
    onCheckAllTicketLines: ''
  },
  checkedLines: [],
  style: 'position:relative; background-color: orange; background-size: cover; color: white; height: 200px; margin: 5px; padding: 5px;',
  components: [{
    kind: 'Scroller',
    maxHeight: '130px',
    style: 'height: 130px',
    thumb: true,
    horizontal: 'hidden',
    components: [{
      components: [{
        style: 'border: 1px solid #F0F0F0; background-color: #E2E2E2; color: black; width: 35%; height: 40px; float: left; text-align: left;',
        components: [{
          style: 'padding: 5px 8px 0px 3px;',
          initComponents: function () {
            this.setContent(OB.I18N.getLabel('OBPOS_LineDiscount'));
          }
        }]
      }, {
        name: 'discountsContainer',
        style: 'border: 1px solid #F0F0F0; float: left; width: 63%;',
        components: [{
          name: 'discountsList',
          kind: 'OB.UI.DiscountList'
        }]
      }]
    }, {
      style: 'clear: both'
    }, {
      components: [{
        style: 'border: 1px solid #F0F0F0; background-color: #E2E2E2; color: black; width: 35%; height: 40px; float: left; text-align: left;',
        components: [{
          style: 'padding: 5px 8px 0px 3px;',
          initComponents: function () {
            this.setContent(OB.I18N.getLabel('OBPOS_overridePromotions'));
          }
        }]
      }, {
        style: 'border: 1px solid #F0F0F0; float: left; width: 63%;',
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
      name: 'applyCheckSelectAll',
      components: [{
        style: 'border: 1px solid #F0F0F0; background-color: #E2E2E2; color: black; width: 35%; height: 40px; float: left;  text-align: left;',
        components: [{
          style: 'padding: 5px 8px 0px 3px;',
          initComponents: function () {
            this.setContent(OB.I18N.getLabel('OBPOS_applyToAllLines'));
          }
        }]
      }, {
        style: 'border: 1px solid #F0F0F0; float: left; width: 63%;',
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
    }]
  }, {
    style: 'padding: 10px;',
    components: [{
      style: 'text-align: center;',
      components: [{
        kind: 'OB.OBPOSPointOfSale.UI.Discounts.btnDiscountsApply',
        name: 'btnApply'
      }, {
        kind: 'OB.OBPOSPointOfSale.UI.Discounts.btnDiscountsCancel'
      }]
    }]
  }],
  show: function () {
    var me = this;
    me.$.btnApply.setDisabled(true);
    me.discounts.reset();
    me.order.trigger('showDiscount');
    //uncheck lines
    this.doCheckAllTicketLines({
      status: false
    });
    //load discounts
    OB.Dal.find(OB.Model.Discount, {
      _whereClause: "where m_offer_type_id in (" + OB.Model.Discounts.getManualPromotions() + ") AND date('now') BETWEEN DATEFROM AND COALESCE(date(DATETO), date('9999-12-31'))"
      // filter by order price list
      + " AND (" + " (pricelist_selection = 'Y' AND NOT EXISTS " //
      + " (SELECT 1 FROM m_offer_pricelist opl WHERE m_offer.m_offer_id = opl.m_offer_id AND opl.m_pricelist_id = '" + me.order.get('priceList') + "'))" // 
      + " OR (pricelist_selection = 'N' AND EXISTS " //
      + " (SELECT 1 FROM m_offer_pricelist opl WHERE m_offer.m_offer_id = opl.m_offer_id AND opl.m_pricelist_id = '" + me.order.get('priceList') + "')))" //
      // filter discretionary discounts by current role
      + " AND ((EM_OBDISC_ROLE_SELECTION = 'Y'" //
      + " AND NOT EXISTS" //
      + " (SELECT 1" //
      + " FROM OBDISC_OFFER_ROLE" //
      + " WHERE M_OFFER_ID = M_OFFER.M_OFFER_ID" //
      + "   AND AD_ROLE_ID = '" + OB.MobileApp.model.get('context').role.id + "'" //
      + " ))" //
      + " OR (EM_OBDISC_ROLE_SELECTION = 'N'" //
      + " AND EXISTS" //
      + " (SELECT 1" //
      + " FROM OBDISC_OFFER_ROLE" //
      + " WHERE M_OFFER_ID = M_OFFER.M_OFFER_ID" //
      + "   AND AD_ROLE_ID = '" + OB.MobileApp.model.get('context').role.id + "'" //
      + " )))" //
    }, function (promos) {
      promos.comparator = function (model) {
        return model.get('_identifier');
      };
      promos.sort();
      me.discounts.reset(promos.models);
      me.ticketLineChecked({}, {
        checkedLines: me.checkedLines
      });
      //set the keyboard for selected discount
      if (promos.length > 0) {
        me.discountChanged({}, {
          originator: me.$.discountsList
        });
      }
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
      me.ticketLineChecked({}, {
        checkedLines: me.checkedLines
      });
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
    if (!OB.DEC.isNumber(inEvent.qty)) {
      this.doShowPopup({
        popup: 'modalNotValidValueForDiscount'
      });
      return;
    }
    var comp = this._searchSelectedComponent(this.$.discountsList.getValue());
    if (comp.units === '%' && OB.DEC.toBigDecimal(inEvent.qty) > 100) {
      this.doShowPopup({
        popup: 'modalNotValidValueForDiscount'
      });
      return;
    }
    comp.setContent(comp.originalText + ' - ' + inEvent.qty + ' ' + comp.units);
    this.$.discountsContainer.amt = inEvent.qty;
  },
  initComponents: function () {
    var discountsModel = Backbone.Collection.extend({
      model: OB.Model.Discounts
    });
    this.inherited(arguments);

    this.discounts = new discountsModel();
    this.$.discountsList.setCollection(this.discounts);
  },
  ticketLineChecked: function (inSender, inEvent) {
    if (inEvent.allChecked) {
      this.$.checkSelectAll.check();
    } else {
      this.$.checkSelectAll.unCheck();
    }
    this.checkedLines = inEvent.checkedLines;
    if (this.checkedLines.length > 0 && this.discounts.length !== 0) {
      this.$.btnApply.setDisabled(false);
      this.$.btnApply.addStyles('color: orange;');
    } else {
      this.$.btnApply.setDisabled(true);
      this.$.btnApply.addStyles('color: #4C4949;');
    }
  },
  discountChanged: function (inSender, inEvent) {
    var comp = this._searchSelectedComponent(this.$.discountsList.getValue()),
        discountsContainer = this.$.discountsContainer;
    discountsContainer.model = comp.model;
    discountsContainer.requiresQty = comp.requiresQty;
    discountsContainer.amt = comp.amt;
    discountsContainer.units = comp.units;
    if (comp.model.get('discountType') === "8338556C0FBF45249512DB343FEFD280" || comp.model.get('discountType') === "7B49D8CC4E084A75B7CB4D85A6A3A578") {
      this.disableKeyboard();
    } else {
      //enable keyboard
      this.enableKeyboard();
    }
  },
  closingDiscounts: function (inSender, inEvent) {
    OB.MobileApp.view.scanningFocus(true);
    this.$.checkSelectAll.unCheck();
    this.setShowing(false);
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
        discountsContainer = this.$.discountsContainer,
        orderLinesCollection = new OB.Collection.OrderLineList(),
        me = this;
    //preApplyDiscountsHook
    OB.UTIL.HookManager.executeHooks('OBPOS_preApplyDiscountsHook', {
      context: this
    }, function (args) {
      if (args && args.cancelOperation) {
        return;
      }
      promotionToAplly.rule = discountsContainer.model;
      promotionToAplly.definition = {};
      promotionToAplly.definition.userAmt = discountsContainer.amt;
      promotionToAplly.definition.applyNext = !me.$.checkOverride.checked;
      promotionToAplly.definition.lastApplied = true;

      if (discountsContainer.requiresQty && !discountsContainer.amt) {
        //Show a modal pop up with the error
        me.doShowPopup({
          popup: 'modalDiscountNeedQty'
        });
        return true;
      }

      _.each(me.checkedLines, function (line) {
        orderLinesCollection.add(line);
      });

      OB.Model.Discounts.addManualPromotion(me.order, orderLinesCollection, promotionToAplly);

      me.closingDiscounts();
    });
  },
  init: function (model) {
    this.order = model.get('order');
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.Discounts.btnDiscountsApply',
  kind: 'OB.UI.ModalDialogButton',
  style: 'color: orange; font-weight: bold;',
  i18nLabel: 'OBMOBC_LblApply',
  events: {
    onApplyDiscounts: ''
  },
  tap: function () {
    this.doApplyDiscounts();
  },
  initComponents: function () {
    this.inherited(arguments);
    this.setDisabled(true);
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.Discounts.btnCheckAll',
  kind: 'OB.UI.CheckboxButton',
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
  name: 'OB.OBPOSPointOfSale.UI.Discounts.btnCheckOverride',
  kind: 'OB.UI.CheckboxButton',
  checked: false
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.Discounts.btnDiscountsCancel',
  kind: 'OB.UI.ModalDialogButton',
  style: 'color: orange; font-weight: bold;',
  events: {
    onDiscountsClose: ''
  },
  i18nContent: 'OBMOBC_LblCancel',
  tap: function () {
    this.doDiscountsClose();
  }
});

enyo.kind({
  name: 'OB.UI.DiscountList',
  kind: 'OB.UI.List',
  tag: 'select',
  onchange: 'discountChanged',
  classes: 'discount-dialog-profile-combo',
  renderEmpty: enyo.Control,
  renderLine: 'OB.UI.DiscountList.Options',
  initComponents: function () {
    this.inherited(arguments);
  }
});

enyo.kind({
  kind: 'enyo.Option',
  name: 'OB.UI.DiscountList.Options',
  initComponents: function () {
    this.setValue(this.model.get('id'));
    this.originalText = this.model.get('_identifier');
    // TODO: this shouldn't be hardcoded but defined in each promotion
    if (this.model.get('discountType') === 'D1D193305A6443B09B299259493B272A' || this.model.get('discountType') === '20E4EC27397344309A2185097392D964') {
      //variable
      this.requiresQty = true;
      if (this.model.get('discountType') === '20E4EC27397344309A2185097392D964') {
        //variable porcentaje
        this.units = '%';
        if (!_.isUndefined(this.model.get('obdiscPercentage')) && !_.isNull(this.model.get('obdiscPercentage'))) {
          this.amt = this.model.get('obdiscPercentage');
        }
      } else if (this.model.get('discountType') === 'D1D193305A6443B09B299259493B272A') {
        //variable qty
        this.units = OB.MobileApp.model.get('terminal').currency$_identifier;
        if (this.model.get('obdiscAmt')) {
          this.amt = this.model.get('obdiscAmt');
        }
      }
    } else {
      //fixed
      this.requiresQty = false;
      if (this.model.get('discountType') === '8338556C0FBF45249512DB343FEFD280') {
        //fixed percentage
        this.units = '%';
        if (!_.isUndefined(this.model.get('obdiscPercentage')) && !_.isNull(this.model.get('obdiscPercentage'))) {
          this.amt = this.model.get('obdiscPercentage');
        }
      } else if (this.model.get('discountType') === '7B49D8CC4E084A75B7CB4D85A6A3A578') {
        //fixed amount
        this.units = OB.MobileApp.model.get('terminal').currency$_identifier;
        if (!_.isUndefined(this.model.get('obdiscAmt')) && !_.isNull(this.model.get('obdiscAmt'))) {
          this.amt = this.model.get('obdiscAmt');
        }
      }
    }
    if (this.amt) {
      this.setContent(this.originalText + ' - ' + this.amt + ' ' + this.units);
    } else {
      this.setContent(this.originalText);
    }
  }
});