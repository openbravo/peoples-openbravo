/*global define,_*/

define(['utilities', 'datasource'], function () {
  
  OB = window.OB || {};
  OB.DATA = window.OB.DATA || {};
  
  OB.DATA.Order = function (context) {
    this._id = 'DataOrder';
    this.context = context;
    
  };
  OB.DATA.Order.prototype.exec = function (order) {
    order.trigger('closed');    
    console.log(JSON.stringify(order.toJSON()));
  };
  
 });