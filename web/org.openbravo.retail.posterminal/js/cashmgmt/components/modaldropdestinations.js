/*global window, define, Backbone */

define(['builder', 'utilities', 'utilitiesui', 'i18n', 'components/commonbuttons', 'cashmgmt/components/renderdropdestinations'], function (B) {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ModalDropDestinations = OB.COMP.Modal.extend({
    id: 'modaldropdestinations',
    header: 'Select Destination for Cash Drop',
    getContentView: function () {
      return (
    	{kind:OB.COMP.RenderDropDestinations}
      );
    },
    showEvent: function (e) {
      // custom bootstrap event, no need to prevent default
      //this.options.modelorderlist.saveCurrent();
    //debugger;
    }
  });
});