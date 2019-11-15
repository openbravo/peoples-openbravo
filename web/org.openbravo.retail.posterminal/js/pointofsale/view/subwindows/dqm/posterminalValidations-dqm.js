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
    static validate(property, value, callback) {
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
    //Validate that email have @ and .*
    var regex = new RegExp(
      /^([a-zA-Z0-9_.-])+@(([a-zA-Z0-9-])+\.)+([a-zA-Z0-9]{2,4})+$/
    );
    if (email === '') {
      return true;
    } else {
      return regex.test(email) ? true : false;
    }
  }

  function validatePhoneFormat(phone) {
    //Validate that phone only have numbers
    var regex = new RegExp(/^([0-9])*$/);
    if (phone === '') {
      return true;
    } else {
      return regex.test(phone) ? true : false;
    }
  }
  OB.DQMController.registerProvider(PosterminalValidations);
})();
