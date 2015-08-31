/*
 ************************************************************************************
 * Copyright (C) 2012-2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, _ */
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.LineProperty',
  components: [{
    classes: 'row-fluid',
    style: 'clear: both;',
    components: [{
      classes: 'span4',
      name: 'propertyLabel'
    }, {
      classes: 'span8',
      components: [{
        tag: 'span',
        name: 'propertyValue'
      }]
    }]
  }],
  render: function (model) {
    if (model) {
      this.$.propertyValue.setContent(model.get(this.propertyToPrint));
    } else {
      this.$.propertyValue.setContent('');
    }
  },
  initComponents: function () {
    this.inherited(arguments);
    this.$.propertyLabel.setContent(OB.I18N.getLabel(this.I18NLabel));
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.LinePropertyDiv',
  components: [{
    classes: 'row-fluid',
    style: 'clear: both;',
    components: [{
      classes: 'span4',
      name: 'propertyLabel'
    }, {
      classes: 'span8',
      components: [{
        tag: 'div',
        name: 'propertyValue'
      }]
    }]
  }],
  render: function (model) {
    if (model) {
      this.$.propertyValue.setContent(model.get(this.propertyToPrint));
    } else {
      this.$.propertyValue.setContent('');
    }
  },
  initComponents: function () {
    this.inherited(arguments);
    this.$.propertyLabel.setContent(OB.I18N.getLabel(this.I18NLabel));
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.EditLine',
  propertiesToShow: [{
    kind: 'OB.OBPOSPointOfSale.UI.LineProperty',
    position: 10,
    name: 'descLine',
    I18NLabel: 'OBPOS_LineDescription',
    render: function (line) {
      if (line) {
        this.$.propertyValue.setContent(line.get('product').get('_identifier'));
      } else {
        this.$.propertyValue.setContent('');
      }
    }
  }, {
    kind: 'OB.OBPOSPointOfSale.UI.LineProperty',
    position: 20,
    name: 'qtyLine',
    I18NLabel: 'OBPOS_LineQuantity',
    multiSelection: false,
    render: function (line) {
      if (line) {
        if (line.get('qty') === 0) {
          this.$.propertyValue.setContent(OB.I18N.getLabel('OBPOS_lblMultiSelectQuantity'));
        } else {
          this.$.propertyValue.setContent(line.printQty() + (this.multiSelection ? " " + OB.I18N.getLabel('OBPOS_lblMultiSelectPerLines') : ""));
        }
      } else {
        this.$.propertyValue.setContent('');
      }
    }
  }, {
    kind: 'OB.OBPOSPointOfSale.UI.LineProperty',
    position: 30,
    name: 'priceLine',
    I18NLabel: 'OBPOS_LinePrice',
    render: function (line) {
      if (line) {
        if (this.multiSelection) {
          this.$.propertyValue.setContent(line.get('hasPrice') ? line.printPrice() + " " + OB.I18N.getLabel('OBPOS_lblMultiSelectPerLines') : OB.I18N.getLabel('OBPOS_lblMultiSelectPrice'));
        } else {
          this.$.propertyValue.setContent(line.printPrice());
        }
      } else {
        this.$.propertyValue.setContent('');
      }
    }
  }, {
    kind: 'OB.OBPOSPointOfSale.UI.LineProperty',
    position: 40,
    name: 'discountedAmountLine',
    I18NLabel: 'OBPOS_LineDiscount',
    multiSelection: false,
    render: function (line) {
      if (line) {
        if (this.multiSelection) {
          var lineDisc = line.printDiscount();
          this.$.propertyValue.setContent(line.get('hasDiscount') ? lineDisc + (lineDisc !== '' ? ' ' + OB.I18N.getLabel('OBPOS_lblMultiSelectPerLines') : '') : OB.I18N.getLabel('OBPOS_lblMultiSelectDiscount'));
        } else {
          this.$.propertyValue.setContent(line.printDiscount());
        }
      } else {
        this.$.propertyValue.setContent('');
      }
    }
  }, {
    kind: 'OB.OBPOSPointOfSale.UI.LineProperty',
    position: 50,
    name: 'grossLine',
    I18NLabel: 'OBPOS_LineTotal',
    render: function (line) {
      if (line) {
        if (line.get('priceIncludesTax')) {
          this.$.propertyValue.setContent(line.printTotalLine());
        } else {
          this.$.propertyValue.setContent(line.printNet());
        }
      } else {
        this.$.propertyValue.setContent('');
      }
    }
  }, {
    kind: 'OB.OBPOSPointOfSale.UI.LineProperty',
    position: 60,
    name: 'warehouseLine',
    I18NLabel: 'OBPOS_LineWarehouse',
    render: function (line) {
      if (line && line.get('warehouse')) {
        this.$.propertyValue.setContent(line.get('warehouse').warehousename);
      } else {
        this.$.propertyValue.setContent(OB.MobileApp.model.get('warehouses')[0].warehousename);
      }
    }
  }],
  actionButtons: [{
    kind: 'OB.UI.SmallButton',
    name: 'deleteLine',
    i18nContent: 'OBPOS_ButtonDelete',
    classes: 'btnlink-orange',
    tap: function () {
      var me = this;
      if (me.owner.owner && me.owner.ownerreceipt && me.owner.owner.receipt.get('isQuotation') && me.owner.owner.receipt.get('hasbeenpaid') === 'Y') {
        me.owner.owner.doShowPopup({
          popup: 'modalNotEditableOrder'
        });
        return;
      }
      OB.UTIL.Approval.requestApproval(
      me.model, 'OBPOS_approval.deleteLine', function (approved, supervisor, approvalType) {
        if (approved) {
          if (me.owner.owner.selectedModels && me.owner.owner.selectedModels.length > 1) {
            var order = me.model.get('order');
            order.deleteLines(me.owner.owner.selectedModels);
            order.trigger('scan');
          } else {
            me.owner.owner.doDeleteLine({
              line: me.owner.owner.line
            });
          }
        }
      });
    },
    init: function (model) {
      this.model = model;
      this.model.get('order').on('change:isPaid change:isLayaway', function (newValue) {
        if (newValue) {
          if (newValue.get('isPaid') === true || newValue.get('isLayaway') === true) {
            this.setShowing(false);
            return;
          }
        }
        this.setShowing(true);
      }, this);
    }
  }, {
    kind: 'OB.UI.SmallButton',
    i18nContent: 'OBPOS_LblDescription',
    name: 'descriptionButton',
    classes: 'btnlink-orange',
    tap: function () {
      if (this.owner.owner.receipt.get('isQuotation') && this.owner.owner.receipt.get('hasbeenpaid') === 'Y') {
        this.owner.owner.doShowPopup({
          popup: 'modalNotEditableOrder'
        });
        return;
      }
      this.owner.owner.doEditLine({
        line: this.owner.owner.line
      });
    },
    init: function (model) {
      this.model = model;
      this.model.get('order').on('change:isPaid change:isLayaway', function (newValue) {
        if (newValue) {
          if (newValue.get('isPaid') === true || newValue.get('isLayaway') === true) {
            this.setShowing(false);
            return;
          }
        }
        this.setShowing(true);
      }, this);
    }
  }, {
    kind: 'OB.UI.SmallButton',
    name: 'returnLine',
    i18nContent: 'OBPOS_LblReturnLine',
    permission: 'OBPOS_ReturnLine',
    classes: 'btnlink-orange',
    showing: false,
    tap: function () {
      var me = this,
          approvalNeeded = false,
          i, j, k, h, line, relatedLine, lineFromSelected, servicesToApprove = '',
          servicesList = [],
          order = this.owner.owner.receipt;
      for (i = 0; i < this.owner.owner.selectedModels.length; i++) {
        line = this.owner.owner.selectedModels[i];
        if (line.get('product').get('productType') === 'S' && !line.isReturnable()) {
          OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_UnreturnableProduct'), OB.I18N.getLabel('OBPOS_UnreturnableProductMessage', [line.get('product').get('_identifier')]));
          return;
        } else {
          // A service with its related product selected doesn't need to be returned, because later it will be modified to returned status depending in the product status
          // In any other case it would require two approvals
          if (line.get('product').get('productType') === 'S') {
            if (line.get('relatedLines')) {
              for (j = 0; j < line.get('relatedLines').length; j++) {
                relatedLine = line.get('relatedLines')[j];
                for (k = 0; k < this.owner.owner.selectedModels.length; k++) {
                  lineFromSelected = this.owner.owner.selectedModels[k];
                  if (lineFromSelected.id === relatedLine.orderlineId) {
                    line.set('notReturnThisLine', true);
                    servicesToApprove += '<br>· ' + line.get('product').get('_identifier');
                    servicesList.push(line.get('product'));
                    break;
                  }
                }
                if (k === this.owner.owner.selectedModels.length) {
                  OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_NotProductSelectedToReturn', [line.get('product').get('_identifier')]));
                  return;
                }
              }
            } else {
              servicesToApprove += '<br>· ' + line.get('product').get('_identifier');
              servicesList.push(line.get('product'));
            }
            if (!approvalNeeded && line.get('net') > 0) {
              approvalNeeded = true;
            }
          }
        }
      }
      for (i = 0; i < order.get('lines').length; i++) { // Check if there is any not returnable related product to a selected line
        line = OB.MobileApp.model.receipt.get('lines').models[i];
        if (line.get('product').get('productType') === 'S' && !line.isReturnable()) {
          if (line.get('relatedLines')) {
            for (j = 0; j < line.get('relatedLines').length; j++) {
              relatedLine = line.get('relatedLines')[j];
              for (k = 0; k < this.owner.owner.selectedModels.length; k++) {
                lineFromSelected = this.owner.owner.selectedModels[k];
                if (lineFromSelected.id === relatedLine.orderlineId) {
                  OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_UnreturnableRelatedService'), OB.I18N.getLabel('OBPOS_UnreturnableRelatedServiceMessage', [line.get('product').get('_identifier'), relatedLine.productName]));
                  return;
                }
              }
            }
          }
        } else if (line.get('product').get('productType') === 'S' && line.isReturnable()) { // Ask for approval for non selected services, related to selected products
          if (line.get('relatedLines')) {
            for (j = 0; j < line.get('relatedLines').length; j++) {
              relatedLine = line.get('relatedLines')[j];
              for (k = 0; k < this.owner.owner.selectedModels.length; k++) {
                lineFromSelected = this.owner.owner.selectedModels[k];
                if (lineFromSelected.id === relatedLine.orderlineId) {
                  for (h = 0; h < servicesList.length; h++) {
                    if (servicesList[h].id === line.get('product').id) {
                      break;
                    }
                  }
                  if (h === servicesList.length) {
                    servicesToApprove += '<br>· ' + line.get('product').get('_identifier');
                    servicesList.push(line.get('product'));
                    if (!approvalNeeded && line.get('net') > 0) {
                      approvalNeeded = true;
                    }
                  }
                }
              }
            }
          }
        }
      }

      function returnLines() {
        order.set('undo', null);
        order.set('multipleUndo', true);
        _.each(me.owner.owner.selectedModels, function (line) {
          if (!line.get('notReturnThisLine')) {
            me.owner.owner.doReturnLine({
              line: line
            });
          } else {
            line.unset('notReturnThisLine');
          }
        });
        order.set('multipleUndo', null);
      }
      if (approvalNeeded) {
        OB.UTIL.Approval.requestApproval(
        me.model, [{
          approval: 'OBPOS_approval.returnService',
          message: 'OBPOS_approval.returnService',
          params: [servicesToApprove]
        }], function (approved, supervisor, approvalType) {
          if (approved) {
            order.set('notApprove', true);
            returnLines();
            order.unset('notApprove');
          } else {
            _.each(me.owner.owner.selectedModels, function (line) {
              if (line.get('notReturnThisLine')) {
                line.unset('notReturnThisLine');
              }
            });
          }
        });
      } else {
        returnLines();
      }
    },
    init: function (model) {
      this.model = model;
      if (OB.MobileApp.model.get('permissions')[this.permission]) {
        this.setShowing(true);
      }
      this.model.get('order').on('change:isPaid change:isLayaway change:isQuotation', function (newValue) {
        if (newValue) {
          if (newValue.get('isPaid') === true || newValue.get('isLayaway') === true || newValue.get('isQuotation') === true) {
            this.setShowing(false);
            return;
          }
        }
        if (OB.MobileApp.model.get('permissions')[this.permission]) {
          this.setShowing(true);
        }
      }, this);
    }
  }, {
    kind: 'OB.UI.SmallButton',
    name: 'showRelatedServices',
    classes: 'btnlink-orange',
    style: 'width: 45px; background-repeat: no-repeat; background-position: center; color: rgba(0, 0, 0, 0)',
    content: '-',
    tap: function (inSender, inEvent) {
      var product = this.owner.owner.line.get('product');
      if (product) {
        OB.UI.SearchProductCharacteristic.prototype.filtersCustomClear();
        OB.UI.SearchProductCharacteristic.prototype.filtersCustomAdd(new OB.UI.SearchServicesFilter({
          text: this.owner.owner.selectedModels.filter(function (line) {
            return line.get('hasRelatedServices');
          }).map(function (line) {
            return line.get('product').get('_identifier');
          }).join(', '),
          productList: this.owner.owner.selectedModels.filter(function (line) {
            return line.get('hasRelatedServices');
          }).map(function (line) {
            return line.get('product').get('id');
          }),
          orderlineList: this.owner.owner.selectedModels.filter(function (line) {
            return line.get('hasRelatedServices');
          })
        }));
        var me = this;
        setTimeout(function () {
          me.bubble('onTabChange', {
            tabPanel: 'searchCharacteristic'
          });
          me.bubble('onSelectFilter', {});
          me.owner.owner.selectedModels.filter(function (line) {
            return line.get('hasRelatedServices');
          }).forEach(function (l) {
            l.set("obposServiceProposed", true);
          });
        }, 1);
        this.addRemoveClass('iconServices_unreviewed', false);
        this.addRemoveClass('iconServices_reviewed', true);
      }
    }
  }, {
    kind: 'OB.UI.SmallButton',
    name: 'removeDiscountButton',
    i18nContent: 'OBPOS_LblRemoveDiscount',
    showing: false,
    classes: 'btnlink-orange',
    tap: function () {
      _.each(this.owner.owner.selectedModels, function (lineModel) {
        if (lineModel.get('promotions') && lineModel.get('promotions').length > 0) {
          var filtered = _.filter(lineModel.get('promotions'), function (prom) {
            //discrectionary discounts ids
            return prom.discountType === '20E4EC27397344309A2185097392D964' || prom.discountType === 'D1D193305A6443B09B299259493B272A' || prom.discountType === '8338556C0FBF45249512DB343FEFD280' || prom.discountType === '7B49D8CC4E084A75B7CB4D85A6A3A578';
          }, this);
          if (filtered.length === lineModel.get('promotions').length) {
            //lines with just discrectionary discounts can be removed.
            lineModel.unset('promotions');
          }
        }
      });
      OB.Model.Discounts.applyPromotions(this.model.get('order'));
      this.model.get('order').calculateGross();
      this.hide();
    },
    init: function (model) {
      this.model = model;
    }
  }, {
    kind: 'OB.OBPOSPointOfSale.UI.EditLine.OpenStockButton',
    name: 'checkStockButton',
    showing: false
  }],
  published: {
    receipt: null
  },
  events: {
    onDeleteLine: '',
    onEditLine: '',
    onReturnLine: '',
    onShowPopup: '',
    onShowMultiSelection: ''
  },
  handlers: {
    onCheckBoxBehaviorForTicketLine: 'checkBoxBehavior',
    onToggledLineSelection: 'toggleLineSelection',
    onSetMultiSelected: 'setMultiSelected'
  },
  checkBoxBehavior: function (inSender, inEvent) {
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
  toggleLineSelection: function (inSender, inEvent) {
    if (inEvent.status) {
      this.line = null;
      this.selectedCallbacks = this.receipt.get('lines')._callbacks.selected;
      this.clickCallbacks = this.receipt.get('lines')._callbacks.click;
      this.receipt.get('lines').off('selected');
      this.receipt.get('lines').off('click');
      this.render();
    } else {
      this.receipt.get('lines')._callbacks.selected = this.selectedCallbacks;
      this.receipt.get('lines')._callbacks.click = this.clickCallbacks;
      if (this.receipt.get('lines').length > 0) {
        var line = this.receipt.get('lines').at(0);
        line.trigger('selected', line);
      }
    }
  },
  setMultiSelected: function (inSender, inEvent) {
    if (inEvent.models && inEvent.models.length > 0 && !(inEvent.models[0] instanceof OB.Model.OrderLine)) {
      return;
    }
    this.selectedModels = inEvent.models;
    this.$.linePropertiesContainer.$.priceLine.multiSelection = inEvent.models.length > 1;
    this.$.linePropertiesContainer.$.qtyLine.multiSelection = inEvent.models.length > 1;
    this.$.linePropertiesContainer.$.discountedAmountLine.multiSelection = inEvent.models.length > 1;
    this.selectedListener(this.selectedModels.length > 0 ? this.selectedModels[0] : undefined);
    this.render();
  },
  isLineInSelection: function (line) {
    var model = _.find(this.selectedModels, function (model) {
      return model.id === line.id;
    });
    return model !== undefined;
  },
  executeOnShow: function (args) {
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
  components: [{
    kind: 'OB.OBPOSPointOfSale.UI.Discounts',
    showing: false,
    name: 'discountsEdit'
  }, {
    name: 'defaultEdit',
    style: 'background-color: #ffffff; color: black; height: 200px; margin: 5px; padding: 5px',
    components: [{
      name: 'msgedit',
      classes: 'row-fluid',
      showing: false,
      components: [{
        name: 'actionButtonsContainer',
        kind: 'Scroller',
        maxHeight: '50px',
        thumb: true,
        horizontal: 'hidden',
        classes: 'span12'
      }, {
        kind: 'OB.UI.List',
        name: 'returnreason',
        classes: 'combo',
        style: 'width: 100%; margin-bottom: 0px; height: 30px ',
        events: {
          onSetReason: ''
        },
        handlers: {
          onchange: 'changeReason'
        },
        changeReason: function (inSender, inEvent) {
          if (this.children[this.getSelected()].getValue() === '') {
            this.owner.line.unset('returnReason');
          } else {
            this.owner.line.set('returnReason', this.children[this.getSelected()].getValue());
          }
        },
        renderHeader: enyo.kind({
          kind: 'enyo.Option',
          initComponents: function () {
            this.inherited(arguments);
            this.setValue('');
            this.setContent(OB.I18N.getLabel('OBPOS_ReturnReasons'));
          }
        }),
        renderLine: enyo.kind({
          kind: 'enyo.Option',
          initComponents: function () {
            this.inherited(arguments);
            this.setValue(this.model.get('id'));
            this.setContent(this.model.get('_identifier'));
          }
        }),
        renderEmpty: 'enyo.Control'
      }, {
        classes: 'span12',
        components: [{
          classes: 'span7',
          kind: 'Scroller',
          name: 'linePropertiesContainer',
          maxHeight: '134px',
          thumb: true,
          horizontal: 'hidden',
          style: 'padding: 8px 0px 4px 25px; line-height: 120%;'
        }, {
          classes: 'span4',
          sytle: 'text-align: right',
          components: [{
            style: 'padding: 2px 10px 10px 10px;',
            components: [{
              tag: 'div',
              classes: 'image-wrap image-editline',
              contentType: 'image/png',
              style: 'width: 128px; height: 128px',
              components: [{
                tag: 'img',
                name: 'icon',
                style: 'margin: auto; height: 100%; width: 100%; background-size: contain; background-repeat:no-repeat; background-position:center;'
              }]
            }, {
              name: 'editlineimage',
              kind: 'OB.UI.Thumbnail',
              classes: 'image-wrap image-editline',
              width: '105px',
              height: '105px'
            }]
          }]
        }]
      }, {
        name: 'msgaction',
        style: 'padding: 10px;',
        components: [{
          name: 'txtaction',
          style: 'float:left;'
        }]
      }]
    }]
  }],
  selectedListener: function (line) {
    if (line && !this.isLineInSelection(line)) {
      return;
    }
    this.$.returnreason.setSelected(0);
    if (this.line) {
      this.line.off('change', this.render);
    }
    this.line = line;
    if (this.line) {
      this.line.on('change', this.render, this);
    }
    if (!this.selectedModels || this.selectedModels.length <= 1) {
      if (this.model.get('order').get('isEditable')) {
        this.$.actionButtonsContainer.$.descriptionButton.show();
      }
      if (this.line) {
        if ((this.line.get('product').get('showstock') || this.line.get('product').get('_showstock')) && !this.line.get('product').get('ispack') && OB.MobileApp.model.get('connectedToERP')) {
          this.$.actionButtonsContainer.$.checkStockButton.show();
        } else {
          this.$.actionButtonsContainer.$.checkStockButton.hide();
        }
      } else {
        this.$.actionButtonsContainer.$.checkStockButton.hide();
      }
    } else {
      this.$.actionButtonsContainer.$.checkStockButton.hide();
      this.$.actionButtonsContainer.$.descriptionButton.hide();
    }
    var promotions = false;
    if (this.selectedModels) {
      _.each(this.selectedModels, function (lineModel) {
        if (lineModel.get('promotions') && lineModel.get('promotions').length > 0) {
          var filtered;
          filtered = _.filter(lineModel.get('promotions'), function (prom) {
            //discrectionary discounts ids
            return prom.discountType === '20E4EC27397344309A2185097392D964' || prom.discountType === 'D1D193305A6443B09B299259493B272A' || prom.discountType === '8338556C0FBF45249512DB343FEFD280' || prom.discountType === '7B49D8CC4E084A75B7CB4D85A6A3A578';
          }, this);
          if (filtered.length === lineModel.get('promotions').length) {
            //lines with just discrectionary discounts can be removed.
            promotions = true;
          }
        }
      });
    }
    if (promotions) {
      this.$.actionButtonsContainer.$.removeDiscountButton.show();
    } else {
      this.$.actionButtonsContainer.$.removeDiscountButton.hide();
    }
    if ((!_.isUndefined(line) && !_.isUndefined(line.get('originalOrderLineId'))) || this.model.get('order').get('orderType') === 1) {
      this.$.actionButtonsContainer.$.returnLine.hide();
    } else if (OB.MobileApp.model.get('permissions')[this.$.actionButtonsContainer.$.returnLine.permission] && !(this.model.get('order').get('isPaid') === true || this.model.get('order').get('isLayaway') === true || this.model.get('order').get('isQuotation') === true)) {
      this.$.actionButtonsContainer.$.returnLine.show();
    }
    if (this.selectedModels && this.selectedModels.length > 0) {
      var proposedServices, existRelatedServices;
      existRelatedServices = this.selectedModels.filter(function (line) {
        return line.get('hasRelatedServices');
      }).length > 0;
      proposedServices = this.selectedModels.filter(function (line) {
        return !line.get('hasRelatedServices') || line.get('obposServiceProposed');
      }).length === this.selectedModels.length;
      if (existRelatedServices) {
        this.$.actionButtonsContainer.$.showRelatedServices.show();
        if (proposedServices) {
          this.$.actionButtonsContainer.$.showRelatedServices.addRemoveClass('iconServices_unreviewed', false);
          this.$.actionButtonsContainer.$.showRelatedServices.addRemoveClass('iconServices_reviewed', true);
        } else {
          this.$.actionButtonsContainer.$.showRelatedServices.addRemoveClass('iconServices_unreviewed', true);
          this.$.actionButtonsContainer.$.showRelatedServices.addRemoveClass('iconServices_reviewed', false);
        }
      } else {
        this.$.actionButtonsContainer.$.showRelatedServices.hide();
      }
    } else if (this.line && this.line.get('hasRelatedServices')) {
      this.$.actionButtonsContainer.$.showRelatedServices.show();
      if (this.line.get('obposServiceProposed')) {
        this.$.actionButtonsContainer.$.showRelatedServices.addRemoveClass('iconServices_unreviewed', false);
        this.$.actionButtonsContainer.$.showRelatedServices.addRemoveClass('iconServices_reviewed', true);
      } else {
        this.$.actionButtonsContainer.$.showRelatedServices.addRemoveClass('iconServices_unreviewed', true);
        this.$.actionButtonsContainer.$.showRelatedServices.addRemoveClass('iconServices_reviewed', false);
      }
    } else {
      this.$.actionButtonsContainer.$.showRelatedServices.hide();
    }
    this.render();
  },
  receiptChanged: function () {
    this.inherited(arguments);

    this.line = null;

    this.receipt.get('lines').on('selected', this.selectedListener, this);
  },

  render: function () {
    var me = this,
        selectedReason;
    this.inherited(arguments);

    if (this.line) {
      this.$.msgaction.hide();
      this.$.msgedit.show();
      if (OB.MobileApp.model.get('permissions')["OBPOS_retail.productImages"]) {
        if (this.selectedModels && this.selectedModels.length > 1) {
          this.$.icon.applyStyle('background-image', 'url(' + "../org.openbravo.mobile.core/assets/img/box.png" + ')');
        } else {
          this.$.icon.applyStyle('background-image', 'url(' + OB.UTIL.getImageURL(this.line.get('product').get('id')) + '), url(' + "../org.openbravo.mobile.core/assets/img/box.png" + ')');
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
      if (this.line.get('qty') < OB.DEC.Zero) {
        if (!_.isUndefined(this.line.get('returnReason'))) {
          selectedReason = _.filter(this.$.returnreason.children, function (reason) {
            return reason.getValue() === me.line.get('returnReason');
          })[0];
          this.$.returnreason.setSelected(selectedReason.getNodeProperty('index'));
        }
        this.$.returnreason.show();
        this.$.linePropertiesContainer.setMaxHeight("110px");
      } else {
        this.$.returnreason.hide();
        this.$.linePropertiesContainer.setMaxHeight("134px");
      }
    } else {
      this.$.txtaction.setContent(OB.I18N.getLabel('OBPOS_NoLineSelected'));
      this.$.msgedit.hide();
      this.$.msgaction.show();
      if (OB.MobileApp.model.get('permissions')["OBPOS_retail.productImages"]) {
        this.$.icon.applyStyle('background-image', '');
      } else {
        if (OB.MobileApp.model.get('permissions')["OBPOS_retail.productImages"]) {
          this.$.icon.applyStyle('background-image', '');
        } else {
          this.$.editlineimage.setImg(null);
        }
      }
    }
    if (this.selectedModels && this.selectedModels.length > 1) {
      var i, quantity = this.selectedModels[0].get('qty'),
          price = this.selectedModels[0].get('price'),
          priceTotal = OB.DEC.mul(price, quantity),
          hasPrice = true,
          hasDiscount = true,
          disc = OB.DEC.mul(OB.DEC.sub(this.selectedModels[0].get('product').get('standardPrice'), this.selectedModels[0].get('price')), this.selectedModels[0].get('qty')),
          discount = this.selectedModels[0].getTotalAmountOfPromotions(),
          warehousename = this.selectedModels[0].get('warehouse') ? this.selectedModels[0].get('warehouse').warehousename : '',
          orderLine = this.selectedModels[0].clone();
      for (i = 1; i < this.selectedModels.length; i++) {
        if (price && price !== this.selectedModels[i].get('price')) {
          hasPrice = false;
        }
        var lineDisc = OB.DEC.mul(OB.DEC.sub(this.selectedModels[i].get('product').get('standardPrice'), this.selectedModels[i].get('price')), this.selectedModels[i].get('qty'));
        if (lineDisc !== disc) {
          hasDiscount = false;
        }
        if (discount !== this.selectedModels[i].getTotalAmountOfPromotions()) {
          hasDiscount = false;
        }
        var warehouse = this.selectedModels[i].get('warehouse') ? this.selectedModels[i].get('warehouse').warehousename : '';
        if (warehousename !== warehouse) {
          warehousename = OB.I18N.getLabel('OBPOS_lblMultiSelectValues');
        }
        if (quantity !== this.selectedModels[i].get('qty')) {
          quantity = 0;
        }
        priceTotal += this.selectedModels[i].get('price') * this.selectedModels[i].get('qty');
      }
      orderLine.get('product').set('_identifier', OB.I18N.getLabel('OBPOS_lblMultiSelectDescription', [this.selectedModels.length]));
      orderLine.set('qty', quantity);
      orderLine.set('_gross', priceTotal);
      orderLine.set('hasPrice', hasPrice);
      if (!hasPrice) {
        price = orderLine.get('product').get('standardPrice');
      }
      orderLine.set('price', price);
      orderLine.set('hasDiscount', hasDiscount);
      if (hasDiscount) {
        orderLine.set('promotions', [{
          amt: discount
        }]);
      }
      if (warehousename !== '') {
        orderLine.set('warehouse', {
          warehousename: warehousename
        });
      } else {
        orderLine.unset('warehouse');
      }
      this.$.linePropertiesContainer.$.descLine.render(orderLine);
      this.$.linePropertiesContainer.$.qtyLine.render(orderLine);
      this.$.linePropertiesContainer.$.priceLine.render(orderLine);
      this.$.linePropertiesContainer.$.discountedAmountLine.render(orderLine);
      this.$.linePropertiesContainer.$.warehouseLine.render(orderLine);
      orderLine.get('product').set('standardPrice', priceTotal);
      orderLine.set('price', priceTotal);
      if (!orderLine.get('priceIncludesTax')) {
        orderLine.set('net', priceTotal);
      }
      this.$.linePropertiesContainer.$.grossLine.render(orderLine);
    } else {
      enyo.forEach(this.$.linePropertiesContainer.getComponents(), function (compToRender) {
        if (compToRender.kindName.indexOf("enyo.") !== 0) {
          compToRender.render(this.line);
        }
      }, this);
    }
  },
  initComponents: function () {
    var sortedPropertiesByPosition;
    this.inherited(arguments);
    sortedPropertiesByPosition = _.sortBy(this.propertiesToShow, function (comp) {
      return (comp.position ? comp.position : (comp.position === 0 ? 0 : 999));
    });
    enyo.forEach(sortedPropertiesByPosition, function (compToCreate) {
      this.$.linePropertiesContainer.createComponent(compToCreate);
    }, this);

    enyo.forEach(this.actionButtons, function (compToCreate) {
      this.$.actionButtonsContainer.createComponent(compToCreate);
    }, this);
  },
  init: function (model) {
    this.model = model;
    this.reasons = new OB.Collection.ReturnReasonList();
    this.$.returnreason.setCollection(this.reasons);

    function errorCallback(tx, error) {
      OB.UTIL.showError("OBDAL error: " + error);
    }

    function successCallbackReasons(dataReasons, me) {
      if (me.destroyed) {
        return;
      }
      if (dataReasons && dataReasons.length > 0) {
        me.reasons.reset(dataReasons.models);
      } else {
        me.reasons.reset();
      }
    }
    OB.Dal.find(OB.Model.ReturnReason, null, successCallbackReasons, errorCallback, this);
  }
});

enyo.kind({
  kind: 'OB.UI.SmallButton',
  name: 'OB.OBPOSPointOfSale.UI.EditLine.OpenStockButton',
  events: {
    onShowLeftSubWindow: ''
  },
  content: '',
  classes: 'btnlink-orange',
  tap: function () {
    var line = this.owner.owner.line;
    var product = this.owner.owner.line.get('product');
    var params = {};
    //show always or just when the product has been set to show stock screen?
    if ((product.get('showstock') || product.get('_showstock')) && !product.get('ispack') && OB.MobileApp.model.get('connectedToERP')) {
      params.leftSubWindow = OB.OBPOSPointOfSale.UICustomization.stockLeftSubWindow;
      params.product = product;
      params.line = line;
      params.warehouse = this.owner.owner.line.get('warehouse');
      this.doShowLeftSubWindow(params);
    }
  },
  initComponents: function () {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel('OBPOS_checkStock'));
  }
});