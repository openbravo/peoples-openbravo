/*global window, B, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};


  OB.COMP.ModalLogout = OB.COMP.ModalAction.extend({
    id: 'logoutDialog',
    header: OB.I18N.getLabel('OBPOS_LogoutDialogLogout'),

    setBodyContent: function() {
      return(
        {kind: B.KindJQuery('div'), content: [
          OB.I18N.getLabel('OBPOS_LogoutDialogText')
        ]}
      );
    },

    setBodyButtons: function() {
      return(
        {kind: B.KindJQuery('div'), content: [
          {kind: OB.COMP.LogoutDialogLogout},
        //{kind: OB.COMP.LogoutDialogLock}, //Disabled until feature be ready
          {kind: OB.COMP.LogoutDialogCancel}
        ]}
      );
    }
  });


  // Logout the application
  OB.COMP.LogoutDialogLogout = OB.COMP.Button.extend({
    render: function () {
      this.$el.addClass('btnlink btnlink-gray modal-dialog-content-button');
      this.$el.html(OB.I18N.getLabel('OBPOS_LogoutDialogLogout'));
      return this;
    },
    clickEvent: function (e) {
      OB.POS.logout();
    }
  });

  // Lock the application
  OB.COMP.LogoutDialogLock = OB.COMP.Button.extend({
    render: function () {
      this.$el.addClass('btnlink btnlink-gray modal-dialog-content-button');
      this.$el.html(OB.I18N.getLabel('OBPOS_LogoutDialogLock'));
      return this;
    },
    clickEvent: function (e) {
      OB.POS.lock();
    }
  });

  // Cancel
  OB.COMP.LogoutDialogCancel = OB.COMP.Button.extend({
    render: function () {
      this.$el.addClass('btnlink btnlink-gray modal-dialog-content-button');
      this.$el.html(OB.I18N.getLabel('OBPOS_LblCancel'));
      this.$el.attr('data-dismiss', 'modal');
      return this;
    },
    clickEvent: function (e) {
      return true;
    }
  });

}());