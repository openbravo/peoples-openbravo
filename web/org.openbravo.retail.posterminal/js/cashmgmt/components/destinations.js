/*global B, Backbone */

(function () {

  OB = window.OB || {};
  OB.DATA = window.OB.DATA || {};

  OB.DATA.Destinations = function(context) {
    this._id = 'DataDestinations';

    this.destinations =  new Backbone.Collection([
    {'id':'01', 'name': 'Envelope 01'},
	{'id':'02', 'name': 'Envelope 02'},
	{'id':'03', 'name': 'Envelope 03'}
	]);

  };
}());

