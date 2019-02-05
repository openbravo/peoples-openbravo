/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB*/

(function () {

  var DocumentSequence = OB.Data.ExtensibleModel.extend({
    modelName: 'DocumentSequence',
    tableName: 'c_document_sequence',
    entityName: '',
    source: '',
    local: true
  });

  DocumentSequence.addProperties([{
    name: 'id',
    column: 'c_document_sequence_id',
    primaryKey: true,
    type: 'TEXT'
  }, {
    name: 'posSearchKey',
    column: 'pos_search_key',
    type: 'TEXT'
  }, {
    name: 'documentSequence',
    column: 'document_sequence',
    type: 'NUMBER'
  }, {
    name: 'quotationDocumentSequence',
    column: 'quotation_document_sequence',
    type: 'NUMBER'
  }, {
    name: 'returnDocumentSequence',
    column: 'return_document_sequence',
    type: 'NUMBER'
  }]);

  OB.Data.Registry.registerModel(DocumentSequence);
}());