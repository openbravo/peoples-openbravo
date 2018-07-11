/*
 ************************************************************************************
 * Copyright (C) 2016-2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, Backbone, moment, _ */
enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.ModalRemoveAssociations_btnApply',
  isDefaultAction: true,
  i18nContent: 'OBPOS_LblApplyButton',
  events: {
    onApplyChanges: '',
    onLineSelected: ''
  },
  tap: function () {
    this.inherited(arguments);
    this.doHideThisPopup();
    this.doApplyChanges();
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.ModalRemoveAssociations_btnCancel',
  isDefaultAction: false,
  i18nContent: 'OBMOBC_LblCancel',
  events: {
    onLineSelected: ''
  },
  tap: function () {
    this.inherited(arguments);
    this.doHideThisPopup();
  }
});

enyo.kind({
  name: 'OB.UI.CheckboxButtonRemoveAssociations',
  kind: 'OB.UI.CheckboxButton',
  classes: 'modal-dialog-btn-check span1',
  style: 'width: 8%',
  events: {
    onLineSelected: ''
  },
  tap: function () {
    this.inherited(arguments);
    this.doLineSelected({
      selected: this.checked
    });
  }
});

enyo.kind({
  name: 'OB.UI.AssociatedOrderLine',
  style: 'border-bottom: 1px solid #cccccc; text-align: center; color: black; padding-top: 9px;',
  handlers: {
    onLineSelected: 'lineSelected'
  },
  components: [{
    kind: 'OB.UI.CheckboxButtonRemoveAssociations',
    name: 'checkboxButtonRemoveAssociations'
  }, {
    name: 'productName',
    classes: 'span4',
    style: 'line-height: 35px; font-size: 17px; width: 180px; padding-left: 10px;'
  }, {
    name: 'orderedQuantity',
    classes: 'span2',
    style: 'line-height: 35px; font-size: 17px; width: 85px;'
  }, {
    name: 'documentNo',
    classes: 'span2',
    style: 'line-height: 35px; font-size: 17px; width:180px;padding-left: 5px;'
  }, {
    name: 'customer',
    classes: 'span2',
    style: 'line-height: 35px; font-size: 17px; width:250px; padding-left: 20px;'
  }, {
    style: 'clear: both;'
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.productName.setContent(this.newAttribute.productName);
    this.$.orderedQuantity.setContent(this.newAttribute.qtyOrdered);
    this.$.documentNo.setContent(this.newAttribute.orderDocumentNo);
    this.$.customer.setContent(this.newAttribute.bpName);
  },
  lineSelected: function (inSender, inEvent) {
    inEvent.selectedLine = this.newAttribute.orderlineId;
  }
});

enyo.kind({
  name: 'OB.UI.ModalRemoveAssociatedTickets',
  kind: 'OB.UI.ModalAction',
  classes: 'modal-dialog',
  style: 'width: 900px;',
  handlers: {
    onApplyChanges: 'applyChanges',
    onCheckedAll: 'checkedAll',
    onLineSelected: 'lineSelected'
  },
  bodyContent: {
    kind: 'Scroller',
    maxHeight: '225px',
    style: 'background-color: #ffffff;margin-top: -8px;',
    thumb: true,
    horizontal: 'hidden',
    components: [{
      name: 'attributes'
    }]
  },
  bodyButtons: {
    components: [{
      kind: 'OB.UI.ModalRemoveAssociations_btnApply'
    }, {
      kind: 'OB.UI.ModalRemoveAssociations_btnCancel'
    }]
  },
  initComponents: function () {
    this.inherited(arguments);
    this.attributeContainer = this.$.bodyContent.$.attributes;
  },
  lineSelected: function (inSender, inEvent) {
    var lines, selectedLine = this.args.selectedLine;
    if (!selectedLine.linesToAssociate) {
      selectedLine.linesToAssociate = this.args.selectedLine.get('relatedLines');
    }
    if (inEvent.selected) {
      lines = selectedLine.linesToAssociate.filter(function (relatedLine) {
        return relatedLine.orderlineId !== inEvent.selectedLine;
      });
      selectedLine.linesToAssociate = lines;
    } else {
      lines = _.find(selectedLine.get('relatedLines'), function (line) {
        return line.orderlineId === inEvent.selectedLine;
      });
      selectedLine.linesToAssociate.push(lines);
    }
  },
  applyChanges: function (inSender, inEvent) {
    var selectedLine = this.args.selectedLine;
    if (!selectedLine.linesToAssociate || selectedLine.linesToAssociate.length === 0) {
      this.args.receipt.deleteLinesFromOrder([selectedLine]);
    } else {
      selectedLine.set('relatedLines', selectedLine.linesToAssociate);
      delete selectedLine.linesToAssociate;
      if (selectedLine.get('quantityRule') === 'PP') {
        selectedLine.set('qty', selectedLine.get('relatedLines').length);
      }
      this.args.receipt.save(function () {
        return true;
      });
    }
  },
  executeOnShow: function () {
    var me = this;
    OB.UTIL.showLoading(false);
    this.$.bodyContent.$.attributes.destroyComponents();
    this.$.header.destroyComponents();
    this.$.header.createComponent({
      name: 'CheckAllHeaderDocNum',
      style: 'text-align: center; color: white;',
      components: [{
        content: OB.I18N.getLabel('OBPOS_ServiceHeader', [me.args.selectedLine.get('product').get('_identifier')]),
        name: 'documentNo',
        classes: 'span12',
        style: 'line-height: 25px; font-size: 24px;'
      }, {
        content: OB.I18N.getLabel('OBPOS_SelectAssociationsToRemoved'),
        name: 'linesLabel',
        classes: 'span12',
        style: 'line-height: 25px; font-size: 20px;'
      }, {
        style: 'clear: both;'
      }]
    });
    this.$.header.addStyles('padding-bottom: 0px; margin: 0px; height: 140px;');
    this.$.header.createComponent({
      name: 'CheckAllHeader',
      style: 'overflow: hidden; padding-top: 20px; border-bottom: 3px solid #cccccc; text-align: center; color: black; margin-top: 15px; padding-bottom: 7px;  font-weight: bold; background-color: white; height:40px;',
      components: [{
        content: OB.I18N.getLabel('OBPOS_LblProductName'),
        name: 'productNameLbl',
        classes: 'span4',
        style: 'line-height: 25px; font-size: 17px;  width: 179px; padding-left: 80px;'
      }, {
        name: 'totalQtyLbl',
        content: OB.I18N.getLabel('OBPOS_LblQty'),
        classes: 'span2',
        style: 'line-height: 25px; font-size: 17px; width: 85px;'
      }, {
        name: 'receiptLbl',
        content: OB.I18N.getLabel('OBPOS_ticket'),
        classes: 'span2',
        style: 'line-height: 25px; font-size: 17px; width: 180px;'
      }, {
        content: OB.I18N.getLabel('OBPOS_LblCustomer'),
        name: 'customerLbl',
        classes: 'span2',
        style: 'line-height: 25px; font-size: 17px; width: 250px; padding-left: 20px;'
      }, {
        style: 'clear: both;'
      }]
    });

    var lineNum = 0;
    _.each(me.args.selectedLine.get('relatedLines'), function (relatedLine) {
      if (_.isUndefined(relatedLine.orderDocumentNo) && me.args.receipt) {
        relatedLine.orderDocumentNo = me.args.receipt.get('documentNo');
      }
      var lineEnyoObject = me.$.bodyContent.$.attributes.createComponent({
        kind: 'OB.UI.AssociatedOrderLine',
        name: 'line' + lineNum,
        newAttribute: relatedLine
      });
      lineNum++;
    });
    this.$.header.render();
    this.$.bodyContent.$.attributes.render();
  }
});