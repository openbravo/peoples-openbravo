/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, Backbone, _*/
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.Discounts',
  classes: 'obObposPointOfSaleUiDiscounts',
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
  components: [
    {
      kind: 'Scroller',
      classes: 'obObposPointOfSaleUiDiscounts-scroller',
      thumb: true,
      components: [
        {
          kind: 'OB.UI.FormElement',
          name: 'formElementDiscountsList',
          classes:
            'obUiFormElement_dataEntry obObposPointOfSaleUiDiscounts-scroller-formElementDiscountsList',
          coreElement: {
            kind: 'OB.UI.DiscountList',
            name: 'discountsList',
            i18nLabel: 'OBPOS_LineDiscount',
            classes:
              'obObposPointOfSaleUiDiscounts-scroller-formElementDiscountsList-discountsList'
          }
        },
        {
          kind: 'OB.UI.FormElement',
          name: 'formElementCheckOverride',
          classes:
            'obUiFormElement_dataEntry obObposPointOfSaleUiDiscounts-scroller-formElementCheckOverride',
          coreElement: {
            kind: 'OB.OBPOSPointOfSale.UI.Discounts.btnCheckOverride',
            name: 'checkOverride',
            i18nLabel: 'OBPOS_overridePromotions',
            classes:
              'obObposPointOfSaleUiDiscounts-scroller-formElementCheckOverride-checkOverride'
          }
        },
        {
          kind: 'OB.UI.FormElement',
          name: 'formElementCheckSelectAll',
          classes:
            'obUiFormElement_dataEntry obObposPointOfSaleUiDiscounts-scroller-formElementCheckSelectAll',
          coreElement: {
            kind: 'OB.OBPOSPointOfSale.UI.Discounts.btnCheckAll',
            name: 'checkSelectAll',
            i18nLabel: 'OBPOS_applyToAllLines',
            classes:
              'obObposPointOfSaleUiDiscounts-scroller-formElementCheckSelectAll-checkSelectAll'
          }
        }
      ]
    },
    {
      classes: 'obObposPointOfSaleUiDiscounts-buttons',
      components: [
        {
          kind: 'OB.OBPOSPointOfSale.UI.Discounts.btnDiscountsCancel',
          classes:
            'obObposPointOfSaleUiDiscounts-buttons-obObposPointOfSaleUiBtnDiscountsCancel'
        },
        {
          kind: 'OB.OBPOSPointOfSale.UI.Discounts.btnDiscountsApply',
          name: 'btnApply',
          isDefaultAction: true,
          classes: 'obObposPointOfSaleUiDiscounts-buttons-btnApply'
        }
      ]
    }
  ],
  show: function() {
    var me = this;
    function loadDiscounts() {
      try {
        let discountArray = OB.Discounts.Pos.manualRuleImpls;

        discountArray = OB.Discounts.Pos.filterDiscountsByDate(
          discountArray,
          new Date()
        );

        discountArray = OB.Discounts.Pos.filterDiscountsByPriceList(
          discountArray,
          me.order.get('priceList')
        );

        discountArray.comparator = function(model) {
          return model.get('_identifier');
        };
        me.discounts.reset(discountArray);
        if (
          OB.MobileApp.model.hasPermission('OBPOS_AutoSelectDiscounts', true)
        ) {
          let selectedModels = OB.MobileApp.view.$.containerWindow.getRoot().$
            .multiColumn.$.rightPanel.$.keyboard.selectedModels;
          let i;
          for (i = 0; i < selectedModels.length; i++) {
            selectedModels[i].trigger('check');
          }
        } else {
          me.ticketLineChecked(
            {},
            {
              checkedLines: me.checkedLines
            }
          );
        }
        //set the keyboard for selected discount
        if (me.discounts.length > 0) {
          let model = me.discounts.at(0);
          let rule =
            OB.Model.Discounts.discountRules[model.get('discountType')];
          let amt = 0;
          let requiresQty = !rule.isFixed; // If fixed discount, no requires qty
          let units;
          if (rule.isAmount) {
            amt = model.get('disctTotalamountdisc')
              ? model.get('disctTotalamountdisc')
              : model.get('obdiscAmt');
            units = OB.MobileApp.model.get('terminal').currency$_identifier;
          } else {
            amt = model.get('disctTotalpercdisc')
              ? model.get('disctTotalpercdisc')
              : model.get('obdiscPercentage');
            units = '%';
          }

          me.discountChanged(
            {},
            {
              originator: me.$.formElementDiscountsList.coreElement,
              model: model,
              amt: amt,
              requiresQty: requiresQty,
              units: units
            }
          );
        }
      } catch (err) {
        //show an error in combo
        var tr;
        me.discounts.reset();
        tr = me.$.formElementDiscountsList.coreElement.createComponent({
          kind: 'OB.UI.FormElement.Select.Option',
          text: OB.I18N.getLabel('OBPOS_errorGettingDiscounts'),
          value: 'error',
          initComponents: function() {
            this.setValue(this.value);
            this.setContent(this.text);
          }
        });
        tr.render();
        if (
          OB.MobileApp.model.hasPermission('OBPOS_AutoSelectDiscounts', true)
        ) {
          let selectedModels = OB.MobileApp.view.$.containerWindow.getRoot().$
            .multiColumn.$.rightPanel.$.keyboard.selectedModels;
          let i;
          for (i = 0; i < selectedModels.length; i++) {
            selectedModels[i].trigger('check');
          }
        } else {
          me.ticketLineChecked(
            {},
            {
              checkedLines: me.checkedLines
            }
          );
        }
      }
    }

    me.$.btnApply.setDisabled(true);
    me.discounts.reset();
    me.order.trigger('showDiscount');
    //uncheck lines
    this.doCheckAllTicketLines({
      status: false
    });
    loadDiscounts();
    this.inherited(arguments);
  },
  disableKeyboard: function() {
    this.doDiscountsModeKeyboard({
      status: true,
      writable: false
    });
  },
  enableKeyboard: function() {
    this.doDiscountsModeKeyboard({
      status: true,
      writable: true
    });
  },
  _searchSelectedComponent: function(selectedId) {
    return _.find(
      this.$.formElementDiscountsList.coreElement.getComponents(),
      function(comp) {
        if (comp.getValue() === selectedId) {
          return true;
        }
      },
      this
    );
  },
  discountQtyChanged: function(inSender, inEvent) {
    if (!OB.DEC.isNumber(inEvent.qty)) {
      this.doShowPopup({
        popup: 'modalNotValidValueForDiscount'
      });
      return;
    }
    var comp = this._searchSelectedComponent(
      this.$.formElementDiscountsList.coreElement.getValue()
    );
    if (comp.units === '%' && OB.DEC.toBigDecimal(inEvent.qty) > 100) {
      this.doShowPopup({
        popup: 'modalNotValidValueForDiscount'
      });
      return;
    }
    comp.setContent(comp.originalText + ' - ' + inEvent.qty + ' ' + comp.units);
    this.$.formElementDiscountsList.amt = inEvent.qty;
    this.$.formElementDiscountsList.amtChanged = true;
  },
  initComponents: function() {
    this.inherited(arguments);
    this.discounts = new Backbone.Collection();
    this.$.formElementDiscountsList.coreElement.setCollection(this.discounts);
  },
  ticketLineChecked: function(inSender, inEvent) {
    var activateButton = false;
    if (inEvent.allChecked) {
      this.$.formElementCheckSelectAll.coreElement.check();
    } else {
      this.$.formElementCheckSelectAll.coreElement.unCheck();
    }
    this.checkedLines = inEvent.checkedLines;

    _.forEach(this.checkedLines, function(checkedLine) {
      if (!checkedLine.get('noDiscountAllow')) {
        activateButton = true;
      }
    });
    if (
      this.checkedLines.length > 0 &&
      this.discounts.length !== 0 &&
      activateButton
    ) {
      this.$.btnApply.setDisabled(false);
      this.$.btnApply.addClass(
        '.obObposPointOfSaleUiDiscounts-buttons-btnApply_activate'
      );
    } else {
      this.$.btnApply.setDisabled(true);
      this.$.btnApply.addClass(
        '.obObposPointOfSaleUiDiscounts-buttons-btnApply_desactivate'
      );
    }
  },
  discountChanged: function(inSender, inEvent) {
    // Build discount container info
    var formElementDiscountsList = this.$.formElementDiscountsList,
      selectedOption = inEvent.originator.getSelected();
    formElementDiscountsList.model = inEvent.model;
    formElementDiscountsList.requiresQty = inEvent.requiresQty;
    formElementDiscountsList.amt = inEvent.amt;
    formElementDiscountsList.units = inEvent.units;

    //Reset all combo options
    if (!OB.UTIL.isNullOrUndefined(this.discounts)) {
      this.discounts.reset(this.discounts.models);
      inEvent.originator.setSelected(selectedOption);
    }

    // Disable keyboard if rule is fixed, otherwise, enable keyboard
    if (
      OB.Model.Discounts.discountRules[inEvent.model.get('discountType')]
        .isFixed
    ) {
      this.disableKeyboard();
    } else {
      this.enableKeyboard();
    }

    OB.UTIL.HookManager.executeHooks(
      'OBPOS_preDiscountChangeHook',
      {
        context: this,
        formElementDiscountsList: formElementDiscountsList,
        inEvent: inEvent,
        hideLineSelectionOptions: false
      },
      function(args) {
        if (args && args.cancelOperation) {
          return;
        }
        if (
          OB.UTIL.isNullOrUndefined(args.formElementDiscountsList) ||
          OB.UTIL.isNullOrUndefined(args.formElementDiscountsList.model)
        ) {
          // Mandatory infornation
          OB.UTIL.showError(
            'Critical discount information is missing: ' +
              (args.formElementDiscountsList
                ? 'Discount model'
                : 'Discount Container')
          );
        } else if (
          OB.UTIL.isNullOrUndefined(args.formElementDiscountsList.amt) ||
          OB.UTIL.isNullOrUndefined(args.formElementDiscountsList.units)
        ) {
          // Without this information, the discounts could not be applied
          OB.UTIL.showWarning(
            'Some discount information is missing, the promotion could not be applied: ' +
              (args.formElementDiscountsList.amt
                ? 'Discount units'
                : 'Discount amount')
          );
        }
        if (args.hideLineSelectionOptions) {
          args.context.$.formElementCheckSelectAll.hide();
          args.context.order.get('lines').trigger('hideAllCheckBtn');
          args.context.$.btnApply.setDisabled(false);
          args.context.$.btnApply.addClass(
            '.obObposPointOfSaleUiDiscounts-buttons-btnApply_activate'
          );
        } else {
          args.context.$.formElementCheckSelectAll.show();
          args.context.order.get('lines').trigger('showAllCheckBtn');

          if (
            args.context.checkedLines.length > 0 &&
            ((!OB.UTIL.isNullOrUndefined(args.context.discounts) &&
              args.context.discounts.length !== 0) ||
              (!OB.UTIL.isNullOrUndefined(args.formElementDiscountsList) &&
                !OB.UTIL.isNullOrUndefined(
                  args.formElementDiscountsList.model
                )))
          ) {
            args.context.$.btnApply.setDisabled(false);
            args.context.$.btnApply.addClass(
              '.obObposPointOfSaleUiDiscounts-buttons-btnApply_activate'
            );
          } else {
            args.context.$.btnApply.setDisabled(true);
            args.context.$.btnApply.addClass(
              '.obObposPointOfSaleUiDiscounts-buttons-btnApply_desactivate'
            );
          }
        }
        if (formElementDiscountsList.model.get('obdiscAllowmultipleinstan')) {
          args.context.$.formElementCheckOverride.hide();
          args.context.$.formElementCheckOverride.$.coreElementContainer.$.checkOverride.unCheck();
        } else {
          args.context.$.formElementCheckOverride.show();
        }
      }
    );
  },
  closingDiscounts: function(inSender, inEvent) {
    OB.MobileApp.view.scanningFocus(true);
    this.$.formElementCheckSelectAll.coreElement.unCheck();
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

  getMaxNoOrder: function(receipt) {
    let maxNoOrder = 0;

    if (receipt.get('discountsFromUser')) {
      if (receipt.get('discountsFromUser').manualPromotions) {
        const manualPromotions = receipt.get('discountsFromUser')
          .manualPromotions;
        manualPromotions.forEach(manualPromotion => {
          const noOrder = manualPromotion.noOrder || 0;
          if (noOrder > maxNoOrder) {
            maxNoOrder = noOrder;
          }
        });
      }

      if (receipt.get('discountsFromUser').bytotalManualPromotions) {
        const bytotalManualPromotions = receipt.get('discountsFromUser')
          .bytotalManualPromotions;
        bytotalManualPromotions.forEach(bytotalManualPromotion => {
          const noOrder = bytotalManualPromotion.noOrder || 0;
          if (noOrder > maxNoOrder) {
            maxNoOrder = noOrder;
          }
        });
      }
    }

    return maxNoOrder;
  },
  applyDiscounts: function(inSender, inEvent) {
    var promotionToApply = {},
      formElementDiscountsList = this.$.formElementDiscountsList,
      orderLinesCollection = new OB.Collection.OrderLineList(),
      me = this;
    //preApplyDiscountsHook
    OB.UTIL.HookManager.executeHooks(
      'OBPOS_preApplyDiscountsHook',
      {
        context: this
      },
      function(args) {
        if (args && args.cancelOperation) {
          me.closingDiscounts();
          return;
        }
        if (!me.$.formElementCheckOverride.showing) {
          me.$.formElementCheckOverride.$.coreElementContainer.$.checkOverride.unCheck();
        }
        promotionToApply.rule = formElementDiscountsList.model;
        promotionToApply.definition = {};
        promotionToApply.definition.userAmt = formElementDiscountsList.amt;
        promotionToApply.definition.applyNext = !me.$.formElementCheckOverride.coreElement.getChecked();
        promotionToApply.definition.lastApplied = true;
        promotionToApply.definition.obdiscLineFinalgross =
          formElementDiscountsList.amt;

        let maxNoOrder = me.getMaxNoOrder(me.order);
        promotionToApply.definition.noOrder = maxNoOrder + 1;

        if (!promotionToApply.definition.applyNext) {
          OB.Discounts.Pos.removeManualPromotionFromLines(me.order);
        }

        if (
          formElementDiscountsList.requiresQty &&
          !formElementDiscountsList.amt
        ) {
          //Show a modal pop up with the error
          me.doShowPopup({
            popup: 'modalDiscountNeedQty'
          });
          return true;
        }
        _.each(me.checkedLines, function(line) {
          orderLinesCollection.add(line);
        });

        OB.Model.Discounts.addManualPromotion(
          me.order,
          orderLinesCollection,
          promotionToApply
        );

        me.closingDiscounts();
      }
    );
  },
  init: function(model) {
    this.order = model.get('order');
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.Discounts.btnDiscountsApply',
  kind: 'OB.UI.ModalDialogButton',
  classes: 'obObposPointOfSaleUiDiscountsBtnDiscountsApply',
  i18nLabel: 'OBMOBC_LblApply',
  events: {
    onApplyDiscounts: ''
  },
  tap: function() {
    if (this.disabled) {
      return true;
    }
    this.doApplyDiscounts();
  },
  initComponents: function() {
    this.inherited(arguments);
    this.setDisabled(true);
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.Discounts.btnCheckAll',
  kind: 'OB.UI.FormElement.Checkbox',
  classes: 'obObposPointOfSaleUiDiscountsBtnCheckAll',
  events: {
    onCheckAllTicketLines: ''
  },
  checked: false,
  tap: function() {
    this.inherited(arguments);
    this.doCheckAllTicketLines({
      status: this.checked
    });
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.Discounts.btnCheckOverride',
  kind: 'OB.UI.FormElement.Checkbox',
  classes: 'obObposPointOfSaleUiDiscountsBtnCheckOverride',
  checked: false
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.Discounts.btnDiscountsCancel',
  kind: 'OB.UI.ModalDialogButton',
  classes: 'obObposPointOfSaleUiDiscountsBtnDiscountsCancel',
  events: {
    onDiscountsClose: ''
  },
  i18nContent: 'OBMOBC_LblCancel',
  tap: function() {
    this.doDiscountsClose();
  }
});

enyo.kind({
  name: 'OB.UI.DiscountList',
  kind: 'OB.UI.List',
  handlers: {
    onchange: 'changeDiscount'
  },
  classes: 'obUiDiscountList',
  renderEmpty: enyo.Control,
  renderLine: 'OB.UI.DiscountList.Options',
  initComponents: function() {
    this.inherited(arguments);
  },
  changeDiscount: function() {
    var model = this.collection.at(this.getSelected());
    var rule = OB.Model.Discounts.discountRules[model.get('discountType')];
    var amt = 0;
    var requiresQty = !rule.isFixed; // If fixed discount, no requires qty
    var units;
    if (rule.isAmount) {
      amt = model.get('disctTotalamountdisc')
        ? model.get('disctTotalamountdisc')
        : model.get('obdiscAmt');
      units = OB.MobileApp.model.get('terminal').currency$_identifier;
    } else {
      amt = model.get('disctTotalpercdisc')
        ? model.get('disctTotalpercdisc')
        : model.get('obdiscPercentage');
      units = '%';
    }

    this.formElement.owner.discountChanged(
      {},
      {
        originator: this,
        model: model,
        amt: amt,
        requiresQty: requiresQty,
        units: units
      }
    );
  }
});

enyo.kind({
  kind: 'OB.UI.FormElement.Select.Option',
  name: 'OB.UI.DiscountList.Options',
  classses: 'obUiDiscountListOptions',
  initComponents: function() {
    var rule = OB.Model.Discounts.discountRules[this.model.get('discountType')],
      propertyToShow = '';
    if (rule.getAmountProperty && rule.getAmountProperty instanceof Function) {
      propertyToShow = OB.Model.Discounts.discountRules[
        this.model.get('discountType')
      ].getAmountProperty();
    }
    this.setValue(this.model.get('id'));
    this.originalText = this.model.get('_identifier');
    // TODO: this shouldn't be hardcoded but defined in each promotion
    if (
      !OB.Model.Discounts.discountRules[this.model.get('discountType')].isFixed
    ) {
      //variable
      this.requiresQty = true;
      if (
        !OB.Model.Discounts.discountRules[this.model.get('discountType')]
          .isAmount
      ) {
        //variable porcentaje
        this.units = '%';
        if (
          !_.isUndefined(this.model.get(propertyToShow)) &&
          !_.isNull(this.model.get(propertyToShow))
        ) {
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
      if (
        !OB.Model.Discounts.discountRules[this.model.get('discountType')]
          .isAmount
      ) {
        //fixed percentage
        this.units = '%';
        if (
          !_.isUndefined(this.model.get(propertyToShow)) &&
          !_.isNull(this.model.get(propertyToShow))
        ) {
          this.amt = this.model.get(propertyToShow);
        }
      } else {
        //fixed amount
        this.units = OB.MobileApp.model.get('terminal').currency$_identifier;
        if (
          !_.isUndefined(this.model.get(propertyToShow)) &&
          !_.isNull(this.model.get(propertyToShow))
        ) {
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
