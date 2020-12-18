/*
 ************************************************************************************
 * Copyright (C) 2018-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo */

enyo.kind({
  name: 'OB.UI.DeleteDiscountLine',
  classes: 'obUiDeletediscountLine',
  handlers: {
    onApplyChange: 'applyChange'
  },
  events: {
    onChangeSelected: ''
  },
  applyChange: function(inSender, inEvent) {
    var index = inEvent.promotionLines.indexOf(this.newAttribute);
    if (index !== -1) {
      if (this.$.checkboxButtonDiscount.checked) {
        inEvent.promotionLines[index].deleteDiscount = true;
      } else {
        inEvent.promotionLines[index].deleteDiscount = false;
      }
    }
  },
  components: [
    {
      kind: 'OB.UI.CheckboxButton',
      name: 'checkboxButtonDiscount',
      classes: 'obUiDeletediscountLine-checkboxButtonDiscount span1',
      tap: function() {
        if (this.checked) {
          this.unCheck();
          this.parent.$.discoutLineDisplay.addClass(
            'obUiDeletediscountLine-checkboxButtonDiscount-discoutLineDisplay_checked'
          );
          this.parent.$.price.addClass(
            'obUiDeletediscountLine-checkboxButtonDiscount-price_checked'
          );
        } else {
          this.check();
          this.parent.$.discoutLineDisplay.removeClass(
            'obUiDeletediscountLine-checkboxButtonDiscount-discoutLineDisplay_checked'
          );
          this.parent.$.price.removeClass(
            'obUiDeletediscountLine-checkboxButtonDiscount-price_checked'
          );
        }
        this.owner.doChangeSelected();
      }
    },
    {
      name: 'discoutLineDisplay',
      classes:
        'obUiDeletediscountLine-checkboxButtonDiscount-discoutLineDisplay',
      components: [
        {
          classes: 'obUiDeletediscountLine-discoutLineDisplay-container1 span4',
          components: [
            {
              name: 'discount',
              classes: 'obUiDeletediscountLine-container1-discount'
            },
            {
              name: 'discountedProducts',
              classes: 'obUiDeletediscountLine-container1-discountedProducts'
            }
          ]
        }
      ]
    },
    {
      name: 'price',
      classes: 'obUiDeletediscountLine-checkboxButtonDiscount-price span4'
    },
    {
      classes: 'obUiDeletediscountLine-checkboxButtonDiscount-container1'
    }
  ],
  initComponents: function() {
    this.inherited(arguments);
    this.renderDiscountLines();
  },
  renderDiscountLines: function() {
    var me = this;
    this.$.checkboxButtonDiscount.check();
    this.$.discount.setContent(this.newAttribute.promotionIdentifier);
    this.$.price.setContent(
      OB.I18N.formatCurrency(this.newAttribute.discAmt * -1)
    );

    //for each line in Discount
    _.each(this.newAttribute.appliedLine, function(lineObj) {
      var productDiscAmt = '',
        nameContent = '';
      var productName =
        lineObj.line.get('qty') > 1
          ? '(' + lineObj.line.get('qty') + 'x) '
          : '';
      productName += lineObj.line.get('product').get('_identifier');
      if (me.newAttribute.appliedLine.length > 1) {
        productDiscAmt = lineObj.discAmt * -1;
      }
      if (productDiscAmt !== '') {
        nameContent = '[' + OB.I18N.formatCurrency(productDiscAmt) + ']';
      }
      me.$.discountedProducts.createComponent({
        classes:
          'obUiDeletediscountLine-container1-discountedProducts-container1',
        components: [
          {
            tag: 'li',
            classes:
              'obUiDeletediscountLine-container1-discountedProducts-container1-container1',
            components: [
              {
                tag: 'span',
                classes:
                  'obUiDeletediscountLine-container1-discountedProducts-container1-container1-element1',
                content: productName
              },
              {
                tag: 'span',
                classes:
                  'obUiDeletediscountLine-container1-discountedProducts-container1-container1-element2',
                content: nameContent
              }
            ]
          },
          {
            classes:
              'obUiDeletediscountLine-container1-discountedProducts-container1-container2'
          }
        ]
      });
    });
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.DeleteDiscountDeleteSelected',
  classes: 'obUiDeleteDiscountDeleteSelected',
  events: {
    onApplyChanges: '',
    onCallbackExecutor: ''
  },
  tap: function() {
    if (this.doApplyChanges()) {
      this.doCallbackExecutor();
      this.doHideThisPopup();
    }
  },
  initComponents: function() {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel('OBPOS_LblDeleteSelected'));
  }
});

enyo.kind({
  name: 'OB.UI.ModalDeleteDiscount',
  kind: 'OB.UI.Modal',
  classes: 'obUiModalDeleteDiscount',
  handlers: {
    onApplyChanges: 'applyChanges',
    onCallbackExecutor: 'callbackExecutor',
    onChangeSelected: 'updateTotal'
  },
  body: {
    classes: 'obUiModalDeleteDiscount-body',
    components: [
      {
        kind: 'Scroller',
        classes: 'obUiModalDeleteDiscount-body-Scroller',
        thumb: true,
        components: [
          {
            name: 'attributes',
            classes: 'obUiModalDeleteDiscount-Scroller-attributes'
          }
        ]
      },
      {
        name: 'totalselected',
        classes: 'obUiModalDeleteDiscount-body-totalselected',
        components: [
          {
            tag: 'span',
            name: 'totalselectedLbl',
            classes: 'obUiModalDeleteDiscount-totalselected-totalselectedLbl'
          },
          {
            tag: 'span',
            name: 'totalselectedAmt',
            classes: 'obUiModalDeleteDiscount-totalselected-totalselectedAmt'
          }
        ]
      },
      {
        classes: 'obUiModalDeleteDiscount-body-container1'
      }
    ]
  },
  footer: {
    classes: 'obUiModalDeleteDiscount-footer',
    components: [
      {
        kind: 'OB.UI.DeleteDiscountDeleteSelected',
        classes:
          'obUiModalDeleteDiscount-footer-obUiDeleteDiscountDeleteSelected'
      },
      {
        kind: 'OB.UI.btnModalCancelDelete',
        classes: 'obUiModalDeleteDiscount-footer-obUibtnModalCancelDelete'
      }
    ]
  },
  applyChanges: function(inSender, inEvent) {
    this.waterfall('onApplyChange', {
      promotionLines: this.promotionsList
    });
    return true;
  },
  callbackExecutor: function(inSender, inEvent) {
    const receipt = this.args.receipt;

    for (let i = 0; i < this.promotionsList.length; i++) {
      if (this.promotionsList[i].deleteDiscount) {
        const promotionObj = this.promotionsList[i].promotionObj;
        let selectedLines = [];
        this.args.selectedLines.forEach(selectedLine => {
          selectedLines.push(selectedLine.get('id'));
        });
        if (
          receipt.get('discountsFromUser') &&
          receipt.get('discountsFromUser').manualPromotions
        ) {
          const manualPromotion = receipt
            .get('discountsFromUser')
            .manualPromotions.find(manualPromotion => {
              return (
                promotionObj.ruleId === manualPromotion.ruleId &&
                promotionObj.discountinstance ===
                  manualPromotion.discountinstance
              );
            });
          if (manualPromotion) {
            let linesToApply = [...manualPromotion.linesToApply];
            for (let j = 0; j < selectedLines.length; j++) {
              linesToApply.splice(linesToApply.indexOf(selectedLines[j]), 1);
            }
            if (linesToApply.length === 0) {
              receipt
                .get('discountsFromUser')
                .manualPromotions.splice(
                  receipt
                    .get('discountsFromUser')
                    .manualPromotions.indexOf(manualPromotion),
                  1
                );
            } else {
              manualPromotion.linesToApply = linesToApply;
            }
          }
        }
      }
    }
    if (this.args.context) {
      this.args.context.owner.owner.rearrangeEditButtonBar(
        this.args.selectedLine
      );
    }
    receipt.calculateReceipt();
  },
  updateTotal: function() {
    var totalSelected = 0;
    _.each(this.$.body.$.attributes.$, function(line) {
      if (line.$.checkboxButtonDiscount.checked === true) {
        totalSelected = OB.DEC.add(
          totalSelected,
          parseFloat(
            line.$.price.content.split(OB.Format.defaultGroupingSymbol).join('')
          )
        );
      }
    });
    this.$.body.$.totalselectedAmt.setContent(
      OB.I18N.formatCurrency(totalSelected)
    );
  },
  executeOnShow: function() {
    this.promotionsList = [];
    var me = this,
      i;
    this.$.body.$.attributes.destroyComponents();
    this.$.header.destroyComponents();
    this.setHeader(OB.I18N.getLabel('OBPOS_LblDiscountsDelete'));

    var selectedLinesModel = this.args.selectedLines;
    _.each(selectedLinesModel, function(line) {
      //for Each Line check all Promotions
      _.each(line.get('promotions'), function(linePromotions) {
        //check manual promotions
        if (
          OB.Discounts.Pos.getManualPromotions().includes(
            linePromotions.discountType
          )
        ) {
          //check if receipt discount
          var promotionExists = false,
            i;
          if (me.promotionsList.length > 0) {
            for (i = 0; i < me.promotionsList.length; i++) {
              if (
                me.promotionsList[i].promotionObj.ruleId ===
                  linePromotions.ruleId &&
                me.promotionsList[i].promotionObj.discountinstance ===
                  linePromotions.discountinstance
              ) {
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
              promotionIdentifier:
                linePromotions.identifier || linePromotions.name,
              appliedLine: [
                {
                  line: line,
                  discAmt: linePromotions.amt
                }
              ],
              discAmt: linePromotions.amt
            });
          }
        }
      });
    });
    //add all promotion lines
    for (i = 0; i < this.promotionsList.length; i++) {
      var lineNumber = i + 1;
      this.$.body.$.attributes.createComponent({
        kind: 'OB.UI.DeleteDiscountLine',
        name: 'deleteDiscountLine' + lineNumber,
        classes: 'obUiModalDeleteDiscount-attributes-deleteDiscountLineGeneric',
        newAttribute: this.promotionsList[i],
        args: this.args
      });
    }
    this.$.body.$.attributes.render();
    this.$.header.render();

    //calculate total
    this.updateTotal();
  },
  initComponents: function() {
    this.inherited(arguments);
    this.attributeContainer = this.$.body.$.attributes;
    this.$.body.$.totalselectedLbl.setContent(
      OB.I18N.getLabel('OBPOS_LblTotalSelected')
    );
  }
});
