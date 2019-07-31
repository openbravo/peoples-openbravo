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
      classes: 'obObposPointOfSaleUiMultiReceiptView-multiReceiptWrapper',
      components: [
        {
          classes:
            'obObposPointOfSaleUiMultiReceiptView-multiReceiptWrapper-container1',
          components: [
            {
              classes:
                'obObposPointOfSaleUiMultiReceiptView-multiReceiptWrapper-container1-container1',
              components: [
                {
                  classes:
                    'obObposPointOfSaleUiMultiReceiptView-multiReceiptWrapper-container1-container1-container1',
                  components: [
                    {
                      classes:
                        'obObposPointOfSaleUiMultiReceiptView-multiReceiptWrapper-container1-container1-container1-container1'
                    }
                  ]
                }
              ]
            }
          ]
        },
        {
          kind: 'OB.UI.MultiOrderView',
          name: 'multiorderview',
          classes:
            'obObposPointOfSaleUiMultiReceiptView-multiReceiptWrapper-multiorderview'
        }
      ]
    }
  ]
});
