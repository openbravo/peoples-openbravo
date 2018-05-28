OB.DataPoolSel = OB.DataPoolSel || {};

OB.DataPoolSel = {
  showMessage: function (data) {
    if (!data || !data.extraSettings || !data.extraSettings.messageKey) {
      return;
    }

    var tab = OB.MainView.TabSet.tabs.find('tabId', 'D829B2F06F444694B7080C9BA19428E6');
    if (tab && tab.pane && tab.pane.view && tab.pane.view.messageBar && tab.pane.view.messageBar.setMessage) {
      tab.pane.view.messageBar.setMessage(isc.OBMessageBar.TYPE_INFO, null, OB.I18N.getLabel(data.extraSettings.messageKey));
    }
  }
};