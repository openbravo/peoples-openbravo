/*
 ************************************************************************************
 * Copyright (C) 2013-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo*/

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.MultiReceiptView',
  classes: 'obObposPointOfSaleUiMultiReceiptView',
  published: {
    order: null,
    orderList: null
  },
  components: [
    {
      classes: 'obObposPointOfSaleUiMultiReceiptView-container1',
      components: [
        {
          classes: 'obObposPointOfSaleUiMultiReceiptView-container1-container1',
          components: [
            {
              classes:
                'obObposPointOfSaleUiMultiReceiptView-container1-container1-container1',
              components: [
                {
                  classes:
                    'obObposPointOfSaleUiMultiReceiptView-container1-container1-container1-container1 row-fluid',
                  components: [
                    {
                      classes:
                        'obObposPointOfSaleUiMultiReceiptView-container1-container1-container1-container1-container1',
                      components: [
                        {
                          classes:
                            'obObposPointOfSaleUiMultiReceiptView-container1-container1-container1-container1-container1-container1',
                          components: [
                            {
                              classes:
                                'obObposPointOfSaleUiMultiReceiptView-container1-container1-container1-container1-container1-container1-element1'
                            }
                          ]
                        }
                      ]
                    }
                  ]
                },
                {
                  classes:
                    'obObposPointOfSaleUiMultiReceiptView-container1-container1-container1-container2 row-fluid',
                  components: [
                    {
                      classes:
                        'obObposPointOfSaleUiMultiReceiptView-container1-container1-container1-container2-container1',
                      components: [
                        {
                          kind: 'OB.UI.MultiOrderView',
                          name: 'multiorderview',
                          classes:
                            'obObposPointOfSaleUiMultiReceiptView-container1-container1-container1-container2-container1-multiorderview'
                        }
                      ]
                    }
                  ]
                }
              ]
            }
          ]
        }
      ]
    }
  ]
});
