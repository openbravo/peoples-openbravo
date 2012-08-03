/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */


// Renders each of the deposit/drops destinations
OB.OBPOSCasgMgmt.UI.RenderDropDepDestinations = OB.COMP.SelectButton.extend({
  attributes: {
    'style': 'background-color:#dddddd;  border: 1px solid #ffffff;'
  },
  contentView: [{
    tag: 'div',
    id: 'divcontent',
    attributes: {
      style: 'padding: 1px 0px 1px 5px;'
    }
  }],

  render: function() {
    this.divcontent.text(this.model.get('name'));
    return this;
  }
});


// Popup with the destinations for deposits/drops
OB.OBPOSCasgMgmt.UI.SearchDepositEvents = Backbone.View.extend({
  tagName: 'div',
  className: 'row-fluid',

  contentView: [{
    tag: 'div',
    attributes: {
      'class': 'span12'
    },
    content: [{
      tag: 'div',
      content: [{
        id: 'tableview',
        view: OB.UI.TableView.extend({
          renderLine: OB.OBPOSCasgMgmt.UI.RenderDropDepDestinations,
          renderEmpty: OB.COMP.RenderEmpty
        })
      }]
    }]
  }],

  initialize: function() {
    OB.UTIL.initContentView(this);

    this.tableview.registerCollection(this.options.parent.model.getData(this.type));
  }
});


// Renders a modal popup with a list of reasons for drops/deposits
OB.UI.ModalDepositEvents = OB.COMP.Modal.extend({
  initialize: function() {
    var theModal, theHeader, theBody, theHeaderText;
    OB.COMP.Modal.prototype.initialize.call(this); // super.initialize();
    theModal = this.$el;
    theHeader = theModal.children(':first');
    theBody = theModal.children(':nth-child(2)');
    theHeaderText = theHeader.children(':nth-child(2)');
    theModal.addClass('modal-dialog');
    theBody.addClass('modal-dialog-body');
    theHeaderText.attr('text-align', 'left');
    theHeaderText.attr('font-weight', '150%');
    theHeaderText.attr('padding-top', '10px');
    theHeaderText.attr('color', 'black');
  },
  getContentView: function() {
    return OB.OBPOSCasgMgmt.UI.SearchDepositEvents.extend({
      type: this.type
    });
  },
  showEvent: function(e) {
    // custom bootstrap event, no need to prevent default
  }
});

//Renders a modal popup with a list of reasons for drops/deposits
enyo.kind({
  name: 'OB.OBPOSCasgMgmt.UI.ModalDepositEvents',
  kind: 'OB.UI.Modal',
  modalClass: 'modal-dialog',
  bodyClass: 'modal-dialog-body',
  body: {
    kind: 'OB.OBPOSCasgMgmt.UI.ListEvents'
  },

  init: function() {
    this.$.body.$.listEvents.init();
  }
});

//Popup with the destinations for deposits/drops
enyo.kind({
  name: 'OB.OBPOSCasgMgmt.UI.ListEvents',
  tag: 'div',
  classes: 'row-fluid',
  components: [{
    tag: 'div',
    classes: 'span12',
    components: [{
      tag: 'div',
      components: [{
        //tableview
        name: 'eventList',
        kind: 'OB.UI.Table',
        renderLine: 'OB.OBPOSCasgMgmt.UI.ListEventLine',
        renderEmpty: 'OB.UI.RenderEmpty'
      }]
    }]
  }],

  init: function() {
    this.$.eventList.setCollection(this.owner.owner.owner.model.getData(this.owner.owner.type));
  }

});

//Renders each of the deposit/drops destinations
enyo.kind({
  name: 'OB.OBPOSCasgMgmt.UI.ListEventLine',
  kind: 'OB.UI.SelectButton',
  //tag: 'div',
  style: 'background-color:#dddddd;  border: 1px solid #ffffff;',
  components: [{
    tag: 'div',
    name: 'line',
    style: 'padding: 1px 0px 1px 5px;'

  }],

  create: function() {
    this.inherited(arguments);
    this.$.line.setContent(this.model.get('name'));
  }
});