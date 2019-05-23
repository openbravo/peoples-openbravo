/*
 ************************************************************************************
 * Copyright (C) 2018-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, _, Backbone, OBRDM */

// FIXME merge this code properly into posterminal module
(function () {
  enyo.kind({
    kind: 'OB.UI.SmallButton',
    name: 'OB.OBPOSPointOfSale.UI.EditLine.DeliveryModesButton',
    i18nContent: 'OBRDM_DeliveryMode',
    classes: 'btnlink-orange',
    handlers: {
      onSetMultiSelected: 'setMultiSelected',
      onRearrangedEditButtonBar: 'hideShowButton'
    },
    hideShowButton: function () {
      this.setShowing(this.model.get('order').get('orderType') !== 1);
    },
    tap: function () {
      if (this.owner.owner.receipt.get('hasbeenpaid') === 'Y') {
        this.owner.owner.doShowPopup({
          popup: 'modalNotEditableOrder'
        });
        return;
      }
      this.owner.owner.doShowPopup({
        popup: 'OBRDM_ReceiptMultilines',
        args: {
          selectedLines: this.owner.owner.selectedModels
        }
      });
    },
    init: function (model) {
      this.model = model;
      this.model.get('order').get('lines').on('add change:qty', function (line) {
        if (!this.model.get('order').get('cancelLayaway') && line.get('qty') < 0 && line.get('obrdmDeliveryMode') !== 'PickAndCarry') {
          line.set('obrdmDeliveryMode', 'PickAndCarry');
          line.unset('obrdmDeliveryDate');
          line.unset('obrdmDeliveryTime');
        }
      }, this);
      this.model.get('order').get('lines').on('add', function (line) {
        if (!line.has('hasDeliveryServices') && line.get('obrdmDeliveryMode') === 'HomeDelivery') {
          //Trigger Delivery Services Search
          if (OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)) {
            var process = new OB.DS.Process('org.openbravo.retail.deliverymodes.process.HasDeliveryServices');
            var params = {},
                date = new Date();
            process.exec({
              lines: [{
                lineId: line.id,
                product: line.get('product').get('id'),
                productCategory: line.get('product').get('productCategory')
              }],
              terminalTime: date,
              terminalTimeOffset: date.getTimezoneOffset(),
              remoteFilters: [{
                columns: [],
                operator: 'filter',
                value: 'OBRDM_DeliveryServiceFilter',
                params: [true]
              }]
            }, function (data, message) {
              if (data && data.exception) {
                //ERROR or no connection
                OB.UTIL.showError(OB.I18N.getLabel('OBPOS_ErrorGettingRelatedServices'));
              } else if (data) {
                line.set('hasDeliveryServices', data[0].hasDeliveryServices);
              } else {
                OB.UTIL.showError(OB.I18N.getLabel('OBPOS_ErrorGettingRelatedServices'));
              }
            });
          } else {
            var criteria = {};

            criteria._whereClause = '';
            criteria.params = [];

            criteria._whereClause = " as product where product.productType = 'S' and product.obrdmIsdeliveryservice = 'true' and (product.isLinkedToProduct = 'true' and ";

            //including/excluding products
            criteria._whereClause += "((product.includeProducts = 'Y' and not exists (select 1 from m_product_service sp where product.m_product_id = sp.m_product_id and sp.m_related_product_id = ? ))";
            criteria._whereClause += "or (product.includeProducts = 'N' and exists (select 1 from m_product_service sp where product.m_product_id = sp.m_product_id and sp.m_related_product_id = ? ))";
            criteria._whereClause += "or product.includeProducts is null) ";

            //including/excluding product categories
            criteria._whereClause += "and ((product.includeProductCategories = 'Y' and not exists (select 1 from m_product_category_service spc where product.m_product_id = spc.m_product_id and spc.m_product_category_id =  ? )) ";
            criteria._whereClause += "or (product.includeProductCategories = 'N' and exists (select 1 from m_product_category_service spc where product.m_product_id = spc.m_product_id and spc.m_product_category_id  = ? )) ";
            criteria._whereClause += "or product.includeProductCategories is null)) ";

            criteria.params.push(line.get('product').get('id'));
            criteria.params.push(line.get('product').get('id'));
            criteria.params.push(line.get('product').get('productCategory'));
            criteria.params.push(line.get('product').get('productCategory'));

            OB.Dal.findUsingCache('deliveryServiceCache', OB.Model.Product, criteria, function (data) {
              if (data && data.length > 0) {
                line.set('hasDeliveryServices', true);
              } else {
                line.set('hasDeliveryServices', false);
              }
            }, function (trx, error) {
              OB.UTIL.showError(OB.I18N.getLabel('OBPOS_ErrorGettingRelatedServices'));
              line.set('hasDeliveryServices', false);
            }, {
              modelsAffectedByCache: ['Product']
            });
          }
        }
      });
    },
    initComponents: function () {
      this.inherited(arguments);
    },
    setMultiSelected: function (inSender, inEvent) {
      var me = this,
          hideButton = false,
          isPaid = inSender.model.get('order').get('isPaid'),
          isEditable = inSender.model.get('order').get('isEditable');
      _.each(inEvent.models, function (line) {
        if (line.get('product') && line.get('product') instanceof OB.Model.Product && (line.get('product').get('productType') === 'S' || line.get('qty') < 0 || isPaid || (line.get('deliveredQuantity') === line.get('qty')) || !isEditable)) {
          hideButton = true;
        }
      });
      if (hideButton) {
        me.setShowing(false);
      } else {
        me.setShowing(true);
      }
    }
  });
  //Register the button...
  OB.OBPOSPointOfSale.UI.EditLine.prototype.actionButtons.push({
    kind: 'OB.OBPOSPointOfSale.UI.EditLine.DeliveryModesButton',
    name: 'deliveryModesButton'
  });

  enyo.kind({
    kind: 'OB.UI.SmallButton',
    name: 'OB.OBPOSPointOfSale.UI.EditLine.DeliveryServicesButton',
    classes: 'btnlink-orange',
    style: 'width: 45px; background-repeat: no-repeat; background-position: center; color: rgba(0, 0, 0, 0)',
    content: '-',
    handlers: {
      onSetMultiSelected: 'setMultiSelected',
      onRearrangedEditButtonBar: 'setMultiSelected'
    },
    tap: function (inSender, inEvent) {
      var product = this.owner.owner.line.get('product');
      if (product) {
        OB.UI.SearchProductCharacteristic.prototype.filtersCustomClear();
        OB.UI.SearchProductCharacteristic.prototype.filtersCustomAdd(new OB.UI.SearchServicesFilter({
          text: this.owner.owner.selectedModels.filter(function (line) {
            return line.get('hasDeliveryServices');
          }).map(function (line) {
            return line.get('product').get('_identifier');
          }).join(', '),
          productList: this.owner.owner.selectedModels.filter(function (line) {
            return line.get('hasDeliveryServices');
          }).map(function (line) {
            return line.get('product').get('id');
          }),
          orderlineList: this.owner.owner.selectedModels.filter(function (line) {
            return line.get('hasDeliveryServices');
          }),
          extraParams: {
            isDeliveryService: true
          }
        }));
        var me = this;
        setTimeout(function () {
          me.bubble('onTabChange', {
            tabPanel: 'searchCharacteristic'
          });
          me.bubble('onSelectFilter', {
            params: {
              skipProductCharacteristic: true
            }
          });
          me.owner.owner.selectedModels.filter(function (line) {
            return line.get('hasDeliveryServices');
          }).forEach(function (l) {
            l.set("deliveryServiceProposed", true);
          });
        }, 1);
      }
    },
    init: function (model) {
      this.model = model;
    },
    setMultiSelected: function (inSender, inEvent) {
      var me = this;
      if (this.owner.owner.selectedModels && this.owner.owner.selectedModels.length > 0) {
        var proposedServices, existRelatedServices;
        existRelatedServices = this.owner.owner.selectedModels.filter(function (line) {
          return line.get('hasDeliveryServices');
        }).length === this.owner.owner.selectedModels.length;
        proposedServices = this.owner.owner.selectedModels.filter(function (line) {
          return !line.get('hasDeliveryServices') || line.get('deliveryServiceProposed');
        }).length === this.owner.owner.selectedModels.length;
        if (existRelatedServices) {
          this.show();
          if (proposedServices) {
            this.addRemoveClass('iconDeliveryServices_unreviewed', false);
            this.addRemoveClass('iconDeliveryServices_reviewed', true);
          } else {
            this.addRemoveClass('iconDeliveryServices_unreviewed', true);
            this.addRemoveClass('iconDeliveryServices_reviewed', false);
          }
        } else {
          this.hide();
        }
      } else if (this.owner.owner.line && this.owner.owner.line.get('hasDeliveryServices')) {
        this.show();
        if (this.owner.owner.line.get('obposServiceProposed')) {
          this.addRemoveClass('iconDeliveryServices_unreviewed', false);
          this.addRemoveClass('iconDeliveryServices_reviewed', true);
        } else {
          this.addRemoveClass('iconDeliveryServices_unreviewed', true);
          this.addRemoveClass('iconDeliveryServices_reviewed', false);
        }
      } else {
        this.hide();
      }
    }
  });

  //Register the button...
  var servicesButtonIndex = OB.OBPOSPointOfSale.UI.EditLine.prototype.actionButtons.findIndex(function (button) {
    return button.name === 'showRelatedServices';
  });
  OB.OBPOSPointOfSale.UI.EditLine.prototype.actionButtons.splice(servicesButtonIndex, 0, {
    kind: 'OB.OBPOSPointOfSale.UI.EditLine.DeliveryServicesButton',
    name: 'deliveryServicesButton'
  });

}());

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OBRDM.UI.MultiReceiptPropertiesDialogApply',
  isDefaultAction: true,
  events: {
    onApplyChanges: ''
  },
  tap: function () {
    var me = this;
    if (this.doApplyChanges()) {
      this.doHideThisPopup();
    }
  },
  initComponents: function () {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel('OBMOBC_LblApply'));
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OBRDM.UI.MultiReceiptPropertiesDialogCancel',
  tap: function () {
    this.doHideThisPopup();
  },
  initComponents: function () {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel('OBMOBC_LblCancel'));
  }
});

enyo.kind({
  name: 'OBRDM.UI.ModalReceiptMultiLinesProperties',
  kind: 'OB.UI.ModalAction',
  handlers: {
    onApplyChanges: 'applyChanges'
  },
  executeOnShow: function () {
    if (this.currentLine) {
      var diff = this.propertycomponents;
      var att;
      for (att in diff) {
        if (diff.hasOwnProperty(att)) {
          this.loadValue(att, diff[att]);
        }
      }
    }
    this.autoDismiss = true;
    if (this && this.args && this.args.autoDismiss === false) {
      this.autoDismiss = false;
    }
  },
  executeOnHide: function () {
    if (this.args && this.args.requiredFiedls && this.args.requiredFieldNotPresentFunction) {
      var smthgPending = _.find(this.args.requiredFiedls, function (fieldName) {
        return OB.UTIL.isNullOrUndefined(this.currentLine.get(fieldName));
      }, this);
      if (smthgPending) {
        this.args.requiredFieldNotPresentFunction(this.currentLine, smthgPending);
      }
    }
  },
  i18nHeader: 'OBRDM_ReceiptLineDeliveryModeDialogTitle',
  bodyContent: {
    kind: 'Scroller',
    maxHeight: '225px',
    style: 'background-color: #ffffff;',
    thumb: true,
    horizontal: 'hidden',
    components: [{
      name: 'attributes'
    }]
  },
  bodyButtons: {
    components: [{
      kind: 'OBRDM.UI.MultiReceiptPropertiesDialogApply',
      name: 'receiptMultiLinePropertiesApplyBtn'
    }, {
      kind: 'OBRDM.UI.MultiReceiptPropertiesDialogCancel',
      name: 'receiptMultiLinePropertiesCancelBtn'
    }]
  },
  loadValue: function (mProperty, component) {
    this.waterfall('onLoadValue', {
      model: this.currentLine,
      modelProperty: mProperty
    });

    // Make it visible or not...
    if (component.showProperty) {
      component.showProperty(this.currentLine, function (value) {
        component.owner.owner.setShowing(value);
      });
    }
  },
  applyChanges: function (inSender, inEvent) {
    var i, diff, att, result = true,
        me = this;
    diff = this.propertycomponents;
    //Delivery date validation
    var dateSelected = diff.obrdmDeliveryDate.getValue();
    var today = new Date();
    if (diff.obrdmDeliveryDate.owner.owner.getShowing()) {
      if (dateSelected !== null) {
        if (new Date(dateSelected.toDateString()) < new Date(today.toDateString())) {
          OB.UTIL.showError(OB.I18N.getLabel('em_obrdm_delivery_date_chk'));
          return;
        }
      } else {
        OB.UTIL.showError(OB.I18N.getLabel('OBRDM_Delivery_Date_Inval'));
        return;
      }
    }
    //Delivery time validation 
    if (diff.obrdmDeliveryTime.owner.owner.getShowing()) {
      var timeSelected = diff.obrdmDeliveryTime.getValue();
      if (timeSelected !== null) {
        dateSelected.setHours(timeSelected.getHours());
        dateSelected.setMinutes(timeSelected.getMinutes());
        dateSelected.setSeconds(today.getSeconds());
        dateSelected.setMilliseconds(today.getMilliseconds());

        if (new Date(dateSelected) < new Date(today)) {
          OB.UTIL.showError(OB.I18N.getLabel('em_obrdm_delivery_datetime_chk'));
          return;
        }
      } else {
        OB.UTIL.showError(OB.I18N.getLabel('OBRDM_Delivery_Time_Inval'));
        return;
      }
    }

    for (att in diff) {
      if (diff.hasOwnProperty(att)) {
        if (diff[att].owner.owner.getShowing()) {
          //Single or multiline selection
          if (this.args.selectedLines) {
            for (i = 0; i < this.args.selectedLines.length; i++) {
              result = result && diff[att].applyValue(this.args.selectedLines[i]);
            }
          } else {
            result = result && diff[att].applyValue(this.currentLine);
          }
        }
      }
    }

    this.model.get('order').save();
    this.model.get('orderList').saveCurrent();

    var carrierLines = this.args.selectedLines.filter(function (l) {
      return l.get('obrdmDeliveryMode') === 'HomeDelivery';
    }).map(function (l) {
      return {
        lineId: l.id,
        product: l.get('product').get('id'),
        productCategory: l.get('product').get('productCategory')
      };
    });

    function countDeliveryServices(data) {
      var countLines = 0;
      data.forEach(function (res) {
        var orderLine = me.args.selectedLines.find(function (l) {
          return l.id === res.lineId;
        });
        orderLine.set('hasDeliveryServices', res.hasDeliveryServices);
        if (res.hasDeliveryServices) {
          countLines++;
        }
      });
      if (countLines === carrierLines.length) {
        //trigger search
        var previousStatus = {
          tab: OB.MobileApp.model.get('lastPaneShown'),
          filterText: '',
          category: '',
          filteringBy: '',
          filter: '',
          brandFilter: '',
          customFilters: '',
          genericParent: ''
        };

        //Clear existing filters in Product Search
        me.owner.$.multiColumn.$.rightPanel.$.toolbarpane.$.searchCharacteristic.$.searchCharacteristicTabContent.doClearAction();

        OB.UI.SearchProductCharacteristic.prototype.filtersCustomClear();
        OB.UI.SearchProductCharacteristic.prototype.filterCustomClearConditions();
        OB.UI.SearchProductCharacteristic.prototype.filtersCustomAdd(new OB.UI.SearchServicesFilter({
          text: me.args.selectedLines.map(function (line) {
            return line.get('product').get('_identifier');
          }).join(', '),
          productList: me.args.selectedLines.map(function (line) {
            return line.get('product').get('id');
          }),
          orderlineList: me.args.selectedLines,
          extraParams: {
            isDeliveryService: true
          }
        }));

        me.bubble('onSelectFilter', {
          params: {
            skipProductCharacteristic: true,
            searchCallback: function (data) {
              if (data && data.length === 1) {
                var attrs = {};
                OB.UI.SearchProductCharacteristic.prototype.customFilters.forEach(function (filter) {
                  var filterAttr = filter.lineAttributes();
                  if (filterAttr) {
                    _.each(_.keys(filterAttr), function (key) {
                      attrs[key] = filterAttr[key];
                    });
                  }
                });
                me.bubble('onAddProduct', {
                  product: data.at(0),
                  attrs: attrs
                });
              } else if (data && data.length > 1) {
                me.bubble('onToggleLineSelection', {
                  status: true
                });
                me.bubble('onTabChange', {
                  tabPanel: 'searchCharacteristic'
                });
                me.bubble('onManageServiceProposal', {
                  proposalType: 'mandatory',
                  previousStatus: previousStatus
                });
                OB.MobileApp.view.waterfallDown('onShowProductList', {
                  productList: data
                });
              }
            }
          }
        });
      }
    }

    if (carrierLines.length === this.args.selectedLines.length) {
      //Trigger Delivery Services Search
      if (OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)) {
        var process = new OB.DS.Process('org.openbravo.retail.deliverymodes.process.HasDeliveryServices');
        var params = {},
            date = new Date();
        process.exec({
          lines: carrierLines,
          terminalTime: date,
          terminalTimeOffset: date.getTimezoneOffset(),
          remoteFilters: [{
            columns: [],
            operator: 'filter',
            value: 'OBRDM_DeliveryServiceFilter',
            params: [true]
          }]
        }, function (data, message) {
          if (data && data.exception) {
            //ERROR or no connection
            OB.UTIL.showError(OB.I18N.getLabel('OBPOS_ErrorGettingRelatedServices'));
          } else if (data) {
            countDeliveryServices(data);
          } else {
            OB.UTIL.showError(OB.I18N.getLabel('OBPOS_ErrorGettingRelatedServices'));
          }
        });
      } else {
        var hasDeliveryServices = function (line, callback) {
            var criteria = {};

            criteria._whereClause = '';
            criteria.params = [];

            criteria._whereClause = " as product where product.productType = 'S' and product.obrdmIsdeliveryservice = 'true' and (product.isLinkedToProduct = 'true' and ";

            //including/excluding products
            criteria._whereClause += "((product.includeProducts = 'Y' and not exists (select 1 from m_product_service sp where product.m_product_id = sp.m_product_id and sp.m_related_product_id = ? ))";
            criteria._whereClause += "or (product.includeProducts = 'N' and exists (select 1 from m_product_service sp where product.m_product_id = sp.m_product_id and sp.m_related_product_id = ? ))";
            criteria._whereClause += "or product.includeProducts is null) ";

            //including/excluding product categories
            criteria._whereClause += "and ((product.includeProductCategories = 'Y' and not exists (select 1 from m_product_category_service spc where product.m_product_id = spc.m_product_id and spc.m_product_category_id =  ? )) ";
            criteria._whereClause += "or (product.includeProductCategories = 'N' and exists (select 1 from m_product_category_service spc where product.m_product_id = spc.m_product_id and spc.m_product_category_id  = ? )) ";
            criteria._whereClause += "or product.includeProductCategories is null)) ";

            criteria.params.push(line.get('product').get('id'));
            criteria.params.push(line.get('product').get('id'));
            criteria.params.push(line.get('product').get('productCategory'));
            criteria.params.push(line.get('product').get('productCategory'));

            OB.Dal.findUsingCache('deliveryServiceCache', OB.Model.Product, criteria, function (data) {
              if (data && data.length > 0) {
                callback(true);
              } else {
                callback(false);
              }
            }, function (trx, error) {
              OB.UTIL.showError(OB.I18N.getLabel('OBPOS_ErrorGettingRelatedServices'));
              callback(false);
            }, {
              modelsAffectedByCache: ['Product']
            });
            };

        var data = [];
        var finalCallback = _.after(carrierLines.length, function () {
          countDeliveryServices(data);
        });

        me.args.selectedLines.forEach(function (carrierLine) {
          hasDeliveryServices(carrierLine, function (res) {
            data.push({
              lineId: carrierLine.get('id'),
              hasDeliveryServices: res
            });
            finalCallback();
          });
        });
      }
    }
    return result;
  },
  validationMessage: function (args) {
    this.owner.doShowPopup({
      popup: 'modalValidateAction',
      args: args
    });
  },
  initComponents: function () {
    this.inherited(arguments);
    this.attributeContainer = this.$.bodyContent.$.attributes;
    this.setHeader(OB.I18N.getLabel(this.i18nHeader));

    this.propertycomponents = {};

    enyo.forEach(this.newAttributes, function (natt) {
      var editline = this.$.bodyContent.$.attributes.createComponent({
        kind: 'OB.UI.PropertyEditLine',
        name: 'line_' + natt.name,
        newAttribute: natt
      });
      this.propertycomponents[natt.modelProperty] = editline.propertycomponent;
      this.propertycomponents[natt.modelProperty].propertiesDialog = this;
    }, this);
  },
  init: function (model) {
    this.model = model;
    this.model.get('order').get('lines').on('selected', function (lineSelected) {
      var diff, att;
      this.currentLine = lineSelected;
      if (lineSelected) {
        diff = this.propertycomponents;
        for (att in diff) {
          if (diff.hasOwnProperty(att)) {
            this.loadValue(att, diff[att]);
          }
        }
      }
    }, this);
  }
});

enyo.kind({
  name: 'OBRDM.UI.ModalReceiptMultiLinesImpl',
  kind: 'OBRDM.UI.ModalReceiptMultiLinesProperties',
  newAttributes: [{
    kind: 'OB.UI.renderComboProperty',
    name: 'ReceiptLineDeliveryMode',
    modelProperty: 'obrdmDeliveryMode',
    i18nLabel: 'OBRDM_DeliveryMode',
    // Model Collection to use. This definition has been created when registering the 
    // ServeOption model 
    collection: new Backbone.Collection(),
    retrievedPropertyForValue: 'id',
    retrievedPropertyForText: 'name',
    // This function is called to get the Modes 
    fetchDataFunction: function (args) {
      OBRDM.UTIL.fillComboCollection(this, args);
    },
    // This function is called when the user accepts on the properties dialog,  
    // and applies the value selected to the row. 
    applyValue: function (row) {
      var value = this.$.renderCombo.getValue(),
          deliveryMode = _.find(OB.MobileApp.model.get('deliveryModes'), function (dm) {
          return dm.id === value;
        });
      row.set(this.modelProperty, value);
      row.set('nameDelivery', deliveryMode.name);

      if (value !== 'PickupInStoreDate' && value !== 'HomeDelivery') {
        row.unset("obrdmDeliveryDate");
      }

      if (value !== 'HomeDelivery') {
        row.unset("obrdmDeliveryTime");
      }

      return true;
    },
    init: function () {
      this.$.renderCombo.change = function (inSender, inEvent) {
        var cond = inSender.getValue();
        this.owner.owner.owner.owner.owner.owner.waterfallDown("onShowhide", {
          showDate: cond === 'PickupInStoreDate' || cond === 'HomeDelivery',
          showTime: cond === 'HomeDelivery'
        });
      };
    }
  }, {
    kind: 'OB.UI.DatePickerSimple',
    name: 'ReceiptLineDeliveryDate',
    style: 'display: flex; justify-content: center;',
    modelProperty: 'obrdmDeliveryDate',
    i18nLabel: 'OBRDM_DeliveryDate',
    handlers: {
      onShowhide: 'onShowhide'
    },
    onShowhide: function (inSender, inEvent) {
      if (inEvent.showDate) {
        this.owner.owner.show();
        this.setValue(this.model.get('obrdmDeliveryDate'));
      } else {
        this.owner.owner.hide();
      }
    },
    // This function is called to determine whether the property is displayed or not
    showProperty: function (row, callback) {
      var oldCaptureTarget = enyo.dispatcher.captureTarget;
      this.model = row;
      this.setLocale(OB.MobileApp.model.get('terminal').language_string);
      var cond = row.get('obrdmDeliveryMode');
      var show = cond === 'PickupInStoreDate' || cond === 'HomeDelivery';
      if (show) {
        this.setValue(row.get('obrdmDeliveryDate') instanceof Date ? row.get('obrdmDeliveryDate') : new Date(row.get('obrdmDeliveryDate')));
      }
      enyo.dispatcher.captureTarget = oldCaptureTarget;
      callback(show);
    },
    // This function is called when the user accepts on the properties dialog,  
    // and applies the value selected to the row. 
    applyValue: function (row) {
      this.getValue().setHours(0);
      this.getValue().setMinutes(0);
      this.getValue().setSeconds(0);
      row.set(this.modelProperty, this.getValue());
      return true;
    }
  }, {
    kind: 'OB.UI.TimePickerSimple',
    name: 'ReceiptLineDeliveryTime',
    style: 'display: flex; justify-content: center;',
    modelProperty: 'obrdmDeliveryTime',
    i18nLabel: 'OBRDM_DeliveryTime',
    handlers: {
      onShowhide: 'onShowhide'
    },
    onShowhide: function (inSender, inEvent) {
      if (inEvent.showTime) {
        this.owner.owner.show();
        this.setValue(this.model.get('obrdmDeliveryDate'));
      } else {
        this.owner.owner.hide();
      }
    },
    // This function is called to determine whether the property is displayed or not
    showProperty: function (row, callback) {
      var oldCaptureTarget = enyo.dispatcher.captureTarget;
      this.model = row;
      this.setLocale(OB.MobileApp.model.get('terminal').language_string);
      var cond = row.get('obrdmDeliveryMode');
      var show = cond === 'HomeDelivery';
      if (show) {
        this.setValue(row.get('obrdmDeliveryTime') instanceof Date ? row.get('obrdmDeliveryTime') : new Date(row.get('obrdmDeliveryTime')));
      }
      enyo.dispatcher.captureTarget = oldCaptureTarget;
      callback(show);
    },
    // This function is called when the user accepts on the properties dialog,  
    // and applies the value selected to the row. 
    applyValue: function (row) {
      var deliveryTime = new Date(row.get('obrdmDeliveryDate'));
      deliveryTime.setHours(this.getValue().getHours());
      deliveryTime.setMinutes(this.getValue().getMinutes());
      deliveryTime.setSeconds(0);
      row.set(this.modelProperty, deliveryTime);
      return true;
    }
  }]
});

OB.UI.WindowView.registerPopup('OB.OBPOSPointOfSale.UI.PointOfSale', {
  kind: 'OBRDM.UI.ModalReceiptMultiLinesImpl',
  name: 'OBRDM_ReceiptMultilines'
});