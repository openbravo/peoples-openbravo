/*
 ************************************************************************************
 * Copyright (C) 2012-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo */

// Renders lines of deposits/drops
enyo.kind({
  name: 'OB.OBPOSCashMgmt.UI.RenderDepositLine',
  classes: 'obObposcashmgmtUiRenderDepositLine',
  components: [
    {
      classes: 'obObposcashmgmtUiRenderDepositLine-wrapper',
      components: [
        {
          name: 'description',
          classes: 'obObposcashmgmtUiRenderDepositLine-wrapper-description'
        },
        {
          name: 'user',
          classes: 'obObposcashmgmtUiRenderDepositLine-wrapper-user'
        },
        {
          name: 'time',
          classes: 'obObposcashmgmtUiRenderDepositLine-wrapper-time'
        },
        {
          name: 'separator',
          content: '-',
          classes: 'obObposcashmgmtUiRenderDepositLine-wrapper-separator'
        },
        {
          name: 'foreignAmt',
          classes: 'obObposcashmgmtUiRenderDepositLine-wrapper-foreignAmt',
          content: ''
        },
        {
          name: 'amt',
          classes: 'obObposcashmgmtUiRenderDepositLine-wrapper-amt'
        }
      ]
    }
  ],
  create: function() {
    var amnt, foreignAmt, lbl;

    this.inherited(arguments);
    if (this.model.get('type') === 'drop') {
      lbl = OB.I18N.getLabel('OBPOS_LblWithdrawal') + ': ';
      if (this.model.get('origAmount') !== this.model.get('amount')) {
        foreignAmt = OB.I18N.formatCurrency(
          OB.DEC.add(0, this.model.get('amount'))
        );
        amnt = OB.I18N.formatCurrency(this.model.get('origAmount'));
      } else {
        amnt = OB.I18N.formatCurrency(OB.DEC.add(0, this.model.get('amount')));
      }
    } else {
      lbl = OB.I18N.getLabel('OBPOS_LblDeposit') + ': ';
      if (this.model.get('origAmount') !== this.model.get('amount')) {
        foreignAmt = OB.I18N.formatCurrency(
          OB.DEC.add(0, this.model.get('amount'))
        );
        amnt = OB.I18N.formatCurrency(this.model.get('origAmount'));
      } else {
        amnt = OB.I18N.formatCurrency(OB.DEC.add(0, this.model.get('amount')));
      }
    }
    var creationDate = new Date(this.model.get('creationDate'));
    this.$.description.setContent(lbl + this.model.get('description'));
    this.$.user.setContent(this.model.get('user'));
    this.$.time.setContent(
      OB.UTIL.padNumber(creationDate.getHours(), 2) +
        ':' +
        OB.UTIL.padNumber(creationDate.getMinutes(), 2)
    );
    if (
      foreignAmt &&
      ((this.model.get('rate') && this.model.get('rate') !== '1') ||
        amnt !== foreignAmt)
    ) {
      this.$.foreignAmt.setContent(
        '(' + foreignAmt + ' ' + this.model.get('isocode') + ')'
      );
    }
    this.$.amt.setContent(amnt);
  }
});

enyo.kind({
  name: 'OB.OBPOSCashMgmt.UI.RenderForeignTotal',
  classes: 'obObposcashmgmtUiRenderForeignTotal',
  published: {
    foreignTotal: null,
    textForeignTotal: ''
  },
  create: function() {
    this.inherited(arguments);
    this.owner.model.on(
      'change:total',
      function(model) {
        this.setForeignTotal(model.get('total'));
      },
      this
    );
  },
  foreignTotalChanged: function(oldValue) {
    this.setContent(this.textForeignTotal);
    if (OB.DEC.compare(this.foreignTotal) < 0) {
      this.parent.addClass('negative');
    } else {
      this.parent.removeClass('negative');
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSCashMgmt.UI.RenderTotal',
  classes: 'obObposcashmgmtUiRenderTotal',
  published: {
    total: null
  },
  create: function() {
    this.inherited(arguments);
    this.owner.model.on(
      'change:total',
      function(model) {
        this.setTotal(model.get('total'));
      },
      this
    );
  },
  totalChanged: function(oldValue) {
    this.setContent(OB.I18N.formatCurrency(this.total));
    if (OB.DEC.compare(this.total) < 0) {
      this.parent.addClass('negative');
    } else {
      this.parent.removeClass('negative');
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
      classes: 'obObposcashmgmtUiRenderDepositsDrops-row1'
    },

    // Total per payment type
    {
      classes: 'obObposcashmgmtUiRenderDepositsDrops-row2',
      components: [
        {
          name: 'startingCashPayName',
          classes:
            'obObposcashmgmtUiRenderDepositsDrops-row2-startingCashPayName'
        },
        {
          name: 'startingCashForeignAmnt',
          classes:
            'obObposcashmgmtUiRenderDepositsDrops-row2-startingCashForeignAmnt',
          content: ''
        },
        {
          name: 'startingCashAmnt',
          classes: 'obObposcashmgmtUiRenderDepositsDrops-row2-startingCashAmnt'
        }
      ]
    },

    // Tendered per payment type
    {
      classes: 'obObposcashmgmtUiRenderDepositsDrops-row3',
      components: [
        {
          name: 'tenderedLbl',
          classes: 'obObposcashmgmtUiRenderDepositsDrops-row3-tenderedLbl'
        },
        {
          name: 'tenderedForeignAmnt',
          classes:
            'obObposcashmgmtUiRenderDepositsDrops-row3-tenderedForeignAmnt',
          content: ''
        },
        {
          name: 'tenderedAmnt',
          classes: 'obObposcashmgmtUiRenderDepositsDrops-row3-tenderedAmnt'
        }
      ]
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
      components: [
        {
          classes: 'obObposcashmgmtUiRenderDepositsDrops-row4',
          components: [
            {
              name: 'availableLbl',
              classes: 'obObposcashmgmtUiRenderDepositsDrops-row4-availableLbl'
            },
            {
              name: 'foreignTotal',
              kind: 'OB.OBPOSCashMgmt.UI.RenderForeignTotal',
              classes: 'obObposcashmgmtUiRenderDepositsDrops-row4-foreignTotal'
            },
            {
              name: 'total',
              kind: 'OB.OBPOSCashMgmt.UI.RenderTotal',
              classes: 'obObposcashmgmtUiRenderDepositsDrops-row4-total'
            }
          ]
        }
      ]
    }
  ],
  create: function() {
    const transactionsCollection = new OB.Collection.CashManagementList();
    OB.App.State.Cashup.Utils.getCashManagementsByPaymentMethodId(
      OB.App.State.getState().Cashup.cashPaymentMethodInfo,
      this.model.get('paymentmethod_id')
    ).forEach(cashManagement =>
      transactionsCollection.add(
        OB.Dal.transform(OB.Model.CashManagement, cashManagement)
      )
    );

    var total;

    var fromCurrencyId =
      OB.MobileApp.model.paymentnames[this.model.attributes.searchKey]
        .paymentMethod.currency;

    this.inherited(arguments);

    total = OB.DEC.add(0, this.model.get('startingCash'));
    total = OB.DEC.add(total, this.model.get('totalSales'));
    total = OB.DEC.sub(total, OB.DEC.abs(this.model.get('totalReturns')));
    var totalDeposits = _.reduce(
      transactionsCollection.models,
      function(accum, trx) {
        if (trx.get('type') === 'deposit') {
          return OB.DEC.add(accum, trx.get('amount'));
        } else {
          return OB.DEC.sub(accum, OB.DEC.abs(trx.get('amount')));
        }
      },
      0
    );
    total = OB.DEC.add(total, totalDeposits);

    this.$.availableLbl.setContent(
      OB.I18N.getLabel('OBPOS_LblNewAvailableIn') + ' ' + this.model.get('name')
    );

    if (OB.UTIL.currency.isDefaultCurrencyId(fromCurrencyId)) {
      this.model.set('total', total, {
        silent: true // prevents triggering change event
      });
      this.$.total.setTotal(total);
    } else {
      var foreignTotal = OB.UTIL.currency.toDefaultCurrency(
        fromCurrencyId,
        total
      );
      this.model.set('total', foreignTotal, {
        silent: true // prevents triggering change event
      });
      this.$.total.setTotal(foreignTotal);
      if (foreignTotal > 0) {
        this.$.foreignTotal.setTextForeignTotal(
          '(' +
            OB.I18N.formatCurrency(total) +
            ' ' +
            this.model.get('isocode') +
            ')'
        );
        this.$.foreignTotal.setForeignTotal(total);
      }
    }

    this.$.theList.setCollection(transactionsCollection);

    if (!this.model.attributes.issafebox) {
      this.$.startingCashPayName.setContent(
        OB.I18N.getLabel('OBPOS_LblStarting') + ' ' + this.model.get('name')
      );
      var startingCash = OB.DEC.add(0, this.model.get('startingCash'));
      this.$.startingCashAmnt.setContent(
        OB.I18N.formatCurrency(
          OB.UTIL.currency.toDefaultCurrency(fromCurrencyId, startingCash)
        )
      );
      if (
        OB.UTIL.currency.isDefaultCurrencyId(fromCurrencyId) === false &&
        startingCash > 0
      ) {
        this.$.startingCashForeignAmnt.setContent(
          '(' +
            OB.I18N.formatCurrency(startingCash) +
            ' ' +
            this.model.get('isocode') +
            ')'
        );
      }
    } else {
      this.$.startingCashPayName.parent.setStyle('display:none');
    }

    this.$.tenderedLbl.setContent(
      OB.I18N.getLabel('OBPOS_LblTotalTendered') + ' ' + this.model.get('name')
    );
    var totalSalesReturns = OB.DEC.add(
      0,
      OB.DEC.sub(this.model.get('totalSales'), this.model.get('totalReturns'))
    );
    if (
      OB.UTIL.currency.isDefaultCurrencyId(fromCurrencyId) === false &&
      totalSalesReturns > 0
    ) {
      this.$.tenderedForeignAmnt.setContent(
        '(' +
          OB.I18N.formatCurrency(totalSalesReturns) +
          ' ' +
          this.model.get('isocode') +
          ')'
      );
    }
    this.$.tenderedAmnt.setContent(
      OB.I18N.formatCurrency(
        OB.UTIL.currency.toDefaultCurrency(fromCurrencyId, totalSalesReturns)
      )
    );
  }
});

//Renders the summary of deposits/drops and contains a list (OB.OBPOSCashMgmt.UI.RenderDepositsDrops)
//with detailed information for each payment type
enyo.kind({
  name: 'OB.OBPOSCashMgmt.UI.ListDepositsDrops',
  classes: 'obObposcashmgmtUiListDepositsDrops',
  components: [
    {
      classes: 'obObposcashmgmtUiListDepositsDrops-wrapper',
      components: [
        {
          classes: 'obObposcashmgmtUiListDepositsDrops-wrapper-components',
          components: [
            {
              classes:
                'obObposcashmgmtUiListDepositsDrops-wrapper-components-header',
              components: [
                {
                  name: 'titleLbl',
                  classes:
                    'obObposcashmgmtUiListDepositsDrops-wrapper-components-header-titleLbl'
                },
                {
                  name: 'userName',
                  classes:
                    'obObposcashmgmtUiListDepositsDrops-wrapper-components-header-userName'
                },
                {
                  name: 'time',
                  classes:
                    'obObposcashmgmtUiListDepositsDrops-wrapper-components-header-time'
                },
                {
                  name: 'store',
                  classes:
                    'obObposcashmgmtUiListDepositsDrops-wrapper-components-header-store'
                },
                {
                  name: 'terminal',
                  classes:
                    'obObposcashmgmtUiListDepositsDrops-wrapper-components-header-terminal'
                }
              ]
            },
            {
              name: 'depositDropsList',
              kind: 'OB.UI.Table',
              classes:
                'obObposcashmgmtUiListDepositsDrops-wrapper-components-depositDropsList',
              renderLine: 'OB.OBPOSCashMgmt.UI.RenderDepositsDrops',
              renderEmpty: 'enyo.Control',
              listStyle: 'list'
            }
          ]
        }
      ]
    }
  ],
  create: function() {
    var now = new Date();
    this.inherited(arguments);
    this.$.userName.setContent(
      OB.I18N.getLabel('OBPOS_LblUser') +
        ': ' +
        OB.MobileApp.model.get('context').user._identifier
    );
    this.$.time.setContent(
      OB.I18N.getLabel('OBPOS_LblTime') +
        ': ' +
        OB.I18N.formatDate(now) +
        ' ' +
        OB.I18N.formatHour(now, true)
    );
    this.$.store.setContent(
      OB.I18N.getLabel('OBPOS_LblStore') +
        ': ' +
        OB.MobileApp.model.get('terminal').organization$_identifier
    );
    this.$.terminal.setContent(
      OB.I18N.getLabel('OBPOS_LblTerminal') +
        ': ' +
        OB.MobileApp.model.get('terminal')._identifier
    );
  },
  init: function(model) {
    // this.owner is the window (OB.UI.WindowView)
    // this.parent is the DOM object on top of the list (usually a DIV)
    this.model = model;
    this.$.depositDropsList.setCollection(this.model.get('payments'));
    this.$.titleLbl.setContent(OB.I18N.getLabel('OBPOS_LblCashManagement'));
  }
});
