/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global define,_,console,Backbone */

(function () {

  var dbSize = 50 * 1024 * 1024,
      undef, wsql = window.openDatabase !== undef,
      db = (wsql && window.openDatabase('WEBPOS', '', 'Openbravo Web POS', dbSize)),
      OP;
  OB.POS.databaseVersion = '0.5';

  function dropTable(db, sql) {
    db.transaction(function (tx) {
      tx.executeSql(sql, {}, function () {
        console.log('succesfully dropped table: ' + sql);
      }, function () {
        window.console.error(arguments);
      });
    });
  }

  db.changeVersion(db.version, OB.POS.databaseVersion, function (t) {
    var model, modelObj;
    if (db.version === OB.POS.databaseVersion) {
      //Database version didn't change. No change needed.
      return;
    }
    //Version of the database changed, we need to drop the tables so they can be created again
    console.log('Updating database model. Tables will be dropped:');
    for (model in OB.Model) {
      if (OB.Model.hasOwnProperty(model)) {
        modelObj = OB.Model[model];
        if (modelObj.prototype && modelObj.prototype.dropStatement) {
          //There is a dropStatement, executing it
          dropTable(db, modelObj.prototype.dropStatement);
        }
      }
    }
  });

  window.OB.Dal.localDB = db;
}());