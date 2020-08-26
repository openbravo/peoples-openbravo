/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines the TaxEngineError class
 * @author Carlos Aristu <carlos.aristu@openbravo.com>
 */
(function TaxEngineErrorDefinition() {
  /**
   * An error that is thrown when a tax calculation fails
   */
  OB.App.Class.TaxEngineError = class TaxEngineError extends Error {
    constructor(message, messageParams) {
      super(message);
      this.name = 'TaxEngineError';
      this.data = {
        message,
        messageParams
      };
    }
  };
})();
