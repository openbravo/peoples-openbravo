/*global B, window, define, $, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.Done =Backbone.View.extend({
		_id: 'donebutton',
	    iconright: 'icon-white',
	    tagName: 'a',
	    className: 'btnlink btnlink-black',
	    label: OB.I18N.getLabel('OBPOS_LblDone'),
	    attributes: {'class': 'btnlink btnlink-gray','style': 'position: relative; overflow: hidden; margin:0px; padding:0px; height:50px; width: 50px;',
	        'href': '#modaldropdestinations', 'data-toggle': 'modal'},
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