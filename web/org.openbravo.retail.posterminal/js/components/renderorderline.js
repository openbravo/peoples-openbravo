/*global B, $ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.RenderOrderLine =  OB.COMP.SelectButton.extend({
    render: function() {
      this.$el.append($('<div/>').css('float', 'left').css('width', '40%').text(this.model.get('product').get('_identifier')))
              .append($('<div/>').css('float', 'left').css('width', '20%').css('text-align', 'right').text(this.model.printQty()))
              .append($('<div/>').css('float', 'left').css('width', '20%').css('text-align', 'right').text(this.model.printPrice()))
              .append($('<div/>').css('float', 'left').css('width', '20%').css('text-align', 'right').text(this.model.printGross()))
              .append($('<div/>').css('clear', 'both'));
      return this;
    }
  });
}());