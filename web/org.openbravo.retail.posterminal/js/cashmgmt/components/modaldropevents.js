/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global window, define, Backbone */

(function () {

  OB = window.OB || {};
  OB.UI = window.OB.UI || {};

  OB.UI.ModalDropEvents = OB.COMP.Modal.extend({
    id: 'modaldropevents',
    header: OB.I18N.getLabel('OBPOS_SelectDropDestinations'),
    initialize: function () {
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
    getContentView: function () {
      return ({
        kind: OB.COMP.SearchDropEvents
      });
    },
    showEvent: function (e) {
      // custom bootstrap event, no need to prevent default
    }
  });
}());