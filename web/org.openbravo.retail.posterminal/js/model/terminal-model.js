/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Backbone, enyo, $, _ */

(function () {


  OB.Model.TerminalWindowModel = OB.Model.WindowModel.extend({

    /** 
     * Abstract function that concrete classes must overwrite to perform actions
     * after a supervisor approves an action
     * or if not overwritten, provide a callback in OB.UTIL.Approval.requestApproval invocation
     */
    approvedRequest: function (approved, supervisor, approvalType, callback) {
      if (enyo.isFunction(callback)) {
        callback(approved, supervisor, approvalType);
      }
    },

    /**
     * Generic approval checker. It validates user/password can approve the approvalType.
     * It can work online in case that user has done at least once the same approvalType
     * in this same browser. Data regarding privileged users is stored in supervisor table
     */
    checkApproval: function (approvalType, username, password, callback) {
      OB.Dal.initCache(OB.Model.Supervisor, [], null, null);
      if (OB.MobileApp.model.get('connectedToERP')) {
        new OB.DS.Process('org.openbravo.retail.posterminal.utility.CheckApproval').exec({
          u: username,
          p: password,
          approvalType: JSON.stringify(approvalType)
        }, enyo.bind(this, function (response, message) {
          var approved = false;
          if (response.exception) {
            OB.UTIL.showError(response.exception.message);
            this.approvedRequest(false, null, null, callback);
          } else {
            approved = response.canApprove;
            if (!approved) {
              OB.UTIL.showError(OB.I18N.getLabel('OBPOS_UserCannotApprove'));
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
                  supervisor.set('permissions', JSON.stringify(approvalType));
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
                  _.each(approvalType, function (perm) {
                    if (!_.contains(permissions, perm)) {
                      permissions.push(perm);
                    }
                  }, this);

                } else {
                  // revoke permission if it exists
                  _.each(approvalType, function (perm) {
                    if (_.contains(permissions, perm)) {
                      permissions = _.without(permissions, perm);
                    }
                  }, this);
                }
                supervisor.set('permissions', JSON.stringify(permissions));

                OB.Dal.save(supervisor);
              }
              this.approvedRequest(approved, supervisor, approvalType, callback);
            }));
          }
        }));
      } else { // offline
        OB.Dal.find(OB.Model.Supervisor, {
          'name': username
        }, enyo.bind(this, function (users) {
          var supervisor, countApprovals = 0,
              approved = false;
          if (users.models.length === 0) {
            alert(OB.I18N.getLabel('OBPOS_OfflineSupervisorNotRegistered'));
          } else {
            supervisor = users.models[0];
            if (supervisor.get('password') === OB.MobileApp.model.generate_sha1(password + supervisor.get('created'))) {
              _.each(approvalType, function (perm) {
                if (_.contains(JSON.parse(supervisor.get('permissions')), perm)) {
                  countApprovals += 1;
                }
              }, this);
              if (countApprovals === approvalType.length) {
                approved = true;
              } else {
                OB.UTIL.showError(OB.I18N.getLabel('OBPOS_UserCannotApprove'));
              }
            } else {
              OB.UTIL.showError(OB.I18N.getLabel('OBPOS_InvalidUserPassword'));
            }
          }
          this.approvedRequest(approved, supervisor, approvalType, callback);
        }), function () {});
      }
    }
  });

}());