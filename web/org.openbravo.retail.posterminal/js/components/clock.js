/*global window, define, $, Backbone */

define(['builder', 'utilities', 'i18n'], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  // Generic Clock
  OB.COMP.Clock = Backbone.View.extend({
    tagName: 'div',
    attributes: {'style': 'position:absolute; bottom: 0px; right: 0px; text-align: center;'},
    initialize: function () {
      this.component = B(
        {kind: B.KindJQuery('div'), content: [ 
          {kind: B.KindJQuery('div'), id: 'clock', attr: {'style': 'font-size:300%; margin: 10px;'}},
          {kind: B.KindJQuery('div'), id: 'date', attr: {'style': 'font-size:125%; margin: 10px;'}}
        ]});
      this.$el.append(this.component.$el);
      
      var $clock = this.component.context.clock.$el;
      var $date = this.component.context.date.$el;
      var updateclock = function () {
        var d = new Date();
        $clock.text(OB.I18N.formatHour(d));
        $date.text(OB.I18N.formatDate(d));
      };
      updateclock();
      setInterval(updateclock, 1000);     
    }    
  });
});    

