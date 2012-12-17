/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

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
    this.$.totalLbl.setContent(this.label);
    this.$.totalQty.setContent(OB.I18N.formatCurrency(this.value));
  },
  initComponents: function () {
    this.inherited(arguments);
    if (this.label && this.value) {
      this.$.totalLbl.setContent(this.label);
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
      style: 'padding: 5px 0px 0px 5px;  border-top: 1px solid #cccccc; float: left; width: 35%',
      content: 'Item Label'
    }, {
      name: 'foreignItemQty',
      style: 'padding: 5px 0px 0px 0px;  border-top: 1px solid #cccccc; float: left; width: 20%; text-align:right;',
      content: ''
    }, {
      name: 'itemQty',
      style: 'padding: 5px 0px 0px 5px;  border-top: 1px solid #cccccc; border-left: 1px solid #cccccc; float: left; width: 20%; text-align:right;',
      content: ''
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
    this.$.itemLbl.setContent(this.label);
    this.$.itemQty.setContent(OB.I18N.formatCurrency(this.value));

    if (this.second && this.second !== this.value && this.convertedValues.indexOf(this.type) != -1) {
      this.$.foreignItemQty.setContent('(' + OB.I18N.formatCurrency(this.second) + ' ' + this.isocode + ')');
    } else if (this.rate && this.rate !== '1' && this.valuestoConvert.indexOf(this.type) != -1) {
      if(this.value){
        this.$.foreignItemQty.setContent('(' + OB.I18N.formatCurrency(this.value) + ' ' + this.isocode + ')');
      }
      this.$.itemQty.setContent(OB.I18N.formatCurrency(OB.DEC.mul(this.value, this.rate)));
    } else {
      this.$.foreignItemQty.setContent('');
    }
  },
  create: function () {
    this.inherited(arguments);
    if (this.model) {
      this.label = this.model[this.lblProperty];
      this.value = this.model[this.qtyProperty];
      this.type = this.owner.typeProperty;
      this.second = this.model.second;
      this.rate = this.model.rate;
      this.isocode = this.model.isocode;
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
    label: OB.I18N.getLabel('OBPOS_LblNetSales')
  }, {
    kind: 'OB.OBPOSCashUp.UI.ppc_collectionLines',
    name: 'salestaxes',
    lblProperty: 'taxName',
    qtyProperty: 'taxAmount'
  }, {
    kind: 'OB.OBPOSCashUp.UI.ppc_totalsLine',
    name: 'totalsales',
    label: OB.I18N.getLabel('OBPOS_LblGrossSales')
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
    label: OB.I18N.getLabel('OBPOS_LblNetReturns')
  }, {
    kind: 'OB.OBPOSCashUp.UI.ppc_collectionLines',
    name: 'retunrnstaxes',
    lblProperty: 'taxName',
    qtyProperty: 'taxAmount'
  }, {
    kind: 'OB.OBPOSCashUp.UI.ppc_totalsLine',
    name: 'totalreturns',
    label: OB.I18N.getLabel('OBPOS_LblGrossReturns')
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
    label: OB.I18N.getLabel('OBPOS_LblTotalRetailTrans')
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
    label: OB.I18N.getLabel('OBPOS_LblTotalWithdrawals')
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
    label: OB.I18N.getLabel('OBPOS_LblTotalDeposits')
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
    label: OB.I18N.getLabel('OBPOS_LblTotalStarting')
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
    label: OB.I18N.getLabel('OBPOS_LblTotalExpected')
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
    label: OB.I18N.getLabel('OBPOS_LblTotalDifference')
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
    label: OB.I18N.getLabel('OBPOS_LblTotalCounted')
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
    label: OB.I18N.getLabel('OBPOS_LblTotalQtyToKeep')
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
    label: OB.I18N.getLabel('OBPOS_LblTotalQtyToDepo')
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
    style: 'overflow:auto; height: 500px; margin: 5px',
    components: [{
      style: 'background-color: #ffffff; color: black; padding: 5px;',
      components: [{
        classes: 'row-fluid',
        components: [{
          classes: 'span12',
          components: [{
            style: 'padding: 10px; border-bottom: 1px solid #cccccc; text-align:center;',
            content: OB.I18N.getLabel('OBPOS_LblStep4of4')
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
              style: 'padding: 20px 20px 20px 10px;',
              attributes: {
                src: '../../utility/ShowImageLogo?logo=yourcompanymenu'
              }
            }, {
              style: 'padding: 5px; text-align:center;',
              name: 'user'
            }, {
              name: 'time',
              style: 'padding: 5px 5px 15px 5px; text-align:center;'
            }]
          }]
        }]
      },
      //FIXME: Iterate taxes
      {
        components: [{
          components: [{}, {
            tag: 'ul',
            classes: 'unstyled',
            style: 'display:block',
            components: [{
              tag: 'li',
              classes: 'selected',
              components: [{
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
            }]
          }, {
            style: 'border-bottom-width: 1px; border-bottom-style: solid; border-bottom-color: rgb(204, 204, 204); padding-top: 15px; padding-right: 15px; padding-bottom: 15px; padding-left: 15px; font-weight: bold; color: rgb(204, 204, 204); display: none;'
          }, {
            style: 'display:none;'
          }]
        }]
      }, {
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
            style: 'padding: 5px 0px 0px 5px; float: left; width: 60%; font-weight:bold;',
            components: [{}]
          }, {
            style: 'clear:both;'
          }]
        }]
      }]
    }]
  }],
  create: function () {
    this.inherited(arguments);
    this.$.user.setContent(OB.I18N.getLabel('OBPOS_LblUser') + ': ' + OB.POS.modelterminal.get('context').user._identifier);
    this.$.time.setContent(OB.I18N.getLabel('OBPOS_LblTime') + ': ' + new Date().toString().substring(3, 24));
  },
  init: function () {},
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

    this.$.sales.setCollection(this.model.get('salesTaxes'));
    this.$.sales.setValue('netsales', this.model.get('netSales'));
    this.$.sales.setValue('totalsales', this.model.get('grossSales'));

    this.$.returns.setCollection(this.model.get('returnsTaxes'));
    this.$.returns.setValue('netreturns', this.model.get('netReturns'));
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
  }
});