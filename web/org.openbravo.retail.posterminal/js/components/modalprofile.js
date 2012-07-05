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
        url: '../../org.openbravo.retail.posterminal.service.profileutils?command=availableRoles&terminalName=' + terminalName + '&userId=' + userId,
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
        url: '../../org.openbravo.retail.posterminal.service.profileutils?command=availableLanguages',
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


      OB.COMP.ModalProfile = OB.COMP.ModalAction.extend({
        id: 'profileDialog',
        header: OB.I18N.getLabel('OBPOS_ProfileDialogTitle'),
        width: '500px',

        setBodyContent: function () {
          return (
            {kind: B.KindJQuery('div'), attr: {'style': 'height: 130px; background-color: #ffffff;'}, content: [
              {kind: B.KindJQuery('div'), content: [
                {kind: B.KindJQuery('div'), attr: {'style': 'border: 1px solid #F0F0F0; background-color: #E2E2E2; color: black; width: 150px; height: 40px; float: left; text-align: right;'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 3px 8px 0px 0px; font-size: 15px;'}, content: [OB.I18N.getLabel('OBPOS_Role')]}
                ]},
                {kind: B.KindJQuery('div'), attr: {'style': 'border: 1px solid #F0F0F0; float: left;'}, content: [
                 {kind: OB.UI.ListView('select'), attr: {
                    collection: myRoleCollection,
                    htmlId: 'profileRoleId',
                    className: 'modal-dialog-profile-combo',
                    renderLine: Backbone.View.extend({
                      tagName: 'option',
                      initialize: function () {
                        this.model = this.options.model;
                      },
                      render: function () {
                        this.$el.attr('value', this.model.get('id')).text(this.model.get('_identifier'));
                        if (roleId === this.model.get('id')) {
                          this.$el.attr('selected', 'selected');
                        }                        
                        return this;
                      }
                    })
                  }}
                ]}
              ]},
              {kind: B.KindJQuery('div'), attr: {style: 'clear: both'}},
              {kind: B.KindJQuery('div'), content: [
                {kind: B.KindJQuery('div'), attr: {'style': 'border: 1px solid #F0F0F0; background-color: #E2E2E2; color: black; width: 150px; height: 40px; float: left; text-align: right;'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 3px 8px 0px 0px; font-size: 15px;'}, content: [OB.I18N.getLabel('OBPOS_Language')]}
                ]},
                {kind: B.KindJQuery('div'), attr: {'style': 'border: 1px solid #F0F0F0; float: left;'}, content: [
                  {kind: OB.UI.ListView('select'), attr: {
                    collection: myLanguageCollection,
                    className: 'modal-dialog-profile-combo',
                    htmlId: 'profileLanguageId',
                    renderLine: Backbone.View.extend({
                      tagName: 'option',
                      initialize: function () {
                        this.model = this.options.model;
                      },
                      render: function () {
                        this.$el.attr('value', this.model.get('id')).text(this.model.get('_identifier'));
                        if (languageId === this.model.get('id')) {
                          this.$el.attr('selected', 'selected');
                        }
                        return this;
                      }
                    })
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
            ]}
          );
        },

        setBodyButtons: function () {
          return (
            {kind: B.KindJQuery('div'), content: [
              {kind: OB.COMP.ProfileDialogApply},
              {kind: OB.COMP.ProfileDialogCancel}
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
                  'default': isDefault,
                  'defaultRoleProperty': 'oBPOSDefaultPOSRole'
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