/*
 ************************************************************************************
 * Copyright (C) 2015-2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, */

enyo.kind({
  kind: 'OB.UI.Modal',
  topPosition: '75px',
  name: 'OB.OBPOSPointOfSale.UI.Modals.ModalPaymentsSelect',
  events: {
    onHideThisPopup: ''
  },
  handlers: {
    onSearchAction: 'searchAction',
    onFiltered: 'searchAction'
  },
  body: {
    components: [{
      style: 'padding: 10px 10px 5px 10px;',
      components: [{
        style: 'display: table;  width: 100%;',
        components: [{
          style: 'display: table-cell; width: 100%;',
          components: [{
            kind: 'OB.UI.SearchInputAutoFilter',
            name: 'paymentname',
            style: 'width: 100%;',
            isFirstFocus: true
          }]
        }, {
          style: 'display: table-cell;',
          components: [{
            kind: 'OB.UI.SmallButton',
            classes: 'btnlink-gray btn-icon-small btn-icon-clear',
            style: 'width: 80px; margin: 0px 5px 8px 19px;',
            tap: function () {
              this.owner.$.paymentname.setValue('');
              this.bubble('onSearchAction');
            }
          }]
        }, {
          style: 'display: table-cell;',
          components: [{
            kind: 'OB.UI.SmallButton',
            classes: 'btnlink-yellow btn-icon-small btn-icon-search',
            style: 'width: 80px; margin: 0px 0px 8px 5px;',
            tap: function () {
              this.bubble('onSearchAction');
            }
          }]
        }]
      }, {
        style: 'margin: 5px 0px 5px 0px; width: 100%; border-bottom: 1px solid #cccccc;'
      }, {
        name: 'paymentMethods',
        kind: 'Scroller',
        horizontal: 'hidden',
        maxHeight: '330px; padding-top: 10px;',
        setItems: function (items) {
          var i = 0,
              components = this.getComponents();
          while (i < components.length) {
            if (components[i].name !== 'strategy') {
              components[i++].destroy();
            } else {
              i++;
            }
          }
          items.forEach(function (item) {
            this.createComponent({
              name: item.payment.payment.searchKey,
              classes: 'paymentmethoditems',
              tag: 'div',
              allowHtml: true,
              content: '<img class="paymentmethoditemsimage" src="' + (item.image ? item.image : 'img/PMImgNotAvailable.png') + '"/><div class="paymentmethoditemstext">' + item.name + '</div>',
              payment: item.payment,
              tap: function () {
                if (!item.disabled) {
                  var dialog = this.owner.owner.owner;
                  dialog.doHideThisPopup();
                  dialog.bubble('onStatusChanged', {
                    payment: this.payment,
                    status: this.payment.payment.searchKey,
                    amount: dialog.args.amount,
                    options: dialog.args.options
                  });
                  dialog.bubble('onPaymentChanged', {
                    payment: this.payment,
                    status: this.payment.payment.searchKey,
                    amount: dialog.args.amount
                  });
                }
              },
              initComponents: function () {
                if (item.disabled) {
                  this.addClass('paymentmethoditemsdisabled');
                }
              }
            });
          }, this);
          if (items.length === 0) {
            this.createComponent({
              tag: 'div',
              classes: 'paymentmethodnotitems',
              content: OB.I18N.getLabel('OBPOS_PaymentsNoItems')
            });
          }
          this.render();
        }
      }]
    }]
  },
  searchAction: function () {
    var items = [],
        payments = OB.POS.modelterminal.get('payments'),
        filterBy = this.$.body.$.paymentname.getValue().toUpperCase();
    enyo.forEach(payments, function (payment) {
      if (payment.paymentMethod.paymentMethodCategory && payment.paymentMethod.paymentMethodCategory === this.args.idCategory && OB.MobileApp.model.hasPermission(payment.payment.searchKey)) {
        if (filterBy === '' || payment.paymentMethod._identifier.toUpperCase().indexOf(filterBy) >= 0) {
          var isDisabled = (OB.MobileApp.model.receipt.getTotal() < 0 ? !payment.paymentMethod.refundable : false);
          items.push({
            name: payment.paymentMethod._identifier,
            image: payment.image,
            payment: payment,
            disabled: isDisabled
          });
        }
      }
    }, this);
    this.$.body.$.paymentMethods.setItems(items);
    var me = this;
    setTimeout(function () {
      me.$.body.$.paymentMethods.render();
    }, 1);
  },
  executeOnShow: function () {
    this.setHeader(OB.I18N.getLabel('OBPOS_PaymentsSelectCaption'));
    this.searchAction();
    this.bubble('onClearPaymentSelect');
    this.bubble('onPaymentChanged');
  }
});