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
    if (!receipt.get('isEditable') && !receipt.get('forceCalculateTaxes')) {
      return regenerateTaxes(receipt);
    }

    try {
      initializeTaxes(receipt);
      const taxes = OB.Taxes.Pos.calculateTaxes(receipt);
      setTaxes(receipt, taxes);
    } catch (error) {
      showLineTaxError(receipt, error);
    }
  };

  const regenerateTaxes = function(receipt) {
    const value = _.map(receipt.get('lines').models, function(line) {
      var discAmt,
        lineObj = {};
      _.forEach(line.get('taxes'), function(tax) {
        lineObj[tax.taxId] = {
          amount: tax.taxAmount,
          name: tax.identifier,
          net: tax.taxableAmount,
          rate: tax.taxRate
        };
        line.set(
          {
            taxLines: lineObj,
            taxAmount: OB.DEC.add(line.get('taxAmount') || 0, tax.taxAmount)
          },
          {
            silent: true
          }
        );
      });
      discAmt = line.get('promotions').reduce(function(memo, disc) {
        return OB.DEC.add(memo, disc.actualAmt || disc.amt || 0);
      }, 0);
      line.set(
        {
          net: receipt.get('priceIncludesTax')
            ? line.get('linenetamount')
            : line.get('net')
        },
        {
          silent: true
        }
      );
      if (receipt.get('priceIncludesTax')) {
        line.set(
          {
            discountedNet: line.get('net'),
            discountedLinePrice: OB.DEC.add(
              line.get('net'),
              line.get('taxAmount')
            )
          },
          {
            silent: true
          }
        );
      } else {
        line.set(
          {
            gross: OB.DEC.add(line.get('net'), line.get('taxAmount')),
            discountedNet: OB.DEC.sub(line.get('net'), discAmt),
            discountedNetPrice: OB.DEC.sub(line.get('net'), discAmt),
            discountedGross: OB.DEC.add(
              OB.DEC.sub(line.get('net'), discAmt),
              line.get('taxAmount')
            )
          },
          {
            silent: true
          }
        );
      }
    });

    if (value.length !== receipt.get('lines').length) {
      OB.debug('The number of original lines of the receipt has change!');
      return;
    }

    receipt.set(
      'net',
      receipt.get('lines').reduce(function(memo, line) {
        return OB.DEC.add(memo, line.get('discountedNet'));
      }, 0),
      {
        silent: true
      }
    );

    var taxesColl = {};
    _.forEach(receipt.get('receiptTaxes'), function(receiptTax) {
      var taxObj = {
        amount: receiptTax.amount,
        cascade: receiptTax.cascade,
        docTaxAmount: receiptTax.docTaxAmount,
        lineNo: receiptTax.lineNo,
        name: receiptTax.name,
        net: receiptTax.net,
        rate: receiptTax.rate,
        taxBase: receiptTax.taxBase
      };
      taxesColl[receiptTax.taxid] = taxObj;
    });
    receipt.set('taxes', taxesColl);
  };

  const initializeTaxes = function(receipt) {
    receipt.set(
      {
        taxes: []
      },
      {
        silent: true
      }
    );
  };

  const setTaxes = function(receipt, taxes) {
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
            pricenet: lineTax.netUnitPrice,
            lineRate: lineTax.taxRate,
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
            lineRate: lineTax.taxRate,
            tax: lineTax.tax,
            taxLines: lineTax.taxes
          },
          {
            silent: true
          }
        );
      }
    });
  };

  const showLineTaxError = function(receipt, error) {
    // We use Promise.reject to show async message in case of error
    new Promise((resolve, reject) => {
      const lineIdWithError = error.message.substring(
        error.message.length - 32
      );
      const line =
        receipt.get('lines').find(line => line.id === lineIdWithError) ||
        receipt.get('lines').models[receipt.get('lines').length - 1];
      return reject(line);
    }).catch(function(line) {
      const taxCategoryName = OB.Taxes.Pos.taxCategory
        .concat(OB.Taxes.Pos.taxCategoryBOM)
        .find(
          taxCategory =>
            taxCategory.id === line.get('product').get('taxCategory')
        ).name;

      const errorTitle = OB.I18N.getLabel('OBPOS_TaxNotFound_Header');
      const errorMessage = OB.I18N.getLabel(
        'OBPOS_TaxWithCategoryNotFound_Message',
        [
          receipt.get('bp').get('name') ||
            OB.I18N.getLabel('OBPOS_LblEmptyAddress'),
          receipt.get('bp').get('shipLocName') ||
            OB.I18N.getLabel('OBPOS_LblEmptyAddress'),
          taxCategoryName
        ]
      );
      OB.error(errorTitle + ':' + errorMessage);

      line.set('hasTaxError', true, {
        silent: true
      });
      receipt.deleteLinesFromOrder([line], function() {
        OB.MobileApp.view.$.containerWindow
          .getRoot()
          .bubble('onErrorCalcLineTax', {
            line: line,
            reason: errorMessage
          });
        OB.MobileApp.view.$.containerWindow.getRoot().doShowPopup({
          popup: 'OB_UI_MessageDialog',
          args: {
            header: errorTitle,
            message: errorMessage
          }
        });
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
