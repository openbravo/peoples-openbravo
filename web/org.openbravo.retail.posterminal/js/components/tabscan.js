/*global window, B, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ButtonTabScan = OB.COMP.ToolbarButtonTab.extend({
    tabpanel: '#scan',
    label: OB.I18N.getLabel('OBPOS_LblScan'),
    render: function () {
      OB.COMP.ToolbarButtonTab.prototype.render.call(this); // super.initialize();
      this.options.modelorder.on('clear scan', function() {
        this.$el.tab('show');
        this.$el.parent().parent().addClass('active'); // Due to the complex construction of the toolbar buttons, forced active tab icon is needed
        OB.UTIL.setOrderLineInEditMode(false);
      }, this);
      this.options.SearchBPs.bps.on('click', function (model, index) {
        this.$el.tab('show');
        this.$el.parent().parent().addClass('active'); // Due to the complex construction of the toolbar buttons, forced active tab icon is needed
        OB.UTIL.setOrderLineInEditMode(false);
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

}());