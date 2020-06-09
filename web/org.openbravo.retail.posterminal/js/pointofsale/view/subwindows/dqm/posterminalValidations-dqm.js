/*
 ************************************************************************************
 * Copyright (C) 2019-2020 Openbravo S.L.U.
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
      let functionCallback = function functionCalllback(callbackResult) {
        if (callback) {
          callback(callbackResult);
        }
        return callbackResult;
      };
      switch (property) {
        case 'phone':
          result = validatePhoneFormat(value, functionCallback);
          break;
        case 'alternativePhone':
          result = validatePhoneFormat(value, functionCallback);
          break;
        case 'email':
          result = validateEmailFormat(value, functionCallback);
          break;
        case 'taxID':
          result = validateTaxID(oldCustomer.taxID, value, functionCallback);
          break;
        default:
          result = functionCallback({ status: true });
      }
      return result;
    }
    /* @Override */
    static getImplementorSearchKey() {
      return 'OBPOS_CUSTOMERDETAILSVALIDATIONS';
    }
  }

  function validateEmailFormat(email, callback) {
    let result = { status: true, message: '' },
      regex = new RegExp(
        "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:.[a-zA-Z0-9-]+)*$"
      );
    if (email && !regex.test(email)) {
      result.status = false;
      result.message = OB.I18N.getLabel('OBPOS_WrongFormat');
    }
    return callback(result);
  }

  function validatePhoneFormat(phone, callback) {
    let result = { status: true, message: '' },
      regex = new RegExp(/^([0-9-()+])*$/);
    phone = phone.toString().replace(/\s/g, '');
    if (phone && !regex.test(phone)) {
      result.status = false;
      result.message = OB.I18N.getLabel('OBPOS_WrongFormat');
    }
    return callback(result);
  }

  function validateTaxID(oldValue, newValue, callback) {
    let result = { status: true, message: '' };
    if (!oldValue) {
      result.status = true;
    } else if (!newValue || oldValue !== newValue) {
      result.status = false;
      result.message = OB.I18N.getLabel('OBPOS_TaxIDCannotBeChanged');
    }
    return callback(result);
  }

  OB.DQMController.registerProvider(PosterminalValidations);
})();
