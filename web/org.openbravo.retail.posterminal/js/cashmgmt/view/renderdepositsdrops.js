/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

// Renders lines of deposits/drops
OB.OBPOSCasgMgmt.UI.RenderDepositLine = Backbone.View.extend({
  contentView: [{
    tag: 'div',
    attributes: {
      'class': 'row-fluid'
    },
    content: [{
      tag: 'div',
      attributes: {
        'class': 'span12',
        style: 'border-bottom: 1px solid #cccccc;'
      },
      content: [{
        tag: 'div',
        id: 'description',
        attributes: {
          style: 'padding: 6px 20px 6px 10px;  float: left; width: 40%'
        }
      }, {
        tag: 'div',
        id: 'user',
        attributes: {
          style: 'text-align:right; padding: 6px 20px 6px 10px; float: left;  width: 15%'
        }
      }, {
        tag: 'div',
        id: 'time',
        attributes: {
          style: 'text-align:right; padding: 6px 20px 6px 10px; float: left;  width: 10%'
        }
      }, {
        tag: 'div',
        id: 'amt',
        attributes: {
          style: 'text-align:right; padding: 6px 20px 6px 10px; float: right;'
        }
      }]
    }]
  }],

  initialize: function() {
    OB.UTIL.initContentView(this);
  },

  render: function() {
    console.log(this.model);
    var amnt, lbl, time = new Date(this.model.get('time'));
    if (this.model.get('timeOffset')) {
      time.setMinutes(time.getMinutes() + this.model.get('timeOffset') + time.getTimezoneOffset());
    }
    if (this.model.get('drop') !== 0) {
      amnt = OB.I18N.formatCurrency(OB.DEC.add(0, this.model.get('drop')));
      lbl = OB.I18N.getLabel('OBPOS_LblWithdrawal') + ': ';
    } else {
      amnt = OB.I18N.formatCurrency(OB.DEC.add(0, this.model.get('deposit')));
      lbl = OB.I18N.getLabel('OBPOS_LblDeposit') + ': ';
    }

    this.description.text(lbl + this.model.get('description'));
    this.user.text(this.model.get('user'));
    this.time.text(time.toString().substring(16, 21));
    this.amt.text(amnt);
    return this;
  }
});

// Renders summary of deposits/drops and contains its list
OB.OBPOSCasgMgmt.UI.RenderDepositsDrops = Backbone.View.extend({
  contentView: [
  //Separator
  {
    tag: 'div',
    attributes: {
      'class': 'row-fluid'
    },
    content: [{
      tag: 'div',
      attributes: {
        'class': 'span12',
        style: 'border-bottom: 1px solid #cccccc;'
      },
      content: [{
        tag: 'div',
        attributes: {
          style: 'padding: 10px 20px 10px 10px;  float: left;'
        },
        content: [{
          tag: 'div',
          attributes: {
            style: 'clear: both'
          }
        }]
      }]
    }]
  },

  // Total per payment type
  {
    tag: 'div',
    attributes: {
      'class': 'row-fluid'
    },
    content: [{
      tag: 'div',
      attributes: {
        'class': 'span12',
        style: 'border-bottom: 1px solid #cccccc;'
      },
      content: [{
        tag: 'div',
        id: 'startingCashPayName',
        attributes: {
          style: 'padding: 6px 20px 6px 10px;  float: left; width: 70%'
        }
      }, {
        tag: 'div',
        id: 'startingCashAmnt',
        attributes: {
          style: 'text-align:right; padding: 6px 20px 6px 10px; float: right;'
        }
      }]
    }]
  },

  // Tendered per payment type
  {
    tag: 'div',
    attributes: {
      'class': 'row-fluid'
    },
    content: [{
      tag: 'div',
      attributes: {
        'class': 'span12',
        style: 'border-bottom: 1px solid #cccccc;'
      },
      content: [{
        tag: 'div',
        id: 'tenderedLbl',
        attributes: {
          style: 'padding: 6px 20px 6px 10px;  float: left; width: 70%'
        }
      }, {
        tag: 'div',
        id: 'tenderedAmnt',
        attributes: {
          style: 'text-align:right; padding: 6px 20px 6px 10px; float: right;'
        }
      }]
    }]
  },
  // Drops/deposits
  {
    id: 'theList',
    view: OB.UI.TableView.extend({
      style: 'list',
      renderEmpty: Backbone.View,
      // Not to show anything in case of empty
      renderLine: OB.OBPOSCasgMgmt.UI.RenderDepositLine
    })
  },

  // Available per payment type
  {
    tag: 'div',
    attributes: {
      'class': 'row-fluid'
    },
    content: [{
      tag: 'div',
      attributes: {
        'class': 'span12',
        style: 'border-bottom: 1px solid #cccccc;'
      },
      content: [{
        tag: 'div',
        id: 'availableLbl',
        attributes: {
          style: 'padding: 10px 20px 10px 10px; float: left; width: 70%; font-weight:bold;'
        }
      }, {
        tag: 'div',
        attributes: {
          style: 'padding: 10px 20px 10px 0px;  float: right;'
        },
        content: [{
          view: Backbone.View.extend({
            contentView: [{
              tag: 'span',
              id: 'total',
              attributes: {
                'style': 'float:right; font-weight: bold;'
              }
            }],

            initialize: function() {
              OB.UTIL.initContentView(this);
              this.options.parent.model.on('change:total', function(model) {
                this.newTotal = model.get('total');
                this.render(model.get('total'));
              }, this);
            },

            render: function(amnt) {
              if (!amnt) {
                return this;
              }
              this.total.text(OB.I18N.formatCurrency(amnt));
              if (OB.DEC.compare(amnt) < 0) {
                this.$el.css("color", "red"); //negative value
              } else {
                this.$el.css("color", "black");
              }
              return this;
            }
          })
        }]
      }]
    }]
  }],

  initialize: function() {
    var transactionsArray = this.model.get('listdepositsdrops'),
        transactionsCollection = new Backbone.Collection(transactionsArray),
        total;

    OB.UTIL.initContentView(this);

    total = _.reduce(transactionsArray, function(total, trx) {
      return total + trx.deposit - trx.drop;
    }, 0);

    this.model.set('total', total);
    this.theList.registerCollection(transactionsCollection);
  },

  render: function() {
    this.startingCashPayName.text(OB.I18N.getLabel('OBPOS_LblStarting') + ' ' + this.model.get('payName'));
    this.startingCashAmnt.text(OB.I18N.formatCurrency(OB.DEC.add(0, this.model.get('startingCash'))));
    this.tenderedLbl.text(OB.I18N.getLabel('OBPOS_LblTotalTendered') + ' ' + this.model.get('payName'));
    this.tenderedAmnt.text(OB.I18N.formatCurrency(OB.DEC.add(0, this.model.get('totalTendered'))));
    this.availableLbl.text(OB.I18N.getLabel('OBPOS_LblNewAvailableIn') + ' ' + this.model.get('payName'));

    return this;
  }
});