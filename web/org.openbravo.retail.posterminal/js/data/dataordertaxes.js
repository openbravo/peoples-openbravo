/*
 ************************************************************************************
 * Copyright (C) 2012-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  const calculateTaxes = async function(receipt) {
    // If receipt is not editable, we regenerate taxes info instead of recalculate it
    if (!receipt.get('isEditable') && !receipt.get('forceCalculateTaxes')) {
      return regenerateTaxesInfo(receipt);
    }

    // We calculate taxes info
    initializeTaxes(receipt);
    calculateBOMLineAmount(receipt);
    await modifyProductTaxCategoryByService(receipt);
    const taxes = OB.Taxes.Pos.calculateTaxes(receipt);
    setTaxes(receipt, taxes);

    // In case of services with modifyTax flag activated in price including taxes,
    // we need to recalculate taxes info
    if (
      receipt.get('priceIncludesTax') &&
      receipt
        .get('lines')
        .filter(line => line.get('product').get('modifiedTax'))
        .map(line => {
          line.set(
            'price',
            OB.DEC.mul(
              OB.DEC.div(line.get('price'), line.get('previousLineRate')),
              line.get('lineRate')
            )
          );
          line.set('gross', OB.DEC.mul(line.get('price'), line.get('qty')));
        }).length > 0
    ) {
      const recalculatedTaxes = OB.Taxes.Pos.calculateTaxes(receipt);
      setTaxes(receipt, recalculatedTaxes);
    }
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

  const calculateBOMLineAmount = function(receipt) {
    // We use Promise.reject to show async message in case of error
    new Promise((resolve, reject) => {
      receipt.get('lines').forEach(line => {
        if (line.get('product').has('productBOM')) {
          line
            .get('product')
            .get('productBOM')
            .forEach(bomLine => {
              if (!bomLine.bomprice) {
                return reject(line);
              }

              bomLine.bomamount = OB.DEC.mul(
                bomLine.bomprice,
                bomLine.bomquantity
              );
            });
        }
      });
    }).catch(function(line) {
      const error = OB.I18N.getLabel('OBPOS_BOM_NoPrice');
      showLineTaxError(receipt, line, error);
    });
  };

  // For each service with modifyTax flag activated, look in linked product category table
  // and check if it is necessary to modify the tax category of service related lines
  function modifyProductTaxCategoryByService(receipt) {
    return new Promise(function(resolve, reject) {
      receipt.get('lines').forEach(line => {
        line.set('previousLineRate', line.get('lineRate'));
        line
          .get('product')
          .set('modifiedTax', line.get('product').has('modifiedTaxCategory'));
        line.get('product').unset('modifiedTaxCategory');
      });
      const serviceLines = receipt
        .get('lines')
        .filter(
          serviceLine =>
            serviceLine.get('product').get('modifyTax') &&
            serviceLine.get('relatedLines') &&
            !serviceLine.get('obposIsDeleted')
        );
      for (let i = 0; i < serviceLines.length; i++) {
        const serviceLine = serviceLines[i];
        const serviceId = serviceLine.get('product').get('id');
        const criteria = {
          product: serviceId
        };
        if (OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)) {
          var remoteCriteria = [
            {
              columns: ['product'],
              operator: 'equals',
              value: serviceId
            }
          ];
          criteria.remoteFilters = remoteCriteria;
        }
        OB.Dal.findUsingCache(
          'ProductServiceLinked',
          OB.Model.ProductServiceLinked,
          criteria,
          relatedProductCategories => {
            relatedProductCategories.forEach(relatedProductCategory => {
              serviceLine
                .get('relatedLines')
                .filter(
                  relatedProduct =>
                    relatedProduct.productCategory ===
                    relatedProductCategory.get('productCategory')
                )
                .forEach(relatedProduct => {
                  const relatedLine = receipt
                    .get('lines')
                    .find(line => line.id === relatedProduct.orderlineId);
                  if (relatedLine) {
                    relatedLine.get('product').set('modifiedTax', true);
                    relatedLine
                      .get('product')
                      .set(
                        'modifiedTaxCategory',
                        relatedProductCategory.get('taxCategory')
                      );
                  }
                });
            });
            resolve();
          },
          reject,
          {
            modelsAffectedByCache: ['ProductServiceLinked']
          }
        );
        return;
      }
      resolve();
    });
  }

  const regenerateTaxesInfo = function(receipt) {
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

  const setTaxes = function(receipt, taxes) {
    // We use Promise.reject to show async message in case of error
    new Promise((resolve, reject) => {
      const lineTaxError = taxes.lines.find(lineTax => lineTax.error);
      if (lineTaxError) {
        const line = receipt
          .get('lines')
          .find(line => line.id === lineTaxError.id);
        return reject(line);
      }

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
              lineRate: lineTax.lineRate,
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
              lineRate: lineTax.lineRate,
              tax: lineTax.tax,
              taxLines: lineTax.taxes
            },
            {
              silent: true
            }
          );
        }
      });
    }).catch(function(line) {
      const error = OB.I18N.getLabel('OBPOS_TaxNotFound_Message', [
        receipt.get('bp').get('name') ||
          OB.I18N.getLabel('OBPOS_LblEmptyAddress'),
        receipt.get('bp').get('shipLocName') ||
          OB.I18N.getLabel('OBPOS_LblEmptyAddress')
      ]);
      showLineTaxError(receipt, line, error);
    });
  };

  const showLineTaxError = function(receipt, line, error) {
    const title = OB.I18N.getLabel('OBPOS_TaxNotFound_Header');
    OB.error(title + ':' + error);
    line.set('hasTaxError', true, {
      silent: true
    });
    receipt.deleteLinesFromOrder([line], function() {
      OB.MobileApp.view.$.containerWindow
        .getRoot()
        .bubble('onErrorCalcLineTax', {
          line: line,
          reason: error
        });
      OB.MobileApp.view.$.containerWindow.getRoot().doShowPopup({
        popup: 'OB_UI_MessageDialog',
        args: {
          header: title,
          message: error
        }
      });
    });
  };

  OB.DATA.OrderTaxes = function(modelOfAnOrder) {
    modelOfAnOrder.calculateTaxes = async function(callback) {
      await calculateTaxes(this);
      this.trigger('paintTaxes');
      callback();
    };
  };
})();
