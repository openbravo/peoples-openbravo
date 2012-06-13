/*global window, define, Backbone */

define(['builder', 'utilities', 'utilitiesui', 'i18n', 'components/commonbuttons', 'components/table'], function (B) {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};


  OB.COMP.ModalProfile = OB.COMP.Modal.extend({
    id: 'profileDialog',
    header: OB.I18N.getLabel('OBPOS_ProfileDialogTitle'),
    initialize: function () {
      OB.COMP.Modal.prototype.initialize.call(this); // super.initialize();
      var theModal = this.$el,
          theHeader = theModal.children(':first'),
          theBody = theModal.children(':nth-child(2)'),
          theHeaderText = theHeader.children(':nth-child(2)');
      theModal.addClass('modal-profile');
      theHeader.addClass('modal-profile-header');
      theBody.addClass('modal-profile-body');
      theHeaderText.addClass('modal-profile-header-text');
    },
    getContentView: function () {
      return (
        {kind: B.KindJQuery('div'), content: [

          {kind: B.KindJQuery('div'), attr: {'class': 'modal-profile-content-text'}, content: [
            {kind: B.KindJQuery('div'), attr: {style: 'border: 1px solid #F0F0F0;'}, content: [
              {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #E2E2E2; color: black; width: 150px; height: 40px; float: left; text-align: right;'}, content: [
                {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px 8px 0px 0px;'}, content: [OB.I18N.getLabel('OBPOS_Role')]}
              ]},
              {kind: B.KindJQuery('div'), attr: {'style': 'float: left;'}, content: [
                {kind: B.KindJQuery('select'), attr: {'style': 'height: 40px; width: 343px; margin: 0px;'}, content: [
                  {kind: B.KindJQuery('option'), attr: {'style': 'height: 40px; width: 343px; margin: 0px;'}, content: ['Feature not yet implemented']}
                ]}
               // {kind: OB.COMP.ListView('select'), id: 'xx'}
              ]}
            ]},
            {kind: B.KindJQuery('div'), attr: {style: 'clear: both'}},
            {kind: B.KindJQuery('div'), attr: {style: 'border: 1px solid #F0F0F0;'}, content: [
              {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #E2E2E2; color: black; width: 150px; height: 40px; float: left; text-align: right;'}, content: [
                {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px 8px 0px 0px;'}, content: [OB.I18N.getLabel('OBPOS_Language')]}
              ]},
              {kind: B.KindJQuery('div'), attr: {'style': 'float: left;'}, content: [
                {kind: B.KindJQuery('select'), attr: {'style': 'height: 40px; width: 343px; margin: 0px;'}, content: [
                  {kind: B.KindJQuery('option'), attr: {'style': 'height: 40px; width: 343px; margin: 0px;'}, content: ['Feature not yet implemented']}
                ]}
               // {kind: OB.COMP.ListView('select'), id: 'xx'}
              ]}
            ]}
          ]},

          {kind: B.KindJQuery('div'), attr: {'style': 'padding: 0px 0px 30px 0px;'}, content: [
            {kind: OB.COMP.ProfileDialogApply},
            {kind: OB.COMP.ProfileDialogCancel}
          ]}

        ]}
      );
    }
  });


  // Apply the changes
  OB.COMP.ProfileDialogApply = OB.COMP.Button.extend({
    render: function () {
      this.$el.addClass('btnlink btnlink-gray modal-profile-content-button-apply');
      this.$el.html(OB.I18N.getLabel('OBPOS_LblApply'));
      return this;
    },
    clickEvent: function (e) {
      alert('Feature not yet implemented');
    }
  });

  // Cancel
  OB.COMP.ProfileDialogCancel = OB.COMP.Button.extend({
    render: function () {
      this.$el.addClass('btnlink btnlink-gray modal-profile-content-button-cancel');
      this.$el.html(OB.I18N.getLabel('OBPOS_LblCancel'));
      this.$el.attr('data-dismiss', 'modal');
      return this;
    },
    clickEvent: function (e) {
      return true;
    }
  });

});