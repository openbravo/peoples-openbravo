/*
 ************************************************************************************
 * Copyright (C) 2018-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo*/

enyo.kind({
  name: 'OB.UI.ModalProviderGroupVoid',
  kind: 'OB.UI.Modal',
  header: '',
  autoDismiss: false,
  hideCloseButton: true,
  showPopupMessage: true,
  classes: 'obUiModalProviderGroupVoid',
  events: {
    onHideThisPopup: ''
  },
  body: {
    classes: 'obUiModalProviderGroupVoid-body',
    components: [
      {
        classes: 'obUiModalProviderGroupVoid-body-container1',
        components: [
          {
            classes:
              'obUiModalProviderGroupVoid-body-container1-container1 row-fluid',
            components: [
              {
                classes:
                  'obUiModalProviderGroupVoid-body-container1-container1-lblType',
                name: 'lblType'
              },
              {
                classes:
                  'obUiModalProviderGroupVoid-body-container1-container1-paymenttype',
                name: 'paymenttype'
              }
            ]
          },
          {
            classes: 'obUiModalProviderGroupVoid-body-container1-container2'
          },
          {
            classes:
              'obUiModalProviderGroupVoid-body-container1-container3 row-fluid',
            components: [
              {
                classes:
                  'obUiModalProviderGroupVoid-body-container1-container3-description',
                name: 'description'
              }
            ]
          },
          {
            classes: 'obUiModalProviderGroupVoid-body-container1-container4'
          }
        ]
      },
      {
        classes: 'obUiModalProviderGroupVoid-body-providergroupcomponent',
        name: 'providergroupcomponent'
      }
    ]
  },
  initComponents: function() {
    this.inherited(arguments);
  },
  executeOnShow: function() {
    var payment = this.args.payment;
    var amount = payment.get('amount');
    var provider = payment.get('paymentData').provider;

    this.setHeader(
      OB.I18N.getLabel('OBPOS_LblModalVoidTransaction', [
        OB.I18N.formatCurrency(amount)
      ])
    );
    this.$.body.$.lblType.setContent(OB.I18N.getLabel('OBPOS_LblModalType'));
    this.$.body.$.paymenttype.setContent(provider._identifier);
    this.$.body.$.description.setContent(provider.description);
    this.showPopupMessage = true;

    // Set timeout needed because on ExecuteOnShow
    setTimeout(this.startVoid.bind(this), 0);
  },
  executeOnHide: function() {
    this.args.onhide();
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
  startVoid: function() {
    var payment = this.args.payment;
    var providerinstance = this.args.providerinstance;

    var receipt = this.args.receipt;
    var removeTransaction = this.args.removeTransaction;

    this.$.body.$.providergroupcomponent.destroyComponents();
    if (providerinstance.providerComponent) {
      this.$.body.$.providergroupcomponent
        .createComponent(providerinstance.providerComponent)
        .render();
    }

    providerinstance
      .processVoid({
        receipt: receipt,
        payment: payment
      })
      .then(
        function(response) {
          removeTransaction();
          window.setTimeout(this.doHideThisPopup.bind(this), 0);
        }.bind(this)
      )
      .catch(
        function(exception) {
          this.showPopupMessage =
            exception.showPopupMessage === false ? false : true;
          this.showMessageAndClose(
            providerinstance.getErrorMessage
              ? providerinstance.getErrorMessage(exception)
              : exception.message
          );
        }.bind(this)
      );
  }
});
