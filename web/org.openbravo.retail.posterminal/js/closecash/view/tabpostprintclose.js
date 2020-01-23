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
  classes: 'obObPosCashUpUiPpcLineSeparator row-fluid',
  components: [
    {
      classes: 'obObPosCashUpUiPpcLineSeparator-container1',
      components: [
        {
          classes: 'obObPosCashUpUiPpcLineSeparator-container1-container1',
          components: [
            {
              classes:
                'obObPosCashUpUiPpcLineSeparator-container1-container1-element1',
              allowHtml: true,
              tag: 'span',
              content: '&nbsp;'
            }
          ]
        },
        {
          classes: 'obObPosCashUpUiPpcLineSeparator-container1-container2',
          components: [
            {
              classes:
                'obObPosCashUpUiPpcLineSeparator-container1-container2-element1'
            }
          ]
        }
      ]
    },
    {
      classes: 'obObPosCashUpUiPpcLineSeparator-container1-element1'
    }
  ]
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.ppc_totalsLine',
  classes: 'obObPosCashUpUiPpcTotalsLine row-fluid',
  label: '',
  value: '',
  components: [
    {
      classes: 'obObPosCashUpUiPpcTotalsLine-container1',
      components: [
        {
          classes: 'obObPosCashUpUiPpcTotalsLine-container1-container1',
          components: [
            {
              classes:
                'obObPosCashUpUiPpcTotalsLine-container1-container1-element1',
              allowHtml: true,
              tag: 'span',
              content: '&nbsp;'
            }
          ]
        },
        {
          name: 'totalLbl',
          classes: 'obObPosCashUpUiPpcTotalsLine-container1-totalLbl'
        },
        {
          name: 'totalQty',
          classes: 'obObPosCashUpUiPpcTotalsLine-container1-totalQty'
        },
        {
          classes:
            'obObPosCashUpUiPpcTotalsLine-container1-totalQty-container2',
          components: [
            {
              classes:
                'obObPosCashUpUiPpcTotalsLine-container1-totalQty-container2-element1',
              allowHtml: true,
              tag: 'span',
              content: '&nbsp;'
            }
          ]
        }
      ]
    },
    {
      classes: 'obObPosCashUpUiPpcTotalsLine-element1'
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
  name: 'OB.OBPOSCashUp.UI.ppc_itemLine',
  label: '',
  value: '',
  classes: 'obObPosCashUpUiPpcItemLine row-fluid',
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
      classes: 'obObPosCashUpUiPpcItemLine-container1',
      components: [
        {
          classes: 'obObPosCashUpUiPpcItemLine-container1-container1',
          components: [
            {
              classes:
                'obObPosCashUpUiPpcItemLine-container1-container1-element1',
              allowHtml: true,
              tag: 'span',
              content: '&nbsp;'
            }
          ]
        },
        {
          name: 'itemLbl',
          classes: 'obObPosCashUpUiPpcItemLine-container1-itemLbl',
          allowHtml: true
        },
        {
          name: 'foreignItemQty',
          classes: 'obObPosCashUpUiPpcItemLine-container1-foreignItemQty'
        },
        {
          name: 'itemQty',
          classes: 'obObPosCashUpUiPpcItemLine-container1-itemQty'
        },
        {
          classes: 'obObPosCashUpUiPpcItemLine-container1-container2',
          components: [
            {
              classes:
                'obObPosCashUpUiPpcItemLine-container1-container2-element1',
              allowHtml: true,
              tag: 'span',
              content: '&nbsp;'
            }
          ]
        }
      ]
    },
    {
      classes: 'obObPosCashUpUiPpcItemLine-element1'
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
  name: 'OB.OBPOSCashUp.UI.ppc_collectionLines',
  kind: 'OB.UI.iterateArray',
  classes: 'obObPosCashUpUiPpcCollectionLines',
  renderLine: 'OB.OBPOSCashUp.UI.ppc_itemLine',
  renderEmpty: 'OB.UI.RenderEmpty'
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.ppc_table',
  classes: 'obObPosCashUpUiPpcTable',
  setValue: function(name, value) {
    this.$[name].setValue(value);
  }
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.ppc_salesTable',
  kind: 'OB.OBPOSCashUp.UI.ppc_table',
  classes: 'obObPosCashUpUiPpcSalesTable',
  components: [
    {
      kind: 'OB.OBPOSCashUp.UI.ppc_itemLine',
      name: 'netsales',
      classes: 'obObPosCashUpUiPpcSalesTable-netsales',
      i18nLabel: 'OBPOS_LblNetSales'
    },
    {
      kind: 'OB.OBPOSCashUp.UI.ppc_collectionLines',
      name: 'salestaxes',
      classes: 'obObPosCashUpUiPpcSalesTable-salestaxes',
      lblProperty: 'name',
      qtyProperty: 'amount'
    },
    {
      kind: 'OB.OBPOSCashUp.UI.ppc_totalsLine',
      name: 'totalsales',
      classes: 'obObPosCashUpUiPpcSalesTable-totalsales',
      i18nLabel: 'OBPOS_LblGrossSales'
    },
    {
      kind: 'OB.OBPOSCashUp.UI.ppc_lineSeparator',
      name: 'separator',
      classes: 'obObPosCashUpUiPpcSalesTable-separator'
    }
  ],
  setCollection: function(col) {
    this.$.salestaxes.setCollection(col);
  }
});

enyo.kind({
  kind: 'OB.OBPOSCashUp.UI.ppc_table',
  name: 'OB.OBPOSCashUp.UI.ppc_returnsTable',
  classes: 'obObPosCashUpUiPpcReturnsTable',
  components: [
    {
      kind: 'OB.OBPOSCashUp.UI.ppc_itemLine',
      name: 'netreturns',
      classes: 'obObPosCashUpUiPpcReturnsTable-netreturns',
      i18nLabel: 'OBPOS_LblNetReturns'
    },
    {
      kind: 'OB.OBPOSCashUp.UI.ppc_collectionLines',
      name: 'retunrnstaxes',
      classes: 'obObPosCashUpUiPpcReturnsTable-returnstaxes',
      lblProperty: 'name',
      qtyProperty: 'amount'
    },
    {
      kind: 'OB.OBPOSCashUp.UI.ppc_totalsLine',
      name: 'totalreturns',
      classes: 'obObPosCashUpUiPpcReturnsTable-totalreturns',
      i18nLabel: 'OBPOS_LblGrossReturns'
    },
    {
      kind: 'OB.OBPOSCashUp.UI.ppc_lineSeparator',
      name: 'separator',
      classes: 'obObPosCashUpUiPpcReturnsTable-separator'
    }
  ],
  setCollection: function(col) {
    this.$.retunrnstaxes.setCollection(col);
  }
});

enyo.kind({
  kind: 'OB.OBPOSCashUp.UI.ppc_table',
  name: 'OB.OBPOSCashUp.UI.ppc_totalTransactionsTable',
  components: [
    {
      kind: 'OB.OBPOSCashUp.UI.ppc_totalsLine',
      name: 'totaltransactionsline',
      classes: 'obObPosCashUpUiPpcTotalTransactionsTable-totaltransactionsline',
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
      kind: 'OB.OBPOSCashUp.UI.ppc_lineSeparator',
      name: 'separator',
      classes: 'obObPosCashUpUiPpcTotalTransactionsTable-separator'
    }
  ]
});

enyo.kind({
  kind: 'OB.OBPOSCashUp.UI.ppc_table',
  name: 'OB.OBPOSCashUp.UI.ppc_cashDropsTable',
  classes: 'obObPosCashUpUiPpcCashDropsTable',
  components: [
    {
      kind: 'OB.OBPOSCashUp.UI.ppc_collectionLines',
      name: 'drops',
      classes: 'obObPosCashUpUiPpcCashDropsTable-drops',
      lblProperty: 'description',
      qtyProperty: 'amount',
      typeProperty: 'drops'
    },
    {
      kind: 'OB.OBPOSCashUp.UI.ppc_totalsLine',
      name: 'totaldrops',
      classes: 'obObPosCashUpUiPpcCashDropsTable-totaldrops',
      i18nLabel: 'OBPOS_LblTotalWithdrawals'
    },
    {
      kind: 'OB.OBPOSCashUp.UI.ppc_lineSeparator',
      name: 'separator',
      classes: 'obObPosCashUpUiPpcCashDropsTable-separator'
    }
  ],
  setCollection: function(col) {
    this.$.drops.setCollection(col);
  }
});

enyo.kind({
  kind: 'OB.OBPOSCashUp.UI.ppc_table',
  name: 'OB.OBPOSCashUp.UI.ppc_cashDepositsTable',
  classes: 'obObPosCashUpUiPpcCashDepositsTable',
  components: [
    {
      kind: 'OB.OBPOSCashUp.UI.ppc_collectionLines',
      name: 'deposits',
      classes: 'obObPosCashUpUiPpcCashDepositsTable-description',
      lblProperty: 'description',
      qtyProperty: 'amount',
      typeProperty: 'deposits'
    },
    {
      kind: 'OB.OBPOSCashUp.UI.ppc_totalsLine',
      name: 'totaldeposits',
      classes: 'obObPosCashUpUiPpcCashDepositsTable-totaldeposits',
      i18nLabel: 'OBPOS_LblTotalDeposits'
    },
    {
      kind: 'OB.OBPOSCashUp.UI.ppc_lineSeparator',
      name: 'separator',
      classes: 'obObPosCashUpUiPpcCashDepositsTable-separator'
    }
  ],
  setCollection: function(col) {
    this.$.deposits.setCollection(col);
  }
});

enyo.kind({
  kind: 'OB.OBPOSCashUp.UI.ppc_table',
  name: 'OB.OBPOSCashUp.UI.ppc_startingsTable',
  classes: 'obObPosCashUpUiPpcStartingsTable',
  components: [
    {
      kind: 'OB.OBPOSCashUp.UI.ppc_collectionLines',
      name: 'startings',
      classes: 'obObPosCashUpUiPpcCashDepositsTable-startings',
      lblProperty: 'description',
      qtyProperty: 'amount',
      typeProperty: 'startings'
    },
    {
      kind: 'OB.OBPOSCashUp.UI.ppc_totalsLine',
      name: 'totalstartings',
      classes: 'obObPosCashUpUiPpcStartingsTable-totalstartings',
      i18nLabel: 'OBPOS_LblTotalStarting'
    },
    {
      kind: 'OB.OBPOSCashUp.UI.ppc_lineSeparator',
      name: 'separator',
      classes: 'obObPosCashUpUiPpcStartingsTable-separator'
    }
  ],
  setCollection: function(col) {
    this.$.startings.setCollection(col);
  }
});

enyo.kind({
  kind: 'OB.OBPOSCashUp.UI.ppc_table',
  name: 'OB.OBPOSCashUp.UI.ppc_cashExpectedTable',
  classes: 'obObPosCashUpUiPpcCashExpectedTable',
  components: [
    {
      kind: 'OB.OBPOSCashUp.UI.ppc_collectionLines',
      name: 'expectedPerPayment',
      classes: 'obObPosCashUpUiPpcCashExpectedTable-expectedPerPayment',
      lblProperty: 'name',
      qtyProperty: 'value',
      typeProperty: 'expected'
    },
    {
      kind: 'OB.OBPOSCashUp.UI.ppc_totalsLine',
      name: 'totalexpected',
      classes: 'obObPosCashUpUiPpcCashExpectedTable-totalexpected',
      i18nLabel: 'OBPOS_LblTotalExpected'
    },
    {
      kind: 'OB.OBPOSCashUp.UI.ppc_lineSeparator',
      name: 'separator',
      classes: 'obObPosCashUpUiPpcCashExpectedTable-separator'
    }
  ],
  setCollection: function(col) {
    this.$.expectedPerPayment.setCollection(col);
  }
});

enyo.kind({
  kind: 'OB.OBPOSCashUp.UI.ppc_table',
  name: 'OB.OBPOSCashUp.UI.ppc_cashDifferenceTable',
  classes: 'obObPosCashUpUiPpcCashDifferenceTable',
  components: [
    {
      kind: 'OB.OBPOSCashUp.UI.ppc_collectionLines',
      name: 'differencePerPayment',
      classes: 'obObPosCashUpUiPpcCashDifferenceTable-differencePerPayment',
      lblProperty: 'name',
      qtyProperty: 'value',
      typeProperty: 'difference'
    },
    {
      kind: 'OB.OBPOSCashUp.UI.ppc_totalsLine',
      name: 'totaldifference',
      classes: 'obObPosCashUpUiPpcCashDifferenceTable-totaldifference',
      i18nLabel: 'OBPOS_LblTotalDifference'
    },
    {
      kind: 'OB.OBPOSCashUp.UI.ppc_lineSeparator',
      name: 'separator',
      classes: 'obObPosCashUpUiPpcCashDifferenceTable-separator'
    }
  ],
  setCollection: function(col) {
    this.$.differencePerPayment.setCollection(col);
  }
});

enyo.kind({
  kind: 'OB.OBPOSCashUp.UI.ppc_table',
  name: 'OB.OBPOSCashUp.UI.ppc_cashCountedTable',
  components: [
    {
      kind: 'OB.OBPOSCashUp.UI.ppc_collectionLines',
      name: 'countedPerPayment',
      lblProperty: 'name',
      qtyProperty: 'value',
      typeProperty: 'counted'
    },
    {
      kind: 'OB.OBPOSCashUp.UI.ppc_totalsLine',
      name: 'totalcounted',
      i18nLabel: 'OBPOS_LblTotalCounted'
    },
    {
      kind: 'OB.OBPOSCashUp.UI.ppc_lineSeparator',
      name: 'separator'
    }
  ],
  setCollection: function(col) {
    this.$.countedPerPayment.setCollection(col);
  }
});

enyo.kind({
  kind: 'OB.OBPOSCashUp.UI.ppc_table',
  name: 'OB.OBPOSCashUp.UI.ppc_cashQtyToKeepTable',
  classes: 'obObPosCashUpUiPpcCashQtyToKeepTable',
  components: [
    {
      kind: 'OB.OBPOSCashUp.UI.ppc_collectionLines',
      name: 'qtyToKeepPerPayment',
      classes: 'obObPosCashUpUiPpcCashQtyToKeepTable-qtyToKeepPerPayment',
      lblProperty: 'name',
      qtyProperty: 'value',
      typeProperty: 'qtyToKeep'
    },
    {
      kind: 'OB.OBPOSCashUp.UI.ppc_totalsLine',
      name: 'totalqtyToKeep',
      classes: 'obObPosCashUpUiPpcCashQtyToKeepTable-totalqtyToKeep',
      i18nLabel: 'OBPOS_LblTotalQtyToKeep'
    },
    {
      kind: 'OB.OBPOSCashUp.UI.ppc_lineSeparator',
      name: 'separator',
      classes: 'obObPosCashUpUiPpcCashQtyToKeepTable-separator'
    }
  ],
  setCollection: function(col) {
    this.$.qtyToKeepPerPayment.setCollection(col);
  }
});

enyo.kind({
  kind: 'OB.OBPOSCashUp.UI.ppc_table',
  name: 'OB.OBPOSCashUp.UI.ppc_cashQtyToDepoTable',
  components: [
    {
      kind: 'OB.OBPOSCashUp.UI.ppc_collectionLines',
      name: 'qtyToDepoPerPayment',
      classes: 'obObPosCashUpUiPpcCashQtyToDepoTable-qtyToDepoPerPayment',
      lblProperty: 'name',
      qtyProperty: 'value',
      typeProperty: 'qtyToDepo'
    },
    {
      kind: 'OB.OBPOSCashUp.UI.ppc_totalsLine',
      name: 'totalqtyToDepo',
      classes: 'obObPosCashUpUiPpcCashQtyToDepoTable-totalqtyToDepo',
      i18nLabel: 'OBPOS_LblTotalQtyToDepo'
    },
    {
      kind: 'OB.OBPOSCashUp.UI.ppc_lineSeparator',
      name: 'separator',
      classes: 'obObPosCashUpUiPpcCashQtyToDepoTable-separator'
    }
  ],
  setCollection: function(col) {
    this.$.qtyToDepoPerPayment.setCollection(col);
  }
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.PostPrintClose',
  published: {
    model: null,
    summary: null
  },
  classes: 'obObPosCashUpUiPostPrintClose',
  components: [
    {
      name: 'scrollArea',
      classes: 'obObPosCashUpUiPostPrintClose-wrapper',
      components: [
        {
          classes: 'obObPosCashUpUiPostPrintClose-wrapper-components',
          components: [
            {
              name: 'reporttitle',
              classes: 'obObPosCashUpUiPostPrintClose-wrapper-components-title',
              renderHeader: function(step, count) {
                this.setContent(
                  OB.I18N.getLabel('OBPOS_LblStepNumber', [step, count]) +
                    ' ' +
                    OB.I18N.getLabel('OBPOS_LblStepPostPrintAndClose') +
                    OB.OBPOSCashUp.UI.CashUp.getTitleExtensions()
                );
              }
            },
            {
              kind: 'Scroller',
              thumb: true,
              horizontal: 'hidden',
              classes: 'obObPosCashUpUiPostPrintClose-wrapper-components-body',
              components: [
                {
                  classes:
                    'obObPosCashUpUiPostPrintClose-wrapper-components-body-container1 row-fluid',
                  components: [
                    {
                      classes:
                        'obObPosCashUpUiPostPrintClose-wrapper-components-body-container1-container1',
                      components: [
                        {
                          classes:
                            'obObPosCashUpUiPostPrintClose-wrapper-components-body-container1-container1-container1',
                          components: [
                            {
                              tag: 'img',
                              classes:
                                'obObPosCashUpUiPostPrintClose-wrapper-components-body-container1-container1-container1-img',
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
                                'obObPosCashUpUiPostPrintClose-wrapper-components-body-container1-container1-container1-store'
                            },
                            {
                              name: 'terminal',
                              classes:
                                'obObPosCashUpUiPostPrintClose-wrapper-components-body-container1-container1-container1-terminal'
                            },
                            {
                              name: 'user',
                              classes:
                                'obObPosCashUpUiPostPrintClose-wrapper-components-body-container1-container1-container1-user'
                            },
                            {
                              name: 'openingtime',
                              classes:
                                'obObPosCashUpUiPostPrintClose-wrapper-components-body-container1-container1-container1-openingtime'
                            },
                            {
                              name: 'time',
                              classes:
                                'obObPosCashUpUiPostPrintClose-wrapper-components-body-container1-container1-container1-time'
                            },
                            {
                              classes:
                                'obObPosCashUpUiPostPrintClose-wrapper-components-body-container1-container1-container1-element1'
                            }
                          ]
                        }
                      ]
                    },
                    {
                      classes:
                        'obObPosCashUpUiPostPrintClose-wrapper-components-body-container1-container1-element1'
                    }
                  ]
                },
                //FIXME: Iterate taxes
                {
                  classes:
                    'obObPosCashUpUiPostPrintClose-wrapper-components-body-container2 row-fluid',
                  components: [
                    {
                      tag: 'ul',
                      classes:
                        'obObPosCashUpUiPostPrintClose-wrapper-components-body-container2-container1',
                      components: [
                        {
                          tag: 'li',
                          classes:
                            'obObPosCashUpUiPostPrintClose-wrapper-components-body-container2-container1-container1 selected',
                          components: [
                            {
                              kind: 'OB.OBPOSCashUp.UI.ppc_salesTable',
                              name: 'sales',
                              classes:
                                'obObPosCashUpUiPostPrintClose-wrapper-components-body-container2-container1-container1-sales'
                            },
                            {
                              kind: 'OB.OBPOSCashUp.UI.ppc_returnsTable',
                              name: 'returns',
                              classes:
                                'obObPosCashUpUiPostPrintClose-wrapper-components-body-container2-container1-container1-returns'
                            },
                            {
                              kind:
                                'OB.OBPOSCashUp.UI.ppc_totalTransactionsTable',
                              name: 'totaltransactions',
                              classes:
                                'obObPosCashUpUiPostPrintClose-wrapper-components-body-container2-container1-container1-totaltransactions'
                            }
                          ]
                        }
                      ]
                    },
                    {
                      classes:
                        'obObPosCashUpUiPostPrintClose-wrapper-components-body-container2-element1'
                    },
                    {
                      classes:
                        'obObPosCashUpUiPostPrintClose-wrapper-components-body-container2-element2'
                    }
                  ]
                },
                {
                  classes:
                    'obObPosCashUpUiPostPrintClose-wrapper-components-body-container3 row-fluid',
                  components: [
                    {
                      kind: 'OB.OBPOSCashUp.UI.ppc_startingsTable',
                      name: 'startingsTable',
                      classes:
                        'obObPosCashUpUiPostPrintClose-wrapper-components-body-container3-startingsTable'
                    },
                    {
                      kind: 'OB.OBPOSCashUp.UI.ppc_cashDropsTable',
                      name: 'dropsTable',
                      classes:
                        'obObPosCashUpUiPostPrintClose-wrapper-components-body-container3-dropsTable'
                    },
                    {
                      kind: 'OB.OBPOSCashUp.UI.ppc_cashDepositsTable',
                      name: 'depositsTable',
                      classes:
                        'obObPosCashUpUiPostPrintClose-wrapper-components-body-container3-depositsTable'
                    },
                    {
                      kind: 'OB.OBPOSCashUp.UI.ppc_cashExpectedTable',
                      name: 'expectedTable',
                      classes:
                        'obObPosCashUpUiPostPrintClose-wrapper-components-body-container3-expectedTable'
                    },
                    {
                      kind: 'OB.OBPOSCashUp.UI.ppc_cashCountedTable',
                      name: 'countedTable',
                      classes:
                        'obObPosCashUpUiPostPrintClose-wrapper-components-body-container3-countedTable'
                    },
                    {
                      kind: 'OB.OBPOSCashUp.UI.ppc_cashDifferenceTable',
                      name: 'differenceTable',
                      classes:
                        'obObPosCashUpUiPostPrintClose-wrapper-components-body-container3-differenceTable'
                    },
                    {
                      kind: 'OB.OBPOSCashUp.UI.ppc_cashQtyToKeepTable',
                      name: 'qtyToKeepTable',
                      classes:
                        'obObPosCashUpUiPostPrintClose-wrapper-components-body-container3-qtyToKeepTable'
                    },
                    {
                      kind: 'OB.OBPOSCashUp.UI.ppc_cashQtyToDepoTable',
                      name: 'qtyToDepoTable',
                      classes:
                        'obObPosCashUpUiPostPrintClose-wrapper-components-body-container3-qtyToDepoTable'
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
        OB.OBPOSCashUp.UI.CashUp.getTitleExtensions()
    );

    this.model.get('cashUpReport').on(
      'add',
      function(cashUpReport) {
        this.$.openingtime.setContent(
          OB.I18N.getLabel('OBPOS_LblOpenTime') +
            ': ' +
            OB.I18N.formatDate(new Date(cashUpReport.get('creationDate'))) +
            ' - ' +
            OB.I18N.formatHour(new Date(cashUpReport.get('creationDate')))
        );
        this.$.sales.setValue('netsales', cashUpReport.get('netSales'));
        this.$.sales.setCollection(cashUpReport.get('salesTaxes'));
        this.$.sales.setValue('totalsales', cashUpReport.get('grossSales'));

        this.$.returns.setValue('netreturns', cashUpReport.get('netReturns'));
        this.$.returns.setCollection(cashUpReport.get('returnsTaxes'));
        this.$.returns.setValue(
          'totalreturns',
          cashUpReport.get('grossReturns')
        );

        this.$.totaltransactions.setValue(
          'totaltransactionsline',
          cashUpReport.get('totalRetailTransactions')
        );

        if (!OB.POS.modelterminal.get('terminal').ismaster) {
          this.cashUpReportChanged(cashUpReport);
        }
      },
      this
    );

    this.model.on(
      'change:time',
      function() {
        this.$.time.setContent(
          OB.I18N.getLabel('OBPOS_LblCloseTime') +
            ': ' +
            OB.I18N.formatDate(this.model.get('time')) +
            ' - ' +
            OB.I18N.formatHour(this.model.get('time'))
        );
      },
      this
    );
  },

  filterMovements: function(cashUpReport, isSummary) {
    var startings,
      drops,
      deposits,
      expectedSummary,
      countedSummary,
      differenceSummary,
      qtyToKeepSummary,
      qtyToDepoSummary;
    if (
      cashUpReport &&
      OB.MobileApp.model.hasPermission(
        'OBPOS_retail.cashupRemoveUnusedPayment',
        true
      )
    ) {
      this.paymentWithMovement = [];
      if (isSummary) {
        OB.UTIL.cashupAddPaymentWithSummaryMovement(
          this.paymentWithMovement,
          this.summary.expectedSummary
        );
        if (OB.MobileApp.view.currentWindow !== 'retail.cashuppartial') {
          OB.UTIL.cashupAddPaymentWithSummaryMovement(
            this.paymentWithMovement,
            this.summary.countedSummary
          );
          OB.UTIL.cashupAddPaymentWithSummaryMovement(
            this.paymentWithMovement,
            this.summary.differenceSummary
          );
          OB.UTIL.cashupAddPaymentWithSummaryMovement(
            this.paymentWithMovement,
            this.summary.qtyToKeepSummary
          );
          OB.UTIL.cashupAddPaymentWithSummaryMovement(
            this.paymentWithMovement,
            this.summary.qtyToDepoSummary
          );
          countedSummary = OB.UTIL.cashupGetPaymentWithMovement(
            this.paymentWithMovement,
            this.summary.countedSummary
          );
          differenceSummary = OB.UTIL.cashupGetPaymentWithMovement(
            this.paymentWithMovement,
            this.summary.differenceSummary
          );
          qtyToKeepSummary = OB.UTIL.cashupGetPaymentWithMovement(
            this.paymentWithMovement,
            this.summary.qtyToKeepSummary
          );
          qtyToDepoSummary = OB.UTIL.cashupGetPaymentWithMovement(
            this.paymentWithMovement,
            this.summary.qtyToDepoSummary
          );
        }
        expectedSummary = OB.UTIL.cashupGetPaymentWithMovement(
          this.paymentWithMovement,
          this.summary.expectedSummary
        );
      } else {
        OB.UTIL.cashupAddPaymentWithMovement(
          this.paymentWithMovement,
          cashUpReport.get('startings')
        );
        OB.UTIL.cashupAddPaymentWithMovement(
          this.paymentWithMovement,
          cashUpReport.get('drops')
        );
        OB.UTIL.cashupAddPaymentWithMovement(
          this.paymentWithMovement,
          cashUpReport.get('deposits')
        );
        startings = OB.UTIL.cashupGetPaymentWithMovement(
          this.paymentWithMovement,
          cashUpReport.get('startings')
        );
        drops = OB.UTIL.cashupGetPaymentWithMovement(
          this.paymentWithMovement,
          cashUpReport.get('drops')
        );
        deposits = OB.UTIL.cashupGetPaymentWithMovement(
          this.paymentWithMovement,
          cashUpReport.get('deposits')
        );
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

  cashUpReportChanged: function(cashUpReport) {
    var filtered = this.filterMovements(cashUpReport, false);
    this.$.startingsTable.setCollection(filtered.startings);
    this.$.startingsTable.setValue(
      'totalstartings',
      cashUpReport.get('totalStartings')
    );

    this.$.dropsTable.setCollection(filtered.drops);
    this.$.dropsTable.setValue('totaldrops', cashUpReport.get('totalDrops'));

    this.$.depositsTable.setCollection(filtered.deposits);
    this.$.depositsTable.setValue(
      'totaldeposits',
      cashUpReport.get('totalDeposits')
    );
  },

  summaryChanged: function() {
    var filtered = this.filterMovements(
      this.model.get('cashUpReport').at(0),
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
    var filtered = this.filterMovements(
      this.model.get('cashUpReport').at(0),
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

    this.model.on(
      'change:time',
      function() {
        this.$.time.setContent(
          OB.I18N.getLabel('OBPOS_LblCloseTime') +
            ': ' +
            OB.I18N.formatDate(this.model.get('time')) +
            ' - ' +
            OB.I18N.formatHour(this.model.get('time'))
        );
      },
      this
    );
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
      model.stepNumber('OB.CashUp.PostPrintAndClose'),
      model.stepCount()
    );
    if (
      !model.cashupStepsDefinition[model.stepIndex('OB.CashUp.CashToKeep')]
        .active
    ) {
      _.each(
        model.get('paymentList').models,
        function(model) {
          if (OB.UTIL.isNullOrUndefined(model.get('qtyToKeep'))) {
            model.set('qtyToKeep', 0);
          }
        },
        this
      );
    }
    if (OB.POS.modelterminal.get('terminal').ismaster) {
      if (OB.MobileApp.view.currentWindow === 'retail.cashuppartial') {
        var me = this;
        new OB.DS.Process(
          'org.openbravo.retail.posterminal.ProcessCashMgmtMaster'
        ).exec(
          {
            cashUpId: OB.POS.modelterminal.get('terminal').cashUpId,
            terminalSlave: OB.POS.modelterminal.get('terminal').isslave
          },
          function(data) {
            if (data && !data.exception) {
              me.owner.$.cashMaster.updateCashUpModel(model, data, function() {
                me.cashUpReportChanged(model.get('cashUpReport').at(0));
              });
            }
          }
        );
      } else {
        this.cashUpReportChanged(model.get('cashUpReport').at(0));
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
