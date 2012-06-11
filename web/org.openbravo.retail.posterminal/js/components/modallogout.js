/*global window, define, Backbone */

define(['builder', 'utilities', 'utilitiesui', 'i18n', 'components/commonbuttons'], function (B) {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};


  OB.COMP.ModalLogout = OB.COMP.Modal.extend({
    id: 'logoutDialog',
    header: OB.I18N.getLabel('OBPOS_LogoutDialogLogout'),
    initialize: function () {
      OB.COMP.Modal.prototype.initialize.call(this); // super.initialize();
      var theModal = this.$el,
          theHeader = theModal.children(':first'),
          theHeaderText = theHeader.children(':nth-child(2)');
      theModal.addClass('modal-logout');
      theHeader.addClass('modal-logout-header');
      theHeaderText.addClass('modal-logout-header-text');
    },
    getContentView: function () {
      return (
        {kind: B.KindJQuery('div'), attr: {'class': 'modal-body', 'style': 'text-align: center;'}, content: [
          {kind: B.KindJQuery('div'), attr: {'class': 'modal-logout-content-text'}, content: [{kind: B.KindHTML(OB.I18N.getLabel('OBPOS_LogoutDialogText'))}]},
          {kind: OB.COMP.LogoutDialogLogout},
          {kind: OB.COMP.LogoutDialogLock},
          {kind: OB.COMP.LogoutDialogCancel}

        ]}
      );
    }
  });


  // Logout the application
  OB.COMP.LogoutDialogLogout = OB.COMP.Button.extend({
    render: function () {
      this.$el.addClass('btnlink btnlink-gray modal-logout-content-button');
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
      this.$el.addClass('btnlink btnlink-gray modal-logout-content-button');
      this.$el.html(OB.I18N.getLabel('OBPOS_LogoutDialogLock'));
      return this;
    },
    clickEvent: function (e) {
      alert('Feature not yet implemented');
    }
  });

  // Lock the application
  OB.COMP.LogoutDialogCancel = OB.COMP.Button.extend({
    render: function () {
      this.$el.addClass('btnlink btnlink-gray modal-logout-content-button');
      this.$el.html(OB.I18N.getLabel('OBPOS_LogoutDialogCancel'));
      this.$el.attr('data-dismiss', 'modal');
      return this;
    },
    clickEvent: function (e) {
      return true;
    }
  });

});