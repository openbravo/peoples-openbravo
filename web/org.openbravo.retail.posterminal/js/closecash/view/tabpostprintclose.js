/*
 ************************************************************************************
 * Copyright (C) 2012-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo */

//Renders the summary of deposits/drops and contains a list (OB.OBPOSCasgMgmt.UI.RenderDepositsDrops)
//with detailed information for each payment typ
enyo.kind({
  name: 'OB.OBPOSCloseCash.UI.ppc_lineSeparator',
  classes: 'obObPosCloseCashUiPpcLineSeparator row-fluid',
  components: [
    {
      classes: 'obObPosCloseCashUiPpcLineSeparator-container1',
      components: [
        {
          classes: 'obObPosCloseCashUiPpcLineSeparator-container1-container1',
          components: [
            {
              classes:
                'obObPosCloseCashUiPpcLineSeparator-container1-container1-element1',
              allowHtml: true,
              tag: 'span',
              content: '&nbsp;'
            }
          ]
        },
        {
          classes: 'obObPosCloseCashUiPpcLineSeparator-container1-container2',
          components: [
            {
              classes:
                'obObPosCloseCashUiPpcLineSeparator-container1-container2-element1'
            }
          ]
        }
      ]
    },
    {
      classes: 'obObPosCloseCashUiPpcLineSeparator-container1-element1'
    }
  ]
});

enyo.kind({
  name: 'OB.OBPOSCloseCash.UI.ppc_totalsLine',
  classes: 'obObPosCloseCashUiPpcTotalsLine row-fluid',
  label: '',
  value: '',
  components: [
    {
      classes: 'obObPosCloseCashUiPpcTotalsLine-container1',
      components: [
        {
          classes: 'obObPosCloseCashUiPpcTotalsLine-container1-container1',
          components: [
            {
              classes:
                'obObPosCloseCashUiPpcTotalsLine-container1-container1-element1',
              allowHtml: true,
              tag: 'span',
              content: '&nbsp;'
            }
          ]
        },
        {
          name: 'totalLbl',
          classes: 'obObPosCloseCashUiPpcTotalsLine-container1-totalLbl'
        },
        {
          name: 'totalQty',
          classes: 'obObPosCloseCashUiPpcTotalsLine-container1-totalQty'
        },
        {
          classes:
            'obObPosCloseCashUiPpcTotalsLine-container1-totalQty-container2',
          components: [
            {
              classes:
                'obObPosCloseCashUiPpcTotalsLine-container1-totalQty-container2-element1',
              allowHtml: true,
              tag: 'span',
              content: '&nbsp;'
            }
          ]
        }
      ]
    },
    {
      classes: 'obObPosCloseCashUiPpcTotalsLine-element1'
    }
  ],
  setValue: function(value) {
    this.value = value;
    this.render();
  },
  render: function() {
    if (this.label) {
      this.$.totalLbl.setContent(this.label);
    } else {
      this.$.totalLbl.setContent(OB.I18N.getLabel(this.i18nLabel));
    }
    this.$.totalQty.setContent(
      OB.I18N.formatCurrency(OB.DEC.add(0, this.value))
    );
  },
  initComponents: function() {
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
  name: 'OB.OBPOSCloseCash.UI.ppc_itemLine',
  label: '',
  value: '',
  classes: 'obObPosCloseCashUiPpcItemLine row-fluid',
  convertedValues: [
    'expected',
    'counted',
    'difference',
    'qtyToKeep',
    'qtyToDepo'
  ],
  valuestoConvert: ['deposits', 'drops', 'startings'],
  components: [
    {
      classes: 'obObPosCloseCashUiPpcItemLine-container1',
      components: [
        {
          classes: 'obObPosCloseCashUiPpcItemLine-container1-container1',
          components: [
            {
              classes:
                'obObPosCloseCashUiPpcItemLine-container1-container1-element1',
              allowHtml: true,
              tag: 'span',
              content: '&nbsp;'
            }
          ]
        },
        {
          name: 'itemLbl',
          classes: 'obObPosCloseCashUiPpcItemLine-container1-itemLbl',
          allowHtml: true
        },
        {
          name: 'foreignItemQty',
          classes: 'obObPosCloseCashUiPpcItemLine-container1-foreignItemQty'
        },
        {
          name: 'itemQty',
          classes: 'obObPosCloseCashUiPpcItemLine-container1-itemQty'
        },
        {
          classes: 'obObPosCloseCashUiPpcItemLine-container1-container2',
          components: [
            {
              classes:
                'obObPosCloseCashUiPpcItemLine-container1-container2-element1',
              allowHtml: true,
              tag: 'span',
              content: '&nbsp;'
            }
          ]
        }
      ]
    },
    {
      classes: 'obObPosCloseCashUiPpcItemLine-element1'
    }
  ],
  setValue: function(value) {
    this.value = value;
    this.render();
  },
  render: function() {
    if (this.i18nLabel) {
      this.$.itemLbl.setContent(OB.I18N.getLabel(this.i18nLabel));
    } else {
      this.$.itemLbl.setContent(this.label);
    }
    this.$.itemQty.setContent(
      OB.I18N.formatCurrency(OB.DEC.add(0, this.value))
    );

    if (
      this.convertedValue &&
      this.convertedValue !== this.value &&
      this.convertedValues.indexOf(this.type) !== -1
    ) {
      this.$.foreignItemQty.setContent(
        '(' +
          OB.I18N.formatCurrency(this.convertedValue) +
          ' ' +
          this.isocode +
          ')'
      );
    } else if (
      this.valueToConvert &&
      this.value !== this.valueToConvert &&
      this.valuestoConvert.indexOf(this.type) !== -1
    ) {
      if (this.value) {
        this.$.foreignItemQty.setContent(
          '(' + OB.I18N.formatCurrency(this.value) + ' ' + this.isocode + ')'
        );
      }
      this.$.itemQty.setContent(OB.I18N.formatCurrency(this.valueToConvert));
    } else {
      this.$.foreignItemQty.setContent('');
    }
  },
  create: function() {
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
      if (
        this.convertedValue &&
        this.convertedValues.indexOf(this.type) === -1
      ) {
        OB.warn(
          "DEVELOPER: the type '" +
            this.type +
            "' is being incorrectly implemented."
        );
      }
      if (
        this.valueToConvert &&
        this.valuestoConvert.indexOf(this.type) === -1
      ) {
        OB.warn(
          "DEVELOPER: the type '" +
            this.type +
            "' is being incorrectly implemented."
        );
      }
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSCloseCash.UI.ppc_collectionLines',
  kind: 'OB.UI.iterateArray',
  classes: 'obObPosCloseCashUiPpcCollectionLines',
  renderLine: 'OB.OBPOSCloseCash.UI.ppc_itemLine',
  renderEmpty: 'OB.UI.RenderEmpty'
});

enyo.kind({
  name: 'OB.OBPOSCloseCash.UI.ppc_table',
  classes: 'obObPosCloseCashUiPpcTable',
  setValue: function(name, value) {
    this.$[name].setValue(value);
  }
});

enyo.kind({
  name: 'OB.OBPOSCloseCash.UI.ppc_salesTable',
  kind: 'OB.OBPOSCloseCash.UI.ppc_table',
  classes: 'obObPosCloseCashUiPpcSalesTable',
  components: [
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_itemLine',
      name: 'netsales',
      classes: 'obObPosCloseCashUiPpcSalesTable-netsales',
      i18nLabel: 'OBPOS_LblNetSales'
    },
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_collectionLines',
      name: 'salestaxes',
      classes: 'obObPosCloseCashUiPpcSalesTable-salestaxes',
      lblProperty: 'name',
      qtyProperty: 'amount'
    },
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_totalsLine',
      name: 'totalsales',
      classes: 'obObPosCloseCashUiPpcSalesTable-totalsales',
      i18nLabel: 'OBPOS_LblGrossSales'
    },
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_lineSeparator',
      name: 'separator',
      classes: 'obObPosCloseCashUiPpcSalesTable-separator'
    }
  ],
  setCollection: function(col) {
    this.$.salestaxes.setCollection(col);
  }
});

enyo.kind({
  kind: 'OB.OBPOSCloseCash.UI.ppc_table',
  name: 'OB.OBPOSCloseCash.UI.ppc_returnsTable',
  classes: 'obObPosCloseCashUiPpcReturnsTable',
  components: [
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_itemLine',
      name: 'netreturns',
      classes: 'obObPosCloseCashUiPpcReturnsTable-netreturns',
      i18nLabel: 'OBPOS_LblNetReturns'
    },
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_collectionLines',
      name: 'retunrnstaxes',
      classes: 'obObPosCloseCashUiPpcReturnsTable-returnstaxes',
      lblProperty: 'name',
      qtyProperty: 'amount'
    },
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_totalsLine',
      name: 'totalreturns',
      classes: 'obObPosCloseCashUiPpcReturnsTable-totalreturns',
      i18nLabel: 'OBPOS_LblGrossReturns'
    },
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_lineSeparator',
      name: 'separator',
      classes: 'obObPosCloseCashUiPpcReturnsTable-separator'
    }
  ],
  setCollection: function(col) {
    this.$.retunrnstaxes.setCollection(col);
  }
});

enyo.kind({
  kind: 'OB.OBPOSCloseCash.UI.ppc_table',
  name: 'OB.OBPOSCloseCash.UI.ppc_totalTransactionsTable',
  classes: 'obObPosCloseCashUiPpcTotalTransactionsTable',
  components: [
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_totalsLine',
      name: 'totaltransactionsline',
      classes:
        'obObPosCloseCashUiPpcTotalTransactionsTable-totaltransactionsline',
      i18nLabel: 'OBPOS_LblTotalRetailTrans',
      init: function() {
        if (
          OB.POS.modelterminal.get('terminal').ismaster ||
          OB.POS.modelterminal.get('terminal').isslave
        ) {
          this.label = OB.I18N.getLabel('OBPOS_LblTotalRetailTransLocal');
        }
      }
    },
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_lineSeparator',
      name: 'separator',
      classes: 'obObPosCloseCashUiPpcTotalTransactionsTable-separator'
    }
  ]
});

enyo.kind({
  kind: 'OB.OBPOSCloseCash.UI.ppc_table',
  name: 'OB.OBPOSCloseCash.UI.ppc_cashDropsTable',
  classes: 'obObPosCloseCashUiPpcCashDropsTable',
  components: [
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_collectionLines',
      name: 'drops',
      classes: 'obObPosCloseCashUiPpcCashDropsTable-drops',
      lblProperty: 'description',
      qtyProperty: 'amount',
      typeProperty: 'drops'
    },
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_totalsLine',
      name: 'totaldrops',
      classes: 'obObPosCloseCashUiPpcCashDropsTable-totaldrops',
      i18nLabel: 'OBPOS_LblTotalWithdrawals'
    },
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_lineSeparator',
      name: 'separator',
      classes: 'obObPosCloseCashUiPpcCashDropsTable-separator'
    }
  ],
  setCollection: function(col) {
    this.$.drops.setCollection(col);
  }
});

enyo.kind({
  kind: 'OB.OBPOSCloseCash.UI.ppc_table',
  name: 'OB.OBPOSCloseCash.UI.ppc_cashDepositsTable',
  classes: 'obObPosCloseCashUiPpcCashDepositsTable',
  components: [
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_collectionLines',
      name: 'deposits',
      classes: 'obObPosCloseCashUiPpcCashDepositsTable-description',
      lblProperty: 'description',
      qtyProperty: 'amount',
      typeProperty: 'deposits'
    },
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_totalsLine',
      name: 'totaldeposits',
      classes: 'obObPosCloseCashUiPpcCashDepositsTable-totaldeposits',
      i18nLabel: 'OBPOS_LblTotalDeposits'
    },
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_lineSeparator',
      name: 'separator',
      classes: 'obObPosCloseCashUiPpcCashDepositsTable-separator'
    }
  ],
  setCollection: function(col) {
    this.$.deposits.setCollection(col);
  }
});

enyo.kind({
  kind: 'OB.OBPOSCloseCash.UI.ppc_table',
  name: 'OB.OBPOSCloseCash.UI.ppc_startingsTable',
  classes: 'obObPosCloseCashUiPpcStartingsTable',
  components: [
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_collectionLines',
      name: 'startings',
      classes: 'obObPosloseCashUiPpcCashDepositsTable-startings',
      lblProperty: 'description',
      qtyProperty: 'amount',
      typeProperty: 'startings'
    },
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_totalsLine',
      name: 'totalstartings',
      classes: 'obObPosCloseCashUiPpcStartingsTable-totalstartings',
      i18nLabel: 'OBPOS_LblTotalStarting'
    },
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_lineSeparator',
      name: 'separator',
      classes: 'obObPosCloseCashUiPpcStartingsTable-separator'
    }
  ],
  setCollection: function(col) {
    this.$.startings.setCollection(col);
  }
});

enyo.kind({
  kind: 'OB.OBPOSCloseCash.UI.ppc_table',
  name: 'OB.OBPOSCloseCash.UI.ppc_cashExpectedTable',
  classes: 'obObPosCloseCashUiPpcCashExpectedTable',
  components: [
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_collectionLines',
      name: 'expectedPerPayment',
      classes: 'obObPosCloseCashUiPpcCashExpectedTable-expectedPerPayment',
      lblProperty: 'name',
      qtyProperty: 'value',
      typeProperty: 'expected'
    },
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_totalsLine',
      name: 'totalexpected',
      classes: 'obObPosCloseCashUiPpcCashExpectedTable-totalexpected',
      i18nLabel: 'OBPOS_LblTotalExpected'
    },
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_lineSeparator',
      name: 'separator',
      classes: 'obObPosCloseCashUiPpcCashExpectedTable-separator'
    }
  ],
  setCollection: function(col) {
    this.$.expectedPerPayment.setCollection(col);
  }
});

enyo.kind({
  kind: 'OB.OBPOSCloseCash.UI.ppc_table',
  name: 'OB.OBPOSCloseCash.UI.ppc_cashDifferenceTable',
  classes: 'obObPosCloseCashUiPpcCashDifferenceTable',
  components: [
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_collectionLines',
      name: 'differencePerPayment',
      classes: 'obObPosCloseCashUiPpcCashDifferenceTable-differencePerPayment',
      lblProperty: 'name',
      qtyProperty: 'value',
      typeProperty: 'difference'
    },
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_totalsLine',
      name: 'totaldifference',
      classes: 'obObPosCloseCashUiPpcCashDifferenceTable-totaldifference',
      i18nLabel: 'OBPOS_LblTotalDifference'
    },
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_lineSeparator',
      name: 'separator',
      classes: 'obObPosCloseCashUiPpcCashDifferenceTable-separator'
    }
  ],
  setCollection: function(col) {
    this.$.differencePerPayment.setCollection(col);
  }
});

enyo.kind({
  kind: 'OB.OBPOSCloseCash.UI.ppc_table',
  name: 'OB.OBPOSCloseCash.UI.ppc_cashCountedTable',
  components: [
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_collectionLines',
      name: 'countedPerPayment',
      lblProperty: 'name',
      qtyProperty: 'value',
      typeProperty: 'counted'
    },
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_totalsLine',
      name: 'totalcounted',
      i18nLabel: 'OBPOS_LblTotalCounted'
    },
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_lineSeparator',
      name: 'separator'
    }
  ],
  setCollection: function(col) {
    this.$.countedPerPayment.setCollection(col);
  }
});

enyo.kind({
  kind: 'OB.OBPOSCloseCash.UI.ppc_table',
  name: 'OB.OBPOSCloseCash.UI.ppc_cashQtyToKeepTable',
  classes: 'obObPosCloseCashUiPpcCashQtyToKeepTable',
  components: [
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_collectionLines',
      name: 'qtyToKeepPerPayment',
      classes: 'obObPosCloseCashUiPpcCashQtyToKeepTable-qtyToKeepPerPayment',
      lblProperty: 'name',
      qtyProperty: 'value',
      typeProperty: 'qtyToKeep'
    },
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_totalsLine',
      name: 'totalqtyToKeep',
      classes: 'obObPosCloseCashUiPpcCashQtyToKeepTable-totalqtyToKeep',
      i18nLabel: 'OBPOS_LblTotalQtyToKeep'
    },
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_lineSeparator',
      name: 'separator',
      classes: 'obObPosCloseCashUiPpcCashQtyToKeepTable-separator'
    }
  ],
  setCollection: function(col) {
    this.$.qtyToKeepPerPayment.setCollection(col);
  }
});

enyo.kind({
  kind: 'OB.OBPOSCloseCash.UI.ppc_table',
  name: 'OB.OBPOSCloseCash.UI.ppc_cashQtyToDepoTable',
  classes: 'obObPosCloseCashUiPpcCashQtyToDepoTable',
  components: [
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_collectionLines',
      name: 'qtyToDepoPerPayment',
      classes: 'obObPosCloseCashUiPpcCashQtyToDepoTable-qtyToDepoPerPayment',
      lblProperty: 'name',
      qtyProperty: 'value',
      typeProperty: 'qtyToDepo'
    },
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_totalsLine',
      name: 'totalqtyToDepo',
      classes: 'obObPosCloseCashUiPpcCashQtyToDepoTable-totalqtyToDepo',
      i18nLabel: 'OBPOS_LblTotalQtyToDepo'
    },
    {
      kind: 'OB.OBPOSCloseCash.UI.ppc_lineSeparator',
      name: 'separator',
      classes: 'obObPosCloseCashUiPpcCashQtyToDepoTable-separator'
    }
  ],
  setCollection: function(col) {
    this.$.qtyToDepoPerPayment.setCollection(col);
  }
});

enyo.kind({
  name: 'OB.OBPOSCloseCash.UI.PostPrintClose',
  published: {
    model: null,
    summary: null
  },
  classes: 'obObPosCloseCashUiPostPrintClose',
  components: [
    {
      name: 'scrollArea',
      classes: 'obObPosCloseCashUiPostPrintClose-wrapper',
      components: [
        {
          classes: 'obObPosCloseCashUiPostPrintClose-wrapper-components',
          components: [
            {
              name: 'reporttitle',
              classes:
                'obObPosCloseCashUiPostPrintClose-wrapper-components-title',
              renderHeader: function(step, count) {
                this.setContent(
                  OB.I18N.getLabel('OBPOS_LblStepNumber', [step, count]) +
                    ' ' +
                    OB.I18N.getLabel('OBPOS_LblStepPostPrintAndClose') +
                    OB.OBPOSCloseCash.UI.CloseCash.getTitleExtensions()
                );
              }
            },
            {
              kind: 'Scroller',
              thumb: true,
              horizontal: 'hidden',
              classes:
                'obObPosCloseCashUiPostPrintClose-wrapper-components-body',
              components: [
                {
                  classes:
                    'obObPosCloseCashUiPostPrintClose-wrapper-components-body-container1 row-fluid',
                  components: [
                    {
                      classes:
                        'obObPosCloseCashUiPostPrintClose-wrapper-components-body-container1-container1',
                      components: [
                        {
                          classes:
                            'obObPosCloseCashUiPostPrintClose-wrapper-components-body-container1-container1-container1',
                          components: [
                            {
                              tag: 'img',
                              classes:
                                'obObPosCloseCashUiPostPrintClose-wrapper-components-body-container1-container1-container1-img',
                              initComponents: function() {
                                if (
                                  OB.MobileApp.model.get('terminal')
                                    .organizationImage
                                ) {
                                  this.setAttribute(
                                    'src',
                                    'data:' +
                                      OB.MobileApp.model.get('terminal')
                                        .organizationImageMime +
                                      ';base64,' +
                                      OB.MobileApp.model.get('terminal')
                                        .organizationImage
                                  );
                                }
                              }
                            },
                            {
                              name: 'store',
                              classes:
                                'obObPosCloseCashUiPostPrintClose-wrapper-components-body-container1-container1-container1-store'
                            },
                            {
                              name: 'terminal',
                              classes:
                                'obObPosCloseCashUiPostPrintClose-wrapper-components-body-container1-container1-container1-terminal'
                            },
                            {
                              name: 'user',
                              classes:
                                'obObPosCloseCashUiPostPrintClose-wrapper-components-body-container1-container1-container1-user'
                            },
                            {
                              name: 'openingtime',
                              classes:
                                'obObPosCloseCashUiPostPrintClose-wrapper-components-body-container1-container1-container1-openingtime'
                            },
                            {
                              name: 'time',
                              classes:
                                'obObPosCloseCashUiPostPrintClose-wrapper-components-body-container1-container1-container1-time'
                            },
                            {
                              classes:
                                'obObPosCloseCashUiPostPrintClose-wrapper-components-body-container1-container1-container1-element1'
                            }
                          ]
                        }
                      ]
                    },
                    {
                      classes:
                        'obObPosCloseCashUiPostPrintClose-wrapper-components-body-container1-container1-element1'
                    }
                  ]
                },
                //FIXME: Iterate taxes
                {
                  classes:
                    'obObPosCloseCashUiPostPrintClose-wrapper-components-body-container2 row-fluid',
                  components: [
                    {
                      tag: 'ul',
                      classes:
                        'obObPosCloseCashUiPostPrintClose-wrapper-components-body-container2-container1',
                      components: [
                        {
                          tag: 'li',
                          classes:
                            'obObPosCloseCashUiPostPrintClose-wrapper-components-body-container2-container1-container1 selected',
                          components: [
                            {
                              kind: 'OB.OBPOSCloseCash.UI.ppc_salesTable',
                              name: 'sales',
                              classes:
                                'obObPosCloseCashUiPostPrintClose-wrapper-components-body-container2-container1-container1-sales'
                            },
                            {
                              kind: 'OB.OBPOSCloseCash.UI.ppc_returnsTable',
                              name: 'returns',
                              classes:
                                'obObPosCloseCashUiPostPrintClose-wrapper-components-body-container2-container1-container1-returns'
                            },
                            {
                              kind:
                                'OB.OBPOSCloseCash.UI.ppc_totalTransactionsTable',
                              name: 'totaltransactions',
                              classes:
                                'obObPosCloseCashUiPostPrintClose-wrapper-components-body-container2-container1-container1-totaltransactions'
                            }
                          ]
                        }
                      ]
                    },
                    {
                      classes:
                        'obObPosCloseCashUiPostPrintClose-wrapper-components-body-container2-element1'
                    },
                    {
                      classes:
                        'obObPosCloseCashUiPostPrintClose-wrapper-components-body-container2-element2'
                    }
                  ]
                },
                {
                  classes:
                    'obObPosCloseCashUiPostPrintClose-wrapper-components-body-container3 row-fluid',
                  components: [
                    {
                      kind: 'OB.OBPOSCloseCash.UI.ppc_startingsTable',
                      name: 'startingsTable',
                      classes:
                        'obObPosCloseCashUiPostPrintClose-wrapper-components-body-container3-startingsTable'
                    },
                    {
                      kind: 'OB.OBPOSCloseCash.UI.ppc_cashDropsTable',
                      name: 'dropsTable',
                      classes:
                        'obObPosCloseCashUiPostPrintClose-wrapper-components-body-container3-dropsTable'
                    },
                    {
                      kind: 'OB.OBPOSCloseCash.UI.ppc_cashDepositsTable',
                      name: 'depositsTable',
                      classes:
                        'obObPosCloseCashUiPostPrintClose-wrapper-components-body-container3-depositsTable'
                    },
                    {
                      kind: 'OB.OBPOSCloseCash.UI.ppc_cashExpectedTable',
                      name: 'expectedTable',
                      classes:
                        'obObPosCloseCashUiPostPrintClose-wrapper-components-body-container3-expectedTable'
                    },
                    {
                      kind: 'OB.OBPOSCloseCash.UI.ppc_cashCountedTable',
                      name: 'countedTable',
                      classes:
                        'obObPosCloseCashUiPostPrintClose-wrapper-components-body-container3-countedTable'
                    },
                    {
                      kind: 'OB.OBPOSCloseCash.UI.ppc_cashDifferenceTable',
                      name: 'differenceTable',
                      classes:
                        'obObPosCloseCashUiPostPrintClose-wrapper-components-body-container3-differenceTable'
                    },
                    {
                      kind: 'OB.OBPOSCloseCash.UI.ppc_cashQtyToKeepTable',
                      name: 'qtyToKeepTable',
                      classes:
                        'obObPosCloseCashUiPostPrintClose-wrapper-components-body-container3-qtyToKeepTable'
                    },
                    {
                      kind: 'OB.OBPOSCloseCash.UI.ppc_cashQtyToDepoTable',
                      name: 'qtyToDepoTable',
                      classes:
                        'obObPosCloseCashUiPostPrintClose-wrapper-components-body-container3-qtyToDepoTable'
                    }
                  ]
                }
              ]
            }
          ]
        }
      ]
    }
  ],

  paymentWithMovement: [],

  create: function() {
    this.inherited(arguments);
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
    this.$.user.setContent(
      OB.I18N.getLabel('OBPOS_LblUser') +
        ': ' +
        OB.MobileApp.model.get('context').user._identifier
    );
    this.$.time.setContent(
      OB.I18N.getLabel('OBPOS_LblCloseTime') +
        ': ' +
        OB.I18N.formatDate(new Date()) +
        ' - ' +
        OB.I18N.formatHour(new Date())
    );
  },

  init: function(model) {
    this.model = model;

    this.$.reporttitle.setContent(
      OB.I18N.getLabel(model.reportTitleLabel) +
        OB.OBPOSCloseCash.UI.CloseCash.getTitleExtensions()
    );

    this.model.get('closeCashReport').on('add', closeCashReport => {
      this.$.openingtime.setContent(
        OB.I18N.getLabel('OBPOS_LblOpenTime') +
          ': ' +
          OB.I18N.formatDate(new Date(closeCashReport.get('creationDate'))) +
          ' - ' +
          OB.I18N.formatHour(new Date(closeCashReport.get('creationDate')))
      );
      this.$.sales.setValue('netsales', closeCashReport.get('netSales'));
      this.$.sales.setCollection(closeCashReport.get('salesTaxes'));
      this.$.sales.setValue('totalsales', closeCashReport.get('grossSales'));

      this.$.returns.setValue('netreturns', closeCashReport.get('netReturns'));
      this.$.returns.setCollection(closeCashReport.get('returnsTaxes'));
      this.$.returns.setValue(
        'totalreturns',
        closeCashReport.get('grossReturns')
      );

      this.$.totaltransactions.setValue(
        'totaltransactionsline',
        closeCashReport.get('totalRetailTransactions')
      );

      if (!OB.POS.modelterminal.get('terminal').ismaster) {
        this.closeCashReportChanged(closeCashReport);
      }
    });

    this.model.on('change:time', () => {
      this.$.time.setContent(
        OB.I18N.getLabel('OBPOS_LblCloseTime') +
          ': ' +
          OB.I18N.formatDate(this.model.get('time')) +
          ' - ' +
          OB.I18N.formatHour(this.model.get('time'))
      );
    });
  },

  filterMovements: function(closeCashReport, isSummary) {
    let startings,
      drops,
      deposits,
      expectedSummary,
      countedSummary,
      differenceSummary,
      qtyToKeepSummary,
      qtyToDepoSummary;
    if (
      closeCashReport &&
      OB.MobileApp.model.hasPermission(
        'OBPOS_retail.cashupRemoveUnusedPayment',
        true
      )
    ) {
      this.paymentWithMovement = [];
      if (isSummary) {
        OB.UTIL.closeCashAddPaymentWithSummaryMovement(
          this.paymentWithMovement,
          this.summary.expectedSummary
        );
        if (OB.MobileApp.view.currentWindow !== 'retail.cashuppartial') {
          OB.UTIL.closeCashAddPaymentWithSummaryMovement(
            this.paymentWithMovement,
            this.summary.countedSummary
          );
          OB.UTIL.closeCashAddPaymentWithSummaryMovement(
            this.paymentWithMovement,
            this.summary.differenceSummary
          );
          OB.UTIL.closeCashAddPaymentWithSummaryMovement(
            this.paymentWithMovement,
            this.summary.qtyToKeepSummary
          );
          OB.UTIL.closeCashAddPaymentWithSummaryMovement(
            this.paymentWithMovement,
            this.summary.qtyToDepoSummary
          );
          countedSummary = OB.UTIL.closeCashGetPaymentWithMovement(
            this.paymentWithMovement,
            this.summary.countedSummary
          );
          differenceSummary = OB.UTIL.closeCashGetPaymentWithMovement(
            this.paymentWithMovement,
            this.summary.differenceSummary
          );
          qtyToKeepSummary = OB.UTIL.closeCashGetPaymentWithMovement(
            this.paymentWithMovement,
            this.summary.qtyToKeepSummary
          );
          qtyToDepoSummary = OB.UTIL.closeCashGetPaymentWithMovement(
            this.paymentWithMovement,
            this.summary.qtyToDepoSummary
          );
        }
        expectedSummary = OB.UTIL.closeCashGetPaymentWithMovement(
          this.paymentWithMovement,
          this.summary.expectedSummary
        );
      } else {
        OB.UTIL.closeCashAddPaymentWithMovement(
          this.paymentWithMovement,
          closeCashReport.get('startings')
        );
        OB.UTIL.closeCashAddPaymentWithMovement(
          this.paymentWithMovement,
          closeCashReport.get('drops')
        );
        OB.UTIL.closeCashAddPaymentWithMovement(
          this.paymentWithMovement,
          closeCashReport.get('deposits')
        );
        startings = OB.UTIL.closeCashGetPaymentWithMovement(
          this.paymentWithMovement,
          closeCashReport.get('startings')
        );
        drops = OB.UTIL.closeCashGetPaymentWithMovement(
          this.paymentWithMovement,
          closeCashReport.get('drops')
        );
        deposits = OB.UTIL.closeCashGetPaymentWithMovement(
          this.paymentWithMovement,
          closeCashReport.get('deposits')
        );
      }
    } else {
      if (closeCashReport) {
        startings = closeCashReport.get('startings');
        drops = closeCashReport.get('drops');
        deposits = closeCashReport.get('deposits');
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

  closeCashReportChanged: function(closeCashReport) {
    const filtered = this.filterMovements(closeCashReport, false);
    this.$.startingsTable.setCollection(filtered.startings);
    this.$.startingsTable.setValue(
      'totalstartings',
      closeCashReport.get('totalStartings')
    );

    this.$.dropsTable.setCollection(filtered.drops);
    this.$.dropsTable.setValue('totaldrops', closeCashReport.get('totalDrops'));

    this.$.depositsTable.setCollection(filtered.deposits);
    this.$.depositsTable.setValue(
      'totaldeposits',
      closeCashReport.get('totalDeposits')
    );
  },

  summaryChanged: function() {
    const filtered = this.filterMovements(
      this.model.get('closeCashReport').at(0),
      true
    );
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
      this.$.differenceTable.setValue(
        'totaldifference',
        this.summary.totalDifference
      );

      this.$.qtyToKeepTable.setCollection(filtered.qtyToKeepSummary);
      this.$.qtyToKeepTable.setValue(
        'totalqtyToKeep',
        this.summary.totalQtyToKeep
      );

      this.$.qtyToDepoTable.setCollection(filtered.qtyToDepoSummary);
      this.$.qtyToDepoTable.setValue(
        'totalqtyToDepo',
        this.summary.totalQtyToDepo
      );
    }
  },

  modelChanged: function() {
    const filtered = this.filterMovements(
      this.model.get('closeCashReport').at(0),
      false
    );
    this.$.sales.setValue('netsales', this.model.get('netSales'));
    this.$.sales.setCollection(this.model.get('salesTaxes'));
    this.$.sales.setValue('totalsales', this.model.get('grossSales'));

    this.$.returns.setValue('netreturns', this.model.get('netReturns'));
    this.$.returns.setCollection(this.model.get('returnsTaxes'));
    this.$.returns.setValue('totalreturns', this.model.get('grossReturns'));

    this.$.totaltransactions.setValue(
      'totaltransactionsline',
      this.model.get('totalRetailTransactions')
    );

    this.$.startingsTable.setCollection(filtered.startings);
    this.$.startingsTable.setValue(
      'totalstartings',
      this.model.get('totalStartings')
    );

    this.$.dropsTable.setCollection(filtered.drops);
    this.$.dropsTable.setValue('totaldrops', this.model.get('totalDrops'));

    this.$.depositsTable.setCollection(filtered.deposits);
    this.$.depositsTable.setValue(
      'totaldeposits',
      this.model.get('totalDeposits')
    );

    this.model.on('change:time', () => {
      this.$.time.setContent(
        OB.I18N.getLabel('OBPOS_LblCloseTime') +
          ': ' +
          OB.I18N.formatDate(this.model.get('time')) +
          ' - ' +
          OB.I18N.formatHour(this.model.get('time'))
      );
    });
  },

  displayStep: function(model) {
    if (
      OB.MobileApp.model.hasPermission('OBPOS_HideCashUpInfoToCashier', true)
    ) {
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
    this.$.reporttitle.renderHeader(
      model.stepNumber('OB.CloseCash.PostPrintAndClose'),
      model.stepCount()
    );
    if (
      !model.stepsDefinition[model.stepIndex('OB.CloseCash.CashToKeep')].active
    ) {
      model.get('paymentList').models.forEach(model => {
        if (OB.UTIL.isNullOrUndefined(model.get('qtyToKeep'))) {
          model.set('qtyToKeep', 0);
        }
      });
    }
    if (OB.POS.modelterminal.get('terminal').ismaster) {
      if (OB.MobileApp.view.currentWindow === 'retail.cashuppartial') {
        new OB.DS.Process(
          'org.openbravo.retail.posterminal.ProcessCashMgmtMaster'
        ).exec(
          {
            cashUpId: OB.POS.modelterminal.get('terminal').cashUpId,
            terminalSlave: OB.POS.modelterminal.get('terminal').isslave
          },
          data => {
            if (data && !data.exception) {
              this.owner.$.cashMaster.updateCloseCashModel(model, data, () => {
                this.closeCashReportChanged(model.get('closeCashReport').at(0));
              });
            }
          }
        );
      } else {
        this.closeCashReportChanged(model.get('closeCashReport').at(0));
      }
    }
    this.setSummary(model.getCountCashSummary());
    this.$.time.setContent(
      OB.I18N.getLabel('OBPOS_LblCloseTime') +
        ': ' +
        OB.I18N.formatDate(new Date()) +
        ' - ' +
        OB.I18N.formatHour(new Date())
    );
    this.render();
  }
});
