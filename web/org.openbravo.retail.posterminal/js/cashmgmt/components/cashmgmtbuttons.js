/*global B, window, define, $, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.Done =Backbone.View.extend({
		_id: 'donebutton',
	    iconright: 'icon-white',
	    tagName: 'a',
	    className: 'btnlink btnlink-green',
	    label: OB.I18N.getLabel('OBPOS_LblDone'),
	    attributes: {'style': 'height:25px; width: 125px; font-size:150%; padding-top:15px;'},
	    render: function () {
		      this.$el.addClass('btnlink');
		      if (this.icon) {
		        this.$el.append($('<i class=\"' + this.icon + '\"></i>'));
		      }
		      this.$el.append($('<span>' + this.label + '</span>'));
		      return this;
		    }
	  });
  OB.COMP.CashChange =Backbone.View.extend({
	  _id: 'cashchangebutton',
	  tagName: 'a',
	  label: OB.I18N.getLabel('OBPOS_LblDone'),
	  attributes: {'href': '#modaldropdestinations', 'data-toggle': 'modal'}
  });
}());