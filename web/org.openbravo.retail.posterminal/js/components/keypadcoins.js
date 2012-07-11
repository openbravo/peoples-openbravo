/*global $, _, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
 
  OB.COMP.KeypadCoins = OB.COMP.KeypadBasic.extend({
    name: 'coins',
    label: '$,$,$,..',
    contentView: [{
      tag: 'div',
      attributes: {
        'class': 'row-fluid'
      },
      content: [{
        tag: 'div',
        attributes: {
          'class': 'span4'
        },
        content: [{
          view: OB.COMP.ButtonKey.extend({
            command: '/',
            classButton: 'btnkeyboard-num',
            contentViewButton: ['/']
          })
        }]
      }, {
        tag: 'div',
        attributes: {
          'class': 'span4'
        },
        content: [{
          view: OB.COMP.ButtonKey.extend({
            command: '*',
            classButton: 'btnkeyboard-num',
            contentViewButton: ['*']
          })
        }]
      }, {
        tag: 'div',
        attributes: {
          'class': 'span4'
        },
        content: [{
          view: OB.COMP.ButtonKey.extend({
            command: '%',
            classButton: 'btnkeyboard-num',
            contentViewButton: ['%']
          })
        }]
      }]
    }, {
      tag: 'div',
      attributes: {
        'class': 'row-fluid'
      },
      content: [{
        tag: 'div',
        attributes: {
          'class': 'span4'
        },
        content: [{
          view: OB.COMP.PaymentButton.extend({
            paymenttype: 'OBPOS_payment.cash',
            amount: 10,
            background: '#e9b7c3'
          })
        }]
      }, {
        tag: 'div',
        attributes: {
          'class': 'span4'
        },
        content: [{
          view: OB.COMP.PaymentButton.extend({
            paymenttype: 'OBPOS_payment.cash',
            amount: 20,
            background: '#bac3de'
          })
        }]
      }, {
        tag: 'div',
        attributes: {
          'class': 'span4'
        },
        content: [{
          view: OB.COMP.PaymentButton.extend({
            paymenttype: 'OBPOS_payment.cash',
            amount: 50,
            background: '#f9bb92'
          })
        }]
      }]
    }, {
      tag: 'div',
      attributes: {
        'class': 'row-fluid'
      },
      content: [{
        tag: 'div',
        attributes: {
          'class': 'span4'
        },
        content: [{
          view: OB.COMP.PaymentButton.extend({
            paymenttype: 'OBPOS_payment.cash',
            amount: 1,
            background: '#e4e0e3',
            bordercolor: '#f9e487'
          })
        }]
      }, {
        tag: 'div',
        attributes: {
          'class': 'span4'
        },
        content: [{
          view: OB.COMP.PaymentButton.extend({
            paymenttype: 'OBPOS_payment.cash',
            amount: 2,
            background: '#f9e487',
            bordercolor: '#e4e0e3'
          })
        }]
      }, {
        tag: 'div',
        attributes: {
          'class': 'span4'
        },
        content: [{
          view: OB.COMP.PaymentButton.extend({
            paymenttype: 'OBPOS_payment.cash',
            amount: 5,
            background: '#bccdc5'
          })
        }]
      }]
    }, {
      tag: 'div',
      attributes: {
        'class': 'row-fluid'
      },
      content: [{
        tag: 'div',
        attributes: {
          'class': 'span4'
        },
        content: [{
          view: OB.COMP.PaymentButton.extend({
            paymenttype: 'OBPOS_payment.cash',
            amount: 0.10,
            background: '#f9e487'
          })
        }]
      }, {
        tag: 'div',
        attributes: {
          'class': 'span4'
        },
        content: [{
          view: OB.COMP.PaymentButton.extend({
            paymenttype: 'OBPOS_payment.cash',
            amount: 0.20,
            background: '#f9e487'
          })
        }]
      }, {
        tag: 'div',
        attributes: {
          'class': 'span4'
        },
        content: [{
          view: OB.COMP.PaymentButton.extend({
            paymenttype: 'OBPOS_payment.cash',
            amount: 0.50,
            background: '#f9e487'
          })
        }]
      }]
    }, {
      tag: 'div',
      attributes: {
        'class': 'row-fluid'
      },
      content: [{
        tag: 'div',
        attributes: {
          'class': 'span4'
        },
        content: [{
          view: OB.COMP.PaymentButton.extend({
            paymenttype: 'OBPOS_payment.cash',
            amount: 0.01,
            background: '#f3bc9e'
          })
        }]
      }, {
        tag: 'div',
        attributes: {
          'class': 'span4'
        },
        content: [{
          view: OB.COMP.PaymentButton.extend({
            paymenttype: 'OBPOS_payment.cash',
            amount: 0.02,
            background: '#f3bc9e'
          })
        }]
      }, {
        tag: 'div',
        attributes: {
          'class': 'span4'
        },
        content: [{
          view: OB.COMP.PaymentButton.extend({
            paymenttype: 'OBPOS_payment.cash',
            amount: 0.05,
            background: '#f3bc9e'
          })
        }]
      }]
    }]
  });

}());