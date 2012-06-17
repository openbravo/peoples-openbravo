/*global define,_,console,Backbone */

(function (d) {

  var dbSize = 10 * 1024 * 1024,
      undef, wsql = window.openDatabase !== undef,
      db = d || (wsql && window.openDatabase('WEBPOS', '0.1', 'Openbravo Web POS', dbSize));

  function S4() {
    return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1).toUpperCase();
  }

  function get_uuid() {
    return (S4() + S4() + S4() + S4() + S4() + S4() + S4() + S4());
  }


  function transform(model, obj) {
    var tmp = {},
        modelProto = model.prototype;
    _.each(modelProto.properties, function (prop) {
      tmp[prop] = obj[modelProto.propertyMap[prop]];
    });
    return tmp;
  }

  function dbSuccess() {

  }

  function dbError() {
    if (window.console) {
      window.console.error(arguments);
    }
  }

  function find(model, whereClause, success, error) {
    var tableName = model.prototype.tableName,
        propertyMap = model.prototype.propertyMap,
        sql = 'SELECT * FROM ' + tableName,
        params = null,
        appendWhere = true,
        firstParam = true,
        k, v;

    if (db) {
      // websql
      if (whereClause && !_.isEmpty(whereClause)) {
        _.each(_.keys(whereClause), function (k) {

          if (appendWhere) {
            sql = sql + ' WHERE ';
            params = [];
            appendWhere = false;
          }

          sql = sql + (firstParam ? '' : ' AND ') + ' ' + propertyMap[k] + ' ';

          if (whereClause[k] === null) {
            sql = sql + ' IS null ';
          } else {
            sql = sql + ' = ? ';
            params.push(whereClause[k]);
          }

          if (firstParam) {
            firstParam = false;
          }

        });
      }

      //console.log(sql);
      //console.log(params);
      db.readTransaction(function (tx) {
        tx.executeSql(sql, params, function (tr, result) {
          var i, collectionType = OB.Collection[model.prototype.modelName + 'List'] || Backbone.Collection,
              collection = new collectionType(),
              len = result.rows.length;
          if (len === 0) {
            return null;
          } else {
            for (i = 0; i < len; i++) {
              collection.add(transform(model, result.rows.item(i)));
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
    var modelProto = model.constructor.prototype,
        tableName = modelProto.tableName,
        sql = '',
        params = null,
        firstParam = true,
        uuid;

    if (db) {
      // websql
      if (!tableName) {
        throw 'Missing table name in model';
      }

      if (model.get('id')) {
        // UPDATE
        sql = 'UPDATE ' + tableName + ' SET ';

        _.each(_.keys(model.attributes), function (attr) {

          if (attr === 'id') {
            return;
          }

          if (firstParam) {
            firstParam = false;
            params = [];
          } else {
            sql = sql + ', ';
          }

          sql = sql + modelProto.propertyMap[attr] + ' = ? ';

          params.push(model.attributes[attr]);
        });

        sql = sql + ' WHERE ' + tableName + '_id = ?';
        params.push(model.attributes.c_tax_id);
      } else {
        // INSERT
        params = [];
        sql = modelProto.insertStatement;
        params.push(get_uuid());

        _.each(modelProto.properties, function (property) {
          if ('id' === property) {
            return;
          }
          params.push(model.get(property) === undefined ? null : model.get(property));
        });
        console.log(params.length);
      }

      //console.log(sql);
      //console.log(params);
      db.transaction(function (tx) {
        tx.executeSql(sql, params, success, error);
      });
    } else {
      throw 'Not implemented';
    }
  }

  function get(model, id, success, error) {
    var tableName = model.prototype.tableName,
        sql = 'SELECT * FROM ' + tableName + ' WHERE ' + tableName + '_id = ?';

    if (db) {
      // websql
      db.readTransaction(function (tx) {
        tx.executeSql(sql, [id], function (tr, result) {
          if (result.rows.length === 0) {
            return null;
          } else {
            success(new model(transform(model, result.rows.item(0))));
          }
        }, error);
      });
    } else {
      // localStorage
      throw 'Not implemented';
    }
  }

  function initCache(model, initialData, successCallback, errorCallback) {

    if (db) {
      if (!model.prototype.createStatement || !model.prototype.dropStatement) {
        throw 'Model requires a create and drop statement';
      }

      db.transaction(function (tx) {
        tx.executeSql(model.prototype.dropStatement);
      }, errorCallback);

      db.transaction(function (tx) {
        tx.executeSql(model.prototype.createStatement);
      }, errorCallback);

      if (_.isArray(initialData)) {
        db.transaction(function (tx) {
          var props = model.prototype.properties,
              propMap = model.prototype.propertyMap,
              values, _idx = 0;

          _.each(initialData, function (item) {
            values = [];

            _.each(props, function (propName) {
              if ('_idx' === propName) {
                return;
              }
              values.push(item[propName]);
            });
            values.push(_idx);

            tx.executeSql(model.prototype.insertStatement, values, null, errorCallback);
            _idx++;
          });
        }, errorCallback, function () {
          // transaction success, execute callback
          if (_.isFunction(successCallback)) {
            successCallback();
          }
        });
      } else { // no initial data
        throw 'Intial data must be passed as parameter';
      }
    } else {
      throw 'Not implemented';
    }

  }

  window.OB = window.OB || {};

  window.OB.Dal = {
    save: save,
    find: find,
    get: get,
    initCache: initCache
  };
}(OB && OB.DATA && OB.DATA.OfflineDB));