/*
 ************************************************************************************
 * Copyright (C) 2013-2017 Openbravo S.L.U.
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
      var me = this;
      OB.UTIL.VersionManagement.deprecated(35607, function () {
        OB.UTIL.checkApproval(approvalType, username, password, callback, me);
      });
    }
  });

}());