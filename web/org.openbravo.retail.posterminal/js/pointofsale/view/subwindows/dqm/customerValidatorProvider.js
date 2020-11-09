/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  class CustomerValidatorProvider {
    /**
     * This method must be ALWAYS override in each subclass
     * Returns the list of fields that can be validated
     * Output: Array with Strings with the modelproperty of desired fields
     */
    static getValidatedFields() {
      throw 'getValidatedFields method not implemented';
    }
    /**
     * This method must be ALWAYS override in each subclass
     * Returns the list of fields that can be suggested
     * Output: Array with Strings with the modelproperty of desired fields
     */
    static getSuggestedFields() {
      throw 'getSuggestedFields method not implemented';
    }

    /**
     * This method is OPTIONAL and can be overriden in each subclass
     * Returns the list of suggested fields that can modify values when are selected
     * Output: Array with Strings with the modelproperty of desired fields
     */
    static getOnSuggestSelectedFields() {}
    /**
     * This method must be ALWAYS override in each subclass
     * Implements the logic to validate each field
     * Output: Object contains status (true | false) and message
     */
    static validate(oldCustomer, property, value, callback, formName) {
      throw 'validate method not implemented';
    }
    /**
     * This method must be ALWAYS override in each subclass
     * Implements the logic to suggest each field
     */
    static suggest(oldCustomer, property, value, callback, formName) {
      throw 'suggest method not implemented';
    }
    /**
     * This method is OPTIONAL and can be overriden in each subclass
     * Implements the logic to modify values when a suggested field is selected
     */
    static selectSuggested(
      property,
      data,
      fieldValue,
      field,
      callback,
      formName
    ) {}
    /**
     * This method must be ALWAYS override in each data quality implementor that must be registered
     * Identifies the data quality implementor search key to register in the system
     * Output: String data quality implementor search key
     */
    static getImplementorSearchKey() {
      throw 'getDataQualityImplementor method not implemented';
    }
    /* Each subclass should be registered in the controller:
    OB.DQMController.registerProvider(CustomerValidatorProvider);
     */
  }
  OB.DQMController.CustomerValidatorProvider = CustomerValidatorProvider;
})();
