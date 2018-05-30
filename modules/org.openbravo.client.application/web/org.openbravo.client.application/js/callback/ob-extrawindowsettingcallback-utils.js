OB.Utilities = OB.Utilities || {};

OB.Utilities.ExtraWindowSettingActions = {
  showInfoMessage: function (data) {
    var tab;
    if (!data || !data.extraSettings || !data.extraSettings.messageKey || !data.extraSettings.tabId) {
      return;
    }

    tab = OB.MainView.TabSet.tabs.find('tabId', data.extraSettings.tabId);
    if (tab && tab.pane && tab.pane.view && tab.pane.view.messageBar && tab.pane.view.messageBar.setMessage) {
      tab.pane.view.messageBar.setMessage(isc.OBMessageBar.TYPE_INFO, null, OB.I18N.getLabel(data.extraSettings.messageKey));
    }
  }
};