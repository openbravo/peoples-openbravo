/*global window, define, Backbone */

(function () {

  OB = window.OB || {};
  OB.UI = window.OB.UI || {};

  OB.UI.ModalDepositEvents = OB.COMP.Modal.extend({
    id: 'modaldepositevents',
    header: 'Select Destinations',
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
        kind: OB.COMP.SearchDepositEvents
      });
    },
    showEvent: function (e) {
      // custom bootstrap event, no need to prevent default
    }
  });
}());