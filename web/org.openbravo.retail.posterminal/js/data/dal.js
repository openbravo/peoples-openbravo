/*global define,_,console,Backbone */

(function () {

  var dbSize = 10 * 1024 * 1024,
      undef, wsql = window.openDatabase !== undef,
      db = wsql && window.openDatabase('WEBPOS', '0.1', 'Openbravo Web POS', dbSize);


  function find(model, whereClause, success, error) {
    var tableName = model.prototype.tableName,
        sql = 'select * from ' + tableName,
        params = null,
        appendWhere = true,
        firstParam = true,
        k, v;

    if (db) {
      // websql
      if (whereClause && !_.isEmpty(whereClause)) {
        _.each(_.keys(whereClause), function (k) {

          if (appendWhere) {
            sql = sql + ' where ';
            params = [];
            appendWhere = false;
          }

          sql = sql + (firstParam ? '' : ' and ') + ' ' + k + ' ';

          if (whereClause[k] === null) {
            sql = sql + ' is null ';
          } else {
            sql = sql + ' = ? ';
            params.push(whereClause[k]);
          }

          if (firstParam) {
            firstParam = false;
          }

        });
      }

      console.log(sql);
      console.log(params);

      db.readTransaction(function (tx) {
        tx.executeSql(sql, params, function (tr, result) {
          var i, collectionType = OB.Collection[model.modelName + 's'] || Backbone.Collection,
              collection = new collectionType(),
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

  function save(model, success, error) {
    var tableName = model.constructor.prototype.tableName,
        sql = '',
        params = null,
        firstParam = true;

    if (db) {
      // websql
      if (!tableName) {
        throw 'Missing table name in model';
      }

      if (model.attributes[tableName + '_id']) { // has an id model.get('id')
        // UPDATE
        sql = 'update ' + tableName + ' set ';

        _.each(_.keys(model.attributes), function (attr) {

          if (attr === 'c_tax_id') { // TODO: change to 'id'
            return;
          }

          if (firstParam) {
            firstParam = false;
            params = [];
          } else {
            sql = sql + ', ';
          }

          sql = sql + attr + ' = ? ';

          params.push(model.attributes[attr]);
        });

        sql = sql + ' where ' + tableName + '_id = ?'; // TODO: change to where id = ?
        params.push(model.attributes.c_tax_id);
      } else {
        // INSERT
        throw 'Not implemented';
      }

      console.log(sql);
      console.log(params);

      db.transaction(function (tx) {
        tx.executeSql(sql, params, success, error);
      });
    } else {
      throw 'Not implemented';
    }
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
    find: find,
    get: get
  };
}());