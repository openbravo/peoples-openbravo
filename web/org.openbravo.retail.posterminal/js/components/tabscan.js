/*global window, define, Backbone */

define(['builder', 'utilities', 'utilitiesui', 'i18n', 'components/commonbuttons', 'components/scan'], function (B) {
   
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  OB.COMP.ButtonTabScan = OB.COMP.ButtonTab.extend({
    tabpanel: '#scan',
    label: OB.I18N.getLabel('OBPOS_LblScan'),
    initialize: function () {
      OB.COMP.ButtonTab.prototype.initialize.call(this); // super.initialize();
      
      this.options.modelorder.on('clear scan', function() {
        this.$el.tab('show');                         
      }, this);   
      this.options.SearchBPs.bps.on('click', function (model, index) {
        this.$el.tab('show');
      }, this);         
    },    
    shownEvent: function (e) {      
      this.options.keyboard.show('toolbarscan');
    }       
  });  
  
  OB.COMP.TabScan = OB.COMP.CustomView.extend({
    createView: function () {
      return (
        {kind: B.KindJQuery('div'), attr: {'id': 'scan', 'class': 'tab-pane'}, content: [
          {kind: OB.COMP.Scan }                                                                      
        ]}         
      );
    }   
  }); 
  
});  