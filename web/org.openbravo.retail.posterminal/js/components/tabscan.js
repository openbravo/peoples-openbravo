/*global window, define, Backbone */

define(['builder', 'utilities', 'utilitiesui', 'i18n', 'components/commonbuttons', 'components/scan'], function (B) {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ButtonTabScan = OB.COMP.ButtonTab.extend({
    tabpanel: '#scan',
    label: OB.I18N.getLabel('OBPOS_LblScan'),
    render: function () {
      this.options.modelorder.on('clear scan', function() {
        this.$el.tab('show');
      }, this);
      this.options.SearchBPs.bps.on('click', function (model, index) {
        this.$el.tab('show');
      }, this);
      return this;
    },
    shownEvent: function (e) {
      this.options.keyboard.show('toolbarscan');
    }
  });

  OB.COMP.TabScan = Backbone.View.extend({
    tagName: 'div',
    attributes: {'id': 'scan', 'class': 'tab-pane'},
	initialize: function () {
      var scan = new OB.COMP.Scan(this.options);
      this.$el.append(scan.$el);
    }
  });

});