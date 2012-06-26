/*global B, Backbone */

(function () {

  OB = window.OB || {};
  OB.DATA = window.OB.DATA || {};

  OB.DATA.DropDestinations = function(context) {
    this._id = 'DataDropDestinations';

    this.destinations =  new Backbone.Collection([
    {'id':'01', 'name': 'Drop 01'},
	{'id':'02', 'name': 'Drop 02'},
	{'id':'03', 'name': 'Drop 03'}
	]);

  };
}());

