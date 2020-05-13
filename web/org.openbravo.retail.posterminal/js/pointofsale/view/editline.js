/*
 ************************************************************************************
 * Copyright (C) 2012-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, _ */
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.LineProperty',
  classes: 'obObposPointOfSaleUiLineProperty',
  components: [
    {
      name: 'propertyLabel',
      classes: 'obObposPointOfSaleUiLineProperty-propertyLabel'
    },
    {
      classes: 'obObposPointOfSaleUiLineProperty-propertyValueWrapper',
      components: [
        {
          tag: 'span',
          name: 'propertyValue',
          classes:
            'obObposPointOfSaleUiLineProperty-propertyValueWrapper-propertyValue'
        }
      ]
    }
  ],
  render: function(model) {
    if (model) {
      this.$.propertyValue.setContent(model.get(this.propertyToPrint));
    } else {
      this.$.propertyValue.setContent('');
    }
  },
  initComponents: function() {
    this.inherited(arguments);
    this.$.propertyLabel.setContent(OB.I18N.getLabel(this.I18NLabel));
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.LinePropertyDiv',
  classes: 'obObposPointOfSaleUiLinePropertyDiv',
  components: [
    {
      name: 'propertyLabel',
      classes: 'obObposPointOfSaleUiLinePropertyDiv-propertyLabel'
    },
    {
      classes: 'obObposPointOfSaleUiLinePropertyDiv-propertyValueWrapper',
      components: [
        {
          tag: 'span',
          name: 'propertyValue',
          classes:
            'obObposPointOfSaleUiLinePropertyDiv-propertyValueWrapper-propertyValue'
        }
      ]
    }
  ],
  render: function(model) {
    if (model) {
      this.$.propertyValue.setContent(model.get(this.propertyToPrint));
    } else {
      this.$.propertyValue.setContent('');
    }
  },
  initComponents: function() {
    this.inherited(arguments);
    this.$.propertyLabel.setContent(OB.I18N.getLabel(this.I18NLabel));
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.EditLine',
  classes: 'obObposPointOfSaleUiEditLine',
  propertiesToShow: [
    {
      kind: 'OB.OBPOSPointOfSale.UI.LineProperty',
      position: 10,
      name: 'descLine',
      I18NLabel: 'OBPOS_LineDescription',
      classes: 'obObposPointOfSaleUiEditLine-propertiesToShow-descLine',
      render: function(line) {
        if (line) {
          this.$.propertyValue.setContent(
            line.get('product').get('_identifier')
          );
        } else {
          this.$.propertyValue.setContent('');
        }
      }
    },
    {
      kind: 'OB.OBPOSPointOfSale.UI.LineProperty',
      position: 15,
      name: 'returnReasonLine',
      I18NLabel: 'OBPOS_ReturnReason',
      classes: 'obObposPointOfSaleUiEditLine-propertiesToShow-returnReasonLine',
      render: function(line) {
        if (line && line.get('returnReason')) {
          this.$.propertyValue.setContent(line.get('returnReasonName'));
          this.show();
        } else {
          this.hide();
        }
      }
    },
    {
      kind: 'OB.OBPOSPointOfSale.UI.LineProperty',
      position: 20,
      name: 'qtyLine',
      I18NLabel: 'OBPOS_LineQuantity',
      classes: 'obObposPointOfSaleUiEditLine-propertiesToShow-qtyLine',
      multiSelection: false,
      render: function(line) {
        if (line) {
          if (line.get('qty') === 0) {
            this.$.propertyValue.setContent(
              OB.I18N.getLabel('OBPOS_lblMultiSelectQuantity')
            );
          } else {
            this.$.propertyValue.setContent(
              line.printQty() +
                (this.multiSelection
                  ? ' ' + OB.I18N.getLabel('OBPOS_lblMultiSelectPerLines')
                  : '')
            );
          }
        } else {
          this.$.propertyValue.setContent('');
        }
      }
    },
    {
      kind: 'OB.OBPOSPointOfSale.UI.LineProperty',
      position: 25,
      name: 'priceStdLine',
      I18NLabel: 'OBPOS_LinePriceStd',
      classes: 'obObposPointOfSaleUiEditLine-propertiesToShow-priceStdLine',
      render: function(line) {
        if (line) {
          this.$.propertyValue.setContent(
            OB.I18N.formatCurrency(line.get('product').get('standardPrice'))
          );
        } else {
          this.$.propertyValue.setContent('');
        }
      }
    },
    {
      kind: 'OB.OBPOSPointOfSale.UI.LineProperty',
      position: 30,
      name: 'priceLine',
      I18NLabel: 'OBPOS_LineUnitPrice',
      classes: 'obObposPointOfSaleUiEditLine-propertiesToShow-priceLine',
      render: function(line) {
        if (line) {
          if (this.multiSelection) {
            this.$.propertyValue.setContent(
              line.get('hasPrice')
                ? line.printPrice() +
                    ' ' +
                    OB.I18N.getLabel('OBPOS_lblMultiSelectPerLines')
                : OB.I18N.getLabel('OBPOS_lblMultiSelectPrice')
            );
          } else {
            this.$.propertyValue.setContent(line.printPrice());
          }
        } else {
          this.$.propertyValue.setContent('');
        }
      }
    },
    {
      kind: 'OB.OBPOSPointOfSale.UI.LineProperty',
      position: 40,
      name: 'discountedAmountLine',
      I18NLabel: 'OBPOS_LineDiscount',
      classes:
        'obObposPointOfSaleUiEditLine-propertiesToShow-discountedAmountLine',
      multiSelection: false,
      render: function(line) {
        if (line) {
          if (this.multiSelection) {
            var lineDisc = line.printDiscount();
            this.$.propertyValue.setContent(
              line.get('hasDiscount')
                ? lineDisc +
                    (lineDisc !== ''
                      ? ' ' + OB.I18N.getLabel('OBPOS_lblMultiSelectPerLines')
                      : '')
                : OB.I18N.getLabel('OBPOS_lblMultiSelectDiscount')
            );
          } else {
            var discount = line.getTotalAmountOfPromotions();
            if (discount === 0) {
              this.$.propertyValue.setContent('');
            } else {
              this.$.propertyValue.setContent(OB.I18N.formatCurrency(discount));
            }
          }
        } else {
          this.$.propertyValue.setContent('');
        }
      }
    },
    {
      kind: 'OB.OBPOSPointOfSale.UI.LineProperty',
      position: 50,
      name: 'grossLine',
      I18NLabel: 'OBPOS_LineTotal',
      classes: 'obObposPointOfSaleUiEditLine-propertiesToShow-grossLine',
      render: function(line) {
        if (line) {
          if (line.get('editlinetotal')) {
            // Is has been calculated, (by multiline)
            this.$.propertyValue.setContent(
              OB.I18N.formatCurrency(line.get('editlinetotal'))
            );
          } else if (line.get('priceIncludesTax')) {
            this.$.propertyValue.setContent(line.printTotalLine());
          } else {
            this.$.propertyValue.setContent(
              OB.I18N.formatCurrency(line.get('discountedNet'))
            );
          }
        } else {
          this.$.propertyValue.setContent('');
        }
      }
    },
    {
      kind: 'OB.OBPOSPointOfSale.UI.LineProperty',
      position: 55,
      name: 'storeLine',
      I18NLabel: 'OBPOS_LblStore',
      render: function(line) {
        if (line && line.get('organization')) {
          this.$.propertyValue.setContent(line.get('organization').name);
          this.show();
        } else {
          this.hide();
        }
      }
    },
    {
      kind: 'OB.OBPOSPointOfSale.UI.LineProperty',
      position: 60,
      name: 'warehouseLine',
      I18NLabel: 'OBPOS_LineWarehouse',
      classes: 'obObposPointOfSaleUiEditLine-propertiesToShow-warehouseLine',
      render: function(line) {
        if (line && line.get('warehouse')) {
          this.$.propertyValue.setContent(line.get('warehouse').warehousename);
        } else {
          this.$.propertyValue.setContent(
            OB.MobileApp.model.get('warehouses')[0].warehousename
          );
        }
      }
    },
    {
      kind: 'OB.OBPOSPointOfSale.UI.LineProperty',
      position: 70,
      name: 'deliverableLine',
      I18NLabel: 'OBPOS_LineDeliverable',
      classes: 'obObposPointOfSaleUiEditLine-propertiesToShow-deliverableLine',
      render: function(line) {
        if (this.owner.owner.hideDeliveryLabel) {
          this.hide();
        } else {
          this.show();
          if (this.owner.owner.hideDeliveryButton) {
            this.$.propertyValue.setContent(
              OB.I18N.getLabel('OBPOS_lblMultiSelectValues')
            );
          } else if (line && line.get('obposCanbedelivered')) {
            this.$.propertyValue.setContent(OB.I18N.getLabel('OBMOBC_LblYes'));
            this.owner.owner.$.actionButtonsContainer.$.canDeliver.setContent(
              OB.I18N.getLabel('OBPOS_SetAsUndeliverable')
            );
          } else {
            this.$.propertyValue.setContent(OB.I18N.getLabel('OBMOBC_LblNo'));
            this.owner.owner.$.actionButtonsContainer.$.canDeliver.setContent(
              OB.I18N.getLabel('OBPOS_SetAsDeliverable')
            );
          }
        }
      }
    }
  ],
  actionButtons: [
    {
      kind: 'OB.UI.ActionButton',
      name: 'deleteLine',
      classes:
        'obObposPointOfSaleUiEditLine-propertiesToShow-general obObposPointOfSaleUiEditLine-actionButtons-deleteLine',
      action: {
        window: 'retail.pointofsale',
        name: 'deleteLine'
      },
      processesToListen: ['calculateReceipt', 'addProduct'],
      disabled: false,
      disableButton: function() {
        this.updateDisabled(true);
      },
      enableButton: function() {
        this.updateDisabled(false);
      },
      updateDisabled: function(value) {
        if (
          OB.UTIL.ProcessController.getProcessesInExecByOBj(this).length > 0 &&
          !value
        ) {
          value = true;
        }
        this.disabled = value;
        this.setDisabled(value);
      }
    },
    {
      kind: 'OB.UI.ActionButton',
      name: 'descriptionButton',
      classes:
        'obObposPointOfSaleUiEditLine-propertiesToShow-general obObposPointOfSaleUiEditLine-actionButtons-descriptionButton',
      action: {
        window: 'retail.pointofsale',
        name: 'editLine'
      }
    },
    {
      kind: 'OB.UI.ActionButton',
      name: 'returnLine',
      classes:
        'obObposPointOfSaleUiEditLine-propertiesToShow-general obObposPointOfSaleUiEditLine-actionButtons-returnLine',
      action: {
        window: 'retail.pointofsale',
        name: 'returnLine'
      }
    },
    {
      kind: 'OB.UI.ActionButton',
      name: 'splitlineButton',
      classes:
        'obObposPointOfSaleUiEditLine-propertiesToShow-general obObposPointOfSaleUiEditLine-actionButtons-splitlineButton',
      action: {
        window: 'retail.pointofsale',
        name: 'splitLine'
      }
    },
    {
      kind: 'OB.UI.Button',
      name: 'showRelatedServices',
      classes:
        'obObposPointOfSaleUiEditLine-propertiesToShow-general obObposPointOfSaleUiEditLine-actionButtons-showRelatedServices obUiActionButton',
      /* TODO: obUiActionButton class to be removed once it become a true OB.UI.ActionButton */
      permission: 'OBPOS_ActionButtonShowRelatedServices',
      i18nContent: 'OBPOS_RelatedServices',
      tap: function(inSender, inEvent) {
        var product = this.owner.owner.line.get('product');
        if (product) {
          OB.UI.SearchProductCharacteristic.prototype.filtersCustomClear();
          OB.UI.SearchProductCharacteristic.prototype.filtersCustomAdd(
            new OB.UI.SearchServicesFilter({
              text: this.owner.owner.selectedModels
                .filter(function(line) {
                  return line.get('hasRelatedServices');
                })
                .map(function(line) {
                  return line.get('product').get('_identifier');
                })
                .join(', '),
              productList: this.owner.owner.selectedModels
                .filter(function(line) {
                  return line.get('hasRelatedServices');
                })
                .map(function(line) {
                  return line.get('product').get('id');
                }),
              orderlineList: this.owner.owner.selectedModels.filter(function(
                line
              ) {
                return line.get('hasRelatedServices');
              })
            })
          );
          var me = this;
          setTimeout(function() {
            me.bubble('onTabChange', {
              tabPanel: 'searchCharacteristic'
            });
            me.bubble('onSelectFilter', {
              params: {
                skipProductCharacteristic: true
              }
            });
            me.owner.owner.selectedModels
              .filter(function(line) {
                return line.get('hasRelatedServices');
              })
              .forEach(function(l) {
                l.set('obposServiceProposed', true);
              });
          }, 1);
          this.addRemoveClass(
            'obObposPointOfSaleUiEditLine-actionButtons-showRelatedServices_unreviewed',
            false
          );
          this.addRemoveClass(
            'obObposPointOfSaleUiEditLine-actionButtons-showRelatedServices_reviewed',
            true
          );
        }
      }
    },
    {
      kind: 'OB.UI.Button',
      name: 'removeDiscountButton',
      i18nContent: 'OBPOS_LblRemoveDiscount',
      showing: false,
      classes:
        'obObposPointOfSaleUiEditLine-propertiesToShow-general obObposPointOfSaleUiEditLine-actionButtons-removeDiscountButton obUiActionButton',
      /* TODO: obUiActionButton class to be removed once it become a true OB.UI.ActionButton */
      permission: 'OBPOS_ActionButtonRemoveDiscount',
      tap: function() {
        var i,
          lineModel,
          selectedLines,
          checkFilter,
          linesWithPromotionsLength = 0;

        checkFilter = function(prom) {
          return OB.Discounts.Pos.getManualPromotions().includes(
            prom.discountType
          );
        };

        selectedLines = _.filter(
          this.owner.owner.selectedModels,
          function(line) {
            return (
              this.owner.owner.receipt.get('isEditable') &&
              line.get('isEditable')
            );
          },
          this
        );

        for (i = 0; i < selectedLines.length; i++) {
          lineModel = selectedLines[i];
          if (
            lineModel.get('promotions') &&
            lineModel.get('promotions').length > 0
          ) {
            linesWithPromotionsLength = _.filter(
              lineModel.get('promotions'),
              checkFilter
            ).length;
            if (linesWithPromotionsLength > 0) {
              this.owner.owner.doShowPopup({
                popup: 'modalDeleteDiscount',
                args: {
                  receipt: this.owner.owner.receipt,
                  selectedLines: selectedLines,
                  selectedLine: lineModel,
                  context: this
                }
              });
              break;
            }
          }
        }
      },
      init: function(model) {
        this.model = model;
      }
    },
    {
      kind: 'OB.UI.ActionButton',
      name: 'checkStockButton',
      classes:
        'obObposPointOfSaleUiEditLine-propertiesToShow-general obObposPointOfSaleUiEditLine-actionButtons-checkStockButton',
      action: {
        window: 'retail.pointofsale',
        name: 'showStockLine'
      }
    },
    {
      kind: 'OB.OBPOSPointOfSale.UI.EditLine.OpenAttributeButton',
      name: 'openAttributeButton',
      classes:
        'obObposPointOfSaleUiEditLine-propertiesToShow-general obObposPointOfSaleUiEditLine-actionButtons-openAttributeButton obUiActionButton',
      showing: false
    },
    {
      kind: 'OB.UI.Button',
      name: 'addAssociationsButton',
      i18nContent: 'OBPOS_AddAssociations',
      showing: false,
      classes:
        'obObposPointOfSaleUiEditLine-propertiesToShow-general obObposPointOfSaleUiEditLine-actionButtons-addAssociationsButton obUiActionButton',
      /* TODO: obUiActionButton class to be removed once it become a true OB.UI.ActionButton */
      tap: function() {
        this.owner.owner.doShowPopup({
          popup: 'OBPOS_modalAssociateTickets',
          args: {
            receipt: this.owner.owner.receipt,
            selectedLines: this.owner.owner.selectedModels
          }
        });
      }
    },
    {
      kind: 'OB.UI.Button',
      name: 'removeAssociationsButton',
      i18nContent: 'OBPOS_RemoveAssociations',
      showing: false,
      classes:
        'obObposPointOfSaleUiEditLine-propertiesToShow-general obObposPointOfSaleUiEditLine-actionButtons-removeAssociationsButton obUiActionButton',
      /* TODO: obUiActionButton class to be removed once it become a true OB.UI.ActionButton */
      tap: function() {
        this.owner.owner.doShowPopup({
          popup: 'OBPOS_modalRemoveAssociatedTickets',
          args: {
            receipt: this.owner.owner.receipt,
            selectedLine: this.owner.owner.selectedModels[0]
          }
        });
      }
    },
    {
      kind: 'OB.UI.Button',
      name: 'canDeliver',
      classes:
        'obObposPointOfSaleUiEditLine-propertiesToShow-general obObposPointOfSaleUiEditLine-actionButtons-canDeliver obUiActionButton',
      /* TODO: obUiActionButton class to be removed once it become a true OB.UI.ActionButton */
      content: '-',
      tap: function(inSender, inEvent) {
        var me = this,
          deliveredLines = _.filter(this.owner.owner.selectedModels, function(
            line
          ) {
            return line.get('deliveredQuantity');
          });
        if (!deliveredLines.length) {
          OB.UTIL.Approval.requestApproval(
            me.owner.owner.model,
            'OBPOS_approval.canBeDelivered',
            function(approved, supervisor, approvalType) {
              if (approved) {
                _.each(me.owner.owner.selectedModels, function(line) {
                  if (line.get('obposCanbedelivered')) {
                    line.set('obposCanbedelivered', false);
                  } else {
                    line.set('obposCanbedelivered', true);
                  }
                });
                me.owner.owner.render();
                me.owner.owner.model
                  .get('order')
                  .getPrepaymentAmount(function() {
                    me.owner.owner.model.get('order').save();
                  });
              }
            }
          );
        } else {
          var linesNames = [OB.I18N.getLabel('OBPOS_NotAllowUndeliverable')];
          _.each(deliveredLines, function(line) {
            linesNames.push(
              OB.I18N.getLabel('OBMOBC_Character')[1] +
                ' ' +
                line.get('product').get('_identifier')
            );
          });
          OB.UTIL.showConfirmation.display(
            OB.I18N.getLabel('OBMOBC_Error'),
            linesNames
          );
        }
      }
    }
  ],
  published: {
    receipt: null
  },
  events: {
    onDeleteLine: '',
    onEditLine: '',
    onReturnLine: '',
    onShowPopup: '',
    onShowMultiSelection: '',
    onReceiptLineSelected: ''
  },
  handlers: {
    onCheckBoxBehaviorForTicketLine: 'checkBoxBehavior',
    onToggledLineSelection: 'toggleLineSelection',
    onSetMultiSelected: 'setMultiSelected',
    onHideReturnLineButton: 'hideReturnLineButton',
    onRearrangedEditButtonBar: 'rearrangeEditButtonBar'
  },
  checkBoxBehavior: function(inSender, inEvent) {
    if (inEvent.status) {
      this.line = null;
      //WARN! When off is done the components which are listening to this event
      //are removed. Because of it, the callback for the selected event are saved
      //and then recovered.
      this.selectedCallbacks = this.receipt.get('lines')._callbacks.selected;
      this.receipt.get('lines').off('selected');
      this.render();
    } else {
      //WARN! recover the callbacks for the selected events
      this.receipt.get('lines')._callbacks.selected = this.selectedCallbacks;

      if (this.receipt.get('lines').length > 0) {
        var line = this.receipt.get('lines').at(0);
        line.trigger('selected', line);
      }
    }
  },
  hideReturnLineButton: function(inSender, inEvent) {
    if (this.$.actionButtonsContainer.$.returnLine) {
      if (
        inEvent.hide ||
        !OB.MobileApp.model.hasPermission(
          this.$.actionButtonsContainer.$.returnLine.permission,
          true
        )
      ) {
        this.$.actionButtonsContainer.$.returnLine.hide();
      } else {
        this.$.actionButtonsContainer.$.returnLine.show();
      }
    }
  },
  rearrangeEditButtonBar: function(line) {
    if (
      this.selectedModels &&
      this.selectedModels.length > 0 &&
      this.selectedModels[0] instanceof OB.Model.OrderLine
    ) {
      var selectedServices = _.filter(this.selectedModels, function(line) {
        return (
          line.get('product').get('productType') === 'S' || line.get('qty') < 0
        );
      });
      this.hideDeliveryButton =
        !OB.MobileApp.model.get('terminal').terminalType.calculateprepayments ||
        this.receipt.get('isFullyDelivered') ||
        selectedServices.length
          ? true
          : false;
      this.hideDeliveryLabel =
        !OB.MobileApp.model.get('terminal').terminalType.calculateprepayments ||
        this.receipt.get('isFullyDelivered') ||
        selectedServices.length === this.selectedModels.length
          ? true
          : false;
      if (this.selectedModels.length > 1) {
        var canBeDeliveredLine = this.selectedModels[0].get(
            'obposCanbedelivered'
          ),
          selectedLinesToDeliver = _.filter(this.selectedModels, function(
            line
          ) {
            return (
              (line.get('obposCanbedelivered') === canBeDeliveredLine &&
                !line.getDeliveredQuantity()) ||
              OB.UTIL.isCrossStoreLine(line)
            );
          });
        this.hideDeliveryButton = this.hideDeliveryButton
          ? true
          : selectedLinesToDeliver.length &&
            selectedLinesToDeliver.length < this.selectedModels.length;
        if (this.hideDeliveryButton) {
          this.$.actionButtonsContainer.$.canDeliver.hide();
        } else {
          this.$.actionButtonsContainer.$.canDeliver.show();
          if (!selectedLinesToDeliver.length) {
            this.$.actionButtonsContainer.$.canDeliver.setContent(
              OB.I18N.getLabel('OBPOS_SetAsDeliverable')
            );
          } else {
            this.$.actionButtonsContainer.$.canDeliver.setContent(
              OB.I18N.getLabel('OBPOS_SetAsUndeliverable')
            );
          }
        }
      } else if (this.selectedModels.length === 1) {
        if (
          this.hideDeliveryButton ||
          this.selectedModels[0].getDeliveredQuantity() ||
          OB.UTIL.isCrossStoreLine(this.selectedModels[0])
        ) {
          this.$.actionButtonsContainer.$.canDeliver.hide();
        } else {
          this.$.actionButtonsContainer.$.canDeliver.show();
          if (this.selectedModels[0].get('obposCanbedelivered')) {
            this.$.actionButtonsContainer.$.canDeliver.setContent(
              OB.I18N.getLabel('OBPOS_SetAsUndeliverable')
            );
          } else {
            this.$.actionButtonsContainer.$.canDeliver.setContent(
              OB.I18N.getLabel('OBPOS_SetAsDeliverable')
            );
          }
        }
      }
      this.render();
    }
    if (line) {
      if (line && !this.isLineInSelection(line)) {
        return;
      }
      if (
        this.selectedModels &&
        this.selectedModels.length === 1 &&
        line.get('product') &&
        line.get('product').get('productType') === 'S' &&
        line.get('product').get('isLinkedToProduct') &&
        this.model.get('order').get('isEditable') &&
        line.get('isEditable')
      ) {
        this.$.actionButtonsContainer.$.addAssociationsButton.show();
        this.$.actionButtonsContainer.$.removeAssociationsButton.show();
      } else {
        this.$.actionButtonsContainer.$.addAssociationsButton.hide();
        this.$.actionButtonsContainer.$.removeAssociationsButton.hide();
      }
      this.$.formElementReturnreason.coreElement.setSelected(0);
      if (this.line) {
        this.line.off('change', this.render);
      }
      this.line = line;
      if (this.line) {
        this.line.on('change', this.render, this);
      }
      if (!this.selectedModels || this.selectedModels.length <= 1) {
        if (this.$.actionButtonsContainer.$.openAttributeButton) {
          if (this.line) {
            if (
              (this.receipt.get('isEditable') ||
                this.receipt.get('isLayaway')) &&
              this.line.get('product').get('hasAttributes') &&
              OB.MobileApp.model.get('permissions')
                .OBPOS_EnableSupportForProductAttributes
            ) {
              this.$.actionButtonsContainer.$.openAttributeButton.show();
            } else {
              this.$.actionButtonsContainer.$.openAttributeButton.hide();
            }
          } else {
            this.$.actionButtonsContainer.$.openAttributeButton.hide();
          }
        }
      } else {
        if (this.$.actionButtonsContainer.$.openAttributeButton) {
          this.$.actionButtonsContainer.$.openAttributeButton.hide();
        }
      }
      if (this.$.actionButtonsContainer.$.removeDiscountButton) {
        var promotions = false,
          hasEditableLines = true;
        if (this.selectedModels) {
          _.each(
            this.selectedModels,
            function(lineModel) {
              if (!hasEditableLines) {
                promotions = false;
                return false;
              }
              hasEditableLines =
                this.model.get('order').get('isEditable') &&
                lineModel.get('isEditable');
              if (
                hasEditableLines &&
                lineModel.get('promotions') &&
                lineModel.get('promotions').length > 0
              ) {
                // lines with just discretionary discounts can be removed.
                var filtered = _.filter(
                  lineModel.get('promotions'),
                  function(prom) {
                    return OB.Discounts.Pos.getManualPromotions().includes(
                      prom.discountType
                    );
                  },
                  this
                );
                if (filtered.length > 0) {
                  promotions = true;
                }
              }
            },
            this
          );
        }
        if (promotions) {
          this.$.actionButtonsContainer.$.removeDiscountButton.show();
        } else {
          this.$.actionButtonsContainer.$.removeDiscountButton.hide();
        }
      }
      if (this.$.actionButtonsContainer.$.showRelatedServices) {
        if (this.selectedModels && this.selectedModels.length > 0) {
          var proposedServices, existRelatedServices;
          existRelatedServices =
            this.selectedModels.filter(function(line) {
              return line.get('hasRelatedServices');
            }).length === this.selectedModels.length;
          proposedServices =
            this.selectedModels.filter(function(line) {
              return (
                !line.get('hasRelatedServices') ||
                line.get('obposServiceProposed')
              );
            }).length === this.selectedModels.length;
          if (existRelatedServices) {
            this.$.actionButtonsContainer.$.showRelatedServices.show();
            if (proposedServices) {
              this.$.actionButtonsContainer.$.showRelatedServices.addRemoveClass(
                'obObposPointOfSaleUiEditLine-actionButtons-showRelatedServices_unreviewed ',
                false
              );
              this.$.actionButtonsContainer.$.showRelatedServices.addRemoveClass(
                'obObposPointOfSaleUiEditLine-actionButtons-showRelatedServices_reviewed',
                true
              );
            } else {
              this.$.actionButtonsContainer.$.showRelatedServices.addRemoveClass(
                'obObposPointOfSaleUiEditLine-actionButtons-showRelatedServices_unreviewed ',
                true
              );
              this.$.actionButtonsContainer.$.showRelatedServices.addRemoveClass(
                'obObposPointOfSaleUiEditLine-actionButtons-showRelatedServices_reviewed',
                false
              );
            }
          } else {
            this.$.actionButtonsContainer.$.showRelatedServices.hide();
          }
        } else if (this.line && this.line.get('hasRelatedServices')) {
          this.$.actionButtonsContainer.$.showRelatedServices.show();
          if (this.line.get('obposServiceProposed')) {
            this.$.actionButtonsContainer.$.showRelatedServices.addRemoveClass(
              'obObposPointOfSaleUiEditLine-actionButtons-showRelatedServices_unreviewed',
              false
            );
            this.$.actionButtonsContainer.$.showRelatedServices.addRemoveClass(
              'obObposPointOfSaleUiEditLine-actionButtons-showRelatedServices_reviewed',
              true
            );
          } else {
            this.$.actionButtonsContainer.$.showRelatedServices.addRemoveClass(
              'obObposPointOfSaleUiEditLine-actionButtons-showRelatedServices_unreviewed',
              true
            );
            this.$.actionButtonsContainer.$.showRelatedServices.addRemoveClass(
              'obObposPointOfSaleUiEditLine-actionButtons-showRelatedServices_reviewed',
              false
            );
          }
        } else {
          this.$.actionButtonsContainer.$.showRelatedServices.hide();
        }
      }
      this.render();
    }
  },
  toggleLineSelection: function(inSender, inEvent) {
    if (inEvent.status && this.line) {
      this.selectedLine = this.line;
      this.line = null;
      this.selectedCallbacks = this.receipt.get('lines')._callbacks.selected;
      this.clickCallbacks = this.receipt.get('lines')._callbacks.click;
      this.receipt.get('lines').off('selected');
      this.receipt.get('lines').off('click');
      this.render();
    } else if (!inEvent.status) {
      //The fix for issue 31509 adds a selected callback after 'off'ing the callbacks but before
      //restoring them. We need to ensure that both callback objects are merged
      this.receipt.get('lines')._callbacks.selected = this.selectedCallbacks;
      this.receipt.get('lines')._callbacks.click = this.clickCallbacks;
      if (this.receipt.get('lines').length > 0) {
        var line = this.selectedLine;
        line.trigger('selected', line);
      }
    }
  },
  setMultiSelected: function(inSender, inEvent) {
    if (
      inEvent.models &&
      inEvent.models.length > 0 &&
      !(inEvent.models[0] instanceof OB.Model.OrderLine)
    ) {
      return;
    }
    this.selectedModels = inEvent.models;
    if (this.$.linePropertiesContainer.$.priceLine) {
      this.$.linePropertiesContainer.$.priceLine.multiSelection =
        inEvent.models.length > 1;
    }
    if (this.$.linePropertiesContainer.$.qtyLine) {
      this.$.linePropertiesContainer.$.qtyLine.multiSelection =
        inEvent.models.length > 1;
    }
    this.$.linePropertiesContainer.$.discountedAmountLine.multiSelection =
      inEvent.models.length > 1;
    this.selectedListener(
      this.selectedModels.length > 0 ? this.selectedModels[0] : undefined
    );

    this.render();
  },
  isLineInSelection: function(line) {
    var model = _.find(this.selectedModels, function(model) {
      return model.id === line.id;
    });
    return model !== undefined;
  },
  executeOnShow: function(args) {
    if (args && args.discounts) {
      this.$.defaultEdit.hide();
      this.$.discountsEdit.show();
      this.doShowMultiSelection({
        show: false
      });
      return;
    }
    this.$.defaultEdit.show();
    this.$.discountsEdit.hide();
  },
  components: [
    {
      kind: 'OB.OBPOSPointOfSale.UI.Discounts',
      showing: false,
      name: 'discountsEdit',
      classes: 'obObposPointOfSaleUiEditLine-discountsEdit'
    },
    {
      name: 'defaultEdit',
      classes: 'obObposPointOfSaleUiEditLine-defaultEdit',
      components: [
        {
          name: 'msgedit',
          classes: 'obObposPointOfSaleUiEditLine-defaultEdit-msgedit',
          showing: false,
          components: [
            {
              classes:
                'obObposPointOfSaleUiEditLine-defaultEdit-msgedit-header',
              components: [
                {
                  name: 'actionButtonsContainer',
                  kind: 'Scroller',
                  thumb: true,
                  tap: function() {
                    this.owner.$.actionButtonsContainer.resetContainer();
                    this.owner.$.showLessActionButtons.setShowing(false);
                  },
                  resetContainer: function() {
                    this.owner.$.actionButtonsContainer.removeClass('expanded');
                    this.owner.$.actionButtonsContainerScrim.removeClass(
                      'expanded'
                    );
                    this.owner.$.actionButtonsContainer
                      .hasNode()
                      .scrollTo(0, 0);
                  },
                  classes:
                    'obObposPointOfSaleUiEditLine-msgedit-actionButtonsContainer'
                },
                {
                  name: 'actionButtonsContainerScrim',
                  classes:
                    'obObposPointOfSaleUiEditLine-msgedit-actionButtonsContainerScrim',
                  tap: function() {
                    this.owner.$.actionButtonsContainer.resetContainer();
                    this.owner.$.showLessActionButtons.setShowing(false);
                  }
                },
                {
                  kind: 'OB.OBPOSPointOfSale.UI.EditLine.ShowMoreActionButtons',
                  name: 'showMoreActionButtons',
                  classes:
                    'obObposPointOfSaleUiEditLine-msgedit-showMoreActionButtons'
                },
                {
                  kind: 'OB.OBPOSPointOfSale.UI.EditLine.ShowLessActionButtons',
                  name: 'showLessActionButtons',
                  showing: false,
                  classes:
                    'obObposPointOfSaleUiEditLine-msgedit-showLessActionButtons'
                }
              ]
            },
            {
              kind: 'OB.OBPOSPointOfSale.UI.EditLine.ReturnReason',
              name: 'formElementReturnreason',
              classes:
                'obObposPointOfSaleUiEditLine-msgedit-formElementReturnreason'
            },
            {
              classes: 'obObposPointOfSaleUiEditLine-msgedit-scroller',
              kind: 'Scroller',
              components: [
                {
                  classes:
                    'obObposPointOfSaleUiEditLine-msgedit-scroller-container1',
                  components: [
                    {
                      classes:
                        'obObposPointOfSaleUiEditLine-msgedit-scroller-container1-linePropertiesContainer',
                      kind: 'Scroller',
                      name: 'linePropertiesContainer',
                      thumb: true
                    },
                    {
                      classes:
                        'obObposPointOfSaleUiEditLine-msgedit-scroller-container1-contextImage',
                      name: 'contextImage',
                      components: [
                        {
                          classes:
                            'obObposPointOfSaleUiEditLine-contextImage-container1',
                          components: [
                            {
                              tag: 'div',
                              classes:
                                'obObposPointOfSaleUiEditLine-contextImage-container1-container1',
                              contentType: 'image/png',
                              components: [
                                {
                                  tag: 'img',
                                  name: 'icon',
                                  classes:
                                    'obObposPointOfSaleUiEditLine-contextImage-container1-container1-icon'
                                }
                              ]
                            },
                            {
                              name: 'editlineimage',
                              kind: 'OB.UI.Thumbnail',
                              classes:
                                'obObposPointOfSaleUiEditLine-contextImage-container1-editlineimage'
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
              ]
            },
            {
              name: 'msgaction',
              classes: 'obObposPointOfSaleUiEditLine-msgedit-msgaction',
              components: [
                {
                  name: 'txtaction',
                  classes: 'obObposPointOfSaleUiEditLine-msgaction-txtaction'
                }
              ]
            }
          ]
        }
      ]
    }
  ],
  selectedListener: function(line) {
    this.rearrangeEditButtonBar(line);
  },
  receiptChanged: function() {
    this.inherited(arguments);
    this.line = null;
    var me = this;

    function resetSelectedModel() {
      if (me.receipt.get('lines').length === 0) {
        me.line = null;
        me.selectedModels = null;
        me.render();
      } else {
        me.rearrangeEditButtonBar();
      }
    }
    this.receipt.on(
      'clear',
      function() {
        resetSelectedModel();
      },
      this
    );
    this.receipt.get('lines').on(
      'remove',
      function() {
        resetSelectedModel();
      },
      this
    );
    this.receipt.get('lines').on(
      'selected',
      function(lineSelected) {
        if (lineSelected) {
          me.selectedListener(lineSelected);
          me.doReceiptLineSelected({
            line: lineSelected
          });
        }
      },
      this
    );
  },

  render: function() {
    var me = this,
      selectedReason;
    this.inherited(arguments);

    if (this.line) {
      this.$.msgaction.hide();
      this.$.msgedit.show();
      if (OB.MobileApp.model.hasPermission('OBPOS_HideProductImages', true)) {
        this.$.contextImage.hide();
      } else {
        this.$.contextImage.show();
      }
      if (OB.MobileApp.model.get('permissions')['OBPOS_retail.productImages']) {
        if (this.selectedModels && this.selectedModels.length > 1) {
          this.$.icon.setSrc('../org.openbravo.mobile.core/assets/img/box.png');
        } else {
          this.$.icon.setSrc(
            OB.UTIL.getImageURL(this.line.get('product').get('id'))
          );
          this.$.icon.setAttribute(
            'onerror',
            'if (this.src != "../org.openbravo.mobile.core/assets/img/box.png") this.src = "../org.openbravo.mobile.core/assets/img/box.png"; '
          );
        }
        this.$.editlineimage.hide();
      } else {
        if (this.selectedModels && this.selectedModels.length > 1) {
          this.$.editlineimage.setImg(null);
        } else {
          this.$.editlineimage.setImg(this.line.get('product').get('img'));
        }
        this.$.icon.parent.hide();
      }
      if (
        this.line.get('qty') < OB.DEC.Zero &&
        !this.receipt.get('iscancelled') &&
        !this.receipt.get('isPaid')
      ) {
        if (!_.isUndefined(this.line.get('returnReason'))) {
          selectedReason = _.filter(
            this.$.formElementReturnreason.coreElement.children,
            function(reason) {
              return reason.getValue() === me.line.get('returnReason');
            }
          )[0];
          if (selectedReason) {
            this.$.formElementReturnreason.coreElement.setSelected(
              selectedReason.getNodeProperty('index')
            );
          }
        }
        this.$.formElementReturnreason.show();
        this.$.linePropertiesContainer.removeClass(
          'obObposPointOfSaleUiEditLine-linePropertiesContainer_withoutReturnReasons'
        );
        this.$.contextImage.addClass(
          'obObposPointOfSaleUiEditLine-msgedit-container3-contextImage_withReturnReasons'
        );
      } else {
        this.$.formElementReturnreason.hide();
        this.$.linePropertiesContainer.addClass(
          'obObposPointOfSaleUiEditLine-linePropertiesContainer_withoutReturnReasons'
        );
        this.$.contextImage.removeClass(
          'obObposPointOfSaleUiEditLine-msgedit-container3-contextImage_withReturnReasons'
        );
      }
    } else {
      this.$.txtaction.setContent(OB.I18N.getLabel('OBPOS_NoLineSelected'));
      this.$.msgedit.hide();
      this.$.msgaction.show();
      if (OB.MobileApp.model.get('permissions')['OBPOS_retail.productImages']) {
        this.$.icon.addClass(
          'obObposPointOfSaleUiEditLine-icon_noBackgroundImage'
        );
      } else {
        this.$.editlineimage.setImg(null);
      }
    }
    if (this.selectedModels && this.selectedModels.length > 1) {
      var i,
        quantity = this.selectedModels[0].get('qty'),
        price = this.selectedModels[0].get('price'),
        priceTotal = OB.DEC.mul(price, quantity),
        hasPrice = true,
        hasDiscount = true,
        disc = OB.DEC.mul(
          OB.DEC.sub(
            this.selectedModels[0].get('product').get('standardPrice'),
            this.selectedModels[0].get('price')
          ),
          this.selectedModels[0].get('qty')
        ),
        discount = this.selectedModels[0].getTotalAmountOfPromotions(),
        warehousename = this.selectedModels[0].get('warehouse')
          ? this.selectedModels[0].get('warehouse').warehousename
          : '',
        editlinetotal = this.selectedModels[0].get('priceIncludesTax')
          ? this.selectedModels[0].getTotalLine()
          : this.selectedModels[0].get('discountedNet'),
        orderLine = this.selectedModels[0].clone();
      for (i = 1; i < this.selectedModels.length; i++) {
        if (price && price !== this.selectedModels[i].get('price')) {
          hasPrice = false;
        }
        var lineDisc = OB.DEC.mul(
          OB.DEC.sub(
            this.selectedModels[i].get('product').get('standardPrice'),
            this.selectedModels[i].get('price')
          ),
          this.selectedModels[i].get('qty')
        );
        if (lineDisc !== disc) {
          hasDiscount = false;
        }
        if (discount !== this.selectedModels[i].getTotalAmountOfPromotions()) {
          hasDiscount = false;
        }
        var warehouse = this.selectedModels[i].get('warehouse')
          ? this.selectedModels[i].get('warehouse').warehousename
          : '';
        if (warehousename !== warehouse) {
          warehousename = OB.I18N.getLabel('OBPOS_lblMultiSelectValues');
        }
        if (quantity !== this.selectedModels[i].get('qty')) {
          quantity = 0;
        }
        priceTotal +=
          this.selectedModels[i].get('price') *
          this.selectedModels[i].get('qty');
        editlinetotal = OB.DEC.add(
          editlinetotal,
          this.selectedModels[i].get('priceIncludesTax')
            ? this.selectedModels[i].getTotalLine()
            : this.selectedModels[i].get('discountedNet')
        );
      }
      orderLine
        .get('product')
        .set(
          '_identifier',
          OB.I18N.getLabel('OBPOS_lblMultiSelectDescription', [
            this.selectedModels.length
          ])
        );
      orderLine.set('qty', quantity);
      orderLine.set('_gross', priceTotal);
      orderLine.set('hasPrice', hasPrice);
      if (!hasPrice) {
        price = orderLine.get('product').get('standardPrice');
      }
      orderLine.set('price', price);
      orderLine.set('hasDiscount', hasDiscount);
      if (hasDiscount) {
        orderLine.set('promotions', [
          {
            amt: discount
          }
        ]);
      }
      if (warehousename !== '') {
        orderLine.set('warehouse', {
          warehousename: warehousename
        });
      } else {
        orderLine.unset('warehouse');
      }
      this.$.linePropertiesContainer.$.descLine.render(orderLine);
      this.$.linePropertiesContainer.$.returnReasonLine.render(orderLine);
      if (this.$.linePropertiesContainer.$.qtyLine) {
        this.$.linePropertiesContainer.$.qtyLine.render(orderLine);
      }
      if (this.$.linePropertiesContainer.$.priceLine) {
        this.$.linePropertiesContainer.$.priceLine.render(orderLine);
      }
      this.$.linePropertiesContainer.$.discountedAmountLine.render(orderLine);
      this.$.linePropertiesContainer.$.warehouseLine.render(orderLine);
      this.$.linePropertiesContainer.$.deliverableLine.render(orderLine);
      orderLine.get('product').set('standardPrice', priceTotal);
      orderLine.set('price', priceTotal);
      if (!orderLine.get('priceIncludesTax')) {
        orderLine.set('net', priceTotal);
      }
      orderLine.set('editlinetotal', editlinetotal);
      this.$.linePropertiesContainer.$.grossLine.render(orderLine);
    } else {
      enyo.forEach(
        this.$.linePropertiesContainer.getComponents(),
        function(compToRender) {
          if (compToRender.kindName.indexOf('enyo.') !== 0) {
            compToRender.render(this.line);
          }
        },
        this
      );
    }
  },
  initComponents: function() {
    var sortedPropertiesByPosition;
    this.inherited(arguments);
    sortedPropertiesByPosition = _.sortBy(this.propertiesToShow, function(
      comp
    ) {
      return comp.position ? comp.position : comp.position === 0 ? 0 : 999;
    });
    enyo.forEach(
      sortedPropertiesByPosition,
      function(compToCreate) {
        this.$.linePropertiesContainer.createComponent(compToCreate);
      },
      this
    );
    enyo.forEach(
      this.actionButtons,
      function(compToCreate) {
        if (
          !compToCreate.permission ||
          OB.MobileApp.model.hasPermission(compToCreate.permission, false)
        ) {
          compToCreate.allowTapEventPropagation = true;
          this.$.actionButtonsContainer.createComponent(compToCreate);
        }
      },
      this
    );
  },
  init: function(model) {
    this.model = model;
    this.reasons = new Backbone.Collection();
    this.$.formElementReturnreason.coreElement.setCollection(this.reasons);

    this.model.get('order').on(
      'change:isPaid change:isLayaway change:isQuotation',
      function(newValue) {
        this.rearrangeEditButtonBar();
      },
      this
    );

    try {
      const dataReasons = OB.MobileApp.view.terminal.get('returnreasons');
      if (this.destroyed) {
        return;
      }
      if (dataReasons && dataReasons.length > 0) {
        this.reasons.reset(dataReasons);
      } else {
        this.reasons.reset();
      }
    } catch (err) {
      OB.UTIL.showError(err);
    }
  }
});

enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.OBPOSPointOfSale.UI.EditLine.ShowMoreActionButtons',
  i18nContent: 'OBMOBC_LblShowMore',
  classes: 'obObposPointOfSaleUiEditLineShowMoreActionButtons',
  tap: function() {
    this.owner.$.actionButtonsContainer.addClass('expanded');
    this.owner.$.actionButtonsContainerScrim.addClass('expanded');
    this.owner.$.showLessActionButtons.setShowing(true);
  }
});

enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.OBPOSPointOfSale.UI.EditLine.ShowLessActionButtons',
  i18nContent: 'OBMOBC_LblShowLess',
  classes: 'obObposPointOfSaleUiEditLineShowLessActionButtons',
  tap: function() {
    this.owner.$.actionButtonsContainer.resetContainer();
    this.owner.$.showLessActionButtons.setShowing(false);
  }
});

enyo.kind({
  kind: 'OB.UI.FormElement',
  name: 'OB.OBPOSPointOfSale.UI.EditLine.ReturnReason',
  classes: 'obUiFormElement_dataEntry obObposPointOfSaleUiEditLineReturnReasom',
  coreElement: {
    kind: 'OB.UI.List',
    i18nLabel: 'OBPOS_ReturnReason',
    classes: 'obObposPointOfSaleUiEditLine-msgedit-returnreason',
    change: function(inSender, inEvent) {
      var reason = this.children[this.getSelected()],
        returnReason = reason.getValue(),
        returnReasonName = reason.getContent();
      _.each(this.formElement.owner.selectedModels, function(line) {
        if (returnReason === '') {
          line.unset('returnReason');
          line.unset('returnReasonName');
        } else {
          line.set('returnReason', returnReason);
          line.set('returnReasonName', returnReasonName);
        }
      });
    },
    actionAfterClear: function() {
      _.each(this.formElement.owner.selectedModels, function(line) {
        line.unset('returnReason');
        line.unset('returnReasonName');
      });
    },
    renderHeader: enyo.kind({
      kind: 'OB.UI.FormElement.Select.Option',
      classes:
        'obObposPointOfSaleUiEditLine-returnreason-renderHeader-enyoOption',
      initComponents: function() {
        this.inherited(arguments);
        this.setValue('');
        this.setContent('');
      }
    }),
    renderLine: enyo.kind({
      kind: 'OB.UI.FormElement.Select.Option',
      classes:
        'obObposPointOfSaleUiEditLine-returnreason-renderLine-enyoOption',
      initComponents: function() {
        this.inherited(arguments);
        this.setValue(this.model.get('id'));
        this.setContent(this.model.get('name'));
      }
    }),
    renderEmpty: 'enyo.Control'
  }
});

enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.OBPOSPointOfSale.UI.EditLine.OpenAttributeButton',
  content: '',
  classes: 'obObposPointOfSaleUiEditLineOpenAttributeButton',
  permission: 'OBPOS_EnableSupportForProductAttributes',
  tap: function() {
    var me = this;
    OB.MobileApp.view.waterfall('onShowPopup', {
      popup: 'modalProductAttribute',
      attributeValue: this.owner.owner.line.get('attributeValue'),
      args: {
        callback: function(attributeValue, cancelled) {
          var line = me.owner.owner.line;
          if (!cancelled) {
            if (
              me.owner.owner.receipt.checkSerialAttribute(
                line.get('product'),
                attributeValue
              )
            ) {
              if (_.isEmpty(attributeValue)) {
                // the attributes for layaways accepts empty values, but for manage later easy to be null instead ""
                attributeValue = null;
              }

              line.set('attributeValue', attributeValue);
              // attributeValue is used to save the new attribute
              // but when loading the order from backend it contains the attribute in json format
              // and attSetInstanceDesc contains the transformed attribute into string
              // so when we set again the attributeValue, we have to unset the attSetInstanceDesc,
              // if not the new value in attributeValue will be ignored
              me.owner.owner.line.unset('attSetInstanceDesc');
            } else {
              OB.UTIL.showConfirmation.display(
                OB.I18N.getLabel('OBMOBC_Error'),
                OB.I18N.getLabel('OBPOS_ProductDefinedAsSerialNo')
              );
            }
          }
        },
        options: {
          attSetInstanceDesc: me.owner.owner.line.get('attSetInstanceDesc'),
          attributeValue: me.owner.owner.line.get('attributeValue')
        }
      }
    });
  },
  initComponents: function() {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel('OBPOS_openAttributes'));
  }
});
