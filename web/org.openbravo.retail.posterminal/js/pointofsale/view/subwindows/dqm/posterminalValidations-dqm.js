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
      return ['phone', 'alternativePhone', 'email'];
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
      }
      callback(result);
    }
    /* @Override */
    static getImplementorSearchKey() {
      return 'OBPOS_EMAILPHONEVALIDATIONS';
    }
  }

  function validateEmailFormat(email) {
    let regex = new RegExp(
      "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:.[a-zA-Z0-9-]+)*$"
    );
    if (email === '') {
      return true;
    } else {
      return regex.test(email) ? true : false;
    }
  }

  function validatePhoneFormat(phone) {
    phone = phone.toString().replace(/\s/g, '');
    let regex = new RegExp(/^([0-9-()+])*$/);
    if (phone === '') {
      return true;
    } else {
      return regex.test(phone) ? true : false;
    }
  }

  OB.DQMController.registerProvider(PosterminalValidations);
})();
