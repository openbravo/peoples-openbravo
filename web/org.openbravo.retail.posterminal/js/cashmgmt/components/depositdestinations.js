/*global B, Backbone */

(function () {

  OB = window.OB || {};
  OB.DATA = window.OB.DATA || {};

  OB.DATA.DepositDestinations = function(context) {
    this._id = 'DataDepositDestinations';

    this.destinations =  new Backbone.Collection([
    {'id':'01', 'name': 'Deposit 01'},
	{'id':'02', 'name': 'Deposit 02'},
	{'id':'03', 'name': 'Deposit 03'}
	]);

  };
}());

