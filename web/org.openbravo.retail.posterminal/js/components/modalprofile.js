/*global window, B, Backbone, $ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ModalProfile = function (dialogsContainer) {
    this.dialogsContainer = dialogsContainer;
  };

  OB.COMP.ModalProfile.prototype.setModel = function (terminal) {
    this.terminal = terminal;

    this.terminal.on('change:context', function() {
      var ctx = this.terminal.get('context');

      if (!ctx) {
        return;
      }

      var terminalName = OB.POS.paramTerminal;
      var roleId = ctx.role.id;
      var languageId = OB.Application.language;
      var userId = ctx.user.id;

      var RoleModel = Backbone.Model.extend({});
      var RoleCollection = Backbone.Collection.extend({
        model: RoleModel,
        url: '../../org.openbravo.service.retail.posterminal.profileutils?command=availableRoles&terminalName=' + terminalName + '&userId=' + userId,
        parse: function (response, error) {
          if (response && response.response[0] && response.response[0].data) {
            return response.response[0].data;
          } else {
            return null;
          }
        }
      });
      var myRoleCollection = new RoleCollection();
      myRoleCollection.fetch();

      var LanguageModel = Backbone.Model.extend({});
      var LanguageCollection = Backbone.Collection.extend({
        model: LanguageModel,
        url: '../../org.openbravo.service.retail.posterminal.profileutils?command=availableLanguages',
        parse: function (response, error) {
          if (response && response.response[0] && response.response[0].data) {
            return response.response[0].data;
          } else {
            return null;
          }
        }
      });
      var myLanguageCollection = new LanguageCollection();
      myLanguageCollection.fetch();


      OB.COMP.ModalProfile = OB.COMP.Modal.extend({
        id: 'profileDialog',
        header: OB.I18N.getLabel('OBPOS_ProfileDialogTitle'),
        initialize: function () {
          OB.COMP.Modal.prototype.initialize.call(this); // super.initialize();
          var theModal = this.$el,
              theHeader = theModal.children(':first'),
              theBody = theModal.children(':nth-child(2)'),
              theHeaderText = theHeader.children(':nth-child(2)');
          theModal.addClass('modal-dialog');
          theModal.css('width', '500px');
          theHeader.addClass('modal-dialog-header');
          theBody.addClass('modal-dialog-body');
          theHeaderText.addClass('modal-dialog-header-text');
        },
        getContentView: function () {
          return (
            {kind: B.KindJQuery('div'), content: [

              {kind: B.KindJQuery('div'), attr: {'class': 'modal-dialog-content-text', 'style': 'height: 135px;'}, content: [
                {kind: B.KindJQuery('div'), content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'border: 1px solid #F0F0F0; background-color: #E2E2E2; color: black; width: 150px; height: 40px; float: left; text-align: right;'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'style': 'padding: 3px 8px 0px 0px; font-size: 15px;'}, content: [OB.I18N.getLabel('OBPOS_Role')]}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'border: 1px solid #F0F0F0; float: left;'}, content: [
                   {kind: OB.COMP.ListView('select'), attr: {
                      collection: myRoleCollection,
                      htmlId: 'profileRoleId',
                      className: 'modal-dialog-profile-combo',
                      renderLine: function (model) {
                        var optionElement;
                        if (roleId === model.get('id')) {
                          optionElement = {kind: B.KindJQuery('option'), attr: {value: model.get('id'), selected: 'selected'}, content: [
                              model.get('_identifier')
                          ]};
                        } else {
                          optionElement = {kind: B.KindJQuery('option'), attr: {value: model.get('id')}, content: [
                              model.get('_identifier')
                          ]};
                        }
                        return optionElement;
                      }
                    }}
                  ]}
                ]},
                {kind: B.KindJQuery('div'), attr: {style: 'clear: both'}},
                {kind: B.KindJQuery('div'), content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'border: 1px solid #F0F0F0; background-color: #E2E2E2; color: black; width: 150px; height: 40px; float: left; text-align: right;'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'style': 'padding: 3px 8px 0px 0px; font-size: 15px;'}, content: [OB.I18N.getLabel('OBPOS_Language')]}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'border: 1px solid #F0F0F0; float: left;'}, content: [
                    {kind: OB.COMP.ListView('select'), attr: {
                      collection: myLanguageCollection,
                      className: 'modal-dialog-profile-combo',
                      htmlId: 'profileLanguageId',
                      renderLine: function (model) {
                        var optionElement;
                        if (languageId === model.get('id')) {
                          optionElement = {kind: B.KindJQuery('option'), attr: {value: model.get('id'), selected: 'selected'}, content: [
                              model.get('_identifier')
                          ]};
                        } else {
                          optionElement = {kind: B.KindJQuery('option'), attr: {value: model.get('id')}, content: [
                              model.get('_identifier')
                          ]};
                        }
                        return optionElement;
                      }
                    }}

                  ]}
                ]},
                {kind: B.KindJQuery('div'), attr: {style: 'clear: both'}},
                {kind: B.KindJQuery('div'), content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'border: 1px solid #F0F0F0; background-color: #E2E2E2; color: black; width: 150px; height: 40px; float: left; text-align: right;'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'style': 'padding: 3px 8px 0px 0px; font-size: 15px;'}, content: ['Set as Default']}
                  ]},
                  {kind: B.KindJQuery('div'), attr: {'style': 'border: 1px solid #F0F0F0; float: left;'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'class': 'modal-dialog-profile-combo'}, content: [
                      {kind: OB.COMP.CheckboxButton, attr: {'className': 'modal-dialog-btn-check', 'id': 'profileDefault'}}
                    ]}
                  ]}
                ]}
              ]},

              {kind: B.KindJQuery('div'), attr: {'class': 'modal-dialog-content-buttons-container'}, content: [
                {kind: OB.COMP.ProfileDialogApply},
                {kind: OB.COMP.ProfileDialogCancel}
              ]}

            ]}
          );
        }
      });

      // Apply the changes
      OB.COMP.ProfileDialogApply = OB.COMP.Button.extend({
        isActive: true,
        render: function () {
          this.$el.addClass('btnlink btnlink-gray modal-dialog-content-button');
          this.$el.html(OB.I18N.getLabel('OBPOS_LblApply'));
          return this;
        },
        clickEvent: function (e) {
          if (OB.COMP.ProfileDialogApply.prototype.isActive) {
            OB.COMP.ProfileDialogApply.prototype.isActive = false;
            var newLanguageId = $('#profileLanguageId').val(),
                newRoleId = $('#profileRoleId').val(),
                isDefault = $('#profileDefault').hasClass('active'),
                actionURL = '../../org.openbravo.client.kernel?command=save&_action=org.openbravo.client.application.navigationbarcomponents.UserInfoWidgetActionHandler',
                postData = {
                  'language': newLanguageId,
                  'role': newRoleId,
                  'default': isDefault
                };
            $.ajax({
              url: actionURL,
              type: 'POST',
              contentType: 'application/json;charset=utf-8',
              dataType: 'json',
              data: JSON.stringify(postData),
              success: function (data, textStatus, jqXHR) {
                if(data.result === 'success') {
                  window.location.reload();
                } else {
                  OB.UTIL.showError(data.result);
                }
                OB.COMP.ProfileDialogApply.prototype.isActive = true;
              },
              error: function (jqXHR, textStatus, errorThrown) {
                OB.UTIL.showError(errorThrown);
                OB.COMP.ProfileDialogApply.prototype.isActive = true;
              }
            });
          }
        }
      });

      // Cancel
      OB.COMP.ProfileDialogCancel = OB.COMP.Button.extend({
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

      this.dialogsContainer.append(B({kind: OB.COMP.ModalProfile}).$el);

    },this);
  };

}());