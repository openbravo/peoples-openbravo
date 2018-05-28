OB.ExtraWindowSettingCallbackUtils = OB.ExtraWindowSettingCallbackUtils || {};

OB.ExtraWindowSettingCallbackUtils = {
  showInfoMessage: function (data) {
    if (!data || !data.extraSettings || !data.extraSettings.messageKey || !data.extraSettings.tabId) {
      return;
    }

    var tab = OB.MainView.TabSet.tabs.find('tabId', data.extraSettings.tabId);
    if (tab && tab.pane && tab.pane.view && tab.pane.view.messageBar && tab.pane.view.messageBar.setMessage) {
      tab.pane.view.messageBar.setMessage(isc.OBMessageBar.TYPE_INFO, null, OB.I18N.getLabel(data.extraSettings.messageKey));
    }
  }
};