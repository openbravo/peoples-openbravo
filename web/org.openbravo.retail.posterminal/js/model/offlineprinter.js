/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone, _ */

(function () {

  var OfflinePrinter = OB.Data.ExtensibleModel.extend({
    modelName: 'OfflinePrinter',
    tableName: 'OfflinePrinter',
    entityName: 'OfflinePrinter',
    source: '',
    local: true
  });

  OfflinePrinter.addProperties([{
    name: 'id',
    column: 'offline_id',
    primaryKey: true,
    type: 'TEXT'
  }, {
    name: 'data',
    column: 'data',
    type: 'TEXT'
  }, {
    name: 'sendfunction',
    column: 'sendfunction',
    type: 'TEXT'
  }]);

  OfflinePrinter.printPendingJobs = function () {
    OB.Dal.find(OB.Model.OfflinePrinter, {}, function (jobs) {
      OB.Model.OfflinePrinter._printPendingJobs(jobs);
    });
  };

  OfflinePrinter._printPendingJobs = function (jobs) {
    var job, sendfunction;
    if (jobs.length > 0) {
      job = jobs.at(0);
      OB.POS.hwserver[job.get('sendfunction')](job.get('data'), function (result) {
        if (result && result.exception) {
          OB.UTIL.showError(result.exception.message);
        } else {
          // success. delete job and continue printing remaining jobs...
          OB.Dal.remove(job);
          jobs.remove(job);
          OB.Model.OfflinePrinter._printPendingJobs(jobs);
        }
      });
    }
  };

  OB.Data.Registry.registerModel(OfflinePrinter);
}());