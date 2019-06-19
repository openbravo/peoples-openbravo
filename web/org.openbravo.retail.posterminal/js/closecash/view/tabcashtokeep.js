/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.CashToKeepRadioButton',
  kind: 'OB.UI.RadioButton',
  classes: 'obObposCashupUiCashToKeepRadioButton',
  components: [
    {
      name: 'lbl',
      classes: 'obObposCashupUiCashToKeepRadioButton-lbl'
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
  name: 'OB.OBPOSCashUp.UI.KeepDetails',
  classes: 'obObposCashupUiKeepDetails',
  events: {
    onResetQtyToKeep: ''
  },
  init: function(model) {
    this.model = model;
    var me = this;
    this.model.on(
      'change:otherInput',
      function() {
        if (OB.DEC.isNumber(me.model.get('otherInput'))) {
          me.$.variableInput.setContent(OB.DEC.abs(me.model.get('otherInput')));
          me.$.allowvariableamount.setQtyToKeep(
            OB.DEC.abs(me.model.get('otherInput'))
          );
        } else {
          me.$.variableInput.setContent('');
          me.$.allowvariableamount.setQtyToKeep(null);
        }
        me.$.allowvariableamount.tap();
      },
      this
    );
  },
  components: [
    {
      kind: 'Group',
      name: 'RadioGroup',
      classes: 'obObposCashupUiKeepDetails-RadioGroup',
      components: [
        {
          name: 'keepfixedamount',
          classes: 'obObposCashupUiKeepDetails-RadioGroup-keepfixedamount',
          showing: false,
          kind: 'OB.OBPOSCashUp.UI.CashToKeepRadioButton'
        },
        {
          classes: 'obObposCashupUiKeepDetails-RadioGroup-element1'
        },
        {
          name: 'allowmoveeverything',
          classes: 'obObposCashupUiKeepDetails-RadioGroup-allowmoveeverything',
          kind: 'OB.OBPOSCashUp.UI.CashToKeepRadioButton',
          qtyToKeep: 0,
          i18nLabel: 'OBPOS_LblNothing',
          showing: false
        },
        {
          classes: 'obObposCashupUiKeepDetails-RadioGroup-element2'
        },
        {
          name: 'allowdontmove',
          classes: 'obObposCashupUiKeepDetails-RadioGroup-allowdontmove',
          kind: 'OB.OBPOSCashUp.UI.CashToKeepRadioButton',
          showing: false
        },
        {
          classes: 'obObposCashupUiKeepDetails-RadioGroup-element3'
        },
        {
          name: 'allowvariableamount',
          classes: 'obObposCashupUiKeepDetails-RadioGroup-allowvariableamount',
          binded: false,
          kind: 'OB.OBPOSCashUp.UI.CashToKeepRadioButton',
          showing: false,
          qtyToKeep: 0,
          components: [
            {
              classes:
                'obObposCashupUiKeepDetails-RadioGroup-allowvariableamount-container1',
              components: [
                {
                  name: 'variableamount',
                  classes:
                    'obObposCashupUiKeepDetails-RadioGroup-allowvariableamount-container1-variableamount',
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
                    'obObposCashupUiKeepDetails-RadioGroup-allowvariableamount-container1-variableInput',
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
  name: 'OB.OBPOSCashUp.UI.CashToKeep',
  classes: 'obObposCashupUiCashToKeep',
  published: {
    paymentToKeep: null
  },
  components: [
    {
      classes: 'obObposCashupUiCashToKeep-container1',
      components: [
        {
          classes: 'obObposCashupUiCashToKeep-container1-container1',
          components: [
            {
              classes:
                'obObposCashupUiCashToKeep-container1-container1-container1',
              components: [
                {
                  classes:
                    'obObposCashupUiCashToKeep-container1-container1-container1-container1 row-fluid',
                  components: [
                    {
                      classes:
                        'obObposCashupUiCashToKeep-container1-container1-container1-container1-container1 span12',
                      components: [
                        {
                          name: 'cashtokeepheader',
                          classes:
                            'obObposCashupUiCashToKeep-container1-container1-container1-container1-container1-cashtokeepheader',
                          renderHeader: function(value, step, count) {
                            this.setContent(
                              OB.I18N.getLabel('OBPOS_LblStepNumber', [
                                step,
                                count
                              ]) +
                                ' ' +
                                OB.I18N.getLabel('OBPOS_LblStepCashToKeep', [
                                  value
                                ]) +
                                OB.OBPOSCashUp.UI.CashUp.getTitleExtensions()
                            );
                          }
                        }
                      ]
                    }
                  ]
                },
                {
                  kind: 'OB.OBPOSCashUp.UI.KeepDetails',
                  name: 'formkeep',
                  classes:
                    'obObposCashupUiCashToKeep-container1-container1-container1-formkeep',
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
                    var cnted;

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
                    var paymentMethod = modelToDraw.get('paymentMethod');
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
      this.model.stepNumber('OB.CashUp.CashToKeep'),
      this.model.stepCount()
    );
    this.$.formkeep.renderBody(this.paymentToKeep);

    //If fixed quantity to keep is more than counted quantity,
    //counted quantity should be propossed to keep.
    if (this.paymentToKeep.get('paymentMethod').keepfixedamount) {
      this.paymentToKeep.on(
        'change:counted',
        function(mod) {
          this.$.formkeep.renderFixedAmount(this.paymentToKeep);
        },
        this
      );
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
