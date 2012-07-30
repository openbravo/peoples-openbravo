/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone, confirm  */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  var DoneButton = OB.COMP.RegularButton.extend({
    'label': OB.I18N.getLabel('OBPOS_LblDone'),
    'clickEvent': function () {
      var parent = this.options.parent;
      parent.receipt.calculateTaxes(function () {
        parent.receipt.trigger('closed');
        parent.modelorderlist.deleteCurrent();
      });
    }
  });

  var ExactButton = OB.COMP.RegularButton.extend({
    icon: 'btn-icon-small btn-icon-check',
    className: 'btnlink-green',
    attributes: {style: 'width: 69px'},
    'clickEvent': function () {
      this.options.parent.options.keyboard.execStatelessCommand('cashexact');
    }
  });

  var RemovePayment = OB.COMP.SmallButton.extend({
    className: 'btnlink-darkgray btnlink-payment-clear',
    icon: 'btn-icon-small btn-icon-clearPayment',
    initialize: function () {
      OB.UTIL.initContentView(this);
      var parent = this.options.parent;
      this.$el.click(function (e) {       
        e.preventDefault();
        if (parent.options.model.get('paymentData') && !confirm(OB.I18N.getLabel('OBPOS_MsgConfirmRemovePayment'))) {
          return;  
        }
        parent.options.parent.options.parent.receipt.removePayment(parent.options.model);
      });
    }
  });

  var RenderPaymentLine = OB.COMP.SelectPanel.extend({
    contentView: [{
      tag: 'div',
      attributes: {
        'style': 'color:white;'
      },
      content: [{
        tag: 'div',
        id: 'divname',
        attributes: {
          style: 'float: left; width: 15%; padding: 5px 0px 0px 0px;'
        }
      }, {
        tag: 'div',
        id: 'divinfo',
        attributes: {
          style: 'float: left; width: 50%; padding: 5px 0px 0px 0px;'
        }
      }, {
        tag: 'div',
        id: 'divamount',
        attributes: {
          style: 'float: left; width: 20%; padding: 5px 0px 0px 0px; text-align: right;'
        }
      }, {
        tag: 'div',
        attributes: {
          style: 'float: left; width: 15%; text-align: right;'
        },
        content: [{
          view: RemovePayment
        }]
      }, {
        tag: 'div',
        attributes: {
          style: 'clear: both;'
        }
      }]
    }],
    render: function () {
      this.divname.text(OB.POS.modelterminal.getPaymentName(this.model.get('kind')));
      this.divamount.text(this.model.printAmount());
      if (this.model.get('paymentData')) {
        this.divinfo.text(this.model.get('paymentData').Name);
      } else {
        this.divinfo.text('');
      }
      return this;
    }
  });

  OB.COMP.Payment = Backbone.View.extend({
    tag: 'div',
    contentView: [

    {
      tag: 'div',
      attributes: {
        'style': 'background-color: #363636; color: white; height: 200px; margin: 5px; padding: 5px'
      },
      content: [{
        tag: 'div',
        attributes: {
          'class': 'row-fluid'
        },
        content: [{
          tag: 'div',
          attributes: {
            'class': 'span12'
          },
          content: []
        }]
      }, {
        tag: 'div',
        attributes: {
          'class': 'row-fluid'
        },
        content: [{
          tag: 'div',
          attributes: {
            'class': 'span10'
          },
          content: [{
            tag: 'div',
            attributes: {
              'style': 'padding: 10px 0px 0px 10px;'
            },
            content: [{
              tag: 'span',
              id: 'totalpending',
              attributes: {
                style: 'font-size: 24px; font-weight: bold;'
              }
            }, {
              tag: 'span',
              id: 'totalpendinglbl',
              content: [OB.I18N.getLabel('OBPOS_PaymentsRemaining')]
            }, {
              tag: 'span',
              id: 'change',
              attributes: {
                style: 'font-size: 24px; font-weight: bold;'
              }
            }, {
              tag: 'span',
              id: 'changelbl',
              content: [OB.I18N.getLabel('OBPOS_PaymentsChange')]
            }, {
              tag: 'span',
              id: 'overpayment',
              attributes: {
                style: 'font-size: 24px; font-weight: bold;'
              }
            }, {
              tag: 'span',
              id: 'overpaymentlbl',
              content: [OB.I18N.getLabel('OBPOS_PaymentsOverpayment')]
            }, {
              tag: 'span',
              id: 'exactlbl',
              content: [OB.I18N.getLabel('OBPOS_PaymentsExact')]
            }]
          }, {
            tag: 'div',
            attributes: {
              style: 'overflow:auto; width: 100%;'
            },
            content: [{
              tag: 'div',
              attributes: {
                'style': 'padding: 5px'
              },
              content: [{
                tag: 'div',
                attributes: {
                  'style': 'margin: 2px 0px 0px 0px; border-bottom: 1px solid #cccccc;'
                },
                content: []
              }, {
                id: 'tableview',
                view: OB.UI.TableView.extend({
                  renderEmpty: Backbone.View.extend({
                    tagName: 'div',
                    attributes: {
                      'style': 'height: 36px'
                    }
                  }),
                  renderLine: RenderPaymentLine
                })
              }]
            }]
          }]
        }, {
          tag: 'div',
          attributes: {
            'class': 'span2'
          },
          content: [{
            tag: 'div',
            attributes: {
              'style': 'float: right;'
            },
            id: 'doneaction',
            content: [{
              view: DoneButton
            }]
          }, {
            tag: 'div',
            attributes: {
              'style': 'float: right;'
            },
            id: 'exactaction',
            content: [{
              view: ExactButton
            }]
          }]
        }]
      }]
    }],
    initialize: function () {

      OB.UTIL.initContentView(this);

      var i, max;
      var me = this;

      this.modelorderlist = this.options.root.modelorderlist;
      this.receipt = this.options.root.modelorder;
      var payments = this.receipt.get('payments');
      var lines = this.receipt.get('lines');

      this.tableview.registerCollection(payments);

      this.receipt.on('change:payment change:change change:gross', function () {
        this.updatePending();
      }, this);
      this.updatePending();
    },
    updatePending: function () {
      var paymentstatus = this.receipt.getPaymentStatus();
      if (paymentstatus.change) {
        this.change.text(paymentstatus.change);
        this.change.show();
        this.changelbl.show();
      } else {
        this.change.hide();
        this.changelbl.hide();
      }
      if (paymentstatus.overpayment) {
        this.overpayment.text(paymentstatus.overpayment);
        this.overpayment.show();
        this.overpaymentlbl.show();
      } else {
        this.overpayment.hide();
        this.overpaymentlbl.hide();
      }
      if (paymentstatus.done) {
        this.totalpending.hide();
        this.totalpendinglbl.hide();
        this.doneaction.show();
      } else {
        this.totalpending.text(paymentstatus.pending);
        this.totalpending.show();
        this.totalpendinglbl.show();
        this.doneaction.hide();
      }

      if (paymentstatus.done || this.receipt.getGross() === 0) {
        this.exactaction.hide();
      } else {
        this.exactaction.show();
      }

      if (paymentstatus.done && !paymentstatus.change && !paymentstatus.overpayment) {
        this.exactlbl.show();
      } else {
        this.exactlbl.hide();
      }
    }
  });
}());