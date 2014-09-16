/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, OB */

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
  }, {}]
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
  }, {}],
  setValue: function (value) {
    this.value = value;
    this.render();
  },
  render: function () {
    if (this.i18nLabel) {
      this.$.totalLbl.setContent(OB.I18N.getLabel(this.i18nLabel));
    } else {
      this.$.totalLbl.setContent(this.label);
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
  }, {}],
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
      if (this.valueToConvert && this.valuestoConvert.indexOf(this.type) === -1){
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
    i18nLabel: 'OBPOS_LblTotalRetailTrans'
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
    style: 'overflow:auto; height: 612px; margin: 5px',
    components: [{
      style: 'background-color: #ffffff; color: black; padding: 5px;',
      components: [{
        classes: 'row-fluid',
        components: [{
          classes: 'span12',
          components: [{
            style: 'padding: 10px; border-bottom: 1px solid #cccccc; text-align:center;',
            initComponents: function () {
              this.setContent(OB.I18N.getLabel('OBPOS_LblStep4of4') + OB.OBPOSCashUp.UI.CashUp.getTitleExtensions());
            }
          }]
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
                  this.setAttribute('src', 'data:'+OB.MobileApp.model.get('terminal').organizationImageMime+';base64,'+OB.MobileApp.model.get('terminal').organizationImage);
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
              name: 'time',
              style: 'padding: 5px; text-align:center;'
            }, {
              style: 'padding: 0px 0px 10px 0px;'
            }]
          }]
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
      }, {
        classes: 'row-fluid',
        components: [{
          classes: 'span12',
          components: [{
            style: 'width: 10%; float: left',
            components: [{
              tag: 'span',
              allowHtml: true,
              content: '&nbsp;'
            }]
          }, {
            style: 'padding: 5px 0px 0px 5px; float: left; width: 60%; font-weight:bold;'
          }, {
            style: 'clear:both;'
          }]
        }]
      }]
    }]
  }],
  create: function () {
    this.inherited(arguments);
    this.$.store.setContent(OB.I18N.getLabel('OBPOS_LblStore') + ': ' + OB.POS.modelterminal.get('terminal').organization$_identifier);
    this.$.terminal.setContent(OB.I18N.getLabel('OBPOS_LblTerminal') + ': ' + OB.POS.modelterminal.get('terminal')._identifier);
    this.$.user.setContent(OB.I18N.getLabel('OBPOS_LblUser') + ': ' + OB.POS.modelterminal.get('context').user._identifier);
    this.$.time.setContent(OB.I18N.getLabel('OBPOS_LblTime') + ': ' + OB.I18N.formatDate(new Date()) + ' - ' + OB.I18N.formatHour(new Date()));
  },
  init: function (model) {
    this.model = model;
    this.model.get('cashUpReport').on('add', function (cashUpReport) {

      this.$.sales.setValue('netsales', cashUpReport.get('netSales'));
      this.$.sales.setCollection(cashUpReport.get('salesTaxes'));
      this.$.sales.setValue('totalsales', cashUpReport.get('grossSales'));

      this.$.returns.setValue('netreturns', cashUpReport.get('netReturns'));
      this.$.returns.setCollection(cashUpReport.get('returnsTaxes'));
      this.$.returns.setValue('totalreturns', cashUpReport.get('grossReturns'));

      this.$.totaltransactions.setValue('totaltransactionsline', cashUpReport.get('totalRetailTransactions'));

      this.$.startingsTable.setCollection(cashUpReport.get('startings'));
      this.$.startingsTable.setValue('totalstartings', cashUpReport.get('totalStartings'));

      this.$.dropsTable.setCollection(cashUpReport.get('drops'));
      this.$.dropsTable.setValue('totaldrops', cashUpReport.get('totalDrops'));

      this.$.depositsTable.setCollection(cashUpReport.get('deposits'));
      this.$.depositsTable.setValue('totaldeposits', cashUpReport.get('totalDeposits'));
    }, this);

    this.model.on('change:time', function () {
      this.$.time.setContent(OB.I18N.getLabel('OBPOS_LblTime') + ': ' + OB.I18N.formatDate(this.model.get('time')) + ' - ' + OB.I18N.formatHour(this.model.get('time')));
    }, this);
  },
  summaryChanged: function () {
    this.$.expectedTable.setCollection(this.summary.expectedSummary);
    this.$.expectedTable.setValue('totalexpected', this.summary.totalExpected);

    this.$.countedTable.setCollection(this.summary.countedSummary);
    this.$.countedTable.setValue('totalcounted', this.summary.totalCounted);

    this.$.differenceTable.setCollection(this.summary.differenceSummary);
    this.$.differenceTable.setValue('totaldifference', this.summary.totalDifference);

    this.$.qtyToKeepTable.setCollection(this.summary.qtyToKeepSummary);
    this.$.qtyToKeepTable.setValue('totalqtyToKeep', this.summary.totalQtyToKeep);

    this.$.qtyToDepoTable.setCollection(this.summary.qtyToDepoSummary);
    this.$.qtyToDepoTable.setValue('totalqtyToDepo', this.summary.totalQtyToDepo);
  },
  modelChanged: function () {

    this.$.sales.setValue('netsales', this.model.get('netSales'));
    this.$.sales.setCollection(this.model.get('salesTaxes'));
    this.$.sales.setValue('totalsales', this.model.get('grossSales'));

    this.$.returns.setValue('netreturns', this.model.get('netReturns'));
    this.$.returns.setCollection(this.model.get('returnsTaxes'));
    this.$.returns.setValue('totalreturns', this.model.get('grossReturns'));

    this.$.totaltransactions.setValue('totaltransactionsline', this.model.get('totalRetailTransactions'));

    this.$.startingsTable.setCollection(this.model.get('startings'));
    this.$.startingsTable.setValue('totalstartings', this.model.get('totalStartings'));

    this.$.dropsTable.setCollection(this.model.get('drops'));
    this.$.dropsTable.setValue('totaldrops', this.model.get('totalDrops'));

    this.$.depositsTable.setCollection(this.model.get('deposits'));
    this.$.depositsTable.setValue('totaldeposits', this.model.get('totalDeposits'));

    this.model.on('change:time', function () {
      this.$.time.setContent(OB.I18N.getLabel('OBPOS_LblTime') + ': ' + OB.I18N.formatDate(this.model.get('time')) + ' - ' + OB.I18N.formatHour(this.model.get('time')));
    }, this);
  },
  displayStep: function (model) {
    // this function is invoked when displayed.
    this.setSummary(model.getCountCashSummary());
  }
});