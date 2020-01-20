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
      const lineError = taxes.lines.find(lineTax => lineTax.error);
      if (lineError) {
        const line = receipt
          .get('lines')
          .find(line => line.id === lineError.id);
        const reason = OB.I18N.getLabel('OBPOS_TaxNotFound_Message', [
          receipt.get('bp').get('name') ||
            OB.I18N.getLabel('OBPOS_LblEmptyAddress'),
          receipt.get('bp').get('shipLocName') ||
            OB.I18N.getLabel('OBPOS_LblEmptyAddress')
        ]);
        showTaxCalculationError(receipt, line, reason);
      } else {
        receipt.set(
          {
            gross: taxes.header.grossAmount,
            net: taxes.header.netAmount,
            taxes: taxes.header.taxes
          },
          {
            silent: true
          }
        );
        receipt.get('lines').forEach(line => {
          const lineTax = taxes.lines.find(lineTax => lineTax.id === line.id);
          if (receipt.get('priceIncludesTax')) {
            line.set(
              {
                net: lineTax.netAmount,
                discountedNet: lineTax.netAmount,
                pricenet: lineTax.netPrice,
                tax: lineTax.tax,
                taxLines: lineTax.taxes
              },
              {
                silent: true
              }
            );
          } else {
            line.set(
              {
                gross: lineTax.grossAmount,
                discountedGross: lineTax.grossAmount,
                tax: lineTax.tax,
                taxLines: lineTax.taxes
              },
              {
                silent: true
              }
            );
          }
        });
      }
    } catch (error) {
      throw OB.I18N.getLabel('OBPOS_TaxCalculationError_Message');
    }
  };

  const showTaxCalculationError = function(receipt, line, reason) {
    var title = OB.I18N.getLabel('OBPOS_TaxNotFound_Header');
    OB.error(title + ':' + reason);
    receipt.set(
      'taxes',
      {},
      {
        silent: true
      }
    );
    line.set('hasTaxError', true, {
      silent: true
    });
    receipt.deleteLinesFromOrder([line], function() {
      OB.MobileApp.view.$.containerWindow
        .getRoot()
        .bubble('onErrorCalcLineTax', {
          line: line,
          reason: reason
        });
      OB.MobileApp.view.$.containerWindow.getRoot().doShowPopup({
        popup: 'OB_UI_MessageDialog',
        args: {
          header: title,
          message: reason
        }
      });
    });
  };

  OB.DATA.OrderTaxes = function(modelOfAnOrder) {
    modelOfAnOrder.calculateTaxes = function(callback) {
      calculateTaxes(this);
      this.trigger('paintTaxes');
      callback();
    };
  };
})();
