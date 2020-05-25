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
  name: 'OB.OBPOSCloseCash.UI.RenderPaymentsLine',
  classes:
    'obObposCloseCashUiRenderPaymentsLine obObposCloseCashUiRenderPaymentsLine_notCounted',
  events: {
    onLineEditCount: '',
    onSetPaymentMethodStatus: ''
  },
  components: [
    {
      classes: 'obObposCloseCashUiRenderPaymentsLine-listItem',
      name: 'listItem',
      components: [
        {
          classes:
            'obObposCloseCashUiRenderPaymentsLine-listItem-nameContainer',
          components: [
            {
              name: 'name',
              classes:
                'obObposCloseCashUiRenderPaymentsLine-listItem-nameContainer-name'
            }
          ]
        },
        {
          classes:
            'obObposCloseCashUiRenderPaymentsLine-listItem-expectedContainer',
          components: [
            {
              name: 'expected',
              classes:
                'obObposCloseCashUiRenderPaymentsLine-listItem-expectedContainer-expected'
            },
            {
              name: 'foreignExpected',
              classes:
                'obObposCloseCashUiRenderPaymentsLine-listItem-expectedContainer-foreignExpected'
            }
          ]
        },
        {
          classes:
            'obObposCloseCashUiRenderPaymentsLine-listItem-countedContainer',
          components: [
            {
              name: 'buttonOk',
              kind: 'OB.UI.Button',
              classes:
                'obObposCloseCashUiRenderPaymentsLine-listItem-countedContainer-buttonOk',
              ontap: 'lineOK'
            },
            {
              name: 'buttonEdit',
              kind: 'OB.UI.BaseButton',
              classes:
                'obObposCloseCashUiRenderPaymentsLine-listItem-countedContainer-buttonEdit',
              ontap: 'lineEdit',
              components: [
                {
                  classes:
                    'obObposCloseCashUiRenderPaymentsLine-listItem-countedContainer-buttonEdit-components',
                  components: [
                    {
                      name: 'counted',
                      classes:
                        'obObposCloseCashUiRenderPaymentsLine-listItem-countedContainer-buttonEdit-components-counted',
                      content: '-'
                    },
                    {
                      name: 'foreignCounted',
                      classes:
                        'obObposCloseCashUiRenderPaymentsLine-listItem-countedContainer-buttonEdit-components-foreignCounted',
                      content: ''
                    }
                  ]
                }
              ]
            }
          ]
        }
      ]
    }
  ],
  create: function() {
    this.inherited(arguments);
    this.$.name.setContent(this.model.get('name'));
    this.$.counted.setContent(OB.I18N.getLabel('OBMOBC_Character')[3]);
    if (this.model.get('rate') && this.model.get('rate') !== 1) {
      this.$.foreignExpected.setContent(
        '(' +
          OB.I18N.formatCurrency(this.model.get('foreignExpected')) +
          ' ' +
          this.model.get('isocode') +
          ')'
      );
      this.$.foreignExpected.show();
    }
    this.$.expected.setContent(
      OB.I18N.formatCurrency(OB.DEC.add(0, this.model.get('expected')))
    );
  },
  render: function() {
    this.inherited(arguments);
    if (this.model.get('firstEmptyPayment')) {
      this.$.listItem.addClass('closeCash-first-empty-payment');
    }
    if (OB.MobileApp.model.hasPermission('OBPOS_HideCountInformation', true)) {
      this.$.expected.hide();
      this.$.foreignExpected.hide();
    }
    let counted = this.model.get('counted');
    if (counted !== null && counted !== undefined) {
      this.$.counted.setContent(OB.I18N.formatCurrency(OB.DEC.add(0, counted)));
      if (this.model.get('rate') && this.model.get('rate') !== 1) {
        this.$.foreignCounted.setContent(
          '(' +
            OB.I18N.formatCurrency(this.model.get('foreignCounted')) +
            ' ' +
            this.model.get('isocode') +
            ')'
        );
      }
      this.addClass('obObposCloseCashUiRenderPaymentsLine_alreadyCounted');
    }
    if (OB.MobileApp.model.hasPermission('OBPOS_HideCountInformation', true)) {
      this.addClass('obObposCloseCashUiRenderPaymentsLine_alreadyCounted');
    }
    this.$.buttonEdit.setDisabled(
      this.model.get('paymentMethod').iscash &&
        this.model.get('paymentMethod').countcash
    );
  },
  lineEdit: function() {
    this.doLineEditCount();
    this.doSetPaymentMethodStatus();
  },
  lineOK: function(inSender, inEvent) {
    this.model.set('counted', this.model.get('expected'));
    this.model.set('foreignCounted', this.model.get('foreignExpected'));
  }
});

enyo.kind({
  name: 'OB.OBPOSCloseCash.UI.ListPaymentMethods',
  classes: 'obObposCloseCashUiListPaymentMethods',
  handlers: {
    onSetPaymentMethodStatus: 'setPaymentMethodStatus'
  },
  events: {
    onCountAllOK: '',
    onShowPopup: ''
  },
  components: [
    {
      classes: 'obObposCloseCashUiListPaymentMethods-wrapper',
      components: [
        {
          classes: 'obObposCloseCashUiListPaymentMethods-wrapper-components',
          components: [
            {
              classes:
                'obObposCloseCashUiListPaymentMethods-wrapper-components-title',
              name: 'stepsheader',
              renderHeader: function(step, count) {
                this.setContent(
                  OB.I18N.getLabel('OBPOS_LblStepNumber', [step, count]) +
                    ' ' +
                    OB.I18N.getLabel('OBPOS_LblStepPaymentMethods') +
                    OB.OBPOSCloseCash.UI.CloseCash.getTitleExtensions()
                );
              }
            },
            {
              classes:
                'obObposCloseCashUiListPaymentMethods-wrapper-components-body',
              components: [
                {
                  classes:
                    'obObposCloseCashUiListPaymentMethods-wrapper-components-body-header',
                  components: [
                    {
                      classes:
                        'obObposCloseCashUiListPaymentMethods-wrapper-components-body-header-element1',
                      initComponents: function() {
                        this.setContent(
                          OB.I18N.getLabel('OBPOS_LblPaymentMethod')
                        );
                      }
                    },
                    {
                      classes:
                        'obObposCloseCashUiListPaymentMethods-wrapper-components-body-header-element2',
                      initComponents: function() {
                        this.setContent(OB.I18N.getLabel('OBPOS_LblExpected'));
                      }
                    },
                    {
                      classes:
                        'obObposCloseCashUiListPaymentMethods-wrapper-components-body-header-element3',
                      initComponents: function() {
                        this.setContent(OB.I18N.getLabel('OBPOS_LblCounted'));
                      }
                    }
                  ]
                },
                {
                  classes:
                    'obObposCloseCashUiListPaymentMethods-wrapper-components-body-list',
                  components: [
                    {
                      name: 'paymentsList',
                      classes:
                        'obObposCloseCashUiListPaymentMethods-wrapper-components-body-list-paymentsList',
                      kind: 'OB.UI.Table',
                      renderLine: 'OB.OBPOSCloseCash.UI.RenderPaymentsLine',
                      renderEmpty: 'OB.UI.RenderEmpty',
                      listStyle: 'list'
                    }
                  ]
                },
                {
                  classes:
                    'obObposCloseCashUiListPaymentMethods-wrapper-components-body-footer',
                  components: [
                    {
                      classes:
                        'obObposCloseCashUiListPaymentMethods-wrapper-components-body-footer-container1',
                      components: [
                        {
                          name: 'totalLbl',
                          classes:
                            'obObposCloseCashUiListPaymentMethods-wrapper-components-body-footer-container1-totalLbl',
                          initComponents: function() {
                            this.setContent(
                              OB.I18N.getLabel('OBPOS_LblExpected')
                            );
                          }
                        },
                        {
                          name: 'total',
                          classes:
                            'obObposCloseCashUiListPaymentMethods-wrapper-components-body-footer-container1-total',
                          kind: 'OB.OBPOSCloseCash.UI.RenderTotal'
                        }
                      ]
                    },
                    {
                      classes:
                        'obObposCloseCashUiListPaymentMethods-wrapper-components-body-footer-container2',
                      components: [
                        {
                          name: 'countedLbl',
                          classes:
                            'obObposCloseCashUiListPaymentMethods-wrapper-components-body-footer-container2-countedLbl',
                          initComponents: function() {
                            this.setContent(OB.I18N.getLabel('OBPOS_Counted'));
                          }
                        },
                        {
                          name: 'counted',
                          classes:
                            'obObposCloseCashUiListPaymentMethods-wrapper-components-body-footer-container2-counted',
                          kind: 'OB.OBPOSCloseCash.UI.RenderTotal'
                        }
                      ]
                    },
                    {
                      classes:
                        'obObposCloseCashUiListPaymentMethods-wrapper-components-body-footer-container4'
                    },
                    {
                      classes:
                        'obObposCloseCashUiListPaymentMethods-wrapper-components-body-footer-container3',
                      components: [
                        {
                          name: 'differenceLbl',
                          classes:
                            'obObposCloseCashUiListPaymentMethods-wrapper-components-body-footer-container3-differenceLbl',
                          initComponents: function() {
                            this.setContent(
                              OB.I18N.getLabel('OBPOS_Remaining')
                            );
                          }
                        },
                        {
                          name: 'difference',
                          classes:
                            'obObposCloseCashUiListPaymentMethods-wrapper-components-body-footer-container3-difference',
                          kind: 'OB.OBPOSCloseCash.UI.RenderTotal'
                        }
                      ]
                    }
                  ]
                }
              ]
            }
          ]
        }
      ]
    }
  ],
  setCollection: function(col) {
    this.$.paymentsList.setCollection(col);
  },
  displayStep: function(model) {
    // If the cashier is not trusty, hide expected and total amount that should be.
    if (OB.MobileApp.model.hasPermission('OBPOS_HideCountInformation', true)) {
      this.$.total.hide();
      this.$.totalLbl.hide();
      this.$.difference.hide();
      this.$.paymentsList.$.tbody.children.forEach(payment => {
        payment.controls[0].$.expected.hide();
        payment.controls[0].$.foreignExpected.hide();
        if (payment.controls[0].model.get('paymentMethod').countcash) {
          payment.hide();
        }
      });
    } else {
      this.$.total.show();
      this.$.totalLbl.show();
      this.$.difference.show();
      this.$.paymentsList.$.tbody.children.forEach(payment => {
        payment.controls[0].$.expected.show();
        payment.controls[0].$.foreignExpected.show();
        if (payment.controls[0].model.get('paymentMethod').countcash) {
          payment.show();
        }
      });
    }

    this.$.stepsheader.renderHeader(
      model.stepNumber('OB.CloseCash.PaymentMethods'),
      model.stepCount()
    );
    // this function is invoked when displayed.
    const opendrawer = model.get('paymentList').any(payment => {
      const paymentmethod = payment.get('paymentMethod');
      return (
        paymentmethod.iscash &&
        !paymentmethod.countcash &&
        paymentmethod.allowopendrawer
      );
    });

    if (opendrawer) {
      OB.POS.hwserver.openDrawer(
        false,
        OB.MobileApp.model.get('permissions').OBPOS_timeAllowedDrawerCount
      );
    }
    this.initPaymentMethod();
  },
  setPaymentMethodStatus: function(inSender, inEvent) {
    // reset previous status
    if (this.originator && this.originator.$.buttonEdit) {
      this.originator.$.buttonEdit.removeClass(
        'obObposCashupUiRenderPaymentsLine-listItem-countedContainer-buttonEdit_selected'
      );
    }
    // set new status
    if (
      inEvent &&
      inEvent.originator &&
      inEvent.originator !== this.originator
    ) {
      this.originator = inEvent.originator;
      this.originator.$.buttonEdit.addClass(
        'obObposCashupUiRenderPaymentsLine-listItem-countedContainer-buttonEdit_selected'
      );
    } else {
      this.originator = null;
    }
  },
  initPaymentMethod: function() {
    this.setPaymentMethodStatus(null);
  },
  verifyStep: function(model, callback) {
    this.model = model;
    // this function is invoked when going next, invokes callback to continue
    // do not invoke callback to cancel going next.
    if (
      OB.MobileApp.model.hasPermission(
        'OBPOS_retail.cashupGroupExpectedPayment',
        true
      )
    ) {
      // Auto confirm empty payment methods
      model.get('paymentEmptyList').models.forEach(payment => {
        const paymentModel = model.get('paymentList').models.find(p => {
          return p.id === payment.id;
        });
        if (
          paymentModel &&
          OB.UTIL.isNullOrUndefined(paymentModel.get('counted'))
        ) {
          paymentModel.set('counted', paymentModel.get('expected'));
          paymentModel.set(
            'foreignCounted',
            paymentModel.get('foreignExpected')
          );
        }
      });
    }
    let totalCashDiff = 0,
      cashDiff = [];
    model.get('paymentList').models.forEach(payment => {
      const paymentMethod = payment.get('paymentMethod'),
        difference = OB.DEC.abs(payment.get('difference'));
      if (difference !== 0) {
        totalCashDiff = OB.DEC.add(totalCashDiff, difference);
        const countDiffLimit = paymentMethod.countDiffLimit;
        if (
          !OB.UTIL.isNullOrUndefined(countDiffLimit) &&
          difference >= countDiffLimit
        ) {
          cashDiff.push({
            _identifier: paymentMethod._identifier,
            searchKey: paymentMethod.searchKey,
            difference: difference,
            countDiffLimit: countDiffLimit
          });
        }
      }
    });
    let serverMsg = '',
      approvals = [];
    if (cashDiff.length > 0) {
      // Approval count differences by payment method
      const message = [
        {
          message: OB.I18N.getLabel('OBPOS_approval.cashupdifferences')
        }
      ];
      cashDiff.forEach(diff => {
        const msg = OB.I18N.getLabel(
          'OBPOS_approval.paymentmethod.countdifferences',
          [
            diff._identifier,
            OB.I18N.formatCurrency(diff.difference),
            OB.I18N.formatCurrency(diff.countDiffLimit)
          ]
        );
        message.push({
          message: msg,
          padding: '1em',
          fontSize: '16px'
        });
        serverMsg += msg + '\r\n';
      });
      approvals.push({
        approval: 'OBPOS_approval.cashupdifferences',
        message: message
      });
      serverMsg =
        OB.I18N.getLabel('OBPOS_approval.cashupdifferences') +
        '\r\n' +
        serverMsg;
    } else {
      const organizationCountDiffLimit = OB.MobileApp.model.get('terminal')
        .organizationCountDiffLimit;
      if (
        !OB.UTIL.isNullOrUndefined(organizationCountDiffLimit) &&
        totalCashDiff >= organizationCountDiffLimit
      ) {
        approvals.push({
          approval: 'OBPOS_approval.cashupdifferences',
          message: 'OBPOS_approval.global.countdifferences',
          params: [
            OB.I18N.formatCurrency(totalCashDiff),
            OB.I18N.formatCurrency(organizationCountDiffLimit)
          ]
        });
        serverMsg = OB.I18N.getLabel('OBPOS_approval.global.countdifferences', [
          OB.I18N.formatCurrency(totalCashDiff),
          OB.I18N.formatCurrency(organizationCountDiffLimit)
        ]);
      }
    }
    if (approvals.length > 0) {
      OB.UTIL.Approval.requestApproval(
        model,
        approvals,
        (approved, supervisor, approvalType) => {
          if (approved) {
            // Get Approval reason
            if (OB.POS.modelterminal.get('approvalReason').length > 0) {
              this.doShowPopup({
                popup: 'OBPOS_modalApprovalReason',
                args: {
                  supervisor: supervisor.id,
                  message: serverMsg,
                  callback: approved => {
                    if (approved) {
                      callback();
                    }
                  }
                }
              });
            } else {
              model.set('approvals', {
                supervisor: supervisor.id,
                message: serverMsg
              });
              callback();
            }
          }
        }
      );
    } else {
      callback();
    }
  }
});
