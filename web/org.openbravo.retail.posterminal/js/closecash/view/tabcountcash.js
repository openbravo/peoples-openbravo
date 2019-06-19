/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo,_ */

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.RenderPaymentsLine',
  classes: 'obObposCashupUiRenderPaymentsLine',
  events: {
    onLineEditCount: ''
  },
  components: [
    {
      classes: 'obObposCashupUiRenderPaymentsLine-container1',
      components: [
        {
          classes:
            'obObposCashupUiRenderPaymentsLine-container1-container1 row-fluid',
          components: [
            {
              classes:
                'obObposCashupUiRenderPaymentsLine-container1-container1-listItem span12',
              name: 'listItem',
              components: [
                {
                  classes:
                    'obObposCashupUiRenderPaymentsLine-container1-container1-listItem-conatiner1',
                  components: [
                    {
                      name: 'name',
                      classes:
                        'obObposCashupUiRenderPaymentsLine-container1-container1-listItem-conatiner1-name'
                    },
                    {
                      name: 'foreignExpected',
                      classes:
                        'obObposCashupUiRenderPaymentsLine-container1-container1-listItem-conatiner1-foreignExpected'
                    },
                    {
                      name: 'expected',
                      classes:
                        'obObposCashupUiRenderPaymentsLine-container1-container1-listItem-conatiner1-expected'
                    }
                  ]
                },
                {
                  classes:
                    'obObposCashupUiRenderPaymentsLine-container1-container1-listItem-conatiner2',
                  components: [
                    {
                      classes:
                        'obObposCashupUiRenderPaymentsLine-container1-container1-listItem-conatiner2-container1',
                      components: [
                        {
                          name: 'buttonEdit',
                          kind: 'OB.UI.SmallButton',
                          classes:
                            'obObposCashupUiRenderPaymentsLine-container1-container1-listItem-conatiner2-container1-buttonEdit',
                          ontap: 'lineEdit'
                        }
                      ]
                    },
                    {
                      classes:
                        'obObposCashupUiRenderPaymentsLine-container1-container1-listItem-conatiner2-container2',
                      components: [
                        {
                          name: 'buttonOk',
                          kind: 'OB.UI.SmallButton',
                          classes:
                            'obObposCashupUiRenderPaymentsLine-container1-container1-listItem-conatiner2-container2-buttonOk',
                          ontap: 'lineOK'
                        }
                      ]
                    },
                    {
                      name: 'foreignCounted',
                      classes:
                        'obObposCashupUiRenderPaymentsLine-container1-container1-listItem-conatiner2-foreignCounted',
                      content: ''
                    },
                    {
                      name: 'counted',
                      classes:
                        'obObposCashupUiRenderPaymentsLine-container1-container1-listItem-conatiner2-counted',
                      showing: false
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
      this.$.listItem.addClass('cashup-first-empty-payment');
    }
    var counted;
    if (OB.MobileApp.model.hasPermission('OBPOS_HideCountInformation', true)) {
      this.$.expected.hide();
      this.$.foreignExpected.hide();
    }
    counted = this.model.get('counted');
    if (counted !== null && counted !== undefined) {
      this.$.counted.setContent(OB.I18N.formatCurrency(OB.DEC.add(0, counted)));
      this.$.counted.show();
      if (this.model.get('rate') && this.model.get('rate') !== 1) {
        this.$.foreignCounted.setContent(
          '(' +
            OB.I18N.formatCurrency(this.model.get('foreignCounted')) +
            ' ' +
            this.model.get('isocode') +
            ')'
        );
      }
      this.$.buttonOk.hide();
    }
    if (OB.MobileApp.model.hasPermission('OBPOS_HideCountInformation', true)) {
      this.$.buttonOk.hide();
    }
    this.$.buttonEdit.setDisabled(
      this.model.get('paymentMethod').iscash &&
        this.model.get('paymentMethod').countcash
    );
  },
  lineEdit: function() {
    this.doLineEditCount();
  },
  lineOK: function(inSender, inEvent) {
    this.model.set('counted', this.model.get('expected'));
    this.model.set('foreignCounted', this.model.get('foreignExpected'));
  }
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.ListPaymentMethods',
  classes: 'obObposCashupUiListPaymentMethods',
  events: {
    onCountAllOK: '',
    onShowPopup: ''
  },
  components: [
    {
      classes: 'obObposCashupUiListPaymentMethods-container1',
      components: [
        {
          classes: 'obObposCashupUiListPaymentMethods-container1-container1',
          components: [
            {
              classes:
                'obObposCashupUiListPaymentMethods-container1-container1-container1',
              components: [
                {
                  classes:
                    'obObposCashupUiListPaymentMethods-container1-container1-container1-container1 row-fluid',
                  components: [
                    {
                      classes:
                        'obObposCashupUiListPaymentMethods-container1-container1-container1-container1-container1 span12',
                      components: [
                        {
                          classes:
                            'obObposCashupUiListPaymentMethods-container1-container1-container1-container1-container1-stepsheader',
                          name: 'stepsheader',
                          renderHeader: function(step, count) {
                            this.setContent(
                              OB.I18N.getLabel('OBPOS_LblStepNumber', [
                                step,
                                count
                              ]) +
                                ' ' +
                                OB.I18N.getLabel(
                                  'OBPOS_LblStepPaymentMethods'
                                ) +
                                OB.OBPOSCashUp.UI.CashUp.getTitleExtensions()
                            );
                          }
                        }
                      ]
                    }
                  ]
                },
                {
                  classes:
                    'obObposCashupUiListPaymentMethods-container1-container1-container1-container2',
                  components: [
                    {
                      classes:
                        'obObposCashupUiListPaymentMethods-container1-container1-container1-container2-container1',
                      components: [
                        {
                          classes:
                            'obObposCashupUiListPaymentMethods-container1-container1-container1-container2-container1-container1 row-fluid',
                          components: [
                            {
                              classes:
                                'obObposCashupUiListPaymentMethods-container1-container1-container1-container2-container1-container1-container1 span12',
                              components: [
                                {
                                  classes:
                                    'obObposCashupUiListPaymentMethods-container1-container1-container1-container2-container1-container1-container1-container1',
                                  components: [
                                    {
                                      classes:
                                        'obObposCashupUiListPaymentMethods-container1-container1-container1-container2-container1-container1-container1-container1-element1',
                                      initComponents: function() {
                                        this.setContent(
                                          OB.I18N.getLabel(
                                            'OBPOS_LblPaymentMethod'
                                          )
                                        );
                                      }
                                    },
                                    {
                                      classes:
                                        'obObposCashupUiListPaymentMethods-container1-container1-container1-container2-container1-container1-container1-container1-element2',
                                      initComponents: function() {
                                        this.setContent(
                                          OB.I18N.getLabel('OBPOS_LblExpected')
                                        );
                                      }
                                    }
                                  ]
                                },
                                {
                                  classes:
                                    'obObposCashupUiListPaymentMethods-container1-container1-container1-container2-container1-container1-container1-container2',
                                  components: [
                                    {
                                      classes:
                                        'obObposCashupUiListPaymentMethods-container1-container1-container1-container2-container1-container1-container1-container2-element1',
                                      initComponents: function() {
                                        this.setContent(
                                          OB.I18N.getLabel('OBPOS_LblCounted')
                                        );
                                      }
                                    }
                                  ]
                                }
                              ]
                            }
                          ]
                        },
                        {
                          classes:
                            'obObposCashupUiListPaymentMethods-container1-container1-container1-container2-container1-container1-container2 row-fluid',
                          components: [
                            {
                              name: 'paymentsList',
                              classes:
                                'obObposCashupUiListPaymentMethods-container1-container1-container1-container2-container1-container1-container2-paymentsList',
                              kind: 'OB.UI.Table',
                              renderLine:
                                'OB.OBPOSCashUp.UI.RenderPaymentsLine',
                              renderEmpty: 'OB.UI.RenderEmpty',
                              listStyle: 'list'
                            }
                          ]
                        },
                        {
                          classes:
                            'obObposCashupUiListPaymentMethods-container1-container1-container1-container2-container1-container1-container3 row-fluid',
                          components: [
                            {
                              classes:
                                'obObposCashupUiListPaymentMethods-container1-container1-container1-container2-container1-container1-container3-container1 span12',
                              components: [
                                {
                                  classes:
                                    'obObposCashupUiListPaymentMethods-container1-container1-container1-container2-container1-container1-container3-container1-container1',
                                  components: [
                                    {
                                      name: 'totalLbl',
                                      classes:
                                        'obObposCashupUiListPaymentMethods-container1-container1-container1-container2-container1-container1-container3-container1-container1-totalLbl',
                                      initComponents: function() {
                                        this.setContent(
                                          OB.I18N.getLabel('OBPOS_ReceiptTotal')
                                        );
                                      }
                                    },
                                    {
                                      classes:
                                        'obObposCashupUiListPaymentMethods-container1-container1-container1-container2-container1-container1-container3-container1-container1-container1',
                                      components: [
                                        {
                                          name: 'total',
                                          classes:
                                            'obObposCashupUiListPaymentMethods-container1-container1-container1-container2-container1-container1-container3-container1-container1-container1-total',
                                          kind: 'OB.OBPOSCashUp.UI.RenderTotal'
                                        }
                                      ]
                                    }
                                  ]
                                },
                                {
                                  components: [
                                    {
                                      classes:
                                        'obObposCashupUiListPaymentMethods-container1-container1-container1-container2-container1-container1-container3-container1-container2',
                                      components: [
                                        {
                                          name: 'difference',
                                          classes:
                                            'obObposCashupUiListPaymentMethods-container1-container1-container1-container2-container1-container1-container3-container1-container2-difference',
                                          kind: 'OB.OBPOSCashUp.UI.RenderTotal'
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
      _.forEach(this.$.paymentsList.$.tbody.children, function(payment) {
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
      _.forEach(this.$.paymentsList.$.tbody.children, function(payment) {
        payment.controls[0].$.expected.show();
        payment.controls[0].$.foreignExpected.show();
        if (payment.controls[0].model.get('paymentMethod').countcash) {
          payment.show();
        }
      });
    }

    this.$.stepsheader.renderHeader(
      model.stepNumber('OB.CashUp.PaymentMethods'),
      model.stepCount()
    );
    // this function is invoked when displayed.
    var opendrawer = model.get('paymentList').any(function(payment) {
      var paymentmethod = payment.get('paymentMethod');
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
      _.each(model.get('paymentEmptyList').models, function(payment) {
        var paymentModel = _.find(model.get('paymentList').models, function(p) {
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
    var totalCashDiff = 0,
      cashDiff = [],
      me = this;
    _.each(model.get('paymentList').models, function(payment) {
      var paymentMethod = payment.get('paymentMethod'),
        difference = OB.DEC.abs(payment.get('difference'));
      if (difference !== 0) {
        totalCashDiff = OB.DEC.add(totalCashDiff, difference);
        var countDiffLimit = paymentMethod.countDiffLimit;
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
    var serverMsg = '',
      approvals = [];
    if (cashDiff.length > 0) {
      // Approval count differences by payment method
      var message = [
        {
          message: OB.I18N.getLabel('OBPOS_approval.cashupdifferences')
        }
      ];
      _.each(cashDiff, function(diff) {
        var msg = OB.I18N.getLabel(
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
      var organizationCountDiffLimit = OB.MobileApp.model.get('terminal')
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
      OB.UTIL.Approval.requestApproval(model, approvals, function(
        approved,
        supervisor,
        approvalType
      ) {
        if (approved) {
          // Get Approval reason
          if (OB.POS.modelterminal.get('approvalReason').length > 0) {
            me.doShowPopup({
              popup: 'OBPOS_modalApprovalReason',
              args: {
                supervisor: supervisor.id,
                message: serverMsg,
                callback: callback
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
      });
    } else {
      callback();
    }
  }
});
