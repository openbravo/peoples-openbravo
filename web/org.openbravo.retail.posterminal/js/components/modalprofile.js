/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, $ */

enyo.kind({
  name: 'OB.UI.ModalProfile',
  kind: 'OB.UI.ModalAction',
  header: OB.I18N.getLabel('OBPOS_ProfileDialogTitle'),
  //bodyContentClass: 'modal-dialog-content-profile',
  bodyContent: {
    style: 'height: 127px; background-color: #ffffff;',
    components: [{
      components: [{
        style: 'border: 1px solid #F0F0F0; background-color: #E2E2E2; color: black; width: 150px; height: 40px; float: left; text-align: right;',
        components: [{
          style: 'padding: 5px 8px 0px 0px; font-size: 15px;',
          content: OB.I18N.getLabel('OBPOS_Role')
        }]
      }, {
        style: 'border: 1px solid #F0F0F0; float: left;',
        components: [{
          kind: 'OB.UI.List',
          name: 'roleList',
          classes: 'modal-dialog-profile-combo',
          renderEmpty: enyo.Control,
          renderLine: enyo.kind({
            kind: 'enyo.Option',
            initComponents: function () {
              this.setValue(this.model.get('id'));
              this.setContent(this.model.get('_identifier'));
              if (this.model.get('id') === this.owner.owner.owner.ctx.role.id) {
                this.setAttribute('selected', 'selected');
              }
            }
          })
        }]
      }]
    }, {
      style: 'clear: both'
    }, {
      components: [{
        style: 'border: 1px solid #F0F0F0; background-color: #E2E2E2; color: black; width: 150px; height: 40px; float: left; text-align: right;',
        components: [{
          style: 'padding: 5px 8px 0px 0px; font-size: 15px;',
          content: OB.I18N.getLabel('OBPOS_Language')
        }]
      }, {
        style: 'border: 1px solid #F0F0F0; float: left;',
        name: 'lang',
        components: [{
          kind: 'OB.UI.List',
          name: 'langList',
          tag: 'select',
          classes: 'modal-dialog-profile-combo',
          renderEmpty: enyo.Control,
          renderLine: enyo.kind({
            kind: 'enyo.Option',
            initComponents: function () {
              this.setValue(this.model.get('id'));
              this.setContent(this.model.get('_identifier'));
              this.setContent(this.model.get('_identifier'));
              if (this.model.get('id') === OB.Application.language) {
                this.setAttribute('selected', 'selected');
              }
            }
          })
        }]
      }]
    }, {
      style: 'clear: both'
    }, {
      components: [{
        style: 'border: 1px solid #F0F0F0; background-color: #E2E2E2; color: black; width: 150px; height: 40px; float: left; text-align: right;',
        components: [{
          style: 'padding: 5px 8px 0px 0px; font-size: 15px;',
          content: OB.I18N.getLabel('OBPOS_SetAsDefault')
        }]
      }, {
        style: 'border: 1px solid #F0F0F0; float: left;',
        components: [{
          classes: 'modal-dialog-profile-checkbox',
          components: [{
            kind: 'OB.UI.CheckboxButton',
            name: 'defaultBox',
            classes: 'modal-dialog-btn-check'
          }]
        }]
      }]
    }]
  },
  bodyButtons: {
    components: [{
      kind: 'OB.UI.ProfileDialogApply'
    }, {
      kind: 'OB.UI.ProfileDialogCancel'
    }]
  },
  initComponents: function () {
    this.inherited(arguments);
    // TODO: check this, not working: this.addStyles('width: 500px;');
    this.ctx = OB.POS.modelterminal.get('context');
    var terminalName = OB.POS.paramTerminal;
    var userId = this.ctx.user.id;
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
    this.$.bodyContent.$.roleList.setCollection(myRoleCollection);


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
    this.$.bodyContent.$.langList.setCollection(myLanguageCollection);
  }
});


enyo.kind({
  name: 'OB.UI.ProfileDialogApply',
  kind: 'OB.UI.Button',
  isActive: true,
  content: OB.I18N.getLabel('OBPOS_LblApply'),
  isApplyButton: true,
  classes: 'btnlink btnlink-gray modal-dialog-content-button',
  tap: function () {
    if (!this.isActive) {
      return;
    }
    this.isActive = false;
    var newLanguageId = this.owner.owner.$.bodyContent.$.langList.getValue(),
        newRoleId = this.owner.owner.$.bodyContent.$.roleList.getValue(),
        isDefault = this.owner.owner.$.bodyContent.$.defaultBox.checked,
        actionURL = '../../org.openbravo.client.kernel?command=save&_action=org.openbravo.client.application.navigationbarcomponents.UserInfoWidgetActionHandler',
        postData = {
        'language': newLanguageId,
        'role': newRoleId,
        'default': isDefault,
        'defaultRoleProperty': 'oBPOSDefaultPOSRole'
        };
    window.localStorage.setItem('POSlanguageId', newLanguageId);
    $.ajax({
      url: actionURL,
      type: 'POST',
      contentType: 'application/json;charset=utf-8',
      dataType: 'json',
      data: JSON.stringify(postData),
      success: function (data, textStatus, jqXHR) {
        if (data.result === 'success') {
          window.location.reload();
        } else {
          OB.UTIL.showError(data.result);
        }
        this.isActive = true;
      },
      error: function (jqXHR, textStatus, errorThrown) {
        OB.UTIL.showError(errorThrown);
        this.isActive = true;
      }
    });
  }
});

enyo.kind({
  name: 'OB.UI.ProfileDialogCancel',
  kind: 'OB.UI.Button',
  content: OB.I18N.getLabel('OBPOS_LblCancel'),
  classes: 'btnlink btnlink-gray modal-dialog-content-button',
  attributes: {
    'onEnterTap': 'hide'
  },
  events: {
    onHideThisPopup: ''
  },
  tap: function () {
    this.doHideThisPopup();
  }
});