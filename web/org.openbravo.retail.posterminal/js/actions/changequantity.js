/*
 ************************************************************************************
 * Copyright (C) 2019-2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  var isQtyEditable = function(line) {
    if (!line) {
      return false;
    }
    var product = line.get('product');
    var qtyeditable = product.get('groupProduct');
    qtyeditable = qtyeditable && line.get('isEditable');
    qtyeditable = qtyeditable && !product.get('isSerialNo');
    qtyeditable =
      qtyeditable &&
      !(product.get('productType') === 'S' && product.get('isLinkedToProduct'));
    return qtyeditable;
  };

  var AbstractCommandQuantity = function(args) {
    OB.Actions.AbstractAction.call(this, args);
    this.calculateToAdd = args.calculateToAdd;
    this.isActive = function(view) {
      var isEditable = view.state.readCommandState({
        name: 'receipt.isEditable'
      });
      var selectedReceiptLine = view.state.readCommandState({
        name: 'selectedReceiptLine'
      });
      var selectedReceiptLines = view.state.readCommandState({
        name: 'selectedReceiptLines'
      });

      var active = isEditable;
      active = active && isQtyEditable(selectedReceiptLine);
      active =
        active &&
        selectedReceiptLines &&
        selectedReceiptLines.every(function(l) {
          return isQtyEditable(l);
        });
      return active;
    };
    this.command = function(view) {
      var me = this;
      var cancelQtyChange = false;
      var cancelQtyChangeReturn = false;

      var editboxvalue = view.state.readCommandState({
        name: 'editbox'
      });
      var isEditable = view.state.readCommandState({
        name: 'receipt.isEditable'
      });
      var selectedReceiptLine = view.state.readCommandState({
        name: 'selectedReceiptLine'
      });
      var selectedReceiptLines = view.state.readCommandState({
        name: 'selectedReceiptLines'
      });
      var value = OB.I18N.parseNumber(editboxvalue || '1');
      var receipt = view.model.get('order');

      var validateQuantity = function() {
        if (
          OB.MobileApp.model.hasPermission('OBPOS_maxQtyUsingKeyboard', true) &&
          value >=
            OB.I18N.parseNumber(
              OB.MobileApp.model.hasPermission(
                'OBPOS_maxQtyUsingKeyboard',
                true
              )
            )
        ) {
          return OB.DIALOGS.confirm({
            title: OB.I18N.getLabel('OBPOS_maxQtyUsingKeyboardHeader'),
            message: OB.I18N.getLabel('OBPOS_maxQtyUsingKeyboardBody', [value])
          });
        } else {
          return Promise.resolve();
        }
      };

      if (
        !selectedReceiptLine ||
        !receipt.get('lines').models.find(function(l) {
          return selectedReceiptLine.get('id') === l.get('id');
        })
      ) {
        return;
      }

      if (!selectedReceiptLines || selectedReceiptLines.length === 0) {
        return;
      } else {
        var selectedLines = [];
        selectedReceiptLines.forEach(function(selectedLine) {
          if (
            receipt.get('lines').models.find(function(l) {
              return selectedLine.get('id') === l.get('id');
            })
          ) {
            selectedLines.push(selectedLine);
          }
        });
        if (selectedLines.length > 0) {
          selectedReceiptLines = selectedLines;
        } else {
          return;
        }
      }

      if (!isFinite(value)) {
        return;
      }

      if (isEditable === false) {
        view.doShowPopup({
          popup: 'modalNotEditableOrder'
        });
        return;
      }

      if (
        selectedReceiptLines.find(function(line) {
          return line.get('product').get('isEditableQty') === false;
        })
      ) {
        view.doShowPopup({
          popup: 'modalNotEditableLine'
        });
        return;
      }

      // Check if is trying to remove delivered units or to modify negative lines in a cancel and replace ticket.
      // In that case stop the flow and show an error popup.
      if (receipt.get('replacedorder')) {
        selectedReceiptLines.forEach(function(line) {
          var oldqty = line.get('qty');
          const newqty = OB.DEC.add(
            oldqty,
            this.calculateToAdd(
              receipt,
              oldqty,
              value,
              line.get('product').get('uOMstandardPrecision')
            ),
            line.get('product').get('uOMstandardPrecision')
          );

          if (oldqty > 0 && newqty < line.get('remainingQuantity')) {
            cancelQtyChange = true;
          } else if (oldqty < 0 && line.get('remainingQuantity')) {
            cancelQtyChangeReturn = true;
          }
        }, this);

        if (cancelQtyChange) {
          OB.UTIL.showConfirmation.display(
            OB.I18N.getLabel('OBMOBC_Error'),
            OB.I18N.getLabel('OBPOS_CancelReplaceQtyEdit')
          );
          return;
        }
        if (cancelQtyChangeReturn) {
          OB.UTIL.showConfirmation.display(
            OB.I18N.getLabel('OBMOBC_Error'),
            OB.I18N.getLabel('OBPOS_CancelReplaceQtyEditReturn')
          );
          return;
        }
      }

      var valueBigDecimal = OB.DEC.toBigDecimal(value);
      if (
        valueBigDecimal.scale() >
        selectedReceiptLine.get('product').get('uOMstandardPrecision')
      ) {
        OB.UTIL.showError(
          OB.I18N.getLabel('OBPOS_StdPrecisionLimitError', [
            selectedReceiptLine.get('product').get('uOMstandardPrecision')
          ])
        );
        return;
      }

      // Validate based new quantity of the selected line
      if (
        receipt.validateAllowSalesWithReturn(
          OB.DEC.add(
            selectedReceiptLine.get('qty'),
            this.calculateToAdd(
              receipt,
              selectedReceiptLine.get('qty'),
              value,
              selectedReceiptLine.get('product').get('uOMstandardPrecision')
            ),
            selectedReceiptLine.get('product').get('uOMstandardPrecision')
          ),
          false,
          selectedReceiptLines
        )
      ) {
        return;
      }

      if (!receipt.validateAvoidBlindAndReturnLines(false)) {
        return;
      }

      validateQuantity()
        .then(
          function() {
            var selection = [],
              deletedlines = [],
              finalCallback;

            if (me.pendingProcess) {
              return;
            }
            me.pendingProcess = true;

            finalCallback = _.after(selectedReceiptLines.length, function() {
              if (deletedlines.length > 0) {
                view.deleteLine(view, {
                  selectedReceiptLines: deletedlines,
                  callback: function() {
                    me.pendingProcess = false;
                  }
                });
              } else {
                me.pendingProcess = false;
              }
              receipt.set('multipleUndo', null);
              receipt.trigger('scan');
              if (selection.length > 0) {
                view.setMultiSelectionItems(view, {
                  selection: selection
                });
              }
            });
            receipt.set('undo', null);
            if (selectedReceiptLines && selectedReceiptLines.length > 1) {
              receipt.set('multipleUndo', true);
            }
            selectedReceiptLines.forEach(function(line) {
              selection.push(line);
              const toadd = this.calculateToAdd(
                receipt,
                line.get('qty'),
                value,
                line.get('product').get('uOMstandardPrecision')
              );
              if (toadd !== 0) {
                const newqty = OB.DEC.add(
                  line.get('qty'),
                  toadd,
                  line.get('product').get('uOMstandardPrecision')
                );
                if (receipt.get('orderType') !== 1 && newqty === 0) {
                  // If final quantity will be 0 then request approval
                  selection.pop();
                  deletedlines.push(line);
                  finalCallback();
                } else if (newqty > 0) {
                  view.addProductToOrder(view, {
                    product: line.get('product'),
                    qty: toadd,
                    options: {
                      line: line,
                      blockAddProduct: true
                    },
                    callback: function() {
                      finalCallback();
                    }
                  });
                } else {
                  receipt.checkReturnableProducts(
                    selectedReceiptLines,
                    line,
                    function(success) {
                      if (!success) {
                        finalCallback();
                        return;
                      }
                      view.addProductToOrder(view, {
                        product: line.get('product'),
                        qty: toadd,
                        options: {
                          line: line,
                          blockAddProduct: true
                        },
                        callback: function() {
                          finalCallback();
                        }
                      });
                    }
                  );
                }
              } else {
                finalCallback();
              }
            }, this);
          }.bind(this)
        )
        .catch(function(error) {
          // Dialog cancelled, finish the action
        });
    };
  };

  OB.MobileApp.actionsRegistry.register(
    new AbstractCommandQuantity({
      window: 'retail.pointofsale',
      name: 'changeQuantity',
      properties: {
        i18nContent: 'OBMOBC_KbQuantity'
      },
      calculateToAdd: function(receipt, qty, value, scale) {
        return receipt.get('orderType') === 1
          ? OB.DEC.add(value, qty, scale)
          : OB.DEC.sub(value, qty, scale);
      }
    })
  );

  OB.MobileApp.actionsRegistry.register(
    new AbstractCommandQuantity({
      window: 'retail.pointofsale',
      name: 'addQuantity',
      properties: {
        label: '+'
      },
      calculateToAdd: function(receipt, qty, value) {
        return value || 1;
      }
    })
  );

  OB.MobileApp.actionsRegistry.register(
    new AbstractCommandQuantity({
      window: 'retail.pointofsale',
      name: 'removeQuantity',
      properties: {
        i18nContent: 'OBMOBC_MinusSign'
      },
      calculateToAdd: function(receipt, qty, value) {
        return -(value || 1);
      }
    })
  );
})();
