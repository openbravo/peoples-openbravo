/*
 ************************************************************************************
 * Copyright (C) 2018-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo */

enyo.kind({
  name: 'OB.UI.ModalProviderGroup',
  kind: 'OB.UI.Modal',
  classes: 'obUiModalProviderGroup',
  header: '',
  autoDismiss: false,
  hideCloseButton: true,
  showPopupMessage: true,
  events: {
    onHideThisPopup: ''
  },
  body: {
    classes: 'obUiModalProviderGroup-body',
    components: [
      {
        classes: 'obUiModalProviderGroup-body-container1',
        components: [
          {
            classes:
              'obUiModalProviderGroup-body-container1-container1 row-fluid',
            components: [
              {
                name: 'lblType',
                classes:
                  'obUiModalProviderGroup-body-container1-container1-lblType'
              },
              {
                name: 'paymenttype',
                classes:
                  'obUiModalProviderGroup-body-container1-container1-paymenttype'
              }
            ]
          },
          {
            classes: 'obUiModalProviderGroup-body-container1-container2'
          },
          {
            classes:
              'obUiModalProviderGroup-body-container1-container3 row-fluid',
            components: [
              {
                name: 'description',
                classes:
                  'obUiModalProviderGroup-body-container1-container3-description'
              }
            ]
          },
          {
            classes: 'obUiModalProviderGroup-body-container1-container4'
          }
        ]
      },
      {
        classes: 'obUiModalProviderGroup-body-providergroupcomponent',
        name: 'providergroupcomponent'
      }
    ]
  },
  initComponents: function() {
    this.inherited(arguments);
  },
  createProvider: function(providername, refund) {
    if (refund) {
      return (
        enyo.createFromKind(providername + 'Refund') ||
        enyo.createFromKind(providername)
      );
    } else {
      return enyo.createFromKind(providername);
    }
  },
  executeOnShow: function() {
    var amount = this.args.amount;
    var refund = this.args.refund;
    var providerGroup = this.args.providerGroup;

    this.setHeader(
      refund
        ? OB.I18N.getLabel('OBPOS_LblModalReturn', [
            OB.I18N.formatCurrency(amount)
          ])
        : OB.I18N.getLabel('OBPOS_LblModalPayment', [
            OB.I18N.formatCurrency(amount)
          ])
    );
    this.$.body.$.lblType.setContent(OB.I18N.getLabel('OBPOS_LblModalType'));
    this.$.body.$.paymenttype.setContent(providerGroup.provider._identifier);
    this.$.body.$.description.setContent(providerGroup.provider.description);
    this.showPopupMessage = true;

    // Set timeout needed because on ExecuteOnShow
    setTimeout(this.startPaymentRefund.bind(this), 0);
  },
  showMessageAndClose: function(message) {
    window.setTimeout(this.doHideThisPopup.bind(this), 0);
    if (this.showPopupMessage) {
      OB.UTIL.showConfirmation.display(
        OB.I18N.getLabel('OBPOS_LblPaymentMethod'),
        message,
        [
          {
            label: OB.I18N.getLabel('OBMOBC_LblOk'),
            isConfirmButton: true
          }
        ],
        {
          autoDismiss: false
        }
      );
    }
  },
  startPaymentRefund: function() {
    var receipt = this.args.receipt;
    var amount = this.args.amount;
    var refund = this.args.refund;
    var currency = this.args.currency;
    var providerGroup = this.args.providerGroup;
    var providerinstance = this.createProvider(
      this.args.providername,
      this.args.refund
    );
    var attributes = this.args.attributes;
    var i;

    this.$.body.$.providergroupcomponent.destroyComponents();
    if (providerinstance.providerComponent) {
      this.$.body.$.providergroupcomponent
        .createComponent(providerinstance.providerComponent)
        .render();
    }

    if (providerinstance.checkOverpayment && !refund) {
      // check over payments in all payments of the group
      for (i = 0; i < providerGroup._payments.length; i++) {
        let payment = providerGroup._payments[i];
        let paymentlimit;
        if (payment.paymentMethod.allowoverpayment) {
          if (_.isNumber(payment.paymentMethod.overpaymentLimit)) {
            paymentlimit = payment.paymentMethod.overpaymentLimit;
          } else {
            paymentlimit = Number.MAX_VALUE;
          }
        } else {
          paymentlimit = 0;
        }

        if (
          receipt &&
          amount > receipt.get('gross') + paymentlimit - receipt.get('payment')
        ) {
          this.showMessageAndClose(
            OB.I18N.getLabel(
              paymentlimit === 0
                ? 'OBPOS_OverpaymentNotAvailable'
                : 'OBPOS_OverpaymentExcededLimit'
            )
          );
          return;
        }
      }
    }

    providerinstance
      .processPayment({
        receipt: receipt,
        currency: currency,
        amount: amount,
        refund: refund,
        providerGroup: providerGroup
      })
      .then(response => {
        OB.info(
          `ModalProviderGroup: processPayment response: ${JSON.stringify(
            response
          )}`
        );

        const properties = response.properties || {};
        const processedAmount =
          response.processedAmount || properties.processedAmount
            ? response.processedAmount || properties.processedAmount
            : amount;
        const addResponseToPayment = payment => {
          // We found the payment method that applies.
          const paymentline = {
            kind: payment.payment.searchKey,
            name: payment.payment._identifier,
            amount: processedAmount,
            rate: payment.rate,
            mulrate: payment.mulrate,
            isocode: payment.isocode,
            allowOpenDrawer: payment.paymentMethod.allowopendrawer,
            isCash: payment.paymentMethod.iscash,
            openDrawer: payment.paymentMethod.openDrawer,
            printtwice: payment.paymentMethod.printtwice,
            paymentData: {
              provider: providerGroup.provider,
              voidConfirmation: false,
              properties: properties,
              // Is the void provider in charge of defining confirmation.
              // Connector Payment common properties
              receiptno: response.receiptno,
              transaction: response.transaction,
              authorization: response.authorization,
              timestamp: response.timestamp,
              processedAmount: response.processedAmount,
              cardlogo: response.cardlogo,
              voidAction: response.voidAction,
              data: response.data
            }
          };
          OB.info(
            `ModalProviderGroup: addPayment ${JSON.stringify(paymentline)}`
          );
          receipt.addPayment(
            new OB.Model.PaymentLine(Object.assign(paymentline, attributes))
          );
          window.setTimeout(this.doHideThisPopup.bind(this), 0);
        };
        this.showPopupMessage =
          response.showPopupMessage === false ? false : true;

        // First attempt. Find an exact match.
        const cardlogo = response.cardlogo || properties.cardlogo;
        let undefinedPayment = null;
        for (i = 0; i < providerGroup._payments.length; i++) {
          const payment = providerGroup._payments[i];
          if (cardlogo === payment.paymentType.searchKey) {
            addResponseToPayment(payment);
            return; // Success
          } else if (
            'SEARCHKEY' === payment.paymentType.searchKey &&
            cardlogo === payment.paymentMethod.searchKey
          ) {
            // Payment Connector by SEARCHKEY
            addResponseToPayment(payment);
            return; // Success
          } else if ('UNDEFINED' === payment.paymentType.searchKey) {
            undefinedPayment = payment;
          }
        }

        // Second attempt. Find UNDEFINED paymenttype.
        if (undefinedPayment) {
          addResponseToPayment(undefinedPayment);
          return; // Success
        }

        // Fail. Cannot find payment to assign response
        OB.warn(`ModalProviderGroup: Cannot find payment method: ${cardlogo}`);

        this.showMessageAndClose(
          OB.I18N.getLabel('OBPOS_CannotFindPaymentMethod')
        );
      })
      .catch(exception => {
        OB.warn(`ModalProviderGroup: exception thrown: ${exception}`);
        this.showPopupMessage =
          exception.showPopupMessage === false ? false : true;
        this.showMessageAndClose(
          providerinstance.getErrorMessage
            ? providerinstance.getErrorMessage(exception)
            : exception.message
        );
      });
  }
});
