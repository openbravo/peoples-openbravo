/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use. this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2016 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

// A generic class of a registry to store functions that can be extended
isc.ClassFactory.defineClass('OBFunctionRegistry');

isc.OBFunctionRegistry.addProperties({

  registry: {},

  register: function (registryId, element, callback, id) {
    var registryEntry, i, overwritten = false;

    if (!this.isValidElement(element)) {
      return;
    }

    if (!this.registry[registryId]) {
      this.registry[registryId] = {};
    }

    registryEntry = this.registry[registryId];
    if (!registryEntry[element]) {
      registryEntry[element] = [];
    }

    if (id && !callback.id) {
      callback.id = id;
    }

    // just set a default sort if not defined
    if (callback.sort !== 0 && !callback.sort) {
      callback.sort = 100;
    }

    // check if there is one with the same name and override it
    for (i = 0; i < registryEntry[element].length; i++) {
      if (registryEntry[element][i] && registryEntry[element][i].id === callback.id) {
        registryEntry[element][i] = callback;
        overwritten = true;
        break;
      }
    }

    // add
    if (!overwritten) {
      registryEntry[element].push(callback);
    }

    // and sort according to the sort property
    registryEntry[element].sortByProperty('sort', this.getSortDirection());
  },

  isValidElement: function (element) {
    return true;
  },

  getSortDirection: function () {
    // functions ordered ascending by sort property
    return true;
  },

  call: function (registryId, element, view, form, grid) {
    var callResult, entries = this.getEntries(registryId, element),
        i;

    if (!entries) {
      return;
    }
    for (i = 0; i < entries.length; i++) {
      if (entries[i]) {
        callResult = entries[i](element, view, form, grid);
        if (callResult === false) {
          return;
        }
      }
    }
  },

  getEntries: function (registryId, element) {
    var entry;
    if (!this.registry[registryId]) {
      return;
    }
    entry = this.registry[registryId];
    return entry[element];
  }

});