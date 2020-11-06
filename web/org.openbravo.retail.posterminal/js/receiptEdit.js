/*
 ************************************************************************************
 * Copyright (C) 2018-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone, _, OBRDM */

var ReceiptDeliveryModeDefinition = {
  kind: 'OB.UI.renderComboProperty',
  name: 'ReceiptDeliveryMode',
  classes: 'receiptDeliveryMode',
  modelProperty: 'obrdmDeliveryModeProperty',
  i18nLabel: 'OBRDM_DefaultDeliveryMode',
  defaultValue: 'PickAndCarry',
  // Model Collection to use. This definition has been created when registering the
  // ServeOption model
  collection: new Backbone.Collection(),
  retrievedPropertyForValue: 'id',
  retrievedPropertyForText: 'name',
  handlers: {
    onApplyChange: 'applyChange',
    onSetModel: 'setModel'
  },
  // This function is called to get the Modes
  fetchDataFunction: function(args) {
    OBRDM.UTIL.fillComboCollection(this, args);
  },
  //This function is called when the user accepts on the properties dialog,
  // and applies the value selected to the row.
  applyChange: function() {
    var value = this.getValue();
    this.model.set(this.modelProperty, value);
  },
  setModel: function(inSender, inEvent) {
    this.model = inEvent.model;
  },
  init: function(model) {
    this.model = model.get('order');
    this.change = function(inSender, inEvent) {
      var cond = inSender.getValue(),
        showDate = cond === 'PickupInStoreDate' || cond === 'HomeDelivery',
        showTime = cond === 'HomeDelivery',
        moveScrollDown,
        me = this;
      moveScrollDown = function() {
        me.bubble('onMoveScrollDown', {
          lineHeight: document.getElementById(me.parent.parent.parent.parent.id)
            .clientHeight,
          target: 'filterSelectorButton_receiptProperties'
        });
      };
      OB.log('ReceiptLineDeliveryModeDefinition.selectChanged: ' + cond);
      this.owner.owner.owner.owner.waterfallDown('onShowhide', {
        showDate: showDate,
        showTime: showTime
      });
      if (showDate) {
        moveScrollDown();
      }
      if (showTime) {
        moveScrollDown();
      }
    };
    this.model.get('lines').on(
      'change:obrdmDeliveryMode',
      function(model) {
        if (
          model.get('obrdmDeliveryMode') !== 'HomeDelivery' &&
          model.has('hasDeliveryServices')
        ) {
          //Remove delivery services associations from current line
          model.set('hasDeliveryServices', false);
          var orderLineToRemove = model.get('id');
          var linesToRemove = [],
            triggerUpdateRelations = false;
          this.model.get('lines').forEach(function(line) {
            if (
              line.get('product').get('productType') === 'S' &&
              line.get('product').get('obrdmIsdeliveryservice')
            ) {
              var relatedLines = line
                .get('relatedLines')
                .filter(function(relatedLine) {
                  return relatedLine.orderlineId !== orderLineToRemove;
                });
              if (relatedLines.length === 0) {
                linesToRemove.push(line);
              } else if (relatedLines.length !== line.get('relatedLines')) {
                line.set('relatedLines', relatedLines);
                triggerUpdateRelations = true;
              }
            }
          });
          if (linesToRemove.length > 0) {
            this.model.deleteLinesFromOrder(linesToRemove);
          } else if (triggerUpdateRelations) {
            this.model.get('lines').trigger('updateRelations');
          }
        }
        if (model.get('obrdmDeliveryMode') === 'HomeDelivery') {
          model.set(
            'country',
            OB.MobileApp.model.receipt.get('bp').get('shipLocId')
              ? OB.MobileApp.model.receipt
                  .get('bp')
                  .get('locationModel')
                  .get('countryId')
              : null
          );
          model.set(
            'region',
            OB.MobileApp.model.receipt.get('bp').get('shipLocId')
              ? OB.MobileApp.model.receipt
                  .get('bp')
                  .get('locationModel')
                  .get('regionId')
              : null
          );
        } else {
          model.set('country', model.get('organization').country);
          model.set('region', model.get('organization').region);
        }
      },
      this
    );
  }
};

var ReceiptDeliveryDateDefinition = {
  name: 'ReceiptDeliveryDate',
  kind: 'OB.UI.DatePicker',
  classes: 'receiptDeliveryDate',
  modelProperty: 'obrdmDeliveryDateProperty',
  i18nLabel: 'OBRDM_DefaultDeliveryDate',
  handlers: {
    onShowhide: 'onShowhide',
    onLoadValue: 'loadValue',
    onApplyChange: 'applyValue',
    onSetModel: 'setModel'
  },
  onShowhide: function(inSender, inEvent) {
    if (inEvent.showDate) {
      this.owner.owner.show();
      this.setValue(this.model.get('obrdmDeliveryDate'));
    } else {
      this.owner.owner.hide();
    }
  },
  loadValue: function(inSender, inEvent) {
    if (inEvent.modelProperty === 'obrdmDeliveryDateProperty') {
      var cond = this.model.get('obrdmDeliveryModeProperty');
      if (cond === 'PickupInStoreDate' || cond === 'HomeDelivery') {
        this.owner.owner.show();
      } else {
        this.owner.owner.hide();
      }
    }
  },
  // This function is called when the user accepts on the properties dialog,
  // and applies the value selected to the row.
  applyValue: function() {
    var value = this.getValue(),
      deliveryModeProperty = this.model.get('obrdmDeliveryModeProperty');
    if (
      deliveryModeProperty === 'PickupInStoreDate' ||
      deliveryModeProperty === 'HomeDelivery'
    ) {
      value.setHours(0);
      value.setMinutes(0);
      value.setSeconds(0);
      this.model.set(this.modelProperty, value);
    } else {
      this.model.set(this.modelProperty, '');
    }
  },
  setModel: function(inSender, inEvent) {
    this.model = inEvent.model;
  },
  init: function(model) {
    this.model = model.get('order');
  }
};

var ReceiptDeliveryTimeDefinition = {
  name: 'ReceiptDeliveryTime',
  kind: 'OB.UI.TimePickerSimple',
  classes: 'receiptDeliveryTime',
  modelProperty: 'obrdmDeliveryTimeProperty',
  i18nLabel: 'OBRDM_DefaultDeliveryTime',
  handlers: {
    onShowhide: 'onShowhide',
    onLoadValue: 'loadValue',
    onApplyChange: 'applyValue',
    onSetModel: 'setModel'
  },
  onShowhide: function(inSender, inEvent) {
    if (inEvent.showTime) {
      this.owner.owner.show();
      this.setValue(this.model.get('obrdmDeliveryTime'));
    } else {
      this.owner.owner.hide();
    }
  },
  loadValue: function(inSender, inEvent) {
    if (inEvent.modelProperty === 'obrdmDeliveryTimeProperty') {
      var cond = this.model.get('obrdmDeliveryModeProperty');
      if (cond === 'HomeDelivery') {
        this.owner.owner.show();
      } else {
        this.owner.owner.hide();
      }
    }
  },
  // This function is called when the user accepts on the properties dialog,
  // and applies the value selected to the row.
  applyValue: function() {
    var value = this.getValue(),
      deliveryModeProperty = this.model.get('obrdmDeliveryModeProperty');
    if (deliveryModeProperty === 'HomeDelivery') {
      var deliveryDateProperty = this.model.get('obrdmDeliveryDateProperty'),
        deliveryTimeProperty = new Date(deliveryDateProperty);
      deliveryTimeProperty.setHours(value.getHours());
      deliveryTimeProperty.setMinutes(value.getMinutes());
      deliveryTimeProperty.setSeconds(0);
      this.model.set(this.modelProperty, deliveryTimeProperty);
    } else {
      this.model.set(this.modelProperty, '');
    }
    this.model.save();
  },
  setModel: function(inSender, inEvent) {
    this.model = inEvent.model;
  },
  init: function(model) {
    this.model = model.get('order');
  }
};

// Register the new property as a new property for receipt lines
OB.UI.ModalReceiptPropertiesImpl.prototype.newAttributes.push(
  ReceiptDeliveryModeDefinition
);
OB.UI.ModalReceiptPropertiesImpl.prototype.newAttributes.push(
  ReceiptDeliveryDateDefinition
);
OB.UI.ModalReceiptPropertiesImpl.prototype.newAttributes.push(
  ReceiptDeliveryTimeDefinition
);

(function() {
  //Delivery date validation
  var origTap = OB.UI.ReceiptPropertiesDialogApply.prototype.tap;
  OB.UI.ReceiptPropertiesDialogApply.prototype.tap = _.wrap(
    OB.UI.ReceiptPropertiesDialogApply.prototype.tap,
    function(wrapped) {
      var tap = _.bind(origTap, this),
        line_ReceiptDeliveryDate = this.owner.owner.$.body.$.attributes.$
          .line_ReceiptDeliveryDate,
        dateComponent = line_ReceiptDeliveryDate
          ? line_ReceiptDeliveryDate.$.newAttribute.$.ReceiptDeliveryDate
          : undefined,
        line_ReceiptDeliveryTime = this.owner.owner.$.body.$.attributes.$
          .line_ReceiptDeliveryTime,
        timeComponent = line_ReceiptDeliveryTime
          ? line_ReceiptDeliveryTime.$.newAttribute.$.ReceiptDeliveryTime
          : undefined;
      if (dateComponent && dateComponent.owner.owner.getShowing()) {
        var dateSelected = dateComponent.getValue(),
          today = new Date();

        if (dateSelected !== null && dateSelected !== '') {
          if (
            new Date(dateSelected.toDateString()) <
            new Date(today.toDateString())
          ) {
            OB.UTIL.showError(OB.I18N.getLabel('em_obrdm_delivery_date_chk'));
            return;
          }
        } else {
          OB.UTIL.showError(OB.I18N.getLabel('OBRDM_Delivery_Date_Inval'));
          return;
        }
        //Delivery time validation
        if (timeComponent && timeComponent.owner.owner.getShowing()) {
          var timeSelected = timeComponent.getValue();
          if (timeSelected !== null) {
            dateSelected.setHours(timeSelected.getHours());
            dateSelected.setMinutes(timeSelected.getMinutes());
            dateSelected.setSeconds(today.getSeconds());
            dateSelected.setMilliseconds(today.getMilliseconds());
            if (new Date(dateSelected) < new Date(today)) {
              OB.UTIL.showError(
                OB.I18N.getLabel('em_obrdm_delivery_datetime_chk')
              );
              return;
            }
          } else {
            OB.UTIL.showError(OB.I18N.getLabel('OBRDM_Delivery_Time_Inval'));
            return;
          }
        }
      }
      tap();
    }
  );
})();
