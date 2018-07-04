/*
 ************************************************************************************
 * Copyright (C) 2012-2018 Openbravo S.L.U.
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
        var model = promos.at(0);
        var rule = OB.Model.Discounts.discountRules[model.get('discountType')];
        var amt = 0;
        var requiresQty = !rule.isFixed; // If fixed discount, no requires qty
        var units;
        if (rule.isAmount) {
          amt = model.get('disctTotalamountdisc') ? model.get('disctTotalamountdisc') : model.get('obdiscAmt');
          units = OB.MobileApp.model.get('terminal').currency$_identifier;
        } else {
          amt = model.get('disctTotalpercdisc') ? model.get('disctTotalpercdisc') : model.get('obdiscPercentage');
          units = '%';
        }

        me.discountChanged({}, {
          originator: me.$.discountsList,
          model: model,
          amt: amt,
          requiresQty: requiresQty,
          units: units
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
    var activateButton = false;
    if (inEvent.allChecked) {
      this.$.checkSelectAll.check();
    } else {
      this.$.checkSelectAll.unCheck();
    }
    this.checkedLines = inEvent.checkedLines;

    _.forEach(this.checkedLines, function (checkedLine) {
      if (!checkedLine.get('noDiscountAllow')) {
        activateButton = true;
      }
    });
    if (this.checkedLines.length > 0 && this.discounts.length !== 0 && activateButton) {
      this.$.btnApply.setDisabled(false);
      this.$.btnApply.addStyles('color: orange;');
    } else {
      this.$.btnApply.setDisabled(true);
      this.$.btnApply.addStyles('color: #4C4949;');
    }
  },
  discountChanged: function (inSender, inEvent) {
    // Build discount container info
    var discountsContainer = this.$.discountsContainer;
    discountsContainer.model = inEvent.model;
    discountsContainer.requiresQty = inEvent.requiresQty;
    discountsContainer.amt = inEvent.amt;
    discountsContainer.units = inEvent.units;

    // Disable keyboard if rule is fixed, otherwise, enable keyboard
    if (OB.Model.Discounts.discountRules[inEvent.model.get('discountType')].isFixed) {
      this.disableKeyboard();
    } else {
      this.enableKeyboard();
    }

    OB.UTIL.HookManager.executeHooks('OBPOS_preDiscountChangeHook', {
      context: this,
      discountsContainer: discountsContainer,
      inEvent: inEvent,
      hideLineSelectionOptions: false
    }, function (args) {
      if (args && args.cancelOperation) {
        return;
      }
      if (OB.UTIL.isNullOrUndefined(args.discountsContainer) || OB.UTIL.isNullOrUndefined(args.discountsContainer.model)) {
        // Mandatory infornation
        OB.UTIL.showError('Critical discount information is missing: ' + (args.discountsContainer ? 'Discount model' : 'Discount Container'));
      } else if (OB.UTIL.isNullOrUndefined(args.discountsContainer.amt) || OB.UTIL.isNullOrUndefined(args.discountsContainer.units)) {
        // Without this information, the discounts could not be applied
        OB.UTIL.showWarning('Some discount information is missing, the promotion could not be applied: ' + (args.discountsContainer.amt ? 'Discount units' : 'Discount amount'));
      }
      if (args.hideLineSelectionOptions) {
        args.context.$.applyCheckSelectAll.hide();
        args.context.order.get('lines').trigger('hideAllCheckBtn');
        args.context.$.btnApply.setDisabled(false);
        args.context.$.btnApply.addStyles('color: orange;');
      } else {
        args.context.$.applyCheckSelectAll.show();
        args.context.order.get('lines').trigger('showAllCheckBtn');

        if (args.context.checkedLines.length > 0 && !OB.UTIL.isNullOrUndefined(args.context.discounts) && args.context.discounts.length !== 0) {
          args.context.$.btnApply.setDisabled(false);
          args.context.$.btnApply.addStyles('color: orange;');
        } else {
          args.context.$.btnApply.setDisabled(true);
          args.context.$.btnApply.addStyles('color: #4C4949;');
        }
      }
    });
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
        me.closingDiscounts();
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
  handlers: {
    onchange: 'changeDiscount'
  },
  classes: 'discount-dialog-profile-combo',
  renderEmpty: enyo.Control,
  renderLine: 'OB.UI.DiscountList.Options',
  initComponents: function () {
    this.inherited(arguments);
  },
  changeDiscount: function () {
    var model = this.collection.at(this.getSelected());
    var rule = OB.Model.Discounts.discountRules[model.get('discountType')];
    var amt = 0;
    var requiresQty = !rule.isFixed; // If fixed discount, no requires qty
    var units;
    if (rule.isAmount) {
      amt = model.get('disctTotalamountdisc') ? model.get('disctTotalamountdisc') : model.get('obdiscAmt');
      units = OB.MobileApp.model.get('terminal').currency$_identifier;
    } else {
      amt = model.get('disctTotalpercdisc') ? model.get('disctTotalpercdisc') : model.get('obdiscPercentage');
      units = '%';
    }

    this.owner.discountChanged({}, {
      originator: this,
      model: model,
      amt: amt,
      requiresQty: requiresQty,
      units: units
    });
  }
});

enyo.kind({
  kind: 'enyo.Option',
  name: 'OB.UI.DiscountList.Options',
  initComponents: function () {
    var rule = OB.Model.Discounts.discountRules[this.model.get('discountType')],
        propertyToShow = '';
    if (rule.getAmountProperty && rule.getAmountProperty instanceof Function) {
      propertyToShow = OB.Model.Discounts.discountRules[this.model.get('discountType')].getAmountProperty();
    }
    this.setValue(this.model.get('id'));
    this.originalText = this.model.get('_identifier');
    // TODO: this shouldn't be hardcoded but defined in each promotion
    if (!OB.Model.Discounts.discountRules[this.model.get('discountType')].isFixed) {
      //variable
      this.requiresQty = true;
      if (!OB.Model.Discounts.discountRules[this.model.get('discountType')].isAmount) {
        //variable porcentaje
        this.units = '%';
        if (!_.isUndefined(this.model.get(propertyToShow)) && !_.isNull(this.model.get(propertyToShow))) {
          this.amt = this.model.get(propertyToShow);
        }
      } else {
        //variable qty
        this.units = OB.MobileApp.model.get('terminal').currency$_identifier;
        if (this.model.get(propertyToShow)) {
          this.amt = this.model.get(propertyToShow);
        }
      }
    } else {
      //fixed
      this.requiresQty = false;
      if (!OB.Model.Discounts.discountRules[this.model.get('discountType')].isAmount) {
        //fixed percentage
        this.units = '%';
        if (!_.isUndefined(this.model.get(propertyToShow)) && !_.isNull(this.model.get(propertyToShow))) {
          this.amt = this.model.get(propertyToShow);
        }
      } else {
        //fixed amount
        this.units = OB.MobileApp.model.get('terminal').currency$_identifier;
        if (!_.isUndefined(this.model.get(propertyToShow)) && !_.isNull(this.model.get(propertyToShow))) {
          this.amt = this.model.get(propertyToShow);
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