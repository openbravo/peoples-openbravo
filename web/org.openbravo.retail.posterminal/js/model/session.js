/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone */

(function () {

  var Session = Backbone.Model.extend({
    modelName: 'Session',
    tableName: 'ad_session',
    entityName: 'Session',
    source: 'org.openbravo.retail.posterminal.Session',
    properties: ['id', 'user', 'terminal', 'active', '_identifier', '_idx'],
    propertyMap: {
      'id': 'ad_session_id',
      'user': 'ad_user_id',
      'terminal': 'terminal',
      'active': 'active',
      '_identifier': '_identifier',
      '_idx': '_idx'
    },
    createStatement: 'CREATE TABLE IF NOT EXISTS ad_session (ad_session_id TEXT PRIMARY KEY , ad_user_id TEXT, terminal TEXT, active TEXT, _identifier TEXT , _idx NUMERIC)',
    dropStatement: 'DROP TABLE IF EXISTS ad_session',
    insertStatement: 'INSERT INTO ad_session(ad_session_id, ad_user_id, terminal, active, _identifier, _idx)  VALUES (?, ?, ?, ?, ?, ?)',
    updateStatement: '',
    local: true
  });

  var SessionList = Backbone.Collection.extend({
    model: Session
  });

  window.OB = window.OB || {};
  window.OB.Model = window.OB.Model || {};
  window.OB.Collection = window.OB.Collection || {};

  window.OB.Model.Session = Session;
  window.OB.Collection.SessionList = SessionList;
}());