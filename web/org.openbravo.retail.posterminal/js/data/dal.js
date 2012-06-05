/*global define */

(function () {

  var dbSize = 10 * 1024 * 1024,
      undef, wsql = window.openDatabase !== undef,
      db = wsql && window.openDatabase('WEBPOS', '0.1', 'Openbravo Web POS', dbSize);


  function query(model, whereClause, success, error) {
    var tableName = model.prototype.tableName,
        sql = 'select * from ' + tableName;

    if (db) {
      // websql
      db.readTransaction(function (tx) {
        tx.executeSql(sql, null, function (tr, result) {
          var i, collection = new OB.Collection[model.modelName + 's'](),
              len = result.rows.length;
          if (len === 0) {
            return null;
          } else {
            for (i = 0; i < len; i++) {
              collection.add(result.rows.item(i));
            }
            success(collection);
          }
        }, error);
      });
    } else {
      // localStorage
      throw 'Not implemented';
    }
  }

  function save(model, sucess, error) {

  }

  function get(model, id, success, error) {
    var tableName = model.prototype.tableName,
        sql = 'select * from ' + tableName + ' where ' + tableName + '_id = ?';

    if (db) {
      // websql
      db.readTransaction(function (tx) {
        tx.executeSql(sql, [id], function (tr, result) {
          if (result.rows.length === 0) {
            return null;
          } else {
            success(new model(result.rows.item(0)));
          }
        }, error);
      });
    } else {
      // localStorage
      throw 'Not implemented';
    }
  }

  window.OB = window.OB || {};

  window.OB.Dal = {
    save: save,
    query: query,
    get: get
  };
}());