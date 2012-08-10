/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone, confirm  */

(function() {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  var DoneButton = OB.COMP.RegularButton.extend({
    'label': OB.I18N.getLabel('OBPOS_LblDone'),
    'clickEvent': function() {
      var parent = this.options.parent;
      parent.receipt.calculateTaxes(function() {
        parent.receipt.trigger('closed');
        parent.modelorderlist.deleteCurrent();
      });
    }
  });

  var ExactButton = OB.COMP.RegularButton.extend({
    icon: 'btn-icon-small btn-icon-check',
    className: 'btnlink-green',
    attributes: {
      style: 'width: 69px'
    },
    'clickEvent': function() {
      this.options.parent.options.keyboard.execStatelessCommand('cashexact');
    }
  });

  var RemovePayment = OB.COMP.SmallButton.extend({
    className: 'btnlink-darkgray btnlink-payment-clear',
    icon: 'btn-icon-small btn-icon-clearPayment',
    initialize: function() {
      OB.UTIL.initContentView(this);
      var parent = this.options.parent;
      this.$el.click(function(e) {
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
    render: function() {
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

  enyo.kind({
    name: 'OB.UI.Payment',
    components: [{
      style: 'background-color: #363636; color: white; height: 200px; margin: 5px; padding: 5px',
      components: [{
        classes: 'row-fluid',
        components: [{
          classes: 'span12'
        }]
      }, {
        classes: 'row-fluid',
        components: [{
          classes: 'span9',
          components: [{
            style: 'padding: 10px 0px 0px 10px;',
            components: [{
              tag: 'span',
              name: 'totalpending',
              style: 'font-size: 24px; font-weight: bold;'
            }, {
              tag: 'span',
              name: 'totalpendinglbl',
              content: OB.I18N.getLabel('OBPOS_PaymentsRemaining')
            }, {
              tag: 'span',
              name: 'change',
              style: 'font-size: 24px; font-weight: bold;'
            }, {
              tag: 'span',
              name: 'changelbl',
              content: OB.I18N.getLabel('OBPOS_PaymentsChange')
            }, {
              tag: 'span',
              name: 'overpayment',
              style: 'font-size: 24px; font-weight: bold;'
            }, {
              tag: 'span',
              name: 'overpaymentlbl',
              content: OB.I18N.getLabel('OBPOS_PaymentsOverpayment')
            }, {
              tag: 'span',
              name: 'exactlbl',
              content: OB.I18N.getLabel('OBPOS_PaymentsExact')
            }]
          }, {
            style: 'overflow:auto; width: 100%;',
            components: [{
              style: 'padding: 5px',
              components: [{
                style: 'margin: 2px 0px 0px 0px; border-bottom: 1px solid #cccccc;'
              }, {
                kind: 'OB.UI.Table',
                name: 'payments',
                renderEmpty: enyo.kind({
                  style: 'height: 36px'
                }),
                renderLine: 'OB.UI.RenderPaymentLine'
              }]
            }]
          }]
        }, {
          classes: 'span3',
          components: [{
            style: 'float: right;',
            name: 'doneaction',
            components: [{
              kind: 'OB.UI.DoneButton'
            }]
          }, {
            style: 'float: right;',
            name: 'exactaction',
            components: [{
              kind: 'OB.UI.ExactButton'
            }]
          }]
        }]

      }]
    }],

    init: function() {
      this.inherited(arguments);

      var receipt = this.owner.owner.model.get('order');

      console.log('init payemnt');

      this.$.payments.setCollection(receipt.get('payments'));

      receipt.on('change:payment change:change change:gross', function() {
        this.updatePending(receipt);
      }, this);
      this.updatePending(receipt);
    },

    updatePending: function(receipt) {
      var paymentstatus = receipt.getPaymentStatus();
      if (paymentstatus.change) {
        this.$.change.setContent(paymentstatus.change);
        this.$.change.show();
        this.$.changelbl.show();
      } else {
        this.$.change.hide();
        this.$.changelbl.hide();
      }
      if (paymentstatus.overpayment) {
        this.$.overpayment.setContent(paymentstatus.overpayment);
        this.$.overpayment.show();
        this.$.overpaymentlbl.show();
      } else {
        this.$.overpayment.hide();
        this.$.overpaymentlbl.hide();
      }
      if (paymentstatus.done) {
        this.$.totalpending.hide();
        this.$.totalpendinglbl.hide();
        this.$.doneaction.show();
      } else {
        this.$.totalpending.setContent(paymentstatus.pending);
        this.$.totalpending.show();
        this.$.totalpendinglbl.show();
        this.$.doneaction.hide();
      }

      if (paymentstatus.done || receipt.getGross() === 0) {
        this.$.exactaction.hide();
      } else {
        this.$.exactaction.show();
      }

      if (paymentstatus.done && !paymentstatus.change && !paymentstatus.overpayment) {
        this.$.exactlbl.show();
      } else {
        this.$.exactlbl.hide();
      }
    }
  });

  enyo.kind({
    name: 'OB.UI.DoneButton',
    kind: 'OB.UI.RegularButton',
    content: OB.I18N.getLabel('OBPOS_LblDone'),
    tap: function() {
      var receipt = this.owner.owner.owner.model.get('order');
      var orderlist = this.owner.owner.owner.model.get('orderList');
      receipt.calculateTaxes(function() {
        console.log('taxes done');
        receipt.trigger('closed');
        orderlist.deleteCurrent();
      });
    }
  });

  enyo.kind({
    name: 'OB.UI.ExactButton',
    kind: 'OB.UI.RegularButton',
    classes: 'btn-icon-small btn-icon-check btnlink-green',
    style: 'width: 69px',
    tap: function() {
      this.owner.owner.owner.$.keyboard.execStatelessCommand('cashexact');
      console.log('exact');
    }
  });

  enyo.kind({
    name: 'OB.UI.RenderPaymentLine',
    style: 'color:white;',
    components: [{
      name: 'name',
      style: 'float: left; width: 15%; padding: 5px 0px 0px 0px;'
    }, {
      name: 'info',
      style: 'float: left; width: 50%; padding: 5px 0px 0px 0px;'
    }, {
      name: 'amount',
      style: 'float: left; width: 20%; padding: 5px 0px 0px 0px; text-align: right;'
    }, {
      style: 'float: left; width: 15%; text-align: right;',
      components: [{
        kind: 'OB.UI.RemovePayment'
      }]
    }, {
      style: 'clear: both;'
    }],
    initComponents: function() {
      this.inherited(arguments);
      console.log('RenderPaymentLine initComponents');
      this.$.name.setContent(OB.POS.modelterminal.getPaymentName(this.model.get('kind')));
      this.$.amount.setContent(this.model.printAmount());
      if (this.model.get('paymentData')) {
        this.$.info.setContent(this.model.get('paymentData').Name);
      } else {
        this.$.info.setContent('');
      }
    }
  });

  enyo.kind({
    name: 'OB.UI.RemovePayment',
    kind: 'OB.UI.SmallButton',
    classes: 'btnlink-darkgray btnlink-payment-clear btn-icon-small btn-icon-clearPayment',
    tap: function() {
      var model = this.owner.owner.owner.owner.owner.owner.owner.model;
      if (model.get('paymentData') && !confirm(OB.I18N.getLabel('OBPOS_MsgConfirmRemovePayment'))) {
        return;
      }
      model.get('order').removePayment(this.owner.model);
      console.log('remove')
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
    initialize: function() {

      OB.UTIL.initContentView(this);

      var i, max;
      var me = this;

      this.modelorderlist = this.options.root.modelorderlist;
      this.receipt = this.options.root.modelorder;
      var payments = this.receipt.get('payments');
      var lines = this.receipt.get('lines');

      this.tableview.registerCollection(payments);

      this.receipt.on('change:payment change:change change:gross', function() {
        this.updatePending();
      }, this);
      this.updatePending();
    },
    updatePending: function() {
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