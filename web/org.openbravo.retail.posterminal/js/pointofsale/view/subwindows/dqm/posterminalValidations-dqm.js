/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  class PosterminalValidations extends OB.DQMController
    .CustomerValidatorProvider {
    /* @Override */
    static getValidatedFields() {
      return ['taxID', 'phone', 'alternativePhone', 'email'];
    }
    /* @Override */
    static getSuggestedFields() {
      return [];
    }
    /* @Override */
    static validate(oldCustomer, property, value, callback) {
      let result;
      switch (property) {
        case 'phone':
          return (result = validatePhoneFormat(value));
        case 'alternativePhone':
          return (result = validatePhoneFormat(value));
        case 'email':
          return (result = validateEmailFormat(value));
        case 'taxID':
          return (result = validateTaxID(oldCustomer.taxID, value));
      }
      callback(result);
    }
    /* @Override */
    static getImplementorSearchKey() {
      return 'OBPOS_CUSTOMERDETAILSVALIDATIONS';
    }
  }

  function validateEmailFormat(email) {
    let result = { status: true, message: '' },
      regex = new RegExp(
        "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:.[a-zA-Z0-9-]+)*$"
      );
    if (email && !regex.test(email)) {
      result.status = false;
      result.message = OB.I18N.getLabel('OBPOS_WrongFormat');
    }
    return result;
  }

  function validatePhoneFormat(phone) {
    let result = { status: true, message: '' },
      regex = new RegExp(/^([0-9-()+])*$/);
    phone = phone.toString().replace(/\s/g, '');
    if (phone && !regex.test(phone)) {
      result.status = false;
      result.message = OB.I18N.getLabel('OBPOS_WrongFormat');
    }
    return result;
  }

  function validateTaxID(oldValue, newValue) {
    let result = { status: true, message: '' };
    if (!oldValue) {
      result.status = true;
    } else if (!newValue || oldValue !== newValue) {
      result.status = false;
      result.message = OB.I18N.getLabel('OBPOS_TaxIDCannotBeChanged');
    }
    return result;
  }

  OB.DQMController.registerProvider(PosterminalValidations);
})();
