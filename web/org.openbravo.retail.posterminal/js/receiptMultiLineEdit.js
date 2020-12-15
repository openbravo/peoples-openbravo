/*
 ************************************************************************************
 * Copyright (C) 2018-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, _, Backbone, OBRDM */

(function() {
  enyo.kind({
    name: 'OB.OBPOSPointOfSale.UI.EditLine.DeliveryModesButton',
    kind: 'OB.UI.Button',
    i18nContent: 'OBRDM_DeliveryMode',
    classes:
      'obObposPointOfSaleUiEditLine-propertiesToShow-general obObposPointOfSaleUiEditLineDeliveryModesButton obUiActionButton',
    detailsView: null,
    handlers: {
      onSetMultiSelected: 'setMultiSelected',
      onRearrangedEditButtonBar: 'hideShowButton'
    },
    hideShowButton: function() {
      this.setShowing(
        this.model.get('order').get('orderType') !== 1 &&
          OB.MobileApp.model.hasPermission('OBRDM_EnableDeliveryModes', true)
      );
    },
    tap: function() {
      if (
        OB.UTIL.isNullOrUndefined(this.detailsView) &&
        this.owner.owner.receipt.get('hasbeenpaid') === 'Y'
      ) {
        this.owner.owner.doShowPopup({
          popup: 'modalNotEditableOrder'
        });
        return;
      }
      if (OB.UTIL.isNullOrUndefined(this.detailsView)) {
        this.owner.owner.doShowPopup({
          popup: 'OBRDM_ReceiptMultilines',
          args: {
            selectedLines: this.owner.owner.selectedModels
          }
        });
      } else {
        this.detailsView.doShowPopup({
          popup: 'OBRDM_ReceiptMultilines',
          args: {
            product: this.detailsView.leftSubWindow.product,
            organization: this.detailsView.leftSubWindow.organization
          }
        });
      }
    },

    init: async function(model) {
      this.model = model;
      this.model
        .get('order')
        .get('lines')
        .on(
          'add change:qty',
          function(line) {
            if (
              !this.model.get('order').get('cancelLayaway') &&
              line.get('qty') < 0 &&
              line.get('obrdmDeliveryMode') !== 'PickAndCarry'
            ) {
              line.set('obrdmDeliveryMode', 'PickAndCarry');
              line.unset('obrdmDeliveryDate');
              line.unset('obrdmDeliveryTime');
            }
          },
          this
        );
      this.model
        .get('order')
        .get('lines')
        .on('add', async function(line) {
          if (
            !line.has('hasDeliveryServices') &&
            line.get('obrdmDeliveryMode') === 'HomeDelivery'
          ) {
            //Trigger Delivery Services Search
            if (
              OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)
            ) {
              const date = new Date();
              const body = {
                lines: [
                  {
                    lineId: line.id,
                    product: line.get('product').get('id'),
                    productCategory: line.get('product').get('productCategory')
                  }
                ],
                terminalTime: date,
                terminalTimeOffset: date.getTimezoneOffset(),
                remoteFilters: [
                  {
                    columns: [],
                    operator: 'filter',
                    value: 'OBRDM_DeliveryServiceFilter',
                    params: [true]
                  }
                ]
              };

              try {
                let data = await OB.App.Request.mobileServiceRequest(
                  'org.openbravo.retail.posterminal.process.HasDeliveryServices',
                  body
                );
                data = data.response.data;

                if (data && data.exception) {
                  //ERROR or no connection
                  OB.UTIL.showError(
                    OB.I18N.getLabel('OBPOS_ErrorGettingRelatedServices')
                  );
                } else if (data) {
                  line.set('hasDeliveryServices', data[0].hasDeliveryServices);
                } else {
                  OB.UTIL.showError(
                    OB.I18N.getLabel('OBPOS_ErrorGettingRelatedServices')
                  );
                }
              } catch (error) {
                OB.UTIL.showError(
                  OB.I18N.getLabel('OBPOS_ErrorGettingRelatedServices')
                );
              }
            } else {
              let criteria = new OB.App.Class.Criteria();
              criteria = await OB.UTIL.servicesFilter(
                criteria,
                line.get('product').get('id'),
                line.get('product').get('productCategory')
              );
              criteria.criterion('obrdmIsdeliveryservice', true);
              try {
                const products = await OB.App.MasterdataModels.Product.find(
                  criteria.build()
                );
                let data = [];
                for (let i = 0; i < products.length; i++) {
                  data.push(OB.Dal.transform(OB.Model.Product, products[i]));
                }

                if (data && data.length > 0) {
                  line.set('hasDeliveryServices', true);
                } else {
                  line.set('hasDeliveryServices', false);
                }
              } catch (error) {
                OB.UTIL.showError(
                  OB.I18N.getLabel('OBPOS_ErrorGettingRelatedServices')
                );
              }
            }
          }
        });
    },
    initComponents: function() {
      this.inherited(arguments);
    },
    setMultiSelected: function(inSender, inEvent) {
      var me = this,
        hideButton = false,
        isPaid = inSender.model.get('order').get('isPaid'),
        isEditable = inSender.model.get('order').get('isEditable');
      _.each(inEvent.models, function(line) {
        if (
          line.get('product') &&
          line.get('product') instanceof OB.Model.Product &&
          (line.get('product').get('productType') === 'S' ||
            line.get('qty') < 0 ||
            isPaid ||
            line.get('deliveredQuantity') === line.get('qty') ||
            !isEditable)
        ) {
          hideButton = true;
        }
      });
      if (hideButton) {
        me.setShowing(false);
      } else {
        me.setShowing(
          OB.MobileApp.model.hasPermission('OBRDM_EnableDeliveryModes', true)
        );
      }
    },
    setDetailsView: function(view) {
      this.detailsView = view;
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
    classes: 'obObposPointOfSaleUiEditLineDeliveryServicesButton',
    i18nContent: 'OBMOBC_MinusSign',
    handlers: {
      onSetMultiSelected: 'setMultiSelected',
      onRearrangedEditButtonBar: 'setMultiSelected'
    },
    tap: function(inSender, inEvent) {
      var product = this.owner.owner.line.get('product');
      if (product) {
        OB.UI.SearchProductCharacteristic.prototype.filtersCustomClear();
        OB.UI.SearchProductCharacteristic.prototype.filtersCustomAdd(
          new OB.UI.SearchServicesFilter({
            text: this.owner.owner.selectedModels
              .filter(function(line) {
                return line.get('hasDeliveryServices');
              })
              .map(function(line) {
                return line.get('product').get('_identifier');
              })
              .join(', '),
            productList: this.owner.owner.selectedModels
              .filter(function(line) {
                return line.get('hasDeliveryServices');
              })
              .map(function(line) {
                return line.get('product').get('id');
              }),
            orderlineList: this.owner.owner.selectedModels.filter(function(
              line
            ) {
              return line.get('hasDeliveryServices');
            }),
            extraParams: {
              isDeliveryService: true
            }
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
              return line.get('hasDeliveryServices');
            })
            .forEach(function(l) {
              l.set('deliveryServiceProposed', true);
            });
        }, 1);
      }
    },
    init: function(model) {
      this.model = model;
    },
    setMultiSelected: function(inSender, inEvent) {
      if (
        this.owner.owner.selectedModels &&
        this.owner.owner.selectedModels.length > 0
      ) {
        var proposedServices, existRelatedServices;
        existRelatedServices =
          this.owner.owner.selectedModels.filter(function(line) {
            return line.get('hasDeliveryServices');
          }).length === this.owner.owner.selectedModels.length;
        proposedServices =
          this.owner.owner.selectedModels.filter(function(line) {
            return (
              !line.get('hasDeliveryServices') ||
              line.get('deliveryServiceProposed')
            );
          }).length === this.owner.owner.selectedModels.length;
        if (existRelatedServices) {
          this.show();
          if (proposedServices) {
            this.addRemoveClass(
              'obObposPointOfSaleUiEditLineDeliveryServicesButton_unreviewed',
              false
            );
            this.addRemoveClass(
              'obObposPointOfSaleUiEditLineDeliveryServicesButton_reviewed',
              true
            );
          } else {
            this.addRemoveClass(
              'obObposPointOfSaleUiEditLineDeliveryServicesButton_unreviewed',
              true
            );
            this.addRemoveClass(
              'obObposPointOfSaleUiEditLineDeliveryServicesButton_reviewed',
              false
            );
          }
        } else {
          this.hide();
        }
      } else if (
        this.owner.owner.line &&
        this.owner.owner.line.get('hasDeliveryServices')
      ) {
        this.show();
        if (this.owner.owner.line.get('obposServiceProposed')) {
          this.addRemoveClass(
            'obObposPointOfSaleUiEditLineDeliveryServicesButton_unreviewed',
            false
          );
          this.addRemoveClass(
            'obObposPointOfSaleUiEditLineDeliveryServicesButton_reviewed',
            true
          );
        } else {
          this.addRemoveClass(
            'obObposPointOfSaleUiEditLineDeliveryServicesButton_unreviewed',
            true
          );
          this.addRemoveClass(
            'obObposPointOfSaleUiEditLineDeliveryServicesButton_reviewed',
            false
          );
        }
      } else {
        this.hide();
      }
    }
  });

  //Register the button...
  var servicesButtonIndex = OB.OBPOSPointOfSale.UI.EditLine.prototype.actionButtons.findIndex(
    function(button) {
      return button.name === 'showRelatedServices';
    }
  );
  OB.OBPOSPointOfSale.UI.EditLine.prototype.actionButtons.splice(
    servicesButtonIndex,
    0,
    {
      kind: 'OB.OBPOSPointOfSale.UI.EditLine.DeliveryServicesButton',
      name: 'deliveryServicesButton'
    }
  );
})();

enyo.kind({
  name: 'OBRDM.UI.MultiReceiptPropertiesDialogApply',
  kind: 'OB.UI.ModalDialogButton',
  classes: 'obrdmUiMultiReceiptPropertiesDialogApply',
  isDefaultAction: true,
  events: {
    onApplyChanges: ''
  },
  tap: function() {
    let me = this;
    this.doApplyChanges({
      callback: function() {
        me.doHideThisPopup();
      }
    });
  },
  initComponents: function() {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel('OBMOBC_LblApply'));
  }
});

enyo.kind({
  name: 'OBRDM.UI.MultiReceiptPropertiesDialogCancel',
  kind: 'OB.UI.ModalDialogButton',
  classes: 'obrdmUiMultiReceiptPropertiesDialogCancel',
  i18nLabel: 'OBMOBC_LblCancel',
  tap: function() {
    this.doHideThisPopup();
  },
  initComponents: function() {
    this.inherited(arguments);
  }
});

enyo.kind({
  name: 'OBRDM.UI.ModalReceiptMultiLinesProperties',
  kind: 'OB.UI.Modal',
  classes: 'obrdmUiModalReceiptMultiLinesProperties',
  handlers: {
    onApplyChanges: 'applyChanges'
  },
  executeOnShow: function() {
    if (!OB.UTIL.isNullOrUndefined(this.args.product)) {
      this.product = this.args.product;
      this.currentLine = null;
      this.organization = this.args.organization;
    }
    if (this.currentLine || this.product) {
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
  executeOnHide: function() {
    this.product = null;
    if (
      this.args &&
      this.args.requiredFiedls &&
      this.args.requiredFieldNotPresentFunction
    ) {
      var smthgPending = _.find(
        this.args.requiredFiedls,
        function(fieldName) {
          return OB.UTIL.isNullOrUndefined(this.currentLine.get(fieldName));
        },
        this
      );
      if (smthgPending) {
        this.args.requiredFieldNotPresentFunction(
          this.currentLine,
          smthgPending
        );
      }
    }
  },
  i18nHeader: 'OBRDM_ReceiptLineDeliveryModeDialogTitle',
  body: {
    kind: 'Scroller',
    classes: 'obrdmUiModalReceiptMultiLinesProperties-body',
    thumb: true,
    horizontal: 'hidden',
    components: [
      {
        name: 'attributes',
        classes: 'obrdmUiModalReceiptMultiLinesProperties-body-attributes'
      }
    ]
  },
  footer: {
    classes: 'obUiModal-footer-mainButtons',
    components: [
      {
        kind: 'OBRDM.UI.MultiReceiptPropertiesDialogCancel',
        name: 'receiptMultiLinePropertiesCancelBtn',
        classes:
          'obrdmUiModalReceiptMultiLinesProperties-receiptMultiLinePropertiesCancelBtn'
      },
      {
        kind: 'OBRDM.UI.MultiReceiptPropertiesDialogApply',
        name: 'receiptMultiLinePropertiesApplyBtn',
        classes:
          'obrdmUiModalReceiptMultiLinesProperties-receiptMultiLinePropertiesApplyBtn'
      }
    ]
  },
  loadValue: function(mProperty, component) {
    this.waterfall('onLoadValue', {
      model: this.currentLine || this.product,
      modelProperty: mProperty,
      organization: this.organization
    });

    // Make it visible or not...
    if (component.showProperty) {
      component.showProperty(this.currentLine || this.product, function(value) {
        component.owner.owner.setShowing(value);
      });
    }
  },
  applyChanges: async function(inSender, inEvent) {
    var i,
      diff,
      att,
      result = true,
      me = this,
      carrierLines = null;
    diff = this.propertycomponents;
    //Delivery date validation
    var dateSelected = diff.obrdmDeliveryDate.getValue();
    var today = new Date();
    if (diff.obrdmDeliveryDate.owner.owner.getShowing()) {
      if (dateSelected !== null && dateSelected !== '') {
        if (
          new Date(dateSelected.toDateString()) < new Date(today.toDateString())
        ) {
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
              result =
                result && diff[att].applyValue(this.args.selectedLines[i]);
            }
          } else {
            result =
              result && diff[att].applyValue(this.currentLine || this.product);
          }
        }
      }
    }

    // this.model.get('order').save();
    this.model.get('order').trigger('updateView');
    this.model.get('order').calculateReceipt();
    if (this.args.selectedLines) {
      carrierLines = this.args.selectedLines
        .filter(function(l) {
          return l.get('obrdmDeliveryMode') === 'HomeDelivery';
        })
        .map(function(l) {
          return {
            lineId: l.id,
            product: l.get('product').get('id'),
            productCategory: l.get('product').get('productCategory')
          };
        });
    }

    async function countDeliveryServices(data) {
      var countLines = 0;
      data.forEach(function(res) {
        var orderLine = me.args.selectedLines.find(function(l) {
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
          customFilters: '',
          genericParent: ''
        };

        //Clear existing filters in Product Search
        me.owner.$.multiColumn.$.rightPanel.$.toolbarpane.$.searchCharacteristic.$.searchCharacteristicTabContent.doClearAction();

        OB.UI.SearchProductCharacteristic.prototype.filtersCustomClear();
        OB.UI.SearchProductCharacteristic.prototype.filterCustomClearConditions();
        OB.UI.SearchProductCharacteristic.prototype.filtersCustomAdd(
          new OB.UI.SearchServicesFilter({
            text: me.args.selectedLines
              .map(function(line) {
                return line.get('product').get('_identifier');
              })
              .join(', '),
            productList: me.args.selectedLines.map(function(line) {
              return line.get('product').get('id');
            }),
            orderlineList: me.args.selectedLines,
            extraParams: {
              isDeliveryService: true
            }
          })
        );

        me.bubble('onSelectFilter', {
          params: {
            skipProductCharacteristic: true,
            searchCallback: function(data) {
              if (data && data.length === 1) {
                var attrs = {};
                OB.UI.SearchProductCharacteristic.prototype.customFilters.forEach(
                  function(filter) {
                    var filterAttr = filter.lineAttributes();
                    if (filterAttr) {
                      _.each(_.keys(filterAttr), function(key) {
                        attrs[key] = filterAttr[key];
                      });
                    }
                  }
                );
                me.bubble('onAddProduct', {
                  product: data[0],
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

    if (
      carrierLines &&
      carrierLines.length === this.args.selectedLines.length
    ) {
      //Trigger Delivery Services Search
      if (OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)) {
        const date = new Date();
        const body = {
          lines: carrierLines,
          terminalTime: date,
          terminalTimeOffset: date.getTimezoneOffset(),
          remoteFilters: [
            {
              columns: [],
              operator: 'filter',
              value: 'OBRDM_DeliveryServiceFilter',
              params: [true]
            }
          ]
        };
        try {
          let data = await OB.App.Request.mobileServiceRequest(
            'org.openbravo.retail.posterminal.process.HasDeliveryServices',
            body
          );
          data = data.response.data;
          if (data && data.exception) {
            //ERROR or no connection
            OB.UTIL.showError(
              OB.I18N.getLabel('OBPOS_ErrorGettingRelatedServices')
            );
          } else if (data) {
            await countDeliveryServices(data);
          } else {
            OB.UTIL.showError(
              OB.I18N.getLabel('OBPOS_ErrorGettingRelatedServices')
            );
          }
        } catch (error) {
          OB.UTIL.showError(
            OB.I18N.getLabel('OBPOS_ErrorGettingRelatedServices')
          );
        }
      } else {
        var hasDeliveryServices = async function(line, callback) {
          let criteria = new OB.App.Class.Criteria();
          criteria = await OB.UTIL.servicesFilter(
            criteria,
            line.get('product').get('id'),
            line.get('product').get('productCategory')
          );
          criteria.criterion('obrdmIsdeliveryservice', true);
          try {
            const products = await OB.App.MasterdataModels.Product.find(
              criteria.build()
            );
            let data = [];
            for (let i = 0; i < products.length; i++) {
              data.push(OB.Dal.transform(OB.Model.Product, products[i]));
            }

            if (data && data.length > 0) {
              callback(true);
            } else {
              callback(false);
            }
          } catch (error) {
            OB.UTIL.showError(
              OB.I18N.getLabel('OBPOS_ErrorGettingRelatedServices')
            );
            callback(false);
          }
        };

        var data = [];
        var finalCallback = _.after(carrierLines.length, async function() {
          await countDeliveryServices(data);
        });

        me.args.selectedLines.forEach(function(carrierLine) {
          hasDeliveryServices(carrierLine, function(res) {
            data.push({
              lineId: carrierLine.get('id'),
              hasDeliveryServices: res
            });
            finalCallback();
          });
        });
      }
    }
    if (result && inEvent.callback) {
      inEvent.callback();
    } else {
      return result;
    }
  },
  validationMessage: function(args) {
    this.owner.doShowPopup({
      popup: 'modalValidateAction',
      args: args
    });
  },
  initComponents: function() {
    this.inherited(arguments);
    this.attributeContainer = this.$.body.$.attributes;

    this.propertycomponents = {};

    enyo.forEach(
      this.newAttributes,
      function(natt) {
        var editline = this.$.body.$.attributes.createComponent({
          kind: 'OB.UI.PropertyEditLine',
          name: 'line_' + natt.name,
          classes: 'obrdmUiModalReceiptMultiLinesProperties-line',
          coreElement: natt
        });
        this.propertycomponents[natt.modelProperty] = editline.coreElement;
        this.propertycomponents[natt.modelProperty].propertiesDialog = this;
      },
      this
    );
  },
  init: function(model) {
    this.model = model;
    this.model
      .get('order')
      .get('lines')
      .on(
        'selected',
        function(lineSelected) {
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
        },
        this
      );
  }
});

enyo.kind({
  name: 'OBRDM.UI.ModalReceiptMultiLinesImpl',
  kind: 'OBRDM.UI.ModalReceiptMultiLinesProperties',
  classes: 'obrdmUiModalReceiptMultiLinesImpl',
  newAttributes: [
    {
      name: 'ReceiptLineDeliveryMode',
      kind: 'OB.UI.renderComboProperty',
      classes: 'obrdmUiModalReceiptMultiLinesImpl-receiptLineDeliveryMode',
      modelProperty: 'obrdmDeliveryMode',
      i18nLabel: 'OBRDM_DeliveryMode',
      // Model Collection to use. This definition has been created when registering the
      // ServeOption model
      collection: new Backbone.Collection(),
      retrievedPropertyForValue: 'id',
      retrievedPropertyForText: 'name',
      // This function is called to get the Modes
      fetchDataFunction: function(args) {
        OBRDM.UTIL.fillComboCollection(this, args);
      },
      // This function is called when the user accepts on the properties dialog,
      // and applies the value selected to the row.
      applyValue: function(row) {
        var value = this.getValue();
        row.set(this.modelProperty, value);

        if (value !== 'PickupInStoreDate' && value !== 'HomeDelivery') {
          row.unset('obrdmDeliveryDate');
        }

        if (value !== 'HomeDelivery') {
          row.unset('obrdmDeliveryTime');
        }

        return true;
      },
      init: function() {
        this.change = function(inSender, inEvent) {
          var cond = inSender.getValue();
          this.owner.owner.owner.owner.owner.owner.waterfallDown('onShowhide', {
            showDate: cond === 'PickupInStoreDate' || cond === 'HomeDelivery',
            showTime: cond === 'HomeDelivery'
          });
        };
      }
    },
    {
      kind: 'OB.UI.DatePicker',
      name: 'ReceiptLineDeliveryDate',
      classes: 'obrdmUiModalReceiptMultiLinesImpl-receiptLineDeliveryDate',
      modelProperty: 'obrdmDeliveryDate',
      i18nLabel: 'OBRDM_DeliveryDate',
      handlers: {
        onShowhide: 'onShowhide'
      },
      onShowhide: function(inSender, inEvent) {
        if (inEvent.showDate) {
          this.owner.owner.show();
          if (OB.UTIL.isNullOrUndefined(this.model.get('obrdmDeliveryDate'))) {
            this.setValue(this.model.get('obrdmDeliveryDate'));
          } else {
            this.setValue(
              this.model.get('obrdmDeliveryDate') instanceof Date
                ? this.model.get('obrdmDeliveryDate')
                : new Date(this.model.get('obrdmDeliveryDate'))
            );
          }
        } else {
          this.owner.owner.hide();
        }
      },
      // This function is called to determine whether the property is displayed or not
      showProperty: function(row, callback) {
        var oldCaptureTarget = enyo.dispatcher.captureTarget;
        this.model = row;
        var cond = row.get('obrdmDeliveryMode');
        var show = cond === 'PickupInStoreDate' || cond === 'HomeDelivery';
        if (show) {
          this.setValue(
            row.get('obrdmDeliveryDate') instanceof Date
              ? row.get('obrdmDeliveryDate')
              : new Date(row.get('obrdmDeliveryDate'))
          );
        }
        enyo.dispatcher.captureTarget = oldCaptureTarget;
        callback(show);
      },
      // This function is called when the user accepts on the properties dialog,
      // and applies the value selected to the row.
      applyValue: function(row) {
        var value = this.getValue();
        value.setHours(0);
        value.setMinutes(0);
        value.setSeconds(0);
        row.set(this.modelProperty, value);
        return true;
      }
    },
    {
      name: 'ReceiptLineDeliveryTime',
      kind: 'OB.UI.TimePickerSimple',
      classes: 'obrdmUiModalReceiptMultiLinesImpl-receiptLineDeliveryTime',
      modelProperty: 'obrdmDeliveryTime',
      i18nLabel: 'OBRDM_DeliveryTime',
      handlers: {
        onShowhide: 'onShowhide'
      },
      onShowhide: function(inSender, inEvent) {
        if (inEvent.showTime) {
          this.owner.owner.show();
          if (OB.UTIL.isNullOrUndefined(this.model.get('obrdmDeliveryDate'))) {
            this.setValue(this.model.get('obrdmDeliveryDate'));
          } else {
            this.setValue(
              this.model.get('obrdmDeliveryDate') instanceof Date
                ? this.model.get('obrdmDeliveryDate')
                : new Date(this.model.get('obrdmDeliveryDate'))
            );
          }
        } else {
          this.owner.owner.hide();
        }
      },
      // This function is called to determine whether the property is displayed or not
      showProperty: function(row, callback) {
        var oldCaptureTarget = enyo.dispatcher.captureTarget;
        this.model = row;
        var cond = row.get('obrdmDeliveryMode');
        var show = cond === 'HomeDelivery';
        if (show) {
          this.setValue(
            row.get('obrdmDeliveryTime') instanceof Date
              ? row.get('obrdmDeliveryTime')
              : new Date(row.get('obrdmDeliveryTime'))
          );
        }
        enyo.dispatcher.captureTarget = oldCaptureTarget;
        callback(show);
      },
      // This function is called when the user accepts on the properties dialog,
      // and applies the value selected to the row.
      applyValue: function(row) {
        var deliveryTime = new Date(row.get('obrdmDeliveryDate'));
        deliveryTime.setHours(this.getValue().getHours());
        deliveryTime.setMinutes(this.getValue().getMinutes());
        deliveryTime.setSeconds(0);
        row.set(this.modelProperty, deliveryTime);
        return true;
      }
    }
  ]
});

OB.UI.WindowView.registerPopup('OB.OBPOSPointOfSale.UI.PointOfSale', {
  kind: 'OBRDM.UI.ModalReceiptMultiLinesImpl',
  name: 'OBRDM_ReceiptMultilines'
});
