/*global define, setInterval */

define(['builder', 'utilities', 'i18n', 'model/order', 'model/terminal'], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.Scan = function (context) {
    var me = this;

    var undoclick;

    this.component = B(
      {kind: B.KindJQuery('div'), content: [
        {kind: B.KindJQuery('div'), attr: {'style': 'background: #7da7d9 url(img/scan.png) center center no-repeat; background-size: cover; color: white; height: 200px; margin: 5px; padding: 5px'}, content: [
          {kind: B.KindJQuery('div'), content: [
            {kind: B.KindJQuery('div'), id: 'msgwelcome', attr: {'style': 'padding: 10px; display: none;'}, content: [
              {kind: B.KindJQuery('div'), attr: {'style': 'float:right;'}, content: [
                OB.I18N.getLabel('OBPOS_WelcomeMessage')
              ]},
              {kind: B.KindJQuery('div'), id: 'clock', attr: {'style': 'float:right;padding-top: 20px; font-size:300%;font-weight:bold;clear:both;'}, content: [
              ]}        
            ]},
            {kind: B.KindJQuery('div'), id: 'msgaction', attr: {'style': 'padding: 10px; display: none;'}, content: [
              {kind: B.KindJQuery('div'), id: 'txtaction', attr: {'style': 'float:left;'}},
              {kind: B.KindJQuery('a'), attr: { 'href': '#', 'class': 'btnlink btnlink-small btnlink-orange', 'style': 'float:right;'}, content: [
                OB.I18N.getLabel('OBPOS_LblUndo')   
              ], init: function () {           
                  this.$el.click(function(e) {
                    e.preventDefault();
                    if (undoclick) {
                      undoclick();
                    }
                  });
              }}                  
            ]}
          ]}                    
        ]}        
      ]}
    );
    this.$el = this.component.$el;
    var msgwelcome = this.component.context.msgwelcome.$el;
    var txtaction = this.component.context.txtaction.$el;
    var msgaction = this.component.context.msgaction.$el;
    var clock = this.component.context.clock.$el;
    var updateclock = function () {
      clock.text(OB.I18N.formatHour(new Date()));
    };
    updateclock();
    setInterval(updateclock, 1000);
    
    this.receipt = context.modelorder;
    
    this.receipt.on('clear change:undo', function() {
      var undoaction = this.receipt.get('undo');
      if (undoaction) {
        msgwelcome.hide();
        msgaction.show();
        txtaction.text(undoaction.text);
        undoclick = undoaction.undo;        
      } else {
        msgaction.hide();
        msgwelcome.show();
      }
    }, this);
    
  };
  
});     