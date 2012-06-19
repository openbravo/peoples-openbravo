/*global window, define, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ModalDropDestinations = OB.COMP.Modal.extend({
    id: 'modaldropdestinations',
    header: 'Select Destination for Cash Drop',
    getContentView: function () {
      return ({kind:OB.COMP.RenderDropDestinations});
    },
    showEvent: function (e) {
      // custom bootstrap event, no need to prevent default
      //this.options.modelorderlist.saveCurrent();
    //debugger;
    }
  });
}());