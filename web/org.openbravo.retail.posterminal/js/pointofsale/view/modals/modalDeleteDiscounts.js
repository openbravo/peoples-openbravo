/*
 ************************************************************************************
 * Copyright (C) 2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, _ */

enyo.kind({
  name: 'OB.UI.DeleteDiscountLine',
  style: 'border-bottom: 1px solid #cccccc; text-align: center; color: black;',
  handlers: {
    onApplyChange: 'applyChange'
  },
  events: {
    onChangeSelected: ''
  },
  applyChange: function (inSender, inEvent) {
    var index = inEvent.promotionLines.indexOf(this.newAttribute);
    if (index !== -1) {
      if (this.$.checkboxButtonDiscount.checked) {
        inEvent.promotionLines[index].deleteDiscount = true;
      } else {
        inEvent.promotionLines[index].deleteDiscount = false;
      }
    }
  },
  components: [{
    kind: 'OB.UI.CheckboxButton',
    name: 'checkboxButtonDiscount',
    classes: 'modal-dialog-btn-check span1',
    style: 'width: 8%;',
    tap: function () {
      if (this.checked) {
        this.unCheck();
        this.parent.$.discoutLineDisplay.addStyles('opacity:.6');
        this.parent.$.price.addStyles('opacity:.6');
      } else {
        this.check();
        this.parent.$.discoutLineDisplay.addStyles('opacity:1');
        this.parent.$.price.addStyles('opacity:1');
      }
      this.owner.doChangeSelected();
    }
  }, {
    name: 'discoutLineDisplay',
    components: [{
      classes: 'span4',
      style: 'line-height: 30px; font-size: 16px; width:70%; text-align: left',
      components: [{
        name: 'discount'
      }, {
        name: 'discountedProducts',
        style: 'padding-left: 20px;'
      }]
    }]
  }, {
    name: 'price',
    classes: 'span4',
    style: 'line-height: 30px; font-size: 16px; width: 18%; text-align: right'
  }, {
    style: 'clear: both;'
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.renderDiscountLines();
  },
  renderDiscountLines: function () {
    var me = this;
    this.$.checkboxButtonDiscount.check();
    this.$.discount.setContent(this.newAttribute.promotionIdentifier);
    this.$.price.setContent(OB.I18N.formatCurrency(this.newAttribute.discAmt * (-1)));

    //for each line in Discount
    _.each(this.newAttribute.appliedLine, function (lineObj) {
      var productDiscAmt = "",
          nameContent = "";
      var productName = (lineObj.line.get('qty') > 1 ? ("(" + lineObj.line.get('qty') + "x) ") : "");
      productName += lineObj.line.get('product').get('_identifier');
      if (me.newAttribute.appliedLine.length > 1) {
        productDiscAmt = lineObj.discAmt * (-1);
      }
      if (productDiscAmt !== "") {
        nameContent = '[' + OB.I18N.formatCurrency(productDiscAmt) + ']';
      }
      me.$.discountedProducts.createComponent({
        components: [{
          tag: 'li',
          components: [{
            tag: 'span',
            content: productName
          }, {
            tag: 'span',
            style: 'color: #999999; padding-left:10px',
            content: nameContent
          }]
        }, {
          style: 'clear: both;'
        }]
      });
    });
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.DeleteDiscountDeleteSelected',
  events: {
    onApplyChanges: '',
    onCallbackExecutor: ''
  },
  tap: function () {
    if (this.doApplyChanges()) {
      this.doCallbackExecutor();
      this.doHideThisPopup();
    }
  },
  initComponents: function () {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel('OBPOS_LblDeleteSelected'));
  }
});

enyo.kind({
  name: 'OB.UI.ModalDeleteDiscount',
  kind: 'OB.UI.ModalAction',
  classes: 'modal-dialog',
  handlers: {
    onApplyChanges: 'applyChanges',
    onCallbackExecutor: 'callbackExecutor',
    onChangeSelected: 'updateTotal'
  },
  bodyContent: {
    components: [{
      kind: 'Scroller',
      maxHeight: '225px',
      style: 'background-color: #ffffff;',
      thumb: true,
      horizontal: 'hidden',
      components: [{
        name: 'attributes'
      }]
    }, {
      name: 'totalselected',
      style: 'font-size: 16px; float: right; height: 35px; background-color: #ffffff; width: 45%;',
      components: [{
        tag: 'span',
        name: 'totalselectedLbl',
        style: 'color: #000000; text-align: left; line-height: 35px;'
      }, {
        tag: 'span',
        name: 'totalselectedAmt',
        style: 'color: #000000; float: right; line-height: 35px; width: 35%; font-weight: bold;'
      }]
    }, {
      style: 'clear: both;'
    }]
  },
  bodyButtons: {
    components: [{
      kind: 'OB.UI.DeleteDiscountDeleteSelected'
    }, {
      kind: 'OB.UI.btnModalCancelDelete'
    }]
  },
  applyChanges: function (inSender, inEvent) {
    this.waterfall('onApplyChange', {
      promotionLines: this.promotionsList
    });
    return true;
  },
  callbackExecutor: function (inSender, inEvent) {
    var receipt = this.args.receipt,
        linePromotions, selectedLines = this.args.selectedLines,
        manualPromotions = receipt.get('orderManualPromotions'),
        i, j, k;

    for (i = 0; i < this.promotionsList.length; i++) {
      if (this.promotionsList[i].deleteDiscount) {
        for (j = 0; j < selectedLines.length; j++) {
          linePromotions = selectedLines[j].get('promotions');
          for (k = 0; k < linePromotions.length; k++) {
            if (linePromotions[k].ruleId === this.promotionsList[i].promotionObj.ruleId) {
              linePromotions.splice(k, 1);
              break;
            }
          }
        }
      }
    }
    receipt.calculateReceipt();
  },
  updateTotal: function () {
    var totalSelected = 0;
    _.each(this.$.bodyContent.$.attributes.$, function (line) {
      if (line.$.checkboxButtonDiscount.checked === true) {
        totalSelected = OB.DEC.add(totalSelected, line.$.price.content);
      }
    });
    this.$.bodyContent.$.totalselectedAmt.setContent(OB.I18N.formatCurrency(totalSelected));
  },
  executeOnShow: function () {
    this.promotionsList = [];
    var me = this,
        totalAmount = 0,
        i;
    this.$.bodyContent.$.attributes.destroyComponents();
    this.$.header.destroyComponents();
    this.$.header.setContent(OB.I18N.getLabel('OBPOS_LblDiscountsDelete'));

    var selectedLinesModel = this.args.selectedLines,
        manualPromotions = OB.Model.Discounts.getManualPromotions();
    _.each(selectedLinesModel, function (line) {
      //for Each Line check all Promotions
      _.each(line.get('promotions'), function (linePromotions) {
        //check manual promotions
        if (manualPromotions.indexOf(linePromotions.discountType) !== -1) {
          //check if receipt discount
          var promotionExists = false,
              i;
          if (me.promotionsList.length > 0) {
            for (i = 0; i < me.promotionsList.length; i++) {
              if (me.promotionsList[i].promotionObj.ruleId === linePromotions.ruleId) {
                //rule already exists, then take existing promotion and add amount
                me.promotionsList[i].discAmt += linePromotions.amt;
                me.promotionsList[i].appliedLine.push({
                  line: line,
                  discAmt: linePromotions.amt
                });
                promotionExists = true;
                break;
              }
            }
          }
          if (me.promotionsList.length === 0 || !promotionExists) {
            me.promotionsList.push({
              promotionObj: linePromotions,
              promotionIdentifier: linePromotions.identifier || linePromotions.name,
              appliedLine: [{
                line: line,
                discAmt: linePromotions.amt
              }],
              discAmt: linePromotions.amt
            });
          }
        }
      });
    });
    //add all promotion lines      
    for (i = 0; i < this.promotionsList.length; i++) {
      this.$.bodyContent.$.attributes.createComponent({
        kind: 'OB.UI.DeleteDiscountLine',
        newAttribute: this.promotionsList[i],
        args: this.args
      });
    }
    this.$.bodyContent.$.attributes.render();
    this.$.header.render();

    //calculate total
    this.updateTotal();
  },
  initComponents: function () {
    this.inherited(arguments);
    this.attributeContainer = this.$.bodyContent.$.attributes;
    this.$.bodyContent.$.totalselectedLbl.setContent(OB.I18N.getLabel('OBPOS_LblTotalSelected'));
  }
});