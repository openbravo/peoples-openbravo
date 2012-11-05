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
  var DocumentSequence = Backbone.Model.extend({
    modelName: 'DocumentSequence',
    tableName: 'c_document_sequence',
    entityName: '',
    source: '',
    local: true,
    properties: ['id', 'posSearchKey', 'documentSequence', 'quotationDocumentSequence'],
    propertyMap: {
      'id': 'c_document_sequence_id',
      'posSearchKey': 'pos_search_key',
      'documentSequence': 'document_sequence',
      'quotationDocumentSequence': 'quotation_document_sequence'
    },

    defaults: {
      documentSequence: 0,
      quotationDocumentSequence: 0
    },

    createStatement: 'CREATE TABLE IF NOT EXISTS c_document_sequence (c_document_sequence_id TEXT PRIMARY KEY, pos_search_key TEXT, document_sequence NUMBER, quotation_document_sequence NUMBER)',
    dropStatement: 'DROP TABLE IF EXISTS c_document_sequence',
    insertStatement: 'INSERT INTO c_document_sequence(c_document_sequence_id, pos_search_key, document_sequence, quotation_document_sequence) VALUES (?,?,?,?)'
  });

  var DocumentSequenceList = Backbone.Collection.extend({
    model: DocumentSequence
  });

  window.OB = window.OB || {};
  window.OB.Model = window.OB.Model || {};
  window.OB.Collection = window.OB.Collection || {};

  window.OB.Model.DocumentSequence = DocumentSequence;
  window.OB.Collection.DocumentSequenceList = DocumentSequenceList;
}());