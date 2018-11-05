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
  processesToListen: ['removeAssociations'],
  events: {
    onApplyChanges: '',
    onLineSelected: ''
  },
  initComponents: function () {
    this.inherited(arguments);
    OB.UTIL.ProcessController.subscribe(this.processesToListen, this);
  },
  destroyComponents: function () {
    this.inherited(arguments);
    OB.UTIL.ProcessController.unSubscribe(this.processesToListen, this);
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
  classes: 'modal-dialog-btn-check span1 checkbox_removeAssociations',
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
  classes: 'associatedOrderLine',
  handlers: {
    onLineSelected: 'lineSelected'
  },
  components: [{
    kind: 'OB.UI.CheckboxButtonRemoveAssociations',
    name: 'checkboxButtonRemoveAssociations'
  }, {
    name: 'productName',
    classes: 'span4 associatedOrderLine-productName'
  }, {
    name: 'orderedQuantity',
    classes: 'span2 associatedOrderLine-orderedQuantity'
  }, {
    name: 'documentNo',
    classes: 'span2 associatedOrderLine-documentNo'
  }, {
    name: 'customer',
    classes: 'span2 associatedOrderLine-customer'
  }, {
    classes: 'changedialog-properties-end'
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.productName.setContent(this.newAttribute.productName);
    this.$.orderedQuantity.setContent(this.newAttribute.qty);
    this.$.documentNo.setContent(this.newAttribute.orderDocumentNo);
    this.$.customer.setContent(this.newAttribute.bpName);
  },
  lineSelected: function (inSender, inEvent) {
    inEvent.selectedLine = this.newAttribute.orderlineId;
    return true;
  }
});

enyo.kind({
  name: 'OB.UI.ModalRemoveAssociatedTickets',
  kind: 'OB.UI.ModalAction',
  classes: 'modal-dialog modal-removeAssociatedTickets',
  handlers: {
    onApplyChanges: 'applyChanges',
    onLineSelected: 'lineSelected'
  },
  bodyContent: {
    kind: 'Scroller',
    maxHeight: '225px',
    classes: 'modal-removeAssociatedTickets-scroller',
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
      selectedLine.linesToAssociate = selectedLine.get('relatedLines');
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
    var execution = OB.UTIL.ProcessController.start('removeAssociations'),
        selectedLine = this.args.selectedLine;
    if (selectedLine.linesToAssociate && selectedLine.linesToAssociate.length === 0) {
      this.args.receipt.deleteLinesFromOrder([selectedLine]);
    } else if (selectedLine.linesToAssociate && selectedLine.linesToAssociate.length > 0) {
      if (selectedLine.get('quantityRule') === 'PP') {
        selectedLine.set('qty', selectedLine.get('relatedLines').length);
      }
      selectedLine.set('relatedLines', selectedLine.linesToAssociate);
      delete selectedLine.linesToAssociate;
      this.args.receipt.trigger('updateServicePrices');
      this.args.receipt.save(function () {
        return true;
      });
    }
    OB.UTIL.ProcessController.finish('removeAssociations', execution);
  },
  executeOnShow: function () {
    var me = this;
    OB.UTIL.showLoading(false);
    this.$.bodyContent.$.attributes.destroyComponents();
    this.$.header.destroyComponents();
    this.$.header.createComponent({
      name: 'ModalRemoveAssociatedTicketsHeader',
      classes: 'modal-removeAssociatedTickets-headerContent',
      components: [{
        content: OB.I18N.getLabel('OBPOS_ServiceHeader', [me.args.selectedLine.get('product').get('_identifier')]),
        name: 'serviceName',
        classes: 'span12 modal-removeAssociatedTickets-header-serviceName'
      }, {
        content: OB.I18N.getLabel('OBPOS_SelectAssociationsToRemoved'),
        name: 'linesLabel',
        classes: 'span12 modal-removeAssociatedTickets-header-linesLabel'
      }, {
        classes: 'changedialog-properties-end'
      }]
    });
    this.$.header.addClass('modal-removeAssociatedTickets-header');
    this.$.header.createComponent({
      name: 'HeaderLabels',
      classes: 'modal-removeAssociatedTickets-headerLabels',
      components: [{
        content: OB.I18N.getLabel('OBPOS_LblProductName'),
        name: 'productNameLbl',
        classes: 'span4 modal-removeAssociatedTickets-headerLabels-productName'
      }, {
        name: 'totalQtyLbl',
        content: OB.I18N.getLabel('OBPOS_LblQty'),
        classes: 'span2 modal-removeAssociatedTickets-headerLabels-totalQty'
      }, {
        name: 'receiptLbl',
        content: OB.I18N.getLabel('OBPOS_ticket'),
        classes: 'span2 modal-removeAssociatedTickets-headerLabels-receipt'
      }, {
        content: OB.I18N.getLabel('OBPOS_LblCustomer'),
        name: 'customerLbl',
        classes: 'span2 modal-removeAssociatedTickets-headerLabels-customer'
      }, {
        classes: 'changedialog-properties-end'
      }]
    });

    var lineNum = 0;
    _.each(me.args.selectedLine.get('relatedLines'), function (relatedLine) {
      if ((_.isUndefined(relatedLine.orderDocumentNo) || _.isUndefined(relatedLine.bpName) || _.isUndefined(relatedLine.qty)) && me.args.receipt) {
        var line = _.find(me.args.receipt.get('lines').models, function (line) {
          return line.id === relatedLine.orderlineId;
        });
        relatedLine.orderDocumentNo = me.args.receipt.get('documentNo');
        relatedLine.bpName = me.args.receipt.get('bp').get('name');
        relatedLine.qty = line.get('qty');
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