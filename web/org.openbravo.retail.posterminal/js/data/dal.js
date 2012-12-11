/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
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
  OB.POS.databaseVersion = '0.4';

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

  OP = {
    EQ: '=',
    CONTAINS: 'contains',
    STARTSWITH: 'startsWith',
    ENDSWITH: 'endsWith'
  };

  function S4() {
    return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1).toUpperCase();
  }

  function get_uuid() {
    return (S4() + S4() + S4() + S4() + S4() + S4() + S4() + S4());
  }


  function transform(model, obj) {
    var tmp = {},
        modelProto = model.prototype,
        val;
    _.each(modelProto.properties, function (prop) {
      val = obj[modelProto.propertyMap[prop]];
      if (val === 'false') {
        tmp[prop] = false;
      } else if (val === 'true') {
        tmp[prop] = true;
      } else {
        tmp[prop] = val;
      }
    });
    return new model(tmp);
  }

  function dbSuccess() {

  }

  function dbError() {
    if (window.console) {
      window.console.error(arguments);
    }
  }

  function getWhereClause(criteria, propertyMap) {
    var appendWhere = true,
        firstParam = true,
        sql = '',
        params = [],
        res = {};
    if (criteria && !_.isEmpty(criteria)) {
      _.each(_.keys(criteria), function (k) {

        var undef, val = criteria[k],
            operator = (val !== null && val.operator !== undef) ? val.operator : '=',
            value = (val !== null && val.value !== undef) ? val.value : val;

        if (appendWhere) {
          sql = sql + ' WHERE ';
          params = [];
          appendWhere = false;
        }

        sql = sql + (firstParam ? '' : ' AND ') + ' ' + propertyMap[k] + ' ';

        if (value === null) {
          sql = sql + ' IS null ';
        } else {

          if (operator === OP.EQ) {
            sql = sql + ' = ? ';
          } else {
            sql = sql + ' like ? ';
          }

          if (operator === OP.CONTAINS) {
            value = '%' + value + '%';
          } else if (operator === OP.STARTSWITH) {
            value = value + '%';
          } else if (operator === OP.ENDSWITH) {
            value = value + '%';
          }
          params.push(value);
        }

        if (firstParam) {
          firstParam = false;
        }

      });
    }
    res.sql = sql;
    res.params = params;
    return res;
  }


  function find(model, whereClause, success, error, args) {
    var tableName = model.prototype.tableName,
        propertyMap = model.prototype.propertyMap,
        sql = 'SELECT * FROM ' + tableName,
        params = null,
        appendWhere = true,
        firstParam = true,
        k, v;

    if (db) {
      // websql
      if (whereClause && whereClause._whereClause) {
        whereClause.sql = ' ' + whereClause._whereClause;
      } else {
        whereClause = getWhereClause(whereClause, propertyMap);
      }
      sql = sql + whereClause.sql;
      params = whereClause.params;

      if (model.prototype.propertyMap._idx) {
        sql = sql + ' ORDER BY _idx ';
      }

      if (model.prototype.dataLimit) {
        sql = sql + ' LIMIT ' + model.prototype.dataLimit;
      }

      //console.log(sql);
      //console.log(params);
      db.readTransaction(function (tx) {
        tx.executeSql(sql, params, function (tr, result) {
          var i, collectionType = OB.Collection[model.prototype.modelName + 'List'] || Backbone.Collection,
              collection = new collectionType(),
              len = result.rows.length;
          if (len === 0) {
            success(collection, args);
          } else {
            for (i = 0; i < len; i++) {
              collection.add(transform(model, result.rows.item(i)));
            }
            success(collection, args);
          }
        }, error);
      });
    } else {
      // localStorage
      throw 'Not implemented';
    }
  }

  function save(model, success, error, forceInsert) {
    var modelProto = model.constructor.prototype,
        tableName = modelProto.tableName,
        sql = '',
        params = null,
        firstParam = true,
        uuid, propertyName, filterVal;

    forceInsert = forceInsert || false;

    if (db) {
      // websql
      if (!tableName) {
        throw 'Missing table name in model';
      }

      if (model.get('id') && forceInsert === false) {
        // UPDATE
        sql = 'UPDATE ' + tableName + ' SET ';

        _.each(_.keys(modelProto.properties), function (attr) {
          propertyName = modelProto.properties[attr];
          if (attr === 'id') {
            return;
          }

          if (firstParam) {
            firstParam = false;
            params = [];
          } else {
            sql = sql + ', ';
          }

          sql = sql + modelProto.propertyMap[propertyName] + ' = ? ';

          params.push(model.get(propertyName));
        });

        if (modelProto.propertiesFilter) {
          filterVal = '';
          _.each(modelProto.propertiesFilter, function (prop) {
            filterVal = filterVal + model.get(prop) + '###';
          });
          sql = sql + ', _filter = ? ';
          params.push(filterVal);
        }

        sql = sql + ' WHERE ' + tableName + '_id = ?';
        params.push(model.get('id'));
      } else {
        // INSERT
        params = [];
        sql = modelProto.insertStatement;
        if (forceInsert === false) {
          uuid = get_uuid();
          params.push(uuid);
          model.set('id', uuid);
        }

        _.each(modelProto.properties, function (property) {
          if (forceInsert === false) {
            if ('id' === property) {
              return;
            }
          }
          params.push(model.get(property) === undefined ? null : model.get(property));
        });

        if (modelProto.propertiesFilter) {
          filterVal = '';
          _.each(modelProto.propertiesFilter, function (prop) {
            filterVal = filterVal + model.get(prop) + '###';
          });
          //Include in the last position but before _idx
          params.splice(params.length - 1, 0, filterVal);
        }

        //console.log(params.length);
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

  function remove(model, success, error) {
    var modelProto = model.constructor.prototype,
        tableName = modelProto.tableName,
        sql = '',
        params = [];

    if (db) {
      // websql
      if (!tableName) {
        throw 'Missing table name in model';
      }

      if (model.get('id')) {
        // UPDATE
        sql = 'DELETE FROM ' + tableName + ' WHERE ' + modelProto.propertyMap.id + ' = ? ';
        params.push(model.get('id'));
      } else {
        throw 'An object without id cannot be deleted';
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

  function removeAll(model, criteria, success, error) {
    var tableName = model.prototype.tableName,
        propertyMap = model.prototype.propertyMap,
        sql, params, whereClause;
    if (db) {
      // websql
      if (!tableName) {
        throw 'Missing table name in model';
      }

      sql = 'DELETE FROM ' + tableName;
      whereClause = getWhereClause(criteria, propertyMap);
      sql = sql + whereClause.sql;
      params = whereClause.params;
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
            success(transform(model, result.rows.item(0)));
          }
        }, error);
      });
    } else {
      // localStorage
      throw 'Not implemented';
    }
  }

  function initCache(model, initialData, success, error, incremental) {
    if (db) {
      if (!model.prototype.createStatement || !model.prototype.dropStatement) {
        throw 'Model requires a create and drop statement';
      }

      if (!initialData) {
        throw 'initialData must be passed as parameter';
      }

      if (!model.prototype.local && !incremental) {
        db.transaction(function (tx) {
          tx.executeSql(model.prototype.dropStatement);
        }, error);
      }

      db.transaction(function (tx) {
        tx.executeSql(model.prototype.createStatement);
      }, error);

      if (_.isArray(initialData)) {
        db.transaction(function (tx) {
          var props = model.prototype.properties,
              propMap = model.prototype.propertyMap,
              filterVal, values, _idx = 0,
              updateRecord = function (tx, model, values) {
              tx.executeSql("DELETE FROM " + model.prototype.tableName + " WHERE " + model.prototype.propertyMap.id + "=?", [values[0]], function () {
                tx.executeSql(model.prototype.insertStatement, values, null, error);
              }, error);
              };

          _.each(initialData, function (item) {
            values = [];

            _.each(props, function (propName) {
              if ('_idx' === propName) {
                return;
              }
              values.push(item[propName]);
            });

            if (model.prototype.propertiesFilter) {
              filterVal = '';
              _.each(model.prototype.propertiesFilter, function (prop) {
                filterVal = filterVal + item[prop] + '###';
              });
              values.push(filterVal);
            }

            values.push(_idx);
            if (incremental) {
              updateRecord(tx, model, values);
            } else {
              tx.executeSql(model.prototype.insertStatement, values, null, error);
            }
            _idx++;
          });
        }, error, function () {
          // transaction success, execute callback
          if (_.isFunction(success)) {
            success();
          }
        });
      } else { // no initial data
        throw 'initialData must be an Array';
      }
    } else {
      throw 'Not implemented';
    }

  }

  window.OB = window.OB || {};

  window.OB.Dal = {
    // constants
    EQ: OP.EQ,
    CONTAINS: OP.CONTAINS,
    STARTSWITH: OP.STARTSWITH,
    ENDSWITH: OP.ENDSWITH,
    // methods
    save: save,
    find: find,
    get: get,
    remove: remove,
    removeAll: removeAll,
    initCache: initCache
  };
}());