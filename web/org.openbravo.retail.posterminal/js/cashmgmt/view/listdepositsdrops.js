/*
 ************************************************************************************
 * Copyright (C) 2012-2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Backbone, _, enyo */

// Renders lines of deposits/drops
enyo.kind({
  name: 'OB.OBPOSCashMgmt.UI.RenderDepositLine',
  classes: 'obObposcashmgmtUiRenderDepositLine',
  components: [{
    classes: 'obObposcashmgmtUiRenderDepositLine-container1 row-fluid',
    components: [{
      classes: 'obObposcashmgmtUiRenderDepositLine-container1-container1 span12',
      components: [{
        name: 'description',
        classes: 'obObposcashmgmtUiRenderDepositLine-container1-container1-description'
      }, {
        name: 'user',
        classes: 'obObposcashmgmtUiRenderDepositLine-container1-container1-user'
      }, {
        name: 'time',
        classes: 'obObposcashmgmtUiRenderDepositLine-container1-container1-time'
      }, {
        name: 'foreignAmt',
        classes: 'obObposcashmgmtUiRenderDepositLine-container1-container1-foreignAmt',
        content: ''
      }, {
        name: 'amt',
        classes: 'obObposcashmgmtUiRenderDepositLine-container1-container1-amt'
      }]
    }]
  }],
  create: function () {
    var amnt, foreignAmt, lbl;

    this.inherited(arguments);
    if (this.model.get('type') === 'drop') {
      lbl = OB.I18N.getLabel('OBPOS_LblWithdrawal') + ': ';
      if (this.model.get('origAmount') !== this.model.get('amount')) {
        foreignAmt = OB.I18N.formatCurrency(OB.DEC.add(0, this.model.get('amount')));
        amnt = OB.I18N.formatCurrency(this.model.get('origAmount'));
      } else {
        amnt = OB.I18N.formatCurrency(OB.DEC.add(0, this.model.get('amount')));
      }
    } else {
      lbl = OB.I18N.getLabel('OBPOS_LblDeposit') + ': ';
      if (this.model.get('origAmount') !== this.model.get('amount')) {
        foreignAmt = OB.I18N.formatCurrency(OB.DEC.add(0, this.model.get('amount')));
        amnt = OB.I18N.formatCurrency(this.model.get('origAmount'));
      } else {
        amnt = OB.I18N.formatCurrency(OB.DEC.add(0, this.model.get('amount')));
      }
    }
    var creationDate = new Date(this.model.get('creationDate'));
    this.$.description.setContent(lbl + this.model.get('description'));
    this.$.user.setContent(this.model.get('user'));
    this.$.time.setContent(OB.UTIL.padNumber(creationDate.getHours(), 2) + ':' + OB.UTIL.padNumber(creationDate.getMinutes(), 2));
    if (foreignAmt && ((this.model.get('rate') && this.model.get('rate') !== '1') || amnt !== foreignAmt)) {
      this.$.foreignAmt.setContent('(' + foreignAmt + ' ' + this.model.get('isocode') + ')');
    }
    this.$.amt.setContent(amnt);
  }
});

enyo.kind({
  name: 'OB.OBPOSCashMgmt.UI.RenderForeignTotal',
  classes: 'obObposcashmgmtUiRenderForeignTotal',
  tag: 'span',
  published: {
    foreignTotal: null,
    textForeignTotal: ''
  },
  create: function () {
    this.inherited(arguments);
    this.owner.model.on('change:total', function (model) {
      this.setForeignTotal(model.get('total'));
    }, this);
  },
  foreignTotalChanged: function (oldValue) {
    this.setContent(this.textForeignTotal);
    if (OB.DEC.compare(this.foreignTotal) < 0) {
      this.addClass('obObposcashmgmtUiRenderForeignTotal_negative');
    } else {
      this.addClass('obObposcashmgmtUiRenderForeignTotal_positive');
    }
  }
});


enyo.kind({
  name: 'OB.OBPOSCashMgmt.UI.RenderTotal',
  classes: 'obObposcashmgmtUiRenderTotal',
  tag: 'span',
  published: {
    total: null
  },
  create: function () {
    this.inherited(arguments);
    this.owner.model.on('change:total', function (model) {
      this.setTotal(model.get('total'));
    }, this);
  },
  totalChanged: function (oldValue) {
    this.setContent(OB.I18N.formatCurrency(this.total));
    if (OB.DEC.compare(this.total) < 0) {
      this.addClass('obObposcashmgmtUiRenderTotal_negative');
    } else {
      this.addClass('obObposcashmgmtUiRenderTotal_positive');
    }
  }
});

//Renders each of the payment types with their summary and a list of deposits/drops (OB.OBPOSCashMgmt.UI.RenderDepositLine)
enyo.kind({
  name: 'OB.OBPOSCashMgmt.UI.RenderDepositsDrops',
  classes: 'obObposcashmgmtUiRenderDepositsDrops',
  components: [
  // separator
  {
    classes: 'obObposcashmgmtUiRenderDepositsDrops-container1 row-fluid',
    components: [{
      classes: 'obObposcashmgmtUiRenderDepositsDrops-container1-container1 span12',
      components: [{
        classes: 'obObposcashmgmtUiRenderDepositsDrops-container1-container1-element1'
      }, {
        classes: 'obObposcashmgmtUiRenderDepositsDrops-container1-container1-element2'
      }]
    }]
  },

  // Total per payment type
  {
    classes: 'obObposcashmgmtUiRenderDepositsDrops-container2 row-fluid',
    components: [{
      classes: 'obObposcashmgmtUiRenderDepositsDrops-container2-container1 span12',
      components: [{
        name: 'startingCashPayName',
        classes: 'obObposcashmgmtUiRenderDepositsDrops-container2-container1-startingCashPayName'
      }, {
        name: 'startingCashForeignAmnt',
        classes: 'obObposcashmgmtUiRenderDepositsDrops-container2-container1-startingCashForeignAmnt',
        content: ''
      }, {
        name: 'startingCashAmnt',
        classes: 'obObposcashmgmtUiRenderDepositsDrops-container2-container1-startingCashAmnt'
      }]
    }]
  },

  // Tendered per payment type
  {
    classes: 'obObposcashmgmtUiRenderDepositsDrops-container3 row-fluid',
    components: [{
      classes: 'obObposcashmgmtUiRenderDepositsDrops-container3-container1 span12',
      components: [{
        name: 'tenderedLbl',
        classes: 'obObposcashmgmtUiRenderDepositsDrops-container3-container1-tenderedLbl'
      }, {
        name: 'tenderedForeignAmnt',
        classes: 'obObposcashmgmtUiRenderDepositsDrops-container3-container1-tenderedForeignAmnt',
        content: ''
      }, {
        name: 'tenderedAmnt',
        classes: 'obObposcashmgmtUiRenderDepositsDrops-container3-container1-tenderedAmnt'
      }]
    }]
  },

  // Drops/deposits
  {
    name: 'theList',
    listStyle: 'list',
    kind: 'OB.UI.Table',
    classes: 'obObposcashmgmtUiRenderDepositsDrops-theList',
    renderLine: 'OB.OBPOSCashMgmt.UI.RenderDepositLine',
    renderEmpty: 'enyo.Control'
  },

  // Available per payment type
  {
    classes: 'obObposcashmgmtUiRenderDepositsDrops-container4 row-fluid',
    components: [{
      classes: 'obObposcashmgmtUiRenderDepositsDrops-container4-container1 span12',
      components: [{
        name: 'availableLbl',
        classes: 'obObposcashmgmtUiRenderDepositsDrops-container4-container1-availableLbl'
      }, {
        classes: 'obObposcashmgmtUiRenderDepositsDrops-container4-container1-container1',
        components: [{
          name: 'foreignTotal',
          kind: 'OB.OBPOSCashMgmt.UI.RenderForeignTotal',
          classes: 'obObposcashmgmtUiRenderDepositsDrops-container4-container1-container1-foreignTotal'
        }]
      }, {
        classes: 'obObposcashmgmtUiRenderDepositsDrops-container4-container1-container2',
        components: [{
          name: 'total',
          kind: 'OB.OBPOSCashMgmt.UI.RenderTotal',
          classes: 'obObposcashmgmtUiRenderDepositsDrops-container4-container1-container2-total'
        }]
      }]
    }]
  }],
  create: function () {
    var transactionsArray = this.model.get('listdepositsdrops'),
        transactionsCollection = new Backbone.Collection(transactionsArray),
        total;

    var fromCurrencyId = OB.MobileApp.model.paymentnames[this.model.attributes.searchKey].paymentMethod.currency;

    this.inherited(arguments);

    total = OB.DEC.add(0, this.model.get('startingCash'));
    total = OB.DEC.add(total, this.model.get('totalSales'));
    total = OB.DEC.sub(total, OB.DEC.abs(this.model.get('totalReturns')));
    var totalDeposits = _.reduce(transactionsArray, function (accum, trx) {
      if (trx.get('type') === 'deposit') {
        return OB.DEC.add(accum, trx.get('amount'));
      } else {
        return OB.DEC.sub(accum, OB.DEC.abs(trx.get('amount')));
      }
    }, 0);
    total = OB.DEC.add(total, totalDeposits);

    this.$.availableLbl.setContent(OB.I18N.getLabel('OBPOS_LblNewAvailableIn') + ' ' + this.model.get('name'));
    if (OB.UTIL.currency.isDefaultCurrencyId(fromCurrencyId)) {
      this.model.set('total', total, {
        silent: true // prevents triggering change event
      });
      this.$.total.setTotal(total);
    } else {
      var foreignTotal = OB.UTIL.currency.toDefaultCurrency(fromCurrencyId, total);
      this.model.set('total', foreignTotal, {
        silent: true // prevents triggering change event
      });
      this.$.total.setTotal(foreignTotal);
      if (foreignTotal > 0) {
        this.$.foreignTotal.setTextForeignTotal('(' + OB.I18N.formatCurrency(total) + ' ' + this.model.get('isocode') + ')');
        this.$.foreignTotal.setForeignTotal(total);
      }
    }

    this.$.theList.setCollection(transactionsCollection);

    this.$.startingCashPayName.setContent(OB.I18N.getLabel('OBPOS_LblStarting') + ' ' + this.model.get('name'));
    var startingCash = OB.DEC.add(0, this.model.get('startingCash'));
    this.$.startingCashAmnt.setContent(OB.I18N.formatCurrency(OB.UTIL.currency.toDefaultCurrency(fromCurrencyId, startingCash)));
    if ((OB.UTIL.currency.isDefaultCurrencyId(fromCurrencyId) === false) && (startingCash > 0)) {
      this.$.startingCashForeignAmnt.setContent('(' + OB.I18N.formatCurrency(startingCash) + ' ' + this.model.get('isocode') + ')');
    }

    this.$.tenderedLbl.setContent(OB.I18N.getLabel('OBPOS_LblTotalTendered') + ' ' + this.model.get('name'));
    var totalSalesReturns = OB.DEC.add(0, OB.DEC.sub(this.model.get('totalSales'), this.model.get('totalReturns')));
    if ((OB.UTIL.currency.isDefaultCurrencyId(fromCurrencyId) === false) && (totalSalesReturns > 0)) {
      this.$.tenderedForeignAmnt.setContent('(' + OB.I18N.formatCurrency(totalSalesReturns) + ' ' + this.model.get('isocode') + ')');
    }
    this.$.tenderedAmnt.setContent(OB.I18N.formatCurrency(OB.UTIL.currency.toDefaultCurrency(fromCurrencyId, totalSalesReturns)));

  }
});

//Renders the summary of deposits/drops and contains a list (OB.OBPOSCashMgmt.UI.RenderDepositsDrops)
//with detailed information for each payment type
enyo.kind({
  name: 'OB.OBPOSCashMgmt.UI.ListDepositsDrops',
  classes: 'obObposcashmgmtUiListDepositsDrops',
  components: [{
    classes: 'obObposcashmgmtUiListDepositsDrops-container1',
    components: [{
      classes: 'obObposcashmgmtUiListDepositsDrops-container1-container1',
      components: [{
        classes: 'obObposcashmgmtUiListDepositsDrops-container1-container1-container1 row-fluid',
        components: [{
          classes: 'obObposcashmgmtUiListDepositsDrops-container1-container1-container1-container1 span12',
          components: [{
            name: 'titleLbl',
            classes: 'obObposcashmgmtUiListDepositsDrops-container1-container1-container1-container1-titleLbl'
          }, {
            name: 'userName',
            classes: 'obObposcashmgmtUiListDepositsDrops-container1-container1-container1-container1-userName'
          }, {
            name: 'time',
            classes: 'obObposcashmgmtUiListDepositsDrops-container1-container1-container1-container1-time'
          }, {
            name: 'store',
            classes: 'obObposcashmgmtUiListDepositsDrops-container1-container1-container1-container1-store'
          }, {
            name: 'terminal',
            classes: 'obObposcashmgmtUiListDepositsDrops-container1-container1-container1-container1-terminal'
          }, {
            name: 'depositDropsList',
            kind: 'OB.UI.Table',
            classes: 'obObposcashmgmtUiListDepositsDrops-container1-container1-container1-container1-depositDropsList',
            renderLine: 'OB.OBPOSCashMgmt.UI.RenderDepositsDrops',
            renderEmpty: 'enyo.Control',
            listStyle: 'list'
          }]
        }]
      }]
    }]
  }],
  create: function () {
    var now = new Date();
    this.inherited(arguments);
    this.$.userName.setContent(OB.I18N.getLabel('OBPOS_LblUser') + ': ' + OB.MobileApp.model.get('context').user._identifier);
    this.$.time.setContent(OB.I18N.getLabel('OBPOS_LblTime') + ': ' + OB.I18N.formatDate(now) + ' ' + OB.I18N.formatHour(now, true));
    this.$.store.setContent(OB.I18N.getLabel('OBPOS_LblStore') + ': ' + OB.MobileApp.model.get('terminal').organization$_identifier);
    this.$.terminal.setContent(OB.I18N.getLabel('OBPOS_LblTerminal') + ': ' + OB.MobileApp.model.get('terminal')._identifier);
  },
  init: function (model) {
    // this.owner is the window (OB.UI.WindowView)
    // this.parent is the DOM object on top of the list (usually a DIV)
    this.model = model;
    this.$.depositDropsList.setCollection(this.model.get('payments'));
    this.$.titleLbl.setContent(OB.I18N.getLabel('OBPOS_LblCashManagement'));
  }
});