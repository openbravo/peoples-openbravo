/*
 ************************************************************************************
 * Copyright (C) 2013-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, _ */
enyo.kind({
  name: 'OB.UI.RemoveMultiOrders',
  kind: 'OB.UI.Button',
  classes: 'obUiRemoveMultiOrders',
  events: {
    onRemoveMultiOrders: ''
  },
  i18nLabel: 'OBMOBC_Remove',
  tap: function() {
    if (_.isUndefined(this.deleting) || this.deleting === false) {
      this.deleting = true;
      this.addClass('obUiRemoveMultiOrders_loading');
      this.doRemoveMultiOrders({
        order: this.owner.model
      });
    }
  }
});

enyo.kind({
  kind: 'OB.UI.SelectButton',
  name: 'OB.UI.RenderMultiOrdersLineValues',
  classes: 'obUiRenderMultiOrdersLineValues',
  handlers: {
    onChangeEditMode: 'changeEditMode'
  },
  events: {
    onShowPopup: ''
  },
  components: [
    {
      name: 'line',
      classes: 'obUiRenderMultiOrdersLineValues-line',
      components: [
        {
          name: 'multiTopLine',
          classes: 'obUiRenderMultiOrdersLineValues-line-multiTopLine',
          components: [
            {
              name: 'documentNo',
              classes:
                'obUiRenderMultiOrdersLineValues-multiTopLine-documentNo',
              initComponents: function() {
                const receipt = OB.MobileApp.model.receipt,
                  docNo = receipt.get('documentNo');
                let bp = '';
                if (bp) {
                  if (receipt.get('externalBusinessPartner')) {
                    bp = new OB.App.Class.ExternalBusinessPartner(
                      receipt.get('externalBusinessPartner')
                    ).getIdentifier();
                  } else {
                    bp = receipt.get('bp').get('_identifier');
                  }
                  if (docNo) {
                    this.setContent(docNo + ' - ' + bp);
                  } else {
                    this.setContent(bp);
                  }
                }
              }
            },
            {
              name: 'total',
              classes: 'obUiRenderMultiOrdersLineValues-multiTopLine-total',
              initComponents: function() {
                const receipt = OB.MobileApp.model.receipt;
                this.setContent(
                  !_.isUndefined(receipt.get('amountToLayaway')) &&
                    !_.isNull(receipt.model.get('amountToLayaway'))
                    ? OB.I18N.formatCurrency(
                        receipt.model.get('amountToLayaway')
                      )
                    : receipt.printPending()
                );
              }
            }
          ]
        },
        {
          name: 'totalAndLayaway',
          classes: 'obUiRenderMultiOrdersLineValues-line-totalAndLayaway',
          components: [
            {
              name: 'totalOrder',
              classes:
                'obUiRenderMultiOrdersLineValues-totalAndLayaway-totalOrder',
              initComponents: function() {
                const receipt = OB.MobileApp.model.receipt;
                this.setContent(
                  OB.I18N.getLabel('OBPOS_LineTotal') +
                    ': ' +
                    receipt.printTotal()
                );
              }
            },
            {
              name: 'isLayaway',
              classes:
                'obUiRenderMultiOrdersLineValues-totalAndLayaway-isLayaway',
              initComponents: function() {
                const receipt = OB.MobileApp.model.receipt;
                if (receipt.get('isLayaway')) {
                  this.setContent(OB.I18N.getLabel('OBPOS_LblLayaway'));
                } else if (
                  !_.isUndefined(receipt.get('amountToLayaway')) &&
                  !_.isNull(receipt.get('amountToLayaway'))
                ) {
                  this.setContent(OB.I18N.getLabel('OBPOS_ToBeLaidaway'));
                }
              }
            }
          ]
        },
        {
          name: 'multiBottonLine',
          classes: 'obUiRenderMultiOrdersLineValues-line-multiBottonLine',
          initComponents: function() {
            const receipt = OB.MobileApp.model.receipt;
            this.setContent(
              OB.I18N.getLabel('OBPOS_RemainingToPay') +
                ': ' +
                receipt.printPending() +
                ' - (' +
                OB.I18N.formatDate(new Date(receipt.get('orderDate'))) +
                ') '
            );
          }
        },
        {
          classes: 'obUiRenderMultiOrdersLineValues-line-element1'
        }
      ]
    }
  ],
  tap: function() {
    if (OB.MobileApp.model.hasPermission('OBPOS_receipt.layawayReceipt')) {
      this.doShowPopup({
        popup: 'modalmultiorderslayaway',
        args: this.owner.model
      });
    }
  },
  changeEditMode: function(inSender, inEvent) {
    this.addRemoveClass('obUiRenderOrderLine_edit', inEvent.edit);
    this.bubble('onShowColumn', {
      colNum: 1
    });
  }
});

enyo.kind({
  name: 'OB.UI.RenderMultiOrdersLine',
  classes: 'obUiRenderMultiOrdersLine',
  components: [
    {
      kind: 'OB.UI.RemoveMultiOrders',
      classes: 'obUiRenderMultiOrdersLine-obUiRemoveMultiOrders'
    },
    {
      kind: 'OB.UI.RenderMultiOrdersLineValues',
      classes: 'obUiRenderMultiOrdersLine-obUiRenderMultiOrdersLineValues'
    }
  ]
});

enyo.kind({
  name: 'OB.UI.RenderMultiOrdersLineEmpty',
  classes: 'obUiRenderMultiOrdersLineEmpty',
  initComponents: function() {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel('OBPOS_ReceiptNew'));
  }
});
