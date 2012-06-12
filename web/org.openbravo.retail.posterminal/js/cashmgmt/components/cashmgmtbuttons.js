/*global window, define, $, Backbone */

define(['builder', 'utilities', 'i18n', 'components/commonbuttons'], function (B) {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.Done =Backbone.View.extend({
		_id: 'donebutton',
	    iconright: 'icon-white',
	    tagName: 'a',
	    className: 'btnlink btnlink-black',
	    label: OB.I18N.getLabel('OBPOS_LblDone'),
	    attributes: {'class': 'btnlink btnlink-gray',
	    	'style': 'position: relative; overflow: hidden; margin:0px; padding:0px; height:50px; width: 50px;',
	        'href': '#modaldropdestinations', 'data-toggle': 'modal'},
	   clickEvent: function (e) {
	    		//this.$el.hide();
	    		//this.options.countcash.;
	    	//this.options.modeldaycash.ok();
		   //this.options.modaldropdestinations.$el.show();
	   },

	   render: function () {
		      this.$el.addClass('btnlink');
		      if (this.icon) {
		        this.$el.append($('<i class=\"' + this.icon + '\"></i>'));
		      }
		      this.$el.append($('<span>' + this.label + '</span>'));
		      return this;
		    },
	  });
});