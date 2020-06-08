/*
 ************************************************************************************
 * Copyright (C) 2012-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

enyo.kind({
  name: 'OB.OBPOSCloseCash.UI.CashToKeepRadioButton',
  kind: 'OB.UI.RadioButton',
  classes: 'obObposCloseCashUiCashToKeepRadioButton',
  components: [
    {
      name: 'lbl',
      classes: 'obObposCloseCashUiCashToKeepRadioButton-lbl'
    }
  ],
  events: {
    onPaymentMethodKept: ''
  },
  tap: function() {
    this.inherited(arguments);
    this.doPaymentMethodKept({
      qtyToKeep: this.qtyToKeep,
      name: this.name
    });
  },
  render: function(content) {
    this.$.lbl.setContent(content);
  },
  setQtyToKeep: function(qty) {
    this.qtyToKeep = qty;
  },
  initComponents: function() {
    this.inherited(arguments);
    if (this.i18nLabel) {
      this.$.lbl.setContent(OB.I18N.getLabel(this.i18nLabel));
      return;
    }
    if (this.label) {
      this.$.lbl.setContent(this.label);
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSCloseCash.UI.KeepDetails',
  classes: 'obObposCloseCashUiKeepDetails',
  events: {
    onResetQtyToKeep: ''
  },
  init: function(model) {
    this.model = model;
    this.model.on('change:otherInput', () => {
      if (OB.DEC.isNumber(this.model.get('otherInput'))) {
        this.$.variableInput.setContent(
          OB.DEC.abs(this.model.get('otherInput'))
        );
        this.$.allowvariableamount.setQtyToKeep(
          OB.DEC.abs(this.model.get('otherInput'))
        );
      } else {
        this.$.variableInput.setContent('');
        this.$.allowvariableamount.setQtyToKeep(null);
      }
      this.$.allowvariableamount.tap();
    });
  },
  components: [
    {
      kind: 'Group',
      name: 'RadioGroup',
      classes: 'obObposCloseCashUiKeepDetails-RadioGroup',
      components: [
        {
          name: 'keepfixedamount',
          classes: 'obObposCloseCashUiKeepDetails-RadioGroup-keepfixedamount',
          showing: false,
          kind: 'OB.OBPOSCloseCash.UI.CashToKeepRadioButton'
        },
        {
          name: 'allowmoveeverything',
          classes:
            'obObposCloseCashUiKeepDetails-RadioGroup-allowmoveeverything',
          kind: 'OB.OBPOSCloseCash.UI.CashToKeepRadioButton',
          qtyToKeep: 0,
          i18nLabel: 'OBPOS_LblNothing',
          showing: false
        },
        {
          name: 'allowdontmove',
          classes: 'obObposCloseCashUiKeepDetails-RadioGroup-allowdontmove',
          kind: 'OB.OBPOSCloseCash.UI.CashToKeepRadioButton',
          showing: false
        },
        {
          name: 'allowvariableamount',
          classes:
            'obObposCloseCashUiKeepDetails-RadioGroup-allowvariableamount',
          binded: false,
          kind: 'OB.OBPOSCloseCash.UI.CashToKeepRadioButton',
          showing: false,
          qtyToKeep: 0,
          components: [
            {
              classes:
                'obObposCloseCashUiKeepDetails-RadioGroup-allowvariableamount-container1',
              components: [
                {
                  name: 'variableamount',
                  classes:
                    'obObposCloseCashUiKeepDetails-RadioGroup-allowvariableamount-container1-variableamount',
                  setAmount: function(amount) {
                    this.setContent(
                      enyo.format(
                        OB.I18N.getLabel('OBPOS_LblOtherMaxAmount'),
                        OB.I18N.formatCurrency(amount)
                      )
                    );
                  },
                  initComponents: function() {
                    this.setAmount(0);
                  }
                },
                {
                  name: 'variableInput',
                  classes:
                    'obObposCloseCashUiKeepDetails-RadioGroup-allowvariableamount-container1-variableInput',
                  content: OB.DEC.Zero
                }
              ]
            }
          ]
        }
      ]
    }
  ]
});

enyo.kind({
  name: 'OB.OBPOSCloseCash.UI.CashToKeep',
  classes: 'obObposCloseCashUiCashToKeep',
  published: {
    paymentToKeep: null
  },
  components: [
    {
      classes: 'obObposCloseCashUiCashToKeep-wrapper',
      components: [
        {
          classes: 'obObposCloseCashUiCashToKeep-wrapper-components',
          components: [
            {
              name: 'cashtokeepheader',
              classes: 'obObposCloseCashUiCashToKeep-wrapper-components-title',
              renderHeader: function(value, step, count) {
                this.setContent(
                  OB.I18N.getLabel('OBPOS_LblStepNumber', [step, count]) +
                    ' ' +
                    OB.I18N.getLabel('OBPOS_LblStepCashToKeep', [value]) +
                    OB.OBPOSCloseCash.UI.CloseCash.getTitleExtensions()
                );
              }
            },
            {
              classes: 'obObposCloseCashUiCashToKeep-wrapper-components-body',
              components: [
                {
                  kind: 'OB.OBPOSCloseCash.UI.KeepDetails',
                  name: 'formkeep',
                  classes:
                    'obObposCloseCashUiCashToKeep-wrapper-components-body-formkeep',
                  handlers: {
                    onChangeOption: 'changeOption'
                  },
                  changeOption: function(inSender, inEvent) {
                    this.$.allowvariableamount.tap();
                  },
                  disableControls: function(modelToDraw) {
                    //remove selected RButtons
                    //reset UI and model.
                    this.$.keepfixedamount.disableRadio();
                    this.$.allowmoveeverything.disableRadio();
                    this.$.allowdontmove.disableRadio();
                    this.$.allowvariableamount.disableRadio();

                    if (modelToDraw && modelToDraw.get('selectedCashToKeep')) {
                      if (
                        modelToDraw.get('selectedCashToKeep') ===
                        'keepfixedamount'
                      ) {
                        this.$.keepfixedamount.tap();
                      } else if (
                        modelToDraw.get('selectedCashToKeep') ===
                        'allowmoveeverything'
                      ) {
                        this.$.allowmoveeverything.tap();
                      } else if (
                        modelToDraw.get('selectedCashToKeep') ===
                        'allowdontmove'
                      ) {
                        this.$.allowdontmove.tap();
                      } else if (
                        modelToDraw.get('selectedCashToKeep') ===
                        'allowvariableamount'
                      ) {
                        this.$.allowvariableamount.tap();
                        this.$.variableInput.setContent(
                          modelToDraw.get('variableAmtToKeep') &&
                            modelToDraw.get('variableAmtToKeep') >= 0
                            ? modelToDraw.get('variableAmtToKeep')
                            : ''
                        );
                      }
                    } else {
                      this.$.variableInput.setContent('');
                    }
                  },
                  renderFixedAmount: function(modelToDraw) {
                    let cnted;

                    if (modelToDraw.get('foreignCounted')) {
                      cnted = modelToDraw.get('foreignCounted');
                    } else {
                      cnted = modelToDraw.get('counted');
                    }
                    this.$.variableamount.setAmount(cnted);
                    this.$.keepfixedamount.setShowing(
                      modelToDraw.get('paymentMethod').keepfixedamount
                    );
                    if (modelToDraw.get('paymentMethod').keepfixedamount) {
                      if (
                        modelToDraw.get('foreignCounted') !== null &&
                        modelToDraw.get('foreignCounted') !== undefined
                      ) {
                        if (cnted < modelToDraw.get('paymentMethod').amount) {
                          this.$.keepfixedamount.render(
                            OB.I18N.formatCurrency(cnted)
                          );
                          this.$.keepfixedamount.setQtyToKeep(cnted);
                        } else {
                          this.$.keepfixedamount.render(
                            OB.I18N.formatCurrency(
                              modelToDraw.get('paymentMethod').amount
                            )
                          );
                          this.$.keepfixedamount.setQtyToKeep(
                            modelToDraw.get('paymentMethod').amount
                          );
                        }
                      } else {
                        this.$.keepfixedamount.render(
                          OB.I18N.formatCurrency(
                            modelToDraw.get('paymentMethod').amount
                          )
                        );
                        this.$.keepfixedamount.setQtyToKeep(
                          modelToDraw.get('paymentMethod').amount
                        );
                      }
                    } else {
                      this.$.keepfixedamount.render('');
                    }
                  },
                  renderBody: function(modelToDraw) {
                    const paymentMethod = modelToDraw.get('paymentMethod');
                    if (!paymentMethod.automatemovementtoother) {
                      return true;
                    }
                    this.disableControls(modelToDraw);
                    //draw
                    this.renderFixedAmount(modelToDraw);

                    this.$.allowmoveeverything.setShowing(
                      paymentMethod.allowmoveeverything
                    );

                    this.$.allowdontmove.setShowing(
                      paymentMethod.allowdontmove
                    );
                    if (paymentMethod.allowdontmove) {
                      this.$.allowdontmove.setQtyToKeep(
                        modelToDraw.get('foreignCounted')
                      );
                      this.$.allowdontmove.render(
                        OB.I18N.getLabel('OBPOS_LblTotalAmount') +
                          ' ' +
                          OB.I18N.formatCurrency(
                            modelToDraw.get('foreignCounted')
                          )
                      );
                    } else {
                      this.$.allowdontmove.render('');
                    }

                    this.$.allowvariableamount.setShowing(
                      paymentMethod.allowvariableamount
                    );
                  }
                }
              ]
            }
          ]
        }
      ]
    }
  ],
  paymentToKeepChanged: function(model) {
    this.$.cashtokeepheader.renderHeader(
      this.paymentToKeep.get('name'),
      this.model.stepNumber('OB.CloseCash.CashToKeep'),
      this.model.stepCount()
    );
    this.$.formkeep.renderBody(this.paymentToKeep);

    //If fixed quantity to keep is more than counted quantity,
    //counted quantity should be propossed to keep.
    if (this.paymentToKeep.get('paymentMethod').keepfixedamount) {
      this.paymentToKeep.on('change:counted', mod => {
        this.$.formkeep.renderFixedAmount(this.paymentToKeep);
      });
    }
  },
  displayStep: function(model) {
    this.model = model;
    // this function is invoked when displayed.
    this.setPaymentToKeep(model.get('paymentList').at(model.get('substep')));
    this.$.formkeep.disableControls(
      model.get('paymentList').at(model.get('substep'))
    );
  }
});
