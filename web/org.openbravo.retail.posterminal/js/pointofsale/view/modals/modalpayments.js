/*
 ************************************************************************************
 * Copyright (C) 2015-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, _*/

enyo.kind({
  kind: 'OB.UI.Modal',
  topPosition: '75px',
  name: 'OB.OBPOSPointOfSale.UI.Modals.ModalPaymentsSelect',
  classes: 'obObposPointOfSaleUiModalsModalPaymentsSelect',
  events: {
    onHideThisPopup: ''
  },
  handlers: {
    onSearchAction: 'searchAction',
    onFiltered: 'searchAction'
  },
  body: {
    classes: 'obObposPointOfSaleUiModalsModalPaymentsSelect-body',
    components: [
      {
        classes:
          'obObposPointOfSaleUiModalsModalPaymentsSelect-body-container1',
        components: [
          {
            classes:
              'obObposPointOfSaleUiModalsModalPaymentsSelect-body-container1-container1',
            components: [
              {
                classes:
                  'obObposPointOfSaleUiModalsModalPaymentsSelect-body-container1-container1-container1',
                components: [
                  {
                    kind: 'OB.UI.SearchInputAutoFilter',
                    name: 'paymentname',
                    classes:
                      'obObposPointOfSaleUiModalsModalPaymentsSelect-body-container1-container1-container1-paymentname',
                    isFirstFocus: true
                  }
                ]
              },
              {
                classes:
                  'obObposPointOfSaleUiModalsModalPaymentsSelect-body-container1-container1-container2',
                components: [
                  {
                    kind: 'OB.UI.SmallButton',
                    classes:
                      'obObposPointOfSaleUiModalsModalPaymentsSelect-body-container1-container1-container2-obUiSmallButton',
                    tap: function() {
                      this.owner.$.paymentname.setValue('');
                      this.bubble('onSearchAction');
                    }
                  }
                ]
              },
              {
                classes:
                  'obObposPointOfSaleUiModalsModalPaymentsSelect-body-container1-container1-container3',
                components: [
                  {
                    kind: 'OB.UI.SmallButton',
                    classes:
                      'obObposPointOfSaleUiModalsModalPaymentsSelect-body-container1-container1-container3-obUiSmallButton',
                    tap: function() {
                      this.bubble('onSearchAction');
                    }
                  }
                ]
              }
            ]
          },
          {
            classes:
              'obObposPointOfSaleUiModalsModalPaymentsSelect-body-container1-container2'
          },
          {
            name: 'paymentMethods',
            kind: 'Scroller',
            classes:
              'obObposPointOfSaleUiModalsModalPaymentsSelect-body-container1-paymentMethods',
            setItems: function(items) {
              var i = 0,
                components = this.getComponents();
              while (i < components.length) {
                if (components[i].name !== 'strategy') {
                  components[i++].destroy();
                } else {
                  i++;
                }
              }
              items.forEach(function(item) {
                this.createComponent({
                  name: item.payment.payment.searchKey,
                  classes:
                    'obObposPointOfSaleUiModalsModalPaymentsSelect-paymentMethods-items',
                  tag: 'div',
                  allowHtml: true,
                  content:
                    '<img class="obObposPointOfSaleUiModalsModalPaymentsSelect-paymentMethods-items-image" src="' +
                    (item.image ? item.image : 'img/PMImgNotAvailable.png') +
                    '"/><div class="obObposPointOfSaleUiModalsModalPaymentsSelect-paymentMethods-items-text">' +
                    item.name +
                    '</div>',
                  payment: item.payment,
                  tap: function() {
                    if (!item.disabled) {
                      var dialog = this.owner.owner.owner;
                      dialog.selectItem = true;
                      dialog.doHideThisPopup();
                      dialog.bubble('onPaymentChanged', {
                        payment: this.payment,
                        status: this.payment.payment.searchKey,
                        amount: dialog.args.amount
                      });
                      if (!dialog.args.cashManagement) {
                        dialog.bubble('onStatusChanged', {
                          payment: this.payment,
                          status: this.payment.payment.searchKey,
                          amount: dialog.args.amount,
                          options: dialog.args.options
                        });
                      }
                    }
                  },
                  initComponents: function() {
                    if (item.disabled) {
                      this.addClass(
                        'obObposPointOfSaleUiModalsModalPaymentsSelect-body-container1-paymentMethods_disabled'
                      );
                    }
                  }
                });
              }, this);
              if (items.length === 0) {
                this.createComponent({
                  tag: 'div',
                  classes:
                    'obObposPointOfSaleUiModalsModalPaymentsSelect-paymentMethods-noItems',
                  content: OB.I18N.getLabel('OBPOS_PaymentsNoItems')
                });
              }
              this.render();
            }
          }
        ]
      }
    ]
  },
  searchAction: function() {
    var items = [],
      payments = OB.POS.modelterminal.get('payments'),
      filterBy = this.$.body.$.paymentname.getValue().toUpperCase();
    if (this.args.availables) {
      enyo.forEach(
        this.args.availables,
        function(sk) {
          var payment = _.find(payments, function(pay) {
            return pay.paymentMethod.searchKey === sk;
          });
          if (payment) {
            items.push({
              name: payment.paymentMethod._identifier,
              image: payment.image,
              payment: payment,
              disabled: false
            });
          }
        },
        this
      );
    } else {
      enyo.forEach(
        payments,
        function(payment) {
          var permission = !_.isUndefined(
            OB.MobileApp.model.hasPermission(payment.payment.searchKey)
          )
            ? OB.MobileApp.model.hasPermission(payment.payment.searchKey)
            : true;
          if (
            payment.paymentMethod.paymentMethodCategory &&
            payment.paymentMethod.paymentMethodCategory ===
              this.args.idCategory &&
            permission
          ) {
            if (
              filterBy === '' ||
              payment.paymentMethod._identifier
                .toUpperCase()
                .indexOf(filterBy) >= 0
            ) {
              var isDisabled =
                OB.MobileApp.model.receipt.getTotal() < 0
                  ? !payment.paymentMethod.refundable
                  : false;
              items.push({
                name: payment.paymentMethod._identifier,
                image: payment.image,
                payment: payment,
                disabled: isDisabled
              });
            }
          }
        },
        this
      );
    }
    this.$.body.$.paymentMethods.setItems(items);
    var me = this;
    setTimeout(function() {
      me.$.body.$.paymentMethods.render();
    }, 1);
  },
  executeOnShow: function() {
    this.setHeader(OB.I18N.getLabel('OBPOS_PaymentsSelectCaption'));
    this.searchAction();
    this.bubble('onClearPaymentSelect');
    this.bubble('onPaymentChanged');
    this.selectItem = false;
  },
  executeOnHide: function() {
    if (!this.selectItem) {
      this.bubble('onPaymentChangedCancelled', {
        cashManagement: this.args.cashManagement
      });
    }
  }
});
