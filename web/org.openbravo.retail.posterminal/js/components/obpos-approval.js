/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

enyo.kind({
  name: 'OB.UTIL.Approval',
  kind: 'OB.UI.ModalAction',
  statics: {
    /**
     * Static method to display the approval popup.
     * 
     * When the approval is requested and checked, 'approvalChecked' event is
     * triggered in model parameter. This event has a boolean parameter 'approved'
     * that determines whether the approval was accepted or rejected. 
     */
    requestApproval: function (model, approvalType) {
      var dialog;

      dialog = OB.MobileApp.view.$.confirmationContainer.createComponent({
        kind: 'OB.UTIL.Approval',
        model: model,
        approvalType: approvalType
      });

      dialog.show();
    }
  },
  handlers: {
    onCheckCredentials: 'checkCredentials'
  },

  header: 'Approval required',
  //TODO: trl
  bodyContent: {
    name: 'loginInputs',
    classes: 'login-inputs-browser-compatible',
    components: [{
      components: [{
        classes: 'login-status-info',
        style: 'float: left;',
        name: 'connectStatus'
      }, {
        classes: 'login-status-info',
        name: 'screenLockedLbl'
      }]
    }, {
      components: [{
        kind: 'enyo.Input',
        type: 'text',
        name: 'username',
        classes: 'input-login',
        onkeydown: 'inputKeydownHandler'
      }]
    }, {
      components: [{
        kind: 'enyo.Input',
        type: 'password',
        name: 'password',
        classes: 'input-login',
        onkeydown: 'inputKeydownHandler'
      }]
    }]
  },

  bodyButtons: {
    components: [{
      kind: 'OB.UTIL.Approval.ApproveButton',
    }]
  },

  initComponents: function () {
    this.inherited(arguments);
    this.$.bodyContent.$.username.attributes.placeholder = OB.I18N.getLabel('OBMOBC_LoginUserInput');
    this.$.bodyContent.$.password.attributes.placeholder = OB.I18N.getLabel('OBMOBC_LoginPasswordInput');
  },

  checkCredentials: function () {
    var username = this.$.bodyContent.$.username.getValue(),
        password = this.$.bodyContent.$.password.getValue();
    OB.Dal.initCache(OB.Model.Supervisor, [], null, null);
    if (OB.MobileApp.model.get('connectedToERP')) {
      new OB.DS.Process('org.openbravo.retail.posterminal.utility.CheckApproval').exec({
        u: username,
        p: password,
        approvalType: this.approvalType
      }, enyo.bind(this, function (response, message) {
        var approved = false;
        if (response.exception) {
          OB.UTIL.showError(response.exception.message);
          approved = false;
        } else {
          approved = response.canApprove;
          if (!approved) {
            OB.UTIL.showError("User cannot approve");
          }

          // saving supervisor in local so next time it is possible to approve offline
          OB.Dal.find(OB.Model.Supervisor, {
            'id': response.userId
          }, enyo.bind(this, function (users) {
            var supervisor, date, permissions = [];
            if (users.models.length === 0) {
              // new user
              if (response.canApprove) {
                // insert in local db only in case it is supervisor for current type
                date = new Date().toString();
                supervisor = new OB.Model.Supervisor();

                supervisor.set('id', response.userId);
                supervisor.set('name', username);
                supervisor.set('password', OB.MobileApp.model.generate_sha1(password + date));
                supervisor.set('created', date);
                supervisor.set('permissions', JSON.stringify([this.approvalType]));
                OB.Dal.save(supervisor, null, null, true);
              }
            } else {
              // update existent user granting or revoking permission
              supervisor = users.models[0];

              supervisor.set('password', OB.MobileApp.model.generate_sha1(password + supervisor.get('created')));
              if (supervisor.get('permissions')) {
                permissions = JSON.parse(supervisor.get('permissions'));
              }

              if (response.canApprove) {
                // grant permission if it does not exist
                if (!_.contains(permissions, this.approvalType)) {
                  permissions.push(this.approvalType);
                }
              } else {
                // revoke permission if it exists
                if (_.contains(permissions, this.approvalType)) {
                  permissions = _.without(permissions, this.approvalType);
                }
              }
              supervisor.set('permissions', JSON.stringify(permissions));

              OB.Dal.save(supervisor);
            }
          }));
        }
        this.model.trigger('approvalChecked', {
          approved: approved
        });
      }));
    } else { // offline
      console.log('offline')
      OB.Dal.find(OB.Model.Supervisor, {
        'name': username
      }, enyo.bind(this, function (users) {
        var supervisor, approved = false;
        if (users.models.length === 0) {
          alert(OB.I18N.getLabel('No offline user'));
        } else {
          supervisor = users.models[0];
          if (supervisor.get('password') === OB.MobileApp.model.generate_sha1(password + supervisor.get('created'))) {
            if (_.contains(JSON.parse(supervisor.get('permissions')), this.approvalType)) {
              approved = true;
            } else {
              OB.UTIL.showError('User cannot approve');
            }
          } else {
            alert('incorrect password');
          }
        }
        this.model.trigger('approvalChecked', {
          approved: approved
        });
      }), function () {});
    }

    this.waterfall('onHideThisPopup', {});
  }
});

enyo.kind({
  name: 'OB.UTIL.Approval.ApproveButton',
  kind: 'OB.UI.ModalDialogButton',
  i18nLabel: 'OBPOS_Approve',
  events: {
    onCheckCredentials: ''
  },
  tap: function () {
    this.doCheckCredentials();
  }
});