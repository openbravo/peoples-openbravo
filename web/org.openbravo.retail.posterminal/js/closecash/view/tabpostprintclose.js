/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, _ */

//Renders the summary of deposits/drops and contains a list (OB.OBPOSCasgMgmt.UI.RenderDepositsDrops)
//with detailed information for each payment typ
enyo.kind({
  name: 'OB.OBPOSCashUp.UI.ppc_lineSeparator',
  classes: 'row-fluid',
  components: [{
    classes: 'span12',
    components: [{
      style: 'width: 10%; float: left;',
      components: [{
        allowHtml: true,
        tag: 'span',
        content: '&nbsp;'
      }]
    }, {
      style: 'padding: 5px 0px 0px 5px; float: left; width: 60%; font-weight:bold;',
      components: [{
        style: 'clear:both;'
      }]
    }]
  }, {
    style: 'clear:both;'
  }]
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.ppc_totalsLine',
  classes: 'row-fluid',
  label: '',
  value: '',
  components: [{
    classes: 'span12',
    components: [{
      style: 'width: 10%; float: left;',
      components: [{
        allowHtml: true,
        tag: 'span',
        content: '&nbsp;'
      }]
    }, {
      name: 'totalLbl',
      style: 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 55%; font-weight:bold;'
    }, {
      name: 'totalQty',
      style: 'padding: 5px 0px 0px 5px; border-bottom: 1px solid #cccccc; border-top: 1px solid #cccccc; float: left; width: 20%; text-align:right; font-weight:bold;'
    }, {
      style: 'width: 10%; float: left;',
      components: [{
        allowHtml: true,
        tag: 'span',
        content: '&nbsp;'
      }]
    }]
  }, {
    style: 'clear:both;'
  }],
  setValue: function (value) {
    this.value = value;
    this.render();
  },
  render: function () {
    if (this.label) {
      this.$.totalLbl.setContent(this.label);
    } else {
      this.$.totalLbl.setContent(OB.I18N.getLabel(this.i18nLabel));
    }
    this.$.totalQty.setContent(OB.I18N.formatCurrency(OB.DEC.add(0, this.value)));
  },
  initComponents: function () {
    this.inherited(arguments);
    if ((this.label || this.i18nLabel) && this.value) {
      if (this.i18nLabel) {
        this.$.totalLbl.setContent(OB.I18N.getLabel(this.i18nLabel));
      } else {
        this.$.totalLbl.setContent(this.label);
      }
      this.$.totalQty.setContent(this.value);
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.ppc_itemLine',
  label: '',
  value: '',
  classes: 'row-fluid',
  convertedValues: ['expected', 'counted', 'difference', 'qtyToKeep', 'qtyToDepo'],
  valuestoConvert: ['deposits', 'drops', 'startings'],
  components: [{
    classes: 'span12',
    components: [{
      style: 'width: 10%; float: left;',
      components: [{
        allowHtml: true,
        tag: 'span',
        content: '&nbsp;'
      }]
    }, {
      name: 'itemLbl',
      allowHtml: true,
      style: 'padding: 5px 0px 0px 5px;  border-top: 1px solid #cccccc; float: left; width: 35%'
    }, {
      name: 'foreignItemQty',
      style: 'padding: 5px 0px 0px 0px;  border-top: 1px solid #cccccc; float: left; width: 20%; text-align:right;'
    }, {
      name: 'itemQty',
      style: 'padding: 5px 0px 0px 5px;  border-top: 1px solid #cccccc; border-left: 1px solid #cccccc; float: left; width: 20%; text-align:right;'
    }, {
      style: 'width: 10%; float: left;',
      components: [{
        allowHtml: true,
        tag: 'span',
        content: '&nbsp;'
      }]
    }]
  }, {
    style: 'clear:both;'
  }],
  setValue: function (value) {
    this.value = value;
    this.render();
  },
  render: function () {
    if (this.i18nLabel) {
      this.$.itemLbl.setContent(OB.I18N.getLabel(this.i18nLabel));
    } else {
      this.$.itemLbl.setContent(this.label);
    }
    this.$.itemQty.setContent(OB.I18N.formatCurrency(OB.DEC.add(0, this.value)));

    if (this.convertedValue && this.convertedValue !== this.value && this.convertedValues.indexOf(this.type) !== -1) {
      this.$.foreignItemQty.setContent('(' + OB.I18N.formatCurrency(this.convertedValue) + ' ' + this.isocode + ')');
    } else if (this.valueToConvert && this.value !== this.valueToConvert && this.valuestoConvert.indexOf(this.type) !== -1) {
      if (this.value) {
        this.$.foreignItemQty.setContent('(' + OB.I18N.formatCurrency(this.value) + ' ' + this.isocode + ')');
      }
      this.$.itemQty.setContent(OB.I18N.formatCurrency(this.valueToConvert));
    } else {
      this.$.foreignItemQty.setContent('');
    }
  },
  create: function () {
    if (this.model && this.model.get('searchKey')) {
      // automated test
      this.name = this.model.get('searchKey');
    }
    this.inherited(arguments);
    if (this.model) {
      this.label = this.model.get(this.lblProperty);
      this.value = this.model.get(this.qtyProperty);
      this.type = this.owner.typeProperty;
      this.valueToConvert = this.model.get('origAmount');
      this.convertedValue = this.model.get('second');
      this.rate = this.model.get('rate');
      this.isocode = this.model.get('isocode');

      // DEVELOPER: This two rules must be followed if you want this kind to keep data meaningfulness
      //            Each type is related to either convertedValues or valuestoConvert
      if (this.convertedValue && this.convertedValues.indexOf(this.type) === -1) {
        OB.warn("DEVELOPER: the type '" + this.type + "' is being incorrectly implemented.");
      }
      if (this.valueToConvert && this.valuestoConvert.indexOf(this.type) === -1) {
        OB.warn("DEVELOPER: the type '" + this.type + "' is being incorrectly implemented.");
      }
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.ppc_collectionLines',
  kind: 'OB.UI.iterateArray',
  renderLine: 'OB.OBPOSCashUp.UI.ppc_itemLine',
  renderEmpty: 'OB.UI.RenderEmpty'
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.ppc_table',
  setValue: function (name, value) {
    this.$[name].setValue(value);
  }
});

enyo.kind({
  kind: 'OB.OBPOSCashUp.UI.ppc_table',
  name: 'OB.OBPOSCashUp.UI.ppc_salesTable',
  components: [{
    kind: 'OB.OBPOSCashUp.UI.ppc_itemLine',
    name: 'netsales',
    i18nLabel: 'OBPOS_LblNetSales'
  }, {
    kind: 'OB.OBPOSCashUp.UI.ppc_collectionLines',
    name: 'salestaxes',
    lblProperty: 'name',
    qtyProperty: 'amount'
  }, {
    kind: 'OB.OBPOSCashUp.UI.ppc_totalsLine',
    name: 'totalsales',
    i18nLabel: 'OBPOS_LblGrossSales'
  }, {
    kind: 'OB.OBPOSCashUp.UI.ppc_lineSeparator',
    name: 'separator'
  }],
  setCollection: function (col) {
    this.$.salestaxes.setCollection(col);
  }
});

enyo.kind({
  kind: 'OB.OBPOSCashUp.UI.ppc_table',
  name: 'OB.OBPOSCashUp.UI.ppc_returnsTable',
  components: [{
    kind: 'OB.OBPOSCashUp.UI.ppc_itemLine',
    name: 'netreturns',
    i18nLabel: 'OBPOS_LblNetReturns'
  }, {
    kind: 'OB.OBPOSCashUp.UI.ppc_collectionLines',
    name: 'retunrnstaxes',
    lblProperty: 'name',
    qtyProperty: 'amount'
  }, {
    kind: 'OB.OBPOSCashUp.UI.ppc_totalsLine',
    name: 'totalreturns',
    i18nLabel: 'OBPOS_LblGrossReturns'
  }, {
    kind: 'OB.OBPOSCashUp.UI.ppc_lineSeparator',
    name: 'separator'
  }],
  setCollection: function (col) {
    this.$.retunrnstaxes.setCollection(col);
  }
});

enyo.kind({
  kind: 'OB.OBPOSCashUp.UI.ppc_table',
  name: 'OB.OBPOSCashUp.UI.ppc_totalTransactionsTable',
  components: [{
    kind: 'OB.OBPOSCashUp.UI.ppc_totalsLine',
    name: 'totaltransactionsline',
    i18nLabel: 'OBPOS_LblTotalRetailTrans',
    init: function () {
      if (OB.POS.modelterminal.get('terminal').ismaster || OB.POS.modelterminal.get('terminal').isslave) {
        this.label = OB.I18N.getLabel('OBPOS_LblTotalRetailTransLocal');
      }
    }
  }, {
    kind: 'OB.OBPOSCashUp.UI.ppc_lineSeparator',
    name: 'separator'
  }]
});

enyo.kind({
  kind: 'OB.OBPOSCashUp.UI.ppc_table',
  name: 'OB.OBPOSCashUp.UI.ppc_cashDropsTable',
  components: [{
    kind: 'OB.OBPOSCashUp.UI.ppc_collectionLines',
    name: 'drops',
    lblProperty: 'description',
    qtyProperty: 'amount',
    typeProperty: 'drops'
  }, {
    kind: 'OB.OBPOSCashUp.UI.ppc_totalsLine',
    name: 'totaldrops',
    i18nLabel: 'OBPOS_LblTotalWithdrawals'
  }, {
    kind: 'OB.OBPOSCashUp.UI.ppc_lineSeparator',
    name: 'separator'
  }],
  setCollection: function (col) {
    this.$.drops.setCollection(col);
  }
});

enyo.kind({
  kind: 'OB.OBPOSCashUp.UI.ppc_table',
  name: 'OB.OBPOSCashUp.UI.ppc_cashDepositsTable',
  components: [{
    kind: 'OB.OBPOSCashUp.UI.ppc_collectionLines',
    name: 'deposits',
    lblProperty: 'description',
    qtyProperty: 'amount',
    typeProperty: 'deposits'
  }, {
    kind: 'OB.OBPOSCashUp.UI.ppc_totalsLine',
    name: 'totaldeposits',
    i18nLabel: 'OBPOS_LblTotalDeposits'
  }, {
    kind: 'OB.OBPOSCashUp.UI.ppc_lineSeparator',
    name: 'separator'
  }],
  setCollection: function (col) {
    this.$.deposits.setCollection(col);
  }
});

enyo.kind({
  kind: 'OB.OBPOSCashUp.UI.ppc_table',
  name: 'OB.OBPOSCashUp.UI.ppc_startingsTable',
  components: [{
    kind: 'OB.OBPOSCashUp.UI.ppc_collectionLines',
    name: 'startings',
    lblProperty: 'description',
    qtyProperty: 'amount',
    typeProperty: 'startings'
  }, {
    kind: 'OB.OBPOSCashUp.UI.ppc_totalsLine',
    name: 'totalstartings',
    i18nLabel: 'OBPOS_LblTotalStarting'
  }, {
    kind: 'OB.OBPOSCashUp.UI.ppc_lineSeparator',
    name: 'separator'
  }],
  setCollection: function (col) {
    this.$.startings.setCollection(col);
  }
});

enyo.kind({
  kind: 'OB.OBPOSCashUp.UI.ppc_table',
  name: 'OB.OBPOSCashUp.UI.ppc_cashExpectedTable',
  components: [{
    kind: 'OB.OBPOSCashUp.UI.ppc_collectionLines',
    name: 'expectedPerPayment',
    lblProperty: 'name',
    qtyProperty: 'value',
    typeProperty: 'expected'
  }, {
    kind: 'OB.OBPOSCashUp.UI.ppc_totalsLine',
    name: 'totalexpected',
    i18nLabel: 'OBPOS_LblTotalExpected'
  }, {
    kind: 'OB.OBPOSCashUp.UI.ppc_lineSeparator',
    name: 'separator'
  }],
  setCollection: function (col) {
    this.$.expectedPerPayment.setCollection(col);
  }
});

enyo.kind({
  kind: 'OB.OBPOSCashUp.UI.ppc_table',
  name: 'OB.OBPOSCashUp.UI.ppc_cashDifferenceTable',
  components: [{
    kind: 'OB.OBPOSCashUp.UI.ppc_collectionLines',
    name: 'differencePerPayment',
    lblProperty: 'name',
    qtyProperty: 'value',
    typeProperty: 'difference'
  }, {
    kind: 'OB.OBPOSCashUp.UI.ppc_totalsLine',
    name: 'totaldifference',
    i18nLabel: 'OBPOS_LblTotalDifference'
  }, {
    kind: 'OB.OBPOSCashUp.UI.ppc_lineSeparator',
    name: 'separator'
  }],
  setCollection: function (col) {
    this.$.differencePerPayment.setCollection(col);
  }
});

enyo.kind({
  kind: 'OB.OBPOSCashUp.UI.ppc_table',
  name: 'OB.OBPOSCashUp.UI.ppc_cashCountedTable',
  components: [{
    kind: 'OB.OBPOSCashUp.UI.ppc_collectionLines',
    name: 'countedPerPayment',
    lblProperty: 'name',
    qtyProperty: 'value',
    typeProperty: 'counted'
  }, {
    kind: 'OB.OBPOSCashUp.UI.ppc_totalsLine',
    name: 'totalcounted',
    i18nLabel: 'OBPOS_LblTotalCounted'
  }, {
    kind: 'OB.OBPOSCashUp.UI.ppc_lineSeparator',
    name: 'separator'
  }],
  setCollection: function (col) {
    this.$.countedPerPayment.setCollection(col);
  }
});

enyo.kind({
  kind: 'OB.OBPOSCashUp.UI.ppc_table',
  name: 'OB.OBPOSCashUp.UI.ppc_cashQtyToKeepTable',
  components: [{
    kind: 'OB.OBPOSCashUp.UI.ppc_collectionLines',
    name: 'qtyToKeepPerPayment',
    lblProperty: 'name',
    qtyProperty: 'value',
    typeProperty: 'qtyToKeep'
  }, {
    kind: 'OB.OBPOSCashUp.UI.ppc_totalsLine',
    name: 'totalqtyToKeep',
    i18nLabel: 'OBPOS_LblTotalQtyToKeep'
  }, {
    kind: 'OB.OBPOSCashUp.UI.ppc_lineSeparator',
    name: 'separator'
  }],
  setCollection: function (col) {
    this.$.qtyToKeepPerPayment.setCollection(col);
  }
});

enyo.kind({
  kind: 'OB.OBPOSCashUp.UI.ppc_table',
  name: 'OB.OBPOSCashUp.UI.ppc_cashQtyToDepoTable',
  components: [{
    kind: 'OB.OBPOSCashUp.UI.ppc_collectionLines',
    name: 'qtyToDepoPerPayment',
    lblProperty: 'name',
    qtyProperty: 'value',
    typeProperty: 'qtyToDepo'
  }, {
    kind: 'OB.OBPOSCashUp.UI.ppc_totalsLine',
    name: 'totalqtyToDepo',
    i18nLabel: 'OBPOS_LblTotalQtyToDepo'
  }, {
    kind: 'OB.OBPOSCashUp.UI.ppc_lineSeparator',
    name: 'separator'
  }],
  setCollection: function (col) {
    this.$.qtyToDepoPerPayment.setCollection(col);
  }
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.PostPrintClose',
  published: {
    model: null,
    summary: null
  },
  classes: 'tab-pane',
  components: [{
    kind: 'Scroller',
    name: 'scrollArea',
    thumb: true,
    maxHeight: '612px',
    horizontal: 'hidden',
    style: 'margin: 5px; background-color: #ffffff;',
    components: [{
      style: 'padding: 5px;',
      components: [{
        classes: 'row-fluid',
        components: [{
          classes: 'span12',
          components: [{
            name: 'reporttitle',
            style: 'padding: 10px; border-bottom: 1px solid #cccccc; text-align:center;',
            renderHeader: function (step, count) {
              this.setContent(OB.I18N.getLabel('OBPOS_LblStepNumber', [step, count]) + " " + OB.I18N.getLabel('OBPOS_LblStepPostPrintAndClose') + OB.OBPOSCashUp.UI.CashUp.getTitleExtensions());
            }
          }]
        }, {
          style: 'clear:both;'
        }]
      }, {
        classes: 'row-fluid',
        components: [{
          classes: 'span12',
          components: [{
            style: 'padding: 10px; text-align:center;',
            components: [{
              tag: 'img',
              style: 'padding: 20px;',
              initComponents: function () {
                if (OB.MobileApp.model.get('terminal').organizationImage) {
                  this.setAttribute('src', 'data:' + OB.MobileApp.model.get('terminal').organizationImageMime + ';base64,' + OB.MobileApp.model.get('terminal').organizationImage);
                }
              }
            }, {
              name: 'store',
              style: 'padding: 5px; text-align:center;'
            }, {
              name: 'terminal',
              style: 'padding: 5px; text-align:center;'
            }, {
              name: 'user',
              style: 'padding: 5px; text-align:center;'
            }, {
              name: 'openingtime',
              style: 'padding: 5px; text-align:center;'
            }, {
              name: 'time',
              style: 'padding: 5px; text-align:center;'
            }, {
              style: 'padding: 0px 0px 10px 0px;'
            }]
          }]
        }, {
          style: 'clear:both;'
        }]
      },
      //FIXME: Iterate taxes
      {
        classes: 'row-fluid',
        components: [{
          tag: 'ul',
          classes: 'unstyled',
          style: 'display:block',
          components: [{
            tag: 'li',
            classes: 'selected',
            components: [{
              kind: 'OB.OBPOSCashUp.UI.ppc_salesTable',
              name: 'sales'
            }, {
              kind: 'OB.OBPOSCashUp.UI.ppc_returnsTable',
              name: 'returns'
            }, {
              kind: 'OB.OBPOSCashUp.UI.ppc_totalTransactionsTable',
              name: 'totaltransactions'
            }]
          }]
        }, {
          style: 'border-bottom-width: 1px; border-bottom-style: solid; border-bottom-color: rgb(204, 204, 204); padding-top: 15px; padding-right: 15px; padding-bottom: 15px; padding-left: 15px; font-weight: bold; color: rgb(204, 204, 204); display: none;'
        }, {
          style: 'display:none;'
        }]
      }, {
        classes: 'row-fluid',
        components: [{
          kind: 'OB.OBPOSCashUp.UI.ppc_startingsTable',
          name: 'startingsTable'
        }, {
          kind: 'OB.OBPOSCashUp.UI.ppc_cashDropsTable',
          name: 'dropsTable'
        }, {
          kind: 'OB.OBPOSCashUp.UI.ppc_cashDepositsTable',
          name: 'depositsTable'
        }, {
          kind: 'OB.OBPOSCashUp.UI.ppc_cashExpectedTable',
          name: 'expectedTable'
        }, {
          kind: 'OB.OBPOSCashUp.UI.ppc_cashCountedTable',
          name: 'countedTable'
        }, {
          kind: 'OB.OBPOSCashUp.UI.ppc_cashDifferenceTable',
          name: 'differenceTable'
        }, {
          kind: 'OB.OBPOSCashUp.UI.ppc_cashQtyToKeepTable',
          name: 'qtyToKeepTable'
        }, {
          kind: 'OB.OBPOSCashUp.UI.ppc_cashQtyToDepoTable',
          name: 'qtyToDepoTable'
        }]
      }]
    }]
  }],

  paymentWithMovement: [],

  create: function () {
    this.inherited(arguments);
    this.$.store.setContent(OB.I18N.getLabel('OBPOS_LblStore') + ': ' + OB.MobileApp.model.get('terminal').organization$_identifier);
    this.$.terminal.setContent(OB.I18N.getLabel('OBPOS_LblTerminal') + ': ' + OB.MobileApp.model.get('terminal')._identifier);
    this.$.user.setContent(OB.I18N.getLabel('OBPOS_LblUser') + ': ' + OB.MobileApp.model.get('context').user._identifier);
    this.$.time.setContent(OB.I18N.getLabel('OBPOS_LblCloseTime') + ': ' + OB.I18N.formatDate(new Date()) + ' - ' + OB.I18N.formatHour(new Date()));
  },

  init: function (model) {
    this.model = model;

    this.$.reporttitle.setContent(OB.I18N.getLabel(model.reportTitleLabel) + OB.OBPOSCashUp.UI.CashUp.getTitleExtensions());

    this.model.get('cashUpReport').on('add', function (cashUpReport) {
      this.$.openingtime.setContent(OB.I18N.getLabel('OBPOS_LblOpenTime') + ': ' + OB.I18N.formatDate(new Date(cashUpReport.get('creationDate'))) + ' - ' + OB.I18N.formatHour(new Date(cashUpReport.get('creationDate'))));
      this.$.sales.setValue('netsales', cashUpReport.get('netSales'));
      this.$.sales.setCollection(cashUpReport.get('salesTaxes'));
      this.$.sales.setValue('totalsales', cashUpReport.get('grossSales'));

      this.$.returns.setValue('netreturns', cashUpReport.get('netReturns'));
      this.$.returns.setCollection(cashUpReport.get('returnsTaxes'));
      this.$.returns.setValue('totalreturns', cashUpReport.get('grossReturns'));

      this.$.totaltransactions.setValue('totaltransactionsline', cashUpReport.get('totalRetailTransactions'));

      if (!OB.POS.modelterminal.get('terminal').ismaster) {
        this.cashUpReportChanged(cashUpReport);
      }
    }, this);

    this.model.on('change:time', function () {
      this.$.time.setContent(OB.I18N.getLabel('OBPOS_LblCloseTime') + ': ' + OB.I18N.formatDate(this.model.get('time')) + ' - ' + OB.I18N.formatHour(this.model.get('time')));
    }, this);
  },

  filterMovements: function (cashUpReport, isSummary) {
    var startings, drops, deposits, expectedSummary, countedSummary, differenceSummary, qtyToKeepSummary, qtyToDepoSummary;
    if (cashUpReport && OB.MobileApp.model.hasPermission('OBPOS_retail.cashupRemoveUnusedPayment', true)) {
      this.paymentWithMovement = [];
      if (isSummary) {
        OB.UTIL.cashupAddPaymentWithSummaryMovement(this.paymentWithMovement, this.summary.expectedSummary);
        if (OB.MobileApp.view.currentWindow !== 'retail.cashuppartial') {
          OB.UTIL.cashupAddPaymentWithSummaryMovement(this.paymentWithMovement, this.summary.countedSummary);
          OB.UTIL.cashupAddPaymentWithSummaryMovement(this.paymentWithMovement, this.summary.differenceSummary);
          OB.UTIL.cashupAddPaymentWithSummaryMovement(this.paymentWithMovement, this.summary.qtyToKeepSummary);
          OB.UTIL.cashupAddPaymentWithSummaryMovement(this.paymentWithMovement, this.summary.qtyToDepoSummary);
          countedSummary = OB.UTIL.cashupGetPaymentWithMovement(this.paymentWithMovement, this.summary.countedSummary);
          differenceSummary = OB.UTIL.cashupGetPaymentWithMovement(this.paymentWithMovement, this.summary.differenceSummary);
          qtyToKeepSummary = OB.UTIL.cashupGetPaymentWithMovement(this.paymentWithMovement, this.summary.qtyToKeepSummary);
          qtyToDepoSummary = OB.UTIL.cashupGetPaymentWithMovement(this.paymentWithMovement, this.summary.qtyToDepoSummary);
        }
        expectedSummary = OB.UTIL.cashupGetPaymentWithMovement(this.paymentWithMovement, this.summary.expectedSummary);
      } else {
        OB.UTIL.cashupAddPaymentWithMovement(this.paymentWithMovement, cashUpReport.get('startings'));
        OB.UTIL.cashupAddPaymentWithMovement(this.paymentWithMovement, cashUpReport.get('drops'));
        OB.UTIL.cashupAddPaymentWithMovement(this.paymentWithMovement, cashUpReport.get('deposits'));
        startings = OB.UTIL.cashupGetPaymentWithMovement(this.paymentWithMovement, cashUpReport.get('startings'));
        drops = OB.UTIL.cashupGetPaymentWithMovement(this.paymentWithMovement, cashUpReport.get('drops'));
        deposits = OB.UTIL.cashupGetPaymentWithMovement(this.paymentWithMovement, cashUpReport.get('deposits'));
      }
    } else {
      if (cashUpReport) {
        startings = cashUpReport.get('startings');
        drops = cashUpReport.get('drops');
        deposits = cashUpReport.get('deposits');
      }
      expectedSummary = this.summary.expectedSummary;
      if (OB.MobileApp.view.currentWindow !== 'retail.cashuppartial') {
        countedSummary = this.summary.countedSummary;
        differenceSummary = this.summary.differenceSummary;
        qtyToKeepSummary = this.summary.qtyToKeepSummary;
        qtyToDepoSummary = this.summary.qtyToDepoSummary;
      }
    }
    return {
      startings: startings,
      drops: drops,
      deposits: deposits,
      expectedSummary: expectedSummary,
      countedSummary: countedSummary,
      differenceSummary: differenceSummary,
      qtyToKeepSummary: qtyToKeepSummary,
      qtyToDepoSummary: qtyToDepoSummary
    };
  },

  cashUpReportChanged: function (cashUpReport) {
    var filtered = this.filterMovements(cashUpReport, false);
    this.$.startingsTable.setCollection(filtered.startings);
    this.$.startingsTable.setValue('totalstartings', cashUpReport.get('totalStartings'));

    this.$.dropsTable.setCollection(filtered.drops);
    this.$.dropsTable.setValue('totaldrops', cashUpReport.get('totalDrops'));

    this.$.depositsTable.setCollection(filtered.deposits);
    this.$.depositsTable.setValue('totaldeposits', cashUpReport.get('totalDeposits'));
  },

  summaryChanged: function () {
    var filtered = this.filterMovements(this.model.get('cashUpReport').at(0), true);
    this.$.expectedTable.setCollection(filtered.expectedSummary);
    this.$.expectedTable.setValue('totalexpected', this.summary.totalExpected);
    if (OB.MobileApp.view.currentWindow === 'retail.cashuppartial') {
      this.$.countedTable.hide();
      this.$.differenceTable.hide();
      this.$.qtyToKeepTable.hide();
      this.$.qtyToDepoTable.hide();
    } else {
      this.$.countedTable.setCollection(filtered.countedSummary);
      this.$.countedTable.setValue('totalcounted', this.summary.totalCounted);

      this.$.differenceTable.setCollection(filtered.differenceSummary);
      this.$.differenceTable.setValue('totaldifference', this.summary.totalDifference);

      this.$.qtyToKeepTable.setCollection(filtered.qtyToKeepSummary);
      this.$.qtyToKeepTable.setValue('totalqtyToKeep', this.summary.totalQtyToKeep);

      this.$.qtyToDepoTable.setCollection(filtered.qtyToDepoSummary);
      this.$.qtyToDepoTable.setValue('totalqtyToDepo', this.summary.totalQtyToDepo);
    }
  },

  modelChanged: function () {
    var filtered = this.filterMovements(this.model.get('cashUpReport').at(0), false);
    this.$.sales.setValue('netsales', this.model.get('netSales'));
    this.$.sales.setCollection(this.model.get('salesTaxes'));
    this.$.sales.setValue('totalsales', this.model.get('grossSales'));

    this.$.returns.setValue('netreturns', this.model.get('netReturns'));
    this.$.returns.setCollection(this.model.get('returnsTaxes'));
    this.$.returns.setValue('totalreturns', this.model.get('grossReturns'));

    this.$.totaltransactions.setValue('totaltransactionsline', this.model.get('totalRetailTransactions'));

    this.$.startingsTable.setCollection(filtered.startings);
    this.$.startingsTable.setValue('totalstartings', this.model.get('totalStartings'));

    this.$.dropsTable.setCollection(filtered.drops);
    this.$.dropsTable.setValue('totaldrops', this.model.get('totalDrops'));

    this.$.depositsTable.setCollection(filtered.deposits);
    this.$.depositsTable.setValue('totaldeposits', this.model.get('totalDeposits'));

    this.model.on('change:time', function () {
      this.$.time.setContent(OB.I18N.getLabel('OBPOS_LblCloseTime') + ': ' + OB.I18N.formatDate(this.model.get('time')) + ' - ' + OB.I18N.formatHour(this.model.get('time')));
    }, this);
  },

  displayStep: function (model) {
    if (OB.MobileApp.model.get('permissions').OBPOS_HideCashUpInfoToCashier) {
      this.$.differenceTable.hide();
      this.$.expectedTable.hide();
      this.$.depositsTable.hide();
      this.$.dropsTable.hide();
      this.$.sales.hide();
      this.$.totaltransactions.hide();
      this.$.returns.hide();
    } else {
      this.$.differenceTable.show();
      this.$.expectedTable.show();
      this.$.depositsTable.show();
      this.$.dropsTable.show();
      this.$.sales.show();
      this.$.totaltransactions.show();
      this.$.returns.show();
    }

    // this function is invoked when displayed.
    this.$.reporttitle.renderHeader(model.stepNumber('OB.CashUp.PostPrintAndClose'), model.stepCount());
    if (!model.cashupStepsDefinition[model.stepIndex('OB.CashUp.CashToKeep')].active) {
      _.each(model.get('paymentList').models, function (model) {
        if (OB.UTIL.isNullOrUndefined(model.get('qtyToKeep'))) {
          model.set('qtyToKeep', 0);
        }
      }, this);
    }
    if (OB.POS.modelterminal.get('terminal').ismaster) {
      if (OB.MobileApp.view.currentWindow === 'retail.cashuppartial') {
        var me = this;
        new OB.DS.Process('org.openbravo.retail.posterminal.ProcessCashMgmtMaster').exec({
          cashUpId: OB.POS.modelterminal.get('terminal').cashUpId,
          terminalSlave: OB.POS.modelterminal.get('terminal').isslave
        }, function (data) {
          if (data && !data.exception) {
            me.owner.$.cashMaster.updateCashUpModel(model, data, function () {
              me.cashUpReportChanged(model.get('cashUpReport').at(0));
            });
          }
        });
      } else {
        this.cashUpReportChanged(model.get('cashUpReport').at(0));
      }
    }
    this.setSummary(model.getCountCashSummary());
    this.$.time.setContent(OB.I18N.getLabel('OBPOS_LblCloseTime') + ': ' + OB.I18N.formatDate(new Date()) + ' - ' + OB.I18N.formatHour(new Date()));
    this.render();
  }
});