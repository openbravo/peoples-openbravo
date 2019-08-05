/*
 ************************************************************************************
 * Copyright (C) 2016-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, _*/
enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.ModalRemoveAssociations_btnApply',
  classes: 'obUiModalRemoveAssociationsBtnApply',
  isDefaultAction: true,
  i18nContent: 'OBPOS_LblApplyButton',
  processesToListen: ['removeAssociations'],
  events: {
    onApplyChanges: '',
    onLineSelected: ''
  },
  initComponents: function() {
    this.inherited(arguments);
    OB.UTIL.ProcessController.subscribe(this.processesToListen, this);
  },
  destroyComponents: function() {
    this.inherited(arguments);
    OB.UTIL.ProcessController.unSubscribe(this.processesToListen, this);
  },
  tap: function() {
    this.inherited(arguments);
    this.doHideThisPopup();
    this.doApplyChanges();
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.ModalRemoveAssociations_btnCancel',
  classes: 'obUiModalRemoveAssociationsBtnCancel',
  isDefaultAction: false,
  i18nContent: 'OBMOBC_LblCancel',
  events: {
    onLineSelected: ''
  },
  tap: function() {
    this.inherited(arguments);
    this.doHideThisPopup();
  }
});

enyo.kind({
  name: 'OB.UI.CheckboxButtonRemoveAssociations',
  kind: 'OB.UI.CheckboxButton',
  classes: 'obUiCheckboxButtonRemoveAssociations span1',
  events: {
    onLineSelected: ''
  },
  tap: function() {
    this.inherited(arguments);
    this.doLineSelected({
      selected: this.checked
    });
  }
});

enyo.kind({
  name: 'OB.UI.AssociatedOrderLine',
  classes: 'obUiAssociatedOrderLine',
  handlers: {
    onLineSelected: 'lineSelected'
  },
  components: [
    {
      kind: 'OB.UI.CheckboxButtonRemoveAssociations',
      name: 'checkboxButtonRemoveAssociations',
      classes: 'obUiAssociatedOrderLine-checkboxButtonRemoveAssociations'
    },
    {
      name: 'productName',
      classes: 'obUiAssociatedOrderLine-productName span4'
    },
    {
      name: 'orderedQuantity',
      classes: 'obUiAssociatedOrderLine-orderedQuantity span2'
    },
    {
      name: 'documentNo',
      classes: 'obUiAssociatedOrderLine-documentNo span2'
    },
    {
      name: 'customer',
      classes: 'obUiAssociatedOrderLine-customer span2'
    },
    {
      classes: 'obUiAssociatedOrderLine-element6 u-clearBoth'
    }
  ],
  initComponents: function() {
    this.inherited(arguments);
    this.$.productName.setContent(this.newAttribute.productName);
    this.$.orderedQuantity.setContent(this.newAttribute.qty);
    this.$.documentNo.setContent(this.newAttribute.orderDocumentNo);
    this.$.customer.setContent(this.newAttribute.bpName);
  },
  lineSelected: function(inSender, inEvent) {
    inEvent.selectedLine = this.newAttribute.orderlineId;
    //Return false value to propagate the event until applyChanges function
    return false;
  }
});

enyo.kind({
  name: 'OB.UI.ModalRemoveAssociatedTickets',
  kind: 'OB.UI.ModalAction',
  classes: 'obUiModalRemoveAssociatedTickets',
  handlers: {
    onApplyChanges: 'applyChanges',
    onLineSelected: 'lineSelected'
  },
  bodyContent: {
    kind: 'Scroller',
    classes: 'obUiModalRemoveAssociatedTickets-bodyContent-scroller',
    thumb: true,
    components: [
      {
        name: 'attributes',
        classes: 'obUiModalRemoveAssociatedTickets-scroller-attributes'
      }
    ]
  },
  bodyButtons: {
    classes: 'obUiModalRemoveAssociatedTickets-bodyButtons',
    components: [
      {
        kind: 'OB.UI.ModalRemoveAssociations_btnCancel',
        classes:
          'obUiModalRemoveAssociatedTickets-bodyButtons-obUiModalRemoveAssociationsBtnCancel'
      },
      {
        kind: 'OB.UI.ModalRemoveAssociations_btnApply',
        classes:
          'obUiModalRemoveAssociatedTickets-bodyButtons-obUiModalRemoveAssociationsBtnApply'
      }
    ]
  },
  initComponents: function() {
    this.inherited(arguments);
    this.attributeContainer = this.$.bodyContent.$.attributes;
  },
  lineSelected: function(inSender, inEvent) {
    var lines,
      selectedLine = this.args.selectedLine;
    if (!selectedLine.linesToAssociate) {
      selectedLine.linesToAssociate = selectedLine.get('relatedLines');
    }
    if (inEvent.selected) {
      lines = selectedLine.linesToAssociate.filter(function(relatedLine) {
        return relatedLine.orderlineId !== inEvent.selectedLine;
      });
      selectedLine.linesToAssociate = lines;
    } else {
      lines = _.find(selectedLine.get('relatedLines'), function(line) {
        return line.orderlineId === inEvent.selectedLine;
      });
      selectedLine.linesToAssociate.push(lines);
    }
    return true;
  },
  applyChanges: function(inSender, inEvent) {
    var execution = OB.UTIL.ProcessController.start('removeAssociations'),
      selectedLine = this.args.selectedLine;
    if (
      selectedLine.linesToAssociate &&
      selectedLine.linesToAssociate.length === 0
    ) {
      this.args.receipt.deleteLinesFromOrder([selectedLine]);
    } else if (
      selectedLine.linesToAssociate &&
      selectedLine.linesToAssociate.length > 0
    ) {
      if (selectedLine.get('quantityRule') === 'PP') {
        selectedLine.set('qty', selectedLine.get('relatedLines').length);
      }
      selectedLine.set('relatedLines', selectedLine.linesToAssociate);
      delete selectedLine.linesToAssociate;
      this.args.receipt.trigger('updateServicePrices');
      this.args.receipt.save(function() {
        OB.UTIL.ProcessController.finish('removeAssociations', execution);
        return true;
      });
    }
    OB.UTIL.ProcessController.finish('removeAssociations', execution);
    return true;
  },
  executeOnShow: function() {
    var me = this;
    OB.UTIL.showLoading(false);
    this.$.bodyContent.$.attributes.destroyComponents();
    this.$.header.destroyComponents();
    this.$.header.createComponent({
      name: 'ModalRemoveAssociatedTicketsHeader',
      classes:
        'obUiModalRemoveAssociatedTickets-header-modalRemoveAssociatedTicketsHeader',
      components: [
        {
          content: OB.I18N.getLabel('OBPOS_ServiceHeader', [
            me.args.selectedLine.get('product').get('_identifier')
          ]),
          name: 'serviceName',
          classes:
            'obUiModalRemoveAssociatedTickets-modalRemoveAssociatedTicketsHeader-serviceName'
        },
        {
          content: OB.I18N.getLabel('OBPOS_SelectAssociationsToRemoved'),
          name: 'linesLabel',
          classes:
            'obUiModalRemoveAssociatedTickets-modalRemoveAssociatedTicketsHeader-linesLabel'
        },
        {
          classes:
            'obUiModalRemoveAssociatedTickets-modalRemoveAssociatedTicketsHeader-element3 u-clearBoth'
        }
      ]
    });
    this.$.header.addClass('obUiModalRemoveAssociatedTickets-header');
    this.$.header.createComponent({
      name: 'HeaderLabels',
      classes: 'obUiModalRemoveAssociatedTickets-header-headerLabels',
      components: [
        {
          content: OB.I18N.getLabel('OBPOS_LblProductName'),
          name: 'productNameLbl',
          classes:
            'obUiModalRemoveAssociatedTickets-headerLabels-productNameLbl span4'
        },
        {
          name: 'totalQtyLbl',
          content: OB.I18N.getLabel('OBPOS_LblQty'),
          classes:
            'obUiModalRemoveAssociatedTickets-headerLabels-totalQtyLbl span2'
        },
        {
          name: 'receiptLbl',
          content: OB.I18N.getLabel('OBPOS_ticket'),
          classes:
            'obUiModalRemoveAssociatedTickets-headerLabels-receiptLbl span2'
        },
        {
          content: OB.I18N.getLabel('OBPOS_LblCustomer'),
          name: 'customerLbl',
          classes:
            'obUiModalRemoveAssociatedTickets-headerLabels-customerLbl span2 modal-removeAssociatedTickets-headerLabels-customer'
        },
        {
          classes:
            'obUiModalRemoveAssociatedTickets-headerLabels-element5 u-clearBoth'
        }
      ]
    });

    var lineNum = 0;
    _.each(me.args.selectedLine.get('relatedLines'), function(relatedLine) {
      if (
        (_.isUndefined(relatedLine.orderDocumentNo) ||
          _.isUndefined(relatedLine.bpName) ||
          _.isUndefined(relatedLine.qty)) &&
        me.args.receipt
      ) {
        var line = _.find(me.args.receipt.get('lines').models, function(line) {
          return line.id === relatedLine.orderlineId;
        });
        relatedLine.orderDocumentNo = me.args.receipt.get('documentNo');
        relatedLine.bpName = me.args.receipt.get('bp').get('name');
        if (!OB.UTIL.isNullOrUndefined(line)) {
          relatedLine.qty = line.get('qty');
        }
      }

      me.$.bodyContent.$.attributes.createComponent({
        kind: 'OB.UI.AssociatedOrderLine',
        name: 'line' + lineNum,
        classes:
          'obUiModalRemoveAssociatedTickets-attributes-obUiAssociatedOrderLine',
        newAttribute: relatedLine
      });
      lineNum++;
    });
    this.$.header.render();
    this.$.bodyContent.$.attributes.render();
  }
});
