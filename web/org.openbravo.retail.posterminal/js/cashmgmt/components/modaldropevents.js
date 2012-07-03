/*global window, define, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ModalDropEvents = OB.COMP.Modal.extend({
    id: 'modaldropevents',
    header: 'Select Destinations',
    initialize: function () {
        OB.COMP.Modal.prototype.initialize.call(this); // super.initialize();
        var theModal = this.$el,
            theHeader = theModal.children(':first'),
            theBody = theModal.children(':nth-child(2)'),
            theHeaderText = theHeader.children(':nth-child(2)');
        theModal.addClass('modal-dialog');
        theBody.addClass('modal-dialog-body');
        theHeaderText.attr('text-align','left');
        theHeaderText.attr('font-weight', '150%');
        theHeaderText.attr('padding-top', '10px');
        theHeaderText.attr('color', 'black');
      },
    getContentView: function () {
      return ({kind:OB.COMP.SearchDropEvents});
    },
    showEvent: function (e) {
      // custom bootstrap event, no need to prevent default
    }
  });
}());