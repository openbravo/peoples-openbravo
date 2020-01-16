/*
 ************************************************************************************
 * Copyright (C) 2012-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  const calculateTaxes = function(receipt) {
    try {
      const taxes = OB.Taxes.Pos.calculateTaxes(receipt);
      receipt.set('gross', taxes.header.gross);
      receipt.set('net', taxes.header.net);
      receipt.set('taxes', taxes.header.taxes);
      receipt.get('lines').forEach(line => {
        const lineTax = taxes.lines.find(lineTax => lineTax.id === line.id);
        if (lineTax) {
          if (receipt.get('priceIncludesTax')) {
            line.set({
              net: lineTax.net,
              discountedNet: lineTax.net,
              tax: lineTax.tax,
              taxLines: lineTax.taxes
            });
          } else {
            line.set({
              gross: lineTax.gross,
              discountedGross: lineTax.gross,
              tax: lineTax.tax,
              taxLines: lineTax.taxes
            });
          }
        } else {
          throw OB.I18N.getLabel('OBPOS_TaxNotFound_Message', [
            receipt.get('bp').get('name') ||
              OB.I18N.getLabel('OBPOS_LblEmptyAddress'),
            receipt.get('bp').get('shipLocName') ||
              OB.I18N.getLabel('OBPOS_LblEmptyAddress')
          ]);
        }
      });
    } catch (error) {
      throw OB.I18N.getLabel('OBPOS_TaxCalculationError_Message');
    }
  };

  OB.DATA.OrderTaxes = function(modelOfAnOrder) {
    modelOfAnOrder.calculateTaxes = function(callback) {
      calculateTaxes(this);
      this.trigger('paintTaxes');
      callback();
    };
  };
})();
